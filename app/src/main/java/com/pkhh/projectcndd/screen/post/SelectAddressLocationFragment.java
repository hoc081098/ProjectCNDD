package com.pkhh.projectcndd.screen.post;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.pkhh.projectcndd.R;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import butterknife.BindView;

public class SelectAddressLocationFragment extends StepFragment<AddressLocationFragmentOutput> implements View.OnClickListener {
  public static final int REQUEST_CODE_SELECT_PROVINCE = 0;
  public static final int REQUEST_CODE_SELECT_DISTRICT = 1;
  public static final int REQUEST_CODE_SELECT_WARD = 2;
  public static final int REQUEST_CODE_PICK_ADDRESS = 3;

  public static final String EXTRA_PROVINCE_ID = "EXTRA_PROVINCE_ID";
  public static final String EXTRA_PROVINCE_NAME = "EXTRA_PROVINCE_NAME";
  public static final String EXTRA_DISTRICT_ID = "EXTRA_DISTRICT_ID";
  public static final String EXTRA_DISTRICT_NAME = "EXTRA_DISTRICT_NAME";
  public static final String EXTRA_WARD_ID = "EXTRA_WARD_ID";
  public static final String EXTRA_WARD_NAME = "EXTRA_WARD_NAME";

  @BindView(R.id.layout_province)
  ConstraintLayout mConstraintLayoutProvince;

  @BindView(R.id.layout_districts)
  ConstraintLayout mConstraintLayoutDistrict;

  @BindView(R.id.layout_wards)
  ConstraintLayout mConstraintLayoutWard;

  @BindView(R.id.tv_province)
  TextView mTextViewProvinceName;

  @BindView(R.id.tv_district)
  TextView mTextViewDistrictName;

  @BindView(R.id.tv_ward)
  TextView mTextViewWardName;

  @BindView(R.id.edit_text_address)
  TextInputLayout mEditTextAddress;

  @BindView(R.id.edit_lat)
  TextInputLayout mTextInputLat;

  @BindView(R.id.edit_lng)
  TextInputLayout mTextInputLng;

  @Override
  public int getLayoutId() { return R.layout.fragment_select_address_location; }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mConstraintLayoutProvince.setOnClickListener(this);
    mConstraintLayoutDistrict.setOnClickListener(this);
    mConstraintLayoutWard.setOnClickListener(this);

    view.findViewById(R.id.image_current_location).setOnClickListener(this);

