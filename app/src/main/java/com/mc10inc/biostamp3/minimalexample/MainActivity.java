package com.mc10inc.biostamp3.minimalexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.mc10inc.biostamp3.minimalexample.databinding.ActivityMainBinding;
import com.mc10inc.biostamp3.sdk.BioStamp;
import com.mc10inc.biostamp3.sdk.BioStampManager;
import com.mc10inc.biostamp3.sdk.ScannedSensorStatus;

import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Code to identify the response to our permissions request in onPermissionsRequestResult
    private static final int PERMISSIONS_REQUEST_FOR_BLE = 1;

    private ActivityMainBinding binding;

    // Store the most recent scanning results
    private Map<String, ScannedSensorStatus> lastSensorsInRange = Collections.emptyMap();

    // Observer to receive scanning results
    @SuppressLint("DefaultLocale")
    private Observer<Map<String, ScannedSensorStatus>> sensorsInRangeObserver = sensorsInRange -> {
        // Store the scanning results so that we can show a list of sensors in a popup
        lastSensorsInRange = sensorsInRange;

        // Update the number of sensors in range in the UI
        binding.sensorsInRangeText.setText(
                String.format("Sensors in range: %d", lastSensorsInRange.size()));
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.scanButton.setOnClickListener(this::scanClicked);
        binding.connectButton.setOnClickListener(this::connectClicked);
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .create()
                .show();
    }

    private void scanClicked(View view) {
        // Before we can scan for sensors, we must check if our app has been granted the necessary
        // permissions by the end user, and request the permissions if not.
        if (BioStampManager.getInstance().hasPermissions()) {
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
                 showMessage("Permissions were not granted. Cannot scan for sensors.");
            }
        }
    }

    private void scanForSensors() {
        // Add an observer to receive scanning results. If our observer is already added, this has
        // no effect.
        BioStampManager.getInstance().getSensorsInRangeLiveData()
                .observe(this, sensorsInRangeObserver);
    }

    private void connectClicked(View v) {
        if (lastSensorsInRange.isEmpty()) {
            showMessage("There are no sensors in range");
            return;
        }

        // Get a list of all sensor serial numbers in range from the scanning results
        CharSequence[] serials = lastSensorsInRange.keySet().toArray(new CharSequence[0]);

        new AlertDialog.Builder(this)
                .setTitle("Select a BioStamp to connect to")
                .setItems(serials, (dialog, which) -> connectToSensor(serials[which].toString()))
                .create()
                .show();
    }

    private void connectToSensor(String serial) {
        // Get the BioStamp object representing the selected sensor
        BioStamp sensor = BioStampManager.getInstance().getBioStamp(serial);

        // Hide the connect button while connection is in progress
        binding.connectButton.setVisibility(View.INVISIBLE);

        // Start a connection attempt
        sensor.connect(new BioStamp.ConnectListener() {
            @Override
            public void connected() {
                blinkLed(sensor);
            }

            @Override
            public void connectFailed() {
                // Re-enable the connect button after the connection attempt fails.
                binding.connectButton.setVisibility(View.VISIBLE);

                showMessage("Failed to connect to sensor " + serial);
            }

            @Override
            public void disconnected() {
                // Re-enable the connect button once we are no longer connected.
                binding.connectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void blinkLed(BioStamp sensor) {
        // Send the blink LED command. This method returns immediately, and the listener will be
        // called once the command completes.
        sensor.blinkLed((error, result) -> {
            if (error != null) {
                // Non-null error means that executing the command failed
                showMessage("Failed to blink LED: " + error.toString());
            }

            // Now that we are done blinking the LED, disconnect.
            sensor.disconnect();
        });
    }
}
