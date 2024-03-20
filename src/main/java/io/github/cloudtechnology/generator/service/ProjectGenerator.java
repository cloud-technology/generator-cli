package io.github.cloudtechnology.generator.service;

import io.github.cloudtechnology.generator.vo.ProjectVo;

public interface ProjectGenerator {
  public void generate(ProjectVo projectVo) throws Exception;
}
