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

public class SplashActivity extends Activity {
    private final static int STORAGE_PERMISSION_REQUEST_CODE = 101;
    private final static int SPLASH_DURATION = 1000;
    Intent main;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_splash);


        // Start MainActivity and close SplashActivity after a while
        new Handler().postDelayed(() -> {
            main = new Intent(SplashActivity.this, MainActivity.class);
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_REQUEST_CODE);
        }, SPLASH_DURATION);
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(SplashActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{permission}, requestCode);
        } else {
            startActivity(main);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(SplashActivity.this, getResources().getString(R.string.info_permission_status, "granted"), Toast.LENGTH_SHORT).show();
                startActivity(main);
                finish();
            } else {
                Toast.makeText(SplashActivity.this, getResources().getString(R.string.info_permission_status, "denied"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}