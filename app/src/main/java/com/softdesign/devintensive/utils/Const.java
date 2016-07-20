package com.softdesign.devintensive.utils;

import okhttp3.MediaType;

/**
 * Used to store all constants
 */
@SuppressWarnings("unused")
public interface Const {
    //String constants
    String TAG_PREFIX = "DEV ";

    //Shared preferences constants
    String USER_JSON_OBJ = "USER_JSON_OBJ";
    String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    String USER_PROFILE_PHOTO_URI = "USER_PROFILE_PHOTO_URI";
    String USER_PROFILE_AVATAR_URI = "USER_PROFILE_AVATAR_URI";
    String PARCELABLE_KEY = "PARCELABLE_KEY";
    String DIALOG_FRAGMENT_KEY = "DIALOG_FRAGMENT_KEY";
    String DIALOG_CONTENT_KEY = "DIALOG_CONTENT_KEY";

    String VK_ACCESS_TOKEN = "VK_ACCESS_TOKEN";
    String BUILTIN_ACCESS_USER_ID = "BUILTIN_ACCESS_USER_ID";
    String BUILTIN_ACCESS_TOKEN = "BUILTIN_ACCESS_TOKEN";
    String SAVE_LOGIN = "SAVE_LOGIN";
    String SAVED_LOGIN_NAME = "SAVED_LOGIN_NAME";

    String DB_UPDATED_TIME_KEY = "DB_UPDATED_TIME";

    MediaType MEDIA_TYPE_MULTIPART_FORM_DATA = MediaType.parse("multipart/form-data");

    //Data methods keys
    int SAVE_LOGIN_NAME = 999;

    //Dialog constants
    int DIALOG_LOAD_PROFILE_PHOTO = 1;
    int DIALOG_SHOW_ERROR = 2;

    //Request permission constants
    int REQUEST_PERMISSIONS_CAMERA = 100;
    int REQUEST_PERMISSIONS_CAMERA_SETTINGS = 101;
    int REQUEST_PERMISSIONS_READ_SDCARD = 102;
    int REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS = 103;

    //Other request constants
    int REQUEST_CAMERA_PICTURE = 99;
    int REQUEST_GALLERY_PICTURE = 98;

    //Some of http response constants
    int HTTP_RESPONSE_NOT_FOUND = 404;

    //Aspect Ratios
    float ASPECT_RATIO_16_9 = 1.78f;
    float ASPECT_RATIO_4_3 = 1.33f;
    float ASPECT_RATIO_3_2 = 1.5f;
    float ASPECT_RATIO_3_4 = 0.75f;
    float ASPECT_RATIO_2_3 = 0.67f;

    //Fragments
    String TAG_USER_LIST_TASK_FRAGMENT = "TAG_USER_LIST_TASK_FRAGMENT";
    String TAG_USER_UPDATE_TASK_FRAGMENT = "TAG_USER_UPDATE_TASK_FRAGMENT";

    //Patterns
    String PATTERN_EMAIL = "^[\\w\\+\\.\\%\\-]{3,}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{1,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{1,25})+$";
    String PATTERN_VK_LINK = "^vk.com\\/\\w{3,}$";
    String PATTERN_GITHUB_LINK = "^github.com\\/\\w{3,}([\\/]\\w*)*$";
}