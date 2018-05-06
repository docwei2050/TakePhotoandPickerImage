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

public class ImageSingleActivity extends AppCompatActivity{
    private int TAKE_PHOTO   = 100;
    private int SELECT_ALBUM = 101;
    private ImageView mIv_selected;
    private ImageView mIv_deleted;
    private Uri         mImageUri;
    private String mPhotoUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_single);
        mIv_selected = findViewById(R.id.iv_selected);
        mIv_deleted = findViewById(R.id.iv_deleted);
        mIv_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mPhotoUrl)) {
                    show(1);
                }else{
                    PreviewSingleImageActivity.startActivity(ImageSingleActivity.this, mPhotoUrl, mIv_selected);
                }
            }
        });
        mIv_deleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoUrl=null;
                displayImage(mPhotoUrl);
            }
        });
    }
    private void show(final int count) {
        TakePhotoVH viewHolder = new TakePhotoVH(this);

        DialogPlus dialog = DialogPlus.newDialog(this)
                                      .setContentHolder(viewHolder)
                                      .setCancelable(true)
                                      .setGravity(Gravity.BOTTOM)
                                      .create();
        dialog.show();
        viewHolder.setCameraAndPhotoListener(new TakePhotoVH.ICameraAndPhotoListener() {
            @Override
            public void takePhoto() {
                //使用相机拍照显示图片，这里保存在该app的关联目录--缓存目录下，所以无需进行外置SD卡的读取权限
                //注意：调用系统相册拍照无需使用相机权限
                //但是由于4.4以前的系统访问关联目录--缓存目录需要sd卡权限，我们为了兼容老版本加上。。

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                                                        Locale.getDefault()).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_"+".jpg";
                File   outputImage   = new File(getExternalCacheDir(), imageFileName);
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    mImageUri = FileProvider.getUriForFile(ImageSingleActivity.this,
                                                           "com.docwei.cameraphotodemo.fileprovider",
                                                           outputImage);
                } else {
                    mImageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(intent, TAKE_PHOTO);

            }

            @Override
            public void selectAlbum() {
                CheckPermission.getInstance(ImageSingleActivity.this)
                               .request(new PermissionOptions.Builder().setRationalMessage(
                                       "要允许酒葫芦访问您设备上的图片、媒体内容吗？")
                                                                       .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                                       .build(),
                                        new PermissionResultListener() {
                                            @Override
                                            public void onGranted() {
                                                //使用系统自带的图片选择功能
                                                //openAlbum();

                                                //使用非系统的实现
                                                ImageChooseActivity.startForResult(ImageSingleActivity.this, count, SELECT_ALBUM);
                                            }

                                            @Override
                                            public void onDenied(List<String> permissions) {
                                                Toast.makeText(ImageSingleActivity.this,
                                                               "您拒绝访问图片的权限了，所以无法使用图片",
                                                               Toast.LENGTH_SHORT)
                                                     .show();
                                            }

                                        });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                mPhotoUrl = mImageUri.toString();
                displayImage(mPhotoUrl);
            }
        } else if (requestCode == SELECT_ALBUM) {
            if (resultCode == RESULT_OK) {
                if(data!=null){
                    ArrayList<String> list = (ArrayList<String>) data.getSerializableExtra("images");
                    if(list!=null&& list.size()>0) {
                        mPhotoUrl=list.get(0);
                        displayImage(mPhotoUrl);
                    }
                }
            }
        }
    }
    public void displayImage(String url){
        GlideApp.with(this).load(url).placeholder(R.mipmap.icon_add_img).error(R.mipmap.icon_add_img).into(mIv_selected);
        mIv_deleted.setVisibility(TextUtils.isEmpty(url)?View.GONE:View.VISIBLE);
    }
}
