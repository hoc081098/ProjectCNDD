package com.pkhh.projectcndd.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import static java.util.Objects.requireNonNull;

public class PostRoomJobIntentService extends JobIntentService {
  public static final int JOB_ID = 10;
  private final Handler mHandler = new Handler();

  private void toast(final CharSequence text) {
    mHandler.post(() -> Toast.makeText(PostRoomJobIntentService.this, text, Toast.LENGTH_SHORT).show());
  }

  public static void enqueue(Context context, Intent intent) {
    enqueueWork(context, PostRoomJobIntentService.class, JOB_ID, intent);
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    final String uid = requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    final StorageReference storageImage = FirebaseStorage.getInstance().getReference();

    final MotelRoom room = intent.getParcelableExtra("room");
    final Map<String, Object> map = room.toMap();
    map.put("created_at", FieldValue.serverTimestamp());
    final List<Uri> imageUris = intent.getParcelableArrayListExtra("uris");

    try {
      showNotification(getString(R.string.posting_room), getString(R.string.please_wait), 0);

      //*******************************************************************************************
      final DocumentReference documentReference = Tasks.await(
          FirebaseFirestore
              .getInstance()
              .collection(Constants.ROOMS_NAME_COLLECION).add(map)
      );
      showNotification(getString(R.string.post_room_successfully), "1/3", 20);
      Log.d("@@@", "documentReference=" + documentReference);
      //*******************************************************************************************


      //*******************************************************************************************
      List<Task<Uri>> uploadImagesTasks = Stream.of(imageUris)
          .map(uri -> {
            final StorageReference storageReference = storageImage
                .child("room_images/" + uid + "/" + documentReference.getId() + "/image_" + System.currentTimeMillis());

            return storageReference
                .putFile(uri)
                .continueWithTask(t -> {
                  if (!t.isSuccessful()) throw requireNonNull(t.getException());
                  return storageReference.getDownloadUrl();
                });
          })
          .toList();
      final List<Object> uris = Tasks.await(
          Tasks.whenAllSuccess(uploadImagesTasks)
      );
      showNotification(getString(R.string.upload_image_done), "2/3", 80);
      Log.d("@@@", "uris=" + uris);
      //*******************************************************************************************


      //*******************************************************************************************
      final Object[] downloadUrls = Stream.ofNullable(uris)
          .map(Object::toString)
          .toArray();
      Tasks.await(
          documentReference.update("images", FieldValue.arrayUnion(downloadUrls))
      );
      showNotification(getString(R.string.post_room_successfully),
          "3/3, kiểm tra trong phần quản lý bài đã đăng", 100);
      Log.d("@@@", "Post done");
      //*******************************************************************************************

      // toast
      toast(getString(R.string.post_room_successfully));
    } catch (ExecutionException | InterruptedException e) {
      Log.d("@@@", e.getMessage(), e);
      showNotification(getString(R.string.post_room_failed), e.getMessage(), 100);

      // toast
      toast(getString(R.string.post_room_failed));
    }
  }

  private void showNotification(CharSequence title, CharSequence content, int progress) {
    final Notification notification = new NotificationCompat.Builder(getApplicationContext(), "???")
        .setAutoCancel(true)
        .setWhen(System.currentTimeMillis())
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setProgress(100, progress, false)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .build();
    ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))
        .notify(0, notification);
  }
}
