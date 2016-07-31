package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

public class ClearGlideCacheOperation extends BaseChronosOperation<Void> {
    @Nullable
    @Override
    public Void run() {
        Glide.get(DevIntensiveApplication.getContext()).clearDiskCache();
        Glide.get(DevIntensiveApplication.getContext()).clearMemory();
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<Void>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<Void> {
    }
}
