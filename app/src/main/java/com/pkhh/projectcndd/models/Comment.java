package com.pkhh.projectcndd.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Objects;

import androidx.annotation.Nullable;

public class Comment extends FirebaseModel {
  private String userId;
  private String roomId;
  private String userName;
  private String userAvatar;
  private String content;
  @Nullable private Timestamp createdAt;
  @Nullable private Timestamp updatedAt;

  public Comment() { }

  public Comment(String userId, String roomId, String userName, String userAvatar, String content, @Nullable Timestamp createdAt, @Nullable Timestamp updatedAt) {
    this.userId = userId;
    this.roomId = roomId;
    this.userName = userName;
    this.userAvatar = userAvatar;
    this.content = content;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Comment comment = (Comment) o;
    return Objects.equals(userId, comment.userId) &&
        Objects.equals(roomId, comment.roomId) &&
        Objects.equals(userName, comment.userName) &&
        Objects.equals(userAvatar, comment.userAvatar) &&
        Objects.equals(content, comment.content) &&
        Objects.equals(createdAt, comment.createdAt) &&
        Objects.equals(updatedAt, comment.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, roomId, userName, userAvatar, content, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return "Comment{" +
        "userId='" + userId + '\'' +
        ", roomId='" + roomId + '\'' +
        ", userName='" + userName + '\'' +
        ", userAvatar='" + userAvatar + '\'' +
        ", content='" + content + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", id='" + id + '\'' +
        '}';
  }

  @PropertyName("user_id")
  public String getUserId() {
    return userId;
  }

  @PropertyName("user_id")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  @PropertyName("user_name")
  public String getUserName() {
    return userName;
  }

  @PropertyName("user_name")
  public void setUserName(String userName) {
    this.userName = userName;
  }

  @PropertyName("user_avatar")
  public String getUserAvatar() {
    return userAvatar;
  }

  @PropertyName("user_avatar")
  public void setUserAvatar(String userAvatar) {
    this.userAvatar = userAvatar;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @ServerTimestamp
  @PropertyName("created_at")
  @Nullable
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  @PropertyName("created_at")
  public void setCreatedAt(  @Nullable Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  @PropertyName("updated_at")
  @Nullable
  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  @PropertyName("updated_at")
  public void setUpdatedAt(@Nullable Timestamp updatedAt) {
    this.updatedAt = updatedAt;
  }

  @PropertyName("room_id")
  public String getRoomId() {
    return roomId;
  }

  @PropertyName("room_id")
  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }
}
