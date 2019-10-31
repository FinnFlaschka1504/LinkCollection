package com.maxMustermannGeheim.linkcollection.Activitys.Knowledge;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxMustermannGeheim.linkcollection.Activitys.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KnowledgeActivity extends AppCompatActivity {
    private static final String ACTION_ADD_KNOWLEDGE = "ACTION_ADD_KNOWLEDGE";
    private Pair<Dialog, Knowledge> dialogKnowledgePair;


    enum SORT_TYPE{
        NAME, RATING
    }

    public enum FILTER_TYPE{
        NAME, CATEGORY
    }


    private boolean reverse;
    private SORT_TYPE sort_type;
    Database database = Database.getInstance();
    private CustomRecycler customRecycler_List;
    private Dialog[] addOrEditDialog = new Dialog[]{null};
    private String searchQuery = "";
    private SharedPreferences mySPR_daten;
    private SearchView.OnQueryTextListener textListener;
    private SearchView videos_search;
    private ArrayList<Knowledge> allKnowledgeList;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.CATEGORY));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
            setContentView(R.layout.activity_knowledge);
//            allVideoList = new ArrayList<>(database.videoMap.values());
//            sortList(allVideoList);
//            filterdVideoList = new ArrayList<>(allVideoList);
//
//            videos_confirmDelete = findViewById(R.id.videos_confirmDelete);
//            videos_confirmDelete.setOnClickListener(view -> {
//                for (String uuidVideo : toDelete) {
//                    filterdVideoList.remove(database.videoMap.get(uuidVideo));
//                    allVideoList.remove(database.videoMap.get(uuidVideo));
//                    database.videoMap.remove(uuidVideo);
//                }
//                delete = false;
//                videos_confirmDelete.setVisibility(View.GONE);
//
//                reLoadRecycler();
//                setResult(RESULT_OK);
//
//                Toast.makeText(this, toDelete.size() + (toDelete.size() == 1 ? " Video" : " Videos") + " gelöscht", Toast.LENGTH_SHORT).show();
//            });
            loadRecycler();

            videos_search = findViewById(R.id.videos_search);
            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchQuery = s;
