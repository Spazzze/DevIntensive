package com.softdesign.devintensive.data.network.api.req;

@SuppressWarnings("unused")
public class UserLoginReq {
    private final String email;
    private final String password;

    public UserLoginReq(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
