package com.softdesign.devintensive.data.providers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;
import com.softdesign.devintensive.ui.view.elements.CropCircleTransformation;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

@SuppressWarnings({"unused", "SameParameterValue"})
public class CustomGlideModule implements GlideModule {

    private static final Context CONTEXT = DevIntensiveApplication.getContext();
    private static final String TAG = Const.TAG_PREFIX + "CustomGlideModule";

    //region :::::::::::::::::::::::::::::::::::::::::: Module
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
        int customBitmapPoolSize = 2 * defaultBitmapPoolSize;
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));

        builder.setDiskCache(
                new DiskLruCacheFactory(DevIntensiveApplication
                        .getContext()
                        .getCacheDir()
                        .getPath(),
                        AppConfig.MAX_GLIDE_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Utils

    public static void loadImage(String path, Drawable placeholder, Drawable error, ImageView target) {
        if (AppUtils.isEmptyOrNull(placeholder, error, target)) {
            Log.e(TAG, "loadImage: Some of arguments is null or empty.");
            return;
        }

        String pathToPhoto = "null";
        if (!AppUtils.isEmptyOrNull(path)) pathToPhoto = path;

        Glide.with(CONTEXT)
                .load(pathToPhoto)
                .error(error)
                .placeholder(placeholder)
                .centerCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(target);
    }

    public static void loadImage(final String path, final int placeholder,
                                 final int error, final ImageView target) {
        if (AppUtils.isEmptyOrNull(placeholder, error, target)) {
            Log.e(TAG, "loadImage: Some of arguments is null or empty.");
            return;
        }

        String pathToPhoto = "null";
        if (!AppUtils.isEmptyOrNull(path)) pathToPhoto = path;

        Glide.with(CONTEXT)
                .load(pathToPhoto)
                .error(error)
                .placeholder(placeholder)
                .centerCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(target);
    }

    public static void loadRoundedImage(final String path, final int placeholder,
                                        final int error, final ImageView target) {

        if (AppUtils.isEmptyOrNull(placeholder, error, target)) {
            Log.e(TAG, "loadImage: Some of arguments is null or empty.");
            return;
        }

        String pathToPhoto = "null";
        if (!AppUtils.isEmptyOrNull(path)) pathToPhoto = path;

        Glide.with(CONTEXT)
                .load(pathToPhoto)
                .error(error)
                .placeholder(placeholder)
                .centerCrop()
                .bitmapTransform(new CropCircleTransformation(CONTEXT))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .crossFade()
                .into(target);
    }

    public static Bitmap downloadBitmap(String path, int width, int height) {
        if (AppUtils.isEmptyOrNull(path)) {
            Log.e(TAG, "downloadBitmap: path is null or empty.");
            return null;
        }

        try {
            return Glide.with(CONTEXT).
                    load(path).
                    asBitmap().
                    into(width, height).
                    get();
        } catch (Exception ignored) {
            return null;
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
