package com.pkhh.projectcndd.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
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
import timber.log.Timber;

import static com.pkhh.projectcndd.App.CHANNEL_ID;
import static java.util.Objects.requireNonNull;

public class PostRoomJobIntentService extends JobIntentService {
  private static final int JOB_ID = 10;
  private static final String TAG = "##PostService";
  private final Handler mHandler = new Handler();

  public static void enqueue(Context context, Intent intent) {
    enqueueWork(context, PostRoomJobIntentService.class, JOB_ID, intent);
  }

  private void toast(final CharSequence text) {
    mHandler.post(() -> Toast.makeText(PostRoomJobIntentService.this, text, Toast.LENGTH_SHORT).show());
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
      Timber.tag(TAG).d("documentReference = %s", documentReference);
      //*******************************************************************************************


      //*******************************************************************************************
      List<Task<String>> uploadImagesTasks = Stream.of(imageUris)
          .map(uri -> {
            final StorageReference storageReference = storageImage
                .child("room_images/" + uid + "/" + documentReference.getId() + "/image_" + System.currentTimeMillis());

            return storageReference
                .putFile(uri)
                .continueWithTask(t -> {
                  if (!t.isSuccessful()) throw requireNonNull(t.getException());
                  return storageReference.getDownloadUrl();
                })
                .continueWith(uriTask -> requireNonNull(uriTask.getResult()).toString());
          })
          .toList();
      final List<String> downloadUrls = Tasks.await(
          Tasks.whenAllSuccess(uploadImagesTasks)
      );
      showNotification(getString(R.string.upload_image_done), "2/3", 80);
      Timber.tag(TAG).d("uris=%s", downloadUrls);
      //*******************************************************************************************


      //*******************************************************************************************
      final Object[] downloadUrlsArray = downloadUrls != null ? downloadUrls.toArray() : null;
      if (downloadUrlsArray == null || downloadUrlsArray.length == 0) {
        showNotification(getString(R.string.post_room_successfully),
            getString(R.string.notification_cannot_upload_image), 100);
        Timber.tag(TAG).d("Post done but upload image not successfully");
        return;
      }
      Tasks.await(
          documentReference.update("images", FieldValue.arrayUnion(downloadUrlsArray))
      );
      showNotification(getString(R.string.post_room_successfully),
          getString(R.string.check_in_post_room_manager), 100);
      Timber.tag(TAG).d("Post done");
      //*******************************************************************************************


      // toast
      toast(getString(R.string.post_room_successfully));

    } catch (ExecutionException | InterruptedException e) {

      Timber.tag(TAG).d(e, e.getMessage());
      showNotification(getString(R.string.post_room_failed), e.getMessage(), 100);


      // toast
      toast(getString(R.string.post_room_failed));
    }
  }

  private void showNotification(CharSequence title, CharSequence content, int progress) {
    final Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
        .setAutoCancel(true)
        .setWhen(System.currentTimeMillis())
        .setContentTitle(title)
        .setContentText(content)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setProgress(100, progress, false)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .build();
    ((NotificationManager) requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).notify(0, notification);
  }
}
