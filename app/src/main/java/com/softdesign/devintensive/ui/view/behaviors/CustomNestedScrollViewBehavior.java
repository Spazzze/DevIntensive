package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.softdesign.devintensive.utils.ConstantManager;

/**
 * Behavior to link Nested scroll to bottom edge of LinearLayout
 */
class CustomNestedScrollViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    private final static String TAG = ConstantManager.TAG_PREFIX + "NSVBehavior";

    public CustomNestedScrollViewBehavior(Context context, AttributeSet attrs) {
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof LinearLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        LinearLayout linearLayout;
        if (dependency instanceof LinearLayout) {
            linearLayout = (LinearLayout) dependency;
            if (lp.getAnchorId() != -1 && lp.getAnchorId() != linearLayout.getId()) {
                // The anchor ID doesn't match the dependency
                return false;
            }
        } else {
            return false;
        }
        lp.topMargin = dependency.getBottom();
        child.setLayoutParams(lp);
        return super.onDependentViewChanged(parent, child, dependency);
    }
}