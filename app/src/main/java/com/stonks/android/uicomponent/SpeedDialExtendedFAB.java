package com.stonks.android.uicomponent;

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

public class SpeedDialExtendedFAB extends ExtendedFloatingActionButton {
    public SpeedDialExtendedFAB(@NonNull Context context) {
        super(context);
    }

    public SpeedDialExtendedFAB(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeedDialExtendedFAB(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Needed by ObjectAnimator
    public void setIconTint(int iconTint) {
        super.setIconTint(ColorStateList.valueOf(iconTint));
    }

    // Needed by ObjectAnimator
    public void setBackgroundTintList(int backgroundTint) {
        super.setIconTint(ColorStateList.valueOf(backgroundTint));
    }

    @Override
    public void setIconTint(@Nullable ColorStateList iconTint) {
        super.setIconTint(iconTint);
    }

    @Override
    public void setBackgroundTintList(@Nullable ColorStateList tintList) {
        super.setBackgroundTintList(tintList);
    }

    public void open(View overlay) {
        this.setIconTint(ColorStateList.valueOf(Color.rgb(7, 78, 232)));

        final ObjectAnimator iconTintAnimator =
                ObjectAnimator.ofInt(this, "iconTint", Color.rgb(7, 78, 232), Color.WHITE);
        iconTintAnimator.setDuration(200L);
        iconTintAnimator.setEvaluator(new ArgbEvaluator());
        iconTintAnimator.setInterpolator(new DecelerateInterpolator(2));
        iconTintAnimator.addUpdateListener(
                animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    this.setIconTint(ColorStateList.valueOf(animatedValue));
                });

        final ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(overlay, "alpha", 0f, 0.6f);
        overlayAnimator.setDuration(200L);
        overlayAnimator.setInterpolator(new DecelerateInterpolator(2));
        overlayAnimator.addUpdateListener(
                animation -> overlay.setAlpha((float) animation.getAnimatedValue()));

        final ObjectAnimator backgroundAnimator =
                ObjectAnimator.ofInt(
                        this, "backgroundTintList", Color.rgb(7, 78, 232), Color.rgb(0, 0, 0));
        backgroundAnimator.setDuration(200L);
        backgroundAnimator.setEvaluator(new ArgbEvaluator());
        backgroundAnimator.setInterpolator(new DecelerateInterpolator(2));
        backgroundAnimator.addUpdateListener(
                animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    this.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                });

        overlayAnimator.start();
        backgroundAnimator.start();
        iconTintAnimator.start();

        this.setIcon(getContext().getDrawable(R.drawable.ic_baseline_close_24));
        this.shrink();
    }

    public void close(View overlay) {

        final ObjectAnimator iconTintAnimator =
                ObjectAnimator.ofInt(this, "iconTint", Color.WHITE, Color.rgb(7, 78, 232));
        iconTintAnimator.setDuration(200L);
        iconTintAnimator.setEvaluator(new ArgbEvaluator());
        iconTintAnimator.setInterpolator(new DecelerateInterpolator(2));
        iconTintAnimator.addUpdateListener(
                animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    this.setIconTint(ColorStateList.valueOf(animatedValue));
                });

        final ObjectAnimator overlayAnimator = ObjectAnimator.ofFloat(overlay, "alpha", 0.6f, 0f);
        overlayAnimator.setDuration(200L);
        overlayAnimator.setInterpolator(new DecelerateInterpolator(2));
        overlayAnimator.addUpdateListener(
                animation -> overlay.setAlpha((float) animation.getAnimatedValue()));

        final ObjectAnimator backgroundAnimator =
                ObjectAnimator.ofInt(
                        this, "backgroundTintList", Color.rgb(0, 0, 0), Color.rgb(7, 78, 232));
        backgroundAnimator.setDuration(200L);
        backgroundAnimator.setEvaluator(new ArgbEvaluator());
        backgroundAnimator.setInterpolator(new DecelerateInterpolator(2));
        backgroundAnimator.addUpdateListener(
                animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    this.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
                });

        overlayAnimator.start();
        backgroundAnimator.start();
        iconTintAnimator.start();

        this.extend();
        this.setIcon(null);
    }

    public void trigger(View overlay) {
        if (this.isExtended()) {
            this.open(overlay);
        } else {
            this.close(overlay);
        }
    }
}
