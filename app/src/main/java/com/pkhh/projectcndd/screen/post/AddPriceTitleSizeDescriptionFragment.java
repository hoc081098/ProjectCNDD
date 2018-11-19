package com.pkhh.projectcndd.screen.post;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.hoc.lib.NumberToVietnamese;
import com.pkhh.projectcndd.R;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import butterknife.BindView;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;

public class AddPriceTitleSizeDescriptionFragment extends StepFragment<PriceTitleSizeDescriptionFragmentOutput> {
  private static final int MIN_LENGTH_OF_TITLE = 10;

  @BindView(R.id.text_input_price)
  TextInputLayout textInputPrice;

  @BindView(R.id.text_price)
  TextView textPrice;

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

  private EditText priceEditText;
  private boolean isPriceValid;

  private EditText sizeEditText;
  private boolean isSizeValid;

  private EditText titleEditText;
  private boolean isTitleValid;

  private EditText descriptionEditText;

  private EditText phoneEditText;
  private boolean isPhoneValid;

  @Nullable
  private String districtName;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    priceEditText = textInputPrice.getEditText();
    sizeEditText = textInputSize.getEditText();
    titleEditText = textInputTitle.getEditText();
    descriptionEditText = textInputDescription.getEditText();
    phoneEditText = textInputPhone.getEditText();

    textSquareMeter.setText(
        HtmlCompat.fromHtml("m<sup><small>2</small></sup>", FROM_HTML_MODE_LEGACY)
    );

    addTextChangeListener();

    chipSuggestTitle.setOnClickListener(__ -> titleEditText.setText(chipSuggestTitle.getText()));
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
        getDataOutput().setPrice(price);

        textPrice.setText(NumberToVietnamese.convert(price));

        if (price < 100_000) {
          textInputPrice.setError("Giá quá thấp. Vui lòng sửa lại giá thực tế");
          isPriceValid = false;
        } else if (price > 10_000_000) {
          textInputPrice.setError("Giá quá cao. Vui lòng sửa lại giá thực tế");
        } else {
          textInputPrice.setError(null);
          isPriceValid = true;
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    priceEditText.setText("");

    sizeEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        double size;
        try {
          size = Double.parseDouble(s.toString());

          if (Double.compare(size, 0) <= 0) {
            isSizeValid = false;
            textInputSize.setError("Nhập sai diện tích");
          } else {

            updateSuggestTitle(size);

            isSizeValid = true;
            textInputSize.setError(null);
          }

        } catch (NumberFormatException e) {
          textInputSize.setError("Nhập sai diện tích");
          isSizeValid = false;
          size = 0;
        }

        getDataOutput().setSize(size);
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    sizeEditText.setText("");

    titleEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() < MIN_LENGTH_OF_TITLE) {
          textInputTitle.setError("Tiêu đề quá ngắn");
          isTitleValid = false;
        } else {
          textInputTitle.setError(null);
          isTitleValid = true;
        }

        getDataOutput().setTitle(s.toString());
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    titleEditText.setText("");

    descriptionEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        getDataOutput().setDescription(s.toString());
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    descriptionEditText.setText("");

    phoneEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        isPhoneValid = Patterns.PHONE.matcher(s).matches();
        textInputPhone.setError(isPhoneValid ? null : "Số điện thoại sai định dạng. Vui lòng nhập lại!");
        getDataOutput().setPhone(s.toString());
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });
    phoneEditText.setText("");
  }

  private void updateSuggestTitle(double size) {
    chipSuggestTitle.setText(
        HtmlCompat.fromHtml(
            "Phòng trọ " + (districtName == null ? "" : districtName) + " " + new DecimalFormat("#.##").format(size) + "m<sup><small>2</small></sup>",
            FROM_HTML_MODE_LEGACY
        )
    );
  }

  @NotNull
  @Override
  public PriceTitleSizeDescriptionFragmentOutput initialData() {
    return new PriceTitleSizeDescriptionFragmentOutput();
  }

  @Override
  protected void onInvalid() {
    super.onInvalid();
    Snackbar.make(Objects.requireNonNull(getView()), "Hãy cung cấp đủ thông tin!", Snackbar.LENGTH_SHORT).show();
  }


  @Override
  public boolean isInvalidData() {
    return !(isPriceValid && isTitleValid && isSizeValid && isPhoneValid);
  }

  @Override
  public int getLayoutId() { return R.layout.fragment_add_price_title_size_description; }

  void setDistrictName(@Nullable String districtName) {
    this.districtName = districtName;
  }
}
