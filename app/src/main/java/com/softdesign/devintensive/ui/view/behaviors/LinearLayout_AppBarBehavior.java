package com.softdesign.devintensive.ui.view.behaviors;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.softdesign.devintensive.utils.ConstantManager;

import static com.softdesign.devintensive.utils.UiHelper.getAppBarSize;
import static com.softdesign.devintensive.utils.UiHelper.getHeight;
import static com.softdesign.devintensive.utils.UiHelper.getStatusBarHeight;

/**
 * Behavior to link LinearLayout to AppBarLayout's bottom edge
 *
 * @param <LinearLayout> to link to.
 */
class LinearLayout_AppBarBehavior<LinearLayout extends View> extends CoordinatorLayout.Behavior<LinearLayout> {

    private final static String TAG = ConstantManager.TAG_PREFIX + "LLBehavior";
    private final Context mContext;
    private float minLLSize, maxScrollDistance, minDependencyScrollY, maxLLSize, mExpandedPercentageFactor;

    public LinearLayout_AppBarBehavior(Context context, AttributeSet attrs) {
        this.mContext = context;
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, LinearLayout child) {
        Bundle bundle = new Bundle();
        bundle.putFloat("mExpandedPercentageFactor", this.mExpandedPercentageFactor);
        bundle.putFloat("minLLSize", this.minLLSize);
        bundle.putFloat("maxLLSize", this.maxLLSize);
        bundle.putFloat("maxScrollDistance", this.maxScrollDistance);
        bundle.putFloat("minDependencyScrollY", this.minDependencyScrollY);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, LinearLayout child, Parcelable state) {
        if (!(state instanceof Bundle)) return; // implicit null check

        Bundle bundle = (Bundle) state;
        this.mExpandedPercentageFactor = bundle.getFloat("mExpandedPercentageFactor");
        this.minLLSize = bundle.getFloat("minLLSize");
        this.maxLLSize = bundle.getFloat("maxLLSize");
        this.minDependencyScrollY = bundle.getFloat("minDependencyScrollY");
        this.maxScrollDistance = bundle.getFloat("maxScrollDistance");
        final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.height = (int) Math.max(minLLSize, maxLLSize * mExpandedPercentageFactor);
        child.setLayoutParams(lp);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        final CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams) child.getLayoutParams();
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

        if (minLLSize == 0.0f) {
            initProperties(parent, child, appBarLayout);
        }

        float curDependencyY = appBarLayout.getBottom() - minDependencyScrollY;
        mExpandedPercentageFactor = curDependencyY / maxScrollDistance;
        lp.height = (int) (minLLSize + (maxLLSize - minLLSize) * mExpandedPercentageFactor);

        child.setTranslationY(appBarLayout.getBottom());
        child.setLayoutParams(lp);

        return true;
    }

    private void initProperties(CoordinatorLayout parent, LinearLayout child, AppBarLayout dependency) {  //расчет начальных параметров
        maxLLSize = child.getHeight();
        minLLSize = getHeight(child);  //найдет минимальную высоту child, которая полностью вместит его контент, т.е. высоту если будет wrap_content
        minDependencyScrollY = getStatusBarHeight(mContext) + getAppBarSize(mContext);
        maxScrollDistance = dependency.getHeight() - minDependencyScrollY;
    }
}