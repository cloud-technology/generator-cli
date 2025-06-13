package io.github.cloudtechnology.generator.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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

    // 🎯 使用我們的自定義生成器來產生簡潔的 Repository
    String generatorName = "io.github.cloudtechnology.generator.jooq.SimpleRepositoryGenerator";

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
  }
} 