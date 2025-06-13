# 設計文檔概覽

## 文檔說明

本目錄包含 Generator CLI 專案的架構設計文檔，採用 ARC42 架構文檔模板組織。

## ARC42 架構文檔結構

### 核心章節

1. **[簡介和目標](01_introduction_and_goals.md)** - 系統目標、功能需求和非功能需求
2. **[上下文和範圍](02_system_scope_and_context.md)** - 系統邊界、外部依賴和 C4 Model 圖表
3. **[建構塊視圖](03_building_block_view.md)** - 系統組件架構和模組設計
4. **[運行時視圖](04_runtime_view.md)** - 系統運行流程和序列圖
5. **[設計決策](05_architectural_decisions.md)** - 重要的架構決策和技術選型
6. **[詞彙表](06_glossary.md)** - 重要術語和概念定義

### 補充文檔

- **[資料庫設計](database_design.md)** - 資料模型和資料庫 Schema
- **[錯誤處理和日誌](error_handling_and_logging.md)** - 錯誤處理策略和日誌規範

## 設計原則

### 架構原則
- **Clean Architecture**: 依賴倒置、層次分離
- **Single Responsibility**: 每個元件有明確且單一的職責
- **Open/Closed Principle**: 開放擴展、封閉修改
- **Dependency Injection**: 使用 Spring Framework DI 容器

### 程式碼原則
- **Clean Code**: 可讀性、可維護性
- **SOLID Principles**: 物件導向設計原則
- **DRY (Don't Repeat Yourself)**: 避免重複程式碼
- **KISS (Keep It Simple, Stupid)**: 保持簡單

### 測試原則
- **Test-Driven Development (TDD)**: 測試驅動開發
- **Unit Testing**: 單元測試覆蓋率 > 80%
- **Integration Testing**: 整合測試覆蓋主要流程
- **Contract Testing**: API 契約測試

## 技術棧概覽

### 開發技術
- **語言**: Java 17+
- **框架**: Spring Boot 3.x, Spring Shell
- **構建工具**: Gradle 8.x
- **測試**: JUnit 5, Mockito, TestContainers

### 代碼生成
- **OpenAPI**: OpenAPI Generator
- **資料庫**: JOOQ Code Generator
- **模板引擎**: Mustache

### 資料庫支援
- **關聯式資料庫**: PostgreSQL, MySQL, Oracle
- **版本控制**: Liquibase
- **ORM**: Spring Data JPA

### 容器化
- **容器**: Docker
- **編排**: Kubernetes
- **部署**: Docker Compose

## 品質屬性

### 性能 (Performance)
- 專案生成時間 < 30 秒
- 支援大型資料庫 (1000+ 資料表)
- 記憶體使用 < 512MB

### 可用性 (Usability)
- 直觀的命令列介面
- 豐富的錯誤提示
- 完整的幫助文檔

### 可維護性 (Maintainability)
- 模組化設計
- 清晰的程式碼結構
- 完整的文檔

### 可擴展性 (Extensibility)
- 插件式架構
- 模板可定制
- 新功能易於添加

### 可靠性 (Reliability)
- 完善的錯誤處理
- 資料一致性保證
- 操作可回復

## 閱讀指南

1. **初次閱讀**: 建議按順序閱讀 01-06 章節
2. **開發人員**: 重點關注建構塊視圖和運行時視圖
3. **架構師**: 重點關注設計決策和架構原則
4. **DevOps**: 重點關注部署和容器化相關內容

## 文檔維護

- **更新頻率**: 每次重大功能更新時同步更新文檔
- **責任人**: 架構師和技術負責人
- **版本控制**: 與程式碼同步版本管理 