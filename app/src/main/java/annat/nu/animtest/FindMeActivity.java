package annat.nu.animtest;

import android.app.Activity;
import android.content.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 */
public class FindMeActivity extends Activity {

    private AudioManager audioManager;
    private static int origVolume = -1;
    private int maxVolume;
    private Uri alarmSound;
    private MediaPlayer mediaPlayer;

    private static class StopMusicReceiver extends BroadcastReceiver {
        private WeakReference<Activity> activity;

        public StopMusicReceiver(Activity activity) {
            this.activity = new WeakReference<Activity>(activity);
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            if (activity != null)
                activity.get().finish();
            activity.get().overridePendingTransition(0, 0);
        }
    }


    private StopMusicReceiver stopMusicReceiver;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(null);
        overridePendingTransition(0, 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_find_me);

        final View contentView = findViewById(R.id.fullscreen_content);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        stopMusicReceiver = new StopMusicReceiver(this);
        registerReceiver(stopMusicReceiver, new IntentFilter("com.nordicUsability.wearaware.STOP_PHONE_FINDER"));
        startMusic();

    }

    private void startMusic() {

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        origVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayer = new MediaPlayer();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ringtone = preferences.getString("ringtone", alarmSound.toString());
        alarmSound = Uri.parse(ringtone);

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        if (origVolume < 0) {
            origVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        }

        mediaPlayer.reset();
        // Sound alarm at max volume.
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), alarmSound);
            mediaPlayer.prepare();

        } catch (IOException e) {
        }
        mediaPlayer.setLooping(false);

        mediaPlayer.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 30000);

    }

    private void stopMusic() {
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, origVolume, 0);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }

    @Override
    protected void onDestroy() {

        stopMusic();
        unregisterReceiver(stopMusicReceiver);
        super.onDestroy();
    }

}
