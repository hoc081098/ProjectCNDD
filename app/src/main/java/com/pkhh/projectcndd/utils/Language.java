package com.pkhh.projectcndd.utils;

public class Language {
  public final int id;
  public final String code;
  public final String name;

  public Language(int id, String code, String name) {
    this.id = id;
    this.code = code;
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
