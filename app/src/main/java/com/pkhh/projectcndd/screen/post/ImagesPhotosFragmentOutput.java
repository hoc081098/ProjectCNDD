package com.pkhh.projectcndd.screen.post;

import android.net.Uri;

import com.pkhh.projectcndd.screen.post.StepFragment.DataOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImagesPhotosFragmentOutput implements DataOutput {
  private List<Uri> uris;

  public ImagesPhotosFragmentOutput() {this(new ArrayList<>());}

  public ImagesPhotosFragmentOutput(List<Uri> uris) {
    this.uris = uris;
  }

  public List<Uri> getUris() {
    return uris;
  }

  public void setUris(List<Uri> uris) {
    this.uris = uris;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ImagesPhotosFragmentOutput that = (ImagesPhotosFragmentOutput) o;
    return Objects.equals(uris, that.uris);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uris);
  }
}
