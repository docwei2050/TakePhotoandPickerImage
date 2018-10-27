package com.docwei.imageupload_lib.album.bean;

/**
 * 选择相册的javabean
 */
public class AlbumInfo {
    private String albumName;
    private int photoCounts;
    private String firstPhoto;
    private boolean isSelect;

    public AlbumInfo() {
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getAlbumName() {
        return albumName;
    }

    public AlbumInfo setAlbumName(String albumName) {
        this.albumName = albumName;
        return this;
    }

    public int getPhotoCounts() {
        return photoCounts;
    }

    public AlbumInfo setPhotoCounts(int photoCounts) {
        this.photoCounts = photoCounts;
        return this;
    }

    public String getFirstPhoto() {
        return firstPhoto;
    }

    public AlbumInfo setFirstPhoto(String firstPhoto) {
        this.firstPhoto = firstPhoto;
        return this;
    }
}