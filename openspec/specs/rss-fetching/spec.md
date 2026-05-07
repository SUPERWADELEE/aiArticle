# rss-fetching Specification

## Purpose

TBD - created by archiving change 'ai-daily-digest'. Update Purpose after archive.

## Requirements

### Requirement: Parallel RSS feed fetching

The system SHALL fetch articles from multiple RSS sources concurrently using CompletableFuture. Each RSS source SHALL be fetched in a separate async task submitted to a dedicated thread pool. The system SHALL support RSS 2.0 and Atom feed formats.

#### Scenario: Successful parallel fetch from all sources

- **WHEN** the digest process is triggered
- **THEN** the system fetches all configured RSS sources in parallel and returns a combined list of articles with title, URL, source name, content, and published date

#### Scenario: One source fails while others succeed

- **WHEN** one RSS source is unreachable or returns invalid XML
- **THEN** the system logs the error for that source and returns articles from all other successful sources without interruption


<!-- @trace
source: ai-daily-digest
updated: 2026-04-21
code:
  - Dockerfile
  - src/main/resources/static/google39a0c0554813bf32.html
  - src/main/java/com/example/aidigest/service/KeywordFilterService.java
  - src/main/java/com/example/aidigest/service/IndexNowService.java
  - src/main/java/com/example/aidigest/config/AuthorTrackerProperties.java
  - .spectra.yaml
  - src/main/resources/templates/layout/base.html
  - src/main/java/com/example/aidigest/scheduler/AuthorTrackerScheduler.java
  - src/main/java/com/example/aidigest/config/AppProperties.java
  - .mvn/wrapper/maven-wrapper.properties
  - src/main/java/com/example/aidigest/service/TrendingAiFetcher.java
  - src/main/java/com/example/aidigest/repository/ArticleRepository.java
  - src/main/resources/templates/source-list.html
  - README.md
  - src/main/java/com/example/aidigest/service/AuthorActivityTracker.java
  - pom.xml
  - src/main/java/com/example/aidigest/model/CrawlLog.java
  - src/main/java/com/example/aidigest/service/DigestService.java
  - src/main/java/com/example/aidigest/config/AppConfig.java
  - scripts/run-all.sh
  - src/main/resources/templates/fragments/header.html
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - scripts/author-check.sh
  - src/main/java/com/example/aidigest/service/GroqSummaryService.java
  - scripts/fetch.sh
  - src/main/java/com/example/aidigest/model/Article.java
  - src/main/java/com/example/aidigest/AiDigestApplication.java
  - mvnw
  - src/main/java/com/example/aidigest/controller/DigestController.java
  - src/main/java/com/example/aidigest/controller/SeoController.java
  - src/main/java/com/example/aidigest/repository/CrawlLogRepository.java
  - nginx/ai-article.elitecombat.dev.conf
  - src/main/resources/application.yml
  - .dockerignore
  - src/main/java/com/example/aidigest/model/CrawlStatus.java
  - src/main/java/com/example/aidigest/service/RssFetchService.java
  - docker-compose.yml
  - src/main/java/com/example/aidigest/util/SourceNameMapper.java
  - src/main/java/com/example/aidigest/config/TrendingAiProperties.java
  - src/main/java/com/example/aidigest/service/GoogleCseSearchService.java
  - .github/workflows/deploy.yml
  - src/main/java/com/example/aidigest/service/TelegramService.java
  - src/main/java/com/example/aidigest/util/SlugUtil.java
  - src/main/java/com/example/aidigest/controller/ViewController.java
  - src/main/resources/templates/article.html
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/fragments/footer.html
  - src/main/resources/templates/index.html
  - src/main/java/com/example/aidigest/scheduler/DailyDigestScheduler.java
  - src/main/java/com/example/aidigest/model/ArticleDto.java
tests:
  - src/test/java/com/example/aidigest/service/KeywordFilterServiceTest.java
  - src/test/resources/application.yml
