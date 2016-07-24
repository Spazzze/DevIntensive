package com.softdesign.devintensive.utils;

import com.softdesign.devintensive.data.network.ServiceGenerator;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class ErrorUtils {
    /**
     * Converts received from server http error into human-readable error
     */
    @SuppressWarnings("unused")
    public static class BackendHttpError {

        private int statusCode;
        private String mErrorMessage;

        public BackendHttpError() {
        }

        public BackendHttpError(String message, int statusCode) {
            this.statusCode = statusCode;
            this.mErrorMessage = message;
        }

        public BackendHttpError(BackendHttpError convert, int statusCode) {
            this.statusCode = statusCode;
            this.mErrorMessage = convert.getErrorMessage();
        }

        public String getErrorMessage() {
            return this.mErrorMessage;
        }

        public int getStatusCode() {
            return this.statusCode;
        }
    }

    public static BackendHttpError parseHttpError(Response<?> response) {
        int errorCode = 0;
        if (!UiHelper.isEmptyOrNull(response)) errorCode = response.code();

        Converter<ResponseBody, BackendHttpError> converter =
                ServiceGenerator.getRetrofit()
                        .responseBodyConverter(BackendHttpError.class, new Annotation[0]);

        try {
            return new BackendHttpError(converter.convert(response.errorBody()), errorCode);
        } catch (IOException e) {
            return new BackendHttpError("Cannot convert error", errorCode);
        }
    }
}