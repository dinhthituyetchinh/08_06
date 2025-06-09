package Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ChargingForegroundService extends Service {

    private PowerReceiver powerReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        // Đăng ký Receiver để theo dõi cắm/rút sạc
        powerReceiver = new PowerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, filter);

        Log.d("MyService", "BroadcastReceiver registered");

        // Tạo thông báo foreground
        Notification notification = new NotificationCompat.Builder(this, "charging_channel")
                .setContentTitle("Đang theo dõi sạc")
                .setContentText("Ứng dụng sẽ phát hiện khi cắm hoặc rút sạc.")
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();

        startForeground(1, notification);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "charging_channel", "Charging Monitor", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        if (powerReceiver != null) {
            unregisterReceiver(powerReceiver);
            Log.d("MyService", "BroadcastReceiver unregistered");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}