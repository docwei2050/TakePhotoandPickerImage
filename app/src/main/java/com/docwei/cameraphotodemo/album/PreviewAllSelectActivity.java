package com.docwei.cameraphotodemo.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.docwei.cameraphotodemo.DensityUtil;
import com.docwei.cameraphotodemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by git on 2018/4/29.
 */

public class PreviewAllSelectActivity extends AppCompatActivity {
    private static final String  SELECTED_PHOTOS="selected_photos";
    private ViewPager mViewPager;
    private LinearLayout mLl_dots;

    public static void startActivity(ArrayList<String> list, Activity src){
        Intent intent=new Intent(src,PreviewAllSelectActivity.class);
        intent.putExtra(SELECTED_PHOTOS,list);
        src.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_all);
        mViewPager = findViewById(R.id.view_pager);
        mLl_dots = findViewById(R.id.ll_dots);
       initData();
    }

    private void initData() {
        Intent intent=getIntent();
        ArrayList<String> list=new ArrayList<>();
        if(intent!=null){
            list= (ArrayList<String>) intent.getSerializableExtra(SELECTED_PHOTOS);
        }
        ImagePageAdapter adapter=new ImagePageAdapter(list,this);
        mViewPager.setAdapter(adapter);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.dip2px(this, 6), DensityUtil.dip2px(this, 6));
        if(mLl_dots.getChildCount()==0) {
            for (int i = 0; i < list.size(); i++) {
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (i < list.size() - 1) {
                    params.rightMargin = DensityUtil.dip2px(this, 7);
                }
                if (i == 0) {
                    imageView.setImageResource(R.drawable.flag_dot_selected);
                } else {
                    imageView.setImageResource(R.drawable.flag_dot_normal);
                }
                imageView.setLayoutParams(params);
                mLl_dots.addView(imageView);
            }
        }


        final ArrayList<String> finalList = list;
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < finalList.size(); i++) {
                    ImageView iv = (ImageView) mLl_dots.getChildAt(i);
                    if (i == position % finalList.size()) {
                        iv.setImageResource(R.drawable.flag_dot_selected);
                    } else {
                        iv.setImageResource(R.drawable.flag_dot_normal);
                    }
                }
            }
        });
       adapter.setOnPageClickListener(new ImagePageAdapter.OnPageClickListener() {
           @Override
           public void clickPage() {
               finish();
           }
       });
    }
}
