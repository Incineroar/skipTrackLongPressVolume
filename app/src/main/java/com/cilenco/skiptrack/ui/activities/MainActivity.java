package com.cilenco.skiptrack.ui.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cilenco.skiptrack.R;

import net.grandcentrix.tray.AppPreferences;

import static com.cilenco.skiptrack.utils.Constants.HUAWEI_CHECK;
import static com.cilenco.skiptrack.utils.Constants.PREF_PERMISSION;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener {
    private static final String PERMISSION = Manifest.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        AppPreferences prefs = new AppPreferences(this);

        int permission = checkSelfPermission(PERMISSION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{PERMISSION}, 1);
        }else {

            prefs.put(PREF_PERMISSION, true);
        }

        //TODO: Put this elsewhere? Not sure where the best place is for this but this seems to be the best place.
        //Since we have permissions for the long button press, then we know we can now run the service.

        //TODO: Check for other OEM power management functions that may affect this task and generate warnings for them.
        //Huawei note: check if the manufacturer of the device is Huawei, and prompt the user about background
        //management. EMUI has aggressive background task killing and it will affect this service.
        try
        {
            //if there are other OEMs to add in here later, a switch will work much better.
            if("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER) && !prefs.getBoolean(HUAWEI_CHECK, false)) {
                prefs.put(HUAWEI_CHECK, true);
                AlertDialog.Builder builder  = new AlertDialog.Builder(this);
                builder.setTitle(R.string.huawei_title).setMessage(R.string.huawei_text)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //do nothing. (there's a better way i know)
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"));
                                startActivity(intent);
                            }
                        })
                        .create().show();
            }
        }
        catch (Exception e)
        {
            //do nothing for now
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            AppPreferences prefs = new AppPreferences(this);
            prefs.put(PREF_PERMISSION, true);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setPositiveButton(android.R.string.ok, this);

            builder.setView(R.layout.dialog_permission);
            builder.setMessage(R.string.permission_description);
            builder.setTitle(R.string.permission_title);
            builder.setCancelable(false);

            builder.show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        finishAndRemoveTask();
    }
}
