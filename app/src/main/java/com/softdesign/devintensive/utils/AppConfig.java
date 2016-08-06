package com.softdesign.devintensive.utils;

public interface AppConfig {
    //Database
    String DB_NAME = "devintensive-db";
    int DB_REFRESH_RATE = 5 * 60 * 1000;         // 5 min

    //API
    String BASE_URL = "http://devintensive.softdesign-apps.ru/api/";
    String FORGOT_PASS_URL = "http://devintensive.softdesign-apps.ru/forgotpass/";

    //Picture cache
    int MAX_GLIDE_CACHE_SIZE = 1024 * 1024 * 500;   //500 Mb

    //Sign-in errors
    int MAX_LOGIN_TRIES = 3;
    int ERROR_FADE_TIME = 1000;
    int ERROR_VIBRATE_TIME = 500;

    //Splash screen fade-in delay
    int SPLASH_FADE_DELAY = 2000;

    //Network timeouts
    long MAX_CONNECTION_TIMEOUT = 10000;
    long MAX_READ_TIMEOUT = 10000;
    long MAX_WRITE_TIMEOUT = 10000;

    //Visualizations
    int ANIM_DURATION_TOOLBAR = 400;
    int ANIM_DURATION_FAB = 500;
    long ANIM_START_DELAY_TOOLBAR = 300;
    long ANIM_START_DELAY_FAB = 300;

    int ANIM_DURATION_BOUNCE_LIKE = 500;

    int ANIM_DURATION_ROTATE_UNLIKE = 700;
    int ANIM_DURATION_BOUNCE_UNLIKE = 400;

    float ANIM_HEART_BREAK_ANGLE = 130f;

    int ANIM_DURATION_FAB_LIKE = 600;
}
