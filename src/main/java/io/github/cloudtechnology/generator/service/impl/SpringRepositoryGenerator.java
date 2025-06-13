package io.github.cloudtechnology.generator.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import io.github.cloudtechnology.generator.jooq.SimpleRepositoryGenerator.TableMetadata;
import io.github.cloudtechnology.generator.service.RepositoryGenerator;
import io.github.cloudtechnology.generator.vo.RepositoryVo;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Data Repository 生成器
 * 基於 JOOQ 收集的可靠表定義信息生成對應的 Spring Data JPA Repository 介面
 * 
 * 主要功能：
 * 1. 讀取 JOOQ 生成的表元數據 JSON 文件
 * 2. 基於可靠的 TableDefinition 信息生成 Repository 介面
 * 3. 自動配置正確的包路徑和類型信息
 * 4. 使用 Mustache 模板引擎生成乾淨的代碼
 * 
 * 設計原則：
 * - 職責單一：專注於 Repository 文件生成
 * - 數據可靠：使用 JOOQ TableDefinition 提供的準確信息
 * - 錯誤處理：完善的異常處理和日誌記錄
 * - 易於維護：清晰的方法分離和文檔說明
 * 
 * @author CloudTechnology Team
 * @version 2.0
 */
@Slf4j
@Service("springRepositoryGenerator")
public class SpringRepositoryGenerator implements RepositoryGenerator {
    
    // 常量定義
    private static final String REPOSITORY_TEMPLATE_PATH = "templates/repository/JpaRepository.mustache";
    private static final String METADATA_FILE_NAME = "repository-metadata.json";
    private static final String REPOSITORY_SUFFIX = "Repository";
    private static final String REPOSITORY_PACKAGE_SUFFIX = ".infrastructure.repositories";
    
    // 模板變數名稱常量
    private static final String TEMPLATE_VAR_PACKAGE_NAME = "packageName";
    private static final String TEMPLATE_VAR_CLASS_NAME = "className";
    private static final String TEMPLATE_VAR_POJO_CLASS_NAME = "pojoClassName";
    private static final String TEMPLATE_VAR_PRIMARY_KEY_TYPE = "primaryKeyType";
    
    @Override
    public void generate(RepositoryVo repositoryVo) throws Exception {
        log.info("🚀 開始生成 Spring Data Repository 介面");
        
        // 1. 讀取 JOOQ 生成的表元數據
        List<TableMetadata> tableMetadataList = loadTableMetadata(repositoryVo);
        
        if (tableMetadataList.isEmpty()) {
            log.warn("⚠️ 沒有找到任何表元數據，跳過 Repository 生成");
            return;
        }
        
        log.info("📋 找到 {} 個表的元數據，開始生成對應的 Repository", tableMetadataList.size());
        
        // 2. 為每個表生成對應的 Repository
        int successCount = 0;
        int failCount = 0;
        
        for (TableMetadata metadata : tableMetadataList) {
            try {
                generateRepositoryForTable(repositoryVo, metadata);
                successCount++;
                log.info("✅ 成功生成 Repository: {}Repository", metadata.getPojoClassName());
            } catch (Exception e) {
                failCount++;
                log.error("❌ 生成 {} Repository 時發生錯誤", metadata.getPojoClassName(), e);
            }
        }
        
        log.info("🎉 Repository 介面生成完成！成功: {}, 失敗: {}", successCount, failCount);
    }
    
    /**
     * 載入 JOOQ 生成的表元數據
     * 從 JSON 文件中讀取可靠的表定義信息
     * 
     * @param repositoryVo Repository 配置信息
     * @return 表元數據列表
     * @throws IOException 檔案讀取異常
     */
    private List<TableMetadata> loadTableMetadata(RepositoryVo repositoryVo) throws IOException {
        // 構建元數據文件路徑
        Path metadataFilePath = repositoryVo.projectTempPath()
                                          .resolve("src/main/java")
                                          .resolve(METADATA_FILE_NAME);
        
        log.info("🔍 讀取表元數據文件: {}", metadataFilePath.toAbsolutePath());
        
        if (!Files.exists(metadataFilePath)) {
            log.warn("⚠️ 表元數據文件不存在: {}", metadataFilePath);
            throw new IOException("找不到表元數據文件: " + metadataFilePath);
        }
        
        try {
            // 讀取 JSON 文件內容
            String jsonContent = Files.readString(metadataFilePath, StandardCharsets.UTF_8);
            
            // 解析為 TableMetadata 對象列表
            ObjectMapper objectMapper = new ObjectMapper();
            List<TableMetadata> tableMetadataList = objectMapper.readValue(
                jsonContent, 
                new TypeReference<List<TableMetadata>>() {}
            );
            
            log.info("✅ 成功載入 {} 個表的元數據", tableMetadataList.size());
            
            // 記錄詳細信息用於調試
            for (TableMetadata metadata : tableMetadataList) {
                log.debug("📊 表元數據: {}", metadata);
            }
            
            return tableMetadataList;
            
        } catch (Exception e) {
            log.error("❌ 解析表元數據文件時發生錯誤: {}", metadataFilePath, e);
            throw new IOException("解析表元數據文件失敗", e);
        }
    }
    
