package com.smart.pmtv;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private Context context;
    private List<CommunityModel> postList;
    private NavController navController;

    public CommunityAdapter(Context context, List<CommunityModel> postList, NavController navController) {
        this.context = context;
        this.postList = postList;
        this.navController = navController;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_community_post, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        CommunityModel post = postList.get(position);

        holder.tvPostAuthorName.setText(post.getAuthorName());
        holder.tvPostContent.setText(post.getContent());

        if (post.getTimestamp() != null) {
            long timeMs = post.getTimestamp().toDate().getTime();
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(timeMs, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            holder.tvPostTimestamp.setText(timeAgo);
        }

        holder.btnLike.setText(String.valueOf(post.getLikesCount()));
        holder.btnDislike.setText(String.valueOf(post.getDislikesCount()));
        holder.btnComment.setText(post.getCommentsCount() + " Comments");

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.ivPostImage.setVisibility(View.VISIBLE);
            // TODO: Load image with Glide
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }

        View.OnClickListener authRequiredListener = v -> {
            UserManager.getInstance().requireLogin(context, navController, "interact with this post");
            if (UserManager.getInstance().isAuthenticated()) {
                Toast.makeText(context, "Action performed (To be linked with Firestore)", Toast.LENGTH_SHORT).show();
            }
        };

        holder.btnLike.setOnClickListener(authRequiredListener);
        holder.btnDislike.setOnClickListener(authRequiredListener);
        holder.btnComment.setOnClickListener(authRequiredListener);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updateList(List<CommunityModel> newList) {
        postList.clear();
        postList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPostAvatar, ivPostImage;
        TextView tvPostAuthorName, tvPostTimestamp, tvPostContent;
        Button btnLike, btnDislike, btnComment;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostAvatar = itemView.findViewById(R.id.ivPostAvatar);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvPostAuthorName = itemView.findViewById(R.id.tvPostAuthorName);
            tvPostTimestamp = itemView.findViewById(R.id.tvPostTimestamp);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            btnComment = itemView.findViewById(R.id.btnComment);
        }
    }
}
