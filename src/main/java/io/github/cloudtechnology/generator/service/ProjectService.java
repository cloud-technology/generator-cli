package io.github.cloudtechnology.generator.service;

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

        public void create(CreateProjectCommand createProjectCommand) throws Exception {
                log.info("path={}", createProjectCommand.getProjectTempPath());
                ProjectVo projectVo = new ProjectVo(createProjectCommand.getProjectTempPath(),
                                createProjectCommand.getBuildTool(),
                                createProjectCommand.getGroupId(), createProjectCommand.getArtifactId(),
                                createProjectCommand.getName(),
                                createProjectCommand.getDescription(), createProjectCommand.getPackageName(),
                                createProjectCommand.getJvmVersion(), createProjectCommand.getRuntime());

                ProjectGenerator projectGenerator = switch (createProjectCommand.getBuildTool()) {
                        case GRADLE:
                                yield (ProjectGenerator) applicationContext.getBean(GradleProjectGenerator.class);
                        default:
                                throw new IllegalArgumentException("Invalid build tool");
                };
                projectGenerator.generate(projectVo);
                //
                if (!ObjectUtils.isEmpty(createProjectCommand.getOpenAPIFIle())) {
                        ApiGenerator apiGenerator = applicationContext.getBean("openAPIGenerator",
                                        ApiGenerator.class);
                        ApiVo apiVo = new ApiVo(createProjectCommand.getProjectTempPath(),
                                        createProjectCommand.getOpenAPIFIle(),
                                        createProjectCommand.getPackageName());
                        apiGenerator.generate(apiVo);
                }

                if (StringUtils.hasText(createProjectCommand.getDbUrl())
                                && StringUtils.hasText(createProjectCommand.getDbUsername())
                                && StringUtils.hasText(createProjectCommand.getDbPassword())) {
                        RepositoryGenerator repositoryGenerator = applicationContext.getBean(
                                        "jooqGenerator",
                                        RepositoryGenerator.class);
                        RepositoryVo repositoryVo = new RepositoryVo(createProjectCommand.getProjectTempPath(),
                                        createProjectCommand.getPackageName(),
                                        createProjectCommand.getDbUrl(), createProjectCommand.getDbUsername(),
                                        createProjectCommand.getDbPassword());
                        repositoryGenerator.generate(repositoryVo);
                        //
                        SchemaVersioning schemaVersioning = applicationContext.getBean(
                                        "liquibaseGenerator",
                                        SchemaVersioning.class);
                        SchemaVo schemaVo = new SchemaVo(createProjectCommand.getProjectTempPath(),
                                        createProjectCommand.getDbUrl(), createProjectCommand.getDbUsername(),
                                        createProjectCommand.getDbPassword());
                        schemaVersioning.generate(schemaVo);
                }
        }

}
