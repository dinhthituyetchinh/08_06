package Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryReceiver extends BroadcastReceiver {

    public interface BatteryListener {
        void onBatteryChanged(int percentage);
    }

    private final BatteryListener listener;

    public BatteryReceiver(BatteryListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Lấy mức pin hiện tại (số nguyên từ 0 → scale) từ Intent được gửi khi pin thay đổi.
        //Nếu không tìm thấy, mặc định là -1
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        //Lấy giá trị tối đa của pin (thường là 100, đôi khi scale = 1000 trên một số thiết bị
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level >= 0 && scale > 0) {
            int batteryPct = (int) ((level / (float) scale) * 100);
            listener.onBatteryChanged(batteryPct);
        }
    }
}
