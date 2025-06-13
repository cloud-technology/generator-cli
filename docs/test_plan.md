# 測試計劃 (Test Plan)

## 1. 測試概覽

### 1.1 測試目標
- 確保 Generator CLI 的功能正確性
- 驗證性能需求達標
- 確保使用者體驗符合預期
- 驗證生成的程式碼品質

### 1.2 測試範圍

#### 包含範圍
- ✅ 核心功能測試（專案生成、API 生成、資料庫整合）
- ✅ 使用者介面測試（CLI 互動）
- ✅ 整合測試（各模組間的協作）
- ✅ 性能測試（大型資料庫處理）
- ✅ 相容性測試（多資料庫、多 OS）
- ✅ 錯誤處理測試

#### 排除範圍
- ❌ 生成專案的業務邏輯測試
- ❌ 第三方依賴的內部測試
- ❌ 生產環境部署測試

### 1.3 測試策略

```mermaid
pyramid
    title 測試金字塔
    L0 : 單元測試 (70%)
    L1 : 整合測試 (20%)
    L2 : 端到端測試 (10%)
```

## 2. 測試類型和方法

### 2.1 單元測試 (Unit Testing)

#### 測試框架
- **JUnit 5**: 核心測試框架
- **Mockito**: Mock 物件
- **AssertJ**: 流暢的斷言

#### 覆蓋範圍
- 目標覆蓋率：**80%+**
- 重點測試的類別：
  - Service 層的業務邏輯
  - Generator 的生成邏輯
  - Utility 類別
  - Value Objects

#### 測試範例
```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private GradleProjectGenerator gradleProjectGenerator;
    
    @InjectMocks
    private ProjectService projectService;
    
    @Test
    @DisplayName("應該成功生成基礎專案結構")
    void shouldGenerateBasicProjectStructure() {
        // Given
        CreateProjectCommand command = createTestCommand();
        
        // When
        projectService.create(command);
        
        // Then
        verify(gradleProjectGenerator).generate(any(ProjectVo.class));
    }
}
```

### 2.2 整合測試 (Integration Testing)

#### 測試框架
- **Spring Boot Test**: Spring 整合測試
- **TestContainers**: 資料庫整合測試
- **WireMock**: 外部服務模擬

#### 測試範圍
- 資料庫連線和查詢
- 檔案系統操作
- Spring Bean 之間的整合
- JOOQ 代碼生成流程

#### 測試範例
```java
@SpringBootTest
@Testcontainers
class JooqGeneratorIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    @DisplayName("應該成功從 PostgreSQL 生成 POJO 類別")
    void shouldGeneratePojoFromPostgreSQL() {
        // Given
        String dbUrl = postgres.getJdbcUrl();
        RepositoryVo repositoryVo = createRepositoryVo(dbUrl);
        
        // When & Then
        assertThatNoException().isThrownBy(() -> 
            jooqGenerator.generate(repositoryVo));
    }
}
```

### 2.3 端到端測試 (End-to-End Testing)

#### 測試工具
- **Spring Shell Test**: CLI 測試
- **Testcontainers**: 真實環境模擬

#### 測試場景
- 完整的專案生成流程
- 錯誤場景的處理
- 使用者互動流程

#### 測試範例
```java
@SpringBootTest
class GeneratorE2ETest {
    
    @Test
    @DisplayName("完整專案生成端到端流程")
    void completeProjectGenerationFlow() {
        // Given
        String[] args = {"--build-tool=GRADLE", "--group-id=com.test"};
        
        // When
        int exitCode = runGeneratorCli(args);
        
        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(generatedProjectExists()).isTrue();
        assertThat(generatedFilesAreValid()).isTrue();
    }
}
```

## 3. 功能測試案例

### 3.1 專案生成功能

