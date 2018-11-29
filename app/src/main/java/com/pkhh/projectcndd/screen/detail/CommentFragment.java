package com.pkhh.projectcndd.screen.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.Comment;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.screen.loginregister.LoginRegisterActivity;
import com.pkhh.projectcndd.utils.Constants;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.pkhh.projectcndd.utils.Constants.COMMENTS_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_MOTEL_ROOM_ID;
import static java.util.Objects.requireNonNull;

class CommentVH extends RecyclerView.ViewHolder {
  private DateFormat dateFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

  @BindView(R.id.item_comment_image_avatar) ImageView imageAvatar;
  @BindView(R.id.item_comment_text_name_date) TextView textNameDate;
  @BindView(R.id.item_comment_text_content) TextView textContent;

  public CommentVH(@NonNull View itemView) {
    super(itemView);
    ButterKnife.bind(this, itemView);
  }

  @SuppressLint("SetTextI18n")
  public void bind(Comment comment) {
    Picasso.get()
        .load(comment.getUserAvatar())
        .fit()
        .centerCrop()
        .noFade()
        .into(imageAvatar);
    textNameDate.setText(comment.getUserName() + " \u2022 " + dateFormat.format(
        comment.getUpdatedAt() != null
            ? comment.getUpdatedAt()
            : comment.getCreatedAt()
    ));
    textContent.setText(comment.getContent());
  }
}

public class CommentFragment extends Fragment {


  public static final int MIN_LENGTH_OF_COMMENT = 5;

  public static CommentFragment newInstance(String id) {
    final CommentFragment fragment = new CommentFragment();
    final Bundle args = new Bundle();
    args.putString(EXTRA_MOTEL_ROOM_ID, id);
    fragment.setArguments(args);
    return fragment;
  }

  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final FirebaseAuth auth = FirebaseAuth.getInstance();

  private String roomId;
  private Unbinder unbinder;
  private AlertDialog requireLoginDialog;
  private FirestoreRecyclerAdapter<Comment, CommentVH> adapter;

  @BindView(R.id.recycler_comments) RecyclerView recyclerComments;
  @BindView(R.id.text_input_comment) TextInputLayout textInputComment;
  @BindView(R.id.image_send) ImageView imageSend;
  @BindView(R.id.empty_layout) ConstraintLayout emptyLayout;
  @BindView(R.id.root_layout) ConstraintLayout rootLayout;
  @BindView(R.id.progressbar) ProgressBar progressbar;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_comment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    roomId = requireNonNull(getArguments()).getString(EXTRA_MOTEL_ROOM_ID);

    setupRecyclerView();

    requireNonNull(textInputComment.getEditText()).addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() < MIN_LENGTH_OF_COMMENT) {
          textInputComment.setError(getString(R.string.invalid_comment, MIN_LENGTH_OF_COMMENT));
          imageSend.setImageResource(R.drawable.ic_send_grey_24dp);
        } else {
          textInputComment.setError(null);
          imageSend.setImageResource(R.drawable.ic_send_accent_24dp);
        }

        if (auth.getCurrentUser() == null) {
          requireLoginDialog = new AlertDialog.Builder(requireContext())
              .setTitle(R.string.require_login)
              .setIcon(R.drawable.ic_exit_to_app_black_24dp)
              .setMessage(R.string.you_must_login_to_perform_this_function)
              .setNegativeButton(getString(R.string.cancel), (dialog, __) -> dialog.dismiss())
              .setPositiveButton(getString(R.string.ok), (dialog, __) -> {
                dialog.dismiss();
                final Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
                startActivity(intent);
              })
              .show();
          return;
        }
      }

      @Override
      public void afterTextChanged(Editable s) { }
    });

    imageSend.setOnClickListener(__ -> {
      final String comment = requireNonNull(textInputComment.getEditText()).getText().toString();
      final FirebaseUser currentUser = auth.getCurrentUser();
      if (comment.length() < MIN_LENGTH_OF_COMMENT || currentUser == null) {
        return;
      }
      final DocumentReference userRef = firestore.document(Constants.USERS_NAME_COLLECION + "/" + currentUser.getUid());

      userRef.get()
          .continueWithTask(task -> {
            final DocumentSnapshot documentSnapshot = requireNonNull(task.getResult());

            final Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("created_at", FieldValue.serverTimestamp());
            commentMap.put("user_id", userRef.getId());
            commentMap.put("room_id", roomId);
            commentMap.put("user_name", requireNonNull(documentSnapshot.get("full_name")));
            commentMap.put("user_avatar", requireNonNull(documentSnapshot.get("avatar")));
            commentMap.put("content", comment);

            return firestore
                .collection(COMMENTS_NAME_COLLECION)
                .add(commentMap);
          })
          .addOnSuccessListener(documentReference -> {
            Toast.makeText(requireContext(), R.string.add_comment_successfully, Toast.LENGTH_SHORT).show();
            requireNonNull(textInputComment.getEditText()).setText(null);
          })
          .addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          });

    });
  }


  private void setupRecyclerView() {
    recyclerComments.setHasFixedSize(true);
    recyclerComments.setLayoutManager(new LinearLayoutManager(requireContext()));
    recyclerComments.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

    final Query query = firestore
        .collection(COMMENTS_NAME_COLLECION)
        .whereEqualTo("room_id", roomId)
        .orderBy("created_at", Query.Direction.DESCENDING);

    final FirestoreRecyclerOptions<Comment> commentFirestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Comment>()
        .setQuery(query, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, Comment.class))
        .build();

    adapter = new FirestoreRecyclerAdapter<Comment, CommentVH>(commentFirestoreRecyclerOptions) {
      @NonNull
      @Override
      public CommentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentVH(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item_layout, parent, false)
        );
      }

      @Override
      protected void onBindViewHolder(@NonNull CommentVH commentVH, int i, @NonNull Comment comment) {
        commentVH.bind(comment);
      }

      @Override
      public void onDataChanged() {
        super.onDataChanged();

        TransitionManager.beginDelayedTransition(rootLayout);

        progressbar.setVisibility(View.INVISIBLE);
        if (getItemCount() == 0) {
          emptyLayout.setVisibility(View.VISIBLE);
          recyclerComments.setVisibility(View.INVISIBLE);
        } else {
          recyclerComments.setVisibility(View.VISIBLE);
          emptyLayout.setVisibility(View.INVISIBLE);
        }
      }
    };
    recyclerComments.setAdapter(adapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    adapter.startListening();
  }

  @Override
  public void onPause() {
    super.onPause();
    adapter.stopListening();
    if (requireLoginDialog != null && requireLoginDialog.isShowing()) {
      requireLoginDialog.dismiss();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
