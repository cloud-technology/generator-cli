package io.github.cloudtechnology.generator.configuration;

import java.io.Serializable;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

// https://www.kabisa.nl/tech/using-jackson-builders-in-spring-boot-native
// https://github.com/spring-projects/spring-authorization-server/issues/1380
// https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator/src/main/resources/JavaSpring/model.mustache

@Slf4j
@ImportRuntimeHints(RuntimeHintsConfig.TemplateResourcesRegistrar.class)
@Configuration
public class RuntimeHintsConfig {

  static class TemplateResourcesRegistrar implements RuntimeHintsRegistrar {

    Set<Class<?>> javaClasses = Set.of(
      ArrayList.class,
      Date.class,
      Duration.class,
      Instant.class,
      URL.class,
      TreeMap.class,
      HashMap.class,
      LinkedHashMap.class,
      List.class
    );

    List<String> classNames = Arrays.asList(
      "com.github.benmanes.caffeine.cache.PSAMS",
      "io.swagger.v3.oas.models.media.JsonSchema",
      "io.swagger.v3.oas.models.examples.Example",
      "io.swagger.v3.oas.models.responses.ApiResponse",
      "io.swagger.v3.oas.models.media.Content",
      "io.swagger.v3.oas.models.media.MediaType",
      "io.swagger.v3.oas.models.parameters.RequestBody",
      "io.swagger.v3.oas.models.PathItem",
      "io.swagger.v3.oas.models.Operation",
      "org.openapitools.codegen.CodegenModel",
      "org.openapitools.codegen.CodegenOperation",
      "org.openapitools.codegen.CodegenParameter",
      "org.openapitools.codegen.CodegenProperty",
      "org.openapitools.codegen.CodegenResponse",
      "org.openapitools.codegen.CodegenSecurity",
      "io.github.cloudtechnology.generator.jooq.JooqJavaGenerator",
      "io.github.cloudtechnology.generator.jooq.CustomNamingStrategy",
      "org.jooq.meta.postgres.PostgresDatabase",
      "liquibase.resource.PathHandlerFactory"
    );

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
      this.registerStaticResources(hints);
      // 處理 java.lang.ClassNotFoundException: com.github.benmanes.caffeine.cache.SSMSA
      hints
        .reflection()
        .registerType(
          TypeReference.of("com.github.benmanes.caffeine.cache.SSMSA"),
          builder ->
            builder.withConstructor(
              List.of(
                TypeReference.of("com.github.benmanes.caffeine.cache.Caffeine"),
                TypeReference.of(
                  "com.github.benmanes.caffeine.cache.AsyncCacheLoader"
                ),
                TypeReference.of("boolean")
              ),
              ExecutableMode.INVOKE
            )
        );
      javaClasses.forEach(type -> {
        this.registerClass(hints, type);
      });
      classNames
        .stream()
        .forEach(packageName -> {
          this.registerClass(hints, packageName);
        });
    }

    /**
     * 註冊靜態資源訪問提示。
     *
     * @param hints RuntimeHints實例用於註冊。
     */
    private void registerStaticResources(RuntimeHints hints) {
      hints
        .resources()
        .registerPattern("templates/**")
        .registerPattern("static/**")
        .registerPattern("JavaSpring/**");
    }

    private void registerClass(RuntimeHints hints, String className) {
      var memberCategories = MemberCategory.values();
      var typeReference = TypeReference.of(className);
      hints.reflection().registerType(typeReference, memberCategories);
      try {
        var clzz = Class.forName(typeReference.getName());
        if (Serializable.class.isAssignableFrom(clzz)) {
          hints.serialization().registerType(typeReference);
        }
      } catch (Throwable t) { //
        log.error(
          "couldn't register serialization hint for " +
          typeReference.getName() +
          ":" +
          t.getMessage()
        );
      }
    }

    private void registerClass(RuntimeHints hints, Class<?> type) {
      var memberCategories = MemberCategory.values();
      var typeReference = TypeReference.of(type);
      hints.reflection().registerType(typeReference, memberCategories);
      try {
        var clzz = Class.forName(typeReference.getName());
        if (Serializable.class.isAssignableFrom(clzz)) {
          hints.serialization().registerType(typeReference);
        }
      } catch (Throwable t) { //
        log.error(
          "couldn't register serialization hint for " +
          typeReference.getName() +
          ":" +
          t.getMessage()
        );
      }
    }
  }
}
