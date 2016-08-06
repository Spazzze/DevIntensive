package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

public class CustomNestedScrollViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    public CustomNestedScrollViewBehavior(Context context, AttributeSet attrs) {
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        child.setY(dependency.getBottom());
        return super.onDependentViewChanged(parent, child, dependency);
    }
}