package com.maxMustermannGeheim.linkcollection.Utilities.CustomAdapter;

import android.graphics.Bitmap;

public class ImageAdapterItem {
    private String text;
    private Bitmap image;
    private String imagePath;

    public ImageAdapterItem(String text, String imagePath) {
        this.text = text;
        this.imagePath = imagePath;
    }

    public ImageAdapterItem(String text) {
        this.text = text;
    }

    public ImageAdapterItem(String text, Bitmap image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public ImageAdapterItem setText(String text) {
        this.text = text;
        return this;
    }

    public Bitmap getImage() {
        return image;
    }

    public ImageAdapterItem setImage(Bitmap image) {
        this.image = image;
        return this;
    }

    public String getImagePath() {
        return imagePath;
    }

    public ImageAdapterItem setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }
}
