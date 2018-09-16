package com.pkhh.projectcndd.ui.login;

import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.pkhh.projectcndd.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.pkhh.projectcndd.utils.FirebaseUtil.getMessageFromFirebaseAuthExceptionErrorCode;
import static java.util.Objects.requireNonNull;

public final class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mButtonLogin;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final ActionBar actionBar = requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Đăng nhập");

        initViews();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mButtonLogin.setOnClickListener(this);
    }

    private void initViews() {
        mEditEmail = findViewById(R.id.edit_email);
        mEditPassword = findViewById(R.id.edit_password);
        mButtonLogin = findViewById(R.id.button_login);
        mProgressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.button_login) {
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
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        mProgressBar.setVisibility(INVISIBLE);
                        mButtonLogin.setEnabled(true);
                        final String message = getMessageFromFirebaseAuthExceptionErrorCode(((FirebaseAuthException) e).getErrorCode());
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    });

        }
    }
}
