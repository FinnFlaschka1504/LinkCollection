package com.maxMustermannGeheim.linkcollection.Activitys.Videos;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import com.maxMustermannGeheim.linkcollection.Activitys.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activitys.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class CatigorysActivity extends AppCompatActivity {
    public static final int START_CATIGORY_SEARCH = 001;

    enum SORT_TYPE {
        NAME, COUNT
    }

    public enum CATEGORIES {
        VIDEO("Video", "Videos"), DARSTELLER("Darsteller", "Darsteller"), STUDIOS("Studio", "Studios"), GENRE("Genre", "Genres"), WATCH_LATER("WATCH_LATER", "WATCH_LATER");

        private String singular;
        private String plural;
        private String id = UUID.randomUUID().toString();

        CATEGORIES(String singular, String plural) {
            this.singular = singular;
            this.plural = plural;
        }

        public String getSingular() {
            return singular;
        }

        public String getPlural() {
            return plural;
        }

        public String getId() {
            return id;
        }
    }


    private int columnCount = 2;
    private CATEGORIES catigory;
    private SORT_TYPE sort_type = SORT_TYPE.NAME;
    private Database database = Database.getInstance();
    SharedPreferences mySPR_daten;
    private String searchQuerry = "";

    private CustomRecycler customRecycler;
    private SearchView catigorys_search;
    private SearchView.OnQueryTextListener textListener;

    private List<Pair<ParentClass, Integer>> allDatenObjektPairList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catigorys);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);


        catigory = CATEGORIES.valueOf(getIntent().getStringExtra(MainActivity.EXTRA_CATEGORY));
        setTitle(catigory.getPlural());
        setDatenObjektIntegerPairLiist();

        sortList(allDatenObjektPairList);
