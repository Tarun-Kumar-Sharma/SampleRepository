package com.example.i2e1.sampleapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PermissionSampleActivity extends AppCompatActivity {

    private boolean isFirst = true;

    public static final String[] WIFI_PERMISSIONS = new String[]{
            /*Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,*/
            // location permission is required by some android versions while scanning available wifi
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            /*Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.WRITE_SETTINGS,*/
            /*Manifest.permission.WAKE_LOCK*/
    };

    private static final int REQUEST_PERMISSIONS = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_sample);

        findViewById(R.id.btnCheckPermission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryPermissionFlow();
            }
        });
        /*retryPermissionFlow();*/
    }

    private void retryPermissionFlow() {
        if(checkPermissions(PermissionSampleActivity.this, WIFI_PERMISSIONS)) {
            Toast.makeText(this, "Permission Already granted", Toast.LENGTH_SHORT).show();
        } else {
            boolean canRequestPermissions = true;
            if (!isFirst) {
                for (int i = 0; i < WIFI_PERMISSIONS.length; i++) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            WIFI_PERMISSIONS[i])) {
                        canRequestPermissions = false;

                        showExplanation("Permission Needed", "Application require permission to perform wifi actions");
                        break;
                    }
                }
            }
            if (canRequestPermissions) {
                isFirst = false;
                ActivityCompat.requestPermissions(PermissionSampleActivity.this, WIFI_PERMISSIONS, REQUEST_PERMISSIONS);
            }
        }
    }

    public static boolean checkPermissions(Context context, String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void showExplanation(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveToAppSettings();
                    }
                });
        builder.create().show();
    }

    private void moveToAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
