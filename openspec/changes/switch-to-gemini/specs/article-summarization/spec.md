## MODIFIED Requirements

### Requirement: Chinese summary generation via Groq API

The system SHALL call the Google Gemini API (native `generateContent` endpoint at `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`) using the `gemini-2.0-flash` model to generate a Traditional Chinese summary for each article. The API key SHALL be passed as a query parameter (`key={apiKey}`). The request body SHALL use Gemini native format with `contents` array containing `parts`. The prompt SHALL instruct the model to summarize in 3 sentences focusing on technical content and practical applications.

#### Scenario: Successful summary generation

- **WHEN** an article with title and content is sent to Gemini API
- **THEN** the system receives a Traditional Chinese summary of approximately 3 sentences, extracted from `response.candidates[0].content.parts[0].text`

#### Scenario: Gemini API call fails

- **WHEN** the Gemini API returns an error or is unreachable for a specific article
- **THEN** the system sets that article's summary to "摘要生成失敗" and continues processing remaining articles

#### Scenario: API key not configured

- **WHEN** the GEMINI_API_KEY environment variable is empty or not set
- **THEN** the system skips summarization and sets summary to "摘要生成失敗"
