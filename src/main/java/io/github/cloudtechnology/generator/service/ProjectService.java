package io.github.cloudtechnology.generator.service;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.github.cloudtechnology.generator.command.CreateProjectCommand;
import io.github.cloudtechnology.generator.service.impl.GradleProjectGenerator;
import io.github.cloudtechnology.generator.vo.ApiVo;
import io.github.cloudtechnology.generator.vo.ProjectVo;
import io.github.cloudtechnology.generator.vo.RepositoryVo;
import io.github.cloudtechnology.generator.vo.SchemaVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ApplicationContext applicationContext;

  public void create(CreateProjectCommand createProjectCommand)
    throws Exception {
    log.info("path={}", createProjectCommand.getProjectTempPath());
    ProjectVo projectVo = new ProjectVo(
      createProjectCommand.getProjectTempPath(),
      createProjectCommand.getBuildTool(),
      createProjectCommand.getGroupId(),
      createProjectCommand.getArtifactId(),
      createProjectCommand.getName(),
      createProjectCommand.getDescription(),
      createProjectCommand.getPackageName(),
      createProjectCommand.getJvmVersion(),
      createProjectCommand.getRuntime()
    );

    ProjectGenerator projectGenerator =
      switch (createProjectCommand.getBuildTool()) {
        case GRADLE:
          yield (ProjectGenerator) applicationContext.getBean(
            GradleProjectGenerator.class
          );
        default:
          throw new IllegalArgumentException("Invalid build tool");
      };
    projectGenerator.generate(projectVo);
    //
    if (!ObjectUtils.isEmpty(createProjectCommand.getOpenAPIFIle())) {
      ApiGenerator apiGenerator = applicationContext.getBean(
        "openAPIGenerator",
        ApiGenerator.class
      );
      ApiVo apiVo = new ApiVo(
        createProjectCommand.getProjectTempPath(),
        createProjectCommand.getOpenAPIFIle(),
        createProjectCommand.getPackageName()
      );
      apiGenerator.generate(apiVo);
    }

    if (
      StringUtils.hasText(createProjectCommand.getDbUrl()) &&
      StringUtils.hasText(createProjectCommand.getDbUsername()) &&
      StringUtils.hasText(createProjectCommand.getDbPassword())
    ) {
      // 1. 先生成 JOOQ 標準類別（POJOs, Tables, Records）
      RepositoryGenerator jooqGenerator = applicationContext.getBean(
        "jooqGenerator",
        RepositoryGenerator.class
      );
      RepositoryVo repositoryVo = new RepositoryVo(
        createProjectCommand.getProjectTempPath(),
        createProjectCommand.getPackageName(),
        createProjectCommand.getDbUrl(),
        createProjectCommand.getDbUsername(),
        createProjectCommand.getDbPassword()
      );
      jooqGenerator.generate(repositoryVo);
      
      // 2. JOOQ 完成後，獨立生成 Repository 介面
      log.info("🔄 JOOQ 生成完成，開始生成 Spring Data Repository 介面...");
      RepositoryGenerator springRepositoryGenerator = applicationContext.getBean(
        "springRepositoryGenerator", 
        RepositoryGenerator.class
      );
      springRepositoryGenerator.generate(repositoryVo);
      log.info("✅ Spring Data Repository 介面生成完成");
      
      // 3. 清理臨時的 repository-metadata.json 檔案
      try {
        Path metadataFilePath = repositoryVo.projectTempPath()
                                          .resolve("src/main/java")
                                          .resolve("repository-metadata.json");
        if (Files.exists(metadataFilePath)) {
          Files.delete(metadataFilePath);
          log.info("🧹 已清理臨時檔案: repository-metadata.json");
        }
      } catch (Exception e) {
        log.warn("⚠️ 清理臨時檔案時發生錯誤: {}", e.getMessage());
      }
      
      // 4. 生成 Liquibase schema versioning
      SchemaVersioning schemaVersioning = applicationContext.getBean(
        "liquibaseGenerator",
        SchemaVersioning.class
      );
      SchemaVo schemaVo = new SchemaVo(
        createProjectCommand.getProjectTempPath(),
        createProjectCommand.getDbUrl(),
        createProjectCommand.getDbUsername(),
        createProjectCommand.getDbPassword()
      );
      schemaVersioning.generate(schemaVo);
    }
  }
}
