package com.pkhh.projectcndd.ui.loginregister;

import android.os.Bundle;
import android.view.MenuItem;

import com.pkhh.projectcndd.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static java.util.Objects.requireNonNull;

public final class LoginRegisterActivity extends AppCompatActivity implements LoginFragment.OnRegisterClick, RegisterFragment.OnLoginClick {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.login_register_container, new LoginFragment(), "LOGIN_FRAGMENT")
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRegisterClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.login_register_container, new RegisterFragment(), "REGISTER_FRAGMENT")
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoginClick() {
        getSupportFragmentManager().popBackStack();
    }
}
