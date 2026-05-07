# digest-orchestration Specification

## Purpose

TBD - created by archiving change 'ai-daily-digest'. Update Purpose after archive.

## Requirements

### Requirement: Scheduled daily execution

The system SHALL execute the digest process automatically every day at 08:00 using Spring Scheduler with cron expression `0 0 8 * * *`.

#### Scenario: Daily trigger at 8 AM

- **WHEN** the system clock reaches 08:00
- **THEN** the digest process is triggered automatically


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
### Requirement: Manual trigger via REST API

The system SHALL provide a REST endpoint `GET /api/fetch` that triggers the digest process on demand and returns the result.

#### Scenario: Manual trigger

- **WHEN** a client sends GET request to `/api/fetch`
- **THEN** the system executes the full digest pipeline and returns the list of newly processed articles


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
### Requirement: Article listing via REST API

The system SHALL provide a REST endpoint `GET /api/articles` that returns all stored articles ordered by creation date descending.

#### Scenario: List all articles

- **WHEN** a client sends GET request to `/api/articles`
- **THEN** the system returns all articles from the database ordered by createdAt descending


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
### Requirement: End-to-end digest pipeline

The system SHALL orchestrate the digest process in the following order:
1. Fetch articles from all RSS sources in parallel
2. Filter by keywords
3. Deduplicate by URL against existing database records
4. Generate summaries via Groq API
5. Persist articles to database
6. Send Telegram notification

#### Scenario: Full pipeline execution

- **WHEN** the digest process is triggered (by scheduler or manual API call)
- **THEN** the system executes all six steps in order and completes the digest cycle

#### Scenario: Pipeline continues on partial failure

- **WHEN** some RSS sources fail or some summaries fail
- **THEN** the system processes all successful articles and sends a notification with whatever articles were successfully processed

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