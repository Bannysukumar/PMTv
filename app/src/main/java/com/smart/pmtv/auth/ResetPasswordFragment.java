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

public class ResetPasswordFragment extends Fragment {

    // Note: To use this fragment, you need to catch the oobCode from the Firebase dynamic link
    private String oobCode = null;

    public ResetPasswordFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // Try to get oobCode from arguments if passed via deep link
        if (getArguments() != null) {
            oobCode = getArguments().getString("oobCode");
        }

        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextInputEditText etNewPassword = view.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        MaterialButton btnResetPassword = view.findViewById(R.id.btn_reset_password);

        btnBack.setOnClickListener(v -> navController.navigateUp());

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (oobCode == null) {
                Toast.makeText(requireContext(), "Invalid reset link (oobCode missing).", Toast.LENGTH_SHORT).show();
                return;
            }

            btnResetPassword.setEnabled(false);
            UserManager.getInstance().getAuth().confirmPasswordReset(oobCode, newPassword)
                    .addOnCompleteListener(task -> {
                        if (isAdded()) {
                            btnResetPassword.setEnabled(true);
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Password reset successfully!", Toast.LENGTH_SHORT).show();
                                navController.navigate(R.id.action_reset_password_to_login);
                            } else {
                                Toast.makeText(requireContext(), "Failed to reset password: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
