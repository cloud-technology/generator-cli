# 需求文檔概覽

## 文檔說明

本目錄包含 Generator CLI 專案的需求分析文檔，涵蓋功能需求、非功能需求和使用者故事。

## 文檔結構

- **user_stories.md** - 詳細的使用者故事和驗收標準

## 需求概要

### 功能需求 (Functional Requirements)

#### FR1: 專案生成
- 支援基於範本快速生成 Spring Boot 專案結構
- 支援 Gradle 構建工具配置
- 自動生成標準的專案目錄結構

#### FR2: API 代碼生成
- 基於 OpenAPI 3.1 規範生成 REST API 控制器
- 生成對應的 DTO 類別和驗證註解
- 支援多種 HTTP 方法 (GET, POST, PUT, DELETE)

#### FR3: 資料庫代碼生成
- 連接資料庫並自動生成 JOOQ 程式碼
- 生成 Spring Data JPA Repository 介面
- 自動識別主鍵類型和關聯關係

#### FR4: 資料庫版本控制
- 基於現有資料庫結構生成 Liquibase changelog
- 支援差異比較和增量更新
- 自動產生版本控制腳本

#### FR5: 容器化支援
- 自動生成 Dockerfile
- 生成 Docker Compose 配置
- 支援 Kubernetes 部署 YAML

### 非功能需求 (Non-Functional Requirements)

#### NFR1: 性能要求
- 單個專案生成時間 < 30 秒
- 支援大型資料庫 (1000+ 資料表)
- 記憶體使用量 < 512MB

#### NFR2: 可用性
- 提供直觀的命令列介面
- 支援參數驗證和錯誤提示
- 提供詳細的幫助文檔

#### NFR3: 可維護性
- 遵循 Clean Code 原則
- 模組化設計，易於擴展
- 完整的單元測試覆蓋率 > 80%

#### NFR4: 相容性
- 支援 Java 17+
- 相容主流資料庫 (PostgreSQL, MySQL, Oracle)
- 支援多種作業系統 (Windows, macOS, Linux)

#### NFR5: 安全性
- 敏感資訊 (如密碼) 的安全處理
- 支援加密連線
- 避免 SQL 注入風險

## 驗收標準

每個功能需求都有對應的驗收標準，詳見 [使用者故事文檔](user_stories.md)。 