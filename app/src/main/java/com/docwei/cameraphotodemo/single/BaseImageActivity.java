package com.docwei.cameraphotodemo.single;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by git on 2018/5/6.
 */

public abstract class BaseImageActivity extends AppCompatActivity {

 /*
    public ISelectImageListener mSelectImageListener;

    public void setISelectImageListener(ISelectImageListener ISelectImageListener) {
        mSelectImageListener = ISelectImageListener;
    }

    public interface ISelectImageListener{
        void selectImage(String imagePath);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        List<SingleImagePickerView> list=getImagePickView();
        if(list!=null&&list.size()>0){
            for(SingleImagePickerView view:list){
                if (requestCode == view.getTAKE_PHOTO()) {
                    if (resultCode == RESULT_OK) {
                        if(null!=view.getImageUri()) {
                            view.setPhotoUrl(view.getImageUri()
                                                 .toString());
                            view.displayImage(view.getImageUri()
                                                  .toString());
                        }
                    }
                } else if (requestCode == view.getSELECT_ALBUM()) {
                    if (resultCode == RESULT_OK) {
                        if(data!=null){
                            ArrayList<String> imagePaths = (ArrayList<String>) data.getSerializableExtra("images");
                            if(imagePaths!=null&& imagePaths.size()>0) {
                                view.setPhotoUrl(imagePaths.get(0));
                                view.displayImage(imagePaths.get(0));
                            }
                        }
                    }
                }
            }
        }

    }
    public abstract List<SingleImagePickerView> getImagePickView();

}
