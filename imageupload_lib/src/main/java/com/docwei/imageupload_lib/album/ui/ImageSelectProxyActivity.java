package com.docwei.imageupload_lib.album.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.docwei.imageupload_lib.R;
import com.docwei.imageupload_lib.album.TakePhotoVH;
import com.docwei.imageupload_lib.album.type.UsageType;
import com.docwei.imageupload_lib.album.type.UsageTypeConstant;
import com.docwei.imageupload_lib.constant.ImageConstant;
import com.docwei.imageupload_lib.dialog.DialogPlus;
import com.docwei.imageupload_lib.permission.Acp;
import com.docwei.imageupload_lib.permission.AcpListener;
import com.docwei.imageupload_lib.permission.AcpOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 此为透明act
 * 弹窗和选择图片的回调
 */
public class ImageSelectProxyActivity extends AppCompatActivity {

    private Uri mImageUri;
    private int TAKE_PHOTO = 100;
    private int SELECT_ALBUM = 101;
    private String mType;
    private int mCount;

    /**
     * @param type    使用类型
     * @param context
     * @param count   上传的数量
     */
    public static void selectImage(Activity context,@UsageType String type, int count) {
        Intent intent = new Intent(context, ImageSelectProxyActivity.class);
        intent.putExtra(ImageConstant.TYPE, type);
        intent.putExtra(ImageConstant.COUNT, count);
        context.startActivityForResult(intent, ImageConstant.REQUEST_CODE_IAMGES);
        context.overridePendingTransition(0, 0);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select_proxy);
        Intent intent = getIntent();
        mType = intent.getStringExtra(ImageConstant.TYPE);
        mCount = intent.getIntExtra(ImageConstant.COUNT, 9);
        show(mCount);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent!=null){
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void show(final int count) {

        TakePhotoVH viewHolder = new TakePhotoVH(this);

        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(viewHolder)
                .setCancelable(false)
                .setMargin(0, 100, 0, 0)
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
                String imageFileName = "JPEG_" + timeStamp + "_" + ".jpg";
                File outputImage = new File(getExternalCacheDir(), imageFileName);
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    mImageUri = FileProvider.getUriForFile(ImageSelectProxyActivity.this,
                            "com.docwei.imageupload_lib.fileprovider",
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
                Acp.getInstance(ImageSelectProxyActivity.this)
                        .request(new AcpOptions.Builder().setRationalMessage(
                                "要允许本应用访问您设备上的图片、媒体内容吗？").setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).build(),
                                new AcpListener() {
                                    @Override
                                    public void onGranted() {
                                        //使用系统自带的图片选择功能
                                        //openAlbum();
                                        //使用非系统的实现
                                        if (mType.equals(UsageTypeConstant.HEAD_PORTRAIT)) {
                                            ImageChooseActivity.startUp(ImageSelectProxyActivity.this, count, mType);

                                        } else {
                                            ImageChooseActivity.startForResult(ImageSelectProxyActivity.this, count, SELECT_ALBUM, mType);
                                        }
                                    }

                                    @Override
                                    public void onDenied(List<String> permissions) {
                                        Toast.makeText(ImageSelectProxyActivity.this, "您拒绝访问图片的权限了，所以无法使用图片", Toast.LENGTH_SHORT).show();
                                    }

                                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            ArrayList<String> images;
            Intent intent = new Intent();
            if (requestCode == TAKE_PHOTO) {
                //头像类型，需要裁剪
                if(mType.equals(UsageTypeConstant.HEAD_PORTRAIT)){
                    CropImageActivity.startActivity(this,mImageUri.toString());
                    return;
                }else {
                //其他类型，一律不裁剪
                    images = new ArrayList<>(1);
                    images.add(mImageUri.toString());
                    intent.putExtra(ImageConstant.SELECTED_IAMGES, images);
                }
            } else if (requestCode == SELECT_ALBUM) {
                if (data != null) {
                    intent = data;
                }
            }
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
