package com.customise.gaadi.camera;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

/**
 * @author lakshaygirdhar
 * @version 1.0
 * @since 27/9/16
 */

public class ApplicationController extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
//        MultiDex.install(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
