package com.smart.pmtv;

import android.content.Context;
import androidx.media3.common.MediaItem;
import androidx.media3.datasource.rtmp.RtmpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.source.MediaSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import android.util.Log;

public class PlayerManager {
    private static PlayerManager instance;
    private ExoPlayer player;
    private boolean isInitialized = false;
    private ListenerRegistration streamListener;
    private String currentUrl = "";
    private Context context;

    private PlayerManager() {}

    public static synchronized PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public void initPlayer(Context context) {
        if (player == null) {
            this.context = context.getApplicationContext();
            // Setup aggressive buffering load control to prevent drops
            DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                            2500,  // min buffer (reduced for live streams)
                            5000,  // max buffer
                            1000,  // buffer for playback
                            1500   // buffer for playback after rebuffer
                    ).build();

            player = new ExoPlayer.Builder(context.getApplicationContext())
                    .setLoadControl(loadControl)
                    .build();

            isInitialized = true;
            listenToStreamChanges();
        }
    }

    private void listenToStreamChanges() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        streamListener = db.collection("settings").document("livestream")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("PlayerManager", "Listen failed.", error);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        String newUrl = snapshot.getString("rtmpUrl");
                        if (newUrl != null && !newUrl.equals(currentUrl)) {
                            currentUrl = newUrl;
                            updatePlayerMedia(newUrl);
                        }
                    }
                });
    }

    private void updatePlayerMedia(String url) {
        if (player == null) return;
        
        MediaItem mediaItem = MediaItem.fromUri(url);
        
        MediaSource mediaSource;
        if (url != null && url.startsWith("rtmp")) {
            RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
            mediaSource = new androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        } else {
            DefaultDataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
            mediaSource = new DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(mediaItem);
        }

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
        
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e("PlayerManager", "Player Error: " + error.getMessage(), error);
            }
        });
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void releasePlayer() {
        if (streamListener != null) {
            streamListener.remove();
            streamListener = null;
        }
        if (player != null) {
            player.release();
            player = null;
            isInitialized = false;
        }
    }
}
