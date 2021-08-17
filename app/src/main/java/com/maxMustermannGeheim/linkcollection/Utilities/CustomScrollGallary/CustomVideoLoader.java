package com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Media.MediaActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Media.VideoPlayerActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.squareup.picasso.Callback;
import com.veinhorn.scrollgalleryview.Constants;
import com.veinhorn.scrollgalleryview.loader.DefaultVideoLoader;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import java.io.File;

public class CustomVideoLoader extends DefaultVideoLoader {
    public static final int START_ACTIVITY_VIDEO_PLAYER = 98765;
    private Media media;
    private MediaPlayer mediaPlayer;

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
                    } else {
                        viewParent.findViewById(R.id.imageFragment_volumeOn).setVisibility(View.GONE);
                        viewParent.findViewById(R.id.imageFragment_volumeOff).setVisibility(View.VISIBLE);
                        ((MediaActivity) context).currentMediaPlayerMap.get(videoView).setVolume(0f, 0f);
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
                mediaPlayer = mp;
                mediaPlayer.getTimestamp();
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

    private void displayVideo(Context context, String uri) {
        if (!new File(uri).exists())
            return;
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.EXTRA_URI, uri);
        if (mediaPlayer != null)
            intent.putExtra(VideoPlayerActivity.EXTRA_TIME, String.valueOf(mediaPlayer.getCurrentPosition()));


//        ActivityResultLauncher<Intent> someActivityResultLauncher = ((AppCompatActivity) context).registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        // Here, no request code
//                        Intent data = result.getData();
//                    }
//                });
//        someActivityResultLauncher.launch(intent);
        final int[] seekTime = {-1};

//        ((AppCompatActivity) context).getLifecycle().addObserver(new LifecycleObserver() {
//            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//            public void onResumed() {
//                if (mediaPlayer != null && seekTime[0] != -1) {
//                    mediaPlayer.seekTo(seekTime[0]);
//                    seekTime[0] = -1;
//                }
//                String BREAKPOINT = null;
//            }
//
////            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
////            public void disconnectListener() {
////                String BREAKPOINT = null;
////            }
//        });

//        ActivityResultHelper.addGenericRequest((AppCompatActivity) context, activity -> activity.startActivityForResult(intent, START_ACTIVITY_VIDEO_PLAYER), (requestCode, resultCode, data) -> {
//            String string = data.getExtras().getString(VideoPlayerActivity.EXTRA_TIME);
//            int msec = Integer.parseInt(string);
//            seekTime[0] = msec;
////            new Handler(Looper.myLooper()).postDelayed(() -> {
////                mediaPlayer.start();
////                mediaPlayer.seekTo(msec);
////
////            }, 3000);
//        });
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
