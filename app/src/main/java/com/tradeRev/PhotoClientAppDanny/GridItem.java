package com.tradeRev.PhotoClientAppDanny;

import java.io.Serializable;

public class GridItem implements Serializable{
    private String image;
    private String imageRaw;
    private String title;

    public GridItem() {
        super();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageRaw(String imageRaw){ this.imageRaw = imageRaw; }

    public String getImageRaw(){ return imageRaw; }
}