| 測試案例 ID | 測試描述 | 前置條件 | 測試步驟 | 期望結果 |
|------------|----------|----------|----------|----------|
| TC-001 | 基本專案生成 | CLI 已啟動 | 1. 執行 generator<br>2. 輸入必要參數 | 生成標準專案結構 |
| TC-002 | 參數驗證 | CLI 已啟動 | 1. 輸入無效參數 | 顯示錯誤訊息 |
| TC-003 | 專案覆蓋確認 | 目標目錄已存在 | 1. 嘗試生成到已存在目錄 | 提示覆蓋確認 |

### 3.2 OpenAPI 整合功能

| 測試案例 ID | 測試描述 | 前置條件 | 測試步驟 | 期望結果 |
|------------|----------|----------|----------|----------|
| TC-101 | 有效 OpenAPI 檔案 | 提供有效 OpenAPI 規範 | 1. 指定 OpenAPI 檔案<br>2. 執行生成 | 生成對應 Controller |
| TC-102 | 無效 OpenAPI 檔案 | 提供無效 OpenAPI 規範 | 1. 指定無效檔案 | 顯示解析錯誤 |
| TC-103 | 檔案不存在 | OpenAPI 檔案不存在 | 1. 指定不存在檔案 | 顯示檔案未找到錯誤 |

### 3.3 資料庫整合功能

| 測試案例 ID | 測試描述 | 前置條件 | 測試步驟 | 期望結果 |
|------------|----------|----------|----------|----------|
| TC-201 | PostgreSQL 連線 | PostgreSQL 可用 | 1. 輸入正確連線資訊<br>2. 執行生成 | 成功生成 POJO 和 Repository |
| TC-202 | MySQL 連線 | MySQL 可用 | 1. 輸入正確連線資訊<br>2. 執行生成 | 成功生成 POJO 和 Repository |
| TC-203 | 連線失敗處理 | 資料庫不可用 | 1. 輸入錯誤連線資訊 | 顯示連線錯誤訊息 |
| TC-204 | 大型資料庫 | 1000+ 資料表 | 1. 連接大型資料庫<br>2. 執行生成 | 在合理時間內完成生成 |

## 4. 性能測試

### 4.1 性能需求

| 指標 | 目標值 | 測量方法 |
|------|--------|----------|
| 小型專案生成時間 | < 10 秒 | 10 個表以下 |
| 中型專案生成時間 | < 30 秒 | 100 個表以下 |
| 大型專案生成時間 | < 60 秒 | 1000 個表以下 |
| 記憶體使用量 | < 512MB | 運行時最大記憶體 |
| CPU 使用率 | < 80% | 生成過程中平均值 |

### 4.2 性能測試場景

#### 場景 1: 基準性能測試
```java
@Test
@DisplayName("基準性能測試 - 100 個表")
void benchmarkPerformanceTest() {
    // Given
    Database with 100 tables
    
    // When
    long startTime = System.currentTimeMillis();
    projectService.create(command);
    long endTime = System.currentTimeMillis();
    
    // Then
    long duration = endTime - startTime;
    assertThat(duration).isLessThan(30_000); // 30 seconds
}
```

#### 場景 2: 記憶體壓力測試
```java
@Test
@DisplayName("記憶體使用測試")
void memoryUsageTest() {
    // Given
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    long beforeUsed = memoryBean.getHeapMemoryUsage().getUsed();
    
    // When
    projectService.create(largeProjectCommand);
    
    // Then
    long afterUsed = memoryBean.getHeapMemoryUsage().getUsed();
    long memoryUsed = afterUsed - beforeUsed;
    assertThat(memoryUsed).isLessThan(512 * 1024 * 1024); // 512MB
}
```

### 4.3 壓力測試

#### 併發生成測試
```java
@Test
@DisplayName("併發專案生成測試")
void concurrentGenerationTest() throws InterruptedException {
    int threadCount = 5;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                projectService.create(createUniqueCommand());
            } finally {
                latch.countDown();
            }
        });
    }
    
    boolean completed = latch.await(5, TimeUnit.MINUTES);
    assertThat(completed).isTrue();
}
```

## 5. 相容性測試

### 5.1 作業系統相容性

