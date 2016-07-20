package com.softdesign.devintensive.ui.view.elements;

import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.softdesign.devintensive.utils.Const;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.softdesign.devintensive.utils.UiHelper.createImageFromName;

/**
 * Target saves received bitmap into given file or creates file from filename and saves into it.
 */
@SuppressWarnings("unused")
public class GlideTargetIntoBitmap extends SimpleTarget<Bitmap> {

    private static final String TAG = Const.TAG_PREFIX + "GlideSTIB";
    private final File file;

    public GlideTargetIntoBitmap(File file) {
        super();
        this.file = file;
    }

    public GlideTargetIntoBitmap(int width, int height, File file) {
        super(width, height);
        this.file = file;
    }

    public GlideTargetIntoBitmap(int width, int height, String name) {
        super(width, height);
        file = createImageFromName(name);
    }

    @Override
    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "GlideTargetIntoBitmap onResourceReady: " + e.getMessage());
        }
    }

    public File getFile() {
        return file;
    }
}
