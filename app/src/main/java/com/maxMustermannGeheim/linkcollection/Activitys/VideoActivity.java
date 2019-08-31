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
import com.maxMustermannGeheim.linkcollection.Daten.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VideoActivity extends AppCompatActivity {
    Database database;
    SharedPreferences mySPR_daten;
    private boolean delete = false;
    private List<UUID> toDelete = new ArrayList<>();
    Video randomVideo;


    private Pair<Dialog, Video> addPasengerDialogVideoPair;
    private Pair<Dialog, Video> editDialogVideoPair;
    private Pair<Dialog, Video> dialogVideoPair;

    private CustomRecycler customRecycler_VideoList;
    FloatingActionButton videos_confirmDelete;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        database = Database.getInstance();
        mySPR_daten = getSharedPreferences("LinkCollection_Daten", 0);

        videos_confirmDelete = findViewById(R.id.videos_confirmDelete);
        videos_confirmDelete.setOnClickListener(view -> {
            for (UUID uuidVideo : toDelete) {
                database.videoMap.remove(uuidVideo);
            }
            delete = false;
            videos_confirmDelete.setVisibility(View.GONE);

            reLoadVideoRecycler();
            setResult(RESULT_OK);
        });
        loadVideoRecycler();
    }

    private void loadVideoRecycler() {
        List<Video> videoList = new ArrayList<>(database.videoMap.values());
        videoList.sort((video1, video2) -> video1.getTitel().compareTo(video2.getTitel()));

        customRecycler_VideoList = CustomRecycler.Builder(this, findViewById(R.id.videos_recycler))
                .setItemView(R.layout.list_item_video)
                .setObjectList(videoList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.listItem_video_deleteCheck);
                    viewIdList.add(R.id.listItem_video_Titel);
                    viewIdList.add(R.id.listItem_video_Darsteller);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    viewIdMap.get(R.id.listItem_video_deleteCheck).setVisibility(delete ? View.VISIBLE :View.GONE);

                    Video video = (Video) object;
                    List<String> darstellerNames = new ArrayList<>();
                    video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));

                    ((TextView) viewIdMap.get(R.id.listItem_video_Titel)).setText(video.getTitel());
                    ((TextView) viewIdMap.get(R.id.listItem_video_Darsteller)).setText(String.join(", ", darstellerNames));
                    viewIdMap.get(R.id.listItem_video_Darsteller).setSelected(true);
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
        if (video != null) {
            video = ((Video) object).cloneVideo();
            video.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
        }
        else
            video = new Video();
        Video finalVideo = video;
        return CustomDialog.Builder(this)
                .setTitle(object == null ? "Neues Video" : "Video Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_video)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                .addButton(CustomDialog.CANCEL_BUTTON, dialog -> {}, false)
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
                        ((EditText) view.findViewById(R.id.dialog_editOrAddVideo_Url)).setText(finalVideo.getUrl());
                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddVideo_rating)).setRating(finalVideo.getRating());
                    }

                    view.findViewById(R.id.dialog_editOrAddVideo_editActor).setOnClickListener(view1 -> {
                        showEditActorDialog(finalVideo == null ? null : finalVideo.getDarstellerList(), finalVideo);
                    });
                })
                .show();
    }


    private Dialog showEditActorDialog(List<UUID> selectedUuidList, Video video) {
        if (selectedUuidList == null)
            selectedUuidList = new ArrayList<>();

        List<UUID> selectedActorList = new  ArrayList<>(selectedUuidList);
        List<UUID> filterdActorList = new ArrayList<>(database.darstellerMap.keySet());
        int saveButtonId = View.generateViewId();
        int saveButtonId_add = View.generateViewId();

        List<UUID> finalSelectedUuidList = selectedUuidList;
        Dialog dialog_AddPassenger = CustomDialog.Builder(this)
                .setTitle("Darsteller Bearbeiten")
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .setView(R.layout.dialog_edit_item)
                .setDimensions(true, true)
                .addButton("Hinzufügen", dialog -> {
                    addPasengerDialogVideoPair = new Pair<>(dialog, video);
                    CustomDialog.Builder(this)
                            .setTitle("Darsteller Hinzufügen")
                            .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                            .addButton(CustomDialog.OK_BUTTON, dialog1 -> {
                                Darsteller darsteller = new Darsteller(CustomDialog.getEditText(dialog1));
                                database.darstellerMap.put(darsteller.getUuid(), darsteller);

                                // ToDo: evl. vor neuStart speichern, welche Darsteller ausgewählt sind
                                addPasengerDialogVideoPair.first.dismiss();
                                addPasengerDialogVideoPair = new Pair<>(
                                        showEditActorDialog(finalSelectedUuidList, video), video);
                            }, saveButtonId_add)
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setFireButtonOnOK(true, saveButtonId_add))
                            .show();

                }, false)
                .addButton("Abbrechen", dialog -> {})
                .addButton("Speichern", dialog -> {
                    video.setDarstellerList(selectedActorList);
                    List<String> darstellerNames = new ArrayList<>();
                    selectedActorList.forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));

                    ((TextView) editDialogVideoPair.first.findViewById(R.id.dialog_editOrAddVideo_Darsteller)).setText(String.join(", ", darstellerNames));
                }, saveButtonId)
                .show();

        Button saveButton = dialog_AddPassenger.findViewById(saveButtonId);
        saveButton.setEnabled(false);

        CustomRecycler customRecycler_selectList = CustomRecycler.Builder(this, dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_selectPassengers));

        CustomRecycler customRecycler_selectedList = CustomRecycler.Builder(this, dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_selectedPassengers))
                .setItemView(R.layout.list_item_actor_bubble)
                .setObjectList(selectedActorList)
                .setShowDivider(false)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.actorList_bubble_name);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    Darsteller darsteller = database.darstellerMap.get(object);
                    ((TextView) viewIdMap.get(R.id.actorList_bubble_name)).setText(darsteller.getName());
                    dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                })
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .setOnClickListener((recycler, view, object, index) -> {
                    Toast.makeText(this,
                            "Halten zum abwählen" , Toast.LENGTH_SHORT).show();
                })
                .setOnLongClickListener((recycler, view, object, index) -> {
                    ((CustomRecycler.MyAdapter) recycler.getAdapter()).removeItemAt(index);
                    selectedActorList.remove(object);

                    if (selectedActorList.size() <= 0) {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
//                        dialog_AddPassenger.findViewById(saveButtonId).setEnabled(false);
                    } else {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                    }
                    dialog_AddPassenger.findViewById(saveButtonId).setEnabled(true);

                    // ToDo: wieder probleme weil entladen
                    customRecycler_selectList.setObjectList(filterdActorList).reload();
                })
                .setUseCustomRipple(true)
                .generateCustomRecycler();

        customRecycler_selectList
                .setItemView(R.layout.list_item_select_actor)
                .setMultiClickEnabled(true)
                .setObjectList(new ArrayList(database.darstellerMap.keySet()))
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.selectUserList_name);
                    viewIdList.add(R.id.selectUserList_selected);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    Darsteller darsteller = database.darstellerMap.get(object);

                    ((TextView) viewIdMap.get(R.id.selectUserList_name)).setText(darsteller.getName());
                    if (selectedActorList.contains(object))
                        ((CheckBox) viewIdMap.get(R.id.selectUserList_selected)).setChecked(true);
                })
                .setOnClickListener((recycler, view, object, index) -> {
                    CheckBox checkBox = view.findViewById(R.id.selectUserList_selected);
                    checkBox.setChecked(!checkBox.isChecked());
                    if (selectedActorList.contains(object))
                        selectedActorList.remove(object);
                    else
                        selectedActorList.add((UUID) object);

                    if (selectedActorList.size() <= 0) {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                        dialog_AddPassenger.findViewById(saveButtonId).setEnabled(false);
                    } else {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                        dialog_AddPassenger.findViewById(saveButtonId).setEnabled(true);
                    }

                    customRecycler_selectedList.setObjectList(selectedActorList).reload();
                })
                .generateCustomRecycler();

        SearchView searchView = dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_search);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                s = s.trim();
                if (s.equals(""))
                    customRecycler_selectList.setObjectList(Collections.singletonList(database.darstellerMap.keySet())).reload();

                Map<UUID, Darsteller> darstellerMap = database.darstellerMap;
                filterdActorList.clear();
                for (UUID uuidActor : database.darstellerMap.keySet()) {
                    if (darstellerMap.get(uuidActor).getName().toLowerCase().contains(s.toLowerCase()))
                        filterdActorList.add(uuidActor);
                }

                customRecycler_selectList.setObjectList(filterdActorList).reload();

                return true;
            }
        });

        return dialog_AddPassenger;

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
            case R.id.taskBar_delete:
                if (database.videoMap.isEmpty()) {
                    Toast.makeText(this, "Keine Videos", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (delete) {
                    videos_confirmDelete.setVisibility(View.GONE);
                } else {
//                    toDelete = Arrays.asList(new Boolean[database.videoMap.size()]);
//                    Collections.fill(toDelete, Boolean.FALSE);
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

        CustomDialog.Builder(this)
                .setTitle("Zufälliges Video")
                .setView(R.layout.dialog_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Nochmal", dialog -> {
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomVideo = (Video) database.videoMap.values().toArray()[(int) (Math.random() * database.videoMap.size())];
                    List<String> darstellerNames_neu = new ArrayList<>();
                    randomVideo.getDarstellerList().forEach(uuid -> darstellerNames_neu.add(database.darstellerMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getTitel());
                    ((TextView) dialog.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames_neu));

                }, false)
                .addButton("Öffnen", dialog -> openUrl(randomVideo), false)
                .setSetViewContent(view -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(randomVideo.getTitel());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames));
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