| 作業系統 | 版本 | 測試狀態 |
|---------|------|----------|
| Windows | 10, 11 | ✅ 通過 |
| macOS | 11+, Intel/M1 | ✅ 通過 |
| Linux | Ubuntu 20.04+ | ✅ 通過 |
| Linux | CentOS 8+ | ✅ 通過 |

### 5.2 資料庫相容性

| 資料庫 | 版本 | 測試狀態 |
|--------|------|----------|
| PostgreSQL | 12+ | ✅ 通過 |
| MySQL | 8.0+ | ✅ 通過 |
| Oracle | 11g+ | 🚧 進行中 |
| SQL Server | 2017+ | 🚧 規劃中 |

### 5.3 Java 版本相容性

| Java 版本 | 測試狀態 |
|-----------|----------|
| Java 17 | ✅ 通過 |
| Java 21 | ✅ 通過 |

## 6. 安全性測試

### 6.1 敏感資訊處理
- 資料庫密碼不應出現在日誌中
- 臨時檔案應安全清理
- 生成的配置檔案不包含硬編碼密碼

### 6.2 輸入驗證
- SQL 注入防護
- 路徑遍歷攻擊防護
- 檔案大小限制

## 7. 使用者體驗測試

### 7.1 CLI 互動測試
- 提示訊息清晰易懂
- 錯誤訊息有明確指導
- 進度指示器正常顯示
- 命令自動完成功能

### 7.2 可用性測試
- 新使用者能在 30 分鐘內完成首次使用
- 幫助文檔完整且易於理解
- 常見錯誤有解決方案指引

## 8. 測試環境

### 8.1 測試基礎設施

```yaml
# Docker Compose 測試環境
version: '3.8'
services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: testdb
      POSTGRES_USER: test
      POSTGRES_PASSWORD: test
    ports:
      - "5432:5432"
  
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: testdb
      MYSQL_USER: test
      MYSQL_PASSWORD: test
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
```

### 8.2 CI/CD 整合

```yaml
# GitHub Actions 測試流程
name: Test Suite
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java: [17, 21]
        database: [postgres, mysql]
    
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: ${{ matrix.java }}
      - name: Run tests
        run: ./gradlew test integrationTest
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## 9. 測試資料管理

### 9.1 測試資料策略
- 使用 TestContainers 提供隔離的測試環境
- 測試資料自動生成和清理
- 敏感資料使用假數據

### 9.2 測試資料範例

```sql
-- 測試資料庫結構
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    total_amount DECIMAL(10,2),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 10. 測試執行計劃

### 10.1 測試階段

| 階段 | 測試類型 | 時間安排 | 負責人 |
|------|----------|----------|--------|
| 開發階段 | 單元測試 | 持續執行 | 開發團隊 |
| 功能完成 | 整合測試 | 每週執行 | QA 團隊 |
| 發布前 | 端到端測試 | 發布前 2 週 | QA 團隊 |
| 發布前 | 性能測試 | 發布前 1 週 | 效能團隊 |
| 發布前 | 相容性測試 | 發布前 1 週 | QA 團隊 |

### 10.2 測試報告
- 每日測試結果自動郵件
- 週報包含測試覆蓋率趨勢
- 發布前提供完整測試報告

## 11. 風險和緩解措施

### 11.1 測試風險

| 風險 | 影響 | 機率 | 緩解措施 |
|------|------|------|----------|
| 測試環境不穩定 | 高 | 中 | 使用 TestContainers |
| 測試資料不一致 | 中 | 高 | 自動化測試資料管理 |
| 效能測試環境差異 | 高 | 中 | 標準化測試環境 |
| 第三方服務不可用 | 中 | 低 | Mock 服務和降級方案 |

### 11.2 測試準出標準

#### 發布標準
- ✅ 單元測試覆蓋率 ≥ 80%
- ✅ 所有整合測試通過
- ✅ 性能測試符合要求
- ✅ 沒有 P0/P1 級別缺陷
- ✅ 相容性測試通過
- ✅ 使用者體驗測試通過 