package io.github.cloudtechnology.generator.vo;

import io.github.cloudtechnology.generator.bo.BuildToolEnum;
import io.github.cloudtechnology.generator.bo.RuntimeEnum;
import java.nio.file.Path;

public record ProjectVo(
  Path projectTempPath,
  BuildToolEnum buildTool,
  String groupId,
  String artifactId,
  String name,
  String description,
  String packageName,
  String jvmVersion,
  RuntimeEnum runtime
) {}
