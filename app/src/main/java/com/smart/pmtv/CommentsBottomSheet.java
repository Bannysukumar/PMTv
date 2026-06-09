package com.smart.pmtv;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CommentsBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private EditText etCommentInput;
    private ImageView btnSendComment;
    private ImageView btnClose;

    private DatabaseReference commentsRef;
    private ChildEventListener commentsListener;

    public CommentsBottomSheet() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvComments = view.findViewById(R.id.rvComments);
        etCommentInput = view.findViewById(R.id.etCommentInput);
        btnSendComment = view.findViewById(R.id.btnSendComment);
        btnClose = view.findViewById(R.id.btnClose);

        commentAdapter = new CommentAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Like YouTube, start from bottom
        rvComments.setLayoutManager(layoutManager);
        rvComments.setAdapter(commentAdapter);

        btnClose.setOnClickListener(v -> dismiss());

        FirebaseDatabase rtdb;
        try {
            rtdb = FirebaseDatabase.getInstance("https://pmtv5464-default-rtdb.firebaseio.com");
        } catch (Exception e) {
            rtdb = FirebaseDatabase.getInstance();
        }
        
        commentsRef = rtdb.getReference("live_stream/comments");

        btnSendComment.setOnClickListener(v -> postComment());

        listenForComments();
    }

    private void listenForComments() {
        commentsListener = commentsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                CommentModel comment = snapshot.getValue(CommentModel.class);
                if (comment != null) {
                    commentAdapter.addComment(comment);
                    rvComments.scrollToPosition(commentAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void postComment() {
        String text = etCommentInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        UserManager userManager = UserManager.getInstance();
        
        if (!userManager.isAuthenticated()) {
            dismiss();
            try {
                userManager.requireLogin(requireContext(), NavHostFragment.findNavController(requireParentFragment()), "post a comment");
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Please login to comment", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        FirebaseUser user = userManager.getCurrentUser();
        String userId = user.getUid();
        String userName = user.getDisplayName();
        
        if (TextUtils.isEmpty(userName)) {
            userName = user.getEmail();
            if (userName != null && userName.contains("@")) {
                userName = userName.substring(0, userName.indexOf("@"));
            }
        }

        String commentId = commentsRef.push().getKey();
        if (commentId == null) return;

        CommentModel newComment = new CommentModel(
                commentId,
                userId,
                userName,
                text,
                System.currentTimeMillis()
        );

        btnSendComment.setEnabled(false);
        commentsRef.child(commentId).setValue(newComment).addOnCompleteListener(task -> {
            btnSendComment.setEnabled(true);
            if (task.isSuccessful()) {
                etCommentInput.setText("");
            } else {
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (commentsRef != null && commentsListener != null) {
            commentsRef.removeEventListener(commentsListener);
        }
    }
}
