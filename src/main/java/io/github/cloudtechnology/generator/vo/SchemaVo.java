package io.github.cloudtechnology.generator.vo;

import java.nio.file.Path;

public record SchemaVo(
  Path projectTempPath,
  String dbUrl,
  String dbUsername,
  String dbPassword
) {}
