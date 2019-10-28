package com.maxMustermannGeheim.linkcollection.Activitys;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

import com.maxMustermannGeheim.linkcollection.Daten.DatenObjekt;
import com.maxMustermannGeheim.linkcollection.Daten.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.maxMustermannGeheim.linkcollection.Activitys.MainActivity.SHARED_PREFERENCES_NAME;

public class CatigorysActivity extends AppCompatActivity {
    public static final int START_CATIGORY_SEARCH = 001;

    enum SORT_TYPE{
        NAME, COUNT
    }


    private int columnCount = 2;
    private String catigoryName;
    private MainActivity.CATIGORYS catigory;
    private SORT_TYPE sort_type = SORT_TYPE.NAME;
    private Database database = Database.getInstance();
    SharedPreferences mySPR_daten;

    private CustomRecycler customRecycler;
    private SearchView catigorys_search;
    private SearchView.OnQueryTextListener textListener;

    private List<Pair<DatenObjekt, Integer>> allDatenObjektPairList;
    private List<Pair<DatenObjekt, Integer>> filterdDatenObjektPairList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catigorys);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);

        setTitle(catigoryName);
        setDatenObjektIntegerPairLiist();

        switch (catigory) {
            case Genre:
                break;
            case Darsteller:
                break;
            case Studios:
                break;
        }
        sortList(allDatenObjektPairList);
        filterdDatenObjektPairList = new ArrayList<>(allDatenObjektPairList);
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
                filterdDatenObjektPairList = new ArrayList<>(allDatenObjektPairList);
                s = s.trim();
                if (!s.equals("")) {
                    for (Pair<DatenObjekt, Integer> datenObjektIntegerPair: allDatenObjektPairList) {
                        DatenObjekt datenObjekt = datenObjektIntegerPair.first;
                        if (!datenObjekt.getName().toLowerCase().contains(s.toLowerCase()))
                            filterdDatenObjektPairList.remove(datenObjektIntegerPair);
                    }
                }
                reLoadRecycler();
                return true;
            }
        };
        catigorys_search.setOnQueryTextListener(textListener);
        catigorys_search.setQueryHint(catigoryName + " filtern");
    }

    private void sortList(List<Pair<DatenObjekt, Integer>> datenObjektPairList) {
        switch (sort_type) {
            case NAME:
                datenObjektPairList.sort((objekt1, objekt2) -> objekt1.first.getName().compareTo(objekt2.first.getName()));
                break;
            case COUNT:
                datenObjektPairList.sort((objekt1, objekt2) -> objekt1.second.compareTo(objekt2.second));
                Collections.reverse(datenObjektPairList);
                break;
        }
    }

    private void setDatenObjektIntegerPairLiist() {
        List<Pair<DatenObjekt, Integer>> pairList = new ArrayList<>();

        catigoryName = getIntent().getStringExtra(MainActivity.EXTRA_CATIGORY);
        setTitle(catigoryName);
        if (catigoryName.equals(MainActivity.CATIGORYS.Darsteller.name())) {
            catigory = MainActivity.CATIGORYS.Darsteller;

            for (DatenObjekt datenObjekt : database.darstellerMap.values()) {
                int count = 0;
                for (Video video : database.videoMap.values()) {
                    if (video.getDarstellerList().contains(datenObjekt.getUuid()))
                        count++;
                }
                pairList.add(new Pair<>(datenObjekt, count));
            }

            allDatenObjektPairList = pairList;
        } else if (catigoryName.equals(MainActivity.CATIGORYS.Genre.name())) {
            catigory = MainActivity.CATIGORYS.Genre;

            for (DatenObjekt datenObjekt : database.genreMap.values()) {
                int count = 0;
                for (Video video : database.videoMap.values()) {
                    if (video.getGenreList().contains(datenObjekt.getUuid()))
                        count++;
                }
                pairList.add(new Pair<>(datenObjekt, count));
            }

            allDatenObjektPairList = pairList;
        } else if (catigoryName.equals(MainActivity.CATIGORYS.Studios.name())) {
            catigory = MainActivity.CATIGORYS.Studios;

            for (DatenObjekt datenObjekt : database.studioMap.values()) {
                int count = 0;
                for (Video video : database.videoMap.values()) {
                    if (video.getStudioList().contains(datenObjekt.getUuid()))
                        count++;
                }
                pairList.add(new Pair<>(datenObjekt, count));
            }

            allDatenObjektPairList = pairList;
        }

    }

    private void loadRecycler() {
        customRecycler = CustomRecycler.Builder(this, findViewById(R.id.catigorys_recycler))
                .setItemLayout(R.layout.list_item_catigory_item)
                .setObjectList(allDatenObjektPairList)
                .setSetItemContent((itemView, object) -> {
                    DatenObjekt datenObjekt = (DatenObjekt) ((Pair) object).first;

                    ((TextView) itemView.findViewById(R.id.listItem_catigoryItem_name)).setText(datenObjekt.getName());
                    ((TextView) itemView.findViewById(R.id.userlistItem_catigoryItem_count)).setText(String.valueOf(((Pair) object).second));
                })
                .setRowOrColumnCount(columnCount)
                .setShowDivider(false)
                .setOnClickListener((recycler, view, object, index) -> {
                    startActivityForResult(new  Intent(this, VideoActivity.class)
                        .putExtra(VideoActivity.EXTRA_SEARCH, ((DatenObjekt) ((Pair) object).first).getName())
                        .putExtra(VideoActivity.EXTRA_SEARCH_CATIGORY, catigoryName),
                            START_CATIGORY_SEARCH);
                })
                .setUseCustomRipple(true)
                .setOnLongClickListener((recycler, view, object, index) -> {
                    if (!Utility.isOnline(this))
                        return;
                    DatenObjekt datenObjekt = (DatenObjekt) ((Pair) object).first;
                    CustomDialog.Builder(this)
                            .setTitle(catigoryName + " Umbenennen, oder Löschen")
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setText(datenObjekt.getName())
                                    .setHint("Name"))
                            .setButtonType(CustomDialog.ButtonType.CUSTOM)
                            .addButton("Löschen", dialog -> {
                                CustomDialog.Builder(this)
                                        .setTitle("Löschen")
                                        .setText("Wirklich '" + ((DatenObjekt) ((Pair) object).first).getName() + "' löschen?")
                                        .setButtonType(CustomDialog.ButtonType.YES_NO)
                                        .addButton(CustomDialog.YES_BUTTON, dialog1 -> {
                                            dialog.dismiss();
                                            removeCatigory((DatenObjekt) ((Pair) object).first);
                                        })
                                        .show();
                            }, false)
                            .addButton("Abbrechen", dialog -> {})
                            .addButton("OK", dialog -> {
                                if (!Utility.isOnline(this))
                                    return;
                                ((DatenObjekt) ((Pair) object).first).setName(CustomDialog.getEditText(dialog));
                                reLoadRecycler();
                                Utility.saveAll(mySPR_daten, database);
                            })
                            .show();
                })
                .generateCustomRecycler();

    }

    private void removeCatigory(DatenObjekt datenObjekt) {
        allDatenObjektPairList.remove(datenObjekt);
        filterdDatenObjektPairList.remove(datenObjekt);
        switch (catigory) {
            case Genre:
                database.genreMap.remove(datenObjekt.getUuid());
                for (Video video : database.videoMap.values()) {
                    if (video.getGenreList().contains(datenObjekt.getUuid()))
                        video.getGenreList().remove(datenObjekt.getUuid());
                }
        }
        setResult(RESULT_OK);
        reLoadRecycler();
    }

    private void reLoadRecycler() {
        sortList(filterdDatenObjektPairList);
        customRecycler.setObjectList(filterdDatenObjektPairList).reload();
    }

    private void showRandomDialog() {
        final Pair<DatenObjekt, Integer>[] randomPair = new Pair[]{filterdDatenObjektPairList.get((int) (Math.random() * filterdDatenObjektPairList.size()))};
        CustomDialog.Builder(this)
                .setTitle("Zufall")
                .setText(randomPair[0].first.getName() + " (" + randomPair[0].second + ")")
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Nochmal", dialog -> {
                    randomPair[0] = filterdDatenObjektPairList.get((int) (Math.random() * filterdDatenObjektPairList.size()));
                            CustomDialog.changeText(dialog, randomPair[0].first.getName() + " (" + randomPair[0].second + ")");
                        },
                        false)
                .addButton("Suchen", dialog -> {
                    startActivityForResult(new  Intent(this, VideoActivity.class)
                                    .putExtra(VideoActivity.EXTRA_SEARCH, randomPair[0].first.getName())
                                    .putExtra(VideoActivity.EXTRA_SEARCH_CATIGORY, catigoryName),
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
