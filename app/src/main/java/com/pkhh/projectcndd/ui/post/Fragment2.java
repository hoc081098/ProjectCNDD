package com.pkhh.projectcndd.ui.post;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pkhh.projectcndd.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import static com.pkhh.projectcndd.ui.post.Activity_district.ID_DIS;
import static com.pkhh.projectcndd.ui.post.Activity_district.NAME_DIS;
import static com.pkhh.projectcndd.ui.post.WardActivity.NAME_WARD;

public class Fragment2 extends Fragment implements View.OnClickListener {
    public static final String ID_PRO = "ID_PRO";
    private ConstraintLayout constraintLayout_province;
    private ConstraintLayout constraintLayout_district;
    private ConstraintLayout constraintLayout_wards;
    private TextView tv_province_name;
    private TextView tv_district_name;
    private TextView tv_ward_name;
    private String province_name ;
    private String province_id;
    private String district_name ;
    private String district_id;
    private String ward_name;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        constraintLayout_province.setOnClickListener(this);
        constraintLayout_district.setOnClickListener(this);
        constraintLayout_wards.setOnClickListener(this);

    }


    private void initView(View view) {
        constraintLayout_province = view.findViewById(R.id.layout_province);
        constraintLayout_district = view.findViewById(R.id.layout_districts);
        constraintLayout_wards = view.findViewById(R.id.layout_wards);
        tv_province_name = view.findViewById(R.id.tv_province);
        tv_district_name = view.findViewById(R.id.tv_district);
        tv_ward_name = view.findViewById(R.id.tv_ward);
    }

    public void updateText(CharSequence s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_province: {
                startActivityForResult(new Intent(requireContext(), Activity_province.class), 0);
            }
            break;
            case R.id.layout_districts: {
                if (province_name != null) {
                    Intent intent = new Intent(requireContext(), Activity_district.class);
                    intent.putExtra(ID_PRO, province_id);
                    startActivityForResult(intent,1);

                } else
                    Toast.makeText(getContext(), "Bạn phải chọn tỉnh trước", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.layout_wards: {
                if (district_name != null) {
                    Intent intent = new Intent(requireContext(), WardActivity.class);
                    intent.putExtra(ID_PRO, province_id);
                    intent.putExtra(ID_DIS, district_id);
                    startActivityForResult(intent, 2);

                } else
                    Toast.makeText(getContext(), "Bạn phải chọn huyện trước", Toast.LENGTH_SHORT).show();
                break;
            }

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            if (resultCode == Activity.RESULT_OK){
                province_id = data. getStringExtra("ID_PRO");
                province_name = data.getStringExtra("NAME_PRO");
                tv_province_name.setText(province_name);
            }
        }
        if(requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                district_id = data. getStringExtra(ID_DIS);
                district_name = data.getStringExtra(NAME_DIS);
                tv_district_name.setText(district_name);
            }
        }
        if(requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                ward_name = data.getStringExtra(NAME_WARD);
                tv_ward_name.setText(ward_name);
            }
        }
    }
}