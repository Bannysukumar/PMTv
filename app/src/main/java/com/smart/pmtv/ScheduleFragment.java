package com.smart.pmtv;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private RecyclerView rvSchedule;
    private ProgressBar pbScheduleLoading;
    private TabLayout tabLayoutSchedule;
    
    private ScheduleAdapter adapter;
    private List<ScheduleModel> scheduleList;
    private FirebaseFirestore db;

    private String currentDayFilter = "Today";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupFirestore();
        setupRecyclerView();
        setupTabs();
        
        fetchSchedule();
    }

    private void initViews(View view) {
        rvSchedule = view.findViewById(R.id.rvSchedule);
        pbScheduleLoading = view.findViewById(R.id.pbScheduleLoading);
        tabLayoutSchedule = view.findViewById(R.id.tabLayoutSchedule);
        scheduleList = new ArrayList<>();
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapter(requireContext(), scheduleList, NavHostFragment.findNavController(this));
        rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSchedule.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayoutSchedule.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) currentDayFilter = "Today";
                else if (tab.getPosition() == 1) currentDayFilter = "Tomorrow";
                else currentDayFilter = "Weekly";
                
                fetchSchedule();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void fetchSchedule() {
        pbScheduleLoading.setVisibility(View.VISIBLE);
        
        Query query = db.collection("Schedule").orderBy("timeString", Query.Direction.ASCENDING);
        
        if (!currentDayFilter.equals("Weekly")) {
            // For now, assuming Firestore document has a string field "dayFilter" mapping to Today/Tomorrow
            query = query.whereEqualTo("dayFilter", currentDayFilter);
        }

        query.addSnapshotListener((value, error) -> {
            pbScheduleLoading.setVisibility(View.GONE);
            if (error != null) {
                Log.e("ScheduleFragment", "Listen failed.", error);
                Toast.makeText(requireContext(), "Error fetching schedule", Toast.LENGTH_SHORT).show();
                return;
            }

            List<ScheduleModel> newSchedule = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    ScheduleModel schedule = doc.toObject(ScheduleModel.class);
                    if (schedule != null) {
                        newSchedule.add(schedule);
                    }
                }
            }
            adapter.updateList(newSchedule);
        });
    }
}
