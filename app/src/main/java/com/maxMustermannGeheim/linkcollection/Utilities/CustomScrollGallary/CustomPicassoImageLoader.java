package com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary;


import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;
import com.veinhorn.scrollgalleryview.loader.picasso.PicassoImageLoader;

import java.io.File;

public class CustomPicassoImageLoader extends PicassoImageLoader {
    private Media media;
//    private String imagePath;
    private Integer thumbWidth;
    private Integer thumbHeight;

    public CustomPicassoImageLoader(Media media) {
        super(media.getImagePath());
        this.media = media;
    }

    public CustomPicassoImageLoader(String url) {
        super(url);
    }

    public CustomPicassoImageLoader(String url, Integer thumbnailWidth, Integer thumbnailHeight) {
        super(url, thumbnailWidth, thumbnailHeight);
        thumbWidth = thumbnailWidth;
        thumbHeight = thumbnailHeight;
    }

    @Override
    public void loadMedia(Context context, ImageView imageView, SuccessCallback callback) {
        if (media.getImagePath().startsWith("/storage/")) {
            Glide.with(context)
                    .load(new File(media.getImagePath()))
                    .placeholder(com.veinhorn.scrollgalleryview.loader.picasso.R.drawable.placeholder_image)
                    .into(imageView);

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
                    .centerInside()
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
