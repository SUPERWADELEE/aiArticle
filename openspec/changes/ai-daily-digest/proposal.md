## Why

建立一個每日 AI 技術文章摘要推播工具，自動從多個 RSS 來源抓取 AI/LLM 相關文章，透過 Groq LLM 產生繁體中文摘要，並經由 Telegram Bot 推播給使用者。這是一個 Java Spring Boot 學習專案，同時也是實際會使用的生產力工具。

## What Changes

- 建立全新的 Spring Boot 3 專案，使用 Java 21
- 新增 RSS 並行抓取功能（CompletableFuture），支援 4 個 RSS 來源
- 新增關鍵字過濾機制，只保留 AI/LLM 相關文章
- 整合 Groq API（llama-3.3-70b-versatile）產生繁體中文摘要
- 整合 Telegram Bot API 推播每日摘要
- 使用 Spring Data JPA + PostgreSQL 儲存文章與摘要
- 提供 REST API 手動觸發抓取與查詢文章
- 設定 Spring Scheduler 每天早上 8 點自動執行
- 提供 Docker Compose 一鍵啟動環境

## Capabilities

### New Capabilities

- `rss-fetching`: 並行抓取多個 RSS 來源並解析文章內容
- `keyword-filtering`: 依關鍵字過濾 AI/LLM 相關文章並以 URL 去重
- `article-summarization`: 呼叫 Groq API 為每篇文章產生繁體中文摘要
- `telegram-notification`: 透過 Telegram Bot 推播每日摘要訊息
- `digest-orchestration`: 編排整個抓取→過濾→摘要→儲存→推播流程，支援排程與手動觸發

### Modified Capabilities

（無，這是全新專案）

## Impact

- **新增程式碼**: Spring Boot 專案完整結構（controller、service、repository、model、config、scheduler）
- **外部依賴**: Spring Boot 3.4.x、Rome RSS parser、PostgreSQL driver、Groq API、Telegram Bot API
- **基礎設施**: 需要 PostgreSQL 資料庫（透過 Docker Compose 提供）
- **外部服務**: Groq API（需 API Key）、Telegram Bot（需 Bot Token 和 Chat ID）
