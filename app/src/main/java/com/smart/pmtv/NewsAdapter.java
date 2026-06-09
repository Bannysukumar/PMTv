package com.smart.pmtv;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsModel> newsList;
    private NavController navController;

    public NewsAdapter(Context context, List<NewsModel> newsList, NavController navController) {
        this.context = context;
        this.newsList = newsList;
        this.navController = navController;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsModel news = newsList.get(position);

        holder.tvNewsTitle.setText(news.getTitle());
        holder.tvCategoryBadge.setText(news.getCategory());

        if (news.getTimestamp() != null) {
            long timeMs = news.getTimestamp().toDate().getTime();
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(timeMs, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            holder.tvNewsTimestamp.setText(timeAgo + " • By " + news.getAuthor());
        }

        // TODO: Load Image using Glide (Currently using placeholder)
        
        holder.btnBookmark.setOnClickListener(v -> {
            UserManager.getInstance().requireLogin(context, navController, "bookmark news");
            if (UserManager.getInstance().isAuthenticated()) {
                Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnShare.setOnClickListener(v -> {
            // Share intent
            Toast.makeText(context, "Sharing: " + news.getTitle(), Toast.LENGTH_SHORT).show();
        });

        holder.itemView.setOnClickListener(v -> {
            // Open News Details
            Toast.makeText(context, "Opening News...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void updateList(List<NewsModel> newList) {
        newsList.clear();
        newsList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNewsImage;
        TextView tvCategoryBadge, tvNewsTitle, tvNewsTimestamp;
        ImageButton btnBookmark, btnShare;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNewsImage = itemView.findViewById(R.id.ivNewsImage);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsTimestamp = itemView.findViewById(R.id.tvNewsTimestamp);
            btnBookmark = itemView.findViewById(R.id.btnBookmark);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}
