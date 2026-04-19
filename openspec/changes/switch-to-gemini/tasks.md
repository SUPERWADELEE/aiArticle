## 1. Gemini Service 實作

- [x] 1.1 新建 GeminiSummaryService，使用 Gemini 原生 generateContent endpoint 實作 Chinese summary generation via Groq API（修改後的 requirement），包含 API key 未設定時的處理
- [x] 1.2 刪除 GroqSummaryService.java

## 2. 設定與注入更新

- [x] [P] 2.1 更新 application.yml：`app.groq.*` → `app.gemini.*`，環境變數改為 GEMINI_API_KEY，model 改為 gemini-2.0-flash
- [x] [P] 2.2 更新 src/test/resources/application.yml：同步 gemini 設定
- [x] [P] 2.3 更新 DigestService.java：將 GroqSummaryService 注入改為 GeminiSummaryService
- [x] [P] 2.4 更新 docker-compose.yml：環境變數 GROQ_API_KEY → GEMINI_API_KEY

## 3. 文件更新

- [x] [P] 3.1 更新 README.md：環境變數表格和技術棧描述
- [x] [P] 3.2 更新 pom.xml description（Groq → Gemini）

## 4. 驗證

- [x] 4.1 執行 ./mvnw test 確認既有測試通過
