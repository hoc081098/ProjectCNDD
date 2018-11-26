package com.pkhh.projectcndd.screen.loginregister;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.utils.Constants;

import java.util.HashMap;
import java.util.Map;

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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.pkhh.projectcndd.utils.FirebaseUtil.getMessageFromFirebaseAuthExceptionErrorCode;
import static java.util.Objects.requireNonNull;

public class LoginFragment extends Fragment {
  private static final int ANIM_DURATION = 300;

  @BindView(R.id.edit_email) TextInputLayout mEditEmail;
  @BindView(R.id.edit_password) TextInputLayout mEditPassword;
  @BindView(R.id.button_login) Button mButtonLogin;
  @BindView(R.id.progress_bar) ProgressBar mProgressBarLogin;
  @BindView(R.id.root_login_frag) ViewGroup mRootLayout;
  @BindView(R.id.fb_login_button) LoginButton fbLoginButton;
  @BindView(R.id.progress_bar_fb) ProgressBar mProgressBarFb;

  private Listener mListener;
  private FirebaseAuth mFirebaseAuth;
  private FirebaseFirestore mFirestore;

  private final CallbackManager callbackManager = CallbackManager.Factory.create();
  private Unbinder unbinder;

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
    unbinder = ButterKnife.bind(this, view);

    initView();

    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirestore = FirebaseFirestore.getInstance();

