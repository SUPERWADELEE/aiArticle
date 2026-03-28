## 1. 專案初始化

- [ ] 1.1 建立 pom.xml，設定 Java 21、Spring Boot 3.4.x 及所有依賴（spring-boot-starter-web、spring-boot-starter-data-jpa、postgresql、Rome RSS parser、spring-boot-starter-test）
- [ ] 1.2 建立 AiDigestApplication 主類別（@SpringBootApplication + @EnableScheduling）
- [ ] 1.3 建立 AppConfig 設定類別：RestClient bean 及自訂 Executor thread pool（對應 design 決策「HTTP Client 使用 Spring RestClient」及「並行抓取使用 CompletableFuture + 自訂 Executor」）
- [ ] 1.4 建立 application.yml，設定資料庫連線、Groq API Key、Telegram Bot Token、RSS sources 清單及 keywords 清單（對應 requirement「Configurable RSS sources」）

## 2. 資料模型與持久層

- [ ] 2.1 建立 Article entity（id、title、url unique、source、content、summary、publishedAt、createdAt），對應 requirement「Article persistence」及 design 決策「資料庫 Schema 使用 JPA auto-ddl」
- [ ] 2.2 建立 ArticleDto response DTO
- [ ] 2.3 建立 ArticleRepository（existsByUrl、findAllByOrderByCreatedAtDesc）

## 3. RSS 抓取

- [ ] 3.1 實作 RssFetchService，使用 CompletableFuture.supplyAsync 並行抓取所有 RSS 來源，RSS 解析使用 Rome (com.rometools:rome)（對應 requirement「Parallel RSS feed fetching」）
- [ ] 3.2 實作每個 source 獨立 try-catch 錯誤處理，單一來源失敗不影響其他

## 4. 關鍵字過濾與去重

- [ ] 4.1 實作 KeywordFilterService，case-insensitive 比對標題和內容中的關鍵字（對應 requirement「Keyword-based article filtering」）
- [ ] 4.2 實作 URL-based deduplication 邏輯，查詢資料庫跳過已存在的文章（對應 requirement「URL-based deduplication」）

## 5. Groq 摘要

- [ ] 5.1 實作 GroqSummaryService，呼叫 Groq API OpenAI-compatible endpoint 產生繁體中文摘要（對應 requirement「Chinese summary generation via Groq API」及 design 決策「Groq API 使用 OpenAI-compatible 格式」）
- [ ] 5.2 實作 API 呼叫失敗時將 summary 設為「摘要生成失敗」的錯誤處理

## 6. Telegram 推播

- [ ] 6.1 實作 TelegramService，格式化 Markdown 訊息並呼叫 Telegram Bot API（對應 requirement「Telegram daily digest notification」）
- [ ] 6.2 實作 message splitting for long content，超過 4096 字元時分段發送（對應 requirement「Message splitting for long content」）

## 7. 流程編排與觸發

- [ ] 7.1 實作 DigestService.runDigest()，編排完整的 end-to-end digest pipeline（對應 requirement「End-to-end digest pipeline」）
- [ ] 7.2 實作 DailyDigestScheduler，cron 排程每天 08:00 觸發（對應 requirement「Scheduled daily execution」）
- [ ] 7.3 實作 DigestController：GET /api/fetch 手動觸發（對應 requirement「Manual trigger via REST API」）及 GET /api/articles 查詢文章列表（對應 requirement「Article listing via REST API」）

## 8. 部署與測試

- [ ] 8.1 建立 Dockerfile（multi-stage build）和 docker-compose.yml（PostgreSQL 16 + app）
- [ ] 8.2 撰寫 KeywordFilterServiceTest 單元測試（至少涵蓋 keyword matching 和 case-insensitive 場景）
- [ ] 8.3 撰寫 README.md，說明環境變數設定與啟動方式
