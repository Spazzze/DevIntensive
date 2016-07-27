package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.softdesign.devintensive.R;

import static com.softdesign.devintensive.utils.AppUtils.getAppBarSize;
import static com.softdesign.devintensive.utils.AppUtils.getViewMinHeight;
import static com.softdesign.devintensive.utils.AppUtils.getStatusBarHeight;

/**
 * Behavior to link LinearLayout to AppBarLayout's bottom edge
 *
 * @param <LinearLayout> to link to.
 */
@SuppressWarnings("unused")
class CustomLinearLayoutBehavior<LinearLayout extends View> extends AppBarLayout.ScrollingViewBehavior {

    private float mMinLLSize, mMaxLLSize, mMinAppbarHeight, mMaxAppbarHeight, mExpandedPercentageFactor;

    public CustomLinearLayoutBehavior(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomLinearLayoutBehavior);
        mMinLLSize = a.getDimensionPixelSize(R.styleable.CustomLinearLayoutBehavior_min_collapsed_height, 0);
        a.recycle();
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, View child) {
        Bundle bundle = new Bundle();
        bundle.putFloat("mMinLLSize", this.mMinLLSize);
        bundle.putFloat("mMaxLLSize", this.mMaxLLSize);
        bundle.putFloat("mExpandedPercentageFactor", this.mExpandedPercentageFactor);
        bundle.putFloat("mMaxAppbarHeight", this.mMaxAppbarHeight);
        bundle.putFloat("mMinAppbarHeight", this.mMinAppbarHeight);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, View child, Parcelable state) {
        if (!(state instanceof Bundle)) return; // implicit null check

        Bundle bundle = (Bundle) state;
        this.mExpandedPercentageFactor = bundle.getFloat("mExpandedPercentageFactor");
        this.mMinLLSize = bundle.getFloat("mMinLLSize");
        this.mMaxLLSize = bundle.getFloat("mMaxLLSize");
        this.mMinAppbarHeight = bundle.getFloat("mMinAppbarHeight");
        this.mMaxAppbarHeight = bundle.getFloat("mMaxAppbarHeight");
        final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.height = (int) (mMinLLSize + (mMaxLLSize - mMinLLSize) * mExpandedPercentageFactor);
        child.setLayoutParams(lp);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

        AppBarLayout appBarLayout;
        if (dependency instanceof AppBarLayout) {
            appBarLayout = (AppBarLayout) dependency;
            if (lp.getAnchorId() != -1 && lp.getAnchorId() != appBarLayout.getId()) {
                // The anchor ID doesn't match the dependency
                return false;
            }
        } else {
            return false;
        }

        if (mMaxLLSize == 0.0f) {
            initProperties(child, appBarLayout);
        }

        float curAppBarHeight = appBarLayout.getBottom() - mMinAppbarHeight;
        mExpandedPercentageFactor = curAppBarHeight / mMaxAppbarHeight;
        lp.height = (int) (mMinLLSize + (mMaxLLSize - mMinLLSize) * mExpandedPercentageFactor);

        child.setLayoutParams(lp);

        return super.onDependentViewChanged(parent, child, dependency);
    }

    private void initProperties(View child, AppBarLayout dependency) {  //расчет начальных параметров
        mMaxLLSize = child.getHeight();
        if (mMinLLSize == 0.0f) mMinLLSize = getViewMinHeight(child);
        mMinAppbarHeight = getStatusBarHeight() + getAppBarSize();
        mMaxAppbarHeight = dependency.getHeight() - mMinAppbarHeight;
    }
}