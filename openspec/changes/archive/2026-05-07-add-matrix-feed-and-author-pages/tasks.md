## 1. AuthorLookupService 擴充

- [x] 1.1 在 AuthorLookupService supports by-handle reverse lookup（呼應 Decision 3: by-handle 文章過濾走 in-memory，不加 repo query）：在 `src/main/java/com/example/aidigest/util/AuthorLookupService.java` 加入 public `Optional<Handle> findByHandle(String handle)`（內部 lower-case，null/未知回 `Optional.empty()`）與 public `List<Article> articlesFor(Handle target, List<Article> all)`（用既有 `find(article)` 比對 handle 並保留原順序）。
- [x] 1.2 在 `src/test/java/com/example/aidigest/util/AuthorLookupServiceTest.java` 新增單元測試覆蓋 `findByHandle`（null、未知、大小寫不一致三種 case）與 `articlesFor`（命中 / 未命中 / 順序保持）。

## 2. Matrix 版面資源（layout 與 fragments）

- [x] 2.1 建立 `src/main/resources/templates/layout/matrix-base.html`（呼應 Decision 1: 新增 `layout/matrix-base.html` 而非擴充 `layout/base.html`），含 `<head th:fragment="head">`：載入 Tailwind CDN、`Space Mono` Google Fonts、`<style>` 區塊設定 `body { background:#0D0D0D; color:#00FF41; font-family:'Space Mono', monospace }`、共用 `.line-clamp-3` utility，並沿用既有 SEO meta 結構（`pageTitle`、canonical、og:*、jsonLd）—— 對應 Matrix theme assets are loaded only by feed and author pages 的需求。
- [x] 2.2 建立 `src/main/resources/templates/fragments/matrix-header.html`：實作設計稿 nav (logo `> AI_FEED` + 游標、navLinks `--explore`/`--authors`/`--topics`/`--bookmarks`、LIVE indicator)。`--explore` 連到 `/`、`--authors` 連到 `/feed`、`--topics` 與 `--bookmarks` 套用 Decision 6: 已知失效連結 (`--bookmarks`、`--topics`、follow、view-all-authors) 用 `href="#"` + `aria-disabled="true"`。
- [x] 2.3 建立 `src/main/resources/templates/fragments/matrix-footer.html`：底部 status bar，左側顯示版本/PID 靜態文字、右側 UTC 時間透過 `#temporals.format(#temporals.createNow(),'yyyy-MM-dd HH:mm:ss')` 動態渲染。

## 3. Feed 頁面（/feed）

- [x] 3.1 在 `src/main/java/com/example/aidigest/controller/ViewController.java` 注入 `AuthorLookupService` 與 `AuthorTrackerProperties`，新增 `GET /feed` handler 對應 Matrix-styled feed route（呼應 Decision 2: `/feed` 與既有 `/` 並存，不互相取代）：clamp `page < 0`、PAGE_SIZE 沿用 20、ordering 同既有首頁、設定 `pageTitle = "AI_FEED | " + siteName`、`canonicalPath = "/feed"`、`ogType = "website"`、`jsonLd = JsonLdBuilder.website(...)`。
- [x] 3.2 在 `/feed` handler 內計算 Feed page exposes the model attributes required by the template 所列欄位（呼應 Decision 4: Top Authors / Feed Stats 用「當頁文章 + 全表 distinct authors」混合計算）：把當頁 articles 經 `authorLookup.find(a)` group by handle 取 top 5 為 `topAuthors`（每筆 `handle/name/title/articleCount`）；`feedStats` map 含 `totalArticles = articleRepository.count()`、`authorsTracked = trackerProperties.handles().size()`、`lastSync = #temporals.format(now,'HH:mm') + " UTC"`。
- [x] 3.3 建立 `src/main/resources/templates/feed.html`：head 用 `~{layout/matrix-base :: head}`、body 套用 matrix header/footer fragment，組裝 hero（靜態 ascii）、filter bar（無 `<form>`，input 純視覺）、文章卡片列、右側 sidebar（Top Authors / Feed Stats / view-all-authors 按鈕），實作 Decorative interactions are visually present but non-functional（filter / view_all_authors / tag chips 全部 `href="#"` + `aria-disabled="true"`）以及 NEW badge is shown for recent articles（`th:if` 用 `#temporals.between(article.publishedAt, #temporals.createNow()).toHours() < 24` 判斷）。
- [x] 3.4 在 `feed.html` 的卡片連結與 source badge 部分套用 Feed page navigates to existing article detail and source pages：標題連到 `@{'/articles/' + ${article.id}}`、source 透過 `@sourceNameMapper.display(article.source)`、作者 chip 用 `@authorLookup.displayWithTitle(article)`、sidebar Top Authors 條目連到 `@{'/authors/' + ${entry.handle}}`，分頁列連到 `@{/feed(page=${currentPage - 1})}` / `@{/feed(page=${currentPage + 1})}`。

