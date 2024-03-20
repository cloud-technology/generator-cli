package io.github.cloudtechnology.generator.vo;

import java.nio.file.Path;

public record ApiVo(
  Path projectTempPath,
  Path specSource,
  String packageName
) {}
