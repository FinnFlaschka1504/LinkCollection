package com.maxMustermannGeheim.linkcollection.Activities.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class CategoriesActivity extends AppCompatActivity {
    public static final int START_CATIGORY_SEARCH = 001;
    public static final String EXTRA_SEARCH_CATEGORY = "EXTRA_SEARCH_CATOGORY";
    public static final String EXTRA_SEARCH = "EXTRA_SEARCH";

    enum SORT_TYPE {
        NAME, COUNT
    }

    public enum CATEGORIES {
        VIDEO("Video", "Videos", VideoActivity.class), DARSTELLER("Darsteller", "Darsteller", VideoActivity.class)
        , STUDIOS("Studio", "Studios", VideoActivity.class), GENRE("Genre", "Genres", VideoActivity.class)
        , WATCH_LATER("WATCH_LATER", "WATCH_LATER", VideoActivity.class), KNOWLEDGE_CATEGORIES("Kategorie", "Kategorien", KnowledgeActivity.class)
        , PERSON("Person", "Personen", OweActivity.class) , JOKE_CATEGORIES("Witz", "Witze", JokeActivity.class);

        private String singular;
        private String plural;
        private Class searchIn;

        CATEGORIES(String singular, String plural, Class searchIn) {
            this.singular = singular;
            this.plural = plural;
            this.searchIn = searchIn;
        }

        public String getSingular() {
            return singular;
        }

        public String getPlural() {
            return plural;
        }

        public Class getSearchIn() {
            return searchIn;
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


        catigory = (CATEGORIES) getIntent().getSerializableExtra(MainActivity.EXTRA_CATEGORY);
        setTitle(catigory.getPlural());
        setDatenObjektIntegerPairLiist();

        sortList(allDatenObjektPairList);
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
                break;
            case KNOWLEDGE_CATEGORIES:
                for (ParentClass parentClass : database.knowledgeCategoryMap.values()) {
                    int count = 0;
                    for (Knowledge knowledge : database.knowledgeMap.values()) {
                        if (knowledge.getCategoryIdList().contains(parentClass.getUuid()))
                            count++;
                    }
                    pairList.add(new Pair<>(parentClass, count));
                }
                break;
            case PERSON:
                for (ParentClass parentClass : database.personMap.values()) {
                    final int[] count = {0};
                    for (Owe owe : database.oweMap.values()) {
                        owe.getItemList().stream().filter(item -> item.getPersonId().equals(parentClass.getUuid())).forEach(item -> count[0] += Math.round(item.getAmount()));
                    }
                    pairList.add(new Pair<>(parentClass, count[0]));
                }
                break;
            case JOKE_CATEGORIES:
                for (ParentClass parentClass : database.jokeCategoryMap.values()) {
                    int count = 0;
                    for (Joke joke : database.jokeMap.values()) {
                        if (joke.getCategoryIdList().contains(parentClass.getUuid()))
                            count++;
                    }
                    pairList.add(new Pair<>(parentClass, count));
                }
                break;

        }
        allDatenObjektPairList = pairList;
    }

    private void loadRecycler() {
        customRecycler = CustomRecycler.Builder(this, findViewById(R.id.catigorys_recycler))
                .setItemLayout(R.layout.list_item_catigory_item)
                .setGetActiveObjectList(() -> sortList(filterList(allDatenObjektPairList)))
                .setSetItemContent((CustomRecycler.SetItemContent<Pair<ParentClass, Integer>>)(itemView, parentClassIntegerPair) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_catigoryItem_name)).setText(parentClassIntegerPair.first.getName());

                    if (catigory == CATEGORIES.PERSON) {
                        final double[] allOwn = {0};
                        final double[] allOther = {0};
                        database.oweMap.values().forEach(owe -> owe.getItemList().forEach(item -> {
                            if (item.isOpen()) {
                                if (item.getPersonId().equals(parentClassIntegerPair.first.getUuid())) {
                                    if (owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OWN)
                                        allOwn[0] += item.getAmount();
                                    else
                                        allOther[0] += item.getAmount();
                                }
                            }
                        }));
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                        if (allOwn[0] != 0)
                            stringBuilder.append("E: " + Utility.formatToEuro(allOwn[0]), new ForegroundColorSpan(Color.RED), Spannable.SPAN_COMPOSING);
                        if (allOther[0] != 0) {
                            if (!stringBuilder.toString().isEmpty())
                                stringBuilder.append(" & ");
                            stringBuilder.append("F: " + Utility.formatToEuro(allOther[0]), new ForegroundColorSpan(getColor(R.color.colorGreen)), Spannable.SPAN_COMPOSING);
                        }
                        if (stringBuilder.toString().isEmpty())
                            stringBuilder.append("<Keine Schulden>", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_COMPOSING);

                        ((TextView) itemView.findViewById(R.id.userlistItem_catigoryItem_count)).setText(stringBuilder);
                    }
                    else
                        ((TextView) itemView.findViewById(R.id.userlistItem_catigoryItem_count)).setText(String.valueOf(parentClassIntegerPair.second));
                })
                .setRowOrColumnCount(columnCount)
                .hideDivider()
                .setOnClickListener((customRecycler, view, object, index) -> {
                    startActivityForResult(new Intent(this, catigory.getSearchIn())
                                    .putExtra(EXTRA_SEARCH, ((ParentClass) ((Pair) object).first).getName())
                                    .putExtra(EXTRA_SEARCH_CATEGORY, catigory),
                            START_CATIGORY_SEARCH);
                })
                .useCustomRipple()
                .setOnLongClickListener((CustomRecycler.OnLongClickListener<Pair<ParentClass, Integer>>)(customRecycler, view, item, index) -> {
                    if (!Utility.isOnline(this))
                        return;
                    ParentClass parentClass = item.first;
                    CustomDialog.Builder(this)
                            .setTitle(catigory.getSingular() + " Umbenennen, oder Löschen")
                            .setEdit(new CustomDialog.EditBuilder()
                                    .setText(parentClass.getName())
                                    .setHint("Name"))
                            .setButtonType(CustomDialog.ButtonType.CUSTOM)
                            .addButton("Löschen", (customDialog, dialog) -> {
                                CustomDialog.Builder(this)
                                        .setTitle("Löschen")
                                        .setText("Wirklich '" + item.first.getName() + "' löschen?")
                                        .setButtonType(CustomDialog.ButtonType.YES_NO)
                                        .addButton(CustomDialog.YES_BUTTON, (customDialog1, dialog1) -> {
                                            dialog.dismiss();
                                            removeCatigory(item);
                                        })
                                        .show();
                            }, false)
                            .addButton("Abbrechen", (customDialog, dialog) -> {
                            })
                            .addButton("OK", (customDialog, dialog) -> {
                                if (!Utility.isOnline(this))
                                    return;
                                ((ParentClass) ((Pair) item).first).setName(CustomDialog.getEditText(dialog));
                                reLoadRecycler();
                                Database.saveAll();
                            })
                            .show();
                })
                .generateCustomRecycler();

    }

    private void removeCatigory(Pair<ParentClass, Integer> item) {
        ParentClass parentClass = item.first;
        allDatenObjektPairList.remove(item);
        switch (catigory) {
            case DARSTELLER:
                database.darstellerMap.remove(parentClass.getUuid());
                for (Video video : database.videoMap.values()) {
                    video.getDarstellerList().remove(parentClass.getUuid());
                }
                break;
            case STUDIOS:
                database.studioMap.remove(parentClass.getUuid());
                for (Video video : database.videoMap.values()) {
                    video.getStudioList().remove(parentClass.getUuid());
                }
                break;
            case GENRE:
                database.genreMap.remove(parentClass.getUuid());
                for (Video video : database.videoMap.values()) {
                    video.getGenreList().remove(parentClass.getUuid());
                }
                break;
            case KNOWLEDGE_CATEGORIES:
                database.knowledgeCategoryMap.remove(parentClass.getUuid());
                for (Knowledge knowledge : database.knowledgeMap.values()) {
                    knowledge.getCategoryIdList().remove(parentClass.getUuid());
                }
                break;
            case PERSON:
                database.personMap.remove(parentClass.getUuid());
                for (Owe owe : database.oweMap.values()) {
                    owe.setItemList(owe.getItemList().stream().filter(item1 -> !item1.getPersonId().equals(parentClass.getUuid())).collect(Collectors.toList()));
                }
                break;
            case JOKE_CATEGORIES:
                database.jokeCategoryMap.remove(parentClass.getUuid());
                for (Joke joke : database.jokeMap.values()) {
                    joke.getCategoryIdList().remove(parentClass.getUuid());
                }
                break;

        }
        Database.saveAll();
        setResult(RESULT_OK);
        reLoadRecycler();
    }

    private void reLoadRecycler() {
        customRecycler.reload();
    }

    private void showRandomDialog() {
        List<Pair<ParentClass, Integer>> filterdDatenObjektPairList = sortList(filterList(allDatenObjektPairList));
        if (filterdDatenObjektPairList.isEmpty()) {
            Toast.makeText(this, "Die Auswahl ist leer", Toast.LENGTH_SHORT).show();
            return;
        }
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
                                    .putExtra(EXTRA_SEARCH, randomPair[0].first.getName())
                                    .putExtra(EXTRA_SEARCH_CATEGORY, catigory),
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
