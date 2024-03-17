package io.github.cloudtechnology.generator.transform;

import org.jline.utils.Log;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.cloudtechnology.generator.bo.BuildToolEnum;
import io.github.cloudtechnology.generator.bo.RuntimeEnum;
import io.github.cloudtechnology.generator.command.CreateProjectCommand;
import io.github.cloudtechnology.generator.command.CreateProjectCommand.CreateProjectCommandBuilder;

@Component
public class GeneratorMapper {

    public CreateProjectCommand toCreateProjectCommand(Path projectTempPath, BuildToolEnum buildTool, String groupId, String artifactId,
            String name, String description, String packageName, String jvmVersion,
            String openapiFilePath, String dbUrl, String dbUsername, String dbPassword,
            RuntimeEnum runtime) throws IOException {
        
        CreateProjectCommandBuilder builder = CreateProjectCommand.builder();
        builder.projectTempPath(projectTempPath).buildTool(buildTool).groupId(groupId).artifactId(artifactId).name(name).description(description)
                .packageName(packageName).jvmVersion(jvmVersion);

        if (!ObjectUtils.isEmpty(openapiFilePath)) {
            Log.info("openapiFilePath: " + openapiFilePath);

            Path openapiPath = Paths.get(openapiFilePath);

            // Path projectOpenapiFIle = projectTempPath.resolve(openapiPath.getFileName());
            // Files.copy(Files.newInputStream(openapiPath), projectOpenapiFIle);
            builder.openAPIFIle(Paths.get(openapiFilePath));
        }

        if (StringUtils.hasText(dbUrl) && StringUtils.hasText(dbUsername) && StringUtils.hasText(dbPassword)) {
            builder.dbUrl(dbUrl).dbUsername(dbUsername).dbPassword(dbPassword);
        }

        builder.runtime(runtime);
        return builder.build();
    }
}
