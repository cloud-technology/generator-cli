package io.github.cloudtechnology.generator.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import io.github.cloudtechnology.generator.bo.BuildToolEnum;
import io.github.cloudtechnology.generator.bo.RuntimeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Validated
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Configuration
@ConfigurationProperties(prefix = "cli")
public class ApplicationProperties {
    /**
     * 編譯工具
     */
    @NotNull
    BuildToolEnum buildTool;
    /**
     * 產生的 package 名稱
     */
    @NotBlank
    String groupId = "com.example";
    /**
     * 產生的專案名稱
     */
    @NotBlank
    String artifactId = "demo";
    /**
     * 產生的專案名稱
     */
    @NotBlank
    String name = "demo";
    /**
     * 專案描述
     */
    @NotNull
    String description = "Demo project for Spring Boot";
    /*
     * package 根路徑
     */
    @NotBlank
    String packageName = "com.example.demo";
    /**
     * Java 版本
     */
    @NotBlank
    String jvmVersion = "17";
    /**
     * OpenAPI 檔案路徑
     */
    String openapiFilePath;
    /**
     * 資料庫連線
     */
    String dbUrl;

    String dbUsername;

    String dbPassword;
    /**
     * 運行環境
     */
    @NotNull
    RuntimeEnum runtime;
}
