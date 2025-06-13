package io.github.cloudtechnology.generator.jooq;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.jooq.codegen.GeneratorStrategy.Mode;
import org.jooq.codegen.JavaGenerator;
import org.jooq.codegen.JavaWriter;
import org.jooq.meta.ColumnDefinition;
import org.jooq.meta.TableDefinition;
import org.jooq.meta.TypedElementDefinition;
import org.jooq.meta.UniqueKeyDefinition;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import lombok.extern.slf4j.Slf4j;

/**
 * 簡潔的 Repository 和 Entity 生成器
 * 基於 JOOQ 表定義生成對應的 Spring Data JPA Repository 和 Entity 類別
 * 
 * 主要功能：
 * 1. 在生成 POJO 時同時生成對應的 Repository 介面
 * 2. 使用 Mustache 模板引擎進行代碼生成
 * 3. 自動識別主鍵類型並配置 Repository 泛型
 * 
 * @author CloudTechnology Team
 * @version 1.0
 */
@Slf4j
public class SimpleRepositoryGenerator extends JavaGenerator {
    
    // 常量定義
    private static final String REPOSITORY_TEMPLATE_PATH = "templates/repository/JpaRepository.mustache";
    private static final String REPOSITORY_SUFFIX = "Repository";
    private static final String JAVA_FILE_EXTENSION = ".java";
    
    // 模板變數名稱常量
    private static final String TEMPLATE_VAR_PACKAGE_NAME = "packageName";
    private static final String TEMPLATE_VAR_CLASS_NAME = "className";
    private static final String TEMPLATE_VAR_POJO_CLASS_NAME = "pojoClassName";
    private static final String TEMPLATE_VAR_PRIMARY_KEY_TYPE = "primaryKeyType";

    // 儲存需要生成的 Repository 資訊
    private final java.util.List<RepositoryInfo> pendingRepositories = new java.util.ArrayList<>();

    /**
     * POJO 類別生成完成後的處理邏輯
     * 在生成 POJO 完成後，記錄需要生成的 Repository 資訊（延遲生成）
     * 
     * @param table 資料表定義
     * @param out   Java 程式碼輸出器
     */
    @Override
    protected void generatePojoClassFooter(TableDefinition table, JavaWriter out) {
        // 執行父類的標準 POJO 生成邏輯
        super.generatePojoClassFooter(table, out);
        
        // 記錄 Repository 資訊，延遲到所有生成完成後再處理
        recordRepositoryForLaterGeneration(table, out);
    }

    /**
     * 記錄需要生成的 Repository 資訊
     * 
     * @param table 資料表定義
     * @param out   Java 程式碼輸出器
     */
    private void recordRepositoryForLaterGeneration(TableDefinition table, JavaWriter out) {
        try {
            log.info("記錄資料表 {} 的 Repository 生成資訊", table.getName());
            
            // 構建類別名稱和套件資訊
            ClassNameInfo classNameInfo = buildClassNameInfo(table);
            
            // 識別主鍵類型
            Optional<String> primaryKeyType = findPrimaryKeyType(table, out);
            if (primaryKeyType.isEmpty()) {
                log.warn("資料表 {} 沒有主鍵，跳過 Repository 記錄", table.getName());
                return;
            }
            
            // 生成 Repository 內容
            Map<String, String> templateVariables = createTemplateVariables(classNameInfo, primaryKeyType.get());
            String generatedContent = generateRepositoryContent(templateVariables);
            
            // 記錄到待處理列表
            pendingRepositories.add(new RepositoryInfo(
                classNameInfo.getRepositoryClassName(),
                generatedContent
            ));
            
            log.info("已記錄資料表 {} 的 Repository 生成資訊", table.getName());
            
        } catch (Exception e) {
            log.error("記錄資料表 {} 的 Repository 資訊時發生錯誤", table.getName(), e);
        }
    }

    /**
     * 在所有生成完成後處理 Repository 檔案
     * 使用 generateCatalog 方法的結尾來確保在 JOOQ 清理後執行
     */
    @Override
    protected void generateCatalog(org.jooq.meta.CatalogDefinition catalog) {
        // 先執行父類的 catalog 生成邏輯
        super.generateCatalog(catalog);
        
        // 在 catalog 生成完成後生成所有記錄的 Repository 檔案
        generateAllPendingRepositories();
    }

    /**
     * 生成所有待處理的 Repository 檔案
     */
    private void generateAllPendingRepositories() {
        if (pendingRepositories.isEmpty()) {
            log.info("沒有待生成的 Repository 檔案");
            return;
        }
        
        log.info("開始生成 {} 個待處理的 Repository 檔案", pendingRepositories.size());
        
        for (RepositoryInfo repositoryInfo : pendingRepositories) {
            try {
                log.info("生成 Repository: {}", repositoryInfo.getClassName());
                writeRepositoryFile(repositoryInfo.getClassName(), repositoryInfo.getContent());
                log.info("✅ 成功生成 Repository: {}", repositoryInfo.getClassName());
            } catch (Exception e) {
                log.error("❌ 生成 Repository {} 時發生錯誤", repositoryInfo.getClassName(), e);
            }
        }
        
        log.info("🎉 所有 Repository 檔案生成完成！");
        pendingRepositories.clear();
    }



