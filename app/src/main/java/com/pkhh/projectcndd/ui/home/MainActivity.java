package com.pkhh.projectcndd.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.User;
import com.pkhh.projectcndd.ui.loginregister.LoginRegisterActivity;
import com.pkhh.projectcndd.ui.post.PostActivity;
import com.pkhh.projectcndd.ui.saved.SavedRoomsActivity;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static com.pkhh.projectcndd.utils.Constants.USER_NAME_COLLECION;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        findViewById(R.id.fab).setOnClickListener(view -> {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

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
    }

    private void setupNavigationView() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final View headerView = navigationView.getHeaderView(0);
        textName = headerView.findViewById(R.id.text_name);
        textEmail = headerView.findViewById(R.id.text_email);
        imageAvatar = headerView.findViewById(R.id.image_avatar);

        navigationView.getMenu().findItem(R.id.nav_home).setCheckable(true);
        navigationView.getMenu().findItem(R.id.nav_post).setCheckable(false);
        navigationView.getMenu().findItem(R.id.nav_saved).setCheckable(false);
        navigationView.getMenu().findItem(R.id.nav_login).setCheckable(false);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onClickSaved() {

        if (firebaseAuth.getCurrentUser() == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Yêu cầu đăng nhập")
                    .setIcon(R.drawable.ic_exit_to_app_black_24dp)
                    .setMessage("Để có thể xem các nhà trọ đã lưu, yêu cầu bạn phải đăng nhập!")
                    .setNegativeButton("Hủy", (dialog, __) -> dialog.dismiss())
                    .setPositiveButton("Ok", (dialog, __) -> {
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
                    .setTitle("Đăng xuất")
                    .setIcon(R.drawable.ic_exit_to_app_black_24dp)
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setNegativeButton("Không", (dialog, __) -> dialog.dismiss())
                    .setPositiveButton("Có", (dialog, __) -> {
                        dialog.dismiss();
                        firebaseAuth.signOut();
                    })
                    .show();
        }
    }

    private void onClickNavPost() {
        if (firebaseAuth.getCurrentUser() == null) {

            new AlertDialog.Builder(this)
                    .setTitle("Yêu cầu đăng nhập")
                    .setIcon(R.drawable.ic_exit_to_app_black_24dp)
                    .setMessage("Để hạn chế spam, lừa đảo, chức năng này yêu cầu bạn phải đăng nhập!")
                    .setNegativeButton("Hủy", (dialog, __) -> dialog.dismiss())
                    .setPositiveButton("Ok", (dialog, __) -> {
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
            textName.setText("Chưa đăng nhập");
            textEmail.setText("Chưa đăng nhập");

            loginOrLogoutMenuItem.setTitle("Login");
            loginOrLogoutMenuItem.setIcon(R.drawable.ic_person_add_black_24dp);
        } else {
            firebaseFirestore.document(USER_NAME_COLLECION + "/" + currentUser.getUid())
                    .addSnapshotListener(this, (documentSnapshot, e) -> {
                        if (documentSnapshot != null) {
                            final User user = FirebaseModel.documentSnapshotToObject(documentSnapshot, User.class);
                            textName.setText(user.getFullName());
                            textEmail.setText(user.getEmail());

                            final String avatar = user.getAvatar();
                            if (avatar != null && !avatar.isEmpty()) {
                                Picasso.get()
                                        .load(avatar)
                                        .fit()
                                        .centerCrop()
                                        .noFade()
                                        .into(imageAvatar);
                            }
                        }
                    });
            loginOrLogoutMenuItem.setTitle("Logout");
            loginOrLogoutMenuItem.setIcon(R.drawable.ic_exit_to_app_black_24dp);
        }
    }
}
