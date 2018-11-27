package com.pkhh.projectcndd.screen.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.Category;
import com.pkhh.projectcndd.models.FirebaseModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.pkhh.projectcndd.utils.Constants.CATEGORIES_NAME_COLLECION;

/**
 * @author Peter Hoc
 * Updated on 4:27 AM, 9/30/2018
 */

class CategoryAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {
  private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK = new DiffUtil.ItemCallback<Object>() {

    @Override
    public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
      if (oldItem instanceof SelectionCategory && newItem instanceof SelectionCategory) {
        return ((SelectionCategory) oldItem).category.getId().equals(((SelectionCategory) newItem).category.getId());
      }
      return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
      return oldItem.equals(newItem);
    }
  };

  @Nullable
  private final Consumer<Category> onSelectCategory;

  @Nullable
  private Category selectedCategory;
  private List<Category> categories;

  CategoryAdapter(@Nullable Consumer<Category> onSelectCategory) {
    super(DIFF_CALLBACK);
    this.onSelectCategory = onSelectCategory;
  }

  void submitListCategories(@NonNull List<Category> categories) {
    this.categories = categories;
    if (selectedCategory == null) {
      List<Object> selectionCategories = Stream.ofNullable(categories)
          .map(i -> new SelectionCategory(false, i))
          .collect(Collectors.toList());
      super.submitList(selectionCategories);
      return;
    }
    List<Object> list = new ArrayList<>();
    list.add("Đã chọn");
    list.add(new SelectionCategory(true, selectedCategory));
    list.add("Lựa chọn khác");
    List<SelectionCategory> notSelected = Stream.ofNullable(categories)
        .filterNot(i -> i.equals(selectedCategory))
        .map(i -> new SelectionCategory(false, i))
        .toList();
    list.addAll(notSelected);
    super.submitList(list);
  }

  @LayoutRes
  @Override
  public int getItemViewType(int position) {
    Object item = getItem(position);
    if (item instanceof String) {
      return R.layout.province_item_layout;
    }
    if (item instanceof SelectionCategory) {
      return R.layout.category_item_layout;
    }
    throw new IllegalStateException("Don't know item at position: " + position + ", item: " + item);
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @LayoutRes int viewType) {
    final View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

    if (viewType == R.layout.category_item_layout) {
      return new CategoryViewHolder(itemView);
    }

    if (viewType == R.layout.province_item_layout) {
      return new HeaderViewHolder(itemView);
    }

    throw new IllegalStateException();
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    final Object item = getItem(position);

    if (item instanceof SelectionCategory && holder instanceof CategoryViewHolder) {
      ((CategoryViewHolder) holder).bind((SelectionCategory) item);
      return;
    }

    if (item instanceof String && holder instanceof HeaderViewHolder) {
      ((HeaderViewHolder) holder).bind((String) item);
    }
  }

  static class HeaderViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.text_province_name)
    TextView textView;

    HeaderViewHolder(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    void bind(String s) {
      textView.setText(s);
    }
  }

  private static class SelectionCategory {
    final boolean isSelected;
    final Category category;

    private SelectionCategory(boolean isSelected, Category category) {
      this.isSelected = isSelected;
      this.category = category;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SelectionCategory that = (SelectionCategory) o;
      return isSelected == that.isSelected && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
      return Objects.hash(isSelected, category);
    }
  }

  class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.radio_button) RadioButton radioButton;
    @BindView(R.id.text_category_name) TextView textCategoryName;

    CategoryViewHolder(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      itemView.setOnClickListener(this);
      radioButton.setOnClickListener(this);
    }

    void bind(SelectionCategory item) {
      radioButton.setChecked(item.isSelected);
      textCategoryName.setText(item.category.getName());
    }

    @Override
    public void onClick(View v) {
      final int adapterPosition = getAdapterPosition();
      if (adapterPosition != NO_POSITION) {
        Object item = getItem(adapterPosition);
        if (item instanceof SelectionCategory) {

          selectedCategory = ((SelectionCategory) item).category;
          if (onSelectCategory != null) onSelectCategory.accept(selectedCategory);
          submitListCategories(categories);

        }
      }
    }
  }
}

public class SelectCategoryFragment extends StepFragment<CategoryFragmentOutput> {
  private final Query query = FirebaseFirestore.getInstance().collection(CATEGORIES_NAME_COLLECION);
  private final CategoryAdapter adapter = new CategoryAdapter(this::onSelectCategory);
  @BindView(R.id.recycler_category) RecyclerView recyclerView;
  private ListenerRegistration registration;

  private Void onSelectCategory(Category category) {
    getDataOutput().setSelectedCategoryId(category.getId());
    return null;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupRecyclerView();
  }

  @Override
  public void onResume() {
    super.onResume();

    registration = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
      if (e != null) return;
      if (queryDocumentSnapshots != null) {
        List<Category> categories = FirebaseModel.querySnapshotToObjects(queryDocumentSnapshots, Category.class);
        adapter.submitListCategories(categories);
      }
    });
  }

  @Override
  public void onPause() {
    super.onPause();
    registration.remove();
  }

  private void setupRecyclerView() {
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), VERTICAL));
    recyclerView.setAdapter(adapter);
  }

  @Override
  public void onInvalid() {
    super.onInvalid();
    Snackbar.make(Objects.requireNonNull(getView()), "Hãy chọn một thể loại", Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public int getLayoutId() { return R.layout.fragment_select_category; }

  @NotNull
  @Override
  public CategoryFragmentOutput initialData() {
    return new CategoryFragmentOutput();
  }

  @Override
  public boolean isInvalidData() {
    return getDataOutput().getSelectedCategoryId() == null;
  }
}
