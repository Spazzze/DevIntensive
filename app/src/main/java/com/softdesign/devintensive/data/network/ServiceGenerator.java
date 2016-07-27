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

    private static final OkHttpClient.Builder sHttpClient = new OkHttpClient.Builder();

    private static final Retrofit.Builder sBuilder = new Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit sRetrofit;

    public static Retrofit getRetrofit() {
        return sRetrofit;
    }

    public static <S> S createService(Class<S> serviceClass) {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        sHttpClient.addInterceptor(new HeaderInterceptor());
        sHttpClient.addInterceptor(loggingInterceptor);
        sHttpClient.connectTimeout(AppConfig.MAX_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        sHttpClient.readTimeout(AppConfig.MAX_READ_TIMEOUT, TimeUnit.MILLISECONDS);
        sHttpClient.writeTimeout(AppConfig.MAX_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
        sHttpClient.addNetworkInterceptor(new StethoInterceptor());

        sRetrofit = sBuilder
                .client(sHttpClient.build())
                .build();
        return sRetrofit.create(serviceClass);
    }
}

