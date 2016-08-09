package com.softdesign.devintensive.data.network.api.req;

public class UserRestorePassReq {
    private final String email;

    public UserRestorePassReq(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
