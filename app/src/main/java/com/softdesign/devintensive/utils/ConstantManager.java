package com.softdesign.devintensive.utils;

/**
 * Used to store all constants
 */
public interface ConstantManager {
    //String constants
    String TAG_PREFIX = "DEV ";

    //Shared preferences constants
    String USER_JSON_OBJ = "USER_JSON_OBJ";
    String EDIT_MODE_KEY = "EDIT_MODE_KEY";
    String USER_PROFILE_PHOTO_URI = "USER_PROFILE_PHOTO_URI";
    String USER_PROFILE_AVATAR_URI = "USER_PROFILE_AVATAR_URI";

    String AUTHORIZATION_SYSTEM = "AUTHORIZATION_SYSTEM";
    String VK_ACCESS_TOKEN = "VK_ACCESS_TOKEN";
    String GOOGLE_ACCESS_ACC_NAME = "GOOGLE_ACCESS_ACC_NAME";
    String GOOGLE_ACCESS_ACC_TYPE = "GOOGLE_ACCESS_ACC_TYPE";
    String GOOGLE_ACCESS_TOKEN = "GOOGLE_ACCESS_TOKEN";
    String BUILTIN_ACCESS_ID = "BUILTIN_ACCESS_ID";
    String BUILTIN_ACCESS_TOKEN = "BUILTIN_ACCESS_TOKEN";
    String SAVE_LOGIN = "SAVE_LOGIN";
    String SAVED_LOGIN_NAME = "SAVED_LOGIN_NAME";


    //Authorization systems
    String AUTH_VK = "AUTH_VK";
    String AUTH_FACEBOOK = "AUTH_FACEBOOK";
    String AUTH_GOOGLE = "AUTH_GOOGLE";
    String AUTH_BUILTIN = "AUTH_BUILTIN";

    //Dialog constants
    int LOAD_PROFILE_PHOTO = 1;

    //Request permission constants
    int REQUEST_PERMISSIONS_CAMERA = 100;
    int REQUEST_PERMISSIONS_CAMERA_SETTINGS = 101;
    int REQUEST_PERMISSIONS_READ_SDCARD = 102;
    int REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS = 103;
    int REQUEST_PERMISSIONS_GET_ACCOUNTS = 104;

    //Other request constants
    int REQUEST_CAMERA_PICTURE = 99;
    int REQUEST_GALLERY_PICTURE = 98;
    int REQUEST_GOOGLE_SIGN_IN = 97;

    //Google scopes
    String G_PLUS_SCOPE = "oauth2:https://www.googleapis.com/auth/plus.me";
    String USER_INFO_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    //Some of http response constants
    int HTTP_RESPONSE_NOT_FOUND = 404;

}