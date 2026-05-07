## Why

`ai-digest2.pen` 設計稿已經完成兩個新頁面的視覺：Article List Page (`1L2rT`) 與 Author Profile Page (`udMo7`)，採用「黑底螢光綠 Matrix / 終端機」風格作為站台的探索型入口。現有 Spring Boot + Thymeleaf 站台只提供首頁、來源列表、單篇文章三個頁面，缺乏「以作者為中心」的瀏覽路徑與一個更具品牌記憶點的文章探索頁。本次把這兩頁從設計稿落地，補齊作者維度的瀏覽，同時把 Matrix 視覺語彙納入站台。

## What Changes

- 新增 `/feed` 路由：以 Matrix 風格呈現分頁文章列表（hero、filter bar 視覺、文章卡片、Top Authors / Feed Stats 側欄）。資料接 `ArticleRepository` 既有的分頁查詢。
- 新增 `/authors/{handle}` 路由：以 Matrix 風格呈現單一作者的 hero（avatar 裝飾、name、title、文章統計）、活動 feed（該作者的文章）與 sidebar（code rain、topics、related authors）。Author 元資料來自 `AuthorTrackerProperties.handles`，文章列表用 `AuthorLookupService` 過濾既有 articles。
- 新增 Matrix 主題的 Thymeleaf 版面資源：`layout/matrix-base.html`（含 Tailwind CDN + Space Mono Google Font + 主題色 inline CSS）、`fragments/matrix-header.html`、`fragments/matrix-footer.html`。
- 擴充 `AuthorLookupService`：加入 `findByHandle(String)` 與 `articlesFor(Handle, List<Article>)` 兩個 helper。
- 既有 `index.html`、`source-list.html`、`article.html` 與既有 `fragments/header.html` / `fragments/footer.html` 不修改，保留淺色 slate+indigo 主題。

## Non-Goals (optional)

- 不實作 filter bar、follow 按鈕、tag 點擊、bookmarks、view-all-authors 等互動功能；這些先以靜態視覺呈現，未來再分次處理。
- 不擴充 `AuthorTrackerProperties.Handle` schema（不加 bio 欄位）；author 描述以 `title` 為主，不接資料庫的 bio。
- 不更動既有頁面的視覺風格與既有 `fragments/header.html`、`fragments/footer.html`。
- 不引入 Tailwind build pipeline / PostCSS；維持 CDN + arbitrary value class 用法。
- 不為 Article 增加 by-author 的 repository query；先沿用 `findAllByOrderByPublishedAtDescCreatedAtDesc` + `AuthorLookupService` 在記憶體過濾，文章量大時再優化。

## Capabilities

### New Capabilities

- `matrix-feed-page`: 提供 `/feed` 的 Matrix 風格分頁文章列表頁，包含 hero、靜態 filter bar、卡片列、Top Authors 與 Feed Stats 側欄。
- `author-profile-page`: 提供 `/authors/{handle}` 的 Matrix 風格作者頁，含 author hero、該作者文章活動 feed、related authors 側欄；包含 `AuthorLookupService` 對 by-handle 反查的擴充。

### Modified Capabilities

(none)

## Impact

- Affected specs: `matrix-feed-page` (新)、`author-profile-page` (新)
- Affected code:
  - 新增 `src/main/resources/templates/feed.html`
  - 新增 `src/main/resources/templates/author-profile.html`
  - 新增 `src/main/resources/templates/layout/matrix-base.html`
  - 新增 `src/main/resources/templates/fragments/matrix-header.html`
  - 新增 `src/main/resources/templates/fragments/matrix-footer.html`
  - 修改 `src/main/java/com/example/aidigest/controller/ViewController.java`（注入 `AuthorLookupService`、`AuthorTrackerProperties`，新增 `/feed`、`/authors/{handle}` handler）
  - 修改 `src/main/java/com/example/aidigest/util/AuthorLookupService.java`（新增 `findByHandle`、`articlesFor`）
- 不影響既有 `/`、`/sources/{source}`、`/articles/{id}` 路由與資料流。
- 外部依賴：使用 Google Fonts CDN 載入 Space Mono；無新增 Maven 套件。
