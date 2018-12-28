package com.example.developer001.greenzoneapplication.WifiPrinting;

import android.app.Application;

public class PrintDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initSingleton();
    }

    private void initSingleton(){
        ObservableSingleton.initInstance();
    }

}
