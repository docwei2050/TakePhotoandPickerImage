package com.docwei.imageupload_lib.album.bean;

/**
 * Created by wk on 2018/4/29.
 * 图片选择javabean
 */

public class ImageBean {
    public String imagePath;
    public boolean isSelect;

    public ImageBean(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

}
