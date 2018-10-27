package com.docwei.imageupload_lib.album.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.docwei.imageupload_lib.GlideApp;
import com.docwei.imageupload_lib.R;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by git on 2018/4/29.
 */

public class PreviewSingleImageActivity extends AppCompatActivity {
    public static final String IMAGE_PATH = "imagePath";
    public static final String SHARED_ELEMENT_NAME = "preview";

    public static void startActivity(Activity src, String imagePath, View view) {
        Intent intent = new Intent(src, PreviewSingleImageActivity.class);
        intent.putExtra(IMAGE_PATH, imagePath);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(src, view, "preview");
        ActivityCompat.startActivity(src, intent, optionsCompat.toBundle());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(getResources().getColor(R.color.color_status_bar));
        }
        setContentView(R.layout.activity_preview_single);
        PhotoView iv_show = findViewById(R.id.iv_show);
        ImageView iv_back = findViewById(R.id.iv_back);
        Intent intent = getIntent();
        if (intent != null) {
            String imagePath = intent.getStringExtra(IMAGE_PATH);
            GlideApp.with(this).load(imagePath).into(iv_show);
        }

        ViewCompat.setTransitionName(iv_show, SHARED_ELEMENT_NAME);
        iv_show.setOnDoubleTapListener(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                ActivityCompat.finishAfterTransition(PreviewSingleImageActivity.this);
                return true;
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.finishAfterTransition(PreviewSingleImageActivity.this);
            }
        });
    }
}
