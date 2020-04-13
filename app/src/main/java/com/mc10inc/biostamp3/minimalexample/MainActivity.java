package com.mc10inc.biostamp3.minimalexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.mc10inc.biostamp3.minimalexample.databinding.ActivityMainBinding;
import com.mc10inc.biostamp3.sdk.BioStampManager;

public class MainActivity extends AppCompatActivity {
    // Code to identify the response to our permissions request in onPermissionsRequestResult
    private static final int PERMISSIONS_REQUEST_FOR_BLE = 1;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.scanButton.setOnClickListener(this::scanClicked);
    }

    private void scanClicked(View view) {
        // Before we can scan for sensors, we must check if our app has been granted the necessary
        // permissions by the end user, and request the permissions if not.
        BioStampManager bs = BioStampManager.getInstance();
        if (bs.hasPermissions()) {
            scanForSensors();
        } else {
            requestBlePermissions();
        }
    }

    public void requestBlePermissions() {
        // Request the permissions that are needed to scan for sensors. The user will see a popup
        // asking them to grant the permissions, and then Android will call
        // onRequestPermissionsResult with the result.
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSIONS_REQUEST_FOR_BLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_FOR_BLE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The user granted the permissions; now we can start scanning.
                scanForSensors();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("Permissions were not granted. Cannot scan for sensors.")
                        .create()
                        .show();
            }
        }
    }

    private void scanForSensors() {

    }
}
