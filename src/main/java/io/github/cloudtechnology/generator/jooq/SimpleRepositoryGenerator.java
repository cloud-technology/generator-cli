package io.github.cloudtechnology.generator.jooq;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jooq.codegen.GeneratorStrategy.Mode;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.TypedElementDefinition;
import org.jooq.meta.UniqueKeyDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * JOOQ 表定義信息收集器
 * 專責收集 TableDefinition 的元數據信息，為後續的 Repository 生成提供可靠的數據源
 * 
 * 主要功能：
 * 1. 在生成 POJO 時收集表定義信息
 * 2. 從 TableDefinition 直接獲取主鍵類型（最可靠的方法）
 * 3. 輸出 JSON 格式的中間數據文件供其他生成器使用
 * 4. 遵循職責單一原則，不直接生成 Repository 文件
 * 
 * @author CloudTechnology Team
 * @version 2.0
 */
@Slf4j
public class SimpleRepositoryGenerator extends JavaGenerator {
    
    // 常量定義
    private static final String METADATA_FILE_NAME = "repository-metadata.json";
    
    // 儲存收集到的表定義信息
    private final List<TableMetadata> collectedTables = new ArrayList<>();

    /**
     * POJO 類別生成完成後的處理邏輯
     * 收集表定義信息，但不直接生成 Repository 文件
     * 
     * @param table 資料表定義
     * @param out   Java 程式碼輸出器
     */
    @Override
    protected void generatePojoClassFooter(TableDefinition table, JavaWriter out) {
        // 執行父類的標準 POJO 生成邏輯
        super.generatePojoClassFooter(table, out);
        
        // 收集表定義信息
        collectTableMetadata(table, out);
        
        // 在每個 POJO 生成完成後，檢查是否需要輸出 JSON 檔案
        // 這是一個安全的觸發點，確保至少有一些元數據被收集
        tryOutputMetadata();
    }

    /**
     * 收集表定義的元數據信息
     * 
     * @param table 資料表定義
     * @param out   Java 程式碼輸出器
     */
    private void collectTableMetadata(TableDefinition table, JavaWriter out) {
        try {
            log.info("📊 收集資料表 {} 的元數據信息", table.getName());
            
            // 獲取 POJO 類別名稱
            String pojoClassName = getStrategy().getJavaClassName(table, Mode.POJO);
            
            // 獲取包名信息
            String pojoPackageName = getStrategy().getJavaPackageName(table, Mode.POJO);
            
            // 識別主鍵類型
            Optional<String> primaryKeyType = extractPrimaryKeyType(table, out);
            if (primaryKeyType.isEmpty()) {
                log.warn("⚠️ 資料表 {} 沒有主鍵，跳過收集", table.getName());
                return;
            }
            
            // 創建表元數據對象
            TableMetadata metadata = new TableMetadata(
                table.getName(),
                pojoClassName,
                pojoPackageName,
                primaryKeyType.get()
            );
            
            collectedTables.add(metadata);
            
            log.info("✅ 成功收集資料表 {} 的元數據信息", table.getName());
            log.debug("🔍 表元數據: {}", metadata);
            
        } catch (Exception e) {
            log.error("❌ 收集資料表 {} 的元數據時發生錯誤", table.getName(), e);
        }
    }

    /**
     * 嘗試輸出元數據檔案
     * 只有在還沒有輸出過檔案的情況下才會輸出
     */
    private boolean metadataOutputted = false;
    
    private synchronized void tryOutputMetadata() {
        if (!metadataOutputted && !collectedTables.isEmpty()) {
            log.info("🎯 觸發元數據輸出（已收集 {} 個表）", collectedTables.size());
            outputRepositoryMetadata();
            metadataOutputted = true;
        }
    }

    /**
     * 在所有生成完成後輸出中間數據文件
     * 使用 generateCatalog 方法的結尾來確保在所有表處理完成後執行
     */
    @Override
    protected void generateCatalog(org.jooq.meta.CatalogDefinition catalog) {
        log.info("🔄 SimpleRepositoryGenerator.generateCatalog() 被調用");
        
        // 先執行父類的 catalog 生成邏輯
        super.generateCatalog(catalog);
        
        // 在 catalog 生成完成後輸出中間數據文件
        outputRepositoryMetadata();
    }

    /**
     * Schema 生成完成後的處理邏輯
     * 作為備用的觸發點來確保 JSON 檔案被生成
     */
    @Override
    protected void generateSchemaClassFooter(org.jooq.meta.SchemaDefinition schema, JavaWriter out) {
        log.info("🔄 SimpleRepositoryGenerator.generateSchemaClassFooter() 被調用");
        
        // 執行父類邏輯
        super.generateSchemaClassFooter(schema, out);
        
        // 嘗試輸出 JSON 檔案（如果還沒有輸出的話）
        if (!collectedTables.isEmpty()) {
            log.info("📋 在 Schema 完成後觸發元數據輸出");
            outputRepositoryMetadata();
        }
    }

