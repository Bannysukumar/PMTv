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

public class ForgotPasswordFragment extends Fragment {

    public ForgotPasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);
        MaterialButton btnSendReset = view.findViewById(R.id.btn_send_reset);

        btnBack.setOnClickListener(v -> navController.navigateUp());

        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendReset.setEnabled(false);
            UserManager.getInstance().getAuth().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (isAdded()) {
                            btnSendReset.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Reset link sent to " + email, Toast.LENGTH_LONG).show();
                                // Navigate to OTP screen for premium UI flow (even though Firebase handles via email)
                                navController.navigate(R.id.action_forgot_password_to_otp);
                            } else {
                                Toast.makeText(requireContext(), "Failed to send reset link: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
