package com.pkhh.projectcndd.ui.home;

import com.firebase.ui.firestore.SnapshotParser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.models.FirebaseModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public abstract class MyFirebaseLoadMoreAdapter<T extends FirebaseModel> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  static final int TYPE_LOAD_MORE = 1;
  static final int TYPE_FIREBASE_MODEL_ITEM = 2;
  private static final Object LOAD_MORE_ITEM = new Object();
  private final int pageSize;
  private final Query query;
  private final Class<T> tClass;

  private @Nullable
  DocumentSnapshot lastVisible;
  private @NonNull
  List<Object> list = new ArrayList<>();

  private boolean isLoading = false;
  private boolean isLastItemReached = false;
  private boolean hasError = false;
  private double visibleThreshold;
  private SnapshotParser<T> snapshotParser;


  MyFirebaseLoadMoreAdapter(@NonNull Query query, int pageSize, @NonNull RecyclerView recyclerView,
                            @NonNull Class<T> tClass) {
    this(query, pageSize, recyclerView, tClass, snapshot -> FirebaseModel.documentSnapshotToObject(snapshot, tClass));
  }

  MyFirebaseLoadMoreAdapter(@NonNull Query query, int pageSize, @NonNull RecyclerView recyclerView,
                            @NonNull Class<T> tClass, @NonNull SnapshotParser<T> snapshotParser) {

    this.pageSize = pageSize;
    this.query = query;
    this.tClass = tClass;
    this.visibleThreshold = pageSize;
    this.snapshotParser = snapshotParser;

    final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
    if (!(layoutManager instanceof LinearLayoutManager)) {
      throw new IllegalStateException("Not implementation");
    }
    final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        int itemCount = linearLayoutManager.getItemCount();
        if (lastVisible != null && !isLoading
            && lastVisibleItemPosition + visibleThreshold >= itemCount
            && !hasError && !isLastItemReached) {
          loadMore();
          isLoading = true;
        }

        if (lastVisibleItemPosition == itemCount - 1 && isLastItemReached) {
          onLastItemReached();
        }
      }
    });
    loadMore();
  }


  public void refresh() {
    lastVisible = null;
    isLoading = false;
    isLastItemReached = false;
    hasError = false;
    loadMore();
  }

  private void loadMore() {
    if (lastVisible != null) {
      list.add(LOAD_MORE_ITEM);
      notifyItemInserted(list.size() - 1);
    }

    (lastVisible != null ? query.startAfter(lastVisible) : query)
        .limit(pageSize)
        .get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          final List<T> firebaseModels = com.annimon.stream.Stream.of(queryDocumentSnapshots.getDocuments())
              .map(snapshotParser::parseSnapshot)
              .toList();

          if (!this.list.isEmpty()) {
            this.list.remove(this.list.size() - 1);
            notifyItemRemoved(this.list.size());
          }
          if (lastVisible == null) {
            this.list.clear();
            notifyDataSetChanged();
          }
          int oldSize = this.list.size();
          this.list.addAll(firebaseModels);
          notifyItemRangeInserted(oldSize, firebaseModels.size());

          if (lastVisible == null) {
            onFirstLoaded();
          }
          final List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
          if (!documents.isEmpty()) {
            lastVisible = documents.get(queryDocumentSnapshots.size() - 1);
          }
          if (queryDocumentSnapshots.size() < pageSize) {
            isLastItemReached = true;
          }
          isLoading = false;
        })
        .addOnFailureListener(e -> hasError = true);
  }

  protected void onLastItemReached() {
  }

  protected void onFirstLoaded() {
  }

  @NonNull
  public Object getItem(int position) {
    return list.get(position);
  }

  @NonNull
  public List<Object> getList() {
    return list;
  }

  public void setList(@NonNull List<Object> list) {
    this.list = list;
    notifyDataSetChanged();
  }

  @Override
  public int getItemViewType(int position) {
    Object item = getItem(position);
    if (item == LOAD_MORE_ITEM) {
      return TYPE_LOAD_MORE;
    }
    if (item.getClass().equals(tClass)) {
      return TYPE_FIREBASE_MODEL_ITEM;
    }
    return 0;
  }

  @Override
  public int getItemCount() {
    return list.size();
  }
}
