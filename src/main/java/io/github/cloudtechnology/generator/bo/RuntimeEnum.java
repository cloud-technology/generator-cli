package io.github.cloudtechnology.generator.bo;

public enum RuntimeEnum {
  KUBERNETES("KUBERNETES"),
  CLOUDRUN("CLOUDRUN");

  private String value;

  private RuntimeEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static RuntimeEnum fromValue(String value) {
    for (RuntimeEnum b : RuntimeEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
