package com.docwei.cameraphotodemo;


import android.os.Bundle;
import android.support.annotation.Nullable;


import com.docwei.cameraphotodemo.single.BaseImageActivity;
import com.docwei.cameraphotodemo.single.SingleImagePickerView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by git on 2018/5/6.
 */

public class ImageSingleActivity extends BaseImageActivity {
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
