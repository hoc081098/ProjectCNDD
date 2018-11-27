package com.pkhh.projectcndd.screen.post;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

import androidx.annotation.Nullable;

public class AddressLocationFragmentOutput implements StepFragment.DataOutput {
  @Nullable private String provinceId;
  @Nullable private String districtId;
  @Nullable private String districtName;
  @Nullable private String wardId;
  @Nullable private LatLng latLng;
  @Nullable private String address;

  public AddressLocationFragmentOutput() {this(null, null, null, null, null, null);}

  public AddressLocationFragmentOutput(@Nullable String provinceId, @Nullable String districtId, @Nullable String districtName, @Nullable String wardId, @Nullable LatLng latLng, @Nullable String address) {
    this.provinceId = provinceId;
    this.districtId = districtId;
    this.districtName = districtName;
    this.wardId = wardId;
    this.latLng = latLng;
    this.address = address;
  }

  @Nullable
  public String component1() {return provinceId;}

  @Nullable
  public String component2() {return districtId;}

  @Nullable
  public String component3() {return districtName;}

  @Nullable
  public String component4() {return wardId;}

  @Nullable
  public LatLng component5() {return latLng;}

  @Nullable
  public String component6() {return address;}

  @Nullable
  public String getProvinceId() {
    return provinceId;
  }

  public void setProvinceId(@Nullable String provinceId) {
    this.provinceId = provinceId;
  }

  @Nullable
  public String getDistrictId() {
    return districtId;
  }

  public void setDistrictId(@Nullable String districtId) {
    this.districtId = districtId;
  }

  @Nullable
  public String getDistrictName() {
    return districtName;
  }

  public void setDistrictName(@Nullable String districtName) {
    this.districtName = districtName;
  }

  @Nullable
  public String getWardId() {
    return wardId;
  }

  public void setWardId(@Nullable String wardId) {
    this.wardId = wardId;
  }

  @Nullable
  public LatLng getLatLng() {
    return latLng;
  }

  public void setLatLng(@Nullable LatLng latLng) {
    this.latLng = latLng;
  }

  @Nullable
  public String getAddress() {
    return address;
  }

  public void setAddress(@Nullable String address) {
    this.address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddressLocationFragmentOutput that = (AddressLocationFragmentOutput) o;
    return Objects.equals(provinceId, that.provinceId) &&
        Objects.equals(districtId, that.districtId) &&
        Objects.equals(districtName, that.districtName) &&
        Objects.equals(wardId, that.wardId) &&
        Objects.equals(latLng, that.latLng) &&
        Objects.equals(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provinceId, districtId, districtName, wardId, latLng, address);
  }
}
