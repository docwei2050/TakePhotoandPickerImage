package com.docwei.cameraphotodemo.single;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import com.docwei.cameraphotodemo.R;
import com.docwei.imageupload_lib.GlideApp;

import com.docwei.imageupload_lib.TakePhotoVH;
import com.docwei.imageupload_lib.album.ImageChooseActivity;
import com.docwei.imageupload_lib.album.PreviewSingleImageActivity;
import com.docwei.imageupload_lib.dialog.DialogPlus;
import com.docwei.imageupload_lib.permission.CheckPermission;
import com.docwei.imageupload_lib.permission.PermissionOptions;
import com.docwei.imageupload_lib.permission.PermissionResultListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by wk on 2018/5/6.
 * 单张图片上传，适配Fragment和activity
 */
public class SingleImagePickerView extends FrameLayout {
    private int TAKE_PHOTO  ;
    private int SELECT_ALBUM ;
    private ImageView mIv_selected;
    private ImageView mIv_deleted;
    private Uri       mImageUri;
    private String    mPhotoUrl;

    public void setPhotoUrl(String photoUrl) {
        mPhotoUrl = photoUrl;
    }

    public SingleImagePickerView(@NonNull Context context) {
        this(context,null);
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public SingleImagePickerView(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs,0);
    }

    public SingleImagePickerView(@NonNull Context context,
                                 @Nullable AttributeSet attrs,
                                 int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext=context;
        View view=View.inflate(context, R.layout.single_image_picker, this);
        mIv_selected = view.findViewById(R.id.iv_selected);
        mIv_deleted = view.findViewById(R.id.iv_deleted);
    }
    private Context mContext;
    private BaseImageActivity mActivity;

    public  SingleImagePickerView init(BaseImageActivity activity){
        mActivity=activity;
        mIv_selected.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mPhotoUrl)) {
                    show(1);
                }else{
                    PreviewSingleImageActivity.startActivity(mActivity, mPhotoUrl, mIv_selected);
                }
            }
        });
        mIv_deleted.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhotoUrl=null;
                displayImage(mPhotoUrl);
            }
        });
        return this;
    }
    public void setRequestCode(int takePhoto,int select_album){
        TAKE_PHOTO=takePhoto;
        SELECT_ALBUM=select_album;
    }

    public int getTAKE_PHOTO() {
        return TAKE_PHOTO;
    }

    public int getSELECT_ALBUM() {
        return SELECT_ALBUM;
    }

    private void show(final int count) {
        TakePhotoVH viewHolder = new TakePhotoVH(mActivity);

        DialogPlus dialog = DialogPlus.newDialog(mActivity)
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
                File   outputImage   = new File(mContext.getExternalCacheDir(), imageFileName);
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    mImageUri = FileProvider.getUriForFile(mActivity,
                                                           "com.docwei.cameraphotodemo.fileprovider",
                                                           outputImage);
                } else {
                    mImageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                mActivity.startActivityForResult(intent, TAKE_PHOTO);

            }

            @Override
            public void selectAlbum() {
                CheckPermission.getInstance(mActivity)
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
                                                ImageChooseActivity.startForResult(mActivity, count, SELECT_ALBUM);
                                            }

                                            @Override
                                            public void onDenied(List<String> permissions) {
                                                Toast.makeText(mActivity,
                                                               "您拒绝访问图片的权限了，所以无法使用图片",
                                                               Toast.LENGTH_SHORT)
                                                     .show();
                                            }

                                        });
            }
        });
    }


    public void displayImage(String url){
        GlideApp.with(this).load(url).placeholder(R.mipmap.icon_add_img).error(R.mipmap.icon_add_img).into(mIv_selected);
        mIv_deleted.setVisibility(TextUtils.isEmpty(url) ? View.GONE : View.VISIBLE);
    }


}

