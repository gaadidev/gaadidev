package com.gaadi.neon.util;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Lakshay
 * @since 12-03-2015.
 *
 */
public class ApplicationController extends Application {

    public static Context context;
    public static ArrayList<String> selectedFiles = new ArrayList<>();

    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static Context getContext() {
        if(context != null)  {
            context = getApplication().getApplicationContext();
        }
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }
}