    /**
     * 輸出 Repository 元數據到 JSON 文件
     * 供 SpringRepositoryGenerator 使用
     */
    private void outputRepositoryMetadata() {
        log.info("🔍 outputRepositoryMetadata() 被調用，已收集表數量: {}", collectedTables.size());
        
        if (collectedTables.isEmpty()) {
            log.info("📋 沒有收集到任何表元數據");
            return;
        }
        
        try {
            log.info("📝 開始輸出 {} 個表的元數據信息", collectedTables.size());
            
            // 構建輸出文件路徑
            String targetDirectory = getStrategy().getTargetDirectory();
            log.info("🎯 目標目錄: {}", targetDirectory);
            
            Path metadataFilePath = Paths.get(targetDirectory).resolve(METADATA_FILE_NAME);
            log.info("📄 元數據檔案路徑: {}", metadataFilePath.toAbsolutePath());
            
            // 序列化為 JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                                            .writeValueAsString(collectedTables);
            
            log.info("📄 JSON 內容長度: {} 字符", jsonContent.length());
            log.debug("📄 JSON 內容預覽: {}", jsonContent.substring(0, Math.min(200, jsonContent.length())));
            
            // 寫入文件
            Files.writeString(
                metadataFilePath,
                jsonContent,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
            
            // 驗證檔案是否成功寫入
            if (Files.exists(metadataFilePath)) {
                long fileSize = Files.size(metadataFilePath);
                log.info("✅ Repository 元數據已輸出到: {} (檔案大小: {} bytes)", 
                        metadataFilePath.toAbsolutePath(), fileSize);
            } else {
                log.error("❌ 檔案寫入後不存在: {}", metadataFilePath.toAbsolutePath());
            }
            
            log.info("📊 共包含 {} 個表的信息", collectedTables.size());
            
        } catch (Exception e) {
            log.error("❌ 輸出 Repository 元數據時發生錯誤", e);
        }
    }

    /**
     * 從 TableDefinition 提取主鍵類型
     * 這是最可靠的方法，直接從 JOOQ 的表定義中獲取
     * 
     * @param table 資料表定義
     * @param out   Java 程式碼輸出器
     * @return 主鍵類型的 Optional 包裝
     */
    private Optional<String> extractPrimaryKeyType(TableDefinition table, JavaWriter out) {
        for (TypedElementDefinition<?> column : table.getColumns()) {
            ColumnDefinition columnDef = (ColumnDefinition) column;
            UniqueKeyDefinition primaryKey = columnDef.getPrimaryKey();
            
            if (primaryKey != null) {
                // 獲取 Java 類型
                String javaType = getJavaType(
                    column.getType(resolver(out, Mode.POJO)), 
                    out,
                    Mode.POJO
                );
                
                // 簡化類型名稱
                String simplifiedType = simplifyJavaTypeName(javaType);
                
                log.debug("🔑 資料表 {} 的主鍵類型: {} -> {}", 
                         table.getName(), javaType, simplifiedType);
                
                return Optional.of(simplifiedType);
            }
        }
        
        log.warn("⚠️ 資料表 {} 沒有找到主鍵定義", table.getName());
        return Optional.empty();
    }

    /**
     * 簡化 Java 類型名稱
     * 將完整的類名轉換為簡單的類名
     * 
     * 範例：
     * - java.lang.String -> String
     * - java.lang.Long -> Long
     * - java.time.LocalDateTime -> LocalDateTime
     * 
     * @param fullTypeName 完整的類型名稱
     * @return 簡化後的類型名稱
     */
    private String simplifyJavaTypeName(String fullTypeName) {
        if (fullTypeName == null || fullTypeName.trim().isEmpty()) {
            return fullTypeName;
        }
        
        // 處理泛型類型 (例如: List<String>)
        if (fullTypeName.contains("<")) {
            return fullTypeName; // 泛型類型保持原樣
        }
        
        // 獲取最後一個點號後的類名
        int lastDotIndex = fullTypeName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fullTypeName.length() - 1) {
            return fullTypeName.substring(lastDotIndex + 1);
        }
        
        return fullTypeName;
    }

    /**
     * 表元數據封裝類別
     * 用於儲存從 TableDefinition 提取的重要信息
     */
    public static class TableMetadata {
        private String tableName;           // 資料表名稱
        private String pojoClassName;       // POJO 類別名稱
        private String pojoPackageName;     // POJO 包名
        private String primaryKeyType;      // 主鍵類型

        /**
         * 默認建構子（Jackson 序列化需要）
         */
        public TableMetadata() {
        }

        /**
         * 完整建構子
         * 
         * @param tableName        資料表名稱
         * @param pojoClassName    POJO 類別名稱
         * @param pojoPackageName  POJO 包名
         * @param primaryKeyType   主鍵類型
         */
        public TableMetadata(String tableName, String pojoClassName, 
                           String pojoPackageName, String primaryKeyType) {
            this.tableName = tableName;
            this.pojoClassName = pojoClassName;
            this.pojoPackageName = pojoPackageName;
            this.primaryKeyType = primaryKeyType;
        }

        // Getters and Setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }

        public String getPojoClassName() { return pojoClassName; }
        public void setPojoClassName(String pojoClassName) { this.pojoClassName = pojoClassName; }

        public String getPojoPackageName() { return pojoPackageName; }
        public void setPojoPackageName(String pojoPackageName) { this.pojoPackageName = pojoPackageName; }

        public String getPrimaryKeyType() { return primaryKeyType; }
        public void setPrimaryKeyType(String primaryKeyType) { this.primaryKeyType = primaryKeyType; }

        @Override
        public String toString() {
            return String.format("TableMetadata{表名='%s', POJO類別='%s', 包名='%s', 主鍵類型='%s'}", 
                               tableName, pojoClassName, pojoPackageName, primaryKeyType);
        }
    }
} 