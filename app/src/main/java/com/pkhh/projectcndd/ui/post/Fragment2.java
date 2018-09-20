package com.pkhh.projectcndd.ui.post;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pkhh.projectcndd.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment implements View.OnClickListener {
    private ConstraintLayout constraintLayout_province;
    private ConstraintLayout constraintLayout_district;
    private ConstraintLayout constraintLayout_wards;

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
    }

    public void updateText(CharSequence s) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_province:
                startActivityForResult(new Intent(requireContext(), Activity_province.class), 0);
                break;
            case R.id.layout_districts:
                startActivity(new Intent(requireContext(), Activity_district.class));
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

        }
    }
}
