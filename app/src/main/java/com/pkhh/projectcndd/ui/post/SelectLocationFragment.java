package com.pkhh.projectcndd.ui.post;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.pkhh.projectcndd.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

public class SelectLocationFragment extends Fragment implements View.OnClickListener {
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

  private ConstraintLayout mConstraintLayoutProvince;
  private ConstraintLayout mConstraintLayoutDistrict;
  private ConstraintLayout mConstraintLayoutWard;
  private TextView mTextViewProvinceName;
  private TextView mTextViewDistrictName;
  private TextView mTextViewWardName;
  private EditText mEditTextAddress;
  private TextView mTextViewLatLng;

  @Nullable
  private String mProvinceName;
  @Nullable
  private String mProvinceId;

  @Nullable
  private String mDistrictName;
  @Nullable
  private String mDistrictId;

  @Nullable
  private String mWardName;
  @Nullable
  private String mWardId;

  @Nullable
  private String addressString;
  @Nullable
  private LatLng latLng;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_select_location, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView(view);

    mConstraintLayoutProvince.setOnClickListener(this);
    mConstraintLayoutDistrict.setOnClickListener(this);
    mConstraintLayoutWard.setOnClickListener(this);
    view.findViewById(R.id.image_current_location).setOnClickListener(this);
  }


  private void initView(View view) {
    mConstraintLayoutProvince = view.findViewById(R.id.layout_province);
    mConstraintLayoutDistrict = view.findViewById(R.id.layout_districts);
    mConstraintLayoutWard = view.findViewById(R.id.layout_wards);
    mTextViewProvinceName = view.findViewById(R.id.tv_province);
    mTextViewDistrictName = view.findViewById(R.id.tv_district);
    mTextViewWardName = view.findViewById(R.id.tv_ward);
    mEditTextAddress = view.findViewById(R.id.edit_text_address);
    mTextViewLatLng = view.findViewById(R.id.tv_latlng);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.layout_province:
        startActivityForResult(new Intent(requireContext(), ProvinceActivity.class), REQUEST_CODE_SELECT_PROVINCE);
        break;
      case R.id.layout_districts:
        if (mProvinceId != null) {
          Intent intent = new Intent(requireContext(), DistrictActivity.class);
          intent.putExtra(EXTRA_PROVINCE_ID, mProvinceId);
          startActivityForResult(intent, REQUEST_CODE_SELECT_DISTRICT);
        } else {
          Toast.makeText(getContext(), "Bạn phải chọn tỉnh trước", Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.layout_wards:
        if (mProvinceId == null) {
          Toast.makeText(requireContext(), "Bạn phải chọn tỉnh trước", Toast.LENGTH_SHORT).show();
          break;
        }
        if (mDistrictId == null) {
          Toast.makeText(getContext(), "Bạn phải chọn huyện trước", Toast.LENGTH_SHORT).show();
          break;
        }

        Intent intent = new Intent(requireContext(), WardActivity.class);
        intent.putExtra(EXTRA_PROVINCE_ID, mProvinceId);
        intent.putExtra(EXTRA_DISTRICT_ID, mDistrictId);
        startActivityForResult(intent, REQUEST_CODE_SELECT_WARD);
        break;
      case R.id.image_current_location:
        final Intent pickAddressIntent = new Intent(requireContext(), PickAddressActivity.class);
        pickAddressIntent.putExtra(PickAddressActivity.EXTRA_LATLNG, latLng);
        pickAddressIntent.putExtra(PickAddressActivity.EXTRA_ADDRESS, addressString);
        startActivityForResult(pickAddressIntent, REQUEST_CODE_PICK_ADDRESS);
        break;
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_SELECT_PROVINCE && resultCode == Activity.RESULT_OK && data != null) {
      mProvinceId = data.getStringExtra(EXTRA_PROVINCE_ID);
      mProvinceName = data.getStringExtra(EXTRA_PROVINCE_NAME);
      mTextViewProvinceName.setText(mProvinceName);
    }
    if (requestCode == REQUEST_CODE_SELECT_DISTRICT && resultCode == Activity.RESULT_OK && data != null) {
      mDistrictId = data.getStringExtra(EXTRA_DISTRICT_ID);
      mDistrictName = data.getStringExtra(EXTRA_DISTRICT_NAME);
      mTextViewDistrictName.setText(mDistrictName);
    }
    if (requestCode == REQUEST_CODE_SELECT_WARD && resultCode == Activity.RESULT_OK && data != null) {
      mWardName = data.getStringExtra(EXTRA_WARD_NAME);
      mWardId = data.getStringExtra(EXTRA_WARD_ID);
      mTextViewWardName.setText(mWardName);
    }
    if (requestCode == REQUEST_CODE_PICK_ADDRESS && resultCode == Activity.RESULT_OK && data != null) {
      addressString = data.getCharSequenceExtra(PickAddressActivity.EXTRA_ADDRESS).toString();
      latLng = data.getParcelableExtra(PickAddressActivity.EXTRA_LATLNG);

      mEditTextAddress.setText(addressString);
      if (latLng != null) {
        mTextViewLatLng.setVisibility(View.VISIBLE);
        final String s = HtmlCompat.fromHtml("<small>" +
            "<i>Kinh độ: %.4f</i>" +
            "<br>" +
            "<i>Vĩ độ  : %.4f</i>" +
            "</small>", HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
        mTextViewLatLng.setText(String.format(s, latLng.latitude, latLng.longitude));
      } else {
        mTextViewLatLng.setVisibility(View.INVISIBLE);
      }
    }
  }

  @Nullable
  public String getDistrictId() {
    return mDistrictId;
  }

  @Nullable
  public String getProvinceId() {
    return mProvinceId;
  }

  @Nullable
  public String getWardId() {
    return mWardId;
  }

  @Nullable
  public String getAddressString() {
    return addressString;
  }

  @Nullable
  public LatLng getLatLng() {
    return latLng;
  }
}