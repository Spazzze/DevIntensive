package com.softdesign.devintensive.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    private static final Context CONTEXT = DevIntensiveApplication.getContext();
    /**
     * Checks if network is available at this time
     */
    public static Boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