## 4. Author Profile 頁面（/authors/{handle}）

- [x] 4.1 在 `ViewController` 新增 `GET /authors/{handle}` handler 對應 Author profile route by handle：透過 `authorLookup.findByHandle(handle)` 反查；查無時 `throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found")`。
- [x] 4.2 在 `/authors/{handle}` handler 內準備 Author profile model attributes：`author = handle`、`authorPath = "~/authors/" + handle.toLowerCase()`、`authorArticles = authorLookup.articlesFor(author, articleRepository.findAllByOrderByPublishedAtDescCreatedAtDesc(PageRequest.of(0,500)).getContent())`、`articlesThisMonth` 用 UTC `Instant -> ZonedDateTime` 取月份過濾、`relatedAuthors = trackerProperties.handles().stream().filter(h -> !h.handle().equalsIgnoreCase(handle)).limit(3).toList()`、`pageTitle = author.name() + " | " + siteName`、`canonicalPath = "/authors/" + handle.toLowerCase()`、`ogType = "profile"`、`jsonLd = JsonLdBuilder.person(...)` (新方法或沿用 collectionPage)。
- [x] 4.3 建立 `src/main/resources/templates/author-profile.html`：head 用 `~{layout/matrix-base :: head}`、套用 matrix header/footer。實作 Author hero shows configured metadata only（呼應 Decision 5: 不擴 `Handle` schema，bio 用 `title` 合成）：avatar 區塊靜態、author info 顯示 `${authorPath}` / `${author.name()}` / `// ${author.title()}` badge / 合成 bio 文字 `${author.name()} 是 ${author.title()}.`、stats 列 articles=`${authorArticles.size()}` 與 this_month=`${articlesThisMonth}`，topics=12 靜態；右側 follow 按鈕、`last_active`、ascii 裝飾線完全靜態。
- [x] 4.4 在 `author-profile.html` 內實作 Activity feed entries link to existing article detail：tab bar (`activity.log` 預設 active) 與 terminal 標題列、`th:each="article, iter : ${authorArticles}" th:if="${iter.index < 5}"` 列出前 5 筆條目（每條：時間 + source + 標題連到 `@{'/articles/' + ${article.id}}` + `>> src: ...`），`authorArticles` 為空時顯示 `[ no entries yet ]`，load more 連到 `/feed`。
- [x] 4.5 在 `author-profile.html` 的 sidebar 內實作 Related authors sidebar links to author profile pages：`th:each` 渲染 `${relatedAuthors}`，每筆連到 `@{'/authors/' + ${other.handle}}`，加入 `// matrix.stream()` Code Rain 與 `$ ls ./topics` 兩個靜態裝飾區塊以保留設計稿視覺。

## 5. 驗證與回歸

- [x] 5.1 跑 `./mvnw spring-boot:run` 啟動站台，瀏覽 `/feed`、`/authors/{某設定 handle}`：確認黑底螢光綠 + Space Mono、卡片數 = 20、分頁可點、未知 handle 回 404、卡片內連結跳轉 `/articles/{id}` 正常；同時開啟 `/`、`/sources/{src}`、`/articles/{id}` 確認既有頁面仍是淺色 indigo 主題、view-source 沒有意外載入 Space Mono。
- [x] 5.2 用 pencil MCP `get_screenshot` 抓 `1L2rT` 與 `udMo7` 設計稿，與瀏覽器並排比對 hero、卡片、sidebar 三個區塊；色差或間距大的部位回頭微調 inline style 與 Tailwind arbitrary value class。
