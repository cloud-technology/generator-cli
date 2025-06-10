package io.github.cloudtechnology.generator.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Strategy;
import org.jooq.meta.jaxb.Target;
import org.springframework.stereotype.Component;

import io.github.cloudtechnology.generator.service.RepositoryGenerator;
import io.github.cloudtechnology.generator.vo.RepositoryVo;
import lombok.extern.slf4j.Slf4j;

/**
 * https://www.jooq.org/doc/3.19/manual-single-page/#codegen-programmatic
 * https://www.jooq.org/doc/latest/manual/code-generation/codegen-programmatic/
 * https://www.jooq.org/doc/latest/manual/code-generation/codegen-configuration/
 * https://www.jooq.org/doc/latest/manual/code-generation/codegen-custom-code/
 */
@Slf4j
@Component
public class JooqGenerator implements RepositoryGenerator {

  @Override
  public void generate(RepositoryVo repositoryVo) throws Exception {
    log.debug("repositoryVo={}", repositoryVo);
    Generate generate = new Generate();
    generate.setRecords(Boolean.FALSE);
    generate.setPojos(Boolean.TRUE);
    generate.setPojosEqualsAndHashCode(Boolean.FALSE);
    generate.setPojosToString(Boolean.FALSE);
    generate.setJavaTimeTypes(Boolean.TRUE);
    generate.setJpaAnnotations(Boolean.TRUE);
    generate.setJpaVersion("3.0");
    generate.withValidationAnnotations(Boolean.TRUE);
    generate.setSpringAnnotations(Boolean.TRUE);
    generate.setGlobalObjectReferences(Boolean.FALSE);
    generate.setPojosAsJavaRecordClasses(Boolean.FALSE);
    // 正確的 API 來禁用 default catalog 和 schema 檔案生成
    generate.withDefaultCatalog(Boolean.FALSE);  // 禁用 DefaultCatalog.java
    generate.withDefaultSchema(Boolean.FALSE);   // 禁用 DefaultSchema.java
    
    // 禁用空的 catalog 和 schema 檔案生成，避免產生 PublicEntity.java
    generate.withEmptyCatalogs(Boolean.FALSE);   // 不生成空的 catalog 檔案
    generate.withEmptySchemas(Boolean.FALSE);    // 不生成空的 schema 檔案，這會禁用 PublicEntity.java

    String generatorName = "org.jooq.codegen.JavaGenerator";
    // String generatorName = "io.github.cloudtechnology.generator.jooq.JooqJavaGenerator";

    Configuration configuration = new org.jooq.meta.jaxb.Configuration()
      // Configure the database connection here
      .withJdbc(
        new Jdbc()
          .withDriver("org.postgresql.Driver")
          .withUrl(repositoryVo.dbUrl())
          .withUser(repositoryVo.dbUsername())
          .withPassword(repositoryVo.dbPassword())
      )
      .withGenerator(
        new Generator()
          .withName(
            generatorName
          )
          .withDatabase(
            new Database()
              .withName("org.jooq.meta.postgres.PostgresDatabase")
              .withIncludes(".*")
              .withExcludes(
                "flyway_schema_history | databasechangelog | databasechangeloglock"
              )
              .withInputSchema("public")
          )
          .withStrategy(
            new Strategy()
              .withName(
                "io.github.cloudtechnology.generator.jooq.CustomNamingStrategy"
              )
          )
          .withGenerate(generate)
          .withTarget(
            new Target()
              .withPackageName(
                repositoryVo.packageName() + ".infrastructure.repositories"
              )
              .withDirectory(repositoryVo.projectTempPath() + "/src/main/java")
          )
      );

    GenerationTool.generate(configuration);

    Path repositoriePath = Paths.get(
      repositoryVo.projectTempPath() +
      "/src/main/java/" +
      repositoryVo.packageName().replaceAll("\\.", File.separator) +
      File.separator +
      "infrastructure" +
      File.separator +
      "repositories"
    );
    log.debug("repositoriePath={}", repositoriePath);
    Paths.get(repositoriePath + "/DefaultCatalog.java").toFile().delete();
    Paths.get(repositoriePath + "/Public.java").toFile().delete();

    Path tablesPath = Paths.get(repositoriePath + "/tables");
    if (null != tablesPath && null != tablesPath.toFile().listFiles()) {
      Arrays.stream(tablesPath.toFile().listFiles())
        .filter(File::isFile)
        .forEach(File::delete);
    }

    Path pojosPath = Path.of(repositoriePath.toString(), "tables", "pojos");

    // this.convertJakartaToJavax(pojosPath);

    Path repositoryTempPath = Path.of(
      repositoryVo.projectTempPath().toString(),
      "RepositoryTemp"
    );

    File[] files = repositoryTempPath.toFile().listFiles();
    if (null != files) {
      for (File file : files) {
        if (file.isFile()) {
          log.debug(
            "move to {}",
            Path.of(repositoriePath.toString(), file.getName())
          );
          Files.move(
            file.toPath(),
            Path.of(repositoriePath.toString(), file.getName()),
            StandardCopyOption.REPLACE_EXISTING
          );
        }
      }
    }
  }

  private void convertJakartaToJavax(Path pojosPath) throws IOException {
    File[] files = pojosPath.toFile().listFiles();
    if (files != null) {
      for (File file : files) {
        String content = Files.readString(
          file.toPath(),
          StandardCharsets.UTF_8
        );
        String replaceContent = StringUtils.replace(
          content,
          "jakarta.persistence.Column",
          "javax.persistence.Column"
        );
        replaceContent = StringUtils.replace(
          replaceContent,
          "jakarta.persistence.Entity",
          "javax.persistence.Entity"
        );
        replaceContent = StringUtils.replace(
          replaceContent,
          "jakarta.persistence.Id",
          "javax.persistence.Id"
        );
        replaceContent = StringUtils.replace(
          replaceContent,
          "jakarta.persistence.Table",
          "javax.persistence.Table"
        );
        replaceContent = StringUtils.replace(
          replaceContent,
          "jakarta.persistence.UniqueConstraint",
          "javax.persistence.UniqueConstraint"
        );
        replaceContent = StringUtils.replace(
          replaceContent,
          "jakarta.validation.constraints.NotNull",
          "javax.validation.constraints.NotNull"
        );
        replaceContent = StringUtils.replace(
          replaceContent,
          "jakarta.validation.constraints.Size",
          "javax.validation.constraints.Size"
        );
        Files.writeString(
          file.toPath(),
          replaceContent,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE
        );
      }
    }
  }
}
