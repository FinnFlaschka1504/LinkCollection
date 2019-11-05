package com.maxMustermannGeheim.linkcollection.Activitys.Knowledge;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import com.maxMustermannGeheim.linkcollection.Activitys.Main.CatigorysActivity;
import com.maxMustermannGeheim.linkcollection.Activitys.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activitys.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class KnowledgeActivity extends AppCompatActivity {
    private static final String ACTION_ADD_KNOWLEDGE = "ACTION_ADD_KNOWLEDGE";
    private Dialog detailDialog;


    enum SORT_TYPE{
        NAME, RATING, LATEST
    }

    public enum FILTER_TYPE{
        NAME, CATEGORY, CONTENT
    }


    private boolean reverse;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    Database database = Database.getInstance();
    private CustomRecycler customRecycler_List;
    private Dialog[] addOrEditDialog = new Dialog[]{null};
    private String searchQuery = "";
    private SharedPreferences mySPR_daten;
    private SearchView.OnQueryTextListener textListener;
    private SearchView videos_search;
    private ArrayList<Knowledge> allKnowledgeList;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.CATEGORY, FILTER_TYPE.CONTENT));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

        CatigorysActivity.CATEGORIES extraSearchCategory = (CatigorysActivity.CATEGORIES) getIntent().getSerializableExtra(CatigorysActivity.EXTRA_SEARCH_CATEGORY);
        if (extraSearchCategory != null) {
            filterTypeSet.clear();

            switch (extraSearchCategory) {
                case KNOWLEDGE_CATEGORIES:
                    filterTypeSet.add(FILTER_TYPE.CATEGORY);
                    break;
            }

            String extraSearch = getIntent().getStringExtra(CatigorysActivity.EXTRA_SEARCH);
            if (extraSearch != null) {
                videos_search.setQuery(extraSearch, true);
            }
        }

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
//                Toast.makeText(this, toDelete.size() + (toDelete.size() == 1 ? " VIDEO" : " Videos") + " gelöscht", Toast.LENGTH_SHORT).show();
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
                    searchQuery = s.trim().toLowerCase();
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
//                            List<VIDEO> subList = new ArrayList<>(filterdVideoList);
//                            for (VIDEO video : subList) {
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
                            knowledge.getCategoryIdList().stream().map(categoryId -> database.knowledgeCategoryMap.get(categoryId).getName()).collect(Collectors.toList())));
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
                .addSubOnClickListener(R.id.listItem_knowledge_details, (recycler, view, object, index) -> detailDialog = showDetailDialog((Knowledge) object), false)
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
        switch (sort_type) {
            case NAME:
                knowledgeList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
                break;
            case RATING:
                knowledgeList.sort((o1, o2) -> o1.getRating().compareTo(o2.getRating()) * -1);
                break;
            case LATEST:
                knowledgeList.sort((o1, o2) -> o1.getLastChanged().compareTo(o2.getLastChanged()) * -1);
                break;
        }
        if (reverse)
            Collections.reverse(knowledgeList);
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
            newKnowledge[0].getCategoryIdList().forEach(uuid -> categoriesNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
            newKnowledge[0].getSources().forEach(nameUrlPair  -> sourcesNames.add(nameUrlPair.first));
        }
        Dialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(knowledge == null ? "Neues Wissen" : "Wissen Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_knowledge)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                .addButton(CustomDialog.SAVE_BUTTON, (customDialog, dialog) -> {
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
                                .addButton(CustomDialog.YES_BUTTON, (customDialog1, dialog1) ->
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
                            Utility.showEditCatigoryDialog(this, addOrEditDialog, newKnowledge[0].getCategoryIdList(), newKnowledge[0], CatigorysActivity.CATEGORIES.KNOWLEDGE_CATEGORIES));
//                    view.findViewById(R.id.dialog_editOrAddKnowledge_editStudio).setOnClickListener(view1 ->
//                            Utility.showEditCatigoryDialog(this, addOrEditDialog, newKnowledge[0].getStudioList(), newKnowledge[0], ParentClass.OBJECT_TYPE.STUDIO ));
                })
                .show();
        return returnDialog;
    }

    private Dialog showDetailDialog(Knowledge knowledge) {
        setResult(RESULT_OK);
        List<String> categoriesNames = new ArrayList<>();
        knowledge.getCategoryIdList().forEach(uuid -> categoriesNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
        Dialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_detail_knowledge)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Bearbeiten", (customDialog, dialog) ->
                        addOrEditDialog[0] = showEditOrNewDialog(knowledge), false)
//                .addButton("Öffnen mit...", dialog -> openUrl(knowledge, true), false)
                .setSetViewContent(view -> {
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_title)).setText(knowledge.getName());
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_content)).setText(knowledge.getContent());
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_categories)).setText(String.join(", ", categoriesNames));
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_lastChanged)).setText(String.format("%s Uhr", new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(knowledge.getLastChanged())));
                    view.findViewById(R.id.dialog_detailKnowledge_details).setVisibility(View.VISIBLE);
