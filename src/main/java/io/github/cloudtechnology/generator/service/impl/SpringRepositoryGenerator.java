package io.github.cloudtechnology.generator.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import io.github.cloudtechnology.generator.service.RepositoryGenerator;
import io.github.cloudtechnology.generator.vo.RepositoryVo;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Data Repository 生成器
 * 基於已生成的 JOOQ POJO 類別來創建對應的 Spring Data JPA Repository 介面
 * 
 * 主要功能：
 * 1. 掃描已生成的 JOOQ POJO 檔案
 * 2. 從 @Id 註解中提取主鍵類型信息
 * 3. 生成對應的 Spring Data Repository 介面
 * 4. 避免與 JOOQ 清理機制的衝突
 * 
 * @author CloudTechnology Team
 * @version 1.0
 */
@Slf4j
@Service("springRepositoryGenerator")
public class SpringRepositoryGenerator implements RepositoryGenerator {
    
    private static final String REPOSITORY_TEMPLATE_PATH = "templates/repository/JpaRepository.mustache";
    private static final String REPOSITORY_SUFFIX = "Repository";
    private static final String POJO_PACKAGE_SUFFIX = ".infrastructure.repositories.tables.pojos";
    private static final String REPOSITORY_PACKAGE_SUFFIX = ".infrastructure.repositories";
    
    @Override
    public void generate(RepositoryVo repositoryVo) throws Exception {
        log.info("🚀 開始生成 Spring Data Repository 介面");
        
        // 1. 掃描已生成的 POJO 檔案
        List<PojoInfo> pojoInfos = scanGeneratedPojoFiles(repositoryVo);
        
        if (pojoInfos.isEmpty()) {
            log.warn("⚠️ 沒有找到任何 POJO 檔案，跳過 Repository 生成");
            return;
        }
        
        log.info("📋 找到 {} 個 POJO 類別，開始生成對應的 Repository", pojoInfos.size());
        
        // 2. 為每個 POJO 生成對應的 Repository
        for (PojoInfo pojoInfo : pojoInfos) {
            try {
                generateRepositoryForPojo(repositoryVo, pojoInfo);
                log.info("✅ 成功生成 Repository: {}Repository", pojoInfo.getClassName());
            } catch (Exception e) {
                log.error("❌ 生成 {} Repository 時發生錯誤", pojoInfo.getClassName(), e);
            }
        }
        
        log.info("🎉 Spring Data Repository 介面生成完成！總共生成 {} 個 Repository", pojoInfos.size());
    }
    
    /**
     * 掃描已生成的 POJO 檔案
     * 
     * @param repositoryVo Repository 配置信息
     * @return POJO 信息列表
     * @throws IOException 檔案讀取異常
     */
    private List<PojoInfo> scanGeneratedPojoFiles(RepositoryVo repositoryVo) throws IOException {
        List<PojoInfo> pojoInfos = new ArrayList<>();
        
        // 構建 POJO 檔案路徑
        String basePackage = repositoryVo.packageName();
        String pojoPackage = basePackage + POJO_PACKAGE_SUFFIX;
        String packagePath = pojoPackage.replace('.', '/');
        Path pojoDirectory = repositoryVo.projectTempPath().resolve("src/main/java").resolve(packagePath);
        
        log.info("🔍 掃描 POJO 目錄: {}", pojoDirectory.toAbsolutePath());
        
        if (!Files.exists(pojoDirectory)) {
            log.warn("⚠️ POJO 目錄不存在: {}", pojoDirectory);
            return pojoInfos;
        }
        
        // 掃描所有 .java 檔案，但排除 Entity、Repository 和其他非 POJO 文件
        try (Stream<Path> paths = Files.walk(pojoDirectory)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                 .filter(path -> !path.getFileName().toString().equals("package-info.java"))
                 .filter(path -> !path.getFileName().toString().endsWith("Entity.java"))
                 .filter(path -> !path.getFileName().toString().endsWith("Repository.java"))
                 .filter(path -> !path.getFileName().toString().contains("DefaultCatalog"))
                 .filter(path -> !path.getFileName().toString().contains("Public"))
                 .forEach(path -> {
                     try {
                         PojoInfo pojoInfo = analyzePojoFile(path, pojoPackage);
                         if (pojoInfo != null) {
                             pojoInfos.add(pojoInfo);
                             log.debug("📄 分析 POJO: {} (主鍵類型: {})", pojoInfo.getClassName(), pojoInfo.getPrimaryKeyType());
                         }
                     } catch (Exception e) {
                         log.warn("⚠️ 分析 POJO 檔案失敗: {}", path, e);
                     }
                 });
        }
        
        return pojoInfos;
    }
    
