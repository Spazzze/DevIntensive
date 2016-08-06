package com.softdesign.devintensive.ui.view.behaviors;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;

@SuppressWarnings("unchecked")
public class Animations {

/*    android:src="@{profile.authorizedUser ? (profile.editMode ? @drawable/ic_done_white : @drawable/ic_edit_white) : (profile.liked ? @drawable/ic_heart_accent : @drawable/ic_heart_outline_accent)}"
    app:changeFABColor="@{profile.authorizedUser ? @color/color_accent : @color/color_white}"*/

    public static void animateFABLikeButton(final FloatingActionButton fab, boolean isLiked) {

        Pair<Integer, Integer> imageSize = (Pair) fab.getTag(R.id.fab_size_tag);
        Pair<Boolean, AnimatorSet> tag = (Pair) fab.getTag(R.id.fab_anim_tag);

        if (imageSize == null) {
            imageSize = new Pair<>(fab.getMeasuredWidth(), fab.getMeasuredHeight());
            fab.setTag(R.id.likeAnimSize_tag, imageSize);
            fab.setPivotX(imageSize.first / 2);
            fab.setPivotY(imageSize.second / 2);
        }

        if (tag == null || tag.first != isLiked) {
            if (tag != null && tag.second != null) {
                tag.second.end();
            }

            fab.setImageResource(!isLiked ? R.drawable.ic_heart_accent : R.drawable.ic_heart_broken);

            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator AnimX = AppUtils.getBounceXAnimator(fab, AppConfig.ANIM_DURATION_FAB_LIKE);
            ObjectAnimator AnimY = AppUtils.getBounceYAnimator(fab, AppConfig.ANIM_DURATION_FAB_LIKE);

            if (isLiked) {
                AnimY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.setImageResource(R.drawable.ic_heart_outline_accent);
                    }
                });
            }

            animatorSet
                    .play(AnimX)
                    .with(AnimY);

            fab.setTag(R.id.fab_anim_tag, new Pair<>(isLiked, animatorSet));

            animatorSet.start();
        }
    }

    public static void animateLikeButton(final ImageView likeImageLeft, ImageView likeImageRight, boolean isLiked) {

        Pair<Integer, Integer> imageSize = (Pair) likeImageLeft.getTag(R.id.likeAnimSize_tag);
        Pair<Boolean, AnimatorSet> pairL = (Pair) likeImageLeft.getTag(R.id.likeAnimL_tag);
        Pair<Boolean, AnimatorSet> pairR = (Pair) likeImageRight.getTag(R.id.likeAnimR_tag);

        if (imageSize == null) {
            imageSize = new Pair<>(likeImageLeft.getMeasuredWidth(), likeImageLeft.getMeasuredHeight());
            likeImageLeft.setTag(R.id.likeAnimSize_tag, imageSize);
            likeImageLeft.setPivotX(imageSize.first / 2);
            likeImageLeft.setPivotY(imageSize.second / 2);
        }

        if (pairL == null || pairL.first != isLiked) {
            if (pairL != null && pairL.second != null) {
                likeImageLeft.setPivotY(imageSize.second / 2);
                likeImageLeft.setRotation(0f);
                pairL.second.end();
            }
            if (pairR != null && pairR.second != null) {
                likeImageRight.setVisibility(View.GONE);
                pairR.second.end();
            }
            if (pairR == null) {
                likeImageRight.setVisibility(View.VISIBLE);
                likeImageRight.setPivotX(imageSize.first / 2);
                likeImageRight.setPivotY(imageSize.second);
                likeImageRight.setVisibility(View.GONE);
            }

            likeImageLeft.setImageResource(!isLiked ? R.drawable.ic_heart_accent : R.drawable.ic_heart_broken_left);
            likeImageRight.setVisibility(isLiked ? View.VISIBLE : View.GONE);

            AnimatorSet animatorSetL = new AnimatorSet();
            AnimatorSet animatorSetR = new AnimatorSet();

            if (!isLiked) {
                likeImageLeft.setPivotY(imageSize.second / 2);

                animatorSetL
                        .play(AppUtils.getBounceXAnimator(likeImageLeft, AppConfig.ANIM_DURATION_BOUNCE_LIKE))
                        .with(AppUtils.getBounceYAnimator(likeImageLeft, AppConfig.ANIM_DURATION_BOUNCE_LIKE));
            } else {
                likeImageLeft.setPivotY(imageSize.second);

                ObjectAnimator rotationAnimL = AppUtils.getRotationAnimator(likeImageLeft,
                        AppConfig.ANIM_DURATION_ROTATE_UNLIKE, -AppConfig.ANIM_HEART_BREAK_ANGLE);

                ObjectAnimator rotationAnimR = AppUtils.getRotationAnimator(likeImageRight,
                        AppConfig.ANIM_DURATION_ROTATE_UNLIKE, AppConfig.ANIM_HEART_BREAK_ANGLE);

                ObjectAnimator growAnimX = AppUtils.getGrowXAnimator(likeImageLeft, AppConfig.ANIM_DURATION_BOUNCE_UNLIKE);
                ObjectAnimator growAnimY = AppUtils.getGrowYAnimator(likeImageLeft, AppConfig.ANIM_DURATION_BOUNCE_UNLIKE);

                final int defPivotY = imageSize.second / 2;
                rotationAnimL.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        likeImageLeft.setPivotY(defPivotY);
                        likeImageLeft.setRotation(0f);
                        likeImageLeft.setImageResource(R.drawable.ic_heart_outline_accent);
                    }
                });

                rotationAnimR.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        likeImageRight.setVisibility(View.GONE);
                    }
                });

                animatorSetL
                        .play(growAnimX)
                        .with(growAnimY)
                        .after(rotationAnimL);

                animatorSetR.play(rotationAnimR);
            }

            likeImageLeft.setTag(R.id.likeAnimL_tag, new Pair<>(isLiked, animatorSetL));
            likeImageRight.setTag(R.id.likeAnimR_tag, new Pair<>(isLiked, animatorSetR));

            animatorSetL.start();
            animatorSetR.start();
        }
    }

    public static void animateLikeButton(final UsersAdapter.UserViewHolder holder) {

        ImageView likeImageLeft = holder.getBinding().buttonLikesLayout.btnLikeImgL;
        ImageView likeImageRight = holder.getBinding().buttonLikesLayout.btnLikeImgR;
        boolean isLiked = holder.getBinding().getProfile().isLiked();

        animateLikeButton(likeImageLeft, likeImageRight, isLiked);
    }
}
