package com.smart.pmtv.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.smart.pmtv.R;
import com.smart.pmtv.UserManager;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        MaterialButton btnGuest = view.findViewById(R.id.btn_guest);
        MaterialButton btnLogin = view.findViewById(R.id.btn_login);
        MaterialButton btnSignup = view.findViewById(R.id.btn_signup);

        btnLogin.setOnClickListener(v -> navController.navigate(R.id.action_welcome_to_login));
        btnSignup.setOnClickListener(v -> navController.navigate(R.id.action_welcome_to_signup));

        btnGuest.setOnClickListener(v -> {
            UserManager.getInstance().signInAnonymously(new UserManager.OnAuthListener() {
                @Override
                public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                    navController.navigate(R.id.action_welcome_to_home);
                }

                @Override
                public void onFailure(Exception e) {
                    // Show error dialog
                }
            });
        });
    }
}
