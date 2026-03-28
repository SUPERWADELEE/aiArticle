## Context

這是一個全新的 Spring Boot 3 專案，目標是每日自動抓取 AI/LLM 相關文章，用 Groq LLM 生成繁體中文摘要，並透過 Telegram 推播。專案同時作為學習 Java Spring Boot 的練習，因此選用標準分層架構。目前沒有任何既有程式碼。

## Goals / Non-Goals

**Goals:**

- 提供完整的每日 AI 文章摘要推播功能
- 使用 CompletableFuture 練習 Java 並行處理
- 標準 Spring Boot 分層架構（controller / service / repository / model）
- Docker Compose 一鍵啟動，降低環境設定門檻
- 所有敏感設定透過環境變數管理

**Non-Goals:**

- 不做前端 UI，僅提供 REST API
- 不做使用者認證/授權
- 不做文章全文爬取（僅使用 RSS feed 提供的內容）
- 不做多使用者支援（單一 Telegram chat 推播）
- 不做摘要品質評估或重試優化

## Decisions

### RSS 解析使用 Rome (com.rometools:rome)

Rome 是 Java 生態系中最成熟的 RSS/Atom 解析器，支援 RSS 1.0/2.0 和 Atom 格式，社群維護穩定。相較於手動解析 XML 或使用較冷門的 library，Rome 的 API 簡潔且文件完整。

### HTTP Client 使用 Spring RestClient

Spring 6 引入的 RestClient 是 RestTemplate 的現代替代品，fluent API 更直覺。相較於 WebClient（reactive），RestClient 是同步的，更適合學習且足以應對本專案的呼叫量。

### 並行抓取使用 CompletableFuture + 自訂 Executor

使用 `CompletableFuture.supplyAsync(task, executor)` 搭配固定大小的 thread pool（4 threads），平行抓取所有 RSS 來源。相較於 `@Async` 註解，直接使用 CompletableFuture 更明確地練習多執行緒概念。

### 資料庫 Schema 使用 JPA auto-ddl

開發階段使用 `hibernate.ddl-auto: update` 自動建表。生產環境建議改用 Flyway，但本專案暫不引入以降低複雜度。

### Groq API 使用 OpenAI-compatible 格式

Groq 提供 OpenAI-compatible 的 REST API（`/v1/chat/completions`），直接用 RestClient 建構 JSON request body 呼叫，不需引入額外 SDK。

## Risks / Trade-offs

- **[RSS 來源不穩定]** → 每個 source 獨立 try-catch，單一來源失敗不影響其他來源；失敗時記錄 log 繼續執行
- **[Groq API 速率限制]** → 逐篇呼叫而非並行，避免觸發 rate limit；失敗時將 summary 設為「摘要生成失敗」
- **[Telegram 訊息長度限制 4096 字元]** → 訊息超長時自動分段發送
- **[RSS 內容可能不含全文]** → 部分 RSS 僅提供摘要，摘要品質取決於 feed 內容品質，不做額外爬取
- **[ddl-auto: update 的風險]** → 適用於開發階段，欄位改動可能不會自動刪除舊欄位；生產建議改用 migration tool
