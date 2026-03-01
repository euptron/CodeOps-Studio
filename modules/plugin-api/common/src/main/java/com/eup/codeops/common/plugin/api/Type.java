package com.eup.codeops.common.plugin.api;

public enum Type {
  JAR("*.jar"),
  DEX("*.dex"),
  JSON("*.json"),
  APK(".apk"),
  JS(".js");

  private final String ext;

  Type(String ext) {
    this.ext = ext;
  }

  public String getExtension() {
    return this.ext;
  }
}
