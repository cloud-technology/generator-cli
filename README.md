# Spring Boot 專案產生工具

這個開源專案提供了一個全面的程式碼產生工具，旨在簡化 Spring Boot 應用程序的建立過程。它無縫整合了 JOOQ、OpenAPI、JPA 以及 Liquibase，適合需要快速啟動並遵循最佳實踐的 Spring Boot 項目開發者。

## 特性

- **Spring Boot 整合**：生成包含必要配置和依賴的 Spring Boot 項目。
- **JOOQ Codegen**：自動基於數據庫結構產生 JPA 實體。
- **OpenAPI 規範**：根據 OpenAPI 規範生成 API 文檔和服務器框架。
- **Spring Data JPA**：產生 Spring Data 資料訪問介面。
- **Liquibase 遷移**：使用 Liquibase 管理數據庫模式更改。

## 開始使用

### 前提條件

- JDK 17 或更高版本
- Gradle
- Docker（用於某些功能，例如 Testcontainers）

### 安裝

1. 複製倉庫：

```bash
git clone https://github.com/cloud-technology/generator-cli.git
```

2. 切換到項目目錄：

```bash
cd generator-cli
```

3. 使用 Gradle 構建項目：

```bash
sdk use java 17.0.10-librca
./gradlew --no-daemon clean build
cp ./build/libs/generator-cli-0.0.1.jar .
```

### 使用方法

在您的工作目錄中創建一個 `application.yml` 配置文件，並填入以下內容：

```yaml
cli:
  build-tool: gradle
  group-id: com.example
  artifact-id: demo
  name: demo
  description: Demo project for Spring Boot
  package-name: com.example.demo
  runtime: cloud-run
  openapiFilePath: /path/to/your/openapi.yaml
  dbUrl: jdbc:postgresql://localhost:5432/mydatabase
  dbUsername: myuser
  dbPassword: secret
```

確保替換 `openapiFilePath` 中的路徑為您的 OpenAPI 規範文件實際路徑。

然後，執行以下命令以啟動代碼生成過程：

```bash
java -jar ./generator-cli-0.0.1.jar generator
```

## 項目結構

項目包含數個關鍵的包和類：

- `generator.cli`：含有啟動項目生成過程的 CLI 介面。
- `generator.configuration.properties`：定義應用程序的屬性和配置選項。
- `generator.service`：實現生成項目構件的邏輯。
- `generator.bo`：定義了整個應用中使用的業務對象和枚舉。

## 貢獻

歡迎社區成員的貢獻！如果您有興趣幫助改善此工具，請查看我們的貢獻指南。

## 許可證

此專案是開源的，根據 [APACHE LICENSE, VERSION 2.0](https://www.apache.org/licenses/LICENSE-2.0) 發布。





