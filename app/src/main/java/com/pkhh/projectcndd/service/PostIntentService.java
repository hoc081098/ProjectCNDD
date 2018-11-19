package com.pkhh.projectcndd.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
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

public class PostIntentService extends JobIntentService {

  public static final int JOB_ID = 10;

  public static void enqueue(Context context, Intent intent) {
    enqueueWork(context, PostIntentService.class, JOB_ID, intent);
  }

  @Override
  protected void onHandleWork(@NonNull Intent intent) {
    final Notification notification = new NotificationCompat.Builder(getApplicationContext(), "???")
        .setAutoCancel(true)
        .setWhen(System.currentTimeMillis())
        .setContentTitle("Posting...")
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentText("Please wait...")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .build();
    ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))
        .notify(0, notification);


    final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    final String uid = requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    final StorageReference storageImage = storage.getReference();

    MotelRoom room = intent.getParcelableExtra("room");
    final Map<String, Object> map = room.toMap();
    map.put("created_at", FieldValue.serverTimestamp());
    List<Uri> imageUris = intent.getParcelableArrayListExtra("uris");

    try {
      final Task<Void> total = firestore.collection(Constants.ROOMS_NAME_COLLECION)
          .add(map)
          .continueWithTask(addTask -> {
            if (!addTask.isSuccessful()) throw requireNonNull(addTask.getException());
            final DocumentReference documentReference = addTask.getResult();

            List<Task<Uri>> uploadImagesTasks = Stream.of(imageUris)
                .map(uri -> {
                  StorageReference storageReference = storageImage
                      .child("room_images/" + uid + "/" + documentReference.getId() + "/image_" + System.currentTimeMillis());

                  return storageReference
                      .putFile(uri)
                      .continueWithTask(t -> {
                        if (!t.isSuccessful()) throw requireNonNull(t.getException());
                        return storageReference.getDownloadUrl();
                      });
                })
                .toList();
            return Tasks
                .whenAllSuccess(uploadImagesTasks)
                .continueWithTask(uploadTasks -> {
                  Object[] uris = uploadTasks.getResult().toArray();
                  return documentReference.update("images", FieldValue.arrayUnion(uris));
                });
          });

      Tasks.await(total);
      Toast.makeText(getApplicationContext(), "Đăng bài thành công", Toast.LENGTH_SHORT).show();

      final Notification notification1 = new NotificationCompat.Builder(getApplicationContext(), "???")
          .setAutoCancel(true)
          .setWhen(System.currentTimeMillis())
          .setContentTitle("Post done")
          .setContentText("...")
          .setSmallIcon(R.mipmap.ic_launcher_round)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
          .build();
      ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE))
          .notify(0, notification1);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
      ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
    }
  }
}
