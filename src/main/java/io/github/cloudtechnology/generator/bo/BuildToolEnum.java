package io.github.cloudtechnology.generator.bo;

public enum BuildToolEnum {
  GRADLE("GRADLE"),
  MAVEN("MAVEN");

  private String value;

  private BuildToolEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static BuildToolEnum fromValue(String value) {
    for (BuildToolEnum b : BuildToolEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
