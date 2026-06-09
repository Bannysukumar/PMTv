package com.smart.pmtv;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {

    private ShapeableImageView ivEditProfilePic;
    private FloatingActionButton fabChangePhoto;
    private TextInputEditText etEditName, etEditBio;
    private MaterialButton btnCancelEdit, btnSaveProfile;
    private ProgressBar pbEditProfile;

    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Runnable onProfileUpdatedCallback;

    public void setOnProfileUpdatedCallback(Runnable callback) {
        this.onProfileUpdatedCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this).load(uri).circleCrop().into(ivEditProfilePic);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivEditProfilePic = view.findViewById(R.id.ivEditProfilePic);
        fabChangePhoto = view.findViewById(R.id.fabChangePhoto);
        etEditName = view.findViewById(R.id.etEditName);
        etEditBio = view.findViewById(R.id.etEditBio);
        btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        pbEditProfile = view.findViewById(R.id.pbEditProfile);

        loadCurrentUserData();

        fabChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnCancelEdit.setOnClickListener(v -> dismiss());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            etEditName.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                     .load(user.getPhotoUrl())
                     .circleCrop()
                     .placeholder(android.R.drawable.ic_menu_gallery)
                     .into(ivEditProfilePic);
            }

            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.contains("bio")) {
                            etEditBio.setText(doc.getString("bio"));
                        }
                    });
        }
    }

    private void saveProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String newName = etEditName.getText() != null ? etEditName.getText().toString().trim() : "";
        String newBio = etEditBio.getText() != null ? etEditBio.getText().toString().trim() : "";

        if (newName.isEmpty()) {
            etEditName.setError("Name cannot be empty");
            return;
        }

        setLoading(true);

        if (selectedImageUri != null) {
            uploadImageAndSave(user, newName, newBio);
        } else {
            updateUserData(user, newName, newBio, user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        }
    }

    private void uploadImageAndSave(FirebaseUser user, String newName, String newBio) {
        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("profile_pictures")
                .child(user.getUid() + ".jpg");

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateUserData(user, newName, newBio, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserData(FirebaseUser user, String name, String bio, String photoUrl) {
        UserProfileChangeRequest.Builder requestBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(name);
        
        if (photoUrl != null) {
            requestBuilder.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(requestBuilder.build()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> dbUpdates = new HashMap<>();
                dbUpdates.put("name", name);
                dbUpdates.put("bio", bio);
                if (photoUrl != null) dbUpdates.put("photoUrl", photoUrl);

                FirebaseFirestore.getInstance().collection("users")
                        .document(user.getUid())
                        .set(dbUpdates, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            setLoading(false);
                            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                            if (onProfileUpdatedCallback != null) onProfileUpdatedCallback.run();
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            setLoading(false);
                            Toast.makeText(getContext(), "Failed to update bio", Toast.LENGTH_SHORT).show();
                        });
            } else {
                setLoading(false);
                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        pbEditProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!isLoading);
        btnCancelEdit.setEnabled(!isLoading);
        fabChangePhoto.setEnabled(!isLoading);
    }
}
