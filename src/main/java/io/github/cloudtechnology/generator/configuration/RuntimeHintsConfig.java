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

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class to register runtime hints for reflection, resources, and serialization
 * to support AOT (Ahead-of-Time) compilation in Spring applications.
 * Ref:
 * https://www.kabisa.nl/tech/using-jackson-builders-in-spring-boot-native
 * https://github.com/spring-projects/spring-authorization-server/issues/1380
 * https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator/src/main/resources/JavaSpring/model.mustache
 */
@Slf4j
@ImportRuntimeHints(RuntimeHintsConfig.TemplateResourcesRegistrar.class)
@Configuration
public class RuntimeHintsConfig {

  /**
   * Registrar for template resources, Java classes, and other runtime hints.
   */
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
      "io.github.cloudtechnology.generator.jooq.SimpleRepositoryGenerator",
      "org.jooq.meta.postgres.PostgresDatabase",
      "liquibase.resource.PathHandlerFactory",
      // JOOQ Core Classes
      "org.jooq.impl.DSL",
      "org.jooq.impl.DefaultDSLContext",
      "org.jooq.impl.DefaultBinding",
      "org.jooq.impl.DefaultDataType",
      "org.jooq.impl.SQLDataType",
      "org.jooq.impl.BuiltInDataType",
      "org.jooq.impl.ArrayDataType",
      "org.jooq.impl.AbstractDataType",
      "org.jooq.Configuration",
      "org.jooq.ConnectionProvider",
      "org.jooq.Settings",
      // JOOQ Code Generation Classes
      "org.jooq.codegen.GenerationTool",
      "org.jooq.codegen.JavaGenerator",
      "org.jooq.codegen.DefaultGeneratorStrategy",
      "org.jooq.codegen.JavaWriter",
      // JOOQ Meta Classes
      "org.jooq.meta.DefaultDatabase",
      "org.jooq.meta.AbstractDatabase",
      "org.jooq.meta.AbstractTableDefinition",
      "org.jooq.meta.AbstractColumnDefinition",
      "org.jooq.meta.DefaultColumnDefinition",
      "org.jooq.meta.DefaultDataTypeDefinition",
      // PostgreSQL Driver Classes
      "org.postgresql.Driver",
      "org.postgresql.ds.PGSimpleDataSource",
      "org.postgresql.jdbc.PgConnection",
      "org.postgresql.jdbc.PgStatement",
      "org.postgresql.jdbc.PgPreparedStatement",
      "org.postgresql.jdbc.PgResultSet",
      "org.postgresql.util.PGobject",
      // Additional JOOQ Implementation Classes
      "org.jooq.impl.DefaultConnection",
      "org.jooq.impl.DefaultConnectionProvider",
      "org.jooq.impl.DefaultConfiguration",
      "org.jooq.impl.DefaultExecuteContext",
      "org.jooq.impl.DefaultExecuteListener",
      "org.jooq.impl.LoggerListener",
      "org.jooq.impl.StopWatchListener"
    );

    /**
     * Registers runtime hints for resources, classes, and a specific case for
     * Caffeine cache.
     *
     * @param hints       the RuntimeHints instance to register the hints against.
     * @param classLoader the ClassLoader to use for class name resolution.
     */
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
      registerStaticResources(hints);
      registerClasses(hints);
      registerCaffeineSpecialCase(hints);
      registerJooqSpecificHints(hints);
    }

    /**
     * Registers JOOQ specific runtime hints for Native Image compilation.
     * This includes database type converters and binding classes.
     */
    private void registerJooqSpecificHints(RuntimeHints hints) {
      // Register JOOQ binding classes with all member categories
      List<String> jooqBindingClasses = Arrays.asList(
        "org.jooq.Binding",
        "org.jooq.Converter",
        "org.jooq.impl.AbstractBinding",
        "org.jooq.impl.DefaultBinding",
        "org.jooq.impl.IdentityConverter",
        "org.jooq.impl.QOM"
      );

      jooqBindingClasses.forEach(className -> {
        try {
          TypeReference typeRef = TypeReference.of(className);
          hints.reflection().registerType(typeRef, MemberCategory.values());
          log.debug("已註冊 JOOQ 反射提示: {}", className);
        } catch (Exception e) {
          log.warn("無法註冊 JOOQ 反射提示 {}: {}", className, e.getMessage());
        }
      });

      // Register PostgreSQL specific types
      List<String> postgresqlTypes = Arrays.asList(
        "org.postgresql.util.PGInterval",
        "org.postgresql.util.PGmoney",
        "org.postgresql.geometric.PGpoint",
        "org.postgresql.geometric.PGline",
        "org.postgresql.geometric.PGlseg",
        "org.postgresql.geometric.PGbox",
        "org.postgresql.geometric.PGpath",
        "org.postgresql.geometric.PGpolygon",
        "org.postgresql.geometric.PGcircle",
        "org.postgresql.jdbc.PgArray"
      );

      postgresqlTypes.forEach(className -> {
        try {
          TypeReference typeRef = TypeReference.of(className);
          hints.reflection().registerType(typeRef, MemberCategory.values());
          log.debug("已註冊 PostgreSQL 類型反射提示: {}", className);
        } catch (Exception e) {
          log.warn("無法註冊 PostgreSQL 類型反射提示 {}: {}", className, e.getMessage());
        }
      });

      // Register resource patterns for JOOQ
      hints.resources()
        .registerPattern("META-INF/services/org.jooq.*")
        .registerPattern("org/jooq/**")
        .registerPattern("org/postgresql/**");
    }

    /**
     * Registers patterns for static resources that should be available at runtime.
     *
     * @param hints the RuntimeHints instance to register the resources against.
     */
    private void registerStaticResources(RuntimeHints hints) {
      hints
        .resources()
        .registerPattern("templates/**")
        .registerPattern("static/**")
        .registerPattern("JavaSpring/**");
    }

    /**
     * Registers Java classes and custom class names for reflection and
     * serialization.
     *
     * @param hints the RuntimeHints instance to register the classes against.
     */
    private void registerClasses(RuntimeHints hints) {
      javaClasses.forEach(javaClass -> registerClass(hints, javaClass));
      classNames.forEach(className -> registerClassByName(hints, className));
    }

    /**
     * Helper method to register a Java class for reflection and attempts to
     * register it for serialization if applicable.
     * This method creates a TypeReference from the class object and delegates to
     * {@link #registerClass(RuntimeHints, TypeReference)}.
     *
     * @param hints the RuntimeHints instance to register the class against.
     * @param type  the class to be registered.
     */
    private void registerClass(RuntimeHints hints, Class<?> type) {
      registerClass(hints, TypeReference.of(type));
    }

    /**
     * Registers a class by its name for reflection and possible serialization.
     *
     * @param hints     the RuntimeHints instance to register the class against.
     * @param className the name of the class to be registered.
     */
    private void registerClassByName(RuntimeHints hints, String className) {
      registerClass(hints, TypeReference.of(className));
    }

    /**
     * Registers a class for reflection and attempts to register it for
     * serialization if applicable.
     *
     * @param hints         the RuntimeHints instance to register the class against.
     * @param typeReference the TypeReference of the class to be registered.
     */
    private void registerClass(
      RuntimeHints hints,
      TypeReference typeReference
    ) {
      hints.reflection().registerType(typeReference, MemberCategory.values());
      attemptSerializationRegistration(hints, typeReference);
    }

    /**
     * Attempts to register a class for serialization if it is Serializable.
     *
     * @param hints         the RuntimeHints instance to register the serialization
     *                      hint against.
     * @param typeReference the TypeReference of the class to check for
     *                      serialization capability.
     */
    private void attemptSerializationRegistration(
      RuntimeHints hints,
      TypeReference typeReference
    ) {
      try {
        Class<?> clzz = Class.forName(typeReference.getName());
        if (Serializable.class.isAssignableFrom(clzz)) {
          hints.serialization().registerType(typeReference);
        }
      } catch (Throwable t) {
        log.error(
          "couldn't register serialization hint for {}:{}",
          typeReference.getName(),
          t.getMessage()
        );
      }
    }

    /**
     * Handles a special registration case for the Caffeine cache.
     *
     * @param hints the RuntimeHints instance to register the Caffeine cache class
     *              against.
     */
    private void registerCaffeineSpecialCase(RuntimeHints hints) {
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
    }
  }
}
