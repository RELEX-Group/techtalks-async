package ru.relex.techtalks.controller;

import java.util.List;

/**
 * @author Nikita Skornyakov
 */
public class Hospital {

  private String title;
  private String location;
  private List<String> categories;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }
}
