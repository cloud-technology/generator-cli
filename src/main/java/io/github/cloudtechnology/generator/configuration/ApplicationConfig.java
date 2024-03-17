package io.github.cloudtechnology.generator.configuration;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@ImportRuntimeHints(ApplicationConfig.TemplateResourcesRegistrar.class)
public class ApplicationConfig {

    static class TemplateResourcesRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            System.out.println("編譯執行");
            hints.resources()
                    .registerPattern("db/**/")
                    .registerPattern("templates/project/*.mustache")
                    // .registerPattern("templates/project/application.yml.mustache")
                    // .registerPattern("templates/**/")
                    .registerPattern("static/**");
        }
    }

}
