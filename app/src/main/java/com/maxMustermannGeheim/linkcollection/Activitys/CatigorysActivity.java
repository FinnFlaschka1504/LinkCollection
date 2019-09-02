package com.maxMustermannGeheim.linkcollection.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxMustermannGeheim.linkcollection.Daten.DatenObjekt;
import com.maxMustermannGeheim.linkcollection.Daten.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.List;

import static com.maxMustermannGeheim.linkcollection.Activitys.MainActivity.SHARED_PREFERENCES_NAME;

public class CatigorysActivity extends AppCompatActivity {
    private String catigoryName;
    private MainActivity.CATIGORYS catigory;
    private Database database = Database.getInstance();
    SharedPreferences mySPR_daten;

    private CustomRecycler customRecycler;
    private SearchView catigorys_search;
    private SearchView.OnQueryTextListener textListener;

    private List<DatenObjekt> allDatenObjektList;
    private List<DatenObjekt> filterdDatenObjektList;

    // ToDo: zurück navigieren hinzufügen und fixen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catigorys);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);

        catigoryName = getIntent().getStringExtra(MainActivity.EXTRA_CATIGORY);
        setTitle(catigoryName);
        if (catigoryName.equals(MainActivity.CATIGORYS.Darsteller.name()))
            catigory = MainActivity.CATIGORYS.Darsteller;
        else if (catigoryName.equals(MainActivity.CATIGORYS.Genre.name()))
            catigory = MainActivity.CATIGORYS.Genre;
        else if (catigoryName.equals(MainActivity.CATIGORYS.Studios.name()))
            catigory = MainActivity.CATIGORYS.Studios;

        switch (catigory) {
            case Genre:
                allDatenObjektList = new ArrayList<>(database.genreMap.values());
                break;
            case Darsteller:
                allDatenObjektList = new ArrayList<>(database.darstellerMap.values());
                break;
            case Studios:
                allDatenObjektList = new ArrayList<>(database.studioMap.values());
                break;
        }
        allDatenObjektList.sort((objekt1, objekt2) -> objekt1.getName().compareTo(objekt2.getName()));
        filterdDatenObjektList = new ArrayList<>(allDatenObjektList);
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
                filterdDatenObjektList = new ArrayList<>(allDatenObjektList);
                s = s.trim();
                if (!s.equals("")) {
                    for (DatenObjekt datenObjekt : allDatenObjektList) {
                            if (!datenObjekt.getName().toLowerCase().contains(s.toLowerCase()))
                                filterdDatenObjektList.remove(datenObjekt);
                        }
                }
                reLoadRecycler();
                return true;
            }
        };
        catigorys_search.setOnQueryTextListener(textListener);
        catigorys_search.setQueryHint(catigoryName + " filtern");

    }

    private void loadRecycler() {
        customRecycler = CustomRecycler.Builder(this, findViewById(R.id.catigorys_recycler))
                .setItemLayout(R.layout.list_item_catigory_item)
                .setObjectList(allDatenObjektList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.listItem_catigoryItem_name);
                    viewIdList.add(R.id.userlistItem_catigoryItem_count);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, viewIdMap, object) -> {
                    DatenObjekt datenObjekt = (DatenObjekt) object;
                    ((TextView) viewIdMap.get(R.id.listItem_catigoryItem_name)).setText(datenObjekt.getName());

                    int count = 0;
                    for (Video video : database.videoMap.values()) {
                        switch (catigory) {
                            case Genre:
                                if (video.getGenreList().contains(datenObjekt.getUuid()))
                                    count++;
                                break;
                            case Darsteller:
                                if (video.getDarstellerList().contains(datenObjekt.getUuid()))
                                    count++;
                                break;
                            case Studios:
                                if (video.getStudioList().contains(datenObjekt.getUuid()))
                                    count++;
                                break;
                        }
                    }
                    ((TextView) viewIdMap.get(R.id.userlistItem_catigoryItem_count)).setText(String.valueOf(count));
                })
                .setRowOrColumnCount(2)
                .setShowDivider(false)
                .setOnClickListener((recycler, view, object, index) -> {
                    startActivity(new  Intent(this, VideoActivity.class)
                        .putExtra(VideoActivity.EXTRA_SEARCH, ((DatenObjekt) object).getName())
                        .putExtra(VideoActivity.EXTRA_SEARCH_CATIGORY, catigoryName));
                })
                .setUseCustomRipple(true)
                .setOnLongClickListener((recycler, view, object, index) -> {
                    if (!Utility.isOnline(this))
                        return;
                    DatenObjekt datenObjekt = (DatenObjekt) object;
                    CustomDialog.Builder(this)
                            .setTitle(catigoryName + " Umbenennen, oder Löschen")
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setText(datenObjekt.getName())
                                    .setHint("Name"))
                            .setButtonType(CustomDialog.ButtonType.CUSTOM)
                            .addButton("Löschen", dialog -> {
                                CustomDialog.Builder(this)
                                        .setTitle("Löschen")
                                        .setText("Wirklich '" + ((DatenObjekt) object).getName() + "' löschen?")
                                        .setButtonType(CustomDialog.ButtonType.YES_NO)
                                        .addButton(CustomDialog.YES_BUTTON, dialog1 -> {
                                            dialog.dismiss();
                                            removeCatigory((DatenObjekt) object);
                                        })
                                        .show();
                            }, false)
                            .addButton("Abbrechen", dialog -> {})
                            .addButton("OK", dialog -> {
                                if (!Utility.isOnline(this))
                                    return;
                                ((DatenObjekt) object).setName(CustomDialog.getEditText(dialog));
                                reLoadRecycler();
                                Utility.saveAll(mySPR_daten, database);
                            })
                            .show();
                })
                .generateCustomRecycler();

    }

    private void removeCatigory(DatenObjekt datenObjekt) {
        allDatenObjektList.remove(datenObjekt);
        filterdDatenObjektList.remove(datenObjekt);
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
//        sortList(filterdVideoList);
        customRecycler.setObjectList(filterdDatenObjektList).reload();
    }

}
