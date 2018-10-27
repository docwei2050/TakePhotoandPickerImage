package com.docwei.imageupload_lib.album;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.docwei.imageupload_lib.GlideApp;
import com.docwei.imageupload_lib.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

/**
 * Created by git on 2018/4/29.
 */

public class ImagePageAdapter extends PagerAdapter {
    public OnPageClickListener mOnPageClickListener;
    private List<String> list;
    private Context mContext;

    public ImagePageAdapter(List<String> list, Context context) {
        this.list = list;
        mContext = context;

    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_preview_all_vp_item, container, false);
        PhotoView photoView = (PhotoView) view;
        GlideApp.with(mContext).load(list.get(position)).placeholder(R.drawable.img_default)
                .error(R.drawable.img_fail).into(photoView);
        container.addView(photoView);
        photoView.setOnDoubleTapListener(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mOnPageClickListener != null) {
                    mOnPageClickListener.clickPage();
                }
                return true;
            }
        });
        return photoView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public void setOnPageClickListener(OnPageClickListener onPageClickListener) {
        mOnPageClickListener = onPageClickListener;
    }

    public interface OnPageClickListener {
        void clickPage();
    }
}
