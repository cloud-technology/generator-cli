package io.github.cloudtechnology.generator.service;

import io.github.cloudtechnology.generator.vo.RepositoryVo;

public interface RepositoryGenerator {
  public void generate(RepositoryVo repositoryVo) throws Exception;
}
