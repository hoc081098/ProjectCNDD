package com.pkhh.projectcndd.screen.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
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
import com.pkhh.projectcndd.screen.profile.UserProfileActivity;
import com.pkhh.projectcndd.utils.Constants;
import com.pkhh.projectcndd.utils.RecyclerOnLongClickListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.pkhh.projectcndd.utils.Constants.COMMENTS_NAME_COLLECION;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_USER_FULL_NAME;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_USER_ID;
import static java.util.Objects.requireNonNull;

class CommentVH extends RecyclerView.ViewHolder {
  private DateFormat dateFormat = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());

  @BindView(R.id.item_comment_image_avatar) ImageView imageAvatar;
  @BindView(R.id.item_comment_text_name_date) TextView textNameDate;
  @BindView(R.id.item_comment_text_content) TextView textContent;


  public CommentVH(@NonNull View itemView, final @NonNull RecyclerOnLongClickListener longClickListener, final @NonNull RecyclerOnLongClickListener clickListener) {
    super(itemView);
    ButterKnife.bind(this, itemView);
    imageAvatar.setOnClickListener(v -> {
      final int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        clickListener.onLongClick(v, position);
      }
    });
    itemView.setOnLongClickListener(v -> {
      final int position = getAdapterPosition();
      if (position != RecyclerView.NO_POSITION) {
        longClickListener.onLongClick(v, position);
      }
      return true;
    });
  }

  @SuppressLint("SetTextI18n")
  public void bind(Comment comment) {
    Timber.tag("%$%").d("bind %s", comment);

    Picasso.get()
        .load(comment.getUserAvatar())
        .networkPolicy(NetworkPolicy.NO_CACHE)
        .memoryPolicy(MemoryPolicy.NO_CACHE)
        .fit()
        .centerCrop()
        .noFade()
        .placeholder(R.drawable.avatar_default_icon)
        .error(R.drawable.avatar_default_icon)
        .into(imageAvatar);

    final Timestamp updatedAt = comment.getUpdatedAt();
    final Timestamp createdAt = comment.getCreatedAt();
    if (updatedAt == null && createdAt == null) {
      textNameDate.setText(comment.getUserName());
    } else {
      textNameDate.setText(
          comment.getUserName() + " \u2022 " + dateFormat.format(
              updatedAt != null
                  ? updatedAt.toDate()
                  : createdAt.toDate()
          )
      );
    }
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
  private AlertDialog deleteDialog;
  private AlertDialog editDialog;
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
      final String commentStr = requireNonNull(textInputComment.getEditText()).getText().toString();
      final FirebaseUser currentUser = auth.getCurrentUser();

      if (commentStr.length() < MIN_LENGTH_OF_COMMENT) {
        return;
      } else if (currentUser == null) {
        requireLoginDialog = new AlertDialog.Builder(requireContext())
            .setTitle(R.string.require_login)
            .setIcon(R.drawable.ic_exit_to_app_black_24dp)
            .setMessage(R.string.you_must_login_to_perform_this_function)
            .setNegativeButton(getString(R.string.cancel), (dialog, ___) -> dialog.dismiss())
            .setPositiveButton(getString(R.string.ok), (dialog, ___) -> {
              dialog.dismiss();
              final Intent intent = new Intent(requireContext(), LoginRegisterActivity.class);
              startActivity(intent);
            })
            .show();
        return;
      }
      final DocumentReference userRef = firestore.document(Constants.USERS_NAME_COLLECION + "/" + currentUser.getUid());

      userRef.get()
          .continueWithTask(task -> {
            final DocumentSnapshot documentSnapshot = requireNonNull(task.getResult());

            Comment comment =new Comment(
                currentUser.getUid(),
                roomId,
                requireNonNull(documentSnapshot.getString("full_name")),
                requireNonNull(documentSnapshot.getString("avatar")),
                commentStr,
                null,
                null
            );

            return firestore
                .collection(COMMENTS_NAME_COLLECION)
                .add(comment);
          })
          .addOnSuccessListener(requireActivity(), documentReference -> {
            Toast.makeText(requireContext(), R.string.add_comment_successfully, Toast.LENGTH_SHORT).show();
            requireNonNull(textInputComment.getEditText()).setText(null);
            final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(requireContext()) {
              @Override
              protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
              }
            };
            linearSmoothScroller.setTargetPosition(0);
            requireNonNull(recyclerComments.getLayoutManager()).startSmoothScroll(linearSmoothScroller);
          })
          .addOnFailureListener(requireActivity(), e -> {
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
            LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item_layout, parent, false),
            CommentFragment.this::onLongClick,
            CommentFragment.this::onClick);
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

    adapter.startListening();
  }

  private void onClick(View view, int position) {
    if (view.getId() == R.id.item_comment_image_avatar) {
      final Comment item = adapter.getItem(position);
      final Intent intent = new Intent(requireContext(), UserProfileActivity.class);
      intent.putExtra(EXTRA_USER_ID, item.getUserId());
      intent.putExtra(EXTRA_USER_FULL_NAME, item.getUserName());
      startActivity(intent);
    }
  }

  private void onLongClick(View view, int position) {
    final Comment item = adapter.getItem(position);
    final FirebaseUser currentUser = auth.getCurrentUser();
    Timber.tag("%$%").d("%s %s", item, currentUser);

    if (currentUser != null && Objects.equals(item.getUserId(), currentUser.getUid())) {
      final PopupMenu popupMenu = new PopupMenu(requireContext(), view);
      popupMenu.inflate(R.menu.comment_popup_menu);
      popupMenu.setOnMenuItemClickListener(menuItem -> {
        switch (menuItem.getItemId()) {
          case R.id.action_delete_comment:
            deleteComment(item.getId());
            return true;
          case R.id.action_edit_comment:
            editComment(item);
            return true;
        }
        return false;
      });
      popupMenu.show();
      Timber.tag("%$%").d("show dialog");
    }
  }

  private void editComment(Comment comment) {
    View view = getLayoutInflater().inflate(R.layout.edit_comment_dialog, null);
    final TextInputLayout textInputComment = view.findViewById(R.id.text_input_comment);
    final EditText editText = requireNonNull(textInputComment.getEditText());
    editText.setText(comment.getContent());

    editDialog = new AlertDialog.Builder(requireContext())
        .setTitle(R.string.edit_comment)
        .setIcon(R.drawable.ic_edit_black_24dp)
        .setView(view)
        .setNegativeButton(R.string.cancel, (dialog, __) -> dialog.dismiss())
        .setPositiveButton(R.string.ok, (dialog, __) -> {
          dialog.dismiss();
          final String newContent = editText.getText().toString();
          if (!Objects.equals(newContent, comment.getContent())) {
            Map<String, Object> map = new HashMap<>();
            map.put("content", newContent);
            map.put("updated_at", FieldValue.serverTimestamp());

            firestore
                .document(COMMENTS_NAME_COLLECION + "/" + comment.getId())
                .update(map)
                .addOnSuccessListener(requireActivity(), documentReference -> {
                  Toast.makeText(requireContext(), R.string.edit_comment_successfully, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(requireActivity(), e -> {
                  Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
          }
        })
        .show();
  }

  private void deleteComment(String id) {
    deleteDialog = new AlertDialog.Builder(requireContext())
        .setTitle(R.string.delete_comment)
        .setMessage(R.string.sure_delete_comment)
        .setIcon(R.drawable.ic_delete_grey_24dp)
        .setNegativeButton(R.string.cancel, (dialog, __) -> dialog.dismiss())
        .setPositiveButton(R.string.ok, (dialog, __) -> {
          dialog.dismiss();
          firestore
              .document(COMMENTS_NAME_COLLECION + "/" + id)
              .delete()
              .addOnSuccessListener(requireActivity(), documentReference -> {
                Toast.makeText(requireContext(), R.string.delete_comment_successfully, Toast.LENGTH_SHORT).show();
              })
              .addOnFailureListener(requireActivity(), e -> {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
              });
        })
        .show();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (requireLoginDialog != null && requireLoginDialog.isShowing()) {
      requireLoginDialog.dismiss();
    }
    if (deleteDialog != null && deleteDialog.isShowing()) {
      deleteDialog.dismiss();
    }
    if (editDialog != null && editDialog.isShowing()) {
      editDialog.dismiss();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    adapter.stopListening();

    unbinder.unbind();
  }
}
