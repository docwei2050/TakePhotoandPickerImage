package com.docwei.cameraphotodemo.album;

/**
 * Created by git on 2018/4/29.
 */

public class ImageBean  {
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
