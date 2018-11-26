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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import timber.log.Timber;

import static com.pkhh.projectcndd.utils.Constants.EXTRA_ADDRESS;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_DISTRICT_ID;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_DISTRICT_NAME;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_LATLNG;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_PROVINCE_ID;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_PROVINCE_NAME;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_WARD_ID;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_WARD_NAME;
import static java.util.Objects.requireNonNull;

public class SelectAddressLocationFragment extends StepFragment<AddressLocationFragmentOutput> implements View.OnClickListener {
  public static final int REQUEST_CODE_SELECT_PROVINCE = 0;
  public static final int REQUEST_CODE_SELECT_DISTRICT = 1;
  public static final int REQUEST_CODE_SELECT_WARD = 2;
  public static final int REQUEST_CODE_PICK_ADDRESS = 3;

  @BindView(R.id.layout_province) ConstraintLayout mConstraintLayoutProvince;
  @BindView(R.id.layout_districts) ConstraintLayout mConstraintLayoutDistrict;
  @BindView(R.id.layout_wards) ConstraintLayout mConstraintLayoutWard;
  @BindView(R.id.tv_province) TextView mTextViewProvinceName;
  @BindView(R.id.tv_district) TextView mTextViewDistrictName;
  @BindView(R.id.tv_ward) TextView mTextViewWardName;
  @BindView(R.id.edit_text_address) TextInputLayout mEditTextAddress;
  @BindView(R.id.edit_lat) TextInputLayout mTextInputLat;
  @BindView(R.id.edit_lng) TextInputLayout mTextInputLng;

  @Override
  public int getLayoutId() { return R.layout.fragment_select_address_location; }

  @Nullable private Double latInput = null;
  @Nullable private Double lngInput = null;

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mConstraintLayoutProvince.setOnClickListener(this);
    mConstraintLayoutDistrict.setOnClickListener(this);
    mConstraintLayoutWard.setOnClickListener(this);
    view.findViewById(R.id.image_current_location).setOnClickListener(this);

    setupEditTexts();
  }

  private void setupEditTexts() {
    requireNonNull(mEditTextAddress.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String addressString = s.toString();
        if (addressString.isEmpty()) {
          getDataOutput().setAddress(null);
          mEditTextAddress.setError("Hãy nhập địa chỉ");
        } else {
          getDataOutput().setAddress(addressString);
          mEditTextAddress.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    requireNonNull(mTextInputLat.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String latString = s.toString();
        if (latString.isEmpty()) {

          getDataOutput().setLatLng(null);
          latInput = null;
          mTextInputLat.setError("Hãy nhập vĩ độ");
          return;

        }

        try {
          final double lat = Double.parseDouble(latString);

          if (-90D <= lat && lat <= 90D) {

            if (lngInput != null) {
              getDataOutput().setLatLng(new LatLng(lat, lngInput));
            }
            latInput = lat;
            mTextInputLat.setError(null);

          } else {

            latInput = null;
            getDataOutput().setLatLng(null);
            mTextInputLat.setError("Vĩ độ phải trong khoảng -90..90");

          }
        } catch (NumberFormatException e) {

          getDataOutput().setLatLng(null);
          latInput = null;
          mTextInputLat.setError("Vĩ độ sai định dạng");

        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    requireNonNull(mTextInputLng.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String lngString = s.toString();
        if (lngString.isEmpty()) {

          lngInput = null;
          getDataOutput().setLatLng(null);
          mTextInputLng.setError("Hãy nhập kinh độ");
          return;

        }

        try {
          final double lng = Double.parseDouble(lngString);

          if (-180D <= lng && lng <= 180D) {

            if (latInput != null) {
              getDataOutput().setLatLng(new LatLng(latInput, lng));
            }
            lngInput = lng;
            mTextInputLng.setError(null);

          } else {

            lngInput = null;
            getDataOutput().setLatLng(null);
            mTextInputLng.setError("Kinh độ phải trong khoảng -180..180");

          }
        } catch (NumberFormatException e) {

          lngInput = null;
          getDataOutput().setLatLng(null);
          mTextInputLng.setError("Kinh độ sai định dạng");

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
        pickAddressIntent.putExtra(EXTRA_LATLNG, getDataOutput().getLatLng());
        pickAddressIntent.putExtra(EXTRA_ADDRESS, getDataOutput().getAddress());
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
      final String addressString = data.getCharSequenceExtra(EXTRA_ADDRESS).toString();
      final LatLng latLng = data.getParcelableExtra(EXTRA_LATLNG);

      getDataOutput().setAddress(addressString);
      getDataOutput().setLatLng(latLng);

      requireNonNull(mEditTextAddress.getEditText()).setText(addressString);
      requireNonNull(mTextInputLat.getEditText()).setText(String.format(Locale.getDefault(), "%.4f", latLng.latitude));
      requireNonNull(mTextInputLng.getEditText()).setText(String.format(Locale.getDefault(), "%.4f", latLng.longitude));
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
    Snackbar.make(requireNonNull(getView()), "Hãy cung cấp đầy đủ địa chỉ", Snackbar.LENGTH_SHORT).show();
  }

  @NotNull
  @Override
  public AddressLocationFragmentOutput initialData() {
    return new AddressLocationFragmentOutput();
  }

  @Override
  public boolean isInvalidData() {
    final AddressLocationFragmentOutput dataOutput = getDataOutput();
    Timber.tag("@@@").d(dataOutput.toString());
    return dataOutput.component1() == null ||
        dataOutput.component2() == null ||
        dataOutput.component3() == null ||
        dataOutput.component4() == null ||
        dataOutput.component5() == null ||
        dataOutput.component6() == null;
  }
}