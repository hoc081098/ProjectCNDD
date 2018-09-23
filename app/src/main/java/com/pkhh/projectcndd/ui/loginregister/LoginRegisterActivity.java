package com.pkhh.projectcndd.ui.loginregister;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.ui.home.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static java.util.Objects.requireNonNull;

public final class LoginRegisterActivity extends AppCompatActivity implements LoginFragment.Listener, RegisterFragment.Listener {
    @Nullable
    private Class<?> clazz;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        clazz = (Class<?>) getIntent().getSerializableExtra(MainActivity.CLAZZ);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(R.id.login_register_container, new LoginFragment(), "LOGIN_FRAGMENT")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRegisterClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.login_register_container, new RegisterFragment(), "REGISTER_FRAGMENT")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoginSuccessfully() {
        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        if (clazz != null) {
            startActivity(new Intent(this, clazz));
        }
        finish();
    }

    @Override
    public void onLoginClick() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onRegisterSuccessfully() {
        Toast.makeText(this, "Đăng kí thành công", Toast.LENGTH_SHORT).show();
        if (clazz != null) {
            startActivity(new Intent(this, clazz));
        }
        finish();
    }
}
