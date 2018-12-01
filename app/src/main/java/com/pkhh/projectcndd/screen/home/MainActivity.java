package com.pkhh.projectcndd.screen.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.User;
import com.pkhh.projectcndd.screen.loginregister.LoginRegisterActivity;
import com.pkhh.projectcndd.screen.nearby.NearbyActivity;
import com.pkhh.projectcndd.screen.post.PostActivity;
import com.pkhh.projectcndd.screen.posted.PostedRoomsActivity;
import com.pkhh.projectcndd.screen.profile.UserProfileActivity;
import com.pkhh.projectcndd.screen.saved.SavedRoomsActivity;
import com.pkhh.projectcndd.utils.Language;
import com.pkhh.projectcndd.utils.LanguageUtil;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import timber.log.Timber;

import static com.pkhh.projectcndd.utils.Constants.EXTRA_USER_FULL_NAME;
import static com.pkhh.projectcndd.utils.Constants.EXTRA_USER_ID;
import static com.pkhh.projectcndd.utils.Constants.USERS_NAME_COLLECION;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, FirebaseAuth.AuthStateListener {
  public static final int REQUEST_CODE_POST = 1;
  public static final int REQUEST_CODE_LOGIN_SAVED = 2;

  private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

  private DrawerLayout drawerLayout;
  private TextView textName;
  private TextView textEmail;
  private ImageView imageAvatar;
  private NavigationView navigationView;

  @Nullable private User user;
  @Nullable private ListenerRegistration listenerRegistration;
  @Nullable private AlertDialog changeLanguageDialog;

  public static void printHashKey(Context pContext) {
    try {
      @SuppressLint("PackageManagerGetSignatures") PackageInfo info = pContext.getPackageManager().getPackageInfo("com.pkhh.projectcndd", PackageManager.GET_SIGNATURES);
      for (Signature signature : info.signatures) {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(signature.toByteArray());
        String hashKey = new String(Base64.encode(md.digest(), 0));
        Timber.tag("##printHashKey").i("printHashKey() Hash Key: %s", hashKey);
      }
    } catch (NoSuchAlgorithmException e) {
      Timber.tag("##printHashKey").e(e, "printHashKey()");
    } catch (Exception e) {
      Timber.tag("##printHashKey").e(e, "printHashKey()");
    }
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LanguageUtil.loadLocale(this);
    setContentView(R.layout.activity_main);

    // setup toolbar
    setSupportActionBar(findViewById(R.id.toolbar));
    getSupportActionBar().setTitle(R.string.app_name);


    findViewById(R.id.fab).setOnClickListener(__ -> startActivity(new Intent(this, NearbyActivity.class)));

    drawerLayout = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this,
        drawerLayout,
        findViewById(R.id.toolbar),
        R.string.navigation_drawer_open,
        R.string.navigation_drawer_close
    );
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    setupNavigationView();

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
              R.anim.slide_in_left, R.anim.slide_out_right)
          .add(R.id.main_content, new MotelRoomsListFragment(), MotelRoomsListFragment.TAG)
          .commit();
    }

    printHashKey(this);
  }

  private void setupNavigationView() {
    navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    final View headerView = navigationView.getHeaderView(0);
    textName = headerView.findViewById(R.id.text_name);
    textEmail = headerView.findViewById(R.id.text_email);
    imageAvatar = headerView.findViewById(R.id.image_avatar);

    headerView.findViewById(R.id.nav_header).setOnClickListener(__ -> {
      if (firebaseAuth.getCurrentUser() != null) {
        final Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(EXTRA_USER_ID, user != null ? user.getId() : null);
        intent.putExtra(EXTRA_USER_FULL_NAME, user != null ? user.getFullName() : null);
        startActivity(intent);
      } else {
        Toast.makeText(this, R.string.must_login, Toast.LENGTH_SHORT).show();
      }
    });

    navigationView.getMenu().findItem(R.id.nav_home).setCheckable(true);
    navigationView.getMenu().findItem(R.id.nav_post).setCheckable(false);
    navigationView.getMenu().findItem(R.id.nav_saved).setCheckable(false);
    navigationView.getMenu().findItem(R.id.nav_login).setCheckable(false);
    navigationView.getMenu().findItem(R.id.nav_posted_room).setCheckable(false);

    textName.setText(getString(R.string.loading));
    textEmail.setText(getString(R.string.loading));
  }

  @Override
  protected void onStart() {
    super.onStart();
    firebaseAuth.addAuthStateListener(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    firebaseAuth.removeAuthStateListener(this);
    if (changeLanguageDialog != null) {
      changeLanguageDialog.dismiss();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (listenerRegistration != null) {
      listenerRegistration.remove();
    }
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_change_language) {
      showChangeLanguageDialog();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void showChangeLanguageDialog() {
    final List<Language> languages = LanguageUtil.getAllLanguages(this);
    final ArrayAdapter<Language> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages);

    changeLanguageDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.change_language_title)
        .setSingleChoiceItems(
            adapter,
            languages.indexOf(LanguageUtil.getCurrentLanguage(this)),
            (dialog, position) -> {
              dialog.dismiss();

              LanguageUtil.changeLanguage(this, languages.get(position));
              final Intent intent = getIntent();
              finish();
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
            }
        )
        .setNegativeButton(getString(R.string.cancel), (dialog, __) -> dialog.dismiss())
        .show();

  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.

    switch (item.getItemId()) {
      case R.id.nav_home:
        break;
      case R.id.nav_post:
        onClickNavPost();
        break;
      case R.id.nav_saved:
        onClickSaved();
        break;
      case R.id.nav_login:
        onClickNavLogin();
        break;
      case R.id.nav_posted_room:
        onClickNavPostedRoom();
        break;
    }

    drawerLayout.closeDrawer(GravityCompat.START);
    return true;
  }

  private void onClickNavPostedRoom() {
    startActivity(new Intent(this, PostedRoomsActivity.class));
  }

  private void onClickSaved() {

    if (firebaseAuth.getCurrentUser() == null) {
      new AlertDialog.Builder(this)
          .setTitle(getString(R.string.require_login))
          .setIcon(R.drawable.ic_exit_to_app_black_24dp)
          .setMessage(getString(R.string.require_login_to_see_saved_room))
          .setNegativeButton(getString(R.string.cancel), (dialog, __) -> dialog.dismiss())
          .setPositiveButton(getString(R.string.ok), (dialog, __) -> {
            dialog.dismiss();

            final Intent intent = new Intent(this, LoginRegisterActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN_SAVED);
          })
          .show();
    } else {
      startActivity(new Intent(this, SavedRoomsActivity.class));
    }
  }

  private void onClickNavLogin() {
    if (firebaseAuth.getCurrentUser() == null) {
      startActivity(new Intent(this, LoginRegisterActivity.class));
    } else {
      new AlertDialog.Builder(this)
          .setTitle(getString(R.string.logout))
          .setIcon(R.drawable.ic_exit_to_app_black_24dp)
          .setMessage(getString(R.string.sure_logout))
          .setNegativeButton(getString(R.string.no), (dialog, __) -> dialog.dismiss())
          .setPositiveButton(getString(R.string.yes), (dialog, __) -> {
            dialog.dismiss();

            final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            for (UserInfo userInfo : currentUser.getProviderData()) {
              if ("facebook.com".equals(userInfo.getProviderId())) {
                LoginManager.getInstance().logOut();
              }
            }

            firebaseAuth.signOut();
          })
          .show();
    }
  }

  private void onClickNavPost() {
    if (firebaseAuth.getCurrentUser() == null) {

      new AlertDialog.Builder(this)
          .setTitle(getString(R.string.require_login))
          .setIcon(R.drawable.ic_exit_to_app_black_24dp)
          .setMessage(getString(R.string.require_login_description))
          .setNegativeButton(getString(R.string.cancel), (dialog, __) -> dialog.dismiss())
          .setPositiveButton(getString(R.string.ok), (dialog, __) -> {
            dialog.dismiss();

            final Intent intent = new Intent(this, LoginRegisterActivity.class);
            startActivityForResult(intent, REQUEST_CODE_POST);
          })
          .show();
    } else {
      startActivity(new Intent(this, PostActivity.class));
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_CODE_LOGIN_SAVED:
        if (resultCode == RESULT_OK) {
          startActivity(new Intent(this, SavedRoomsActivity.class));
        }
        break;
      case REQUEST_CODE_POST:
        if (resultCode == RESULT_OK) {
          startActivity(new Intent(this, PostActivity.class));
        }
        break;
    }
  }

  @Override
  public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
    final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    final MenuItem loginOrLogoutMenuItem = navigationView.getMenu().findItem(R.id.nav_login);

    if (currentUser == null) {
      Picasso.get()
          .load(R.drawable.avatar_default_icon)
          .fit()
          .centerCrop()
          .noFade()
          .into(imageAvatar);
      textName.setText(getString(R.string.not_sign_in));
      textEmail.setText(getString(R.string.not_sign_in));

      loginOrLogoutMenuItem.setTitle(getString(R.string.login));
      loginOrLogoutMenuItem.setIcon(R.drawable.ic_person_add_black_24dp);

      user = null;
      if (listenerRegistration != null) {
        listenerRegistration.remove();
      }
    } else {
      listenerRegistration = firebaseFirestore.document(USERS_NAME_COLLECION + "/" + currentUser.getUid())
          .addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null) {
              user = FirebaseModel.documentSnapshotToObject(documentSnapshot, User.class);
              textName.setText(user.getFullName());
              textEmail.setText(user.getEmail());

              if (!TextUtils.isEmpty(user.getAvatar())) {
                Picasso
                    .get()
                    .load(user.getAvatar())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .noFade()
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.avatar_default_icon)
                    .error(R.drawable.avatar_default_icon)
                    .into(imageAvatar);
              }
            }
          });
      loginOrLogoutMenuItem.setTitle(getString(R.string.logout));
      loginOrLogoutMenuItem.setIcon(R.drawable.ic_exit_to_app_black_24dp);
    }
  }
}
