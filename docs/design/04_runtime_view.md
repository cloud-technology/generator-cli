# 4. 運行時視圖 (Runtime View)

## 4.1 使用者故事運行時序列

### 4.1.1 US-001: 快速專案生成流程

```mermaid
sequenceDiagram
    participant User as 開發者
    participant CLI as Spring Shell CLI
    participant PS as ProjectService
    participant PG as GradleProjectGenerator
    participant FS as FileSystem

    User->>CLI: 執行 generator 命令
    CLI->>User: 顯示互動式提示
    
    User->>CLI: 輸入專案資訊
    CLI->>CLI: 驗證輸入參數
    
    alt 參數有效
        CLI->>PS: create(CreateProjectCommand)
        PS->>PG: generate(ProjectVo)
        
        PG->>FS: 創建專案目錄結構
        PG->>FS: 生成 build.gradle
        PG->>FS: 生成 application.yml
        PG->>FS: 生成 Application.java
        PG->>FS: 生成 .devcontainer/
        
        PG-->>PS: 專案結構生成完成
        PS-->>CLI: 生成成功
        CLI-->>User: 顯示成功訊息
    else 參數無效
        CLI-->>User: 顯示錯誤訊息
    end
```

### 4.1.2 US-002: OpenAPI 整合流程

```mermaid
sequenceDiagram
    participant User as 開發者
    participant CLI as Spring Shell CLI
    participant PS as ProjectService
    participant AG as OpenApiGenerator
    participant OAS as OpenAPI 檔案
    participant FS as FileSystem

    User->>CLI: 輸入 OpenAPI 檔案路徑
    CLI->>PS: 包含 OpenAPI 路徑的命令
    PS->>AG: generate(ApiVo)
    
    AG->>OAS: 讀取 OpenAPI 規範
    AG->>AG: 解析 API 定義
    AG->>AG: 生成 Controller 程式碼
    AG->>AG: 生成 DTO 類別
    
    AG->>FS: 寫入 Controller 檔案
    AG->>FS: 寫入 DTO 檔案
    AG->>FS: 更新 build.gradle (添加依賴)
    
    AG-->>PS: API 程式碼生成完成
    PS-->>CLI: 回報生成狀態
    CLI-->>User: 顯示 API 生成結果
```

### 4.1.3 US-003: 資料庫整合流程

```mermaid
sequenceDiagram
    participant User as 開發者
    participant CLI as Spring Shell CLI
    participant PS as ProjectService
    participant JG as JooqGenerator
    participant SRG as SimpleRepositoryGenerator
    participant SPG as SpringRepositoryGenerator
    participant DB as Database
    participant FS as FileSystem

    User->>CLI: 輸入資料庫連線資訊
    CLI->>PS: 包含資料庫配置的命令
    PS->>JG: generate(RepositoryVo)
    
    JG->>DB: 連接資料庫
    JG->>DB: 查詢資料庫結構
    JG->>SRG: 使用自定義生成器
    
    loop 每個資料表
        SRG->>SRG: generatePojoClassFooter()
        SRG->>SRG: collectTableMetadata()
        SRG->>FS: 生成 POJO 類別
    end
    
    SRG->>FS: 輸出 repository-metadata.json
    JG-->>PS: JOOQ 生成完成
    
    PS->>SPG: generate(RepositoryVo)
    SPG->>FS: 讀取 repository-metadata.json
    
    loop 每個表元數據
        SPG->>SPG: generateRepositoryForTable()
        SPG->>FS: 生成 Repository 介面
    end
    
    SPG-->>PS: Repository 生成完成
    PS->>FS: 清理 repository-metadata.json
    PS-->>CLI: 資料庫整合完成
    CLI-->>User: 顯示生成結果
```

### 4.1.4 US-005: Liquibase 整合流程

