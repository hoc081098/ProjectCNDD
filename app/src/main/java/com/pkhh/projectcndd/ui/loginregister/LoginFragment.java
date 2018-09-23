package com.pkhh.projectcndd.ui.loginregister;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import static com.pkhh.projectcndd.utils.FirebaseUtil.getMessageFromFirebaseAuthExceptionErrorCode;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mButtonLogin;
    private ProgressBar mProgressBar;
    private View mButtonRegister;
    private ViewGroup root_login_frag;

    private Listener mListener;
    private FirebaseAuth mFirebaseAuth;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (Listener) context;
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
        mButtonRegister.setOnClickListener(this);
    }

    private void initViews(@NonNull View view) {
        mEditEmail = view.findViewById(R.id.edit_email);
        mEditPassword = view.findViewById(R.id.edit_password);
        mButtonLogin = view.findViewById(R.id.button_login);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mButtonRegister = view.findViewById(R.id.button_register);
        root_login_frag = view.findViewById(R.id.root_login_frag);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.button_login) {
            onLogin();
        } else if (v.getId() == R.id.button_register) {
            mListener.onRegisterClick();
        }
    }

    private void onLogin() {
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

//        mProgressBar.setVisibility(VISIBLE);
//        mButtonLogin.setEnabled(false);
        int duration = 300;
        TransitionManager.beginDelayedTransition(root_login_frag, new TransitionSet()
                .addTransition(
                        new ChangeBounds()
                                .addTarget(mButtonLogin)
                                .setDuration(duration)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                )
                .addTransition(
                        new Fade()
                                .addTarget(mButtonLogin)
                                .setDuration(duration)
                )
                .addTransition(
                        new Fade().addTarget(mProgressBar)
                                .setDuration(duration)
                )
                .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
        );

        final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mButtonLogin.getLayoutParams();
        layoutParams.width = layoutParams.height;
        mButtonLogin.setLayoutParams(layoutParams);
        mButtonLogin.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
//                    mProgressBar.setVisibility(INVISIBLE);
//                    mButtonLogin.setEnabled(true);

                    final TransitionSet transition = new TransitionSet()
                            .addTransition(
                                    new Fade().addTarget(mProgressBar)
                                            .setDuration(duration)
                            )
                            .addTransition(
                                    new Fade()
                                            .addTarget(mButtonLogin)
                                            .setDuration(duration)
                            )
                            .addTransition(
                                    new ChangeBounds()
                                            .addTarget(mButtonLogin)
                                            .setDuration(duration)
                                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            ).setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
                    transition.addListener(new TransitionListenerAdapter() {
                        @Override
                        public void onTransitionEnd(@NonNull Transition transition) {
                            super.onTransitionEnd(transition);

                            mListener.onLoginSuccessfully();
                        }
                    });

                    TransitionManager.beginDelayedTransition(root_login_frag, transition);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mButtonLogin.setVisibility(View.VISIBLE);
                    final ConstraintLayout.LayoutParams layoutParams2 = (ConstraintLayout.LayoutParams) mButtonLogin.getLayoutParams();
                    layoutParams2.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
                    mButtonLogin.setLayoutParams(layoutParams2);

                })
                .addOnFailureListener(e -> {
                    final TransitionSet transition = new TransitionSet()
                            .addTransition(
                                    new Fade().addTarget(mProgressBar)
                                            .setDuration(duration)
                            )
                            .addTransition(
                                    new Fade()
                                            .addTarget(mButtonLogin)
                                            .setDuration(duration)
                            )
                            .addTransition(
                                    new ChangeBounds()
                                            .addTarget(mButtonLogin)
                                            .setDuration(duration)
                                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            ).setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
                    transition.addListener(new TransitionListenerAdapter() {
                        @Override
                        public void onTransitionEnd(@NonNull Transition transition) {
                            super.onTransitionEnd(transition);

                            if (e instanceof FirebaseAuthException) {
                                final String message = getMessageFromFirebaseAuthExceptionErrorCode(((FirebaseAuthException) e).getErrorCode());
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    TransitionManager.beginDelayedTransition(root_login_frag, transition);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mButtonLogin.setVisibility(View.VISIBLE);
                    final ConstraintLayout.LayoutParams layoutParams2 = (ConstraintLayout.LayoutParams) mButtonLogin.getLayoutParams();
                    layoutParams2.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
                    mButtonLogin.setLayoutParams(layoutParams2);
                });
    }

    interface Listener {
        void onRegisterClick();

        void onLoginSuccessfully();
    }
}
