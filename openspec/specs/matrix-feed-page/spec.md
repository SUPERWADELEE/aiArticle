# matrix-feed-page Specification

## Purpose

TBD - created by archiving change 'add-matrix-feed-and-author-pages'. Update Purpose after archive.

## Requirements

### Requirement: Matrix-styled feed route

The system SHALL expose a `GET /feed` route that renders a Matrix-themed paginated article list page using the `templates/feed.html` Thymeleaf template.

#### Scenario: First page request

- **WHEN** an unauthenticated user issues `GET /feed` with no `page` query parameter
- **THEN** the system returns HTTP 200 rendering `feed.html` with `currentPage = 0`, `totalPages = ceil(totalArticles / 20)`, and the first 20 articles ordered by `publishedAt DESC, createdAt DESC`

#### Scenario: Specific page request

- **WHEN** the user requests `GET /feed?page=2`
- **THEN** the system returns HTTP 200 with `currentPage = 2` and articles 41-60 in the same ordering

#### Scenario: Negative page parameter

- **WHEN** the user requests `GET /feed?page=-3`
- **THEN** the system clamps `page` to `0` and returns the first page

#### Scenario: Page beyond total

- **WHEN** the user requests `GET /feed?page=999` and only 30 articles exist
- **THEN** the system returns HTTP 200 with an empty article list, `currentPage = 999`, and `totalPages = 2`


<!-- @trace
source: add-matrix-feed-and-author-pages
updated: 2026-05-07
code:
  - src/main/resources/templates/fragments/matrix-header.html
  - src/main/resources/templates/article.html
  - src/main/resources/templates/layout/matrix-base.html
  - src/main/resources/templates/fragments/matrix-footer.html
  - src/main/resources/templates/feed.html
  - CLAUDE.md
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/author-profile.html
  - src/main/java/com/example/aidigest/controller/ViewController.java
tests:
  - src/test/java/com/example/aidigest/util/AuthorLookupServiceTest.java
-->

---
### Requirement: Matrix theme assets are loaded only by feed and author pages

The system SHALL render `feed.html` with `<head th:replace="~{layout/matrix-base :: head}">` so the Matrix-styled head fragment (Tailwind CDN, Space Mono Google Font, dark theme inline CSS) is loaded, while the existing `layout/base.html`, `index.html`, `source-list.html`, `article.html`, `fragments/header.html`, and `fragments/footer.html` MUST remain unmodified.

#### Scenario: Feed page styling

- **WHEN** the rendered HTML of `/feed` is inspected
- **THEN** the document head contains the Tailwind CDN script tag AND a `<link>` tag fetching the `Space Mono` Google Font
- **AND** `body` background resolves to `#0D0D0D` and primary text color is `#00FF41`

#### Scenario: Existing pages are not affected

- **WHEN** the rendered HTML of `/`, `/sources/{src}`, or `/articles/{id}` is inspected after this change
- **THEN** the body class continues to be `bg-slate-50 text-slate-900` and Space Mono is NOT loaded


<!-- @trace
source: add-matrix-feed-and-author-pages
updated: 2026-05-07
code:
  - src/main/resources/templates/fragments/matrix-header.html
  - src/main/resources/templates/article.html
  - src/main/resources/templates/layout/matrix-base.html
  - src/main/resources/templates/fragments/matrix-footer.html
  - src/main/resources/templates/feed.html
  - CLAUDE.md
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/author-profile.html
  - src/main/java/com/example/aidigest/controller/ViewController.java
tests:
  - src/test/java/com/example/aidigest/util/AuthorLookupServiceTest.java
-->

---
### Requirement: Feed page exposes the model attributes required by the template

The system SHALL populate the model with `articles`, `currentPage`, `totalPages`, `topAuthors`, `feedStats`, `pageTitle`, `pageDescription`, `canonicalPath`, `ogType`, and `jsonLd` before rendering `feed.html`.

#### Scenario: Top authors aggregation for current page

- **WHEN** the feed page renders with the current page's articles
- **THEN** `topAuthors` contains at most 5 entries, each with `handle`, `name`, `title`, and `articleCount`, sorted by `articleCount DESC`, computed by mapping each article through `AuthorLookupService.find(article)` and grouping by handle

#### Scenario: Feed stats values

- **WHEN** the feed page renders
- **THEN** `feedStats` is a map containing `totalArticles` (= `articleRepository.count()`), `authorsTracked` (= `AuthorTrackerProperties.handles().size()`), and `lastSync` (= the rendering instant formatted as `HH:mm UTC`)

