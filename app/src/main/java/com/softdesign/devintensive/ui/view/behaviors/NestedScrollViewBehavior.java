package com.softdesign.devintensive.ui.view.behaviors;


import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.softdesign.devintensive.utils.ConstantManager;

public class NestedScrollViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    private final static String TAG = ConstantManager.TAG_PREFIX + "NSVBehavior";
    private Context mContext;


    public NestedScrollViewBehavior(Context context, AttributeSet attrs) {
        this.mContext = context;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        child.setY(dependency.getBottom());
        return super.onDependentViewChanged(parent, child, dependency);
    }
}
