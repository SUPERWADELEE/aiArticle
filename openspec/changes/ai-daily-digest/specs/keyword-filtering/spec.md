## ADDED Requirements

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

### Requirement: URL-based deduplication

The system SHALL skip articles whose URL already exists in the database. Deduplication SHALL use the article URL as the unique identifier.

#### Scenario: New article URL

- **WHEN** a fetched article URL does not exist in the database
- **THEN** the article proceeds to summarization

#### Scenario: Duplicate article URL

- **WHEN** a fetched article URL already exists in the database
- **THEN** the article is skipped and not sent for summarization
