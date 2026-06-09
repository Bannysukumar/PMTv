package com.smart.pmtv;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import com.facebook.shimmer.ShimmerFrameLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private RecyclerView rvNews;
    private ShimmerFrameLayout shimmerViewContainer;
    private ChipGroup chipGroupCategories;
    
    private NewsAdapter adapter;
    private List<NewsModel> newsList;
    private FirebaseFirestore db;

    private String currentCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupChips();
        
        fetchNews();
    }

    private void initViews(View view) {
        rvNews = view.findViewById(R.id.rvNews);
        shimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        newsList = new ArrayList<>();
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(requireContext(), newsList, NavHostFragment.findNavController(this));
        rvNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNews.setAdapter(adapter);
    }

    private void setupChips() {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) currentCategory = "All";
            else if (id == R.id.chipPolitics) currentCategory = "Politics";
            else if (id == R.id.chipSports) currentCategory = "Sports";
            else if (id == R.id.chipTech) currentCategory = "Technology";
            
            fetchNews();
        });
    }

    private void fetchNews() {
        shimmerViewContainer.startShimmer();
        shimmerViewContainer.setVisibility(View.VISIBLE);
        rvNews.setVisibility(View.GONE);
        
        Query query = db.collection("news").orderBy("timestamp", Query.Direction.DESCENDING);
        if (!currentCategory.equals("All")) {
            query = query.whereEqualTo("category", currentCategory);
        }

        query.addSnapshotListener((value, error) -> {
            shimmerViewContainer.stopShimmer();
            shimmerViewContainer.setVisibility(View.GONE);
            rvNews.setVisibility(View.VISIBLE);
            
            if (error != null) {
                Log.e("NewsFragment", "Listen failed.", error);
                Toast.makeText(requireContext(), "Error fetching news", Toast.LENGTH_SHORT).show();
                return;
            }

            List<NewsModel> newNews = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    NewsModel news = doc.toObject(NewsModel.class);
                    if (news != null) {
                        newNews.add(news);
                    }
                }
            }
            adapter.updateList(newNews);
        });
    }
}
