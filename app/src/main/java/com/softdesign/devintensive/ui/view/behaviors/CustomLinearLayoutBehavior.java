package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.AppUtils;

import static com.softdesign.devintensive.utils.AppUtils.getAppBarSize;
import static com.softdesign.devintensive.utils.AppUtils.getStatusBarHeight;

public class CustomLinearLayoutBehavior extends AppBarLayout.ScrollingViewBehavior {

    private float mMinLLSize;
    private float mMaxLLSize;
    private float mMinAppbarHeight;
    private float mMaxAppbarHeight;

    @SuppressWarnings({"unused"})
    public CustomLinearLayoutBehavior(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomLinearLayoutBehavior);
        mMinLLSize = a.getDimensionPixelSize(R.styleable.CustomLinearLayoutBehavior_min_collapsed_height, 0);
        mMaxLLSize = a.getDimensionPixelSize(R.styleable.CustomLinearLayoutBehavior_max_collapsed_height, 0);
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

        if (mMinAppbarHeight == 0.0f) {
            initProperties(child, dependency);
        }

        float curAppBarHeight = dependency.getBottom() - mMinAppbarHeight;
        float expandedPercentageFactor = curAppBarHeight / mMaxAppbarHeight;
        lp.height = (int) (mMinLLSize + (mMaxLLSize - mMinLLSize) * expandedPercentageFactor);

        child.setLayoutParams(lp);

        return super.onDependentViewChanged(parent, child, dependency);
    }

    private void initProperties(View child, View dependency) {  //расчет начальных параметров
        if (mMaxLLSize == 0.0f) mMaxLLSize = child.getHeight();
        if (mMinLLSize == 0.0f) mMinLLSize = AppUtils.getViewMinHeight(child);
        mMinAppbarHeight = getStatusBarHeight() + getAppBarSize();
        mMaxAppbarHeight = dependency.getHeight() - mMinAppbarHeight;
    }
}