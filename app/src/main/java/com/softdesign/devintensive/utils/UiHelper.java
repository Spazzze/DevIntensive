package com.softdesign.devintensive.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.RepositoryEntity;
import com.softdesign.devintensive.data.storage.models.RepositoryEntityDao;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.models.UserEntityDao;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to work with UI
 */
@SuppressWarnings("unused")
public class UiHelper {

    private static final Context CONTEXT = DevIntensiveApplication.getContext();

    //region UI calculations

    /**
     * checks all args if they equals null or empty
     *
     * @param args array of args
     * @return true if null or empty
     */
    public static boolean isEmptyOrNull(Object... args) {
        for (Object s : args) {
            if (s == null || s.toString().trim().isEmpty())
                return true;
        }
        return false;
    }

    public static boolean isEmptyOrNull(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * @param context cur context
     * @return StatusBarHeight in current context
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * @param context cur context
     * @return Action bar height in current context
     */
    public static float getAppBarSize(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        float mActionBarSize = styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return mActionBarSize;
    }

    /**
     * @param v examined view
     * @return minimum view height which this view needs to wrap its content
     */
    public static int getMinHeight(View v) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthMeasureSpec, heightMeasureSpec);
        return v.getMeasuredHeight();
    }

    /**
     * @return current screen width
     */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int deviceWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }
        return deviceWidth;
    }

    /**
     * sets ListView Height Based On its Children
     *
     * @param listView to resize
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        View listItem = listAdapter.getView(0, null, listView);
        listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
        totalHeight = listItem.getMeasuredHeight() + (listItem.getMeasuredHeight() * (listAdapter.getCount() - 1));

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    //endregion

    //region IO system methods

    public static void fillDataBase(List<UserListRes> response) {

        RepositoryEntityDao repoDao = DataManager.getInstance().getDaoSession().getRepositoryEntityDao();
        UserEntityDao userDao = DataManager.getInstance().getDaoSession().getUserEntityDao();

        List<RepositoryEntity> allRepositories = new ArrayList<>();
        List<UserEntity> allUsers = new ArrayList<>();

        for (UserListRes user : response) {
            List<RepositoryEntity> l = user.getRepositories()
                    .getRepoEntitiesList(String.valueOf(user.getId()));
            allRepositories.addAll(l);
            allUsers.add(new UserEntity(user));
        }

        repoDao.insertOrReplaceInTx(allRepositories);
        userDao.insertOrReplaceInTx(allUsers);
    }

    /**
     * creates empty png file at SDCARD in folder Pictures with name IMG_yyyyMMdd_HHmmss_1238162378618.png
     *
     * @return file
     */
    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".png", storageDir);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        CONTEXT.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    /**
     * creates image file with given name -> name.png  at internal storage
     *
     * @param fileName name
     * @return file
     */
    public static File createImageFromName(String fileName) {
        File image = new File(CONTEXT.getFilesDir(), fileName + ".png");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        CONTEXT.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);

        return image;
    }

    /**
     * converts internal path like content:// into correct file path
     *
     * @param uri uri
     * @return correct filepath
     */
    public static String filePathFromUri(@NonNull Uri uri) {
        String filePath = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = CONTEXT.getContentResolver().query(uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                filePath = cursor.getString(0);
                cursor.close();
            }
        } else {
            filePath = uri.getPath();
        }
        return filePath;
    }
    //endregion

    //region Packages and Activities methods

    /**
     * checks if there some apps that can handle this implicit intent
     *
     * @param intent to check
     * @return true if there any
     */
    public static Boolean queryIntentActivities(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.SENDTO")) {
            ComponentName emailApp = intent.resolveActivity(context.getPackageManager());
            ComponentName unsupportedAction = ComponentName.unflattenFromString("com.android.fallback/.Fallback");
            return emailApp != null && !emailApp.equals(unsupportedAction);
        } else {
            PackageManager packageManager = context.getPackageManager();
            List activities = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return activities.size() > 0;
        }
    }

    /**
     * @param context     cur context
     * @param packageName package to check
     * @return true if packageName is installed
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void openApplicationSetting(Activity activity, int flag) {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(appSettingsIntent, flag);
    }

    //endregion

    //region Converters
    public static String getJsonFromObject(Object object, Class<?> typeClass) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(object, typeClass);
    }

    public static Object getObjectFromJson(String json, Class<?> typeClass) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(json, typeClass);
    }
    //endregion
}