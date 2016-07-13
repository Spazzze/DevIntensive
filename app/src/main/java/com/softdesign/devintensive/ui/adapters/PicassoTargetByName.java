package com.softdesign.devintensive.ui.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.softdesign.devintensive.utils.UiHelper.createFile;


public class PicassoTargetByName implements Target {

    private File file;

    public PicassoTargetByName(String fileName) {
        try {
            file = createFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    public File getFile() {
        return file;
    }
}
