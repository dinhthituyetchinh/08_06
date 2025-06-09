package com.example.myapplication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import Services.ChargingForegroundService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OVERLAY = 1234;
    private static final int REQUEST_CODE_NOTIFICATION = 1001;
    private AlertDialog batteryOptimizationDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkBatteryOptimization();
        }

        checkDrawOverlayPermission();
        checkNotificationPermission();

        // Khởi động ForegroundService để theo dõi sạc
        Intent serviceIntent = new Intent(this, ChargingForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void checkBatteryOptimization() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        String packageName = getPackageName();

        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Cấp quyền chạy nền")
                    .setMessage("Để ứng dụng hoạt động ổn định khi tắt màn hình hoặc khởi động lại, vui lòng tắt tối ưu hóa pin.")
                    .setPositiveButton("Cấp quyền", (dialog, which) -> requestIgnoreBatteryOptimization())
                    .setNegativeButton("Bỏ qua", null);

            batteryOptimizationDialog = builder.create();

            if (!isFinishing() && !isDestroyed()) {
                batteryOptimizationDialog.show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryOptimizationDialog != null && batteryOptimizationDialog.isShowing()) {
            batteryOptimizationDialog.dismiss();
        }
    }

    private void requestIgnoreBatteryOptimization() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                showBatteryOptimizationNotification();
            }
        }
    }

    private void showBatteryOptimizationNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "battery_channel",
                    "Battery Optimization",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "battery_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Thông báo")
                .setContentText("App đã có quyền chạy nền")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
            manager.notify(1001, notification);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION);
            }
        }
    }

    private void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_OVERLAY);
            } else {
                showAutoStartDialogIfNeeded();
                //startFloatingViewActivity();
                toNextActivity();
            }
        } else {
            showAutoStartDialogIfNeeded();
            // startFloatingViewActivity();
            toNextActivity();
        }
    }

    private void showAutoStartDialogIfNeeded() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        Log.d("AutoStartDialog", "Device manufacturer: " + manufacturer);

        if (manufacturer.contains("xiaomi") || manufacturer.contains("oppo") || manufacturer.contains("vivo")) {
            new AlertDialog.Builder(this)
                    .setTitle("Bật khởi động cùng hệ thống")
                    .setMessage("Để ứng dụng hoạt động đúng khi khởi động lại máy, vui lòng vào:\n\nCài đặt > Quyền > Tự khởi động > Bật cho ứng dụng này.")
                    .setPositiveButton("Tôi hiểu", null)
                    .show();
        }
    }

    private void startFloatingViewActivity() {
        Intent intent = new Intent(this, ChargingActivity.class);
        startActivity(intent);
    }

    private void toNextActivity() {
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    showAutoStartDialogIfNeeded();
                    //startFloatingViewActivity();
                    toNextActivity();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền hiển thị trên ứng dụng khác", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "POST_NOTIFICATIONS granted");
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để nhận thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
