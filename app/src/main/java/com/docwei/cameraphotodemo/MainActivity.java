package com.docwei.cameraphotodemo;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.docwei.cameraphotodemo.album.GriditemDecoration;
import com.docwei.cameraphotodemo.album.ImageChooseActivity;
import com.docwei.cameraphotodemo.album.PreviewSingleImageActivity;
import com.docwei.cameraphotodemo.album.RectImageView;
import com.docwei.cameraphotodemo.dialog.DialogPlus;
import com.docwei.cameraphotodemo.permission.CheckPermission;
import com.docwei.cameraphotodemo.permission.PermissionOptions;
import com.docwei.cameraphotodemo.permission.PermissionResultListener;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity {

    private TextView    mTv_show;
    private ViewGroup   mDecorView;
    private FrameLayout mBaseContainer;
    private FrameLayout mContent_container;
    private View        mContentView;
    private FrameLayout mContent_container1;
    private Uri         mImageUri;

    private int TAKE_PHOTO   = 100;
    private int SELECT_ALBUM = 101;
    private ImageView mIv;
    private RecyclerView mRv_image;
    private ImageSelectedAdapter mImagesAdapter;
    private TextView mTv_upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv_show = findViewById(R.id.tv_show);
        mIv = findViewById(R.id.iv);
        mRv_image = findViewById(R.id.recycler_img);
        mTv_upload = findViewById(R.id.tv_upload);
        mRv_image.setLayoutManager(new GridLayoutManager(this,4));
        mImagesAdapter = new ImageSelectedAdapter(this,9);

        mRv_image.setAdapter(mImagesAdapter);
        initClick8();

    }


    private void initClick8() {
        mImagesAdapter.setOnImageHandleListener(new ImageSelectedAdapter.OnImageHandleListener() {
            @Override
            public void previewImage(String imagePath, RectImageView iv) {
                PreviewSingleImageActivity.startActivity(MainActivity.this,imagePath,iv);
            }

            @Override
            public void addImages(int count) {
                show(count);
            }
        });
        mTv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> list = mImagesAdapter.getSelectImages();
                if (list.size() == 0) {
                    //无须压缩
                    return;
                }
                Flowable.just(list)
                        .observeOn(Schedulers.io())
                        .map(new Function<List<String>, List<File>>() {
                            @Override
                            public List<File> apply(@NonNull List<String> list)
                                    throws Exception
                            {

                                return Luban.with(MainActivity.this)
                                            .setTargetDir(getPath())
                                            .load(list)
                                            .get();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<File>>() {
                            @Override
                            public void accept(@NonNull List<File> list)
                                    throws Exception
                            {
                                //发射一个上传一个到七牛云
                                Toast.makeText(MainActivity.this, "压缩完成", Toast.LENGTH_SHORT).show();
                            }
                        });


            }});

    }
    private String getPath() {
        String path = getExternalCacheDir() + "/images";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
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
                    mImageUri = FileProvider.getUriForFile(MainActivity.this,
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
                CheckPermission.getInstance(MainActivity.this)
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
                                                ImageChooseActivity.startForResult(MainActivity.this,count,SELECT_ALBUM);
                                            }

                                            @Override
                                            public void onDenied(List<String> permissions) {
                                                Toast.makeText(MainActivity.this,
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
                try {
                    mImagesAdapter.updateDataFromCamera(mImageUri.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == SELECT_ALBUM) {
            if (resultCode == RESULT_OK) {
                if(data!=null){
                    ArrayList<String> list= (ArrayList<String>) data.getSerializableExtra("images");
                    if(list!=null&& list.size()>0) {
                        mImagesAdapter.updateDataFromAlbum(list);
                    }
                }
            }
        }
    }


}
