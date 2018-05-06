package com.docwei.cameraphotodemo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.docwei.cameraphotodemo.album.ImageChooseActivity;
import com.docwei.cameraphotodemo.album.PreviewSingleImageActivity;
import com.docwei.cameraphotodemo.dialog.DialogPlus;
import com.docwei.cameraphotodemo.permission.CheckPermission;
import com.docwei.cameraphotodemo.permission.PermissionOptions;
import com.docwei.cameraphotodemo.permission.PermissionResultListener;
import com.docwei.cameraphotodemo.single.BaseImageActivity;
import com.docwei.cameraphotodemo.single.SingleImagePickerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by git on 2018/5/6.
 */

public class ImageSingleActivity extends BaseImageActivity{
    private int TAKE_PHOTO_1   = 100;
    private int TAKE_PHOTO_2   = 200;
    private int TAKE_PHOTO_3   = 300;
    private int SELECT_ALBUM_1 = 101;
    private int SELECT_ALBUM_2 = 201;
    private int SELECT_ALBUM_3 = 301;
    private SingleImagePickerView mPicker3;
    private SingleImagePickerView mPicker2;
    private SingleImagePickerView mPicker1;
    private List<SingleImagePickerView> mList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_single);
        mPicker1 = findViewById(R.id.pick);
        mPicker1.init(this).setRequestCode(TAKE_PHOTO_1, SELECT_ALBUM_1);
        mPicker2 = findViewById(R.id.pick2);
        mPicker2.init(this).setRequestCode(TAKE_PHOTO_2, SELECT_ALBUM_2);
        mPicker3 = findViewById(R.id.pick3);
        mPicker3.init(this).setRequestCode(TAKE_PHOTO_3, SELECT_ALBUM_3);
        mList = new ArrayList<>();
        mList.add(mPicker1);
        mList.add(mPicker2);
        mList.add(mPicker3);
    }




    @Override
    public List<SingleImagePickerView> getImagePickView() {
        return mList;
    }


}