    loginFacebook();
  }

  private void initView() {
    requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Đăng nhập");

    requireNonNull(mEditEmail.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String email = s.toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          mEditEmail.setError(getString(R.string.invalid_email_address));
        } else {
          mEditEmail.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
    requireNonNull(mEditPassword.getEditText()).addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String password = s.toString();
        if (password.length() < 6) {
          mEditPassword.setError(getString(R.string.min_length_password_is_6));
        } else {
          mEditPassword.setError(null);
        }
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    requireNonNull(mEditEmail.getEditText()).setText("");
    requireNonNull(mEditPassword.getEditText()).setText("");
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  private void loginFacebook() {
    fbLoginButton.setReadPermissions("email", "public_profile");
    // If using in a fragment
    fbLoginButton.setFragment(this);
    // Callback registration
    fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onSuccess(LoginResult loginResult) {
        // App code
        handleFacebookAccessToken(loginResult.getAccessToken());
      }

      @Override
      public void onCancel() {
        // App code
        Toast.makeText(requireContext(), "Facebook login cancelled", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onError(FacebookException exception) {
        // App code
        Toast.makeText(requireContext(), "Facebook login error: " + exception.toString(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void handleFacebookAccessToken(AccessToken accessToken) {
    final AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
    beginTransition(fbLoginButton, mProgressBarFb);

    mFirebaseAuth.signInWithCredential(credential)
        .addOnSuccessListener(requireActivity(), authResult -> {
          final FirebaseUser firebaseUser = authResult.getUser();
          final Map<String, Object> userMap = new HashMap<>();
          if (firebaseUser.getEmail() != null) {
            userMap.put("email", firebaseUser.getEmail());
          }
          if (firebaseUser.getDisplayName() != null) {
            userMap.put("full_name", firebaseUser.getDisplayName());
          }
          userMap.put("is_active", true);
          if (firebaseUser.getPhoneNumber() != null) {
            userMap.put("phone", firebaseUser.getPhoneNumber());
          }
          if (firebaseUser.getPhotoUrl() != null) {
            userMap.put("avatar", firebaseUser.getPhotoUrl().toString());
          }

          mFirestore.document(Constants.USERS_NAME_COLLECION + "/" + firebaseUser.getUid())
              .get()
              .addOnSuccessListener(requireActivity(), documentSnapshot -> {
                final Task<Void> task;

                if (!documentSnapshot.exists()) {
                  userMap.put("created_at", FieldValue.serverTimestamp());
                  task = mFirestore
                      .document(Constants.USERS_NAME_COLLECION + "/" + firebaseUser.getUid())
                      .set(userMap);
                } else {
                  userMap.put("updated_at", FieldValue.serverTimestamp());
                  task = mFirestore
                      .document(Constants.USERS_NAME_COLLECION + "/" + firebaseUser.getUid())
                      .update(userMap);
                }

                task
                    .addOnSuccessListener(requireActivity(),
                        __ -> LoginFragment.this.onComplete(new TransitionListenerAdapter() {
                          @Override
                          public void onTransitionEnd(@NonNull Transition transition) {
                            mListener.onLoginSuccessfully();
                          }
                        }, fbLoginButton, mProgressBarFb))
                    .addOnFailureListener(requireActivity(), e -> Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
              });
        })
        .addOnFailureListener(requireActivity(), e -> Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show());
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    callbackManager.onActivityResult(requestCode, resultCode, data);
    super.onActivityResult(requestCode, resultCode, data);
  }

  @OnClick({
      R.id.button_login,
      R.id.button_register
  })
  public void onClick(@NonNull View v) {
    final int id = v.getId();
    if (id == R.id.button_login) {
      onLoginWithEmail();
    } else if (id == R.id.button_register) {
      mListener.onRegisterClick();
    }
  }

  private void onLoginWithEmail() {
    boolean isValid = true;

    final String email = requireNonNull(mEditEmail.getEditText()).getText().toString();
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      isValid = false;
    }

    final String password = requireNonNull(mEditPassword.getEditText()).getText().toString();
    if (password.length() < 6) {
      isValid = false;
    }

    if (!isValid) {
      return;
    }

    beginTransition(mButtonLogin, mProgressBarLogin);

    mFirebaseAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(requireActivity(), task -> {
          if (task.isSuccessful()) {
            LoginFragment.this.onComplete(new TransitionListenerAdapter() {
              @Override
              public void onTransitionEnd(@NonNull Transition transition) {
                mListener.onLoginSuccessfully();
              }
            }, mButtonLogin, mProgressBarLogin);
          } else {
            LoginFragment.this.onComplete(new TransitionListenerAdapter() {
              @Override
              public void onTransitionEnd(@NonNull Transition transition) {
                Exception e = task.getException();
                if (e instanceof FirebaseAuthException) {
                  final String message = getMessageFromFirebaseAuthExceptionErrorCode(((FirebaseAuthException) e).getErrorCode());
                  Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                  Toast.makeText(requireContext(), e != null ? e.getMessage() : "", Toast.LENGTH_SHORT).show();
                }
              }
            }, mButtonLogin, mProgressBarLogin);
          }
        });
  }

  private void beginTransition(Button button, ProgressBar progressBar) {
    TransitionManager.beginDelayedTransition(mRootLayout, new TransitionSet()
        .addTransition(
            new ChangeBounds()
                .addTarget(button)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
        )
        .addTransition(
            new Fade()
                .addTarget(button)
                .setDuration(ANIM_DURATION)
        )
        .addTransition(
            new Fade().addTarget(progressBar)
                .setDuration(ANIM_DURATION)
        )
        .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
    );

    final ConstraintLayout.LayoutParams lp1 = (ConstraintLayout.LayoutParams) button.getLayoutParams();
    lp1.width = lp1.height;
    button.setLayoutParams(lp1);
    button.setVisibility(View.INVISIBLE);
    progressBar.setVisibility(View.VISIBLE);
  }

  private void onComplete(Transition.TransitionListener listener, Button button, ProgressBar progressBar) {
    final TransitionSet transition = new TransitionSet()
        .addTransition(
            new Fade().addTarget(progressBar)
                .setDuration(ANIM_DURATION)
        )
        .addTransition(
            new Fade()
                .addTarget(button)
                .setDuration(ANIM_DURATION)
        )
        .addTransition(
            new ChangeBounds()
                .addTarget(button)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
        ).setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
    transition.addListener(listener);

    TransitionManager.beginDelayedTransition(mRootLayout, transition);

    progressBar.setVisibility(View.INVISIBLE);
    button.setVisibility(View.VISIBLE);
    final ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) button.getLayoutParams();
    params.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
    button.setLayoutParams(params);
  }

  interface Listener {
    void onRegisterClick();

    void onLoginSuccessfully();
  }
}
