package com.mc10inc.biostamp3.minimalexample;

import android.app.Application;

import com.mc10inc.biostamp3.sdk.BioStampManager;

// Create our own subclass of Application so that we can initialize the BioStamp SDK at application
// launch. This class must be registered in the application's manifest.
public class MinimalExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the BioStamp SDK. This must be done once at application launch before calling
        // BioStampManager.getInstance() to access the SDK.
        BioStampManager.initialize(this);
    }
}
