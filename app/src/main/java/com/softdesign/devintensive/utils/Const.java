package com.softdesign.devintensive.utils;

import okhttp3.MediaType;

/**
 * Used to store all constants
 */
@SuppressWarnings("unused")
public interface Const {
    //Debug TAG
    String TAG_PREFIX = "DEV ";

    //region Shared preferences constants
    String USER_JSON_OBJ = "USER_JSON_OBJ";
    String USER_PROFILE_PHOTO_URI = "USER_PROFILE_PHOTO_URI";
    String USER_PROFILE_AVATAR_URI = "USER_PROFILE_AVATAR_URI";
    String VK_ACCESS_TOKEN = "VK_ACCESS_TOKEN";
    String BUILTIN_ACCESS_USER_ID = "BUILTIN_ACCESS_USER_ID";
    String BUILTIN_ACCESS_TOKEN = "BUILTIN_ACCESS_TOKEN";
    String SAVE_LOGIN = "SAVE_LOGIN";
    String SAVED_LOGIN_NAME = "SAVED_LOGIN_NAME";
    String DB_UPDATED_TIME_KEY = "DB_UPDATED_TIME";
    //endregion

    //region Other
    MediaType MEDIATYPE_MULTIPART_FORM_DATA = MediaType.parse("multipart/form-data");
    String DIALOG_FRAGMENT_KEY = "DIALOG_FRAGMENT_KEY";
    String DIALOG_CONTENT_KEY = "DIALOG_CONTENT_KEY";

    String PARCELABLE_KEY_NAV_VIEW = "PARCELABLE_KEY_NAV_VIEW";
    String PARCELABLE_KEY_PROFILE = "PARCELABLE_KEY_PROFILE";
    String PARCELABLE_KEY_LIKES = "PARCELABLE_KEY_LIKES";
    String PARCELABLE_KEY_LIKES_MODEL = "PARCELABLE_KEY_LIKES_MODEL";
    String PARCELABLE_KEY_AUTH = "PARCELABLE_KEY_AUTH";
    String PARCELABLE_KEY_USER_LIST = "PARCELABLE_KEY_USER_LIST";
    String PARCELABLE_KEY_USER_LIST_SORT = "PARCELABLE_KEY_USER_LIST_SORT";
    String PARCELABLE_KEY_USER_ID = "PARCELABLE_KEY_USER_ID";

    String MAP_KEY_GEN = "_KEY_";

    String ACTION_LIKE_BUTTON_CLICKED = "ACTION_LIKE_BUTTON_CLICKED";
    String ACTION_LIKE_COUNTER_CHANGED = "ACTION_LIKE_COUNTER_CHANGED";
    //endregion

    //region Dialog constants
    int DIALOG_LOAD_PROFILE_PHOTO = 1;
    int DIALOG_SHOW_ERROR = 2;
    int DIALOG_SHOW_ERROR_RETURN_TO_MAIN = 3;
    int DIALOG_SHOW_ERROR_RETURN_TO_AUTH = 4;
    int DIALOG_LOAD_PROFILE_AVATAR = 5;
    //endregion

    //region Request constants
    int REQUEST_PERMISSIONS_CAMERA = 1000;
    int REQUEST_PERMISSIONS_CAMERA_SETTINGS = 1001;
    int REQUEST_PERMISSIONS_READ_SDCARD = 1002;
    int REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS = 1003;

    int REQUEST_PHOTO_FROM_CAMERA = 999;
    int REQUEST_PHOTO_FROM_GALLERY = 998;
    int REQUEST_AVATAR_FROM_CAMERA = 997;
    int REQUEST_AVATAR_FROM_GALLERY = 996;

    int NETWORK_REQUEST_DEFAULT = 2000;
    int NETWORK_REQUEST_LIKE = 2001;
    int NETWORK_REQUEST_LOAD_DB = 2002;
    int NETWORK_REQUEST_SILENT_AUTH = 2003;
    int NETWORK_REQUEST_AUTH = 2004;
    int NETWORK_REQUEST_UPLOAD_PHOTO = 2005;
    int NETWORK_REQUEST_UPLOAD_AVATAR = 2006;
    int NETWORK_REQUEST_UPLOAD_DATA = 2007;
    //endregion

    //Some of http response constants
    int HTTP_RESPONSE_NOT_FOUND = 404;
    int HTTP_UNAUTHORIZED = 401;
    int HTTP_FORBIDDEN = 403;

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