    /**
     * 為單個表生成對應的 Repository 介面
     * 
     * @param repositoryVo Repository 配置信息
     * @param metadata     表元數據信息
     * @throws IOException 檔案操作異常
     */
    private void generateRepositoryForTable(RepositoryVo repositoryVo, TableMetadata metadata) throws IOException {
        log.debug("📝 開始生成 Repository: {}", metadata.getPojoClassName());
        
        // 準備模板變數
        RepositoryInfo repositoryInfo = buildRepositoryInfo(repositoryVo, metadata);
        Map<String, Object> templateVariables = createTemplateVariables(repositoryInfo, metadata);
        
        // 生成 Repository 內容
        String repositoryContent = generateRepositoryContent(templateVariables);
        
        // 寫入 Repository 檔案
        writeRepositoryFile(repositoryVo, repositoryInfo, repositoryContent);
        
        log.debug("✅ Repository 檔案生成完成: {}", repositoryInfo.getClassName());
    }
    
    /**
     * 構建 Repository 相關信息
     * 計算 Repository 類別名稱、包名等信息
     * 
     * @param repositoryVo Repository 配置信息
     * @param metadata     表元數據信息
     * @return Repository 信息對象
     */
    private RepositoryInfo buildRepositoryInfo(RepositoryVo repositoryVo, TableMetadata metadata) {
        String repositoryClassName = metadata.getPojoClassName() + REPOSITORY_SUFFIX;
        String repositoryPackageName = repositoryVo.packageName() + REPOSITORY_PACKAGE_SUFFIX;
        
        return new RepositoryInfo(repositoryClassName, repositoryPackageName);
    }
    
    /**
     * 創建模板變數 Map
     * 準備 Mustache 模板所需的所有變數
     * 
     * @param repositoryInfo Repository 信息
     * @param metadata       表元數據信息
     * @return 模板變數 Map
     */
    private Map<String, Object> createTemplateVariables(RepositoryInfo repositoryInfo, TableMetadata metadata) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(TEMPLATE_VAR_PACKAGE_NAME, repositoryInfo.getPackageName());
        variables.put(TEMPLATE_VAR_CLASS_NAME, repositoryInfo.getClassName());
        variables.put(TEMPLATE_VAR_POJO_CLASS_NAME, metadata.getPojoClassName());
        variables.put(TEMPLATE_VAR_PRIMARY_KEY_TYPE, metadata.getPrimaryKeyType());
        
        // 計算 POJO 的完整 import 路徑
        String pojoImportPath = metadata.getPojoPackageName() + "." + metadata.getPojoClassName();
        variables.put("pojoImportPath", pojoImportPath);
        
        log.debug("🔧 模板變數: {}", variables);
        
        return variables;
    }
    
    /**
     * 使用模板生成 Repository 內容
     * 
     * @param templateVariables 模板變數
     * @return 生成的 Repository 程式碼內容
     * @throws IOException 模板處理異常
     */
    private String generateRepositoryContent(Map<String, Object> templateVariables) throws IOException {
        String templateContent = loadTemplateContent(REPOSITORY_TEMPLATE_PATH);
        Template template = Mustache.compiler().compile(templateContent);
        return template.execute(templateVariables);
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
     * 寫入 Repository 檔案
     * 
     * @param repositoryVo   Repository 配置信息
     * @param repositoryInfo Repository 信息
     * @param content        Repository 檔案內容
     * @throws IOException 檔案寫入異常
     */
    private void writeRepositoryFile(RepositoryVo repositoryVo, RepositoryInfo repositoryInfo, 
                                   String content) throws IOException {
        // 構建檔案路徑
        String packagePath = repositoryInfo.getPackageName().replace('.', '/');
        Path repositoryDir = repositoryVo.projectTempPath()
                                       .resolve("src/main/java")
                                       .resolve(packagePath);
        Path repositoryFilePath = repositoryDir.resolve(repositoryInfo.getClassName() + ".java");
        
        log.debug("📂 Repository 檔案路徑: {}", repositoryFilePath.toAbsolutePath());
        
        // 確保目錄存在
        if (!Files.exists(repositoryDir)) {
            log.debug("📁 創建目錄: {}", repositoryDir);
            Files.createDirectories(repositoryDir);
        }
        
        // 寫入檔案
        Files.writeString(
            repositoryFilePath,
            content,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        );
        
        // 驗證檔案是否成功寫入
        if (Files.exists(repositoryFilePath)) {
            long fileSize = Files.size(repositoryFilePath);
            log.debug("📝 Repository 檔案已寫入: {} ({} bytes)", 
                     repositoryFilePath.toAbsolutePath(), fileSize);
        } else {
            throw new IOException("Repository 檔案寫入失敗: " + repositoryFilePath);
        }
    }
    
    /**
     * Repository 信息封裝類別
     * 用於儲存 Repository 的基本信息
     */
    private static class RepositoryInfo {
        private final String className;
        private final String packageName;
        
        /**
         * 建構子
         * 
         * @param className   Repository 類別名稱
         * @param packageName Repository 包名
         */
        public RepositoryInfo(String className, String packageName) {
            this.className = className;
            this.packageName = packageName;
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getPackageName() {
            return packageName;
        }
        
        @Override
        public String toString() {
            return String.format("RepositoryInfo{類別名稱='%s', 包名='%s'}", className, packageName);
        }
    }
} 