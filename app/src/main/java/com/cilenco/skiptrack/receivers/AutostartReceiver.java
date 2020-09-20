package com.cilenco.skiptrack.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.cilenco.skiptrack.services.VolumeKeyService;

public class AutostartReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent arg1)
    {
        Log.i("cilenco", "AutostartReceiver: received message");

        Intent intent = new Intent(context,VolumeKeyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        Log.i("cilenco", "AutostartReceiver: startService called");
    }    
}