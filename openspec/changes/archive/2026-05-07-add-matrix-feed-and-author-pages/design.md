## Context

現有站台 (`com.example.aidigest`) 是 Spring Boot 3.4 + Thymeleaf + Tailwind CDN，三個既有頁面（`/`、`/sources/{src}`、`/articles/{id}`）共用同一個 head fragment `templates/layout/base.html` 與 `templates/fragments/header.html`、`fragments/footer.html`，視覺為淺色 slate + indigo。

設計稿 `ai-digest2.pen` 內兩個頁面採用「Matrix 終端機」深色風格（#0D0D0D 背景、#00FF41 文字、Space Mono 等寬字、ascii 邊框），與現行視覺完全相反。提案明確要求只在新頁面 `/feed`、`/authors/{handle}` 套用 Matrix 風，舊頁面不動。

資料層方面：

- `ArticleRepository` 已有 `findAllByOrderByPublishedAtDescCreatedAtDesc(Pageable)` 與 `findBySource...`；**沒有** `findByAuthor*`。
- `AuthorLookupService.find(Article)` 把 article 比對回 `AuthorTrackerProperties.Handle`；**沒有反向 by-handle 查詢**。
- `AuthorTrackerProperties.Handle` 只有 `handle / name / title` 三欄，**沒有 bio**。

## Goals / Non-Goals

**Goals:**

- 新頁面 1:1 還原設計稿視覺結構與配色，可在不引入 build pipeline 的前提下完成。
- 新版面資源（matrix-base / matrix-header / matrix-footer）與既有 light theme 資源並存，互不污染（既有頁面的字型 / 顏色不被改動）。
- 新增的兩個 controller method 沿用既有的 `@ModelAttribute` 全域變數機制與 SEO meta 欄位（`pageTitle`、`pageDescription`、`canonicalPath`、`ogType`、`jsonLd`）。
- `/authors/{handle}` 對未知 handle 回 404（與現有 `/articles/{id}`、`/sources/{src}` 行為一致）。

**Non-Goals:**

- 不抽象出「主題切換系統」；只是兩套並存的 head fragment。
- 不為 `Article` 加 by-author 的 JPA query；先在 controller 內以 `AuthorLookupService` 過濾。
- 不實作 filter bar、follow、bookmarks 互動（純視覺）。
- 不修改既有三個 templates / 兩個 fragments / 既有 controller method。

## Decisions

### Decision 1: 新增 `layout/matrix-base.html` 而非擴充 `layout/base.html`

**選擇**：另開一個 head fragment 給 Matrix 主題。新 templates 用 `<head th:replace="~{layout/matrix-base :: head}">`。

**理由**：

- 既有 `layout/base.html` 的 `body { font-family: ... sans-serif }` 是寫死在 `<style>`，若要做主題參數化必須改 fragment 簽章 + 既有三個 templates 的呼叫端，違反「不動既有頁面」的 non-goal。
- 兩套 head fragment 共用 Tailwind CDN URL 與 SEO meta 欄位結構，差異只在字型 + 顏色 + 額外的 utility CSS；複製 + 改一份成本低於導入主題機制。
- 將來若再多兩三個 Matrix 頁面，沿用同一個 fragment 即可，無重構壓力。

**替代方案**：在 `base.html` 加 `theme` Thymeleaf 變數做條件 class —— 否決，因為要動到既有三個 template 並引入額外的全域 model attribute。

### Decision 2: `/feed` 與既有 `/` 並存，不互相取代

**選擇**：保留 `/` 為既有 indigo 風格的首頁；`/feed` 作為新 Matrix 風列表入口。

**理由**：

- 兩個頁面同樣是 article list，但設計目的不同：`/` 偏「乾淨的閱讀首頁」（featured + secondary），`/feed` 偏「探索 + 互動 hooks」（filter bar、Top Authors sidebar）。
- 提案 non-goal 已排除動既有頁，所以不能用 `/` 重定向。
- 路徑 `/feed` 對應設計稿 logo `> AI_FEED` 的命名語彙。
- 之後若決定收斂為單一首頁，可再做一個 archive change 處理。

**替代方案**：把 `/` 換成新版、舊版下到 `/classic` —— 否決，與 non-goal 衝突。

### Decision 3: by-handle 文章過濾走 in-memory，不加 repo query

**選擇**：在 `AuthorLookupService` 加 `articlesFor(Handle, List<Article>)`，在 controller 內 `articleRepository.findAllByOrderByPublishedAtDescCreatedAtDesc(PageRequest.of(0, 500))` 後過濾。

