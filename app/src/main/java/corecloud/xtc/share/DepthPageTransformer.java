package corecloud.xtc.share;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DepthPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        int pageWidth = page.getWidth();

        if (position < -1f || position > 1f) {
            page.setAlpha(0f);
        } else {
            float absPosition = Math.abs(position);

            if (absPosition >= 0.99f) {
                page.setAlpha(0f);
            } else {
                float scaleFactor = 0.82f + (1f - 0.82f) * (1f - absPosition);
                float alphaFactor = (1f - absPosition);

                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setAlpha(alphaFactor);

                page.setTranslationX(-position * pageWidth * 0.45f);
                page.setTranslationZ(-absPosition);
            }
        }
    }
}