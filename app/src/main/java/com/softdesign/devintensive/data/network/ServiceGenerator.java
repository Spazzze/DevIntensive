package com.softdesign.devintensive.data.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.softdesign.devintensive.data.network.interceptors.HeaderInterceptor;
import com.softdesign.devintensive.utils.AppConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static final OkHttpClient.Builder HTTP_CLIENT = new OkHttpClient.Builder();
    private static final Retrofit.Builder RF_BUILDER = new Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit sRetrofit;

    public static <S> S createService(Class<S> serviceClass) {

        HTTP_CLIENT.addInterceptor(new HeaderInterceptor());
        HTTP_CLIENT.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        HTTP_CLIENT.connectTimeout(AppConfig.MAX_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        HTTP_CLIENT.readTimeout(AppConfig.MAX_READ_TIMEOUT, TimeUnit.MILLISECONDS);
        HTTP_CLIENT.writeTimeout(AppConfig.MAX_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
        HTTP_CLIENT.addNetworkInterceptor(new StethoInterceptor());

        sRetrofit = RF_BUILDER
                .client(HTTP_CLIENT.build())
                .build();

        return sRetrofit.create(serviceClass);
    }

    public static Retrofit getRetrofit() {
        return sRetrofit;
    }
}