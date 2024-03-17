package io.github.cloudtechnology.generator.vo;

import java.nio.file.Path;

public record RepositoryVo(Path projectTempPath, String packageName, String dbUrl, String dbUsername,
        String dbPassword) {

}
