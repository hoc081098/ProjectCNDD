package com.pkhh.projectcndd.ui.loginregister;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;
import static com.pkhh.projectcndd.utils.FirebaseUtil.getMessageFromFirebaseAuthExceptionErrorCode;

public class RegisterFragment extends Fragment implements View.OnClickListener {
    private static final int CHOOSE_AVATAR_IMAGE_REQUEST_CODE = 1;

    private ImageView mImageAvatar;
    private EditText mEditName;
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mButtonRegister;
    private View mButtonBackToLogin;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStorage;

    private Listener mListener;
    @Nullable
    private Uri mSelectedImageUri;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (Listener) context;
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
        mFirebaseStorage = FirebaseStorage.getInstance();

        mButtonRegister.setOnClickListener(this);
        mButtonBackToLogin.setOnClickListener(this);
        mImageAvatar.setOnClickListener(this);
    }

    private void initViews(@NonNull View view) {
        mEditName = view.findViewById(R.id.edit_full_name);
        mEditEmail = view.findViewById(R.id.edit_email);
        mEditPassword = view.findViewById(R.id.edit_password);
        mButtonRegister = view.findViewById(R.id.button_register);
        mButtonBackToLogin = view.findViewById(R.id.button_back_to_login);
        mImageAvatar = view.findViewById(R.id.image_avatar);
    }

    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.button_register:
                onRegister();
                break;
            case R.id.button_back_to_login:
                mListener.onLoginClick();
                break;
            case R.id.image_avatar:
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), CHOOSE_AVATAR_IMAGE_REQUEST_CODE);
                break;
        }
    }

    private void onRegister() {
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

        mProgressDialog = new ProgressDialog(requireContext());
        mProgressDialog.setTitle("Đang xử lý");
        mProgressDialog.setMessage("Vui lòng chờ...");
        mProgressDialog.show();
        mButtonRegister.setEnabled(false);

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    final String uid = authResult.getUser().getUid();

                    final Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("full_name", name);
                    user.put("is_active", true);
                    user.put("phone", "");
                    user.put("address", "");
                    user.put("avatar", "");
                    user.put("created_at", FieldValue.serverTimestamp());

                    if (mSelectedImageUri == null) {
                        insertUserToFirestore(uid, user);
                    } else {
                        uploadAvatarToStorage(uid, user, mSelectedImageUri);
                    }
                })
                .addOnFailureListener(this::onError);
    }

    private void uploadAvatarToStorage(String uid, Map<String, Object> user, Uri uri) {
        final StorageReference reference = mFirebaseStorage.getReference("avatar_images/" + uid);
        reference.putFile(uri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return reference.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.put("avatar", task.getResult().toString());
                        insertUserToFirestore(uid, user);
                    } else {
                        onError(task.getException());
                    }
                });
    }

    private void onError(@Nullable Exception e) {
        mProgressDialog.dismiss();
        mButtonRegister.setEnabled(true);

        String message = e instanceof FirebaseAuthException
                ? getMessageFromFirebaseAuthExceptionErrorCode(((FirebaseAuthException) e).getErrorCode())
                : e != null ? e.getMessage() : "Lỗi chưa xác định";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void insertUserToFirestore(String uid, Map<String, Object> user) {
        mFirebaseFirestore.document(Constants.USER_NAME_COLLECION + "/" + uid)
                .set(user)
                .addOnSuccessListener(documentReference -> {
                    mProgressDialog.dismiss();
                    mButtonRegister.setEnabled(true);
                    mListener.onRegisterSuccessfully();
                })
                .addOnFailureListener(this::onError);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_AVATAR_IMAGE_REQUEST_CODE
                && resultCode == RESULT_OK && data != null) {
            mSelectedImageUri = data.getData();
            if (mSelectedImageUri != null) {
                Picasso.get()
                        .load(mSelectedImageUri)
                        .fit()
                        .centerCrop()
                        .into(mImageAvatar);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    interface Listener {
        void onLoginClick();

        void onRegisterSuccessfully();
    }
}