**理由**：

- `AuthorLookupService.find(Article)` 是用「name 模糊比對 + last name + handle 包含」三段式判斷，無法直接翻成單一 SQL where 子句。要讓 query 與 in-memory 比對結果一致，得把比對邏輯搬進 native query 或建索引欄位，工作量大且超出本次 scope。
- 一頁 500 筆做迴圈 + map lookup 在文章量級數萬以下都不會是瓶頸（每筆 lookup O(1)~O(handles)）。
- 程式裡留 `// optimize when articles exceed 5000` 註記。

**替代方案 A**：加 `findByAuthorContainingIgnoreCase` JPA 方法 —— 否決，因為 `Article.author` 欄位常是 RSS 給的雜亂字串，與 `Handle.name` 不一定字面相同（既有 `AuthorLookupService` 才能對應）。
**替代方案 B**：寫 native query + 為 article 預先計算 handle —— 否決，schema migration 與比對邏輯重複，超出 scope。

### Decision 4: Top Authors / Feed Stats 用「當頁文章 + 全表 distinct authors」混合計算

**選擇**：

- `topAuthors`：在 `/feed` 載入當頁 articles 時，透過 `authorLookup.find(a)` group by handle，取前 5 名（可能因為頁面差異而結果浮動）。
- `feedStats`：`total_articles = articleRepository.count()`、`authors_tracked = trackerProperties.handles().size()`、`last_sync` 取目前時間（暫定，未來接 `CrawlLogRepository`）。

**理由**：

- 真正的「全站 top authors by count」需要 group by + 反向比對，成本高；當頁統計足以呈現視覺。
- `feedStats` 只是版面裝飾，先給合理數字即可。
- 預期將來會出現專屬服務（如 `AuthorActivityTracker` 已存在）能提供更準確的統計，介面預留 model 變數即可。

**替代方案**：純靜態寫死 —— 否決，因為設計稿的這塊資訊會被使用者實際當索引看，給真實數字成本不高。

### Decision 5: 不擴 `Handle` schema，bio 用 `title` 合成

**選擇**：author hero 的 `$ cat bio.txt` 區塊內容組合為 `${author.name()} 是 ${author.title()}.`，不增加 bio 欄位。

**理由**：

- 提案 non-goal 已排除 schema 擴充。
- `application.yml` 已經填好的 handles 數量有限，要回填 bio 是另一個獨立工作。

### Decision 6: 已知失效連結 (`--bookmarks`、`--topics`、follow、view-all-authors) 用 `href="#"` + `aria-disabled="true"`

**選擇**：保留視覺不刪元素；連結用 `href="#"` 並加 `aria-disabled="true"` class `pointer-events-auto opacity-70`。

**理由**：

- 設計稿視覺要求這些元素存在；移除會破壞排版。
- `aria-disabled` 對輔助技術明確標示「目前不可用」。
- 之後實作該功能時，只需把 `href` 換成真實路由 + 移除屬性。

## Risks / Trade-offs

- **[字型載入抖動]** Google Fonts CDN 在首次載入會 FOUT（fallback 字型先閃過）→ 用 `display=swap`，並讓 fallback 也是 monospace；不致影響可讀性。
- **[Tailwind CDN 與 arbitrary value]** Tailwind Play CDN 支援 arbitrary value class（`bg-[#0D0D0D]`），但部分情境下 JIT 編譯需要花時間 → 接受短暫 unstyled flash；若日後有問題再評估改 PostCSS build。
- **[in-memory 作者過濾的擴展性]** 文章量達數萬筆時，`/authors/{handle}` 每次拉 500 筆做 in-memory filter 會吃 CPU/RAM → Decision 3 已說明後續可改 native query。
- **[Top Authors 浮動]** `/feed` 翻頁時 sidebar Top Authors 排序會跟著當頁變動，可能造成混淆 → 視覺上仍可接受；之後做全站統計再修。
- **[Matrix 風與既有風落差]** 從 `/` 點到 `/feed` 視覺切換劇烈 → 是設計刻意的對比，不在這次解決；可在後續加過場或做 onboarding 提示。
- **[/authors/{handle} 對 unknown handle 的 404]** 必須測試 lower/upper case，避免「Sundar-Pichai」連到 404；`AuthorLookupService.findByHandle` 內部已 `toLowerCase`。
