## ADDED Requirements

### Requirement: Chinese summary generation via Groq API

The system SHALL call the Groq API (OpenAI-compatible endpoint at `https://api.groq.com/openai/v1/chat/completions`) using the `llama-3.3-70b-versatile` model to generate a Traditional Chinese summary for each article. The prompt SHALL instruct the model to summarize in 3 sentences focusing on technical content and practical applications.

#### Scenario: Successful summary generation

- **WHEN** an article with title and content is sent to Groq API
- **THEN** the system receives a Traditional Chinese summary of approximately 3 sentences

#### Scenario: Groq API call fails

- **WHEN** the Groq API returns an error or is unreachable for a specific article
- **THEN** the system sets that article's summary to "摘要生成失敗" and continues processing remaining articles

### Requirement: Article persistence

The system SHALL store each processed article in PostgreSQL with the following fields: id (auto-generated), title, url (unique), source, content, summary, publishedAt, and createdAt (auto-set to current timestamp).

#### Scenario: Article saved successfully

- **WHEN** an article has been summarized
- **THEN** the article and its summary are persisted to the database with all fields populated

#### Scenario: Article with failed summary is still saved

- **WHEN** summary generation fails for an article
- **THEN** the article is still saved with summary set to "摘要生成失敗"
