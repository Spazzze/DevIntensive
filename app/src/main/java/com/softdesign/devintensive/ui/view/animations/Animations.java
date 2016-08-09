package com.softdesign.devintensive.ui.view.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.ui.view.animations.internal.BaseItemAnimator;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

@SuppressWarnings("unchecked")
public class Animations {
    private static final Context CONTEXT = DevIntensiveApplication.getContext();

    public static void animateLikeButton(final ImageView likeImageLeft, ImageView likeImageRight, boolean isLiked) {
        if (likeImageLeft == null || likeImageRight == null) return;

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
        if (holder == null || holder.getBinding() == null) return;

        ImageView likeImageLeft = holder.getBinding().buttonLikesLayout.btnLikeImgL;
        ImageView likeImageRight = holder.getBinding().buttonLikesLayout.btnLikeImgR;
        boolean isLiked = holder.getBinding().getProfile().isLiked();

        animateLikeButton(likeImageLeft, likeImageRight, isLiked);
    }

    public static void animateFabAppearance(FloatingActionButton fab, float translation) {
        if (fab == null) return;

        if (fab.getTranslationY() != translation) fab.setTranslationY(translation);

        float neededTranslation = (translation == 0.0f) ?
                (2 * CONTEXT.getResources().getDimensionPixelOffset(R.dimen.size_medium_56)) : 0.0f;

        fab.animate()
                .translationY(neededTranslation)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(AppConfig.UL_ANIM_START_DELAY_FAB)
                .setDuration(AppConfig.UL_ANIM_DURATION_FAB)
                .start();
    }

    public enum AnimationType {

        FadeInUp(new FadeInUpAnimator(new OvershootInterpolator(1f))),
        FadeInRight(new FadeInRightAnimator(new OvershootInterpolator(1f))),
        Landing(new LandingAnimator(new OvershootInterpolator(1f))),
        ScaleInTop(new ScaleInTopAnimator(new OvershootInterpolator(1f))),
        FlipInRightY(new FlipInRightYAnimator(new OvershootInterpolator(1f))),
        SlideInUp(new SlideInUpAnimator(new OvershootInterpolator(1f))),
        OvershootInRight(new OvershootInRightAnimator(1.0f));

        private BaseItemAnimator mAnimator;

        AnimationType(BaseItemAnimator animator) {
            mAnimator = animator;
        }

        public BaseItemAnimator getAnimator() {
            return mAnimator;
        }
    }
}
