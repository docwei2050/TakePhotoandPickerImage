package com.docwei.cameraphotodemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.docwei.imageupload_lib.GlideApp;
import com.docwei.imageupload_lib.album.type.UsageTypeConstant;
import com.docwei.imageupload_lib.album.ui.ImageSelectProxyActivity;
import com.docwei.imageupload_lib.album.ui.PreviewSingleImageActivity;
import com.docwei.imageupload_lib.constant.ImageConstant;
import com.docwei.imageupload_lib.view.RectImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.Luban;

public class ImageActivity extends AppCompatActivity {


    private ImageView mIv;
    private RecyclerView mRv_image;
    private ImageSelectedAdapter mImagesAdapter;
    private TextView mTv_upload;
    private ImageView mIv_logo;

    private String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRv_image = findViewById(R.id.recycler_img);
        mTv_upload = findViewById(R.id.tv_upload);
        mIv_logo = findViewById(R.id.iv_logo);

        mRv_image.setLayoutManager(new GridLayoutManager(this, 4));
        mImagesAdapter = new ImageSelectedAdapter(this, 9);
        mRv_image.setAdapter(mImagesAdapter);
        initClick8();

    }


    private void initClick8() {
        mImagesAdapter.setOnImageHandleListener(new ImageSelectedAdapter.OnImageHandleListener() {
            @Override
            public void previewImage(String imagePath, RectImageView iv) {
                PreviewSingleImageActivity.startActivity(ImageActivity.this, imagePath, iv);
            }

            @Override
            public void addImages(int count) {
                mType = UsageTypeConstant.OTHER;
                // 添加图片，每次添加9张
                ImageSelectProxyActivity.selectImage(ImageActivity.this, UsageTypeConstant.OTHER, 9);
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
                                    throws Exception {

                                return Luban.with(ImageActivity.this)
                                        .setTargetDir(getPath())
                                        .load(list)
                                        .get();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<File>>() {
                            @Override
                            public void accept(@NonNull List<File> list)
                                    throws Exception {
                                //发射一个上传一个到七牛云
                                Toast.makeText(ImageActivity.this, "压缩完成", Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });
        mIv_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = UsageTypeConstant.HEAD_PORTRAIT;
                // 更换头像 ，每次只有一张
                ImageSelectProxyActivity.selectImage(ImageActivity.this, UsageTypeConstant.HEAD_PORTRAIT, 1);
            }
        });
    }

    private String getPath() {
        String path = getExternalCacheDir() + "/images";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            if (requestCode == ImageConstant.REQUEST_CODE_IAMGES) {
                ArrayList<String> list = (ArrayList<String>) data.getSerializableExtra(ImageConstant.SELECTED_IAMGES);
                //场景一：评论等上传 这里不裁剪
                if (mType.equals(UsageTypeConstant.OTHER)) {
                    mImagesAdapter.updateDataFromAlbum(list);
                }


                if (mType.equals(UsageTypeConstant.HEAD_PORTRAIT)) {
                    //场景二：头像等上传 有裁剪操作
                    if (list != null && list.size() > 0) {
                        GlideApp.with(this).load(list.get(0)).circleCrop().into(mIv_logo);
                    }
                }
            }
        }
    }
}
