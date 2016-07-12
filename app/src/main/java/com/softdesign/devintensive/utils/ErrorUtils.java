package com.softdesign.devintensive.utils;

import com.softdesign.devintensive.data.network.ServiceGenerator;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class ErrorUtils {

    public static class BackendHttpError {

        private String err;

        public BackendHttpError() {
        }

        public String getErrMessage() {
            return err;
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
            return new BackendHttpError();
        }

        return error;
    }
}