//                    filterdVideoList = new ArrayList<>(allVideoList);
//
//                    if (!s.trim().equals("")) {
//                        if (s.trim().equals(WATCH_LATER_SEARCH)) {
//                            filterdVideoList = new ArrayList<>();
//                            for (String videoUuid : database.watchLaterList) {
//                                filterdVideoList.add(database.videoMap.get(videoUuid));
//                            }
//                            reLoadRecycler();
//                            return true;
//                        }
//
//                        for (String subQuery : s.split("\\|")) {
//                            subQuery = subQuery.trim();
//                            List<Video> subList = new ArrayList<>(filterdVideoList);
//                            for (Video video : subList) {
//                                if (!Utility.containedInVideo(subQuery, video, filterTypeSet))
//                                    filterdVideoList.remove(video);
//                            }
//                        }
//                    }
                    reLoadRecycler();
                    return true;
                }
            };
            videos_search.setOnQueryTextListener(textListener);

            if (Objects.equals(getIntent().getAction(), KnowledgeActivity.ACTION_ADD_KNOWLEDGE))
                showEditOrNewDialog(null);
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        }
        else
            whenLoaded.run();

    }

    private void loadRecycler() {
        customRecycler_List = CustomRecycler.Builder(this, findViewById(R.id.knowledge_recycler))
                .setItemLayout(R.layout.list_item_knowledge)
                .setGetActiveObjectList(() -> {
                    if (searchQuery.equals("")) {
                        allKnowledgeList = new ArrayList<>(database.knowledgeMap.values());
                        return sortList(allKnowledgeList);
                    }
                    else
                        return filterList(allKnowledgeList);
                })
                .setSetItemContent((CustomRecycler.SetItemContent<Knowledge>) (itemView, knowledge) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_knowledge_title)).setText(knowledge.getName());
                    ((TextView) itemView.findViewById(R.id.listItem_knowledge_content)).setText(knowledge.getContent());


                    ((TextView) itemView.findViewById(R.id.listItem_knowledge_categories)).setText(String.join(", ",
                            knowledge.getCategoryIdList().stream().map(categoryId -> database.categoryMap.get(categoryId).getName()).collect(Collectors.toList())));
                    itemView.findViewById(R.id.listItem_knowledge_categories).setSelected(true);

                    if (knowledge.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_knowledge_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_knowledge_rating)).setText(String.valueOf(knowledge.getRating()));
                    }
                    else
                        itemView.findViewById(R.id.listItem_knowledge_rating_layout).setVisibility(View.GONE);
                })
                .setUseCustomRipple(true)
                .setOnClickListener((recycler, view, object, index) -> {
                    TextView listItem_knowledge_content = view.findViewById(R.id.listItem_knowledge_content);
                    if (listItem_knowledge_content.isFocusable()) {
                        listItem_knowledge_content.setSingleLine(true);
                        listItem_knowledge_content.setFocusable(false);

                    } else {
                        listItem_knowledge_content.setSingleLine(false);
                        listItem_knowledge_content.setFocusable(true);
                    }
//                  openUrl(object, false);
                })
                .addSubOnClickListener(R.id.listItem_knowledge_details, (recycler, view, object, index) -> {
//                    dialogKnowledgePair = new Pair<>(showDetailDialog((Knowledge) object), (Knowledge) object);
                }, false)
                .setOnLongClickListener((recycler, view, object, index) -> {
                    addOrEditDialog[0] = showEditOrNewDialog((Knowledge) object);
                })
                .setShowDivider(false)
                .generateCustomRecycler();
    }

    private List<Knowledge> filterList(ArrayList<Knowledge> allKnowledgeList) {
        return sortList(allKnowledgeList.stream().filter(knowledge -> Utility.containedInKnowledge(searchQuery, knowledge, filterTypeSet)).collect(Collectors.toList()));
    }

    private List<Knowledge> sortList(List<Knowledge> knowledgeList) {
        return knowledgeList;
        // ToDo: implementieren
    }

    private void reLoadRecycler() {
        customRecycler_List.reload();
    }

    private Dialog showEditOrNewDialog(Knowledge knowledge) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);

        final Knowledge[] newKnowledge = {null};
        List<String> categoriesNames = new ArrayList<>();
        List<String> sourcesNames = new ArrayList<>();
        if (knowledge != null) {
            newKnowledge[0] = knowledge.cloneKnowledge();
            newKnowledge[0].getCategoryIdList().forEach(uuid -> categoriesNames.add(database.darstellerMap.get(uuid).getName()));
            newKnowledge[0].getSources().forEach(nameUrlPair  -> sourcesNames.add(nameUrlPair.first));
        }
        Dialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(knowledge == null ? "Neues Wissen" : "Wissen Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_knowledge)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                .addButton(CustomDialog.SAVE_BUTTON, dialog -> {
                    String titel = ((EditText) dialog.findViewById(R.id.dialog_editOrAddKnowledge_Titel)).getText().toString().trim();
                    if (titel.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String content = ((EditText) dialog.findViewById(R.id.dialog_editOrAddKnowledge_content)).getText().toString().trim();
//
                    if (content.equals("")){
                        CustomDialog.Builder(this)
                                .setTitle("Ohne Inhalt speichern?")
                                .setText("Möchtest du wirklich ohne einen Inhalt speichern")
                                .setButtonType(CustomDialog.ButtonType.YES_NO)
                                .addButton(CustomDialog.YES_BUTTON, dialog1 ->
                                        saveKnowledge(dialog, titel, content, newKnowledge, knowledge))
                                .show();
                    }
                    else
                        saveKnowledge(dialog, titel, content, newKnowledge, knowledge);

                }, false)
                .setSetViewContent(view -> {
                    if (newKnowledge[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAddKnowledge_Titel)).setText(newKnowledge[0].getName());
                        ((EditText) view.findViewById(R.id.dialog_editOrAddKnowledge_content)).setText(knowledge.getContent());
                        ((TextView) view.findViewById(R.id.dialog_editOrAddKnowledge_categories)).setText(String.join(", ", categoriesNames));
                        view.findViewById(R.id.dialog_editOrAddKnowledge_categories).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddKnowledge_sources)).setText(String.join(", ", sourcesNames));
                        view.findViewById(R.id.dialog_editOrAddKnowledge_sources).setSelected(true);
                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddKnowledge_rating)).setRating(newKnowledge[0].getRating());
                    }
                    else
                        newKnowledge[0] = new Knowledge("");


                    view.findViewById(R.id.dialog_editOrAddKnowledge_editCategories).setOnClickListener(view1 ->
                            Utility.showEditCatigoryDialog(this, addOrEditDialog, newKnowledge[0].getCategoryIdList(), newKnowledge[0], ParentClass.OBJECT_TYPE.KNOWLEDGE_CATEGORY));
