## ADDED Requirements

### Requirement: Parallel RSS feed fetching

The system SHALL fetch articles from multiple RSS sources concurrently using CompletableFuture. Each RSS source SHALL be fetched in a separate async task submitted to a dedicated thread pool. The system SHALL support RSS 2.0 and Atom feed formats.

#### Scenario: Successful parallel fetch from all sources

- **WHEN** the digest process is triggered
- **THEN** the system fetches all configured RSS sources in parallel and returns a combined list of articles with title, URL, source name, content, and published date

#### Scenario: One source fails while others succeed

- **WHEN** one RSS source is unreachable or returns invalid XML
- **THEN** the system logs the error for that source and returns articles from all other successful sources without interruption

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
