package com.smart.pmtv.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.smart.pmtv.R;
import com.smart.pmtv.UserManager;

public class SignupFragment extends Fragment {

    public SignupFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);
        TextInputEditText etPassword = view.findViewById(R.id.et_password);
        TextInputEditText etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        MaterialCheckBox cbTerms = view.findViewById(R.id.cb_terms);
        MaterialButton btnSignup = view.findViewById(R.id.btn_signup);
        TextView tvLogin = view.findViewById(R.id.tv_login);

        btnBack.setOnClickListener(v -> navController.navigateUp());

        tvLogin.setOnClickListener(v -> navController.navigate(R.id.action_signup_to_login));

        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbTerms.isChecked()) {
                Toast.makeText(requireContext(), "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSignup.setEnabled(false);
            UserManager.getInstance().getAuth().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (isAdded()) {
                            btnSignup.setEnabled(true);
                            if (task.isSuccessful()) {
                                navController.navigate(R.id.action_signup_to_complete_profile);
                            } else {
                                Toast.makeText(requireContext(), "Signup failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
