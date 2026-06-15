package com.smart.pmtv;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {

    private PreferencesManager prefs;
    private BottomNavigationView bottomNavigationView;
    private NavigationRailView navigationRailView;
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        
        prefs = new PreferencesManager(this);
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationRailView = findViewById(R.id.navigation_rail);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            if (bottomNavigationView != null) {
                NavigationUI.setupWithNavController(bottomNavigationView, navController);
            } else if (navigationRailView != null) {
                NavigationUI.setupWithNavController(navigationRailView, navController);
            }

            // Determine Start Destination
            if (!UserManager.getInstance().isAuthenticated() && UserManager.getInstance().getCurrentUser() == null) {
                navController.navigate(R.id.nav_welcome);
            }

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean isAuthFlow = id == R.id.nav_welcome || id == R.id.nav_login || id == R.id.nav_signup ||
                    id == R.id.nav_forgot_password || id == R.id.nav_reset_password || id == R.id.nav_complete_profile;
                    
                if (bottomNavigationView != null) {
                    bottomNavigationView.setVisibility(isAuthFlow ? View.GONE : View.VISIBLE);
                }
                if (navigationRailView != null) {
                    navigationRailView.setVisibility(isAuthFlow ? View.GONE : View.VISIBLE);
                }
            });
        }
        
        setupBanListener();
    }

    private void setupBanListener() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userListener = FirebaseFirestore.getInstance().collection("users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    if (snapshot != null && snapshot.exists()) {
                        Boolean isBanned = snapshot.getBoolean("isBanned");
                        if (isBanned != null && isBanned) {
                            Toast.makeText(this, "Your account has been banned by an administrator.", Toast.LENGTH_LONG).show();
                            UserManager.getInstance().logout();
                            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                            if (navHostFragment != null) {
                                navHostFragment.getNavController().navigate(R.id.nav_welcome);
                            }
                        }
                    }
                });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
        }
    }

    public void setNavigationVisible(boolean visible) {
        if (navigationRailView != null) {
            navigationRailView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void enterPiP() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9))
                    .build();
            enterPictureInPictureMode(params);
        } else {
            Toast.makeText(this, "PiP not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
        }
        if (navigationRailView != null) {
            navigationRailView.setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
        }
    }

    private void applyTheme() {
        switch (prefs.getTheme()) {
            case PreferencesManager.THEME_LIGHT:
                setTheme(R.style.Theme_PMTv_Light);
                break;
            case PreferencesManager.THEME_BLUE:
                setTheme(R.style.Theme_PMTv_Blue);
                break;
            case PreferencesManager.THEME_AMOLED:
                setTheme(R.style.Theme_PMTv_AMOLED);
                break;
            case PreferencesManager.THEME_STUDIO:
                setTheme(R.style.Theme_PMTv_Studio);
                break;
            case PreferencesManager.THEME_DARK:
            default:
                setTheme(R.style.Theme_PMTv_Dark);
                break;
        }
    }
}