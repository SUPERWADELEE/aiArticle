# keyword-filtering Specification

## Purpose

TBD - created by archiving change 'ai-daily-digest'. Update Purpose after archive.

## Requirements

### Requirement: Keyword-based article filtering

The system SHALL filter articles by checking whether the title or content contains at least one of the configured keywords. Matching SHALL be case-insensitive. The default keyword list SHALL be: claude, llm, gpt, gemini, ai agent, rag, fine-tune, vector, embedding, mcp.

#### Scenario: Article matches a keyword in title

- **WHEN** an article has "New Claude Model Released" as its title
- **THEN** the article passes the keyword filter because "claude" is in the keyword list

#### Scenario: Article matches a keyword in content

- **WHEN** an article has a generic title but its content mentions "RAG pipeline"
- **THEN** the article passes the keyword filter because "rag" is in the keyword list

#### Scenario: Article matches no keywords

- **WHEN** an article title is "JavaScript Framework Update" and content has no configured keywords
- **THEN** the article is excluded from the digest

#### Scenario: Case-insensitive matching

- **WHEN** an article title contains "LLM" in uppercase
- **THEN** the article passes the filter because matching is case-insensitive against "llm"


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
### Requirement: URL-based deduplication

The system SHALL skip articles whose URL already exists in the database. Deduplication SHALL use the article URL as the unique identifier.

#### Scenario: New article URL

- **WHEN** a fetched article URL does not exist in the database
- **THEN** the article proceeds to summarization

#### Scenario: Duplicate article URL

- **WHEN** a fetched article URL already exists in the database
- **THEN** the article is skipped and not sent for summarization

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