    /**
     * 構建類別名稱相關資訊
     * 
     * @param table 資料表定義
     * @return 類別名稱資訊物件
     */
    private ClassNameInfo buildClassNameInfo(TableDefinition table) {
        String pojoClassName = getStrategy().getJavaClassName(table, Mode.POJO);
        String repositoryClassName = pojoClassName + REPOSITORY_SUFFIX;
        // 使用專門的 Repository 包路徑
        String repositoryPackageName = getRepositoryPackageName();
        
        return new ClassNameInfo(pojoClassName, repositoryClassName, repositoryPackageName);
    }

    /**
     * 獲取 Repository 專用的包名
     * 直接使用 JOOQ 提供的目標包名，因為它已經配置為正確的 infrastructure.repositories 包
     * 
     * @return Repository 包名
     */
    private String getRepositoryPackageName() {
        String targetPackage = getStrategy().getTargetPackage();
        log.debug("JOOQ 目標包名: {}", targetPackage);
        
        // 直接使用 JOOQ 配置的目標包名，不需要額外添加後綴
        // 因為 JOOQ 配置中已經設定為 infrastructure.repositories 包
        return targetPackage;
    }

    /**
     * 獲取 Repository 專用的包名 - 備用方案
     * 如果需要，可以直接使用與 POJO 相同的包名
     * 
     * @return Repository 包名
     */
    private String getRepositoryPackageNameFallback() {
        return getStrategy().getTargetPackage();
    }

    /**
     * 構建 Repository 檔案的完整路徑
     * 
     * @param repositoryClassName Repository 類別名稱
     * @param packageName        包名
     * @return Repository 檔案路徑
     */
    private Path buildRepositoryFilePath(String repositoryClassName, String packageName) {
        String targetDirectory = getStrategy().getTargetDirectory();
        String packagePath = packageName.replace('.', '/');
        
        // 使用絕對路徑避免路徑問題
        Path baseDir = Paths.get(targetDirectory).toAbsolutePath();
        Path packageDir = baseDir.resolve(packagePath);
        
        return packageDir.resolve(repositoryClassName + JAVA_FILE_EXTENSION);
    }

    /**
     * 尋找資料表的主鍵類型
     * 
     * @param table 資料表定義
     * @param out   Java 程式碼輸出器
     * @return 主鍵類型的 Optional 包裝
     */
    private Optional<String> findPrimaryKeyType(TableDefinition table, JavaWriter out) {
        for (TypedElementDefinition<?> column : table.getColumns()) {
            ColumnDefinition columnDef = (ColumnDefinition) column;
            UniqueKeyDefinition primaryKey = columnDef.getPrimaryKey();
            
            if (primaryKey != null) {
                String javaType = getJavaType(
                    column.getType(resolver(out, Mode.POJO)), 
                    out,
                    Mode.POJO
                );
                // 簡化類型名稱：java.lang.String -> String
                String simplifiedType = simplifyTypeName(javaType);
                return Optional.of(simplifiedType);
            }
        }
        
        return Optional.empty();
    }

