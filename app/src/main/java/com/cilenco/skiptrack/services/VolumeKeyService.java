package com.cilenco.skiptrack.services;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaSessionManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.cilenco.skiptrack.R;

import net.grandcentrix.tray.AppPreferences;
import net.grandcentrix.tray.core.OnTrayPreferenceChangeListener;
import net.grandcentrix.tray.core.TrayItem;

import java.util.Collection;

import static android.view.KeyEvent.FLAG_FROM_SYSTEM;
import static android.view.KeyEvent.FLAG_LONG_PRESS;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;
import static com.cilenco.skiptrack.utils.Constants.HUAWEI_CHECK;
import static com.cilenco.skiptrack.utils.Constants.PREF_DEBUG;
import static com.cilenco.skiptrack.utils.Constants.PREF_ENABLED;
import static com.cilenco.skiptrack.utils.Constants.PREF_NO_MEDIA;
import static com.cilenco.skiptrack.utils.Constants.PREF_PERMISSION;
import static com.cilenco.skiptrack.utils.Constants.PREF_SCREEN_ON;

public class VolumeKeyService extends NotificationListenerService implements MediaSessionManager.OnVolumeKeyLongPressListener, OnTrayPreferenceChangeListener {
    
    private static final String PERMISSION = Manifest.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER;

    //Add a reference for a vibrator, as well as for the import.
    //Don't forget the manifest declaration to use the vibrator or it will not work.
    Vibrator vibrator;

    private AppPreferences preferences;

    private MediaSessionManager mediaSessionManager;
    private PowerManager powerManager;
    private AudioManager audioManager;

    private Handler mHandler=null;

    private boolean debugEnabled;
    private boolean prefScreenOn;
    private boolean prefNoMedia;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("cilenco", "VolumeKeyService: onCreate");

        //get the vibrator from system
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        preferences = new AppPreferences(this);
        preferences.registerOnTrayPreferenceChangeListener(this);

        audioManager = getSystemService(AudioManager.class);
        powerManager = getSystemService(PowerManager.class);

        mediaSessionManager = getSystemService(MediaSessionManager.class);


        int permission = checkSelfPermission(PERMISSION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //requestPermissions(new String[]{PERMISSION}, 1);
            Log.i("cilenco", "VolumeKeyService: permission NOT granted");
        }else {

            preferences.put(PREF_PERMISSION, true);
        }


    }

    private Handler getHandler() {
        if(mHandler==null) {
            mHandler = new Handler();
        }
        return mHandler;
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        Log.i("cilenco", "VolumeKeyService: onStart");
        startService();
    }

    @Override
    public void onTrayPreferenceChanged(Collection<TrayItem> items) {
        Log.d("cilenco", "VolumeKeyService: onTrayPreferenceChanged");
        startService();
    }

    private void startService() {
        boolean permission = preferences.getBoolean(PREF_PERMISSION, false);
        boolean serviceEnabled = preferences.getBoolean(PREF_ENABLED, false);

        debugEnabled = preferences.getBoolean(PREF_DEBUG, false);
        prefScreenOn = preferences.getBoolean(PREF_SCREEN_ON, false);
        prefNoMedia = preferences.getBoolean(PREF_NO_MEDIA, false);

        if(serviceEnabled && permission) {
            mediaSessionManager.setOnVolumeKeyLongPressListener(this, getHandler());
            Log.d("cilenco", "Registered VolumeKeyListener");
        } else {
            mediaSessionManager.setOnVolumeKeyLongPressListener(null, null);
            Log.d("cilenco", "Unregistered VolumeKeyListener");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        preferences.unregisterOnTrayPreferenceChangeListener(this);
        mediaSessionManager.setOnVolumeKeyLongPressListener(null, null);
    }

    @Override
    public void onVolumeKeyLongPress(KeyEvent keyEvent) {
        boolean screenOn = powerManager.isInteractive();
        boolean musicPlaying = audioManager.isMusicActive();

        int flags = keyEvent.getFlags();

        //if(keyEvent.getFlags() != FLAG_FROM_SYSTEM) return;
        if(!(flags == FLAG_FROM_SYSTEM || flags == FLAG_LONG_PRESS)) return;

        if((musicPlaying || prefNoMedia) && (!screenOn || prefScreenOn)) {

            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getRepeatCount() <= 1) {
                int keyCode = keyEvent.getKeyCode();

                int event = (keyCode == KEYCODE_VOLUME_UP) ? KEYCODE_MEDIA_NEXT : KEYCODE_MEDIA_PREVIOUS;
                int msgRes = (keyCode == KEYCODE_VOLUME_UP) ? R.string.msg_media_next : R.string.msg_media_pre;

                //It seems that the commented out code below, while it does work, only does ONE action.
                //After testing, it appears both an DOWN and UP event are required for maximum media player compatibility.
                //This fixes the issue reported where media was not switching in Droidsound-E, and presumably fixes it
                //for other media players where this service was not working originally.

                //KeyEvent skipEvent = new KeyEvent(keyEvent.getAction(), event);
                //audioManager.dispatchMediaKeyEvent(skipEvent);

                //We use the same event keycode on both the DOWN and UP actions to simulate the full press states of the
                //simulated button. They must also both be dispatched.

                KeyEvent skipEvent = new KeyEvent(keyEvent.ACTION_DOWN, event);
                audioManager.dispatchMediaKeyEvent(skipEvent);

                skipEvent = new KeyEvent(keyEvent.ACTION_UP, event);
                audioManager.dispatchMediaKeyEvent(skipEvent);

                //Add a very brief vibrate after the button presses are sent as feedback for the user to release the button
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));

                if (debugEnabled) Toast.makeText(this, getString(msgRes), Toast.LENGTH_SHORT).show();
            }

            return;
        }

        // Let the MediaSessionManager deal with the event

        mediaSessionManager.setOnVolumeKeyLongPressListener(null, null);
        mediaSessionManager.dispatchVolumeKeyEvent(keyEvent, audioManager.getUiSoundsStreamType(), false);
        mediaSessionManager.setOnVolumeKeyLongPressListener(this, getHandler());
    }
}
