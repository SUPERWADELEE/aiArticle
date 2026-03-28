## ADDED Requirements

### Requirement: Telegram daily digest notification

The system SHALL send a formatted digest message to a configured Telegram chat via the Telegram Bot API (`https://api.telegram.org/bot{token}/sendMessage`). The message SHALL use Markdown formatting and include each article's title, summary, and link.

#### Scenario: Digest with articles

- **WHEN** the digest process produces 1 or more summarized articles
- **THEN** the system sends a Telegram message containing the article titles, summaries, and links

#### Scenario: No new articles

- **WHEN** the digest process produces zero new articles after filtering and deduplication
- **THEN** the system does not send a Telegram message

### Requirement: Message splitting for long content

The system SHALL split the Telegram message into multiple messages if the total length exceeds 4096 characters (Telegram's per-message limit).

#### Scenario: Message within limit

- **WHEN** the formatted digest is 4096 characters or fewer
- **THEN** the system sends it as a single Telegram message

#### Scenario: Message exceeds limit

- **WHEN** the formatted digest exceeds 4096 characters
- **THEN** the system splits the content into multiple messages, each within the 4096-character limit, and sends them sequentially