//        filterdDatenObjektPairList = new ArrayList<>(allDatenObjektPairList);
        loadRecycler();

        catigorys_search = findViewById(R.id.catigorys_search);
        textListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                this.onQueryTextChange(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
//                filterdDatenObjektPairList = new ArrayList<>(allDatenObjektPairList);
                searchQuerry = s.trim();

                reLoadRecycler();
                return true;
            }
        };
        catigorys_search.setOnQueryTextListener(textListener);
        catigorys_search.setQueryHint(catigory.getPlural() + " filtern");
    }

    private List<Pair<ParentClass,Integer>> filterList(List<Pair<ParentClass,Integer>> datenObjektPairList) {
        if (!searchQuerry.equals("")) {
//            for (Pair<ParentClass, Integer> datenObjektIntegerPair : allDatenObjektPairList) {
//                ParentClass parentClass = datenObjektIntegerPair.first;
//                if (!parentClass.getName().toLowerCase().contains(searchQuerry.toLowerCase()))
//                    filterdDatenObjektPairList.remove(datenObjektIntegerPair);
//            }
            return datenObjektPairList.stream().filter(datenObjektIntegerPair -> datenObjektIntegerPair.first.getName().toLowerCase().contains(searchQuerry.toLowerCase())).collect(Collectors.toList());
        } else
            return new ArrayList<>(datenObjektPairList);
    }

    private List<Pair<ParentClass,Integer>> sortList(List<Pair<ParentClass,Integer>> datenObjektPairList) {
        switch (sort_type) {
            case NAME:
                datenObjektPairList.sort((objekt1, objekt2) -> objekt1.first.getName().compareTo(objekt2.first.getName()));
                break;
            case COUNT:
                datenObjektPairList.sort((objekt1, objekt2) -> objekt1.second.compareTo(objekt2.second));
                Collections.reverse(datenObjektPairList);
                break;
        }
        return datenObjektPairList;
    }

    private void setDatenObjektIntegerPairLiist() {
        List<Pair<ParentClass, Integer>> pairList = new ArrayList<>();

        switch (catigory) {
            case DARSTELLER:

                for (ParentClass parentClass : database.darstellerMap.values()) {
                    int count = 0;
                    for (Video video : database.videoMap.values()) {
                        if (video.getDarstellerList().contains(parentClass.getUuid()))
                            count++;
                    }
                    pairList.add(new Pair<>(parentClass, count));
                }

                allDatenObjektPairList = pairList;
                break;
            case GENRE:

                for (ParentClass parentClass : database.genreMap.values()) {
                    int count = 0;
                    for (Video video : database.videoMap.values()) {
                        if (video.getGenreList().contains(parentClass.getUuid()))
                            count++;
                    }
                    pairList.add(new Pair<>(parentClass, count));
                }

                allDatenObjektPairList = pairList;
                break;
            case STUDIOS:

                for (ParentClass parentClass : database.studioMap.values()) {
                    int count = 0;
                    for (Video video : database.videoMap.values()) {
                        if (video.getStudioList().contains(parentClass.getUuid()))
                            count++;
                    }
                    pairList.add(new Pair<>(parentClass, count));
                }

                allDatenObjektPairList = pairList;
                break;
        }

    }

    private void loadRecycler() {
        customRecycler = CustomRecycler.Builder(this, findViewById(R.id.catigorys_recycler))
                .setItemLayout(R.layout.list_item_catigory_item)
                .setGetActiveObjectList(() -> sortList(filterList(allDatenObjektPairList)))
                .setSetItemContent((itemView, object) -> {
                    ParentClass parentClass = (ParentClass) ((Pair) object).first;

                    ((TextView) itemView.findViewById(R.id.listItem_catigoryItem_name)).setText(parentClass.getName());
                    ((TextView) itemView.findViewById(R.id.userlistItem_catigoryItem_count)).setText(String.valueOf(((Pair) object).second));
                })
                .setRowOrColumnCount(columnCount)
                .setShowDivider(false)
                .setOnClickListener((recycler, view, object, index) -> {
                    startActivityForResult(new Intent(this, VideoActivity.class)
                                    .putExtra(VideoActivity.EXTRA_SEARCH, ((ParentClass) ((Pair) object).first).getName())
                                    .putExtra(VideoActivity.EXTRA_SEARCH_CATEGORY, catigory.name()),
                            START_CATIGORY_SEARCH);
                })
                .setUseCustomRipple(true)
                .setOnLongClickListener((recycler, view, object, index) -> {
                    if (!Utility.isOnline(this))
                        return;
                    ParentClass parentClass = (ParentClass) ((Pair) object).first;
                    CustomDialog.Builder(this)
                            .setTitle(catigory.getSingular() + " Umbenennen, oder Löschen")
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setText(parentClass.getName())
                                    .setHint("Name"))
                            .setButtonType(CustomDialog.ButtonType.CUSTOM)
                            .addButton("Löschen", (customDialog, dialog) -> {
                                CustomDialog.Builder(this)
                                        .setTitle("Löschen")
                                        .setText("Wirklich '" + ((ParentClass) ((Pair) object).first).getName() + "' löschen?")
                                        .setButtonType(CustomDialog.ButtonType.YES_NO)
                                        .addButton(CustomDialog.YES_BUTTON, (customDialog1, dialog1) -> {
                                            dialog.dismiss();
                                            removeCatigory((ParentClass) ((Pair) object).first);
                                        })
                                        .show();
                            }, false)
                            .addButton("Abbrechen", (customDialog, dialog) -> {
                            })
                            .addButton("OK", (customDialog, dialog) -> {
                                if (!Utility.isOnline(this))
                                    return;
                                ((ParentClass) ((Pair) object).first).setName(CustomDialog.getEditText(dialog));
                                reLoadRecycler();
                                Database.saveAll();
                            })
                            .show();
                })
                .generateCustomRecycler();

    }

    private void removeCatigory(ParentClass parentClass) {
        allDatenObjektPairList.remove(parentClass);
//        filterdDatenObjektPairList.remove(parentClass);
        switch (catigory) {
            case GENRE:
                database.genreMap.remove(parentClass.getUuid());
                for (Video video : database.videoMap.values()) {
                    if (video.getGenreList().contains(parentClass.getUuid()))
                        video.getGenreList().remove(parentClass.getUuid());
                }
        }
        setResult(RESULT_OK);
        reLoadRecycler();
    }

    private void reLoadRecycler() {
//        sortList(filterdDatenObjektPairList);
        customRecycler.reload();
    }

    private void showRandomDialog() {

        List<Pair<ParentClass, Integer>> filterdDatenObjektPairList = sortList(filterList(allDatenObjektPairList));
        final Pair<ParentClass, Integer>[] randomPair = new Pair[]{filterdDatenObjektPairList.get((int) (Math.random() * filterdDatenObjektPairList.size()))};
        CustomDialog.Builder(this)
                .setTitle("Zufall")
                .setText(randomPair[0].first.getName() + " (" + randomPair[0].second + ")")
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Nochmal", (customDialog, dialog) -> {
                            randomPair[0] = filterdDatenObjektPairList.get((int) (Math.random() * filterdDatenObjektPairList.size()));
                            CustomDialog.changeText(dialog, randomPair[0].first.getName() + " (" + randomPair[0].second + ")");
                        },
                        false)
                .addButton("Suchen", (customDialog, dialog) -> {
                    startActivityForResult(new Intent(this, VideoActivity.class)
                                    .putExtra(VideoActivity.EXTRA_SEARCH, randomPair[0].first.getName())
                                    .putExtra(VideoActivity.EXTRA_SEARCH_CATEGORY, catigory.name()),
                            START_CATIGORY_SEARCH);
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_catigory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_catigory__random:
                showRandomDialog();
                break;

            case R.id.taskBar_catigory__sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_catigory__sortByViews:
                sort_type = SORT_TYPE.COUNT;
                item.setChecked(true);
                reLoadRecycler();
                break;

            case R.id.taskBar_catigory__showAs:
                if (columnCount == 2) {
                    columnCount = 1;
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_show_as_grid));
                } else {
                    columnCount = 2;
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_show_as_list));
                }
                customRecycler.setRowOrColumnCount(columnCount).reloadNew();
                break;

            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK /*&& requestCode == START_VIDEOS*/) {
            setDatenObjektIntegerPairLiist();
            textListener.onQueryTextChange(catigorys_search.getQuery().toString());
            reLoadRecycler();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
