## ADDED Requirements

### Requirement: Scheduled daily execution

The system SHALL execute the digest process automatically every day at 08:00 using Spring Scheduler with cron expression `0 0 8 * * *`.

#### Scenario: Daily trigger at 8 AM

- **WHEN** the system clock reaches 08:00
- **THEN** the digest process is triggered automatically

### Requirement: Manual trigger via REST API

The system SHALL provide a REST endpoint `GET /api/fetch` that triggers the digest process on demand and returns the result.

#### Scenario: Manual trigger

- **WHEN** a client sends GET request to `/api/fetch`
- **THEN** the system executes the full digest pipeline and returns the list of newly processed articles

### Requirement: Article listing via REST API

The system SHALL provide a REST endpoint `GET /api/articles` that returns all stored articles ordered by creation date descending.

#### Scenario: List all articles

- **WHEN** a client sends GET request to `/api/articles`
- **THEN** the system returns all articles from the database ordered by createdAt descending

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