```mermaid
sequenceDiagram
    participant PS as ProjectService
    participant LG as LiquibaseGenerator
    participant DB as Database
    participant FS as FileSystem

    PS->>LG: generate(SchemaVo)
    LG->>DB: 連接資料庫
    LG->>DB: 查詢資料庫結構
    
    LG->>LG: 生成資料表定義
    LG->>LG: 生成索引定義
    LG->>LG: 生成約束定義
    
    LG->>FS: 創建 db/changelog/ 目錄
    LG->>FS: 生成 db-changelog-master.yaml
    LG->>FS: 生成各個 changelog 檔案
    
    LG-->>PS: Liquibase 配置生成完成
```

## 4.2 錯誤處理流程

### 4.2.1 資料庫連線失敗處理

```mermaid
sequenceDiagram
    participant CLI as Spring Shell CLI
    participant PS as ProjectService
    participant JG as JooqGenerator
    participant DB as Database

    CLI->>PS: create(command)
    PS->>JG: generate(repositoryVo)
    JG->>DB: 嘗試連接資料庫
    DB-->>JG: 連線失敗
    
    JG->>JG: 捕獲 SQLException
    JG->>JG: 記錄錯誤日誌
    JG-->>PS: 拋出 DatabaseConnectionException
    
    PS->>PS: 捕獲異常
    PS->>PS: 清理已生成的檔案
    PS-->>CLI: 回報錯誤
    CLI-->>User: 顯示詳細錯誤訊息
```

### 4.2.2 檔案系統錯誤處理

```mermaid
sequenceDiagram
    participant PS as ProjectService
    participant PG as ProjectGenerator
    participant FS as FileSystem

    PS->>PG: generate(projectVo)
    PG->>FS: 創建專案目錄
    FS-->>PG: 權限不足錯誤
    
    PG->>PG: 捕獲 IOException
    PG->>PG: 記錄錯誤日誌
    PG-->>PS: 拋出 FileSystemException
    
    PS->>PS: 捕獲異常
    PS->>PS: 執行清理邏輯
    PS-->>CLI: 回報錯誤
```

## 4.3 效能關鍵路徑

### 4.3.1 大型資料庫處理

```mermaid
sequenceDiagram
    participant JG as JooqGenerator
    participant DB as Database
    participant SRG as SimpleRepositoryGenerator
    participant SPG as SpringRepositoryGenerator
    participant FS as FileSystem

    JG->>DB: 批次查詢資料庫結構
    
    par 並行處理資料表
        SRG->>SRG: 處理表 1-100
        SRG->>SRG: 處理表 101-200
        SRG->>SRG: 處理表 201-300
    end
    
    SRG->>FS: 批次輸出 POJO 檔案
    SRG->>FS: 輸出表元數據
    
    par 並行生成 Repository
        SPG->>SPG: 生成 Repository 1-100
        SPG->>SPG: 生成 Repository 101-200
        SPG->>SPG: 生成 Repository 201-300
    end
    
    SPG->>FS: 批次輸出 Repository 檔案
```

### 4.3.2 記憶體最佳化策略

```mermaid
flowchart TD
    START([開始處理]) --> BATCH[批次處理 100 個表]
    BATCH --> PROCESS[處理當前批次]
    PROCESS --> GENERATE[生成程式碼]
    GENERATE --> FLUSH[清理記憶體]
    FLUSH --> CHECK{還有更多?}
    CHECK -->|是| BATCH
    CHECK -->|否| END([完成])
```

## 4.4 併發處理模式

### 4.4.1 多執行緒生成策略

```java
@Service
public class ProjectService {
    private final ExecutorService executorService = 
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
    public void create(CreateProjectCommand command) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // 1. 並行生成不同類型的程式碼
        if (hasOpenApi(command)) {
            futures.add(CompletableFuture.runAsync(() -> 
                generateApiCode(command), executorService));
        }
        
        if (hasDatabase(command)) {
            futures.add(CompletableFuture.runAsync(() -> 
                generateDatabaseCode(command), executorService));
        }
        
        // 2. 等待所有任務完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 3. 最後生成容器化配置
        generateContainerConfig(command);
    }
}
```

