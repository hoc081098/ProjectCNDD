package com.pkhh.projectcndd.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;

public class MotelRoom extends FirebaseModel implements Parcelable {
  private String title;
  private String description;
  private long price;
  private long countView;
  private double size;
  private String address;
  private GeoPoint addressGeoPoint;
  private List<String> images;
  private String phone;
  private boolean isActive;
  private boolean approve;
  private Map<String, Object> utilities;
  private DocumentReference user;
  private DocumentReference category;
  private DocumentReference province;
  private DocumentReference ward;
  private DocumentReference district;
  private String districtName;
  private Date createdAt;
  private Date updatedAt;
  private List<String> userIdsSaved;

  // Firebase Firestore require empty constructor
  public MotelRoom() {
  }

  protected MotelRoom(Parcel in) {
    title = in.readString();
    description = in.readString();
    price = in.readLong();
    countView = in.readLong();
    size = in.readDouble();
    address = in.readString();
    addressGeoPoint = new GeoPoint(in.readDouble(), in.readDouble());
    images = new ArrayList<>();
    in.readStringList(images);
    phone = in.readString();
    isActive = in.readByte() == (byte) 1;
    approve = in.readByte() == (byte) 1;
    utilities = (HashMap<String, Object>) in.readSerializable();
    user = FirebaseFirestore.getInstance().document(in.readString());
    category = FirebaseFirestore.getInstance().document(in.readString());
    province = FirebaseFirestore.getInstance().document(in.readString());
    ward = FirebaseFirestore.getInstance().document(in.readString());
    district = FirebaseFirestore.getInstance().document(in.readString());
    createdAt = new Date(in.readLong());
    final long aLong = in.readLong();
    updatedAt = aLong >= 0 ? new Date(aLong) : null;
    userIdsSaved = new ArrayList<>();
    in.readStringList(userIdsSaved);
    districtName = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(title);
    dest.writeString(description);
    dest.writeLong(price);
    dest.writeLong(countView);
    dest.writeDouble(size);
    dest.writeString(address);
    dest.writeDouble(addressGeoPoint.getLatitude());
    dest.writeDouble(addressGeoPoint.getLongitude());
    dest.writeStringList(images);
    dest.writeString(phone);
    dest.writeByte((byte) (isActive ? 1 : 0));
    dest.writeByte((byte) (approve ? 1 : 0));
    dest.writeSerializable(new HashMap<>(utilities));
    dest.writeString(user.getPath());
    dest.writeString(category.getPath());
    dest.writeString(province.getPath());
    dest.writeString(ward.getPath());
    dest.writeString(district.getPath());
    dest.writeLong(createdAt == null ? -1 : createdAt.getTime());
    dest.writeLong(updatedAt == null ? -1 : updatedAt.getTime());
    dest.writeStringList(userIdsSaved);
    dest.writeString(districtName);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<MotelRoom> CREATOR = new Creator<MotelRoom>() {
    @Override
    public MotelRoom createFromParcel(Parcel in) {
      return new MotelRoom(in);
    }

    @Override
    public MotelRoom[] newArray(int size) {
      return new MotelRoom[size];
    }
  };

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getPrice() {
    return price;
  }

  public void setPrice(long price) {
    this.price = price;
  }

  @PropertyName("count_view")
  public long getCountView() {
    return countView;
  }

  @PropertyName("count_view")
  public void setCountView(long countView) {
    this.countView = countView;
  }

  public double getSize() {
    return size;
  }

  public void setSize(double size) {
    this.size = size;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  @PropertyName("address_geopoint")
  public GeoPoint getAddressGeoPoint() {
    return addressGeoPoint;
  }

  @PropertyName("address_geopoint")
  public void setAddressGeoPoint(GeoPoint addressGeoPoint) {
    this.addressGeoPoint = addressGeoPoint;
  }

  public List<String> getImages() {
    return images;
  }

  public void setImages(List<String> images) {
    this.images = images;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @PropertyName("is_active")
  public boolean isActive() {
    return isActive;
  }

  @PropertyName("is_active")
  public void setActive(boolean active) {
    this.isActive = active;
  }

  public boolean isApprove() {
    return approve;
  }

  public void setApprove(boolean approve) {
    this.approve = approve;
  }

  public Map<String, Object> getUtilities() {
    return utilities;
  }

  public void setUtilities(Map<String, Object> utilities) {
    this.utilities = utilities;
  }

  public DocumentReference getUser() {
    return user;
  }

  public void setUser(DocumentReference user) {
    this.user = user;
  }

  public DocumentReference getCategory() {
    return category;
  }

  public void setCategory(DocumentReference category) {
    this.category = category;
  }

  public DocumentReference getProvince() {
    return province;
  }

  public void setProvince(DocumentReference province) {
    this.province = province;
  }

  public DocumentReference getWard() {
    return ward;
  }

  public void setWard(DocumentReference ward) {
    this.ward = ward;
  }

  public DocumentReference getDistrict() {
    return district;
  }

  public void setDistrict(DocumentReference district) {
    this.district = district;
  }

  @PropertyName("created_at")
  public Date getCreatedAt() {
    return createdAt;
  }

  @PropertyName("created_at")
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @PropertyName("updated_at")
  public Date getUpdatedAt() {
    return updatedAt;
  }

  @PropertyName("updated_at")
  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  @NonNull
  @PropertyName("user_ids_saved")
  public List<String> getUserIdsSaved() {
    return userIdsSaved == null ? new ArrayList<>() : userIdsSaved;
  }

  @PropertyName("user_ids_saved")
  public void setUserIdsSaved(List<String> userIdsSaved) {
    this.userIdsSaved = userIdsSaved;
  }

  @PropertyName("district_name")
  public String getDistrictName() {
    return districtName;
  }

  @PropertyName("district_name")
  public void setDistrictName(String districtName) {
    this.districtName = districtName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MotelRoom motelRoom = (MotelRoom) o;
    return Objects.equals(id, motelRoom.id) &&
        price == motelRoom.price &&
        countView == motelRoom.countView &&
        Double.compare(motelRoom.size, size) == 0 &&
        isActive == motelRoom.isActive &&
        approve == motelRoom.approve &&
        Objects.equals(title, motelRoom.title) &&
        Objects.equals(description, motelRoom.description) &&
        Objects.equals(address, motelRoom.address) &&
        Objects.equals(addressGeoPoint, motelRoom.addressGeoPoint) &&
        Objects.equals(images, motelRoom.images) &&
        Objects.equals(phone, motelRoom.phone) &&
        Objects.equals(utilities, motelRoom.utilities) &&
        Objects.equals(user, motelRoom.user) &&
        Objects.equals(category, motelRoom.category) &&
        Objects.equals(province, motelRoom.province) &&
        Objects.equals(ward, motelRoom.ward) &&
        Objects.equals(district, motelRoom.district) &&
        Objects.equals(createdAt, motelRoom.createdAt) &&
        Objects.equals(updatedAt, motelRoom.updatedAt) &&
        Objects.equals(userIdsSaved, motelRoom.userIdsSaved) &&
        Objects.equals(districtName, motelRoom.districtName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, price, countView, size, address, addressGeoPoint,
        images, phone, isActive, approve, utilities, user, category, province, ward, district, createdAt,
        updatedAt, userIdsSaved, districtName);
  }

  @Override
  public String toString() {
    return "MotelRoom{" +
        "title='" + title + '\'' +
        ", description='" + description + '\'' +
        ", price=" + price +
        ", countView=" + countView +
        ", size=" + size +
        ", address='" + address + '\'' +
        ", addressGeoPoint=" + addressGeoPoint +
        ", images=" + images +
        ", phone='" + phone + '\'' +
        ", isActive=" + isActive +
        ", approve=" + approve +
        ", utilities=" + utilities +
        ", user=" + user +
        ", category=" + category +
        ", province=" + province +
        ", ward=" + ward +
        ", district=" + district +
        ", districtName='" + districtName + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", userIdsSaved=" + userIdsSaved +
        '}';
  }

  public Map<String, Object> toMap() {
    HashMap<String, Object> map = new HashMap<>();
    map.put("title", title);
    map.put("description", description);
    map.put("price", price);
    map.put("count_view", countView);
    map.put("size", size);
    map.put("address", address);
    map.put("address_geopoint", addressGeoPoint);
    map.put("images", images);
    map.put("phone", phone);
    map.put("is_active", isActive);
    map.put("approve", approve);
    map.put("utilities", utilities);
    map.put("user", user);
    map.put("category", category);
    map.put("province", province);
    map.put("ward", ward);
    map.put("district", district);
    map.put("created_at", createdAt);
    map.put("updated_at", updatedAt);
    map.put("user_ids_saved", userIdsSaved);
    map.put("district_name", districtName);
    return map;
  }
}