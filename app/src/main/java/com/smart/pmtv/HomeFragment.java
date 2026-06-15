package com.smart.pmtv;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private ExoPlayer player;
    private androidx.media3.ui.PlayerView playerView;

    private DatabaseReference presenceRef;
    private ValueEventListener presenceListener;
    private String viewerId;
    private TextView tvViewerCount, tvCurrentProgram, tvProgramDesc;
    private com.google.android.material.button.MaterialButton btnComments, btnLike, btnDislike;

    private DatabaseReference statsRef;
    private DatabaseReference userActionsRef;
    private DatabaseReference commentsRef;
    private ValueEventListener statsListener;
    private ValueEventListener userActionListener;
    private ValueEventListener commentsListener;

    private Handler viewerHandler = new Handler(Looper.getMainLooper());
    private Runnable viewerRunnable;

    private ImageButton btnLock, btnMute, btnFullscreenToggle, btnSettings, btnPip, btnCast;
    private View gestureIndicatorContainer, seekLeftContainer, seekRightContainer;
    private TextView gestureText, tvScreenLocked;
    private ImageView gestureIcon;

    private boolean isTV = false;
    private boolean isFullscreen = false;
    private boolean isNetworkAvailable = true;
    private boolean isScreenLocked = false;
    private boolean isMuted = false;

    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable retryRunnable;

    private PlayerService playerService;
    private boolean isBound = false;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private AudioManager audioManager;
    private float currentBrightness;
    private int currentVolume;
    private int maxVolume;

    private long lastRxBytes = 0;
    private long lastRxTime = 0;
    private Runnable networkSpeedRunnable;
    private PreferencesManager prefs;

    private View scrollView;
    private View appBarLayout;
    private ViewGroup fragmentRoot;
    private ViewGroup playerContainer;
    private ViewGroup playerContainerOriginalParent;
    private int playerContainerOriginalIndex;
    private ViewGroup.LayoutParams playerContainerOriginalLayoutParams;
    private float playerContainerOriginalCornerRadius;
    private float playerContainerOriginalElevation;
    private OnBackPressedCallback fullscreenBackCallback;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            playerService = binder.getService();
            isBound = true;
            
            playerService.setPlayerActionCallback(new PlayerService.PlayerActionCallback() {
                @Override
                public void onPlay() {
                    if (player != null) player.play();
                }

                @Override
                public void onPause() {
                    if (player != null) player.pause();
                }

                @Override
                public void onStop() {
                    if (player != null) {
                        player.stop();
                        if (getActivity() != null) getActivity().finish();
                    }
                }
            });
            if (player != null) {
                playerService.updateNotification(player.isPlaying());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefs = new PreferencesManager(requireContext());
        
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            currentVolume = prefs.getLastVolume(maxVolume / 2);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        }

        android.app.UiModeManager uiModeManager = (android.app.UiModeManager) requireContext().getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            isTV = true;
        }

        initViews(view);
        setupCustomControls(view);
        setupGestures();

        View liveDot = playerView.findViewById(R.id.liveDot);
        if (liveDot != null) {
            android.view.animation.Animation pulse = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.anim_pulse);
            liveDot.startAnimation(pulse);
        }

        checkNotificationPermission();
        initializePlayer();
        setupNetworkMonitoring();
        handleOrientation(getResources().getConfiguration().orientation);
        startNetworkSpeedMonitor();
        
        fullscreenBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                exitFullscreen();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), fullscreenBackCallback);

        listenToViewerCount();
    }

    private void initViews(View view) {
        playerView = view.findViewById(R.id.playerView);
        fragmentRoot = view.findViewById(R.id.main_layout);
        scrollView = view.findViewById(R.id.scroll_view);
        appBarLayout = view.findViewById(R.id.app_bar_layout);
        playerContainer = view.findViewById(R.id.playerContainer);

        gestureIndicatorContainer = view.findViewById(R.id.gestureIndicatorContainer);
        gestureText = view.findViewById(R.id.gestureText);
        gestureIcon = view.findViewById(R.id.gestureIcon);
        seekLeftContainer = view.findViewById(R.id.seekLeftContainer);
        seekRightContainer = view.findViewById(R.id.seekRightContainer);
        tvScreenLocked = view.findViewById(R.id.tvScreenLocked);
        tvViewerCount = view.findViewById(R.id.tv_viewer_count);
        tvCurrentProgram = view.findViewById(R.id.tv_current_program);
        tvProgramDesc = view.findViewById(R.id.tv_program_desc);
    }

    private void setupCustomControls(View view) {
        btnLock = view.findViewById(R.id.btnLock);
        btnMute = view.findViewById(R.id.btnMute);
        btnFullscreenToggle = view.findViewById(R.id.btnFullscreenToggle);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnPip = view.findViewById(R.id.btnPip);
        btnCast = view.findViewById(R.id.btnCast);

        if (btnLock != null) {
            btnLock.setOnClickListener(v -> toggleScreenLock(view));
        }

        if (btnMute != null) {
            btnMute.setOnClickListener(v -> {
                isMuted = !isMuted;
                player.setVolume(isMuted ? 0f : 1f);
                btnMute.setImageResource(isMuted ? android.R.drawable.ic_lock_silent_mode : android.R.drawable.ic_lock_silent_mode_off);
            });
        }

        if (btnFullscreenToggle != null) {
            btnFullscreenToggle.setOnClickListener(v -> toggleFullscreen());
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                SettingsBottomSheet bottomSheet = new SettingsBottomSheet();
                bottomSheet.show(getChildFragmentManager(), "SettingsBottomSheet");
            });
        }

        if (btnPip != null) {
            btnPip.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).enterPiP();
                }
            });
        }

        if (btnCast != null) {
            try {
                com.google.android.gms.cast.framework.CastContext.getSharedInstance(requireContext());
                androidx.mediarouter.media.MediaRouteSelector selector = new androidx.mediarouter.media.MediaRouteSelector.Builder()
                        .addControlCategory(com.google.android.gms.cast.CastMediaControlIntent.categoryForCast(
                                com.google.android.gms.cast.CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID))
                        .build();

                btnCast.setOnClickListener(v -> {
                    androidx.mediarouter.app.MediaRouteChooserDialog dialog =
                            new androidx.mediarouter.app.MediaRouteChooserDialog(requireContext());
                    dialog.setRouteSelector(selector);
                    dialog.show();
                });
            } catch (Exception e) {
                btnCast.setOnClickListener(v -> Toast.makeText(requireContext(), "Cast not available", Toast.LENGTH_SHORT).show());
            }
        }
        
        btnComments = view.findViewById(R.id.btn_comments);
        btnLike = view.findViewById(R.id.btn_like);
        btnDislike = view.findViewById(R.id.btn_dislike);
        if (btnComments != null) {
            btnComments.setOnClickListener(v -> {
                CommentsBottomSheet bottomSheet = new CommentsBottomSheet();
                bottomSheet.show(getChildFragmentManager(), "CommentsBottomSheet");
            });
        }

        View btnPlay = view.findViewById(androidx.media3.ui.R.id.exo_play);
        View btnPause = view.findViewById(androidx.media3.ui.R.id.exo_pause);
        View btnRew = view.findViewById(R.id.btnRewind);
        View btnFfwd = view.findViewById(R.id.btnForward);
        
        if (btnPlay != null) {
            btnPlay.setOnClickListener(v -> {
                if (player != null) {
                    player.play();
                    btnPlay.setVisibility(View.GONE);
                    if (btnPause != null) btnPause.setVisibility(View.VISIBLE);
                }
            });
        }
        
        if (btnPause != null) {
            btnPause.setOnClickListener(v -> {
                if (player != null) {
                    player.pause();
                    btnPause.setVisibility(View.GONE);
                    if (btnPlay != null) btnPlay.setVisibility(View.VISIBLE);
                }
            });
        }
        
        if (btnRew != null) {
            btnRew.setOnClickListener(v -> {
                if (player != null) player.seekTo(Math.max(0, player.getCurrentPosition() - 10000));
            });
        }
        
        if (btnFfwd != null) {
            btnFfwd.setOnClickListener(v -> {
                if (player != null) player.seekTo(player.getCurrentPosition() + 10000);
            });
        }

        if (isTV) {
            View.OnFocusChangeListener focusChangeListener = (v, hasFocus) -> {
                if (hasFocus) {
                    v.animate().scaleX(1.15f).scaleY(1.15f).setDuration(200).start();
                    v.setBackgroundResource(R.drawable.ripple_glass_rounded); 
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                    v.setBackgroundResource(R.drawable.ripple_glass_circle);
                }
            };

            if (btnLock != null) btnLock.setOnFocusChangeListener(focusChangeListener);
            if (btnMute != null) btnMute.setOnFocusChangeListener(focusChangeListener);
            if (btnFullscreenToggle != null) btnFullscreenToggle.setOnFocusChangeListener(focusChangeListener);
            if (btnSettings != null) btnSettings.setOnFocusChangeListener(focusChangeListener);
            if (btnPip != null) btnPip.setOnFocusChangeListener(focusChangeListener);
            if (btnCast != null) btnCast.setOnFocusChangeListener(focusChangeListener);
            
            if (btnPlay != null) btnPlay.setOnFocusChangeListener(focusChangeListener);
            if (btnPause != null) btnPause.setOnFocusChangeListener(focusChangeListener);
            if (btnRew != null) btnRew.setOnFocusChangeListener(focusChangeListener);
            if (btnFfwd != null) btnFfwd.setOnFocusChangeListener(focusChangeListener);
        }
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    private void enterFullscreen() {
        if (playerContainer == null || fragmentRoot == null || isFullscreen) return;

        playerContainerOriginalParent = (ViewGroup) playerContainer.getParent();
        if (playerContainerOriginalParent == null) return;

        playerContainerOriginalIndex = playerContainerOriginalParent.indexOfChild(playerContainer);
        playerContainerOriginalLayoutParams = playerContainer.getLayoutParams();

        if (playerContainer instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) playerContainer;
            playerContainerOriginalCornerRadius = card.getRadius();
            playerContainerOriginalElevation = card.getCardElevation();
            card.setRadius(0f);
            card.setCardElevation(0f);
        }

        playerContainerOriginalParent.removeView(playerContainer);

        androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams fullscreenParams =
                new androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
        playerContainer.setLayoutParams(fullscreenParams);
        fragmentRoot.addView(playerContainer);

        if (scrollView != null) scrollView.setVisibility(View.GONE);
        if (appBarLayout != null) appBarLayout.setVisibility(View.GONE);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavigationVisible(false);
        }

        if (!isTV) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            applyImmersiveUi(true);
        }

        playerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL);
        if (btnFullscreenToggle != null) {
            btnFullscreenToggle.setImageResource(R.drawable.ic_fullscreen_exit);
        }

        isFullscreen = true;
        if (fullscreenBackCallback != null) fullscreenBackCallback.setEnabled(true);
        playerView.showController();
        playerView.requestFocus();
    }

    private void exitFullscreen() {
        if (!isFullscreen || playerContainer == null || fragmentRoot == null || playerContainerOriginalParent == null) {
            return;
        }

        fragmentRoot.removeView(playerContainer);
        playerContainer.setLayoutParams(playerContainerOriginalLayoutParams);

        if (playerContainer instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) playerContainer;
            card.setRadius(playerContainerOriginalCornerRadius);
            card.setCardElevation(playerContainerOriginalElevation);
        }

        playerContainerOriginalParent.addView(playerContainer, playerContainerOriginalIndex);

        if (scrollView != null) scrollView.setVisibility(View.VISIBLE);
        if (appBarLayout != null) appBarLayout.setVisibility(View.VISIBLE);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNavigationVisible(true);
        }

        if (!isTV) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            applyImmersiveUi(false);
        }

        playerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);
        if (btnFullscreenToggle != null) {
            btnFullscreenToggle.setImageResource(R.drawable.ic_fullscreen);
        }

        isFullscreen = false;
        if (fullscreenBackCallback != null) fullscreenBackCallback.setEnabled(false);
    }

    private void applyImmersiveUi(boolean immersive) {
        if (getActivity() == null) return;

        View decorView = requireActivity().getWindow().getDecorView();
        if (immersive) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.view.WindowInsetsController controller = decorView.getWindowInsetsController();
                if (controller != null) {
                    controller.hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.view.WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.show(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
            }
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private void toggleScreenLock(View view) {
        isScreenLocked = !isScreenLocked;
        View btnUnlock = view.findViewById(R.id.btnUnlock);
        
        if (isScreenLocked) {
            playerView.setUseController(false);
            if (btnLock != null) btnLock.setImageResource(android.R.drawable.ic_menu_edit);
            tvScreenLocked.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "Screen Locked", Toast.LENGTH_SHORT).show();
            
            if (btnUnlock != null) {
                btnUnlock.setOnClickListener(v -> toggleScreenLock(view));
            }
        } else {
            playerView.setUseController(true);
            playerView.showController();
            if (btnLock != null) btnLock.setImageResource(android.R.drawable.ic_secure);
            tvScreenLocked.setVisibility(View.GONE);
            if (btnUnlock != null) btnUnlock.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Screen Unlocked", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGestures() {
        scaleGestureDetector = new ScaleGestureDetector(requireContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (isScreenLocked) return true;
                float scaleFactor = detector.getScaleFactor();
                if (scaleFactor > 1.1f) {
                    playerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    showGestureIndicator("Zoom", -1, android.R.drawable.ic_menu_zoom);
                } else if (scaleFactor < 0.9f) {
                    playerView.setResizeMode(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    showGestureIndicator("Fit", -1, android.R.drawable.ic_menu_zoom);
                }
                return true;
            }
        });

        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isScreenLocked || player == null) return true;
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                if (e.getX() < screenWidth / 2f) {
                    long pos = Math.max(0, player.getCurrentPosition() - 10000);
                    player.seekTo(pos);
                    showDoubleTapAnimation(true);
                } else {
                    long pos = player.getCurrentPosition() + 10000;
                    player.seekTo(pos);
                    showDoubleTapAnimation(false);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (isScreenLocked || e1 == null || e2 == null) return true;
                
                if (Math.abs(distanceY) > Math.abs(distanceX)) {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    float deltaY = (e1.getY() - e2.getY()) / getResources().getDisplayMetrics().heightPixels;

                    if (e1.getX() > screenWidth / 2f) {
                        int volChange = (int) (deltaY * maxVolume * 2);
                        if (volChange != 0) {
                            int newVol = Math.min(Math.max(currentVolume + volChange, 0), maxVolume);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);
                            int progress = (int) ((newVol / (float) maxVolume) * 100);
                            showGestureIndicator(progress + "%", progress, android.R.drawable.ic_lock_silent_mode_off);
                            currentVolume = newVol;
                        }
                    } else {
                        float newBrightness = Math.min(Math.max(currentBrightness + deltaY * 2, 0.01f), 1f);
                        WindowManager.LayoutParams lp = requireActivity().getWindow().getAttributes();
                        lp.screenBrightness = newBrightness;
                        requireActivity().getWindow().setAttributes(lp);
                        int progress = (int) (newBrightness * 100);
                        showGestureIndicator(progress + "%", progress, android.R.drawable.ic_menu_always_landscape_portrait);
                        currentBrightness = newBrightness;
                    }
                }
                return true;
            }
        });

        playerView.setOnTouchListener((v, event) -> {
            if (isScreenLocked && event.getAction() == MotionEvent.ACTION_DOWN) {
                View btnUnlock = getView() != null ? getView().findViewById(R.id.btnUnlock) : null;
                if (btnUnlock != null) {
                    btnUnlock.setVisibility(View.VISIBLE);
                    mainHandler.postDelayed(() -> btnUnlock.setVisibility(View.GONE), 3000);
                }
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (audioManager != null) currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (getActivity() != null) {
                    currentBrightness = requireActivity().getWindow().getAttributes().screenBrightness;
                    if (currentBrightness < 0) currentBrightness = 0.5f;
                }
            }

            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    private void showGestureIndicator(String text, int progress, int iconRes) {
        gestureIndicatorContainer.setVisibility(View.VISIBLE);
        gestureText.setText(text);
        ProgressBar pb = getView() != null ? getView().findViewById(R.id.gestureProgressBar) : null;
        if (pb != null) {
            if (progress >= 0) {
                pb.setVisibility(View.VISIBLE);
                pb.setProgress(progress);
            } else {
                pb.setVisibility(View.GONE);
            }
        }
        gestureIcon.setImageResource(iconRes);
        mainHandler.removeCallbacks(hideGestureIndicatorRunnable);
        mainHandler.postDelayed(hideGestureIndicatorRunnable, 1500);
    }

    private final Runnable hideGestureIndicatorRunnable = () -> gestureIndicatorContainer.setVisibility(View.GONE);

    private void showDoubleTapAnimation(boolean isLeft) {
        View target = isLeft ? seekLeftContainer : seekRightContainer;
        target.setVisibility(View.VISIBLE);
        mainHandler.postDelayed(() -> target.setVisibility(View.GONE), 800);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void initializePlayer() {
        PlayerManager.getInstance().initPlayer(requireContext());
        player = PlayerManager.getInstance().getPlayer();
        playerView.setPlayer(player);

        player.addListener(new Player.Listener() {
                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    View btnPlay = getView() != null ? getView().findViewById(androidx.media3.ui.R.id.exo_play) : null;
                    View btnPause = getView() != null ? getView().findViewById(androidx.media3.ui.R.id.exo_pause) : null;
                    if (isPlaying) {
                        if (btnPlay != null) btnPlay.setVisibility(View.GONE);
                        if (btnPause != null) btnPause.setVisibility(View.VISIBLE);
                    } else {
                        if (btnPause != null) btnPause.setVisibility(View.GONE);
                        if (btnPlay != null) btnPlay.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {
                        if (isBound && playerService != null) playerService.updateNotification(true);
                    } else if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                        if (isBound && playerService != null) playerService.updateNotification(false);
                    }
                }

                @Override
                public void onPlayerError(@NonNull PlaybackException error) {
                    Toast.makeText(requireContext(), "Playback Error: Check network connection", Toast.LENGTH_SHORT).show();
                    scheduleReconnect();
                }
            });
    }

    private void scheduleReconnect() {
        if (retryRunnable != null) mainHandler.removeCallbacks(retryRunnable);
        retryRunnable = () -> {
            if (isNetworkAvailable && player != null) {
                player.prepare();
                player.setPlayWhenReady(true);
            } else {
                scheduleReconnect();
            }
        };
        mainHandler.postDelayed(retryRunnable, 5000);
    }

    private void setupNetworkMonitoring() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkRequest request = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    isNetworkAvailable = true;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (player != null && !player.isPlaying() && player.getPlaybackState() != Player.STATE_READY) {
                                player.prepare();
                                player.setPlayWhenReady(true);
                            }
                        });
                    }
                }
                @Override
                public void onLost(@NonNull Network network) {
                    isNetworkAvailable = false;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
    }

    private void startNetworkSpeedMonitor() {
        lastRxBytes = TrafficStats.getTotalRxBytes();
        lastRxTime = System.currentTimeMillis();
        
        networkSpeedRunnable = () -> {
            long currentRxBytes = TrafficStats.getTotalRxBytes();
            long currentTime = System.currentTimeMillis();
            
            if (currentRxBytes != TrafficStats.UNSUPPORTED && lastRxTime != 0) {
                long diffBytes = currentRxBytes - lastRxBytes;
                long diffTime = currentTime - lastRxTime;
                
                if (diffTime > 0) {
                    long bps = (diffBytes * 1000) / diffTime;
                    float mbps = (bps * 8f) / 1000000f;
                    // Network speed could be logged or shown in a different view if needed
                }
            }
            
            lastRxBytes = currentRxBytes;
            lastRxTime = currentTime;
            mainHandler.postDelayed(networkSpeedRunnable, 1000);
        };
        mainHandler.post(networkSpeedRunnable);
    }

    private void handleOrientation(int orientation) {
        if (isTV) return;

        if (isFullscreen) {
            applyImmersiveUi(true);
            return;
        }

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            applyImmersiveUi(true);
            View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        } else {
            applyImmersiveUi(false);
            View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        handleOrientation(newConfig.orientation);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), PlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) requireActivity().startForegroundService(intent);
        else requireActivity().startService(intent);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (prefs != null && !prefs.isBackgroundPlaybackEnabled() && player != null) {
            player.pause();
        }
    }

    @Override
    public void onDestroyView() {
        if (isFullscreen) {
            exitFullscreen();
        }
        super.onDestroyView();
        if (isBound) {
            requireActivity().unbindService(serviceConnection);
            isBound = false;
        }
        if (retryRunnable != null) mainHandler.removeCallbacks(retryRunnable);
        if (networkSpeedRunnable != null) mainHandler.removeCallbacks(networkSpeedRunnable);
        if (viewerRunnable != null) viewerHandler.removeCallbacks(viewerRunnable);
        
        if (presenceRef != null) {
            presenceRef.removeValue();
        }
        if (presenceListener != null) {
            try {
                FirebaseDatabase.getInstance("https://pmtv5464-default-rtdb.firebaseio.com")
                    .getReference("live_stream/viewers").removeEventListener(presenceListener);
            } catch (Exception e) {
                FirebaseDatabase.getInstance().getReference("live_stream/viewers").removeEventListener(presenceListener);
            }
        }
        if (statsRef != null && statsListener != null) statsRef.removeEventListener(statsListener);
        if (userActionsRef != null && userActionListener != null) userActionsRef.removeEventListener(userActionListener);
        if (commentsRef != null && commentsListener != null) commentsRef.removeEventListener(commentsListener);

        // Unbind player from view
        if (playerView != null) {
            playerView.setPlayer(null);
        }
    }

    private void listenToViewerCount() {
        if (tvViewerCount == null) return;

        FirebaseDatabase rtdb;
        try {
            rtdb = FirebaseDatabase.getInstance("https://pmtv5464-default-rtdb.firebaseio.com");
        } catch (Exception e) {
            rtdb = FirebaseDatabase.getInstance();
        }
        
        viewerId = java.util.UUID.randomUUID().toString();
        
        DatabaseReference viewersRef = rtdb.getReference("live_stream/viewers");
        presenceRef = viewersRef.child(viewerId);
        
        presenceRef.setValue(true);
        presenceRef.onDisconnect().removeValue();

        presenceListener = viewersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final long actualCount = snapshot.getChildrenCount();
                if (tvViewerCount != null) {
                    tvViewerCount.setText(formatViewerCount(actualCount) + " watching");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("FirebaseError", "Viewer count error: " + error.getMessage());
            }
        });

        // Listen for live stream metadata from Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("settings").document("livestream")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    if (snapshot != null && snapshot.exists() && getActivity() != null) {
                        String title = snapshot.getString("channelName");
                        String desc = snapshot.getString("description");
                        if (title != null && tvCurrentProgram != null) tvCurrentProgram.setText(title);
                        if (desc != null && tvProgramDesc != null) tvProgramDesc.setText(desc);
                    }
                });
                
        setupLikeDislikeLogic();
    }

    private void setupLikeDislikeLogic() {
        if (btnLike == null || btnDislike == null) return;

        FirebaseDatabase rtdb;
        try {
            rtdb = FirebaseDatabase.getInstance("https://pmtv5464-default-rtdb.firebaseio.com");
        } catch (Exception e) {
            rtdb = FirebaseDatabase.getInstance();
        }

        statsRef = rtdb.getReference("live_stream/stats");
        
        statsListener = statsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long likes = snapshot.child("likes").getValue(Long.class) != null ? snapshot.child("likes").getValue(Long.class) : 0;
                long dislikes = snapshot.child("dislikes").getValue(Long.class) != null ? snapshot.child("dislikes").getValue(Long.class) : 0;
                
                if (btnLike != null) btnLike.setText(likes > 0 ? formatViewerCount(likes) : "Like");
                if (btnDislike != null) btnDislike.setText(dislikes > 0 ? formatViewerCount(dislikes) : "Dislike");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        UserManager userManager = UserManager.getInstance();
        if (userManager.isAuthenticated() && userManager.getCurrentUser() != null) {
            String userId = userManager.getCurrentUser().getUid();
            userActionsRef = rtdb.getReference("live_stream/user_actions").child(userId);
            
            userActionListener = userActionsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String action = snapshot.child("action").getValue(String.class);
                    updateLikeDislikeUI(action);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("FirebaseError", "User action listener error: " + error.getMessage());
                }
            });
        }
        
        // Listen to comments count
        commentsRef = rtdb.getReference("live_stream/comments");
        commentsListener = commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long commentCount = snapshot.getChildrenCount();
                if (btnComments != null) {
                    btnComments.setText(commentCount > 0 ? formatViewerCount(commentCount) : "Comments");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnLike.setOnClickListener(v -> handleInteraction("like"));
        btnDislike.setOnClickListener(v -> handleInteraction("dislike"));
    }

    private void updateLikeDislikeUI(String action) {
        if (btnLike == null || btnDislike == null) return;
        boolean isLiked = "like".equals(action);
        boolean isDisliked = "dislike".equals(action);

        int activeColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.core_accent);
        int defaultColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.black);
        
        // Try getting a subtle background color, otherwise default to transparent or a slight gray
        int activeBgColor = androidx.core.graphics.ColorUtils.setAlphaComponent(activeColor, 40); // 15% opacity
        int defaultBgColor = androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.transparent);

        if (isLiked) {
            btnLike.setIconTint(android.content.res.ColorStateList.valueOf(activeColor));
            btnLike.setTextColor(activeColor);
            btnLike.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeBgColor));
        } else {
            btnLike.setIconTint(android.content.res.ColorStateList.valueOf(defaultColor));
            btnLike.setTextColor(defaultColor);
            btnLike.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultBgColor));
        }

        if (isDisliked) {
            btnDislike.setIconTint(android.content.res.ColorStateList.valueOf(activeColor));
            btnDislike.setTextColor(activeColor);
            btnDislike.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeBgColor));
        } else {
            btnDislike.setIconTint(android.content.res.ColorStateList.valueOf(defaultColor));
            btnDislike.setTextColor(defaultColor);
            btnDislike.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultBgColor));
        }
    }

    private void handleInteraction(String tappedAction) {
        UserManager userManager = UserManager.getInstance();
        if (!userManager.isAuthenticated()) {
            try {
                userManager.requireLogin(requireContext(), androidx.navigation.fragment.NavHostFragment.findNavController(this), "interact with the stream");
            } catch(Exception e) {
                Toast.makeText(requireContext(), "Please login to interact", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (userActionsRef == null) {
            String userId = userManager.getCurrentUser().getUid();
            FirebaseDatabase rtdb;
            try {
                rtdb = FirebaseDatabase.getInstance("https://pmtv5464-default-rtdb.firebaseio.com");
            } catch (Exception e) {
                rtdb = FirebaseDatabase.getInstance();
            }
            userActionsRef = rtdb.getReference("live_stream/user_actions").child(userId);
        }

        userActionsRef.get().addOnSuccessListener(snapshot -> {
            String currentAction = snapshot.child("action").getValue(String.class);
            String newAction;
            
            long likeDelta = 0;
            long dislikeDelta = 0;

            if (tappedAction.equals(currentAction)) {
                // Remove interaction
                newAction = null;
                if ("like".equals(tappedAction)) likeDelta = -1;
                else if ("dislike".equals(tappedAction)) dislikeDelta = -1;
            } else {
                // Change or new interaction
                newAction = tappedAction;
                if ("like".equals(tappedAction)) {
                    likeDelta = 1;
                    if ("dislike".equals(currentAction)) dislikeDelta = -1;
                } else if ("dislike".equals(tappedAction)) {
                    dislikeDelta = 1;
                    if ("like".equals(currentAction)) likeDelta = -1;
                }
            }

            userActionsRef.child("action").setValue(newAction);
            
            // Transactional update of stats
            if (likeDelta != 0 || dislikeDelta != 0) {
                final long lDelta = likeDelta;
                final long dDelta = dislikeDelta;
                statsRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                        Long likes = currentData.child("likes").getValue(Long.class);
                        Long dislikes = currentData.child("dislikes").getValue(Long.class);
                        if (likes == null) likes = 0L;
                        if (dislikes == null) dislikes = 0L;
                        
                        currentData.child("likes").setValue(Math.max(0, likes + lDelta));
                        currentData.child("dislikes").setValue(Math.max(0, dislikes + dDelta));
                        return com.google.firebase.database.Transaction.success(currentData);
                    }
                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
                });
            }
        });
    }

    private String formatViewerCount(long count) {
        if (count < 1000) return String.valueOf(count);
        if (count < 1000000) return String.format(java.util.Locale.US, "%.1fK", count / 1000f);
        return String.format(java.util.Locale.US, "%.1fM", count / 1000000f);
    }
}