    /**
     * 分析 POJO 檔案，提取類名和主鍵類型
     * 
     * @param pojoFilePath POJO 檔案路徑
     * @param packageName  包名
     * @return POJO 信息，如果無法解析則返回 null
     * @throws IOException 檔案讀取異常
     */
    private PojoInfo analyzePojoFile(Path pojoFilePath, String packageName) throws IOException {
        String content = Files.readString(pojoFilePath, StandardCharsets.UTF_8);
        
        // 提取類名
        String className = extractClassName(pojoFilePath.getFileName().toString());
        if (className == null) {
            return null;
        }
        
        // 提取主鍵類型（從 @Id 註解的欄位）
        String primaryKeyType = extractPrimaryKeyType(content);
        if (primaryKeyType == null) {
            log.debug("⚠️ POJO {} 沒有找到 @Id 註解，跳過 Repository 生成", className);
            return null;
        }
        
        return new PojoInfo(className, primaryKeyType, packageName);
    }
    
    /**
     * 從檔案名提取類名
     */
    private String extractClassName(String fileName) {
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return null;
    }
    
    /**
     * 從 POJO 內容中提取主鍵類型
     * 查找 @Id 註解修飾的 getter 方法，並從中提取回傳類型
     * 
     * @param content POJO 檔案內容
     * @return 主鍵類型，如果找不到則返回 null
     */
    private String extractPrimaryKeyType(String content) {
        // 匹配 @Id 註解後的 getter 方法
        // 支援多行格式，例如：
        // @Id
        // @Column(...)
        // public String getId() {
        Pattern idPattern = Pattern.compile(
            "@Id\\s*(?:@[^\\n]*\\n\\s*)*\\s*public\\s+([A-Za-z][A-Za-z0-9_.<>]*)\\s+get\\w+\\s*\\(\\s*\\)", 
            Pattern.MULTILINE | Pattern.DOTALL
        );
        
        Matcher matcher = idPattern.matcher(content);
        if (matcher.find()) {
            String fullType = matcher.group(1);
            return simplifyTypeName(fullType);
        }
        
        return null;
    }
    
    /**
     * 簡化類型名稱
     * java.lang.String -> String
     * java.lang.Long -> Long
     */
    private String simplifyTypeName(String fullTypeName) {
        if (fullTypeName == null || fullTypeName.isEmpty()) {
            return fullTypeName;
        }
        
        // 處理泛型類型
        if (fullTypeName.contains("<")) {
            return fullTypeName;
        }
        
        // 獲取最後一個點號後的類名
        int lastDotIndex = fullTypeName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fullTypeName.length() - 1) {
            return fullTypeName.substring(lastDotIndex + 1);
        }
        
        return fullTypeName;
    }
    
    /**
     * 為單個 POJO 生成對應的 Repository
     * 
     * @param repositoryVo Repository 配置信息
     * @param pojoInfo     POJO 信息
     * @throws IOException 檔案操作異常
     */
    private void generateRepositoryForPojo(RepositoryVo repositoryVo, PojoInfo pojoInfo) throws IOException {
        // 準備模板變數
        String repositoryPackage = repositoryVo.packageName() + REPOSITORY_PACKAGE_SUFFIX;
        String repositoryClassName = pojoInfo.getClassName() + REPOSITORY_SUFFIX;
        
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("packageName", repositoryPackage);
        templateVariables.put("className", repositoryClassName);
        templateVariables.put("pojoClassName", pojoInfo.getClassName());
        templateVariables.put("primaryKeyType", pojoInfo.getPrimaryKeyType());
        
        // 生成內容
        String repositoryContent = generateRepositoryContent(templateVariables);
        
        // 寫入檔案
        writeRepositoryFile(repositoryVo, repositoryClassName, repositoryContent, repositoryPackage);
    }
    
    /**
     * 使用模板生成 Repository 內容
     */
    private String generateRepositoryContent(Map<String, Object> templateVariables) throws IOException {
        String templateContent = loadTemplateContent(REPOSITORY_TEMPLATE_PATH);
        Template template = Mustache.compiler().compile(templateContent);
        return template.execute(templateVariables);
    }
    
    /**
     * 載入模板檔案內容
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
     */
    private void writeRepositoryFile(RepositoryVo repositoryVo, String repositoryClassName, 
                                   String content, String repositoryPackage) throws IOException {
        // 構建檔案路徑
        String packagePath = repositoryPackage.replace('.', '/');
        Path repositoryDir = repositoryVo.projectTempPath().resolve("src/main/java").resolve(packagePath);
        Path repositoryFilePath = repositoryDir.resolve(repositoryClassName + ".java");
        
        // 確保目錄存在
        Files.createDirectories(repositoryDir);
        
        // 寫入檔案
        Files.writeString(
            repositoryFilePath,
            content,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        );
        
        log.debug("📝 Repository 檔案已寫入: {}", repositoryFilePath.toAbsolutePath());
    }
    
    /**
     * POJO 信息封裝類別
     */
    private static class PojoInfo {
        private final String className;
        private final String primaryKeyType;
        private final String packageName;
        
        public PojoInfo(String className, String primaryKeyType, String packageName) {
            this.className = className;
            this.primaryKeyType = primaryKeyType;
            this.packageName = packageName;
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getPrimaryKeyType() {
            return primaryKeyType;
        }
        
        public String getPackageName() {
            return packageName;
        }
    }
} 