package com.maxMustermannGeheim.linkcollection.Activities.Content.Media;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.R;
import com.veinhorn.scrollgalleryview.Constants;

import java.lang.reflect.Field;

public class VideoPlayerActivity extends AppCompatActivity {
    public static final String EXTRA_URI = "EXTRA_URI";
    public static final String EXTRA_TIME = "EXTRA_TIME";
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        String url = getIntent().getExtras().getString(EXTRA_URI);
        int time = Integer.parseInt(getIntent().getExtras().getString(EXTRA_TIME));
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        final VideoView videoView = findViewById(R.id.videoView);
        videoView.setOnPreparedListener(mp -> {
            if (time > 2000)
                mp.seekTo(time);
            mediaPlayer = mp;
            mediaPlayer.setLooping(true);
            findViewById(R.id.videoProgress).setVisibility(View.GONE);

            View changeRotationButton = findViewById(R.id.videoPlayer_rotation);
            MediaActivity.applyChangeRotationButton(this, changeRotationButton);
            videoView.requestFocus();
            final int[] counter = {0};
            Runnable[] runHide = {() -> {}};

            MediaController vidControl = new MediaController(VideoPlayerActivity.this) {
                @Override
                public void hide() {
                    super.hide();
                    changeRotationButton.setVisibility(View.GONE);
                }

                @Override
                public void show() {
                    super.show(0);
                    counter[0]++;
                    postDelayed(runHide[0], 5000);
                    if (!MediaActivity.isAutoRotationOn(VideoPlayerActivity.this))
                        changeRotationButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void show(int timeout) {
                    super.show(0);
                    counter[0]++;
                    postDelayed(runHide[0], 5000);
                }
            };

            vidControl.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    CustomUtility.setPadding(vidControl.getChildAt(0), 15, 0);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            });
            runHide[0] = () -> {
                if (counter[0] == 1) {
                    if (videoView.isPlaying())
                        vidControl.hide();
                }
                if (counter[0] > 0)
                    counter[0]--;
            };
            vidControl.setPrevNextListeners(v -> Toast.makeText(this, "ToDo", Toast.LENGTH_SHORT).show(), v -> Toast.makeText(this, "ToDo", Toast.LENGTH_SHORT).show());
            View parent = (View) videoView.getParent();
            parent.setOnClickListener(v -> {
                if (vidControl.isShowing()) {
                    vidControl.hide();
                } else {
                    vidControl.show();
                }
            });
            videoView.setOnTouchListener((v, event) -> parent.onTouchEvent(event));
            vidControl.setAnchorView(parent);
            videoView.setMediaController(vidControl);
            videoView.start();
            vidControl.show();
        });
        videoView.setVideoURI(Uri.parse(url));

//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);


    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_TIME, String.valueOf(mediaPlayer.getCurrentPosition()));
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
//        super.onBackPressed();
    }
}