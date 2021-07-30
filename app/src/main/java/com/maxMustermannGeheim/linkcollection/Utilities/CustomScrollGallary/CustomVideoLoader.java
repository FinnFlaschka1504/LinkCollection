package com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.R;
import com.squareup.picasso.Callback;
import com.veinhorn.scrollgalleryview.Constants;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.VideoPlayerActivity;
import com.veinhorn.scrollgalleryview.loader.DefaultVideoLoader;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import java.io.File;
import java.util.Date;

public class CustomVideoLoader extends DefaultVideoLoader {
//    String videoPath;
    Media media;

    public CustomVideoLoader(String url, int mId) {
        super(url, mId);
    }

    public CustomVideoLoader(Media media) {
        super(media.getImagePath(), 0);
    }

    @Override
    public void loadMedia(Context context, ImageView imageView, SuccessCallback callback) {
        if (media.getImagePath().startsWith("/storage/")) {
            Glide.with(context)
                    .load(new File(media.getImagePath()))
                    .into(imageView);

            ((FrameLayout) imageView.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
//            Picasso.get()
//                    .load(new File(videoPath))
//                    .placeholder(com.veinhorn.scrollgalleryview.loader.picasso.R.drawable.placeholder_image)
//                    .into(imageView, new ImageCallback(callback));
            GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return super.onSingleTapUp(e);
                }

                @Override
                public void onShowPress(MotionEvent e) {
                    super.onShowPress(e);
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    return super.onDown(e);
                }
            };

            long[] clickedAt = {0L, 0L, 0L};

//            GestureDetectorCompat gestureDetectorCompat = new GestureDetectorCompat(context, simpleOnGestureListener);

            imageView.setOnTouchListener((view, motionEvent) -> {
//                Toast.makeText(context, "" + motionEvent.getAction(), Toast.LENGTH_SHORT).show();
//                return gestureDetectorCompat.onTouchEvent(motionEvent);
                clickedAt[0] = new Date().getTime();
                clickedAt[1] = (long) motionEvent.getX();
                clickedAt[2] = (long) motionEvent.getY();
                return false;
            });
            ((FrameLayout) imageView.getParent()).setOnClickListener(v -> {
                if (true/*new Date().getTime() - clickedAt[0] < 200*/) {
                    RectF imageBounds = getImageBounds(imageView);
                    boolean outHor = clickedAt[1] < imageBounds.left || clickedAt[1] > imageBounds.right;
                    boolean outVert = clickedAt[2] < imageBounds.top || clickedAt[2] > imageBounds.bottom;
                    if (outHor || outVert) {
                        ScrollGalleryView galleryView = ((AppCompatActivity) context).findViewById(R.id.scroll_gallery_view);
                        if (((AppCompatActivity) context).findViewById(com.veinhorn.scrollgalleryview.R.id.thumbnails_scroll_view).getVisibility() == View.VISIBLE) {
                            galleryView.hideThumbnails();
                        }
                        else {
                            galleryView.showThumbnails();
                        }
                    } else
                        displayVideo(context, media.getImagePath());
                }
            });

        } else
            super.loadMedia(context, imageView, callback);
    }

//    class OnImageTouchListener extends GestureDetector.SimpleOnGestureListener

    @Override
    public void loadThumbnail(Context context, final ImageView thumbnailView, final SuccessCallback callback) {
        if (media.getImagePath().startsWith("/storage/")) {
            Glide.with(context)
                    .load(media.getImagePath())
                    .apply(new RequestOptions()
                            .override(100, 100)
                            .centerCrop())
                    .into(thumbnailView);
//            Picasso.get()
//                    .load(new File(videoPath))
////                    .resize(thumbWidth == null ? 100 : thumbWidth,
////                            thumbHeight == null ? 100 : thumbHeight)
//                    .resize(100, 100)
//                    .placeholder(com.veinhorn.scrollgalleryview.loader.picasso.R.drawable.placeholder_image)
//                    .centerInside()
//                    .into(thumbnailView, new ImageCallback(callback));
        } else
            super.loadThumbnail(context, thumbnailView, callback);
    }

    private void displayVideo(Context context, String url) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(Constants.URL, url);
        context.startActivity(intent);
    }

    /**
     * Helper method to get the bounds of image inside the imageView.
     *
     * @param imageView the imageView.
     * @return bounding rectangle of the image.
     */
    public static RectF getImageBounds(ImageView imageView) {
        RectF bounds = new RectF();
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            imageView.getImageMatrix().mapRect(bounds, new RectF(drawable.getBounds()));
        }
        return bounds;
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
