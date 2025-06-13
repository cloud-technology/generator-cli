# Simple Repository Generator 使用說明

## 概述

`SimpleRepositoryGenerator` 是一個基於 JOOQ 的代碼生成器，專門用於自動生成 Spring Data JPA Repository 介面。它遵循 Clean Code 原則設計，提供乾淨、可維護的代碼生成功能。

## 主要功能

- ✅ 自動生成 Spring Data JPA Repository 介面
- ✅ 智能識別資料表主鍵類型
- ✅ 支援 Mustache 模板引擎
- ✅ 提供完整的中文註解
- ✅ 遵循 Clean Code 設計原則
- ✅ 包含 JpaSpecificationExecutor 支援動態查詢

## 使用方式

### 1. Maven 配置

在 `pom.xml` 中配置 JOOQ 代碼生成器：

```xml
<plugin>
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen-maven</artifactId>
    <version>3.20.4</version>
    <configuration>
        <jdbc>
            <driver>com.mysql.cj.jdbc.Driver</driver>
            <url>jdbc:mysql://localhost:3306/your_database</url>
            <user>your_username</user>
            <password>your_password</password>
        </jdbc>
        <generator>
            <!-- 使用我們的自定義生成器 -->
            <name>io.github.cloudtechnology.generator.jooq.SimpleRepositoryGenerator</name>
            <database>
                <name>org.jooq.meta.mysql.MySQLDatabase</name>
                <inputSchema>your_database</inputSchema>
            </database>
            <target>
                <packageName>com.example.generated</packageName>
                <directory>src/main/java</directory>
            </target>
        </generator>
    </configuration>
</plugin>
```

### 2. 生成代碼

執行 Maven 命令：

```bash
mvn jooq-codegen:generate
```

### 3. 生成結果

對於資料表 `user`，將會生成：

1. **POJO 類別**: `User.java`
2. **Repository 介面**: `UserRepository.java`

#### 生成的 Repository 範例：

```java
package com.example.generated;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * User 資料存取層介面
 * 提供標準的 CRUD 操作和複雜查詢功能
 * 
 * 繼承 JpaRepository 提供基本的 CRUD 操作
 * 繼承 JpaSpecificationExecutor 提供動態查詢功能
 * 
 * @author 系統自動生成
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, 
                                       JpaSpecificationExecutor<User> {
    
    // 標準的 JpaRepository 已提供以下基本操作：
    // - save(entity): 儲存或更新實體
    // - findById(id): 根據 ID 查詢實體
    // - findAll(): 查詢所有實體
    // - deleteById(id): 根據 ID 刪除實體
    // - count(): 統計實體數量
    // - existsById(id): 檢查實體是否存在
    
    // 可在此處添加自定義查詢方法
    // 例如：
    // List<User> findByFieldName(String fieldName);
    // Optional<User> findByUniqueField(String uniqueField);
}
```

## 代碼架構說明

### Clean Code 原則實踐

1. **單一職責原則**: 每個方法只負責一個特定功能
2. **開放封閉原則**: 使用常量和模板，易於擴展和修改
3. **依賴倒置**: 通過抽象和接口設計，降低耦合度
4. **命名清晰**: 使用具有描述性的方法和變數名稱

### 主要類別結構

- `SimpleRepositoryGenerator`: 主要的代碼生成器類別
- `ClassNameInfo`: 內部類別，封裝類別命名相關資訊
- 常量定義: 將所有魔術字串提取為常量

### 錯誤處理機制

- 使用 `Optional` 處理可能為空的值
- 提供詳細的錯誤日誌
- 適當的異常處理和重新拋出

## 自定義配置

### 修改模板

如需自定義 Repository 模板，請編輯：
`src/main/resources/templates/repository/JpaRepository.mustache`

### 模板變數

- `{{packageName}}`: 套件名稱
- `{{className}}`: Repository 類別名稱
- `{{pojoClassName}}`: POJO 類別名稱
- `{{primaryKeyType}}`: 主鍵類型

## 注意事項

1. **主鍵要求**: 資料表必須有主鍵，否則會跳過 Repository 生成
2. **命名規範**: Repository 類別名稱 = POJO 類別名稱 + "Repository"
3. **檔案覆蓋**: 每次生成會覆蓋現有的 Repository 檔案
4. **依賴管理**: 確保專案中包含 Spring Data JPA 相關依賴

## 依賴要求

```xml
<dependencies>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- JOOQ -->
    <dependency>
        <groupId>org.jooq</groupId>
        <artifactId>jooq-codegen</artifactId>
        <version>3.20.4</version>
    </dependency>
    
    <!-- Mustache Template Engine -->
    <dependency>
        <groupId>com.samskivert</groupId>
        <artifactId>jmustache</artifactId>
    </dependency>
</dependencies>
```

## 疑難排解

### 常見問題

1. **找不到模板檔案**: 確認 `templates/repository/JpaRepository.mustache` 檔案存在
2. **生成失敗**: 檢查資料庫連線和表結構
3. **主鍵識別錯誤**: 確認資料表有正確的主鍵定義

### 日誌設定

啟用詳細日誌：

```properties
logging.level.io.github.cloudtechnology.generator.jooq=DEBUG
```
