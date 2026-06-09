package com.smart.pmtv;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    private View layoutGuestMode;
    private View layoutAuthenticated;

    private MaterialButton btnGuestLogin;
    private MaterialButton btnGuestSignup;

    private ShapeableImageView ivProfilePic;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private MaterialButton btnLogout;
    private MaterialButton btnDeleteAccount;
    private View btnSettings;
    private View optEditProfile;

    private UserManager userManager;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userManager = UserManager.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupListeners();
        updateUI();
    }

    private void initViews(View view) {
        layoutGuestMode = view.findViewById(R.id.layout_guest_mode);
        layoutAuthenticated = view.findViewById(R.id.layout_authenticated);

        btnGuestLogin = view.findViewById(R.id.btn_guest_login);
        btnGuestSignup = view.findViewById(R.id.btn_guest_signup);

        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnSettings = view.findViewById(R.id.btn_settings);
        optEditProfile = view.findViewById(R.id.opt_edit_profile);
    }

    private void setupListeners() {
        btnGuestLogin.setOnClickListener(v -> 
                NavHostFragment.findNavController(this).navigate(R.id.nav_login));
        
        btnGuestSignup.setOnClickListener(v -> 
                NavHostFragment.findNavController(this).navigate(R.id.nav_signup));
        
        btnSettings.setOnClickListener(v -> {
            SettingsBottomSheet bottomSheet = new SettingsBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "SettingsBottomSheet");
        });

        if (optEditProfile != null) {
            optEditProfile.setOnClickListener(v -> {
                if (userManager.isAuthenticated()) {
                    EditProfileBottomSheet editSheet = new EditProfileBottomSheet();
                    editSheet.setOnProfileUpdatedCallback(this::updateUI);
                    editSheet.show(getParentFragmentManager(), "EditProfileBottomSheet");
                } else {
                    userManager.requireLogin(requireContext(), NavHostFragment.findNavController(this), "edit your profile");
                }
            });
        }

        btnLogout.setOnClickListener(v -> {
            userManager.logout();
            // Automatically sign in as anonymous guest after logout
            userManager.signInAnonymously(new UserManager.OnAuthListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    updateUI();
                }

                @Override
                public void onFailure(Exception e) {
                    updateUI();
                }
            });
        });

        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }
    }

    private void showDeleteAccountDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> {
                userManager.deleteAccount(new UserManager.OnAuthListener() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        userManager.signInAnonymously(new UserManager.OnAuthListener() {
                            @Override
                            public void onSuccess(FirebaseUser u) { updateUI(); }
                            @Override
                            public void onFailure(Exception e) { updateUI(); }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to delete account. You may need to log in again.", Toast.LENGTH_LONG).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void updateUI() {
        if (userManager.isAuthenticated()) {
            layoutGuestMode.setVisibility(View.GONE);
            layoutAuthenticated.setVisibility(View.VISIBLE);
            
            FirebaseUser user = userManager.getCurrentUser();
            tvUserEmail.setText(user.getEmail());
            
            fetchUserData(user.getUid());
        } else {
            layoutGuestMode.setVisibility(View.VISIBLE);
            layoutAuthenticated.setVisibility(View.GONE);
        }
    }
    
    private void fetchUserData(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String name = document.getString("name");
                    if (name != null) tvUserName.setText(name);
                    
                    String photoUrl = document.getString("photoUrl");
                    if (photoUrl != null && !photoUrl.isEmpty() && isAdded()) {
                        Glide.with(this)
                             .load(photoUrl)
                             .circleCrop()
                             .placeholder(android.R.drawable.ic_menu_gallery)
                             .into(ivProfilePic);
                    }
                } else {
                    Log.d("ProfileFragment", "No such user document");
                }
            } else {
                Log.d("ProfileFragment", "get failed with ", task.getException());
            }
        });
    }
}
