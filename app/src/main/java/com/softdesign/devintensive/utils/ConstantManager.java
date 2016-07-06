package com.softdesign.devintensive.utils;

/**
 * Used to store all constants
 */
public interface ConstantManager {
    //String constants
    String TAG_PREFIX = "DEV ";

    //Shared preferences constants
    String USER_PHONE_KEY = "USER_PHONE_KEY";
    String USER_EMAIL_KEY = "USER_EMAIL_KEY";
    String USER_GITHUB_KEY = "USER_GITHUB_KEY";
    String USER_VK_KEY = "USER_VK_KEY";
    String USER_ABOUT_KEY = "USER_ABOUT_KEY";
    String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    String TOOLBAR_SCROLL_KEY = "TOOLBAR_SCROLL_KEY";
    String USER_PROFILE_PHOTO_URI = "USER_PROFILE_PHOTO_URI";

    //Dialog constants
    int LOAD_PROFILE_PHOTO = 1;

    //Request permission constants
    int REQUEST_PERMISSIONS_CAMERA = 100;
    int REQUEST_PERMISSIONS_CAMERA_SETTINGS = 101;
    int REQUEST_PERMISSIONS_READ_SDCARD = 102;
    int REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS = 103;

    //Other request constants
    int REQUEST_CAMERA_PICTURE = 99;
    int REQUEST_GALLERY_PICTURE = 98;

    //Timers constants
    int ET_ERROR_TIMER_LENGTH_LONG = 5000;
    int ET_ERROR_TIMER_LENGTH_NORMAL = 3000;
    int ET_ERROR_TIMER_LENGTH_SHORT = 1500;

}