package com.softdesign.devintensive.utils;

import static com.softdesign.devintensive.ui.view.animations.Animations.AnimationType;

public interface AppConfig {
    //Database
    String DB_NAME = "devintensive-db";
    int DB_REFRESH_RATE = 5 * 60 * 1000;         // 5 min

    //Network timeouts
    long MAX_CONNECTION_TIMEOUT = 10000;
    long MAX_READ_TIMEOUT = 10000;
    long MAX_WRITE_TIMEOUT = 10000;

    //API
    String BASE_URL = "http://devintensive.softdesign-apps.ru/api/";

    //Picture cache
    int MAX_GLIDE_CACHE_SIZE = 1024 * 1024 * 500;   //500 Mb

    //Sign-in errors
    int MAX_LOGIN_TRIES = 3;
    int ERROR_FADE_TIME = 1000;
    int ERROR_VIBRATE_TIME = 500;

    //Splash screen fade-in delay
    int SPLASH_FADE_DELAY = 2000;

    //region :::::::::::::::::::::::::::::::::::::::::: Animations

    //Like button
    int ANIM_DURATION_BOUNCE_LIKE = 500;
    int ANIM_DURATION_ROTATE_UNLIKE = 700;
    int ANIM_DURATION_BOUNCE_UNLIKE = 400;
    float ANIM_HEART_BREAK_ANGLE = 130f;

    //Lists
    int RECYCLER_ANIM_DELAY = 300;

    //UserList
    int UL_ANIM_DURATION_TOOLBAR = 600;
    int UL_ANIM_START_DELAY_TOOLBAR = 0;
    int UL_ANIM_DURATION_FAB = 600;
    int UL_ANIM_START_DELAY_FAB = 800;

    AnimationType UL_MAIN_ANIMATION = AnimationType.SlideInUp;
    int UL_ANIM_DURATION_ITEM_ADD = 700;
    int UL_ANIM_DURATION_ITEM_REMOVE = 500;

    AnimationType UL_CONFIG_ANIMATION = AnimationType.OvershootInRight;
    int UL_ANIM_DURATION_CONF_ITEM_ADD = 500;
    int UL_ANIM_DURATION_CONF_ITEM_REMOVE = 100;

    //LikesList
    AnimationType LL_ANIMATION = AnimationType.FadeInUp;
    int LL_ANIM_DURATION_ITEM_ADD = 500;
    int LL_ANIM_DURATION_ITEM_REMOVE = 500;

    //LikesFooter
    AnimationType LF_ANIMATION = AnimationType.FadeInUp;
    int LF_ANIM_DURATION_ITEM_ADD = 700;
    int LF_ANIM_DURATION_ITEM_REMOVE = 500;

    //endregion :::::::::::::::::::::::::::::::::::::::::: Animations
}
