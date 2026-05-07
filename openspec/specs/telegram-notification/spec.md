# telegram-notification Specification

## Purpose

TBD - created by archiving change 'ai-daily-digest'. Update Purpose after archive.

## Requirements

### Requirement: Telegram daily digest notification

The system SHALL send a formatted digest message to a configured Telegram chat via the Telegram Bot API (`https://api.telegram.org/bot{token}/sendMessage`). The message SHALL use Markdown formatting and include each article's title, summary, and link.

#### Scenario: Digest with articles

- **WHEN** the digest process produces 1 or more summarized articles
- **THEN** the system sends a Telegram message containing the article titles, summaries, and links

#### Scenario: No new articles

- **WHEN** the digest process produces zero new articles after filtering and deduplication
- **THEN** the system does not send a Telegram message


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
### Requirement: Message splitting for long content

The system SHALL split the Telegram message into multiple messages if the total length exceeds 4096 characters (Telegram's per-message limit).

#### Scenario: Message within limit

- **WHEN** the formatted digest is 4096 characters or fewer
- **THEN** the system sends it as a single Telegram message

#### Scenario: Message exceeds limit

- **WHEN** the formatted digest exceeds 4096 characters
- **THEN** the system splits the content into multiple messages, each within the 4096-character limit, and sends them sequentially

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