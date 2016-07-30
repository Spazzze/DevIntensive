package com.softdesign.devintensive.utils;

import okhttp3.MediaType;

/**
 * Used to store all constants
 */
@SuppressWarnings("unused")
public interface Const {
    //Debug TAG
    String TAG_PREFIX = "DEV ";

    //Shared preferences constants
    String MAP_KEY_GEN = "_KEY_";

    String USER_JSON_OBJ = "USER_JSON_OBJ";
    String USER_PROFILE_PHOTO_URI = "USER_PROFILE_PHOTO_URI";
    String USER_PROFILE_AVATAR_URI = "USER_PROFILE_AVATAR_URI";
    String VK_ACCESS_TOKEN = "VK_ACCESS_TOKEN";
    String BUILTIN_ACCESS_USER_ID = "BUILTIN_ACCESS_USER_ID";
    String BUILTIN_ACCESS_TOKEN = "BUILTIN_ACCESS_TOKEN";
    String SAVE_LOGIN = "SAVE_LOGIN";
    String SAVED_LOGIN_NAME = "SAVED_LOGIN_NAME";
    String DB_UPDATED_TIME_KEY = "DB_UPDATED_TIME";

    MediaType MEDIATYPE_MULTIPART_FORM_DATA = MediaType.parse("multipart/form-data");
    String DIALOG_FRAGMENT_KEY = "DIALOG_FRAGMENT_KEY";
    String DIALOG_CONTENT_KEY = "DIALOG_CONTENT_KEY";

    String OTHER_USER_PROFILE_KEY = "OTHER_USER_PROFILE_KEY";

    String PARCELABLE_KEY_NAV_VIEW = "PARCELABLE_KEY_NAV_VIEW";
    String PARCELABLE_KEY_PROFILE = "PARCELABLE_KEY_PROFILE";
    String PARCELABLE_KEY_AUTH = "PARCELABLE_KEY_AUTH";
    String PARCELABLE_KEY_USER_LIST = "PARCELABLE_KEY_USER_LIST";

    //Dialog constants
    int DIALOG_LOAD_PROFILE_PHOTO = 1;
    int DIALOG_SHOW_ERROR = 2;
    int DIALOG_SHOW_ERROR_RETURN_TO_MAIN = 3;
    int DIALOG_SHOW_ERROR_RETURN_TO_AUTH = 4;
    int DIALOG_LOAD_PROFILE_AVATAR = 5;

    //Request permission constants
    int REQUEST_PERMISSIONS_CAMERA = 1000;
    int REQUEST_PERMISSIONS_CAMERA_SETTINGS = 1001;
    int REQUEST_PERMISSIONS_READ_SDCARD = 1002;
    int REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS = 1003;

    //Other request constants
    int REQUEST_PHOTO_FROM_CAMERA = 999;
    int REQUEST_PHOTO_FROM_GALLERY = 998;
    int REQUEST_AVATAR_FROM_CAMERA = 997;
    int REQUEST_AVATAR_FROM_GALLERY = 996;

    //Some of http response constants
    int HTTP_RESPONSE_NOT_FOUND = 404;

    //Aspect Ratios
    float ASPECT_RATIO_16_9 = 1.78f;
    float ASPECT_RATIO_4_3 = 1.33f;
    float ASPECT_RATIO_3_2 = 1.5f;
    float ASPECT_RATIO_3_4 = 0.75f;
    float ASPECT_RATIO_2_3 = 0.67f;

    //Patterns
    String PATTERN_EMAIL = "^[\\w\\+\\.\\%\\-]{3,}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{1,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{1,25})+$";
    String PATTERN_VK_LINK = "^vk.com\\/\\w{3,}$";
    String PATTERN_GITHUB_LINK = "^github.com\\/\\w{3,}([\\/]\\w*)*$";
}