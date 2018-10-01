package com.pkhh.projectcndd.ui.post;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.Category;
import com.pkhh.projectcndd.models.FirebaseModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.pkhh.projectcndd.utils.Constants.CATEGORY_NAME_COLLECION;

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
    static final int TYPE_HEADER = 1;
    static final int TYPE_CATEGORY_ITEM = 2;

    @Nullable
    private Category selectedCategory;
    private List<Category> categories;

    CategoryAdapter() {
        super(DIFF_CALLBACK);
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

    @ViewType
    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof String) {
            return TYPE_HEADER;
        }
        if (item instanceof SelectionCategory) {
            return TYPE_CATEGORY_ITEM;
        }
        throw new IllegalStateException("Don't know item at position: " + position + ", item: " + item);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ViewType int viewType) {
        if (viewType == TYPE_CATEGORY_ITEM) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
            return new CategoryViewHolder(itemView);
        }
        if (viewType == TYPE_HEADER) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.province_item_layout, parent, false);
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

    @Nullable
    public Category getSelectedCategory() {
        return selectedCategory;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(String s) {
            itemView.<TextView>findViewById(R.id.text_province_name).setText(s);
        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RadioButton radioButton;
        private final TextView textCategoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radio_button);
            textCategoryName = itemView.findViewById(R.id.text_category_name);

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
                    submitListCategories(categories);
                }
            }
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

    @IntDef(value = {TYPE_CATEGORY_ITEM, TYPE_HEADER})
    @Retention(value = RetentionPolicy.SOURCE)
    private @interface ViewType {
    }
}

public class SelectCategoryFragment extends Fragment {
    private static final String TAG = SelectCategoryFragment.class.getSimpleName();

    private final Query query = FirebaseFirestore.getInstance().collection(CATEGORY_NAME_COLLECION);
    private final CategoryAdapter adapter = new CategoryAdapter();

    private RecyclerView recyclerView;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_category);
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();

        registration = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.d(TAG, "addSnapshotListener error=" + e, e);
                return;
            }
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

    @Nullable
    public Category getSelectedCategory() {
        return adapter.getSelectedCategory();
    }
}
