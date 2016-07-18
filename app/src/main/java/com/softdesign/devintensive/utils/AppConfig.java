package com.softdesign.devintensive.utils;

public interface AppConfig {
    String DB_NAME = "devintensive-db";
    String BASE_URL = "http://devintensive.softdesign-apps.ru/api/";
    String FORGOT_PASS_URL = "http://devintensive.softdesign-apps.ru/forgotpass/";

    int MAX_LOGIN_TRIES = 3;
    int ERROR_VIBRATE_TIME = 500;

    long MAX_CONNECTION_TIMEOUT = 10000;
    long MAX_READ_TIMEOUT = 10000;
    long MAX_WRITE_TIMEOUT = 10000;
}
