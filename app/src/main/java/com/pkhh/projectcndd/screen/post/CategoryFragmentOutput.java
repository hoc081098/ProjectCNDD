package com.pkhh.projectcndd.screen.post;

import java.util.Objects;

import androidx.annotation.Nullable;

public class CategoryFragmentOutput implements StepFragment.DataOutput {
  @Nullable private String selectedCategoryId;

  public CategoryFragmentOutput() {this(null);}

  public CategoryFragmentOutput(@Nullable String selectedCategoryId) {
    this.selectedCategoryId = selectedCategoryId;
  }

  @Nullable
  public String getSelectedCategoryId() {
    return selectedCategoryId;
  }

  public void setSelectedCategoryId(@Nullable String selectedCategoryId) {
    this.selectedCategoryId = selectedCategoryId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CategoryFragmentOutput that = (CategoryFragmentOutput) o;
    return Objects.equals(selectedCategoryId, that.selectedCategoryId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(selectedCategoryId);
  }
}
