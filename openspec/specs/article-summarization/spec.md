# article-summarization Specification

## Purpose

TBD - created by archiving change 'switch-to-gemini'. Update Purpose after archive.

## Requirements

### Requirement: Chinese summary generation via Groq API

The system SHALL call the Groq API (OpenAI-compatible endpoint at `https://api.groq.com/openai/v1/chat/completions`) using the `llama-3.3-70b-versatile` model to generate a Traditional Chinese summary for each article. The prompt SHALL instruct the model to summarize in 3 sentences focusing on technical content and practical applications.

#### Scenario: Successful summary generation

- **WHEN** an article with title and content is sent to Groq API
- **THEN** the system receives a Traditional Chinese summary of approximately 3 sentences

#### Scenario: Groq API call fails

- **WHEN** the Groq API returns an error or is unreachable for a specific article
- **THEN** the system sets that article's summary to "摘要生成失敗" and continues processing remaining articles


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
### Requirement: Article persistence

The system SHALL store each processed article in PostgreSQL with the following fields: id (auto-generated), title, url (unique), source, content, summary, publishedAt, and createdAt (auto-set to current timestamp).

#### Scenario: Article saved successfully

- **WHEN** an article has been summarized
- **THEN** the article and its summary are persisted to the database with all fields populated

#### Scenario: Article with failed summary is still saved

- **WHEN** summary generation fails for an article
- **THEN** the article is still saved with summary set to "摘要生成失敗"

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