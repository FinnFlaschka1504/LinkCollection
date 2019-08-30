package com.maxMustermannGeheim.linkcollection.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.maxMustermannGeheim.linkcollection.Daten.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;

import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends AppCompatActivity {
    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        database = Database.getInstance();

        loadVideoRecycler();
    }

    private void loadVideoRecycler() {
        List<Video> videoList = new ArrayList<>(database.videoMap.values());
        videoList.sort((video1, video2) -> video1.getTitel().compareTo(video2.getTitel()));

        CustomRecycler.Builder(this, findViewById(R.id.videos_recycler))
                .setItemView(R.layout.list_item_video)
                .setObjectList(videoList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.listItem_video_Titel);
                    viewIdList.add(R.id.listItem_video_Darsteller);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    Video video = (Video) object;
                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));

                    ((TextView) viewIdMap.get(R.id.listItem_video_Titel)).setText(video.getTitel());
                    ((TextView) viewIdMap.get(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    viewIdMap.get(R.id.listItem_video_Darsteller).setSelected(true);
                })
                .setUseCustomRipple(true)
                .setOnClickListener((recycler, view, object, index) -> {
                    Toast.makeText(this, "Show: " + ((Video) object).getTitel(), Toast.LENGTH_SHORT).show();
                })
                .addSubOnClickListener(R.id.listItem_video_edit, (recycler, view, object, index) -> {
                    Toast.makeText(this, "Edit: " + ((Video) object).getTitel(), Toast.LENGTH_SHORT).show();
                }, false)
                .setShowDivider(false)
                .generate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.random) {
            Video video = (Video) database.videoMap.values().toArray()[(int) (Math.random() * database.videoMap.size())];
            Toast.makeText(this, "Random: " + video.getTitel(), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
