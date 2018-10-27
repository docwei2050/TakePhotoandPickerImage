package com.docwei.imageupload_lib.album.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.docwei.imageupload_lib.GlideApp;
import com.docwei.imageupload_lib.R;
import com.docwei.imageupload_lib.constant.ImageConstant;
import com.docwei.imageupload_lib.view.CropImageView;

import java.io.File;
import java.util.ArrayList;


public class CropImageActivity extends AppCompatActivity {
    private static final String IMAGE_PATH = "imagePath";
    private static final int EXPECT_WIDTH = 1080; //期望的宽高
    private static final int EXPECT_HEIGHT = 1080;//期望的宽高
    private CropImageView mCropImageView;
    private TextView mTv_use;
    private ImageView mIv_back;

    public static void startActivity(Context context, String filePath) {
        Intent intent = new Intent(context, CropImageActivity.class);
        intent.putExtra(IMAGE_PATH, filePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(getResources().getColor(R.color.color_status_bar));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        mIv_back = findViewById(R.id.iv_back);
        mCropImageView = findViewById(R.id.iv_show);
        mTv_use = findViewById(R.id.tv_use);
        initData();
    }

    private void initData() {
        String imagePath = getIntent().getStringExtra(IMAGE_PATH);
        GlideApp.with(this).load(imagePath)
                .error(R.drawable.img_fail).into(mCropImageView);
        mTv_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.saveBitmapToFile(getExternalCacheDir(), EXPECT_WIDTH, EXPECT_HEIGHT);
            }
        });
        mCropImageView.setOnBitmapSaveCompleteListener(new CropImageView.OnBitmapSaveCompleteListener() {
            @Override
            public void onBitmapSaveSuccess(File file) {
                ArrayList<String> list = new ArrayList<>(1);
                list.add(file.getAbsolutePath());
                Intent intent = new Intent(CropImageActivity.this, ImageSelectProxyActivity.class);
                intent.putExtra(ImageConstant.SELECTED_IAMGES, list);
                startActivity(intent);
                finish();
            }

            @Override
            public void onBitmapSaveError(File file) {
                Toast.makeText(CropImageActivity.this, R.string.msg_crop_error, Toast.LENGTH_SHORT).show();
            }
        });
        mIv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // ImageSelectProxyActivity是一个透明act，手动触发其onNewIntent调用
        Intent intent = new Intent(CropImageActivity.this, ImageSelectProxyActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlideApp.get(this).clearMemory();
    }
}
