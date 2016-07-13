package com.softdesign.devintensive.utils;


public interface AppConfig {
    String BASE_URL = "http://devintensive.softdesign-apps.ru/api/";
    String FORGOT_PASS_URL = "http://devintensive.softdesign-apps.ru/forgotpass/";

    int MAX_LOGIN_TRIES = 3;
    int ERROR_VIBRATE_TIME = 500;
    int REFRESH_DELAY = 500;

    String PATTERN_EMAIL = "^[\\w\\+\\.\\%\\-]{3,}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{1,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{1,25})+$";
    String PATTERN_VK_LINK = "^vk.com\\/\\w{3,}$";
    String PATTERN_GITHUB_LINK = "^github.com\\/\\w{3,}([\\/]\\w*)*$";

}
