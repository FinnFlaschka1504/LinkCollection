package com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary;


import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;
import com.veinhorn.scrollgalleryview.loader.picasso.PicassoImageLoader;

import java.io.File;

public class CustomGlideImageLoader extends PicassoImageLoader {
    private Media media;
//    private String imagePath;
    private Integer thumbWidth;
    private Integer thumbHeight;

    public CustomGlideImageLoader(Media media) {
        super(media.getImagePath());
        this.media = media;
    }

    public CustomGlideImageLoader(String url) {
        super(url);
    }

    public CustomGlideImageLoader(String url, Integer thumbnailWidth, Integer thumbnailHeight) {
        super(url, thumbnailWidth, thumbnailHeight);
        thumbWidth = thumbnailWidth;
        thumbHeight = thumbnailHeight;
    }

    public Media getMedia() {
        return media;
    }

    @Override
    public void loadMedia(Context context, ImageView imageView, SuccessCallback callback) {
        if (media.getImagePath().startsWith("/storage/")) {
            ((PhotoView) imageView).setScaleLevels(1f,2.5f, 6f);
            Glide.with(context)
                    .load(new File(media.getImagePath()))
                    .placeholder(com.veinhorn.scrollgalleryview.loader.picasso.R.drawable.placeholder_image)
                    .error(R.drawable.ic_broken_image)
                    .into(imageView);
//            ((TextView) ((FrameLayout) imageView.getParent()).findViewById(R.id.imageFragment_description)).setText(media._getDescription());
        } else
            super.loadMedia(context, imageView, callback);
    }

    @Override
    public void loadThumbnail(Context context, final ImageView thumbnailView, final SuccessCallback callback) {
        if (media.getImagePath().startsWith("/storage/")) {
            Picasso.get()
                    .load(new File(media.getImagePath()))
                    .resize(thumbWidth == null ? 100 : thumbWidth,
                            thumbHeight == null ? 100 : thumbHeight)
                    .placeholder(com.veinhorn.scrollgalleryview.loader.picasso.R.drawable.placeholder_image)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(thumbnailView, new ImageCallback(callback));
        } else
            super.loadThumbnail(context, thumbnailView, callback);
    }


    private static class ImageCallback implements Callback {
        private final MediaLoader.SuccessCallback callback;

        public ImageCallback(SuccessCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess() {
            callback.onSuccess();
        }

        @Override
        public void onError(Exception e) {
        }
    }

}
