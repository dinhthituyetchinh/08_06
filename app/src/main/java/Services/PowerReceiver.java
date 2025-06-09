package Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.myapplication.ChargingActivity;


public class PowerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("PowerReceiver", "Received action: " + action);

        if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
            Toast.makeText(context, "Đã cắm sạc", Toast.LENGTH_SHORT).show();

            // Mở ChargingActivity
            //Hiển thị % pin
            Intent chargingIntent = new Intent(context, ChargingActivity.class);
            chargingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Quan trọng để mở từ BroadcastReceiver
            context.startActivity(chargingIntent);

        } else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            Toast.makeText(context, "Đã rút sạc", Toast.LENGTH_SHORT).show();

            // Gửi tín hiệu để đóng ChargingActivity nếu đang mở
            Intent closeIntent = new Intent("CLOSE_CHARGING");
            LocalBroadcastManager.getInstance(context).sendBroadcast(closeIntent);
        }
    }
}

