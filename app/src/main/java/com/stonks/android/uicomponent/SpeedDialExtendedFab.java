package com.stonks.android.uicomponent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.stonks.android.R;
import com.stonks.android.utility.Constants;
import java.util.ArrayList;

public class SpeedDialExtendedFab extends ExtendedFloatingActionButton {
    ObjectAnimator iconTintAnimator, backgroundAnimator;
    final ArrayList<View> speedDial = new ArrayList<>();

    public SpeedDialExtendedFab(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAnimators();
    }

    public SpeedDialExtendedFab(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnimators();
    }

    public SpeedDialExtendedFab(@NonNull Context context) {
        super(context);
        initAnimators();
    }

    private void initAnimators() {
        this.iconTintAnimator =
                ObjectAnimator.ofInt(this, "iconTint", Constants.primaryColor, Color.WHITE);
        this.backgroundAnimator =
                ObjectAnimator.ofInt(
                        this, "backgroundTintList", Constants.primaryColor, Color.BLACK);

        this.iconTintAnimator.setDuration(200L);
        this.iconTintAnimator.setEvaluator(new ArgbEvaluator());
        this.iconTintAnimator.setInterpolator(new DecelerateInterpolator(2));
        this.iconTintAnimator.addUpdateListener(
                animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    this.setIconTint(ColorStateList.valueOf(animatedValue));
                });

        this.backgroundAnimator.setDuration(200L);
        this.backgroundAnimator.setEvaluator(new ArgbEvaluator());
        this.backgroundAnimator.setInterpolator(new DecelerateInterpolator(2));
        this.backgroundAnimator.addUpdateListener(
                animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    this.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                });
    }

    // Needed by ObjectAnimator
    public void setIconTint(final int iconTint) {
        super.setIconTint(ColorStateList.valueOf(iconTint));
    }

    // Needed by ObjectAnimator
    public void setBackgroundTintList(final int backgroundTint) {
        super.setIconTint(ColorStateList.valueOf(backgroundTint));
    }

    public void addToSpeedDial(@NonNull final View v) {
        speedDial.add(v);
        v.setVisibility(GONE);
        v.setTranslationY(v.getHeight());
        v.setAlpha(0f);
    }

    private void showSpeedDial(@NonNull final View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);
        v.setTranslationY(v.getHeight());
        v.animate()
                .setDuration(200)
                .translationY(0)
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }
                        })
                .alpha(1f)
                .start();
    }

    private void hideSpeedDial(@NonNull final View v) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(1f);
        v.setTranslationY(0);

        v.animate()
                .setDuration(200)
                .translationY(v.getHeight())
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                v.setVisibility(View.GONE);
                                super.onAnimationEnd(animation);
                            }
                        })
                .alpha(0f)
                .start();
    }

    public void open(@NonNull final View overlay) {
        this.setIconTint(ColorStateList.valueOf(Constants.primaryColor));

        overlay.setVisibility(VISIBLE);
        overlay.animate()
                .setDuration(200L)
                .alpha(0.9f)
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }
                        })
                .start();

        this.backgroundAnimator.start();
        this.iconTintAnimator.start();

        this.setIcon(getContext().getDrawable(R.drawable.ic_baseline_close_24));
        this.shrink();

        this.speedDial.forEach(this::showSpeedDial);
    }

    public void close(@NonNull final View overlay) {
        overlay.animate()
                .setDuration(200L)
                .alpha(0f)
                .setListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                overlay.setVisibility(GONE);
                                super.onAnimationEnd(animation);
                            }
                        })
                .start();
        backgroundAnimator.reverse();
        iconTintAnimator.reverse();

        this.extend();
        this.setIcon(null);

        this.speedDial.forEach(this::hideSpeedDial);
    }

    public void trigger(@NonNull final View overlay) {
        if (this.isExtended()) {
            this.open(overlay);
        } else {
            this.close(overlay);
        }
    }
}
