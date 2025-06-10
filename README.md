<a href="https://studio.firebase.google.com/import?url=https%3A%2F%2Fgithub.com%2Fcloud-technology%2Fgenerator-cli">
  <img
    height="32"
    alt="Open in Firebase Studio"
    src="https://cdn.firebasestudio.dev/btn/open_light_32.svg">
</a>

# Spring Boot 專案產生工具

這個開源專案提供了一個全面的程式碼產生工具，旨在簡化 Spring Boot 應用程序的建立過程。它無縫整合了 JOOQ、OpenAPI、JPA 以及 Liquibase，適合需要快速啟動並遵循最佳實踐的 Spring Boot 項目開發者。

## 特性

- **Spring Boot 整合**：生成包含必要配置和依賴的 Spring Boot 項目。
- **JOOQ Codegen**：自動基於數據庫結構產生 JPA 實體。
- **OpenAPI 規範**：根據 OpenAPI 規範生成 API 文檔和服務器框架。
- **Spring Data JPA**：產生 Spring Data 資料訪問介面。
- **Liquibase 遷移**：使用 Liquibase 管理數據庫模式更改。

## 開始使用

### Firebase Studio 使用

啟動資料庫, 啟動後會透過 `dev-resources/test/test.sql` 初始化資料庫

```bash
docker compose up -d
```

可透過 `compose.yaml` 的資訊, 連線資料庫  

使用以下命令下載並安裝 `generator-cli`

```bash
# 下載最新版本
curl -f -L -o generator-cli-linux-x86_64.zip "https://github.com/cloud-technology/generator-cli/releases/download/v20250421.1/generator-cli-linux-x86_64.zip"

# 解壓縮
unzip generator-cli-linux-x86_64.zip

# 執行產生專案
./generator-cli-linux-x86_64 generator
```

對於 macOS 用戶，使用 Intel 架構的 Mac 電腦請下載 `generator-cli-darwin-x86_64.zip`, 使用 Apple Silicon 架構的 Mac 電腦請下載 `generator-cli-darwin-aarch64.zip`

### 方法二：從源代碼構建

如果您想從源代碼構建，請按照以下步驟操作：

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
cp ./build/libs/generator-cli-0.0.1.jar ./dev-resources/test
cd ./dev-resources/test
java -jar ./generator-cli-0.0.1.jar generator
```

使用 native build
``` bash
sdk use java 21.0.2-graalce
./gradlew --no-daemon clean nativeCompile
cp ./build/native/nativeCompile/generator-cli ./dev-resources/test
cd ./dev-resources/test
./generator-cli generator
```

## 使用方法

執行時準備資訊選擇或填寫

``` bash
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





