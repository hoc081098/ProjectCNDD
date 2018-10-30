package com.pkhh.projectcndd.ui.post;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;
import com.pkhh.projectcndd.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AddPriceTitleSizeDescriptionFragment extends Fragment {
  private static final int MIN_LENGTH_OF_TITLE = 10;


  @BindView(R.id.cardView)
  ViewGroup card1;

  @BindView(R.id.text_input_price)
  TextInputLayout textInputPrice;

  @BindView(R.id.text_price)
  TextView textPrice;

  @BindView(R.id.text_error_price)
  TextView textErrorPrice;

  @BindView(R.id.text_input_size)
  TextInputLayout textInputSize;

  @BindView(R.id.text_square_meter)
  TextView textSquareMeter;

  @BindView(R.id.text_input_title)
  TextInputLayout textInputTitle;

  @BindView(R.id.chip_suggest_title)
  Chip chipSuggestTitle;

  @BindView(R.id.text_input_description)
  TextInputLayout textInputDescription;

  @BindView(R.id.text_input_phone)
  TextInputLayout textInputPhone;

  private Unbinder unbinder;

  private EditText priceEditText;
  private boolean isPriceValid;

  private EditText sizeEditText;
  private boolean isSizeValid;

  private EditText titleEditText;
  private boolean isTitleValid;

  private EditText descriptionEditText;
  private EditText phoneEditText;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_add_price_title_size_description, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    priceEditText = textInputPrice.getEditText();
    sizeEditText = textInputSize.getEditText();
    titleEditText = textInputTitle.getEditText();
    descriptionEditText = textInputDescription.getEditText();
    phoneEditText = textInputPhone.getEditText();

    textSquareMeter.setText(
        HtmlCompat.fromHtml("m<sup><small>2</small></sup>", HtmlCompat.FROM_HTML_MODE_LEGACY)
    );

    addTextChangeListener();
  }

  private void addTextChangeListener() {
    priceEditText.addTextChangedListener(new TextWatcher() {

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        String str = s.toString();

        long price;
        try {
          price = Long.parseLong(str);
        } catch (NumberFormatException e) {
          price = 0;
          isPriceValid = false;
        }

        if (price < 100_000) {
          TransitionManager.beginDelayedTransition(card1);
          textErrorPrice.setVisibility(View.VISIBLE);
          isPriceValid = false;
        } else {
          TransitionManager.beginDelayedTransition(card1);
          textErrorPrice.setVisibility(View.GONE);
          isPriceValid = true;
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    sizeEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        double size;
        try {
          size = Double.parseDouble(s.toString());
          textInputSize.setError(null);
          isSizeValid = true;

          if (Double.compare(size, 0) <= 0) {
            isSizeValid = false;
            textInputSize.setError("Nhập sai diện tích");
          }
        } catch (NumberFormatException e) {
          textInputSize.setError("Nhập sai diện tích");
          isSizeValid = false;
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    titleEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() <= MIN_LENGTH_OF_TITLE) {
          textInputTitle.setError("Tiêu đề quá ngắn");
          isTitleValid = false;
        } else {
          textInputTitle.setError(null);
          isTitleValid = true;
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  public String getPhone() {
    return phoneEditText.getText().toString();
  }

  public long getPrice() {
    return Long.parseLong(priceEditText.getText().toString());
  }

  public double getSize() {
    return Double.parseDouble(sizeEditText.getText().toString());
  }

  public String getTitleText() {
    return titleEditText.getText().toString();
  }

  public String getDescriptionText() {
    return descriptionEditText.getText().toString();
  }

  public boolean canGoNext() {
    return isPriceValid && isTitleValid && isSizeValid;
  }
}
