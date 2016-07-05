package com.softdesign.devintensive.ui.view.behaviors;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.softdesign.devintensive.utils.ConstantManager;

public class LinearLayout_AppBarBehavior<LinearLayout extends View> extends CoordinatorLayout.Behavior<LinearLayout> {

    private final static String TAG = ConstantManager.TAG_PREFIX + "LLBehavior";
    private Context mContext;
    private float minLLSize, maxScrollDistance, minDependencyScrollY, maxLLSize, mExpandedPercentageFactor;


    public LinearLayout_AppBarBehavior(Context context, AttributeSet attrs) {
        this.mContext = context;
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, LinearLayout child) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState(parent, child));
        bundle.putFloat("mExpandedPercentageFactor", this.mExpandedPercentageFactor); // ... save stuff
        bundle.putFloat("minLLSize", this.minLLSize); // ... save stuff
        bundle.putFloat("maxLLSize", this.maxLLSize); // ... save stuff
        bundle.putFloat("maxScrollDistance", this.maxScrollDistance); // ... save stuff
        bundle.putFloat("minDependencyScrollY", this.minDependencyScrollY); // ... save stuff
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, LinearLayout child, Parcelable state) {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            this.mExpandedPercentageFactor = bundle.getFloat("mExpandedPercentageFactor");
            this.minLLSize = bundle.getFloat("minLLSize");
            this.maxLLSize = bundle.getFloat("maxLLSize");
            this.minDependencyScrollY = bundle.getFloat("minDependencyScrollY");
            this.maxScrollDistance = bundle.getFloat("maxScrollDistance");
            state = bundle.getParcelable("superState");
            final CoordinatorLayout.LayoutParams lp =
                    (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            lp.height = (int) Math.max(minLLSize, maxLLSize * mExpandedPercentageFactor);
            child.setLayoutParams(lp);
        }
        super.onRestoreInstanceState(parent, child, state);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {

        /*Log.d(TAG, "onDependentViewChanged " + child.getClass().getSimpleName() + " depends on " + dependency.getClass().getSimpleName());*/

        final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        AppBarLayout appBarLayout;
        if (dependency instanceof AppBarLayout) {
            appBarLayout = (AppBarLayout) dependency;
            if (lp.getAnchorId() != appBarLayout.getId()) {
                // The anchor ID doesn't match the dependency
                return false;
            }
        } else {
            return false;
        }
        if (minLLSize == 0.0f) {
            initProperties(parent, child, appBarLayout);
        }
        float curDependencyY = appBarLayout.getBottom() - minDependencyScrollY;
        mExpandedPercentageFactor = curDependencyY / maxScrollDistance;

        lp.height = (int) Math.max(minLLSize, maxLLSize * mExpandedPercentageFactor);
        child.setLayoutParams(lp);

/*        Log.d(TAG, " >>> mExpandedPercentageFactor " + mExpandedPercentageFactor + " maxScrollDistance " + maxScrollDistance + " curDependencyY " + curDependencyY + " min_child_height " + minLLSize + " cur_child_height " + lp.height);*/
        return true;
    }

    private void initProperties(CoordinatorLayout parent, LinearLayout child, AppBarLayout dependency) {  //расчет начальных параметров
        /*Log.d(TAG, "initProperties ");*/
        maxLLSize = child.getHeight();
        minLLSize = getHeight(child);  //найдет минимальную высоту child, которая полностью вместит его контент, т.е. высоту если будет wrap_content

        minDependencyScrollY = getStatusBarHeight() + getAppBarSize();
        maxScrollDistance = dependency.getHeight() - minDependencyScrollY;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private float getAppBarSize() {
        final TypedArray styledAttributes = mContext.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        float mActionBarSize = styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return mActionBarSize;
    }

    public static int getHeight(View v) {  //найдет минимальную высоту вьюхи, которая полностью вместит её контент, т.е. высоту если будет wrap_content
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(screenWidth(v.getContext()), View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthMeasureSpec, heightMeasureSpec);
        return v.getMeasuredHeight();
    }

    public static int screenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int deviceWidth;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }
        return deviceWidth;
    }
}
