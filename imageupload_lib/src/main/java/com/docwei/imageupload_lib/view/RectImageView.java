package com.docwei.imageupload_lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.docwei.imageupload_lib.R;


/**
 * Created by wk on 2018/3/11.
 * 正方形控件
 */

public class RectImageView extends android.support.v7.widget.AppCompatImageView {
    private static final float DEFEALT_RATIO = 1.0f;
    private float mRatio = DEFEALT_RATIO;

    public RectImageView(Context context) {
        this(context, null);
    }

    public RectImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RectImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RectImageView);
            mRatio = ta.getFloat(R.styleable.RectImageView_iv_ratio, DEFEALT_RATIO);
            ta.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widht = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(widht, (int) (widht * mRatio));
    }
}


