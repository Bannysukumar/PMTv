package com.smart.pmtv;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.NavController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserManager {

    private static UserManager instance;
    private final FirebaseAuth auth;

    private UserManager() {
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isAuthenticated() {
        FirebaseUser user = getCurrentUser();
        // A user is truly authenticated if they are logged in and NOT anonymous
        return user != null && !user.isAnonymous();
    }

    public void requireLogin(Context context, NavController navController, String featureName) {
        if (isAuthenticated()) return;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_login_required);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMessage = dialog.findViewById(R.id.tvLoginMessage);
        tvMessage.setText("You need to be logged in to " + featureName + ".");

        Button btnLogin = dialog.findViewById(R.id.btnDialogLogin);
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);

        btnLogin.setOnClickListener(v -> {
            dialog.dismiss();
            navController.navigate(R.id.nav_profile);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void signInAnonymously(OnAuthListener listener) {
        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (listener != null) listener.onSuccess(auth.getCurrentUser());
            } else {
                if (listener != null) listener.onFailure(task.getException());
            }
        });
    }

    public void logout() {
        auth.signOut();
    }

    public void deleteAccount(OnAuthListener listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (listener != null) listener.onSuccess(null);
                } else {
                    if (listener != null) listener.onFailure(task.getException());
                }
            });
        } else {
            if (listener != null) listener.onFailure(new Exception("No user logged in"));
        }
    }

    public interface OnAuthListener {
        void onSuccess(FirebaseUser user);
        void onFailure(Exception e);
    }
}
