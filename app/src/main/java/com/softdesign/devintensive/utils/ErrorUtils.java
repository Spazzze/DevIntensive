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
        private String err;

        public BackendHttpError() {
        }

        public BackendHttpError(int statusCode, String message) {
            this.statusCode = statusCode;
            this.err = message;
        }

        public String getErrorMessage() {
            return this.err;
        }

        public int getStatusCode() {
            return this.statusCode;
        }
    }

    public static BackendHttpError parseHttpError(Response<?> response) {
        Converter<ResponseBody, BackendHttpError> converter =
                ServiceGenerator.getRetrofit()
                        .responseBodyConverter(BackendHttpError.class, new Annotation[0]);

        BackendHttpError error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new BackendHttpError(0, "Cannot convert error");
        }

        return error;
    }
}