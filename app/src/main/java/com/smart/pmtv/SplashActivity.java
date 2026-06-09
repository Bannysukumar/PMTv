package com.smart.pmtv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private Player.Listener preloadListener;
    private boolean isNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We do NOT use setContentView() here. 
        // The theme's android:windowBackground (@drawable/splash_background) natively renders the splash screen.
        // This prevents the "double splash screen" glitch.

        // Start preloading the stream immediately
        PlayerManager.getInstance().initPlayer(getApplicationContext());
        
        ExoPlayer player = PlayerManager.getInstance().getPlayer();

        preloadListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY && !isNavigated) {
                    navigateToMain();
                } else if (playbackState == Player.STATE_IDLE && !isNavigated) {
                    navigateToMain(); // Fallback if network fails
                }
            }
        };

        player.addListener(preloadListener);

        // Fallback: Max wait time of 3 seconds just in case of severe network hang
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isNavigated) navigateToMain();
        }, 3000);
    }

    private synchronized void navigateToMain() {
        if (isNavigated) return;
        isNavigated = true;
        
        ExoPlayer player = PlayerManager.getInstance().getPlayer();
        if (player != null && preloadListener != null) {
            player.removeListener(preloadListener);
        }

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
