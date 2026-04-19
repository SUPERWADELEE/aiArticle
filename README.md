# AI Daily Digest

每日自動抓取 AI/LLM 相關技術文章，透過 Groq LLM 產生繁體中文摘要，並經由 Telegram Bot 推播。

## 技術棧

- Java 21 + Spring Boot 3.4
- Spring Data JPA + PostgreSQL
- CompletableFuture 並行抓取
- Rome RSS Parser
- Google Gemini API (gemini-2.0-flash)
- Telegram Bot API

## 環境變數

| 變數 | 說明 | 必填 |
|------|------|------|
| `GEMINI_API_KEY` | Gemini API 金鑰，從 https://aistudio.google.com 取得 | ✅ |
| `TELEGRAM_BOT_TOKEN` | Telegram Bot Token，從 @BotFather 取得 | ✅ |
| `TELEGRAM_CHAT_ID` | 接收訊息的 Chat ID | ✅ |
| `DB_HOST` | PostgreSQL 主機（預設 localhost） | |
| `DB_PORT` | PostgreSQL 連接埠（預設 5432） | |
| `DB_NAME` | 資料庫名稱（預設 aidigest） | |
| `DB_USER` | 資料庫使用者（預設 postgres） | |
| `DB_PASSWORD` | 資料庫密碼（預設 postgres） | |

## 快速啟動

### 使用 Docker Compose（推薦）

```bash
# 設定環境變數
export GEMINI_API_KEY=your_gemini_api_key
export TELEGRAM_BOT_TOKEN=your_bot_token
export TELEGRAM_CHAT_ID=your_chat_id

# 一鍵啟動
docker-compose up -d
```

### 本地開發

```bash
# 1. 啟動 PostgreSQL
docker-compose up -d db

# 2. 設定環境變數
export GEMINI_API_KEY=your_gemini_api_key
export TELEGRAM_BOT_TOKEN=your_bot_token
export TELEGRAM_CHAT_ID=your_chat_id

# 3. 啟動應用
./mvnw spring-boot:run
```

## API

| Endpoint | 說明 |
|----------|------|
| `GET /api/fetch` | 手動觸發抓取流程 |
| `GET /api/articles` | 查詢所有已儲存文章 |

## 排程

每天早上 08:00 自動執行抓取流程。

## RSS 來源

- Hacker News AI 相關
- dev.to AI tag
- Anthropic Blog
- OpenAI Blog

## 測試

```bash
./mvnw test
```
