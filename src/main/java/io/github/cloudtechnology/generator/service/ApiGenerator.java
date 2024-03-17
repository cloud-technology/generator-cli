package io.github.cloudtechnology.generator.service;

import io.github.cloudtechnology.generator.vo.ApiVo;

public interface ApiGenerator {
    public void generate(ApiVo apiVo) throws Exception;
}
