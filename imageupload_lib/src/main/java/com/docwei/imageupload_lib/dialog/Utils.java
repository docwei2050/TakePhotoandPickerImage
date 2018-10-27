package com.docwei.imageupload_lib.dialog;

import android.content.Context;
import android.view.Gravity;

import com.docwei.imageupload_lib.R;


final class Utils {

    private static final int INVALID = -1;

    private Utils() {
        // no instance
    }

    static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    static int getAnimationResource(int gravity, boolean isInAnimation) {
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            return isInAnimation ? R.anim.slide_in_top : R.anim.slide_out_top;
        }
        if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
            return isInAnimation ? R.anim.slide_in_bottom : R.anim.slide_out_bottom;
        }
        if ((gravity & Gravity.CENTER) == Gravity.CENTER) {
            return isInAnimation ? R.anim.fade_in_center : R.anim.fade_out_center;
        }
        return INVALID;
    }
}