    Objects.requireNonNull(mEditTextAddress.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().isEmpty()) {
          mEditTextAddress.setError("Hãy nhập địa chỉ");
        } else {
          mEditTextAddress.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    Objects.requireNonNull(mTextInputLat.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().isEmpty()) {
          mTextInputLat.setError("Hãy nhập vĩ độ");
        } else {
          mTextInputLat.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    Objects.requireNonNull(mTextInputLng.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().isEmpty()) {
          mTextInputLng.setError("Hãy nhập kinh độ");
        } else {
          mTextInputLng.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.layout_province:
        startActivityForResult(new Intent(requireContext(), ProvinceActivity.class), REQUEST_CODE_SELECT_PROVINCE);
        break;
      case R.id.layout_districts:
        if (getDataOutput().getProvinceId() != null) {
          Intent intent = new Intent(requireContext(), DistrictActivity.class);
          intent.putExtra(EXTRA_PROVINCE_ID, getDataOutput().getProvinceId());
          startActivityForResult(intent, REQUEST_CODE_SELECT_DISTRICT);
        } else {
          Toast.makeText(getContext(), "Bạn phải chọn tỉnh trước", Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.layout_wards:
        if (getDataOutput().getProvinceId() == null) {
          Toast.makeText(requireContext(), "Bạn phải chọn tỉnh trước", Toast.LENGTH_SHORT).show();
          break;
        }
        if (getDataOutput().getProvinceId() == null) {
          Toast.makeText(getContext(), "Bạn phải chọn huyện trước", Toast.LENGTH_SHORT).show();
          break;
        }

        Intent intent = new Intent(requireContext(), WardActivity.class);
        intent.putExtra(EXTRA_PROVINCE_ID, getDataOutput().getProvinceId());
        intent.putExtra(EXTRA_DISTRICT_ID, getDataOutput().getDistrictId());
        startActivityForResult(intent, REQUEST_CODE_SELECT_WARD);
        break;
      case R.id.image_current_location:
        final Intent pickAddressIntent = new Intent(requireContext(), PickAddressActivity.class);
        pickAddressIntent.putExtra(PickAddressActivity.EXTRA_LATLNG, getDataOutput().getLatLng());
        pickAddressIntent.putExtra(PickAddressActivity.EXTRA_ADDRESS, getDataOutput().getAddress());
        startActivityForResult(pickAddressIntent, REQUEST_CODE_PICK_ADDRESS);
        break;
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_SELECT_PROVINCE && resultCode == Activity.RESULT_OK && data != null) {
      final String provinceId = data.getStringExtra(EXTRA_PROVINCE_ID);
      if (provinceId == null) {
        resetProvinceText();
        resetDistrictText();
        resetWardText();
      } else if (!provinceId.equals(getDataOutput().getProvinceId())) {
        getDataOutput().setProvinceId(provinceId);

        resetDistrictText();
        resetWardText();
        mTextViewProvinceName.setText(data.getStringExtra(EXTRA_PROVINCE_NAME));
        mTextViewProvinceName.setTextColor(Color.parseColor("#ff424242"));
      }
    }
    if (requestCode == REQUEST_CODE_SELECT_DISTRICT && resultCode == Activity.RESULT_OK && data != null) {
      final String districtId = data.getStringExtra(EXTRA_DISTRICT_ID);
      if (districtId == null) {
        resetDistrictText();
        resetWardText();
      } else if (!districtId.equals(getDataOutput().getDistrictId())) {
        final String districtName = data.getStringExtra(EXTRA_DISTRICT_NAME);
        getDataOutput().setDistrictId(districtId);
        getDataOutput().setDistrictName(districtName);
        mTextViewDistrictName.setText(districtName);
        mTextViewDistrictName.setTextColor(Color.parseColor("#ff424242"));
        resetWardText();
      }
    }
    if (requestCode == REQUEST_CODE_SELECT_WARD && resultCode == Activity.RESULT_OK && data != null) {
      getDataOutput().setWardId(data.getStringExtra(EXTRA_WARD_ID));
      mTextViewWardName.setText(data.getStringExtra(EXTRA_WARD_NAME));
      mTextViewWardName.setTextColor(Color.parseColor("#ff424242"));
    }
    if (requestCode == REQUEST_CODE_PICK_ADDRESS && resultCode == Activity.RESULT_OK && data != null) {
      final String addressString = data.getCharSequenceExtra(PickAddressActivity.EXTRA_ADDRESS).toString();
      final LatLng latLng = data.getParcelableExtra(PickAddressActivity.EXTRA_LATLNG);

      getDataOutput().setAddress(addressString);
      getDataOutput().setLatLng(latLng);

      Objects.requireNonNull(mEditTextAddress.getEditText()).setText(addressString);
      if (latLng != null) {
        Objects.requireNonNull(mTextInputLat.getEditText()).setText(String.format(Locale.getDefault(), "%.4f", latLng.latitude));
        Objects.requireNonNull(mTextInputLng.getEditText()).setText(String.format(Locale.getDefault(), "%.4f", latLng.longitude));
      } else {
        Objects.requireNonNull(mTextInputLat.getEditText()).setText("");
        Objects.requireNonNull(mTextInputLng.getEditText()).setText("");
      }
    }
  }

  private void resetWardText() {
    getDataOutput().setWardId(null);
    mTextViewWardName.setText(getString(R.string.select_ward));
    mTextViewWardName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError));
  }

  private void resetProvinceText() {
    getDataOutput().setProvinceId(null);
    mTextViewProvinceName.setText(getString(R.string.select_ward));
    mTextViewProvinceName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError));
  }

  private void resetDistrictText() {
    getDataOutput().setDistrictName(null);
    getDataOutput().setDistrictId(null);
    mTextViewDistrictName.setText(getString(R.string.select_ward));
    mTextViewDistrictName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError));
  }

  @Override
  public void onInvalid() {
    super.onInvalid();
    Snackbar.make(Objects.requireNonNull(getView()), "Hãy cung cấp đầy đủ địa chỉ", Snackbar.LENGTH_SHORT).show();
  }

  @NotNull
  @Override
  public AddressLocationFragmentOutput initialData() {
    return new AddressLocationFragmentOutput();
  }

  @Override
  public boolean isInvalidData() {
    final AddressLocationFragmentOutput dataOutput = getDataOutput();
    return dataOutput.component1() == null ||
        dataOutput.component2() == null ||
        dataOutput.component3() == null ||
        dataOutput.component4() == null ||
        dataOutput.component5() == null ||
        dataOutput.component6() == null;
  }
}