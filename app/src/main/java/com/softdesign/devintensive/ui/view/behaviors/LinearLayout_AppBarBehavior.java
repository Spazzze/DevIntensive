package com.softdesign.devintensive.ui.view.behaviors;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.softdesign.devintensive.utils.ConstantManager;

public class LinearLayout_AppBarBehavior<LinearLayout extends View> extends CoordinatorLayout.Behavior<LinearLayout> {

    private final static String TAG = ConstantManager.TAG_PREFIX + "LLBehavior";
    private Context mContext;
    private float minLLSize, maxScrollDistance, minDependencyScrollY;


    public LinearLayout_AppBarBehavior(Context context, AttributeSet attrs) {
        this.mContext = context;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {

/*        Log.d(TAG, "onDependentViewChanged " + child.getClass().getSimpleName() + " depends on " + dependency.getClass().getSimpleName());*/

        if (!dependency.getClass().getSimpleName().equals("AppBarLayout")) return false;

        if (minLLSize == 0) {
            initProperties(parent, child, dependency);
        }
        float curDependencyY = dependency.getBottom() - minDependencyScrollY;
        float expandedPercentageFactor = curDependencyY / maxScrollDistance;

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.height = (int) (minLLSize + minLLSize * expandedPercentageFactor);
        child.setLayoutParams(lp);

/*        Log.d(TAG, " >>> expandedPercentageFactor " + expandedPercentageFactor + " maxScrollDistance " + maxScrollDistance + " curDependencyY " + curDependencyY + " min_child_height " + minLLSize + " cur_child_height " + lp.height);*/
        return true;
    }

    private void initProperties(CoordinatorLayout parent, LinearLayout child, View dependency) {  //расчет начальных параметров

        minLLSize = getHeight(child);  //найдет минимальную высоту child, которая полностью вместит его контент, т.е. высоту если будет wrap_content

        minDependencyScrollY = getStatusBarHeight() + getActionBarSize();
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

    private float getActionBarSize() {
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
