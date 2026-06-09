package com.smart.pmtv;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import com.facebook.shimmer.ShimmerFrameLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView rvCommunityPosts;
    private ShimmerFrameLayout shimmerViewContainer;
    private ExtendedFloatingActionButton fabAddPost;
    
    private CommunityAdapter adapter;
    private List<CommunityModel> postList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupFab();
        
        fetchPosts();
    }

    private void initViews(View view) {
        rvCommunityPosts = view.findViewById(R.id.rvCommunityPosts);
        shimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        fabAddPost = view.findViewById(R.id.fabAddPost);
        postList = new ArrayList<>();
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    private void setupRecyclerView() {
        adapter = new CommunityAdapter(requireContext(), postList, NavHostFragment.findNavController(this));
        rvCommunityPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCommunityPosts.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddPost.setOnClickListener(v -> {
            UserManager.getInstance().requireLogin(requireContext(), NavHostFragment.findNavController(this), "create a post");
            if (UserManager.getInstance().isAuthenticated()) {
                showCreatePostDialog();
            }
        });
    }

    private void showCreatePostDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_post, null);
        dialog.setContentView(dialogView);

        EditText etContent = dialogView.findViewById(R.id.et_post_content);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_post);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit_post);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            if (content.isEmpty()) {
                etContent.setError("Post cannot be empty");
                return;
            }

            btnSubmit.setEnabled(false);
            java.util.Map<String, Object> post = new java.util.HashMap<>();
            post.put("authorName", "PMTv User");
            post.put("authorRole", "Member");
            post.put("content", content);
            post.put("likes", 0);
            post.put("comments", 0);
            post.put("shares", 0);
            post.put("timeAgo", "Just now");

            db.collection("community_posts")
                    .add(post)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "Post published!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchPosts();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to publish post", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                    });
        });

        dialog.show();
    }

    private void fetchPosts() {
        shimmerViewContainer.startShimmer();
        shimmerViewContainer.setVisibility(View.VISIBLE);
        rvCommunityPosts.setVisibility(View.GONE);
        
        Query query = db.collection("community_posts").orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            rvCommunityPosts.setVisibility(View.VISIBLE);
            
            if (error != null) {
                Log.e("CommunityFragment", "Listen failed.", error);
                Toast.makeText(requireContext(), "Error fetching posts", Toast.LENGTH_SHORT).show();
                return;
            }

            List<CommunityModel> newPosts = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    CommunityModel post = doc.toObject(CommunityModel.class);
                    if (post != null) {
                        newPosts.add(post);
                    }
                }
            }
            adapter.updateList(newPosts);
        });
    }
}