//                    view.findViewById(R.id.dialog_editOrAddKnowledge_editStudio).setOnClickListener(view1 ->
//                            Utility.showEditCatigoryDialog(this, addOrEditDialog, newKnowledge[0].getStudioList(), newKnowledge[0], ParentClass.OBJECT_TYPE.STUDIO ));
                })
                .show();
        return returnDialog;
    }

    private Dialog showDetailDialog(Knowledge knowledge) {
        setResult(RESULT_OK);
        List<String> categoriesNames = new ArrayList<>();
        knowledge.getCategoryIdList().forEach(uuid -> categoriesNames.add(database.darstellerMap.get(uuid).getName()));
        Dialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_video)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Bearbeiten", dialog ->
                        addOrEditDialog[0] = showEditOrNewDialog(knowledge), false)
//                .addButton("Öffnen mit...", dialog -> openUrl(knowledge, true), false)
                .setSetViewContent(view -> {
                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(knowledge.getName());
                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", categoriesNames));
                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);
                    view.findViewById(R.id.dialog_video_details).setVisibility(View.VISIBLE);
//                    ((TextView) view.findViewById(R.id.dialog_video_Url)).setText(knowledge.getUrl());
//                    ((TextView) view.findViewById(R.id.dialog_video_views)).setText(String.valueOf(knowledge.getDateList().size()));
                    ((RatingBar) view.findViewById(R.id.dialog_video_rating)).setRating(knowledge.getRating());

                    final boolean[] isInWatchLater = {database.watchLaterList.contains(knowledge.getUuid())};
                    view.findViewById(R.id.dialog_video_watchLater_background).setPressed(isInWatchLater[0]);
                    view.findViewById(R.id.dialog_video_watchLater).setOnClickListener(view1 -> {
                        isInWatchLater[0] = !isInWatchLater[0];
                        view.findViewById(R.id.dialog_video_watchLater_background).setPressed(isInWatchLater[0]);
                        if (isInWatchLater[0]) {
                            database.watchLaterList.add(knowledge.getUuid());
                            Toast.makeText(this, "Zu 'Später-Ansehen' hinzugefügt", Toast.LENGTH_SHORT).show();
                        } else {
                            database.watchLaterList.remove(knowledge.getUuid());
                            Toast.makeText(this, "Aus 'Später-Ansehen' entfernt", Toast.LENGTH_SHORT).show();
                        }
                        textListener.onQueryTextSubmit(videos_search.getQuery().toString());
                        setResult(RESULT_OK);
                        Database.saveAll();
                    });

                })
                .show();
        returnDialog.setOnDismissListener(dialogInterface -> dialogKnowledgePair = null);
        return returnDialog;
    }

    private void saveKnowledge(Dialog dialog, String titel, String content, Knowledge[] newKnowledge, Knowledge knowledge) {

        if (knowledge == null)
            knowledge = newKnowledge[0];

        knowledge.setName(titel);
        knowledge.setContent(content);
        knowledge.setCategoryIdList(newKnowledge[0].getCategoryIdList());
        knowledge.setSources(newKnowledge[0].getSources());
        knowledge.setRating(((RatingBar) dialog.findViewById(R.id.dialog_editOrAddKnowledge_rating)).getRating());

        database.knowledgeMap.put(knowledge.getUuid(), knowledge);
        reLoadRecycler();
        dialog.dismiss();

        Database.saveAll();

        Utility.showCenterdToast(this, "Wissen gespeichert");

        if (dialogKnowledgePair == null)
            return;

        dialogKnowledgePair.first.dismiss();
        dialogKnowledgePair = new Pair<>(showDetailDialog(knowledge), dialogKnowledgePair.second);

    }

    private void showRandomDialog() {
        List<Knowledge> filterdKnowledgeList = filterList(allKnowledgeList);
        if (filterdKnowledgeList.isEmpty()) {
            Toast.makeText(this, "Keine Videos", Toast.LENGTH_SHORT).show();
            return;
        }
        final Knowledge[] randomKnowledge = {filterdKnowledgeList.get((int) (Math.random() * filterdKnowledgeList.size()))};
        List<String> categoryNames = new ArrayList<>();
        randomKnowledge[0].getCategoryIdList().forEach(uuid -> categoryNames.add(database.categoryMap.get(uuid).getName()));

//        CustomDialog.Builder(this)
//                .setTitle("Zufälliges Video")
//                .setView(R.layout.dialog_video)
//                .setButtonType(CustomDialog.ButtonType.CUSTOM)
//                .addButton("Nochmal", dialog -> {
//                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
//                    randomKnowledge[0] = filterdKnowledgeList.get((int) (Math.random() * filterdKnowledgeList.size()));
//                    ((TextView) dialog.findViewById(R.id.dialog_video_Titel)).setText(randomKnowledge[0].getName());
//
//                    List<String> darstellerNames_neu = new ArrayList<>();
//                    randomKnowledge[0].getDarstellerList().forEach(uuid -> darstellerNames_neu.add(database.darstellerMap.get(uuid).getName()));
//                    ((TextView) dialog.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", darstellerNames_neu));
//
//
//                }, false)
//                .addButton("Öffnen", dialog -> openUrl(randomKnowledge[0], false), false)
//                .setSetViewContent(view -> {
//                    ((TextView) view.findViewById(R.id.dialog_video_Titel)).setText(randomKnowledge[0].getName());
//                    ((TextView) view.findViewById(R.id.dialog_video_Darsteller)).setText(String.join(", ", categoryNames));
//                    ((TextView) view.findViewById(R.id.dialog_video_Studio)).setText(String.join(", ", studioNames));
//                    ((TextView) view.findViewById(R.id.dialog_video_Genre)).setText(String.join(", ", genreNames));
//                    view.findViewById(R.id.dialog_video_Darsteller).setSelected(true);
//
//                })
//                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_bar_knowledge, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_knowledge_add:
                addOrEditDialog[0] = showEditOrNewDialog(null);
                break;
            case R.id.taskBar_knowledge_random:
                showRandomDialog();
                break;

            case R.id.taskBar_knowledge_sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_knowledge_sortByRating:
                sort_type = SORT_TYPE.RATING;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_knowledge_sortReverse:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                reverse = checked;
                reLoadRecycler();
                break;

            case R.id.taskBar_knowledge_filterByName:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.NAME);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.NAME);
                    item.setChecked(true);
                }
                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;
            case R.id.taskBar_knowledge_filterByCategory:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.CATEGORY);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.CATEGORY);
                    item.setChecked(true);
                }
                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;

            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }

}
