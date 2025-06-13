package io.github.cloudtechnology.generator.jooq;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;

/**
 * 自訂的命名策略類，繼承自jOOQ的默認生成策略。
 * 這個策略用於自定義數據庫表映射到Java類的命名規則。
 */
public class CustomNamingStrategy extends DefaultGeneratorStrategy {

  /**
   * 根據提供的定義和模式生成Java類名。
   *
   * @param definition 定義對象，包含數據庫表或其他元素的詳細信息。
   * @param mode       生成模式，指示生成的是類名、接口名還是其他。
   * @return 返回自定義規則下的Java類名。
   */
  @Override
  public String getJavaClassName(Definition definition, Mode mode) {
    String name = definition.getName(); // 從定義中獲取原始名稱

    // 根據不同的模式應用不同的命名規則
    if (mode == Mode.POJO) {
      // 對於 POJO 模式，使用標準的 PascalCase 命名，不添加 Entity 後綴
      if (name.startsWith("tb_")) {
        name = name.substring(3); // 移除"tb_"前綴
      }
      return toPascalCase(name);
    } else {
      // 對於其他模式（如 DEFAULT, RECORD 等），添加 Entity 後綴
      if (name.startsWith("tb_")) {
        name = name.substring(3); // 移除"tb_"前綴
        name = toPascalCase(name) + "Entity"; // 轉換為PascalCase並添加"Entity"後綴
      } else {
        name = toPascalCase(name) + "Entity"; // 轉換為PascalCase並添加"Entity"後綴
      }
      return name;
    }
  }

  /**
   * 將下劃線分隔的字符串轉換為PascalCase（駝峰命名法）。
   *
   * @param input 需要轉換的字符串。
   * @return 轉換後的PascalCase字符串。
   */
  private String toPascalCase(String input) {
    StringBuilder result = new StringBuilder();
    // 按下劃線分隔字符串，並對每個部分進行處理
    for (String part : input.split("_")) {
      if (!part.isEmpty()) {
        // 將每個部分的首字母轉大寫，其餘轉小寫，然後拼接
        result
          .append(part.substring(0, 1).toUpperCase())
          .append(part.substring(1).toLowerCase());
      }
    }
    return result.toString();
  }
}
