# Generator CLI - 產品需求規劃書 (PRD)

## 文檔概覽

本目錄包含 Generator CLI 專案的完整產品需求規劃書，採用 ARC42 架構文檔模板組織。

## 文檔結構

```
docs/
├── README.md                           # 本文檔 - 專案和文檔概覽
├── requirements/
│   ├── README.md                       # 需求文檔概覽
│   └── user_stories.md                 # 使用者故事文檔
├── design/
│   ├── README.md                       # 設計文檔概覽
│   ├── 01_introduction_and_goals.md    # 1. 簡介和目標
│   ├── 02_system_scope_and_context.md  # 2. 上下文和範圍
│   ├── 03_building_block_view.md       # 3. 建構塊視圖
│   ├── 04_runtime_view.md              # 4. 運行時視圖
│   ├── 05_architectural_decisions.md   # 5. 設計決策
│   ├── 06_glossary.md                  # 6. 詞彙表
│   ├── database_design.md              # 資料庫設計
│   └── error_handling_and_logging.md   # 錯誤處理和日誌策略
├── test_plan.md                        # 測試策略
├── deployment_plan.md                  # 部署流程
└── appendices/
    └── README.md                       # 附錄概覽
```

## 專案概覽

Generator CLI 是一個強大的代碼生成器命令列工具，用於快速生成符合現代化企業級標準的 Spring Boot 專案。

### 主要功能
- 🚀 基於 OpenAPI 3.1 規範自動生成 REST API 程式碼
- 🗄️ 從資料庫反向工程生成 JOOQ 和 Spring Data JPA 程式碼
- 📊 自動生成 Liquibase 資料庫版本控制腳本
- 🐳 內建 Docker 和 Kubernetes 部署支援
- 🛠️ 支援 Gradle 構建工具
- ✨ 遵循 Clean Code 和 Clean Architecture 原則

### 技術棧
- **語言**: Java 17+
- **框架**: Spring Boot 3.x, Spring Shell
- **構建工具**: Gradle
- **資料庫**: 支援 PostgreSQL, MySQL 等
- **代碼生成**: JOOQ, OpenAPI Generator
- **容器化**: Docker, Kubernetes

## 快速導航

- [需求分析](requirements/) - 功能需求和非功能需求
- [系統設計](design/) - 架構設計和技術決策
- [測試計劃](test_plan.md) - 測試策略和測試案例
- [部署計劃](deployment_plan.md) - 部署流程和環境配置

## 文檔維護

本文檔採用 Living Documentation 原則，隨專案演進持續更新。

**最後更新**: 2024-12-13
**版本**: v1.0.0
**維護者**: CloudTechnology Team 