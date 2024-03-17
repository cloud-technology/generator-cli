package io.github.cloudtechnology.generator.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.springframework.util.StringUtils;
import org.springframework.shell.command.annotation.Command;

import io.github.cloudtechnology.generator.bo.BuildToolEnum;
import io.github.cloudtechnology.generator.bo.RuntimeEnum;
import io.github.cloudtechnology.generator.command.CreateProjectCommand;
import io.github.cloudtechnology.generator.configuration.properties.ApplicationProperties;
import io.github.cloudtechnology.generator.service.ProjectService;
import io.github.cloudtechnology.generator.service.RepositoryGenerator;
import io.github.cloudtechnology.generator.transform.GeneratorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command
@RequiredArgsConstructor
public class ProjectGenerator {
    private final GeneratorMapper generatorMapper;
    private final ApplicationProperties applicationProperties;
    private final ProjectService projectService;

    @Command(command = { "generator" })
    public void generator() {
        Path projectTempPath = null;
        BuildToolEnum buildTool = applicationProperties.getBuildTool();
        String groupId = applicationProperties.getGroupId();
        String artifactId = applicationProperties.getArtifactId();
        String name = applicationProperties.getName();
        String description = applicationProperties.getDescription();
        String packageName = applicationProperties.getPackageName();
        String jvmVersion = applicationProperties.getJvmVersion();
        String openapiFilePath = applicationProperties.getOpenapiFilePath();
        String dbUrl = applicationProperties.getDbUrl();
        String dbUsername = applicationProperties.getDbUsername();
        String dbPassword = applicationProperties.getDbPassword();
        RuntimeEnum runtime = applicationProperties.getRuntime();
        CreateProjectCommand createProjectCommand = null;
        try {

            // projectTempPath = Files.createTempDirectory("project_");
            Path userDirectoryPath = Paths.get("");
            projectTempPath = userDirectoryPath.resolve(Path.of(name + "-" + new Random().nextInt(1000)));

            createProjectCommand = generatorMapper.toCreateProjectCommand(projectTempPath, buildTool, groupId, artifactId,
                    name, description, packageName, jvmVersion, openapiFilePath, dbUrl, dbUsername, dbPassword,
                    runtime);
            projectService.create(createProjectCommand);
            //

        } catch (Exception e) {
            log.error("", e);
        }

    }

}
