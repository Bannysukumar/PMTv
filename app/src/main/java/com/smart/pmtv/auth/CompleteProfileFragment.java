package com.smart.pmtv.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smart.pmtv.R;
import com.smart.pmtv.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class CompleteProfileFragment extends Fragment {

    public CompleteProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_complete_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextInputEditText etDisplayName = view.findViewById(R.id.et_display_name);
        TextInputEditText etBio = view.findViewById(R.id.et_bio);
        TextInputEditText etLocation = view.findViewById(R.id.et_location);
        MaterialButton btnFinishSetup = view.findViewById(R.id.btn_finish_setup);

        btnBack.setOnClickListener(v -> navController.navigateUp());

        btnFinishSetup.setOnClickListener(v -> {
            String displayName = etDisplayName.getText() != null ? etDisplayName.getText().toString().trim() : "";
            String bio = etBio.getText() != null ? etBio.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

            if (TextUtils.isEmpty(displayName)) {
                Toast.makeText(requireContext(), "Display Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = UserManager.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "User not found. Please log in.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnFinishSetup.setEnabled(false);
            
            // Save to Firestore
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("uid", user.getUid());
            profileData.put("email", user.getEmail());
            profileData.put("displayName", displayName);
            profileData.put("bio", bio);
            profileData.put("location", location);
            profileData.put("memberSince", System.currentTimeMillis());

            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .set(profileData)
                    .addOnCompleteListener(task -> {
                        if (isAdded()) {
                            btnFinishSetup.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Profile setup complete!", Toast.LENGTH_SHORT).show();
                                navController.navigate(R.id.action_complete_profile_to_home);
                            } else {
                                Toast.makeText(requireContext(), "Failed to save profile: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
