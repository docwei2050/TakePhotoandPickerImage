package com.docwei.imageupload_lib.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.docwei.imageupload_lib.R;

import java.util.Arrays;

public class DialogPlusBuilder {
    private static final int INVALID = -1;

    private final int[] margin = new int[4];
    private final int[] padding = new int[4];
    private final int[] outMostMargin = new int[4];
    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM);

    private Context context;
    private Holder holder;
    private int gravity = Gravity.BOTTOM;


    private OnDismissListener onDismissListener;
    private OnCancelListener onCancelListener;
    private boolean isFromDecorView = true;
    private ViewGroup anchorView;


    private boolean isCancelable = true;
    private int contentBackgroundResource = android.R.color.white;
    private int inAnimation = INVALID;
    private int outAnimation = INVALID;
    private int defaultContentHeight;
    private int overlayBackgroundResource = R.color.dialogplus_black_overlay;

    private DialogPlusBuilder() {
    }


    DialogPlusBuilder(Context context) {
        if (context == null) {
            throw new NullPointerException("Context may not be null");
        }
        this.context = context;
        Arrays.fill(margin, INVALID);
    }

    public DialogPlusBuilder setContentHolder(Holder holder) {
        this.holder = holder;
        return this;
    }

    public Holder getHolder() {

        return holder;
    }

    @Deprecated
    public DialogPlusBuilder setBackgroundColorResId(int resourceId) {
        return setContentBackgroundResource(resourceId);
    }

    public DialogPlusBuilder setGravity(int gravity) {
        this.gravity = gravity;
        params.gravity = gravity;
        return this;
    }

    public DialogPlusBuilder setOutMostMargin(int left, int top, int right, int bottom) {
        this.outMostMargin[0] = left;
        this.outMostMargin[1] = top;
        this.outMostMargin[2] = right;
        this.outMostMargin[3] = bottom;
        return this;
    }

    public DialogPlusBuilder setMargin(int left, int top, int right, int bottom) {
        this.margin[0] = left;
        this.margin[1] = top;
        this.margin[2] = right;
        this.margin[3] = bottom;
        return this;
    }

    public DialogPlusBuilder setPadding(int left, int top, int right, int bottom) {
        this.padding[0] = left;
        this.padding[1] = top;
        this.padding[2] = right;
        this.padding[3] = bottom;
        return this;
    }

    public DialogPlusBuilder setFromWhichView(boolean isFromDecorView) {
        this.isFromDecorView = isFromDecorView;
        return this;
    }

    public DialogPlusBuilder setContentHeight(int height) {
        params.height = height;
        return this;
    }

    public DialogPlusBuilder setContentWidth(int width) {
        params.width = width;
        return this;
    }

    public DialogPlus create() {
        return new DialogPlus(this);
    }

    public Context getContext() {
        return context;
    }

    public Animation getInAnimation() {
        int res = (inAnimation == INVALID)
                ? Utils.getAnimationResource(this.gravity, true)
                : inAnimation;
        return AnimationUtils.loadAnimation(context, res);
    }

    public DialogPlusBuilder setInAnimation(int inAnimResource) {
        this.inAnimation = inAnimResource;
        return this;
    }

    public Animation getOutAnimation() {
        int res = (outAnimation == INVALID)
                ? Utils.getAnimationResource(this.gravity, false)
                : outAnimation;
        return AnimationUtils.loadAnimation(context, res);
    }

    public DialogPlusBuilder setOutAnimation(int outAnimResource) {
        this.outAnimation = outAnimResource;
        return this;
    }

    public FrameLayout.LayoutParams getContentParams() {
        return params;
    }

    public FrameLayout.LayoutParams getOutmostLayoutParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(outMostMargin[0], outMostMargin[1], outMostMargin[2], outMostMargin[3]);
        return params;
    }

    public boolean isCancelable() {
        return isCancelable;
    }

    public DialogPlusBuilder setCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
        return this;
    }

    public boolean isFromDecorView() {
        return isFromDecorView;
    }

    public ViewGroup getAnchorView() {
        return anchorView;
    }

    public DialogPlusBuilder setAnchorView(ViewGroup anchorView) {
        this.anchorView = anchorView;
        return this;
    }

    public OnDismissListener getOnDismissListener() {
        return onDismissListener;
    }

    public DialogPlusBuilder setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
        return this;
    }

    public OnCancelListener getOnCancelListener() {
        return onCancelListener;
    }

    public DialogPlusBuilder setOnCancelListener(OnCancelListener listener) {
        this.onCancelListener = listener;
        return this;
    }

    public int[] getContentMargin() {
        int minimumMargin = context.getResources()
                .getDimensionPixelSize(R.dimen.dialogplus_default_center_margin);
        for (int i = 0; i < margin.length; i++) {
            margin[i] = getMargin(this.gravity, margin[i], minimumMargin);
        }
        return margin;
    }

    public int[] getContentPadding() {
        return padding;
    }

    public int getDefaultContentHeight() {
        Activity activity = (Activity) context;
        Display display = activity.getWindowManager()
                .getDefaultDisplay();
        int displayHeight = display.getHeight() - Utils.getStatusBarHeight(activity);
        if (defaultContentHeight == 0) {
            defaultContentHeight = (displayHeight * 2) / 5;
        }
        return defaultContentHeight;
    }

    public int getOverlayBackgroundResource() {
        return overlayBackgroundResource;
    }

    public DialogPlusBuilder setOverlayBackgroundResource(int resourceId) {
        this.overlayBackgroundResource = resourceId;
        return this;
    }

    public int getContentBackgroundResource() {
        return contentBackgroundResource;
    }

    public DialogPlusBuilder setContentBackgroundResource(int resourceId) {
        this.contentBackgroundResource = resourceId;
        return this;
    }

    private int getMargin(int gravity, int margin, int minimumMargin) {
        switch (gravity) {
            case Gravity.CENTER:
                return (margin == INVALID)
                        ? minimumMargin
                        : margin;
            default:
                return (margin == INVALID)
                        ? 0
                        : margin;
        }
    }

}
