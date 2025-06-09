package Helper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;

public class ExoPlayerHelper {
    private static final String TAG = "ExoPlayerHelper";

    private ExoPlayer player;

    @UnstableApi
    public ExoPlayerHelper(Context context) {
        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(2000, 5000, 1000, 2000)
                .build();

        player = new ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .build();
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    /**
     * Chuẩn bị và phát một file MP4 từ Uri
     */
    public void prepareAndPlay(Uri uri) {
        if (player == null) return;

        try {
            MediaItem mediaItem = MediaItem.fromUri(uri);
            player.setMediaItem(mediaItem);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.setVolume(0f);
            player.setPlayWhenReady(true);
            player.prepare();

            Log.d(TAG, "Playing media: " + uri.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error while preparing ExoPlayer", e);
        }
    }

    /**
     * Giải phóng player
     */
    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