    /**
     * 簡化 Java 類型名稱
     * 將完整的類名轉換為簡單的類名
     * 例如：java.lang.String -> String, java.lang.Long -> Long
     * 
     * @param fullTypeName 完整的類型名稱
     * @return 簡化後的類型名稱
     */
    private String simplifyTypeName(String fullTypeName) {
        if (fullTypeName == null || fullTypeName.isEmpty()) {
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
     * 創建模板變數 Map
     * 
     * @param classNameInfo   類別名稱資訊
     * @param primaryKeyType  主鍵類型
     * @return 模板變數 Map
     */
    private Map<String, String> createTemplateVariables(ClassNameInfo classNameInfo, String primaryKeyType) {
        Map<String, String> variables = new HashMap<>();
        variables.put(TEMPLATE_VAR_PACKAGE_NAME, classNameInfo.getPackageName());
        variables.put(TEMPLATE_VAR_CLASS_NAME, classNameInfo.getRepositoryClassName());
        variables.put(TEMPLATE_VAR_POJO_CLASS_NAME, classNameInfo.getPojoClassName());
        variables.put(TEMPLATE_VAR_PRIMARY_KEY_TYPE, primaryKeyType);
        return variables;
    }

    /**
     * 使用模板生成 Repository 程式碼內容
     * 
     * @param templateVariables 模板變數
     * @return 生成的程式碼內容
     * @throws IOException 模板處理異常
     */
    private String generateRepositoryContent(Map<String, String> templateVariables) throws IOException {
        String templateContent = loadTemplateContent(REPOSITORY_TEMPLATE_PATH);
        Template template = Mustache.compiler().compile(templateContent);
        return template.execute(templateVariables);
    }

    /**
     * 寫入 Repository 檔案到指定路徑
     * 
     * @param repositoryClassName Repository 類別名稱
     * @param content            檔案內容
     * @throws IOException       檔案寫入異常
     */
    private void writeRepositoryFile(String repositoryClassName, String content) throws IOException {
        String repositoryPackage = getRepositoryPackageName();
        
        log.info("開始寫入 Repository 檔案");
        log.info("Repository 類別名稱: {}", repositoryClassName);
        log.info("Repository 包名: {}", repositoryPackage);
        log.info("內容長度: {} 字元", content.length());
        log.debug("內容預覽: {}", content.length() > 100 ? content.substring(0, 100) + "..." : content);
        
        try {
            // 嘗試使用專門的 infrastructure.repositories 包路徑
            Path repositoryFilePath = buildRepositoryFilePath(repositoryClassName, repositoryPackage);
            
            log.info("目標檔案路徑: {}", repositoryFilePath.toAbsolutePath());
            log.info("父目錄: {}", repositoryFilePath.getParent().toAbsolutePath());
            
            // 確保目錄存在
            Path parentDir = repositoryFilePath.getParent();
            if (!Files.exists(parentDir)) {
                log.info("創建目錄: {}", parentDir);
                Files.createDirectories(parentDir);
                log.info("目錄創建成功");
            } else {
                log.info("目錄已存在: {}", parentDir);
            }
            
            // 寫入檔案
            log.info("開始寫入檔案內容...");
            Files.writeString(
                repositoryFilePath,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
            
            // 驗證檔案是否真的被創建
            if (Files.exists(repositoryFilePath)) {
                long fileSize = Files.size(repositoryFilePath);
                log.info("✅ Repository 檔案已成功寫入: {}", repositoryFilePath.toAbsolutePath());
                log.info("檔案大小: {} bytes", fileSize);
            } else {
                log.error("❌ 檔案寫入後不存在: {}", repositoryFilePath.toAbsolutePath());
            }
            
        } catch (Exception e) {
            log.error("❌ 寫入 Repository 檔案時發生錯誤", e);
            
            // 備用方案：使用與 POJO 相同的包路徑
            String fallbackPackage = getRepositoryPackageNameFallback();
            Path fallbackFilePath = buildRepositoryFilePath(repositoryClassName, fallbackPackage);
            
            log.info("🔄 備用方案，寫入 Repository 到: {}", fallbackFilePath.toAbsolutePath());
            
            try {
                // 確保目錄存在
                Files.createDirectories(fallbackFilePath.getParent());
                
                // 寫入檔案
                Files.writeString(
                    fallbackFilePath,
                    content.replace("package " + repositoryPackage + ";", "package " + fallbackPackage + ";"),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
                );
                
                // 驗證備用檔案
                if (Files.exists(fallbackFilePath)) {
                    long fileSize = Files.size(fallbackFilePath);
                    log.info("✅ Repository 檔案已寫入備用路徑: {}", fallbackFilePath.toAbsolutePath());
                    log.info("檔案大小: {} bytes", fileSize);
                } else {
                    log.error("❌ 備用檔案寫入後也不存在: {}", fallbackFilePath.toAbsolutePath());
                }
                
            } catch (Exception fallbackException) {
                log.error("❌ 備用方案也失敗了", fallbackException);
                throw new IOException("Repository 檔案寫入完全失敗", fallbackException);
            }
        }
    }

    /**
     * 載入模板檔案內容
     * 
     * @param templatePath 模板檔案路徑
     * @return 模板內容字串
     * @throws IOException 檔案讀取異常
     */
    private String loadTemplateContent(String templatePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IOException("找不到模板檔案: " + templatePath);
            }
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }

    /**
     * 類別名稱資訊封裝類別
     * 用於整理和傳遞類別相關的命名資訊
     */
    private static class ClassNameInfo {
        private final String pojoClassName;
        private final String repositoryClassName;
        private final String packageName;

        /**
         * 建構子
         * 
         * @param pojoClassName       POJO 類別名稱
         * @param repositoryClassName Repository 類別名稱
         * @param packageName         套件名稱
         */
        public ClassNameInfo(String pojoClassName, String repositoryClassName, String packageName) {
            this.pojoClassName = pojoClassName;
            this.repositoryClassName = repositoryClassName;
            this.packageName = packageName;
        }

        public String getPojoClassName() {
            return pojoClassName;
        }

        public String getRepositoryClassName() {
            return repositoryClassName;
        }

        public String getPackageName() {
            return packageName;
        }
    }

    /**
     * Repository 資訊封裝類別
     * 用於暫存待生成的 Repository 檔案資訊
     */
    private static class RepositoryInfo {
        private final String className;
        private final String content;

        /**
         * 建構子
         * 
         * @param className Repository 類別名稱
         * @param content   Repository 檔案內容
         */
        public RepositoryInfo(String className, String content) {
            this.className = className;
            this.content = content;
        }

        public String getClassName() {
            return className;
        }

        public String getContent() {
            return content;
        }
    }
} 