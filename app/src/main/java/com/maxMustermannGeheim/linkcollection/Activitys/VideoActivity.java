package com.maxMustermannGeheim.linkcollection.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.maxMustermannGeheim.linkcollection.Daten.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Studio;
import com.maxMustermannGeheim.linkcollection.Daten.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VideoActivity extends AppCompatActivity {
    enum EDIT_TYPE{
        DARSTELLER, STUDIO, GENRE
    }
    Database database;
    SharedPreferences mySPR_daten;
    private boolean delete = false;
    private List<String> toDelete = new ArrayList<>();
    Video randomVideo;
    private boolean scrolling = true;


    private Pair<Dialog, Video> addPasengerDialogVideoPair;
    private Pair<Dialog, Video> editDialogVideoPair;
    private Pair<Dialog, Video> dialogVideoPair;

    private CustomRecycler customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ToDo: databaseCode in den SharedPreferences speichern

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        database = Database.getInstance();
        mySPR_daten = getSharedPreferences("LinkCollection_Daten", 0);

        videos_confirmDelete = findViewById(R.id.videos_confirmDelete);
        videos_confirmDelete.setOnClickListener(view -> {
            for (String uuidVideo : toDelete) {
                database.videoMap.remove(uuidVideo);
            }
            delete = false;
            videos_confirmDelete.setVisibility(View.GONE);

            reLoadVideoRecycler();
            setResult(RESULT_OK);
        });
        loadVideoRecycler();

        ((SearchView) findViewById(R.id.videos_search)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<Video> videoList = new ArrayList<>(database.videoMap.values());
                List<Set<Video>> setList = new ArrayList<>();

                for (String subQuery : s.split("\\|")) {
                    subQuery = subQuery.trim();
                    Set<Video> sortedVideoSet = new HashSet<>();
                    for (Video video : videoList) {
                        if (Utility.containedInVideo(subQuery, video))
                            sortedVideoSet.add(video);
                    }
                    setList.add(sortedVideoSet);
                }

                Set<Video> resultSet = new HashSet<>(setList.get(0));
                for (int i = 1; i < setList.size(); i++) {
                    resultSet.retainAll(setList.get(i));
                }

                List<Video> resultList = new ArrayList<>(resultSet);
                sortList(resultList);

                customRecycler_VideoList.setObjectList(resultList).reload();
                return true;
            }
        });
    }

    private void sortList(List<Video> videoList) {
        videoList.sort((video1, video2) -> video1.getTitel().compareTo(video2.getTitel()));
    }

    private void loadVideoRecycler() {
        List<Video> videoList = new ArrayList<>(database.videoMap.values());
        sortList(videoList);
        customRecycler_VideoList = CustomRecycler.Builder(this, findViewById(R.id.videos_recycler))
                .setItemView(R.layout.list_item_video)
                .setObjectList(videoList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.listItem_video_deleteCheck);
                    viewIdList.add(R.id.listItem_video_Titel);
                    viewIdList.add(R.id.listItem_video_Views);
                    viewIdList.add(R.id.listItem_video_Darsteller);
                    viewIdList.add(R.id.listItem_video_Studio);
                    viewIdList.add(R.id.listItem_video_Genre);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    viewIdMap.get(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE :View.GONE);

                    Video video = (Video) object;
                    ((TextView) viewIdMap.get(R.id.listItem_video_Titel)).setText(video.getTitel());
                    ((TextView) viewIdMap.get(R.id.listItem_video_Views)).setText(String.valueOf(video.getDateList().size()));

                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) viewIdMap.get(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    viewIdMap.get(R.id.listItem_video_Darsteller).setSelected(scrolling);

                    List<String> studioNames = new ArrayList<>();
                    video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
                    ((TextView) viewIdMap.get(R.id.listItem_video_Studio)).setText(String.join(", ", studioNames));
                    viewIdMap.get(R.id.listItem_video_Studio).setSelected(scrolling);

                    List<String> genreNames = new ArrayList<>();
                    video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
                    ((TextView) viewIdMap.get(R.id.listItem_video_Genre)).setText(String.join(", ", genreNames));
                    viewIdMap.get(R.id.listItem_video_Genre).setSelected(scrolling);
                })
                .setUseCustomRipple(true)
                .setOnClickListener((recycler, view, object, index) -> {
                    if (!delete) {
                        openUrl(object);
                    } else {
                        CheckBox checkBox = view.findViewById(R.id.listItem_video_deleteCheck);
                        checkBox.setChecked(!checkBox.isChecked());
                        if (checkBox.isChecked())
                            toDelete.add(((Video) object).getUuid());
                        else
                            toDelete.remove(((Video) object).getUuid());
                        String test = null;
                    }
                })
                .addSubOnClickListener(R.id.listItem_video_edit, (recycler, view, object, index) -> {
                    editDialogVideoPair = new Pair<>(showEditOrNewDialog(object), (Video) object);
                }, false)
                .setOnLongClickListener((recycler, view, object, index) -> {
                    dialogVideoPair = new Pair<>(showDetailDialog(object), (Video) object);
                })
                .setShowDivider(false)
                .generateCustomRecycler();
    }

    private void reLoadVideoRecycler() {
        List<Video> videoList = new ArrayList<>(database.videoMap.values());
        videoList.sort((video1, video2) -> video1.getTitel().compareTo(video2.getTitel()));

        customRecycler_VideoList.setObjectList(videoList).reload();
    }

    private Dialog showDetailDialog(Object object) {
        Video video = (Video) object;
        List<String> darstellerNames = new ArrayList<>();
        video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
        List<String> studioNames = new ArrayList<>();
        video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
        List<String> genreNames = new ArrayList<>();
        video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
        return CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Bearbeiten", dialog ->
                        editDialogVideoPair = new Pair<>(showEditOrNewDialog(object), (Video) object), false)
                .addButton("Öffnen", dialog -> openUrl(object), false)
                .setSetViewContent(view -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(video.getTitel());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames));
                    view.findViewById(R.id.dialog_video_Studio).setSelected(true);
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames));
                    view.findViewById(R.id.dialog_video_Genre).setSelected(true);
                    view.findViewById(R.id.dialog_video_details).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.dialog_video_Url)).setText(video.getUrl());
                    ((TextView) view.findViewById(R.id.dialog_video_views)).setText(String.valueOf(video.getDateList().size()));
                    ((RatingBar) view.findViewById(R.id.dialog_video_rating)).setRating(video.getRating());
                })
                .show();
    }

    private Dialog showEditOrNewDialog(Object object) {
        Video video = (Video) object;
        List<String> darstellerNames = new ArrayList<>();
        List<String> studioNames = new ArrayList<>();
        List<String> genreNames = new ArrayList<>();
        if (video != null) {
            video = ((Video) object).cloneVideo();
            video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
            video.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
            video.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));
        }
        else
            video = new Video();
        Video finalVideo = video;
        return CustomDialog.Builder(this)
                .setTitle(object == null ? "Neues Video" : "Video Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_video)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
//                .addButton(CustomDialog.CANCEL_BUTTON, dialog -> {}, false)
                .addButton(CustomDialog.SAVE_BUTTON, dialog -> {
                    String titel = ((EditText) dialog.findViewById(R.id.dialog_editOrAddVideo_Titel)).getText().toString();
                    if (titel.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Video videoNeu;
                    if (object == null)
                        videoNeu = new Video(titel);
                    else
                        videoNeu = database.videoMap.get(((Video) object).getUuid());

                    videoNeu.setTitel(titel);
                    videoNeu.setDarstellerList(finalVideo.getDarstellerList());
                    videoNeu.setStudioList(finalVideo.getStudioList());
                    videoNeu.setGenreList(finalVideo.getGenreList());
                    videoNeu.setUrl(((EditText) dialog.findViewById(R.id.dialog_editOrAddVideo_Url)).getText().toString());
                    videoNeu.setRating(((RatingBar) dialog.findViewById(R.id.dialog_editOrAddVideo_rating)).getRating());

                    database.videoMap.put(videoNeu.getUuid(), videoNeu);
                    reLoadVideoRecycler();
                    dialog.dismiss();
                    setResult(RESULT_OK);
                    if (dialogVideoPair == null)
                        return;

                    dialogVideoPair.first.dismiss();
                    dialogVideoPair = new Pair<>(showDetailDialog(object), dialogVideoPair.second);
                }, false)
                .setSetViewContent(view -> {
                    if (finalVideo != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_Titel)).setText(finalVideo.getTitel());
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(String.join(", ", darstellerNames));
                        view.findViewById(R.id.dialog_editOrAddVideo_Darsteller).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(String.join(", ", studioNames));
                        view.findViewById(R.id.dialog_editOrAddVideo_Studio).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(String.join(", ", genreNames));
                        view.findViewById(R.id.dialog_editOrAddVideo_Genre).setSelected(true);
                        ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_Url)).setText(finalVideo.getUrl());
                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddVideo_rating)).setRating(finalVideo.getRating());
                    }

                    view.findViewById(R.id.dialog_editOrAddVideo_editActor).setOnClickListener(view1 ->
                            showEditActorOrGenreDialog(finalVideo == null ? null : finalVideo.getDarstellerList(), finalVideo, EDIT_TYPE.DARSTELLER));
                    view.findViewById(R.id.dialog_editOrAddVideo_editStudio).setOnClickListener(view1 ->
                            showEditActorOrGenreDialog(finalVideo == null ? null : finalVideo.getStudioList(), finalVideo, EDIT_TYPE.STUDIO ));
                    view.findViewById(R.id.dialog_editOrAddVideo_editGenre).setOnClickListener(view1 ->
                            showEditActorOrGenreDialog(finalVideo == null ? null : finalVideo.getGenreList(), finalVideo, EDIT_TYPE.GENRE));
                })
                .show();
    }


    private Dialog showEditActorOrGenreDialog(List<String> preSelectedUuidList, Video video, EDIT_TYPE editType) {
        if (preSelectedUuidList == null)
            preSelectedUuidList = new ArrayList<>();

        List<String> selectedUuidList = new  ArrayList<>(preSelectedUuidList);
        List<String> filterdUuidList;
        String editType_string = "";
        switch (editType){
            default:
            case DARSTELLER:
                filterdUuidList = new ArrayList<>(database.darstellerMap.keySet());
                editType_string = "Darsteller";
                break;
            case STUDIO:
                filterdUuidList = new ArrayList<>(database.studioMap.keySet());
                editType_string = "Studio";
                break;
            case GENRE:
                filterdUuidList = new ArrayList<>(database.genreMap.keySet());
                editType_string = "Genre";
                break;
        }

        int saveButtonId = View.generateViewId();
        int saveButtonId_add = View.generateViewId();


        List<String> finalSelectedUuidList = preSelectedUuidList;
        String finalEditType_string = editType_string;
        String finalEditType_string1 = editType_string;
        Dialog dialog_AddActorOrGenre = CustomDialog.Builder(this)
                .setTitle(editType_string + " Bearbeiten")
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .setView(R.layout.dialog_edit_item)
                .setDimensions(true, true)
                .addButton("Hinzufügen", dialog -> {
                    addPasengerDialogVideoPair = new Pair<>(dialog, video);
                    CustomDialog.Builder(this)
                            .setTitle(finalEditType_string + " Hinzufügen")
                            .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                            .addButton(CustomDialog.OK_BUTTON, dialog1 -> {
                                switch (editType){
                                    case DARSTELLER:
                                        Darsteller darsteller = new Darsteller(CustomDialog.getEditText(dialog1));
                                        database.darstellerMap.put(darsteller.getUuid(), darsteller);
                                        break;
                                    case STUDIO:
                                        Studio studio = new Studio(CustomDialog.getEditText(dialog1));
                                        database.studioMap.put(studio.getUuid(), studio);
                                        break;
                                    case GENRE:
                                        Genre genre = new Genre(CustomDialog.getEditText(dialog1));
                                        database.genreMap.put(genre.getUuid(), genre);
                                        break;
                                }
                                addPasengerDialogVideoPair.first.dismiss();
                                addPasengerDialogVideoPair = new Pair<>(
                                        showEditActorOrGenreDialog(finalSelectedUuidList, video, editType), video);

                            }, saveButtonId_add)
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setFireButtonOnOK(true, saveButtonId_add)
                                    .setHint(finalEditType_string1 + "-Name")
                                    .setText(((SearchView) dialog.findViewById(R.id.dialogAddPassenger_search)).getQuery().toString()))
                            .show();

                }, false)
                .addButton("Abbrechen", dialog -> {})
                .addButton("Speichern", dialog -> {
                    List<String> nameList = new ArrayList<>();
                    switch (editType){
                        case DARSTELLER:
                            video.setDarstellerList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.darstellerMap.get(uuid).getName()));
                            ((TextView) editDialogVideoPair.first.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(String.join(", ", nameList));
                            break;
                        case STUDIO:
                            video.setStudioList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.studioMap.get(uuid).getName()));
                            ((TextView) editDialogVideoPair.first.findViewById(R.id.dialog_editOrAddVideo_Studio)).setText(String.join(", ", nameList));
                            break;
                        case GENRE:
                            video.setGenreList(selectedUuidList);
                            selectedUuidList.forEach(uuid -> nameList.add(database.genreMap.get(uuid).getName()));
                            ((TextView) editDialogVideoPair.first.findViewById(R.id.dialog_editOrAddVideo_Genre)).setText(String.join(", ", nameList));
                            break;
                    }
                }, saveButtonId)
                .show();

        Button saveButton = dialog_AddActorOrGenre.findViewById(saveButtonId);
        saveButton.setEnabled(false);

        CustomRecycler customRecycler_selectList = CustomRecycler.Builder(this, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectPassengers));

        CustomRecycler customRecycler_selectedList = CustomRecycler.Builder(this, dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_selectedPassengers))
                .setItemView(R.layout.list_item_bubble)
                .setObjectList(selectedUuidList)
                .setShowDivider(false)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.list_bubble_name);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    switch (editType){
                        case DARSTELLER:
                            Darsteller darsteller = database.darstellerMap.get(object);
                            ((TextView) viewIdMap.get(R.id.list_bubble_name)).setText(darsteller.getName());
                            break;
                        case STUDIO:
                            Studio studio = database.studioMap.get(object);
                            ((TextView) viewIdMap.get(R.id.list_bubble_name)).setText(studio.getName());
                            break;
                        case GENRE:
                            Genre genre = database.genreMap.get(object);
                            ((TextView) viewIdMap.get(R.id.list_bubble_name)).setText(genre.getName());
                            break;
                    }

                    dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                })
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .setOnClickListener((recycler, view, object, index) -> {
                    Toast.makeText(this,
                            "Halten zum abwählen" , Toast.LENGTH_SHORT).show();
                })
                .setOnLongClickListener((recycler, view, object, index) -> {
                    ((CustomRecycler.MyAdapter) recycler.getAdapter()).removeItemAt(index);
                    selectedUuidList.remove(object);

                    if (selectedUuidList.size() <= 0) {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectList.setObjectList(filterdUuidList).reload();
                })
                .setUseCustomRipple(true)
                .generateCustomRecycler();

        List<Object> objectList;
        switch (editType){
            default:
            case DARSTELLER:
                objectList = new ArrayList(database.darstellerMap.keySet());
                break;
            case STUDIO:
                objectList = new ArrayList(database.studioMap.keySet());
                break;
            case GENRE:
                objectList = new ArrayList(database.genreMap.keySet());
                break;
        }

        customRecycler_selectList
                .setItemView(R.layout.list_item_select_actor)
                .setMultiClickEnabled(true)
                .setObjectList(objectList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.selectList_name);
                    viewIdList.add(R.id.selectList_selected);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    switch (editType){
                        case DARSTELLER:
                            Darsteller darsteller = database.darstellerMap.get(object);
                            ((TextView) viewIdMap.get(R.id.selectList_name)).setText(darsteller.getName());
                            break;
                        case STUDIO:
                            Studio studio = database.studioMap.get(object);
                            ((TextView) viewIdMap.get(R.id.selectList_name)).setText(studio.getName());
                            break;
                        case GENRE:
                            Genre genre = database.genreMap.get(object);
                            ((TextView) viewIdMap.get(R.id.selectList_name)).setText(genre.getName());
                            break;
                    }

                    if (selectedUuidList.contains(object))
                        ((CheckBox) viewIdMap.get(R.id.selectList_selected)).setChecked(true);
                })
                .setOnClickListener((recycler, view, object, index) -> {
                    CheckBox checkBox = view.findViewById(R.id.selectList_selected);
                    checkBox.setChecked(!checkBox.isChecked());
                    if (selectedUuidList.contains(object))
                        selectedUuidList.remove(object);
                    else
                        selectedUuidList.add((String) object);

                    if (selectedUuidList.size() <= 0) {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                    } else {
                        dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddActorOrGenre.findViewById(saveButtonId).setEnabled(true);

                    customRecycler_selectedList.setObjectList(selectedUuidList).reload();
                })
                .generateCustomRecycler();

        SearchView searchView = dialog_AddActorOrGenre.findViewById(R.id.dialogAddPassenger_search);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                s = s.trim();
                if (s.equals("")) {
                    switch (editType){
                        case DARSTELLER:
                            customRecycler_selectList.setObjectList(Collections.singletonList(database.darstellerMap.keySet())).reload();
                            break;
                        case STUDIO:
                            customRecycler_selectList.setObjectList(Collections.singletonList(database.studioMap.keySet())).reload();
                            break;
                        case GENRE:
                            customRecycler_selectList.setObjectList(Collections.singletonList(database.genreMap.keySet())).reload();
                            break;
                    }
                }
                switch (editType){
                    case DARSTELLER:
                        Map<String, Darsteller> darstellerMap = database.darstellerMap;
                        filterdUuidList.clear();
                        for (String uuidActor : database.darstellerMap.keySet()) {
                            if (darstellerMap.get(uuidActor).getName().toLowerCase().contains(s.toLowerCase()))
                                filterdUuidList.add(uuidActor);
                        }
                        break;
                    case STUDIO:
                        Map<String, Studio> studioMap = database.studioMap;
                        filterdUuidList.clear();
                        for (String uuidActor : database.studioMap.keySet()) {
                            if (studioMap.get(uuidActor).getName().toLowerCase().contains(s.toLowerCase()))
                                filterdUuidList.add(uuidActor);
                        }
                        break;
                    case GENRE:
                        Map<String, Genre> genreMap = database.genreMap;
                        filterdUuidList.clear();
                        for (String uuidActor : database.genreMap.keySet()) {
                            if (genreMap.get(uuidActor).getName().toLowerCase().contains(s.toLowerCase()))
                                filterdUuidList.add(uuidActor);
                        }
                        break;
                }

                customRecycler_selectList.setObjectList(filterdUuidList).reload();

                return true;
            }
        });

        return dialog_AddActorOrGenre;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_bar, menu);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_add:
                editDialogVideoPair = new Pair<>(showEditOrNewDialog(null), null);
                break;
            case R.id.taskBar_random:
                showRandomDialog();
                break;
            case R.id.taskBar_scroll:
                if (item.isChecked()) {
                    item.setChecked(false);
                    scrolling = false;
                } else {
                    item.setChecked(true);
                    scrolling = true;
                }
                customRecycler_VideoList.reload();
                break;
            case R.id.taskBar_delete:
                if (database.videoMap.isEmpty()) {
                    Toast.makeText(this, "Keine Videos", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (delete) {
                    videos_confirmDelete.setVisibility(View.GONE);
                } else {
                    videos_confirmDelete.setVisibility(View.VISIBLE);
                }
                customRecycler_VideoList.reload();
                delete = !delete;
                break;

        }
        return true;


//        return super.onOptionsItemSelected(item);
    }

    private void showRandomDialog() {
        if (database.videoMap.isEmpty()) {
            Toast.makeText(this, "Keine Videos", Toast.LENGTH_SHORT).show();
            return;
        }
        randomVideo = (Video) database.videoMap.values().toArray()[(int) (Math.random() * database.videoMap.size())];
        List<String> darstellerNames = new ArrayList<>();
        randomVideo.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
        List<String> studioNames = new ArrayList<>();
        randomVideo.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
        List<String> genreNames = new ArrayList<>();
        randomVideo.getGenreList().forEach(uuid -> genreNames.add(database.genreMap.get(uuid).getName()));

        CustomDialog.Builder(this)
                .setTitle("Zufälliges Video")
                .setView(R.layout.dialog_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Nochmal", dialog -> {
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomVideo = (Video) database.videoMap.values().toArray()[(int) (Math.random() * database.videoMap.size())];
                    ((TextView) dialog.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getTitel());

                    List<String> darstellerNames_neu = new ArrayList<>();
                    randomVideo.getDarstellerList().forEach(uuid -> darstellerNames_neu.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames_neu));

                    List<String> studioNames_neu = new ArrayList<>();
                    randomVideo.getStudioList().forEach(uuid -> studioNames_neu.add(database.studioMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames_neu));

                    List<String> genreNames_neu = new ArrayList<>();
                    randomVideo.getGenreList().forEach(uuid -> genreNames_neu.add(database.genreMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames_neu));

                }, false)
                .addButton("Öffnen", dialog -> openUrl(randomVideo), false)
                .setSetViewContent(view -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getTitel());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames));
                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);

                })
                .show();

    }

    public void openUrl(Object object) {
        String url = ((Video) object).getUrl();
        if (url == null || url.equals("")) {
            Toast.makeText(this, "Keine URL hinterlegt", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!url.contains("http://") && !url.contains("https://"))
            url = "http://".concat(url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBackPressed() {
        if (delete) {
            videos_confirmDelete.setVisibility(View.GONE);
            reLoadVideoRecycler();
            delete = false;
            return;
        }

        super.onBackPressed();
    }

    @SuppressLint("RestrictedApi")
    public boolean onSupportNavigateUp() {
        if (delete) {
            videos_confirmDelete.setVisibility(View.GONE);
            reLoadVideoRecycler();
            delete = false;
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        Utility.saveDatabase(mySPR_daten);
        super.onDestroy();
    }

}