## 4.5 狀態管理和事務性

### 4.5.1 生成流程狀態機

```mermaid
stateDiagram-v2
    [*] --> INITIALIZING: 開始生成
    INITIALIZING --> VALIDATING: 初始化完成
    VALIDATING --> GENERATING_PROJECT: 驗證通過
    VALIDATING --> ERROR: 驗證失敗
    
    GENERATING_PROJECT --> GENERATING_API: 專案結構完成
    GENERATING_API --> GENERATING_DB: API 生成完成
    GENERATING_DB --> GENERATING_CONTAINER: 資料庫生成完成
    GENERATING_CONTAINER --> CLEANING: 容器配置完成
    CLEANING --> COMPLETED: 清理完成
    
    GENERATING_PROJECT --> ERROR: 生成失敗
    GENERATING_API --> ERROR: 生成失敗
    GENERATING_DB --> ERROR: 生成失敗
    GENERATING_CONTAINER --> ERROR: 生成失敗
    
    ERROR --> ROLLBACK: 執行回滾
    ROLLBACK --> [*]: 清理完成
    COMPLETED --> [*]: 生成完成
```

### 4.5.2 事務性保證機制

```java
@Component
public class TransactionalProjectGenerator {
    private final List<Path> generatedFiles = new ArrayList<>();
    private final List<Path> generatedDirectories = new ArrayList<>();
    
    public void generate(CreateProjectCommand command) {
        try {
            // 記錄所有生成的檔案和目錄
            generateWithTracking(command);
        } catch (Exception e) {
            // 發生錯誤時回滾所有變更
            rollback();
            throw e;
        }
    }
    
    private void rollback() {
        // 刪除所有生成的檔案
        generatedFiles.forEach(this::deleteQuietly);
        // 刪除所有生成的目錄
        generatedDirectories.forEach(this::deleteDirectoryQuietly);
    }
}
```

## 4.6 監控和可觀測性

### 4.6.1 生成進度追蹤

```java
@Component
public class ProgressTracker {
    private final List<ProgressListener> listeners = new ArrayList<>();
    
    public void reportProgress(String stage, int current, int total) {
        double percentage = (double) current / total * 100;
        ProgressEvent event = new ProgressEvent(stage, current, total, percentage);
        listeners.forEach(listener -> listener.onProgress(event));
    }
}
```

### 4.6.2 效能監控

```java
@Component
public class PerformanceMonitor {
    private final MeterRegistry meterRegistry;
    
    public <T> T measureTime(String operation, Supplier<T> supplier) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return supplier.get();
        } finally {
            sample.stop(Timer.builder("generator.operation.duration")
                .tag("operation", operation)
                .register(meterRegistry));
        }
    }
}
```

## 4.7 資源管理

### 4.7.1 資料庫連線管理

```java
@Component
public class DatabaseConnectionManager {
    private final HikariDataSource dataSource;
    
    public <T> T executeWithConnection(String url, String username, String password, 
                                      ConnectionCallback<T> callback) {
        try (Connection connection = createConnection(url, username, password)) {
            return callback.execute(connection);
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to execute database operation", e);
        }
    }
}
```

### 4.7.2 記憶體管理

```java
@Component
public class MemoryManager {
    private static final int MAX_BATCH_SIZE = 100;
    
    public <T> void processBatches(List<T> items, BatchProcessor<T> processor) {
        for (int i = 0; i < items.size(); i += MAX_BATCH_SIZE) {
            List<T> batch = items.subList(i, Math.min(i + MAX_BATCH_SIZE, items.size()));
            processor.process(batch);
            
            // 強制垃圾回收以釋放記憶體
            if (i % (MAX_BATCH_SIZE * 10) == 0) {
                System.gc();
            }
        }
    }
}
``` 