-->

---
### Requirement: Configurable RSS sources

The system SHALL read the list of RSS source URLs from application configuration (application.yml). The default sources SHALL be:
- `https://hnrss.org/newest?q=AI+LLM+claude+chatgpt&count=20`
- `https://dev.to/feed/tag/ai`
- `https://www.anthropic.com/rss.xml`
- `https://openai.com/blog/rss.xml`

#### Scenario: Sources loaded from configuration

- **WHEN** the application starts
- **THEN** RSS source URLs are read from `app.rss.sources` in application.yml

#### Scenario: Source list is modified

- **WHEN** a new RSS URL is added to the configuration
- **THEN** the system fetches from the new source on the next execution without code changes

<!-- @trace
source: ai-daily-digest
updated: 2026-04-21
code:
  - Dockerfile
  - src/main/resources/static/google39a0c0554813bf32.html
  - src/main/java/com/example/aidigest/service/KeywordFilterService.java
  - src/main/java/com/example/aidigest/service/IndexNowService.java
  - src/main/java/com/example/aidigest/config/AuthorTrackerProperties.java
  - .spectra.yaml
  - src/main/resources/templates/layout/base.html
  - src/main/java/com/example/aidigest/scheduler/AuthorTrackerScheduler.java
  - src/main/java/com/example/aidigest/config/AppProperties.java
  - .mvn/wrapper/maven-wrapper.properties
  - src/main/java/com/example/aidigest/service/TrendingAiFetcher.java
  - src/main/java/com/example/aidigest/repository/ArticleRepository.java
  - src/main/resources/templates/source-list.html
  - README.md
  - src/main/java/com/example/aidigest/service/AuthorActivityTracker.java
  - pom.xml
  - src/main/java/com/example/aidigest/model/CrawlLog.java
  - src/main/java/com/example/aidigest/service/DigestService.java
  - src/main/java/com/example/aidigest/config/AppConfig.java
  - scripts/run-all.sh
  - src/main/resources/templates/fragments/header.html
  - src/main/java/com/example/aidigest/util/AuthorLookupService.java
  - scripts/author-check.sh
  - src/main/java/com/example/aidigest/service/GroqSummaryService.java
  - scripts/fetch.sh
  - src/main/java/com/example/aidigest/model/Article.java
  - src/main/java/com/example/aidigest/AiDigestApplication.java
  - mvnw
  - src/main/java/com/example/aidigest/controller/DigestController.java
  - src/main/java/com/example/aidigest/controller/SeoController.java
  - src/main/java/com/example/aidigest/repository/CrawlLogRepository.java
  - nginx/ai-article.elitecombat.dev.conf
  - src/main/resources/application.yml
  - .dockerignore
  - src/main/java/com/example/aidigest/model/CrawlStatus.java
  - src/main/java/com/example/aidigest/service/RssFetchService.java
  - docker-compose.yml
  - src/main/java/com/example/aidigest/util/SourceNameMapper.java
  - src/main/java/com/example/aidigest/config/TrendingAiProperties.java
  - src/main/java/com/example/aidigest/service/GoogleCseSearchService.java
  - .github/workflows/deploy.yml
  - src/main/java/com/example/aidigest/service/TelegramService.java
  - src/main/java/com/example/aidigest/util/SlugUtil.java
  - src/main/java/com/example/aidigest/controller/ViewController.java
  - src/main/resources/templates/article.html
  - src/main/java/com/example/aidigest/util/JsonLdBuilder.java
  - src/main/resources/templates/fragments/footer.html
  - src/main/resources/templates/index.html
  - src/main/java/com/example/aidigest/scheduler/DailyDigestScheduler.java
  - src/main/java/com/example/aidigest/model/ArticleDto.java
tests:
  - src/test/java/com/example/aidigest/service/KeywordFilterServiceTest.java
  - src/test/resources/application.yml
-->