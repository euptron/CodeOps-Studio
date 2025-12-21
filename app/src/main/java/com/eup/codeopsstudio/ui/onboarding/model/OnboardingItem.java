package com.eup.codeopsstudio.ui.onboarding.model;

public class OnboardingItem {

  private final int image;
  private final String header;
  private final String title;
  private final String description;

  public OnboardingItem(int image, String header, String title, String description) {
    this.image = image;
    this.header = header;
    this.title = title;
    this.description = description;
  }

  public int getImage() {
    return image;
  }

  public String getHeader() {
    return header;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }
}
