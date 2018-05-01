package com.docwei.cameraphotodemo.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.docwei.cameraphotodemo.DensityUtil;
import com.docwei.cameraphotodemo.MainActivity;
import com.docwei.cameraphotodemo.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

/**
 * Created by git on 2018/4/29.
 */

public class PreviewSingleImageActivity extends AppCompatActivity {
    public static void startActivity(Activity src,String imagePath,View view){
        Intent                intent        =new Intent(src, PreviewSingleImageActivity.class);
        intent.putExtra("imagePath",imagePath);
        ActivityOptionsCompat optionsCompat =ActivityOptionsCompat.makeSceneTransitionAnimation(src, view, "preview");
        ActivityCompat.startActivity(src, intent, optionsCompat.toBundle());
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_single);
        PhotoView iv_show = findViewById(R.id.iv_show);
        Intent    intent  =getIntent();
        if(intent!=null){
            String imagePath=intent.getStringExtra("imagePath");
            Glide.with(this).load(imagePath).into(iv_show);
        }

        ViewCompat.setTransitionName(iv_show, "preview");
        iv_show.setOnDoubleTapListener(new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                ActivityCompat.finishAfterTransition(PreviewSingleImageActivity.this);
                return true;
            }
        });
    }
}
