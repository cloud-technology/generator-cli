# 6. 詞彙表 (Glossary)

## A

**API (Application Programming Interface)**
- 應用程式介面，定義不同軟體元件之間的通訊規範

**ADR (Architectural Decision Record)**
- 架構決策記錄，記錄重要技術決策的文件格式

**Artifact**
- Maven/Gradle 構建產生的成品，通常是 JAR 檔案

## B

**Build Tool**
- 構建工具，如 Gradle 或 Maven，用於專案建置和依賴管理

**BO (Business Object)**
- 業務物件，包含業務邏輯的領域物件

## C

**Clean Architecture**
- 清潔架構，強調依賴倒置和層次分離的軟體架構模式

**Clean Code**
- 清潔程式碼，強調可讀性、可維護性的程式設計原則

**CLI (Command Line Interface)**
- 命令列介面，透過文字命令與程式互動的使用者介面

**Container**
- 容器，輕量級的應用程式虛擬化技術

**CRUD**
- Create, Read, Update, Delete 的縮寫，基本的資料操作

## D

**DTO (Data Transfer Object)**
- 資料傳輸物件，用於不同層之間傳遞資料的物件

**DI (Dependency Injection)**
- 依賴注入，控制反轉的實現方式

**DevOps**
- 開發和維運一體化的軟體開發方法論

## E

**Entity**
- 實體，具有唯一識別碼的領域物件

**Endpoint**
- 端點，API 的具體存取位址

## F

**Framework**
- 框架，提供基礎功能的軟體架構

**FR (Functional Requirements)**
- 功能需求，系統必須提供的功能

## G

**Generator**
- 生成器，自動產生程式碼或配置檔案的工具

**Gradle**
- 基於 Groovy/Kotlin DSL 的構建自動化工具

## H

**HTTP**
- 超文本傳輸協定，Web 應用的通訊協定

## I

**IDE (Integrated Development Environment)**
- 整合開發環境，如 IntelliJ IDEA, Eclipse

**Infrastructure**
- 基礎設施，系統的底層技術組件

## J

**JDBC (Java Database Connectivity)**
- Java 資料庫連線 API

**JDK (Java Development Kit)**
- Java 開發工具包

**JPA (Java Persistence API)**
- Java 持久化 API，ORM 的標準規範

**JOOQ**
- Java Object Oriented Querying，類型安全的 SQL 建構工具

**JPA Repository**
- Spring Data JPA 提供的資料存取抽象層

## K

**K8s (Kubernetes)**
- 容器編排系統，用於管理容器化應用

## L

**Liquibase**
- 資料庫結構版本控制工具

## M

**Maven**
- Java 專案管理和構建工具

**Metadata**
- 中繼資料，描述其他資料的資料

**Mustache**
- 無邏輯模板引擎

**MySQL**
- 開源關聯式資料庫管理系統

## N

**NFR (Non-Functional Requirements)**
- 非功能需求，系統的品質屬性要求

## O

**OpenAPI**
- API 規範標準，前身為 Swagger

**ORM (Object-Relational Mapping)**
- 物件關聯對映，在物件和關聯式資料庫間建立對映

**Oracle**
- 企業級關聯式資料庫管理系統

## P

**POJO (Plain Old Java Object)**
- 普通的 Java 物件，不依賴特定框架

**PostgreSQL**
- 功能強大的開源關聯式資料庫

**PRD (Product Requirements Document)**
- 產品需求規劃書

## Q

**Query**
- 查詢，從資料庫獲取資料的操作

## R

**Repository**
- 儲存庫模式，封裝資料存取邏輯的設計模式

**REST (Representational State Transfer)**
- 表現層狀態轉移，Web 服務架構風格

**Record**
- Java 14+ 引入的不可變資料類別

**Runtime**
- 運行時環境，程式執行的環境

## S

**Spring Boot**
- 簡化 Spring 應用開發的框架

**Spring Shell**
- Spring 項目的命令列應用框架

**Spring Data JPA**
- Spring 對 JPA 的整合封裝

**SQL**
- 結構化查詢語言

**SOLID**
- 物件導向設計的五個基本原則

**Schema**
- 資料庫結構定義

## T

**Template**
- 模板，用於生成程式碼的範本

