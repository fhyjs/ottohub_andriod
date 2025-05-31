package org.eu.hanana.reimu.ottohub_andriod;

import android.app.Application;

import org.eu.hanana.reimu.lib.ottohub.api.OttohubApi;

// MyApp.java
public class MyApp extends Application {
    private static MyApp instance;
    private OttohubApi ottohubApi;

    public OttohubApi getOttohubApi() {
        return ottohubApi;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ottohubApi=new OttohubApi();
    }

    public static MyApp getInstance() {
        return instance;
    }


}