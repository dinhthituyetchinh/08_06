package com.example.myapplication;

import static Utils.MediaUtils.getDownloadedAnimationFile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;

import java.io.File;

import Helper.ExoPlayerHelper;
import Helper.PreferencesHelper;
import Services.BatteryReceiver;

public class ChargingActivity extends AppCompatActivity implements Services.BatteryReceiver.BatteryListener{
    ImageView imgFullScreen;
    private TextView textBelowImage;
    private PlayerView playerView;
    ExoPlayerHelper exoPlayerHelper;
    private TextView batteryPercentageTextView;
    private BatteryReceiver batteryReceiver;

    @UnstableApi
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đánh thức màn hình nếu đang tắt
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        setContentView(R.layout.activity_charging);

        hideSystemUI();

        imgFullScreen = findViewById(R.id.imgFullScreen);
        playerView = findViewById(R.id.playerViewCharging);

        if (exoPlayerHelper != null) {
            exoPlayerHelper.release();  // đảm bảo xóa player cũ nếu còn tồn tại
        }
        exoPlayerHelper = new ExoPlayerHelper(this);
        playerView.setUseController(false);
        playerView.setPlayer(exoPlayerHelper.getPlayer());


        // Lấy dữ liệu SharedPreferences
        PreferencesHelper appPreferences = new PreferencesHelper(this);
        String appliedName = appPreferences.getAppliedAnimationName();
        String appliedCategory = appPreferences.getAppliedCategoryName();

        Log.d("ChargingActivity", "Applied name: " + appliedName);
        Log.d("ChargingActivity", "Applied category: " + appliedCategory);

        File file = getDownloadedAnimationFile(this, appliedName, appliedCategory);

        if (file != null) {
            if (file.getName().endsWith(".gif")) {
                // Hiển thị GIF
                exoPlayerHelper.release();
                playerView.setVisibility(View.GONE);
                imgFullScreen.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .asGif()
                        .load(file)
                        .into(imgFullScreen);

            } else if (file.getName().endsWith(".mp4")) {
                // Hiển thị video MP4
                Glide.with(this).clear(imgFullScreen);
                imgFullScreen.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);

                playMp4WithPlayerView(file);
            }
        } else {
            Toast.makeText(this, "Không tìm thấy file đã áp dụng", Toast.LENGTH_SHORT).show();
        }
        textBelowImage = findViewById(R.id.textBelowImage);

        updateChargingType();

        batteryPercentageTextView = findViewById(R.id.batteryPercentageTextView);

        // Khởi tạo receiver
        batteryReceiver = new BatteryReceiver(ChargingActivity.this);
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    @UnstableApi
    private void playMp4WithPlayerView(File file) {
        Uri videoUri = Uri.fromFile(file);
        exoPlayerHelper.prepareAndPlay(videoUri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayerHelper.release();
        unregisterReceiver(batteryReceiver);

    }
    // Callback từ BatteryReceiver
    @Override
    public void onBatteryChanged(int percentage) {
        batteryPercentageTextView.setText("Pin: " + percentage + "%");
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(closeReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(closeReceiver);
    }

    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("CLOSE_CHARGING".equals(intent.getAction())) {
                exoPlayerHelper.release();
                finish(); // Chỉ đóng nếu là broadcast nội bộ
            }

        }
    };

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoCloseRunnable = new Runnable() {
        @Override
        public void run() {
            exoPlayerHelper.release();
            finish();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Đăng ký local broadcast
        IntentFilter filter = new IntentFilter("CLOSE_CHARGING");
        LocalBroadcastManager.getInstance(this).registerReceiver(closeReceiver, filter);

        // Hẹn giờ tự động đóng sau 10 giây
        handler.postDelayed(autoCloseRunnable, 10_000); // 10 giây
    }


    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
        exoPlayerHelper.release();
    }


    private void updateChargingType() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        boolean wirelessCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;

        String chargingType;
        if (usbCharge) {
            chargingType = "Đang sạc qua USB";
        } else if (acCharge) {
            chargingType = "Đang sạc nhanh (ổ cắm AC)";
        } else if (wirelessCharge) {
            chargingType = "Đang sạc không dây";
        } else {
            chargingType = "Không sạc";
        }

        textBelowImage.setText(chargingType);
    }
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }
}