**TDD (Test-Driven Development)**
- 測試驅動開發

## U

**URL (Uniform Resource Locator)**
- 統一資源定位符

**Use Case**
- 使用案例，描述系統功能的方法

**User Story**
- 使用者故事，從使用者角度描述功能需求

## V

**VO (Value Object)**
- 值物件，沒有唯一識別碼的不可變物件

**VCS (Version Control System)**
- 版本控制系統，如 Git

## W

**Workflow**
- 工作流程，一系列有序的工作步驟

## Y

**YAML**
- 人類可讀的資料序列化標準

## 技術縮寫對照表

| 縮寫 | 英文全稱 | 中文翻譯 |
|------|----------|----------|
| API | Application Programming Interface | 應用程式介面 |
| CLI | Command Line Interface | 命令列介面 |
| DTO | Data Transfer Object | 資料傳輸物件 |
| JPA | Java Persistence API | Java 持久化 API |
| ORM | Object-Relational Mapping | 物件關聯對映 |
| REST | Representational State Transfer | 表現層狀態轉移 |
| CRUD | Create, Read, Update, Delete | 增查改刪 |
| POJO | Plain Old Java Object | 普通 Java 物件 |
| SOLID | Single/Open/Liskov/Interface/Dependency | 單一/開放/里氏/介面/依賴 |
| TDD | Test-Driven Development | 測試驅動開發 |
| DI | Dependency Injection | 依賴注入 |
| IoC | Inversion of Control | 控制反轉 |

## 業務術語說明

### 代碼生成器 (Code Generator)
自動產生程式碼的工具，根據模板和輸入資料生成標準化的程式碼檔案。

### 反向工程 (Reverse Engineering)
從現有的資料庫結構或 API 規範中分析並產生對應的程式碼。

### 腳手架 (Scaffolding)
快速建立專案基礎結構的工具或方法。

### 模板引擎 (Template Engine)
將模板檔案和資料結合生成最終文件的工具。

### 依賴注入容器 (DI Container)
管理物件生命週期和依賴關係的容器。

### 資料存取層 (Data Access Layer)
負責與資料庫互動的應用程式層次。

### 業務邏輯層 (Business Logic Layer)
包含核心業務規則和處理邏輯的應用程式層次。

### 表現層 (Presentation Layer)
負責與使用者互動的應用程式層次。

### 基礎設施層 (Infrastructure Layer)
提供技術服務和外部系統整合的應用程式層次。

## 品質屬性術語

### 可維護性 (Maintainability)
軟體在其生命週期內被修改的容易程度。

### 可擴展性 (Scalability)
系統處理增長的工作負載的能力。

### 可用性 (Usability)
使用者能夠有效、高效、滿意地使用系統的程度。

### 可靠性 (Reliability)
系統在規定條件下和規定時間內完成規定功能的能力。

### 性能 (Performance)
系統在給定條件下的執行效率。

### 安全性 (Security)
保護系統免受意外或惡意存取的能力。

## 開發方法論術語

### 敏捷開發 (Agile Development)
強調個體互動、可工作軟體、客戶協作和回應變化的軟體開發方法。

### 持續整合 (Continuous Integration, CI)
開發人員頻繁地將程式碼整合到主分支的實踐。

### 持續部署 (Continuous Deployment, CD)
自動化將程式碼變更部署到生產環境的實踐。

### DevOps
結合軟體開發和 IT 營運的實踐，旨在縮短系統開發生命週期。

### 基礎設施即代碼 (Infrastructure as Code, IaC)
透過程式碼管理和配置基礎設施的實踐。

## 資料庫相關術語

### 關聯式資料庫 (Relational Database)
基於關聯模型的資料庫管理系統。

### 主鍵 (Primary Key)
唯一識別資料表中每一行記錄的欄位或欄位組合。

### 外鍵 (Foreign Key)
參照其他資料表主鍵的欄位。

### 索引 (Index)
提高資料查詢效率的資料庫物件。

### 約束 (Constraint)
限制資料表中資料完整性的規則。

### 遷移 (Migration)
資料庫結構變更的腳本或過程。

### 變更日誌 (Changelog)
記錄資料庫結構變更歷史的檔案。 