package org.thunderdog.challegram.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Drawables;
import org.thunderdog.challegram.tool.Paints;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.tool.UI;

import me.vkryl.android.AnimatorUtils;
import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.animator.FactorAnimator;
import me.vkryl.core.ColorUtils;
import me.vkryl.core.MathUtils;

public class TranslationCounterDrawable extends Drawable implements FactorAnimator.Target {
  public static final int TRANSLATE_STATUS_DEFAULT = 0;
  public static final int TRANSLATE_STATUS_LOADING = 1;
  public static final int TRANSLATE_STATUS_SUCCESS = 2;
  public static final int TRANSLATE_STATUS_ERROR = 3;

  private final static int ANIMATOR_LOADING = 0;
  private final static int ANIMATOR_ERROR = 1;
  private final static int ANIMATOR_OFFSET = 2;
  private final BoolAnimator isLoading = new BoolAnimator(ANIMATOR_LOADING, this, AnimatorUtils.DECELERATE_INTERPOLATOR, 180L);
  private final BoolAnimator isError = new BoolAnimator(ANIMATOR_ERROR, this, AnimatorUtils.DECELERATE_INTERPOLATOR, 180L);
  private final BoolAnimator offsetAnimator = new BoolAnimator(ANIMATOR_OFFSET, this, AnimatorUtils.LINEAR_INTERPOLATOR, 750L);

  private final Drawable drawable;
  private final Drawable drawableBg;
  private final int width, height;
  private Runnable invalidateCallback;

  private int backgroundColorId = R.id.theme_color_bubbleIn_time;
  private int loadingColorId = R.id.theme_color_bubbleIn_textLink;

  public TranslationCounterDrawable (Drawable drawable) {
    this.drawable = drawable;
    this.drawableBg = drawable.getConstantState().newDrawable().mutate();
    this.width = drawable.getMinimumWidth();
    this.height = drawable.getMinimumHeight();

    UI.post(this::checkStatus);
  }

  public void setColors (int backgroundColorId, int loadingColorId) {
    this.backgroundColorId = backgroundColorId;
    this.loadingColorId = loadingColorId;
  }

  public void setInvalidateCallback (Runnable invalidateCallback) {
    this.invalidateCallback = invalidateCallback;
  }

  public void setStatus (int status, boolean animated) {
    setStatus(status == TRANSLATE_STATUS_LOADING, status == TRANSLATE_STATUS_ERROR, animated);
  }

  private void setStatus (boolean isLoading, boolean isError, boolean animated) {
    this.isLoading.setValue(isLoading, animated);
    this.isError.setValue(isError, animated);
    if (animated) {
      checkStatus();
    }
  }

  private void checkStatus () {
    if (isLoading.getValue() && !isError.getValue() && !offsetAnimator.isAnimating()) {
      offsetAnimator.setValue(!offsetAnimator.getValue(), true);
    }
  }

  public float getLoadingTextAlpha () {
    float alpha = 1f - isLoading.getFloatValue() * 0.5f + ((offsetAnimator.getFloatValue() - 0.5f) * 2f * 0.1f) * isLoading.getFloatValue();
    return MathUtils.clamp(alpha);
  }

  @Override
  public void draw (@NonNull Canvas canvas) {
    final float loadedProgress = 1f - isLoading.getFloatValue();
    final float errorProgress = isError.getFloatValue();
    final int iconColor1 = ColorUtils.fromToArgb(Theme.getColor(loadingColorId), Theme.getColor(R.id.theme_color_iconActive), loadedProgress);
    final int iconColor = ColorUtils.fromToArgb(iconColor1, Theme.getColor(R.id.theme_color_iconNegative), errorProgress);

    if (loadedProgress == 1f) {
      Drawables.draw(canvas, drawable, 0, 0, Paints.getPorterDuffPaint(iconColor));
    } else {
      Drawables.draw(canvas, drawableBg, 0, 0, Paints.getPorterDuffPaint(Theme.getColor(backgroundColorId)));

      float lineWidth = MathUtils.fromTo(0.571f, 1f, loadedProgress) * width;
      float offset = MathUtils.fromTo(MathUtils.fromTo(-lineWidth - width * 0.5f, width * 1.5f, offsetAnimator.getFloatValue()), 0, loadedProgress);

      canvas.save();
      canvas.rotate(45, width / 2f, height / 2f);
      canvas.clipRect(offset, -height, offset + lineWidth, height * 2);
      canvas.rotate(-45, width / 2f, height / 2f);
      Drawables.draw(canvas, drawable, 0, 0, Paints.getPorterDuffPaint(iconColor));
      canvas.restore();
    }
  }

  private void invalidate () {
    if (invalidateCallback != null) {
      invalidateCallback.run();
    }
  }

  @Override
  public int getMinimumWidth () {
    return width;
  }

  @Override
  public int getMinimumHeight () {
    return height;
  }

  @Override
  public void setAlpha (int i) {

  }

  @Override
  public void setColorFilter (@Nullable ColorFilter colorFilter) {

  }

  @Override
  public int getOpacity () {
    return drawable.getOpacity();
  }

  @Override
  public void onFactorChanged (int id, float factor, float fraction, FactorAnimator callee) {
    invalidate();
  }

  @Override
  public void onFactorChangeFinished (int id, float finalFactor, FactorAnimator callee) {
    if (id == ANIMATOR_OFFSET) {
      checkStatus();
    }
  }
}
