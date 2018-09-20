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
import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.User;
import com.pkhh.projectcndd.utils.Constants;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.pkhh.projectcndd.utils.FirebaseUtil.getMessageFromFirebaseAuthExceptionErrorCode;

public class RegisterFragment extends Fragment implements View.OnClickListener {
    private EditText mEditName;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mButtonRegister;
    private ProgressBar mProgressBar;
    private View mTextLogin;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private OnLoginClick onLoginClick;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onLoginClick = (OnLoginClick) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Đăng kí");
        initViews(view);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        mButtonRegister.setOnClickListener(this);

        mTextLogin.setOnClickListener(this);
    }

    private void initViews(@NonNull View view) {
        mEditName = view.findViewById(R.id.edit_full_name);
        mEditEmail = view.findViewById(R.id.edit_email);
        mEditPassword = view.findViewById(R.id.edit_password);
        mButtonRegister = view.findViewById(R.id.button_register);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mTextLogin = view.findViewById(R.id.text_login);
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

            final String name = mEditName.getText().toString();
            if (name.length() < 3) {
                mEditName.setError("Tên tối thiểu 3 kí tự!");
                isValid = false;
            }

            if (!isValid) {
                return;
            }

            mProgressBar.setVisibility(VISIBLE);
            mButtonRegister.setEnabled(false);

            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        final User user = new User();
                        user.email = email;
                        user.fullName = name;
                        user.isActive = true;
                        user.phone = "";
                        user.address = "";

                        mFirebaseFirestore.document(Constants.USER_NAME_COLLECION + "/" + authResult.getUser().getUid())
                                .set(user)
                                .addOnSuccessListener(documentReference -> {
                                    mProgressBar.setVisibility(INVISIBLE);
                                    mButtonRegister.setEnabled(true);

                                    Toast.makeText(requireContext(), "Đăng kí thành công", Toast.LENGTH_SHORT).show();
                                    requireActivity().finish();
                                })
                                .addOnFailureListener(e -> {
                                    mProgressBar.setVisibility(INVISIBLE);
                                    mButtonRegister.setEnabled(true);

                                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        mProgressBar.setVisibility(INVISIBLE);
                        mButtonRegister.setEnabled(true);

                        final String message = getMessageFromFirebaseAuthExceptionErrorCode(((FirebaseAuthException) e).getErrorCode());
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    });
        } else if (v.getId() == R.id.text_login) {
            onLoginClick.onLoginClick();
        }
    }

    interface OnLoginClick {
        void onLoginClick();
    }
}
