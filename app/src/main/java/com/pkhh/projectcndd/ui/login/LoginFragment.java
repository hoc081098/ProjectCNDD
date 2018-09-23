package com.pkhh.projectcndd.ui.login;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.pkhh.projectcndd.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.pkhh.projectcndd.utils.FirebaseUtil.getMessageFromFirebaseAuthExceptionErrorCode;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mButtonLogin;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;
    private View mTextRegister;
    private OnRegisterClick onRegisterClick;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onRegisterClick = (OnRegisterClick) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Đăng nhập");
        initViews(view);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mButtonLogin.setOnClickListener(this);

        mTextRegister.setOnClickListener(this);
    }

    private void initViews(@NonNull View view) {
        mEditEmail = view.findViewById(R.id.edit_email);
        mEditPassword = view.findViewById(R.id.edit_password);
        mButtonLogin = view.findViewById(R.id.button_login);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mTextRegister = view.findViewById(R.id.text_register);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.button_register) {
            boolean isValid = true;

            final String email = mEditEmail.getText().toString();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mEditEmail.setError("Địa chỉ email không hợp lệ!");
                isValid = false;
            }

            final String password = mEditPassword.getText().toString();
            if (password.length() < 6) {
                mEditPassword.setError("Mật khẩu ít nhất 6 kí tự!");
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            mProgressBar.setVisibility(VISIBLE);
            mButtonLogin.setEnabled(false);
            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        mProgressBar.setVisibility(INVISIBLE);
                        mButtonLogin.setEnabled(true);
                        Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();
                    })
                    .addOnFailureListener(e -> {
                        mProgressBar.setVisibility(INVISIBLE);
                        mButtonLogin.setEnabled(true);
                        final String message = getMessageFromFirebaseAuthExceptionErrorCode(((FirebaseAuthException) e).getErrorCode());
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    });

        } else if (v.getId() == R.id.text_register) {
            onRegisterClick.onRegisterClick();
        }
    }

    interface OnRegisterClick {
        void onRegisterClick();
    }
}
