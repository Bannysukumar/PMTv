package com.smart.pmtv.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smart.pmtv.R;

public class OtpVerificationFragment extends Fragment {

    public OtpVerificationFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        TextInputEditText etOtp = view.findViewById(R.id.etOtp);
        MaterialButton btnVerify = view.findViewById(R.id.btnVerify);

        btnBack.setOnClickListener(v -> navController.navigateUp());

        btnVerify.setOnClickListener(v -> {
            String otp = etOtp.getText() != null ? etOtp.getText().toString().trim() : "";

            if (TextUtils.isEmpty(otp) || otp.length() < 4) {
                Toast.makeText(requireContext(), "Please enter a valid 4-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // Dummy validation success - navigate to reset password
            navController.navigate(R.id.action_otp_to_reset_password);
        });
    }
}
