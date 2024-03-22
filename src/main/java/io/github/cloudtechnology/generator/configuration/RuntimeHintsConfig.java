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
      "org.jooq.meta.postgres.PostgresDatabase",
      "liquibase.resource.PathHandlerFactory"
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
