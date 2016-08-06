package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.softdesign.devintensive.R;

import static com.softdesign.devintensive.utils.AppUtils.getAppBarSize;
import static com.softdesign.devintensive.utils.AppUtils.getStatusBarHeight;
import static com.softdesign.devintensive.utils.AppUtils.getViewMinHeight;

public class CustomLinearLayoutBehavior<LinearLayout extends View> extends CoordinatorLayout.Behavior<LinearLayout> {

    private float mMinLLSize, mMaxLLSize, mMinAppbarHeight, mMaxAppbarHeight, mExpandedPercentageFactor;

    public CustomLinearLayoutBehavior(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomLinearLayoutBehavior);
        mMinLLSize = a.getDimensionPixelSize(R.styleable.CustomLinearLayoutBehavior_min_collapsed_height, 0);
        mMaxLLSize = a.getDimensionPixelSize(R.styleable.CustomLinearLayoutBehavior_max_collapsed_height, 0);
        a.recycle();
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, LinearLayout child) {
        Bundle bundle = new Bundle();
        bundle.putFloat("mMinLLSize", this.mMinLLSize);
        bundle.putFloat("mMaxLLSize", this.mMaxLLSize);
        bundle.putFloat("mExpandedPercentageFactor", this.mExpandedPercentageFactor);
        bundle.putFloat("mMaxAppbarHeight", this.mMaxAppbarHeight);
        bundle.putFloat("mMinAppbarHeight", this.mMinAppbarHeight);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, LinearLayout child, Parcelable state) {
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
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) child.getLayoutParams();

        if (mMinAppbarHeight == 0.0f) {
            initProperties(child, dependency);
        }

        float curAppBarHeight = dependency.getBottom() - mMinAppbarHeight;
        mExpandedPercentageFactor = curAppBarHeight / mMaxAppbarHeight;
        lp.height = (int) (mMinLLSize + (mMaxLLSize - mMinLLSize) * mExpandedPercentageFactor);

        child.setLayoutParams(lp);

        return super.onDependentViewChanged(parent, child, dependency);
    }

    private void initProperties(View child, View dependency) {  //расчет начальных параметров
        if (mMaxLLSize == 0.0f) mMaxLLSize = child.getHeight();
        if (mMinLLSize == 0.0f) mMinLLSize = getViewMinHeight(child);
        mMinAppbarHeight = getStatusBarHeight() + getAppBarSize();
        mMaxAppbarHeight = dependency.getHeight() - mMinAppbarHeight;
    }
}