//                    ((TextView) view.findViewById(R.id.dialog_video_Url)).setText(knowledge.getUrl());
//                    ((TextView) view.findViewById(R.id.dialog_video_views)).setText(String.valueOf(knowledge.getDateList().size()));
                    ((RatingBar) view.findViewById(R.id.dialog_detailKnowledge_rating)).setRating(knowledge.getRating());

//                    final boolean[] isInWatchLater = {database.watchLaterList.contains(knowledge.getUuid())};
//                    view.findViewById(R.id.dialog_video_watchLater_background).setPressed(isInWatchLater[0]);
//                    view.findViewById(R.id.dialog_video_watchLater).setOnClickListener(view1 -> {
//                        isInWatchLater[0] = !isInWatchLater[0];
//                        view.findViewById(R.id.dialog_video_watchLater_background).setPressed(isInWatchLater[0]);
//                        if (isInWatchLater[0]) {
//                            database.watchLaterList.add(knowledge.getUuid());
//                            Toast.makeText(this, "Zu 'Später-Ansehen' hinzugefügt", Toast.LENGTH_SHORT).show();
//                        } else {
//                            database.watchLaterList.remove(knowledge.getUuid());
//                            Toast.makeText(this, "Aus 'Später-Ansehen' entfernt", Toast.LENGTH_SHORT).show();
//                        }
//                        textListener.onQueryTextSubmit(videos_search.getQuery().toString());
//                        setResult(RESULT_OK);
//                        Database.saveAll();
//                    });

                })
                .show();
        returnDialog.setOnDismissListener(dialogInterface -> detailDialog = null);
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
        knowledge.setLastChanged(new Date());

        database.knowledgeMap.put(knowledge.getUuid(), knowledge);
        reLoadRecycler();
        dialog.dismiss();

        Database.saveAll();

        Utility.showCenterdToast(this, "Wissen gespeichert");

        if (detailDialog == null)
            return;

        detailDialog.dismiss();
        detailDialog = showDetailDialog(knowledge);

    }

    private void showRandomDialog() {
        List<Knowledge> filterdKnowledgeList = filterList(allKnowledgeList);
        if (filterdKnowledgeList.isEmpty()) {
            Toast.makeText(this, "Nichts zum Zeigen", Toast.LENGTH_SHORT).show();
            return;
        }
        final Knowledge[] randomKnowledge = {filterdKnowledgeList.get((int) (Math.random() * filterdKnowledgeList.size()))};
        List<String> categoryNames = new ArrayList<>();
        randomKnowledge[0].getCategoryIdList().forEach(uuid -> categoryNames.add(database.knowledgeCategoryMap.get(uuid).getName()));

        CustomDialog.Builder(this)
                .setTitle("Zufälliges Wissen")
                .setView(R.layout.dialog_detail_knowledge)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Nochmal", (customDialog, dialog) -> {
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomKnowledge[0] = filterdKnowledgeList.get((int) (Math.random() * filterdKnowledgeList.size()));
                    ((TextView) dialog.findViewById(R.id.dialog_detailKnowledge_title)).setText(randomKnowledge[0].getName());
                    ((TextView) dialog.findViewById(R.id.dialog_detailKnowledge_content)).setText(randomKnowledge[0].getContent());

                    List<String> darstellerNames_neu = new ArrayList<>();
                    randomKnowledge[0].getCategoryIdList().forEach(uuid -> darstellerNames_neu.add(database.knowledgeCategoryMap.get(uuid).getName()));
                    ((TextView) dialog.findViewById(R.id.dialog_detailKnowledge_categories)).setText(String.join(", ", darstellerNames_neu));


                }, false)
//                .addButton("Öffnen", (customDialog, dialog) -> openUrl(randomKnowledge[0], false), false)
                .setSetViewContent(view -> {
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_title)).setText(randomKnowledge[0].getName());
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_content)).setText(randomKnowledge[0].getContent());
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_categories)).setText(String.join(", ", categoryNames));

                })
                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_bar_knowledge, menu);

        Menu subMenu = menu.findItem(R.id.taskBar_filter).getSubMenu();
        subMenu.findItem(R.id.taskBar_knowledge_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        subMenu.findItem(R.id.taskBar_knowledge_filterByCategory)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.CATEGORY));
        subMenu.findItem(R.id.taskBar_knowledge_filterByContent)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.CONTENT));

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
            case R.id.taskBar_knowledge_sortByLatest:
                sort_type = SORT_TYPE.LATEST;
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
                reLoadRecycler();
//                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;
            case R.id.taskBar_knowledge_filterByCategory:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.CATEGORY);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.CATEGORY);
                    item.setChecked(true);
                }
                reLoadRecycler();
//                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;
            case R.id.taskBar_knowledge_filterByContent:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.CONTENT);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.CONTENT);
                    item.setChecked(true);
                }
                reLoadRecycler();
                break;

            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }

}
