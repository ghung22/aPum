package com.hcmus.apum;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Splash extends Activity {
    private static final int STORAGE_PERMISSION_CODE = 101;
    private final int SPLASH_DURATION = 1000;
    Intent main;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash);


        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                main = new Intent(Splash.this, MainActivity.class);
                //Request permission
//                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
            }
        }, SPLASH_DURATION);
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(Splash.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(Splash.this, new String[] { permission }, requestCode);
        }
        else {
            // Toast.makeText(Splash.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            startActivity(main);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Splash.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
                startActivity(main);
                finish();
            }
            else {
                Toast.makeText(Splash.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}