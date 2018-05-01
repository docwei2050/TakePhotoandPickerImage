package com.docwei.cameraphotodemo.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.docwei.cameraphotodemo.R;


public class DialogPlus {

    //Dialog的根布局
    private final ViewGroup rootView;

    //Dialog的内容布局
    private final ViewGroup contentContainer;

    //点击黑色背景是否被取消
    private final boolean isCancelable;

    // 是否正在消失
    private boolean isDismissing;

    private final OnDismissListener onDismissListener;


    private final OnCancelListener onCancelListener;




    private final ViewGroup decorView;

    private final Animation outAnim;
    private final Animation inAnim;

    private boolean isFromDecorView=true;
    private ViewGroup anchorView;

    /**
     * Content
     */
    private final Holder         holder;
    private final LayoutInflater mLayoutInflater;

    DialogPlus(DialogPlusBuilder builder) {
        mLayoutInflater = LayoutInflater.from(builder.getContext());

        Activity activity = (Activity) builder.getContext();

        holder = builder.getHolder();
        onDismissListener = builder.getOnDismissListener();
        onCancelListener = builder.getOnCancelListener();
        isCancelable = builder.isCancelable();
        isFromDecorView=builder.isFromDecorView();
        anchorView=builder.getAnchorView();
        if(isFromDecorView) {
            decorView = (ViewGroup) activity.getWindow()
                                            .getDecorView()
                                            .findViewById(android.R.id.content);
        }else{
            decorView=anchorView;
        }


        rootView = (ViewGroup) mLayoutInflater.inflate(R.layout.base_container, decorView, false);
        rootView.setLayoutParams(builder.getOutmostLayoutParams());
        rootView.setBackgroundResource(builder.getOverlayBackgroundResource());

        contentContainer = (ViewGroup) rootView.findViewById(R.id.dialogplus_content_container);
        contentContainer.setLayoutParams(builder.getContentParams());

        outAnim = builder.getOutAnimation();
        inAnim = builder.getInAnimation();
        initContentView(mLayoutInflater, builder.getContentPadding(), builder.getContentMargin());

        initCancelable();

    }

    public static DialogPlusBuilder newDialog(Context context) {
        return new DialogPlusBuilder(context);
    }


    public void show() {
        if (isShowing()) {
            return;
        }
        onAttached(rootView);
    }


    public boolean isShowing() {
        View view = decorView.findViewById(R.id.dialogplus_outmost_container);
        return view != null;
    }

    public void dismiss() {
        if (isDismissing) {
            return;
        }
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                decorView.post(new Runnable() {
                    @Override
                    public void run() {
                        decorView.removeView(rootView);
                        isDismissing = false;
                        if (onDismissListener != null) {
                            onDismissListener.onDismiss(DialogPlus.this);
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        contentContainer.startAnimation(outAnim);
        isDismissing = true;
    }


    private void initContentView(LayoutInflater inflater, int[] padding, int[] margin) {
        View contentView = holder.getInflatedView(inflater,this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                       ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(margin[0], margin[1], margin[2], margin[3]);
        contentView.setLayoutParams(params);
        contentView.setPadding(padding[0], padding[1], padding[2], padding[3]);
        contentContainer.addView(contentView);
    }
    private void initCancelable() {
        if (!isCancelable) {
            return;
        }
        View view = rootView.findViewById(R.id.dialogplus_outmost_container);
        view.setOnTouchListener(onCancelableTouchListener);
    }


    private void onAttached(View view) {
        decorView.addView(view);
        contentContainer.startAnimation(inAnim);
        contentContainer.setFocusable(true);
        contentContainer.requestFocusFromTouch();
        contentContainer.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                switch (event.getAction()) {
                    case KeyEvent.ACTION_UP:
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            onBackPressed(DialogPlus.this);
                            return true;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }


    public void onBackPressed(DialogPlus dialogPlus) {
        if (onCancelListener != null) {
            onCancelListener.onCancel(DialogPlus.this);
        }
        dismiss();
    }

    private final View.OnTouchListener onCancelableTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (onCancelListener != null) {
                    onCancelListener.onCancel(DialogPlus.this);
                }
                dismiss();
            }
            return false;
        }
    };
}
