# author-profile-page Specification

## Purpose

TBD - created by archiving change 'add-matrix-feed-and-author-pages'. Update Purpose after archive.

## Requirements

### Requirement: Author profile route by handle

The system SHALL expose a `GET /authors/{handle}` route that renders a Matrix-themed author profile page using the `templates/author-profile.html` Thymeleaf template, where `{handle}` is matched against `AuthorTrackerProperties.handles()` case-insensitively.

#### Scenario: Known handle in lower case

- **WHEN** the user requests `GET /authors/sundar-pichai` and `application.yml` configures a handle `sundar-pichai`
- **THEN** the system returns HTTP 200 rendering `author-profile.html` with the matching `Handle` record bound as `author`

#### Scenario: Known handle with mixed case

- **WHEN** the user requests `GET /authors/Sundar-Pichai` and the configured handle is `sundar-pichai`
- **THEN** the system returns HTTP 200 with the same matching `Handle`

#### Scenario: Unknown handle returns 404

- **WHEN** the user requests `GET /authors/unknown-person` and no configured handle matches
- **THEN** the system returns HTTP 404 with status reason `"Author not found"`


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
### Requirement: AuthorLookupService supports by-handle reverse lookup

The system SHALL extend `AuthorLookupService` with two public methods: `Optional<Handle> findByHandle(String handle)` and `List<Article> articlesFor(Handle target, List<Article> all)`.

#### Scenario: findByHandle returns matching handle

- **WHEN** `findByHandle("sundar-pichai")` is called and the service holds a handle `Sundar-Pichai`
- **THEN** the returned `Optional` contains the matching `Handle`

#### Scenario: findByHandle handles null and unknown

- **WHEN** `findByHandle(null)` or `findByHandle("nobody")` is called
- **THEN** the returned `Optional` is empty

#### Scenario: articlesFor filters by mapped handle

- **WHEN** `articlesFor(handle, allArticles)` is called with an `allArticles` list of 50 articles, of which 8 map back to `handle` via `find(article)`
- **THEN** the returned list contains exactly those 8 articles in their original order


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
### Requirement: Author profile model attributes

The system SHALL populate the model with `author`, `authorPath`, `authorArticles`, `articlesThisMonth`, `relatedAuthors`, `pageTitle`, `pageDescription`, `canonicalPath`, `ogType`, and `jsonLd` before rendering `author-profile.html`.

#### Scenario: Author articles list

- **WHEN** the page renders for handle `sundar-pichai`
- **THEN** `authorArticles` is the result of calling `AuthorLookupService.articlesFor(author, allArticles)` where `allArticles` is the most recent 500 articles ordered by `publishedAt DESC, createdAt DESC`

#### Scenario: Articles this month count

- **WHEN** the page renders at UTC time `T`
- **THEN** `articlesThisMonth` equals the count of `authorArticles` whose `publishedAt` falls within the same calendar month as `T` in UTC

#### Scenario: Related authors selection

- **WHEN** the page renders for a handle
- **THEN** `relatedAuthors` contains up to 3 entries from `AuthorTrackerProperties.handles()`, excluding the current handle, in the order they appear in configuration

#### Scenario: Author path string

- **WHEN** the page renders for handle `sundar-pichai`
- **THEN** `authorPath` equals `"~/authors/sundar-pichai"`

#### Scenario: SEO metadata

- **WHEN** the page renders for an author
- **THEN** `pageTitle` equals `author.name() + " | " + siteName`, `canonicalPath` equals `"/authors/" + handle.toLowerCase()`, `ogType` equals `"profile"`, and `jsonLd` is a non-empty JSON-LD document of type `Person`


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
### Requirement: Activity feed entries link to existing article detail

The system SHALL render the activity feed terminal section with the first 5 entries of `authorArticles`, each entry linking the title to `/articles/{id}` and showing the source via `@sourceNameMapper.display(article.source)`.

#### Scenario: Activity feed has up to 5 entries

- **WHEN** `authorArticles` contains 12 articles
- **THEN** the activity feed renders exactly 5 entry blocks (the first 5 by feed order)

#### Scenario: Activity feed empty state

- **WHEN** `authorArticles` is empty
- **THEN** the activity feed renders a single `[ no entries yet ]` placeholder line and no entry blocks

#### Scenario: Load more link target

- **WHEN** the user clicks the `[ press ENTER to load more entries... ]` link
- **THEN** the browser navigates to `/feed`


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
### Requirement: Author hero shows configured metadata only

The system SHALL display only data sourced from `AuthorTrackerProperties.Handle` (`handle`, `name`, `title`) plus computed `authorArticles.size()` and `articlesThisMonth` in the hero. No additional `Handle` fields are introduced by this change.

#### Scenario: Bio section composition

- **WHEN** the hero renders
- **THEN** the `$ cat bio.txt` block displays exactly the text `${author.name()} 是 ${author.title()}.` with no additional sentences from any data source

#### Scenario: Static decorative elements

- **WHEN** the hero renders
- **THEN** the avatar block, follow button, `last_active` text, and ascii decoration lines render with hard-coded content from the template, with NO controller-supplied data binding for these elements


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
### Requirement: Related authors sidebar links to author profile pages

The system SHALL render each entry in the related authors sidebar as a link to `/authors/{handle}` using the lower-cased handle from `AuthorTrackerProperties`.

#### Scenario: Related author entry click

- **WHEN** the user clicks a related author entry on `/authors/sundar-pichai`
- **THEN** the browser navigates to `/authors/{otherHandle}` using the configured handle of the other author

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