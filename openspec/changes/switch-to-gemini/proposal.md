## Why

將 LLM 摘要供應商從 Groq API 切換為 Google Gemini 原生 API（gemini-2.0-flash 模型）。Gemini 提供充足的免費額度（每分鐘 15 次），且用戶已決定使用 Gemini 原生 `generateContent` endpoint 而非 OpenAI-compatible 模式。

## What Changes

- **BREAKING** 將 `GroqSummaryService` 替換為 `GeminiSummaryService`，使用 Gemini 原生 API 格式
- 環境變數從 `GROQ_API_KEY` 改為 `GEMINI_API_KEY`
- 設定從 `app.groq.*` 改為 `app.gemini.*`
- API request/response 格式改為 Gemini 原生（`generateContent` endpoint）

## Capabilities

### New Capabilities

（無）

### Modified Capabilities

- `article-summarization`: 將 LLM API 從 Groq (OpenAI-compatible) 改為 Gemini 原生 API，包含不同的 endpoint URL、request body 格式、response 解析、及 API key 傳遞方式

## Impact

- **程式碼**: `GroqSummaryService.java`（刪除）→ `GeminiSummaryService.java`（新建）、`DigestService.java`（更新注入）
- **設定**: `application.yml`、`src/test/resources/application.yml`
- **部署**: `docker-compose.yml`（環境變數更新）
- **文件**: `README.md`、`pom.xml` description
