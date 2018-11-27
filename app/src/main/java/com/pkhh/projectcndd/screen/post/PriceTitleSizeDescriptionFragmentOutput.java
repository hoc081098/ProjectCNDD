package com.pkhh.projectcndd.screen.post;

import com.pkhh.projectcndd.screen.post.StepFragment.DataOutput;

import java.util.Objects;

public class PriceTitleSizeDescriptionFragmentOutput implements DataOutput {
  private long price;
  private String title;
  private double size;
  private String description;
  private String phone;


  public PriceTitleSizeDescriptionFragmentOutput() {this(0, "", 0, "", "");}

  public PriceTitleSizeDescriptionFragmentOutput(long price, String title, double size, String description, String phone) {
    this.price = price;
    this.title = title;
    this.size = size;
    this.description = description;
    this.phone = phone;
  }

  public long getPrice() {
    return price;
  }

  public void setPrice(long price) {
    this.price = price;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public double getSize() {
    return size;
  }

  public void setSize(double size) {
    this.size = size;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PriceTitleSizeDescriptionFragmentOutput that = (PriceTitleSizeDescriptionFragmentOutput) o;
    return price == that.price &&
        Double.compare(that.size, size) == 0 &&
        Objects.equals(title, that.title) &&
        Objects.equals(description, that.description) &&
        Objects.equals(phone, that.phone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(price, title, size, description, phone);
  }
}
