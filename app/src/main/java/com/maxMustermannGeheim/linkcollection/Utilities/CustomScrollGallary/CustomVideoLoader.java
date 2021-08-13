package com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.finn.androidUtilities.CustomUtility;
import com.github.chrisbanes.photoview.PhotoView;
import com.maxMustermannGeheim.linkcollection.Activities.Content.MediaActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.squareup.picasso.Callback;
import com.veinhorn.scrollgalleryview.Constants;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.VideoPlayerActivity;
import com.veinhorn.scrollgalleryview.loader.DefaultVideoLoader;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomVideoLoader extends DefaultVideoLoader {
    public static final int START_ACTIVITY_VIDEO_PLAYER = 98765;
    private Media media;

    public CustomVideoLoader(String url, int mId) {
        super(url, mId);
    }

    public CustomVideoLoader(Media media) {
        super(media.getImagePath(), 0);
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }

    @Override
    public void loadMedia(Context context, ImageView imageView, SuccessCallback callback) {
        if (media.getImagePath().startsWith("/storage/")) {

            FrameLayout parent = (FrameLayout) imageView.getParent();

            Utility.DoubleGenericInterface<VideoView, Boolean> setPreviewVolume = (singleVideoView, on) -> {
                ((MediaActivity) context).isVideoPreviewSoundOn = on;

                CustomList<VideoView> changeList = singleVideoView != null ? new CustomList<>(singleVideoView) : new CustomList<>(((MediaActivity) context).currentMediaPlayerMap.keySet());
                changeList.forEach(videoView -> {
                    FrameLayout viewParent = (FrameLayout) videoView.getParent();
                    if (on) {
                        viewParent.findViewById(R.id.imageFragment_volumeOn).setVisibility(View.VISIBLE);
                        viewParent.findViewById(R.id.imageFragment_volumeOff).setVisibility(View.GONE);
                        ((MediaActivity) context).currentMediaPlayerMap.get(videoView).setVolume(1f, 1f);
//                        ((MediaActivity) context).currentMediaPlayerMap.values().forEach(mp -> mp.setVolume(1f, 1f));
                    } else {
                        viewParent.findViewById(R.id.imageFragment_volumeOn).setVisibility(View.GONE);
                        viewParent.findViewById(R.id.imageFragment_volumeOff).setVisibility(View.VISIBLE);
                        ((MediaActivity) context).currentMediaPlayerMap.get(videoView).setVolume(0f, 0f);
//                        ((MediaActivity) context).currentMediaPlayerMap.values().forEach(mp -> mp.setVolume(0f, 0f));
                    }
                });
            };

            parent.findViewById(R.id.imageFragment_volumeLayout).setOnClickListener(v -> setPreviewVolume.run(null, !((MediaActivity) context).isVideoPreviewSoundOn));

            parent.findViewById(R.id.imageFragment_videoButtonLayout).setVisibility(View.VISIBLE);
            int visibility = ((AppCompatActivity) context).findViewById(com.veinhorn.scrollgalleryview.R.id.thumbnails_scroll_view).getVisibility();
            parent.findViewById(R.id.imageFragment_playVideo).setVisibility(visibility);
            parent.findViewById(R.id.imageFragment_volumeLayout).setVisibility(visibility);


            VideoView videoView = parent.findViewById(R.id.imageFragment_video);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoPath(media.getImagePath());
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                ((MediaActivity) context).currentMediaPlayerMap.put(videoView, mp);
                setPreviewVolume.run(videoView, ((MediaActivity) context).isVideoPreviewSoundOn);
            });

            videoView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    if (context instanceof MediaActivity) {
                        MediaActivity.shouldVideoPreviewStart((MediaActivity) context, videoView, media);
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    ((MediaActivity) context).currentMediaPlayerMap.remove(videoView);
                }
            });


//            videoView.setOnTouchListener((v, event) -> imageView.onTouchEvent(event));

//            Glide.with(context)
//                    .load(new File(media.getImagePath()))
//                    .into(imageView);
//            parent.setOnClickListener(CustomUtility.getOnClickListener(imageView));
//            parent.setClickable(true);
//            imageView.setVisibility(View.INVISIBLE);
            imageView.setAlpha(0f);
//            ((PhotoView) imageView).setZoomable(false);

            parent.findViewById(R.id.imageFragment_playVideo).setOnClickListener(v -> {
                displayVideo(context, media.getImagePath());
            });

//            long[] clickedAt = {0L, 0L, 0L};
//
//            imageView.setOnTouchListener((view, motionEvent) -> {
//                clickedAt[0] = new Date().getTime();
//                clickedAt[1] = (long) motionEvent.getX();
//                clickedAt[2] = (long) motionEvent.getY();
//                return false;
//            });
//            parent.setOnClickListener(v -> {
//                if (true/*new Date().getTime() - clickedAt[0] < 200*/) {
//                    RectF imageBounds = getImageBounds(imageView);
//                    boolean outHor = clickedAt[1] < imageBounds.left || clickedAt[1] > imageBounds.right;
//                    boolean outVert = clickedAt[2] < imageBounds.top || clickedAt[2] > imageBounds.bottom;
//                    if (outHor || outVert) {
//                        ScrollGalleryView galleryView = ((AppCompatActivity) context).findViewById(R.id.scroll_gallery_view);
//                        boolean visible = ((AppCompatActivity) context).findViewById(com.veinhorn.scrollgalleryview.R.id.thumbnails_scroll_view).getVisibility() == View.VISIBLE;
//                        if (visible) {
//                            galleryView.hideThumbnails();
//                        }
//                        else {
//                            galleryView.showThumbnails();
//                        }
//                        try {
//                            Field field;
//                            field = ScrollGalleryView.class.getDeclaredField("isThumbnailsHidden");
//                            field.setAccessible(true);
//                            field.set(galleryView, visible);
//                        } catch (NoSuchFieldException | IllegalAccessException ignored) {
//                        }
//                        MediaActivity.toggleDescriptionVisibility((AppCompatActivity) context);
//                    } else
//                        displayVideo(context, media.getImagePath());
//                }
//            });

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
                            .override(200, 200)
                            .centerCrop())
                    .into(thumbnailView);
        } else
            super.loadThumbnail(context, thumbnailView, callback);
    }

    private void displayVideo(Context context, String url) {
        if (!new File(url).exists())
            return;
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(Constants.URL, url);
        ((AppCompatActivity) context).startActivityForResult(intent, START_ACTIVITY_VIDEO_PLAYER);
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
