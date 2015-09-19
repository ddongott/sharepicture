package com.example.ddong.xphoto;

import android.graphics.Bitmap;

/**
 * Created by ddong on 2015-09-13.
 */
public class ImageItem {
    private Bitmap image;
    private String title;
    private String path;

    public ImageItem(Bitmap image, String title, String path) {
        super();
        this.image = image;
        this.title = title;
        this.path = path;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
