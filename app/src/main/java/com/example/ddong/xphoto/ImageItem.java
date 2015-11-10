package com.example.ddong.xphoto;

import android.graphics.Bitmap;

/**
 * Created by ddong on 2015-09-13.
 */
public class ImageItem {
    private int id;
    private Bitmap image;
    private String title;
    private String path;
    private String owner;
    private String serverid;

    public ImageItem(Bitmap image, String path, int id) {
        super();
        this.image = image;
        this.path = path;
        this.id = id;
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

    public void setServerid(String serverid) { this.serverid = serverid; }

    public String getServerid() { return serverid; }

    public void setOwner(String owner) { this.owner = owner; }

    public String getOwner() { return owner; }

}
