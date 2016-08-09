package com.softdesign.devintensive.data.network;

import android.support.annotation.StringRes;

import com.softdesign.devintensive.utils.DevIntensiveApplication;

@SuppressWarnings("unused")
public class NetworkRequest {

    public enum Status {
        PENDING,
        RUNNING,
        FINISHED, Status,
    }

    public enum ID {
        DEFAULT,
        LIKE,
        UNLIKE,
        LOAD_DB,
        SILENT_AUTH,
        AUTH,
        FORGOT_PASS,
        UPLOAD_PHOTO,
        UPLOAD_AVATAR,
        UPLOAD_DATA,
        LOAD_USER_INFO,
    }

    private Status mStatus = Status.PENDING;
    private ID mId = ID.DEFAULT;
    private String mError = null;
    private boolean mCancelled = false;
    private boolean isAnnounceError = false;
    private boolean isErrorCritical = false;
    private Object mAdditionalInfo = null;

    public NetworkRequest() {
    }

    public NetworkRequest(ID id) {
        this.mId = id;
    }

    public NetworkRequest(ID id, Object additionalInfo) {
        mId = id;
        mAdditionalInfo = additionalInfo;
    }

    public NetworkRequest started() {
        this.mStatus = Status.RUNNING;
        this.mError = null;
        this.mCancelled = false;
        this.isAnnounceError = false;
        this.isErrorCritical = false;
        return this;
    }

    public NetworkRequest failed(@StringRes int error, boolean isAnnounceError, boolean isErrorCritical) {
        failed(DevIntensiveApplication.getContext().getString(error), isAnnounceError, isErrorCritical);
        return this;
    }

    public NetworkRequest failed(String error, boolean isAnnounceError, boolean isErrorCritical) {
        this.mStatus = Status.FINISHED;
        this.mError = error;
        this.mCancelled = true;
        this.isAnnounceError = isAnnounceError;
        this.isErrorCritical = isErrorCritical;
        return this;
    }

    public NetworkRequest critical(@StringRes int id) {
        this.mStatus = Status.FINISHED;
        this.mError = DevIntensiveApplication.getContext().getString(id);
        this.mCancelled = true;
        this.isAnnounceError = true;
        this.isErrorCritical = true;
        return this;
    }

    public NetworkRequest successful() {
        this.mStatus = Status.FINISHED;
        this.mError = null;
        this.mCancelled = false;
        this.isAnnounceError = false;
        this.isErrorCritical = false;
        return this;
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Getters

    public Object getAdditionalInfo() {
        return mAdditionalInfo;
    }

    public ID getId() {
        return mId;
    }

    public Status getStatus() {
        return mStatus;
    }

    public String getError() {
        return mError;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public boolean isAnnounceError() {
        return isAnnounceError;
    }

    public boolean isErrorCritical() {
        return isErrorCritical;
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: Getters

    //region :::::::::::::::::::::::::::::::::::::::::: Setters

    public void setAdditionalInfo(Object additionalInfo) {
        this.mAdditionalInfo = additionalInfo;
    }

    public void setId(ID id) {
        this.mId = id;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public void setError(String error) {
        mError = error;
    }

    public void setCancelled(boolean cancelled) {
        mCancelled = cancelled;
    }

    public void setAnnounceError(boolean announceError) {
        isAnnounceError = announceError;
    }

    public void setErrorCritical(boolean errorCritical) {
        isErrorCritical = errorCritical;
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: Setters

    @Override
    public String toString() {
        return "NetworkRequest{" +
                "mStatus=" + mStatus +
                ", mId=" + mId +
                ", mError='" + mError + '\'' +
                ", mCancelled=" + mCancelled +
                ", isAnnounceError=" + isAnnounceError +
                ", isErrorCritical=" + isErrorCritical +
                ", mAdditionalInfo class =" + (mAdditionalInfo != null ? mAdditionalInfo.getClass() : "") +
                ", mAdditionalInfo=" + mAdditionalInfo +
                '}';
    }
}