#### Scenario: SEO metadata

- **WHEN** the feed page renders
- **THEN** `pageTitle` equals `"AI_FEED | " + siteName`, `canonicalPath` equals `"/feed"`, `ogType` equals `"website"`, and `jsonLd` is a non-empty JSON-LD `WebSite` document


<!-- @trace
source: add-matrix-feed-and-author-pages
updated: 2026-05-07
code:
  - src/main/resources/templates/fragments/matrix-header.html
  - src/main/resources/templates/article.html
  - src/main/resources/templates/layout/matrix-base.html
  - src/main/resources/templates/fragments/matrix-footer.html
  - src/main/resources/templates/feed.html
  - CLAUDE.md
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/author-profile.html
  - src/main/java/com/example/aidigest/controller/ViewController.java
tests:
  - src/test/java/com/example/aidigest/util/AuthorLookupServiceTest.java
-->

---
### Requirement: Feed page navigates to existing article detail and source pages

The system SHALL render each article card with a link to `/articles/{id}` and the source badge text via `@sourceNameMapper.display(article.source)`, reusing existing helpers without modifying them.

#### Scenario: Click article title

- **WHEN** the user clicks the title of an article card on `/feed`
- **THEN** the browser navigates to `/articles/{article.id}` using the existing controller route

#### Scenario: Top author entry click

- **WHEN** the user clicks an entry in the Top Authors sidebar of `/feed`
- **THEN** the browser navigates to `/authors/{handle}` using the lower-cased handle from `AuthorTrackerProperties`


<!-- @trace
source: add-matrix-feed-and-author-pages
updated: 2026-05-07
code:
  - src/main/resources/templates/fragments/matrix-header.html
  - src/main/resources/templates/article.html
  - src/main/resources/templates/layout/matrix-base.html
  - src/main/resources/templates/fragments/matrix-footer.html
  - src/main/resources/templates/feed.html
  - CLAUDE.md
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/author-profile.html
  - src/main/java/com/example/aidigest/controller/ViewController.java
tests:
  - src/test/java/com/example/aidigest/util/AuthorLookupServiceTest.java
-->

---
### Requirement: Decorative interactions are visually present but non-functional

The system SHALL render the filter bar, tag chips, and `view_all_authors` button as static visual elements with `href="#"` and `aria-disabled="true"`, with no backend handling required.

#### Scenario: Filter input has no submit handler

- **WHEN** the rendered HTML of `/feed` is inspected
- **THEN** the filter bar element is NOT inside a `<form>` element AND NOT bound to any controller action

#### Scenario: Disabled link semantics

- **WHEN** the rendered HTML of `/feed` is inspected
- **THEN** every `<a>` whose intended functionality is deferred has `aria-disabled="true"` and `href="#"`


<!-- @trace
source: add-matrix-feed-and-author-pages
updated: 2026-05-07
code:
  - src/main/resources/templates/fragments/matrix-header.html
  - src/main/resources/templates/article.html
  - src/main/resources/templates/layout/matrix-base.html
  - src/main/resources/templates/fragments/matrix-footer.html
  - src/main/resources/templates/feed.html
  - CLAUDE.md
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/author-profile.html
  - src/main/java/com/example/aidigest/controller/ViewController.java
tests:
  - src/test/java/com/example/aidigest/util/AuthorLookupServiceTest.java
-->

---
### Requirement: NEW badge is shown for recent articles

The system SHALL display a `● NEW` badge on each article card whose `publishedAt` is within the last 24 hours of the rendering instant.

#### Scenario: Recent article shows badge

- **WHEN** an article has `publishedAt` 6 hours before the rendering instant
- **THEN** its card includes the `● NEW` badge element

#### Scenario: Older article hides badge

- **WHEN** an article has `publishedAt` 30 hours before the rendering instant or `publishedAt` is null
- **THEN** its card does NOT include the `● NEW` badge element

<!-- @trace
source: add-matrix-feed-and-author-pages
updated: 2026-05-07
code:
  - src/main/resources/templates/fragments/matrix-header.html
  - src/main/resources/templates/article.html
  - src/main/resources/templates/layout/matrix-base.html
  - src/main/resources/templates/fragments/matrix-footer.html
  - src/main/resources/templates/feed.html
  - CLAUDE.md
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/author-profile.html
  - src/main/java/com/example/aidigest/controller/ViewController.java
tests:
  - src/test/java/com/example/aidigest/util/AuthorLookupServiceTest.java
-->