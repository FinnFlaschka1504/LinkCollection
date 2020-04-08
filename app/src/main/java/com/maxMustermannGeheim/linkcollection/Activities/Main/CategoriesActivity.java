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
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomUtility;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class CategoriesActivity extends AppCompatActivity {
    public static final int START_CATIGORY_SEARCH = 001;
    public static final String EXTRA_SEARCH_CATEGORY = "EXTRA_SEARCH_CATOGORY";
    public static final String EXTRA_SEARCH = "EXTRA_SEARCH";
    public static String pictureRegex = "((https:)|/)([,+%&?=()/|.|\\w|\\s|-])+\\.(?:jpe?g|png|svg)";
    public static String pictureRegexAll = pictureRegex.split("\\\\\\.")[0];
    private Helpers.SortHelper<Pair<ParentClass, Integer>> sortHelper;

    enum SORT_TYPE {
        NAME, COUNT
    }

    public enum CATEGORIES {
        VIDEO("Video", "Videos", VideoActivity.class), DARSTELLER("Darsteller", "Darsteller", VideoActivity.class), STUDIOS("Studio", "Studios", VideoActivity.class), GENRE("Genre", "Genres", VideoActivity.class), KNOWLEDGE_CATEGORIES("Kategorie", "Kategorien", KnowledgeActivity.class), PERSON("Person", "Personen", OweActivity.class), JOKE_CATEGORIES("Witz", "Witze", JokeActivity.class), SHOW_GENRES("Genre", "Genres", ShowActivity.class), SHOW("Serie", "Serien", ShowActivity.class), EPISODE("Episode", "Episoden", ShowActivity.class);

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
    private boolean multiSelectMode;
    private List<ParentClass> selectedList = new ArrayList<>();

    private CustomRecycler customRecycler;
    private SearchView catigorys_search;
    private SearchView.OnQueryTextListener textListener;
    private TextView elementCount;

    private List<Pair<ParentClass, Integer>> allDatenObjektPairList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catigorys);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        sortHelper = new Helpers.SortHelper<Pair<ParentClass, Integer>>()
                .addSorter(SORT_TYPE.NAME)
                .changeType(parentClassIntegerPair -> parentClassIntegerPair.first.getName())

                .addSorter(SORT_TYPE.COUNT)
                .changeType(parentClassIntegerPair -> parentClassIntegerPair.second)
                .enableReverseDefaultComparable()
                .finish();

        catigory = (CATEGORIES) getIntent().getSerializableExtra(MainActivity.EXTRA_CATEGORY);
        setDatenObjektIntegerPairLiist();

        sortList(allDatenObjektPairList);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(catigory.getPlural());
        setSupportActionBar(toolbar);
        elementCount = findViewById(R.id.elementCount);

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        View noItem = findViewById(R.id.no_item);
        LinearLayout search_layout = findViewById(R.id.search_layout);
        appBarLayout.measure(0,0);
        toolbar.measure(0,0);
        search_layout.measure(0,0);
        float maxOffset = -(appBarLayout.getMeasuredHeight() - (toolbar.getMeasuredHeight() + search_layout.getMeasuredHeight()));
        float distance = 118;
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            float alpha = 1f - ((verticalOffset - maxOffset) / distance);
            noItem.setAlpha(alpha > 0f ? alpha : 0f);
        });

        catigorys_search = findViewById(R.id.search);

        loadRecycler();

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

        if (getIntent().hasExtra(EXTRA_SEARCH)) {
            catigorys_search.setQuery(getIntent().getStringExtra(EXTRA_SEARCH), false);
        }
    }

    private List<Pair<ParentClass, Integer>> filterList(List<Pair<ParentClass, Integer>> datenObjektPairList) {
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

    private List<Pair<ParentClass, Integer>> sortList(List<Pair<ParentClass, Integer>> datenObjektPairList) {
        sortHelper
                .setList(datenObjektPairList)
                .sort(sort_type);

//        switch (sort_type) {
//            case NAME:
//                datenObjektPairList.sort((objekt1, objekt2) -> objekt1.first.getName().compareTo(objekt2.first.getName()));
//                break;
//            case COUNT:
//                datenObjektPairList.sort((objekt1, objekt2) -> objekt1.second.compareTo(objekt2.second));
//                Collections.reverse(datenObjektPairList);
//                break;
//        }
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
            case SHOW_GENRES:
                for (ParentClass parentClass : database.showGenreMap.values()) {
                    int count = 0;
                    for (Show show : database.showMap.values()) {
                        if (show.getGenreIdList().contains(parentClass.getUuid()))
                            count++;
                    }
                    pairList.add(new Pair<>(parentClass, count));
                }
                break;

        }
        allDatenObjektPairList = pairList;
    }

    private void loadRecycler() {
        customRecycler = new CustomRecycler<Pair<ParentClass, Integer>>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_catigory_item)
                .setGetActiveObjectList(() -> {
                    List<Pair<ParentClass, Integer>> filteredList = sortList(filterList(allDatenObjektPairList));

                    TextView noItem = findViewById(R.id.no_item);
                    String text = catigorys_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
                    elementCount.setText(elementCountText);
                    return filteredList;

                })
                .setSetItemContent((customRecycler, itemView, parentClassIntegerPair) -> {
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
                    } else
                        ((TextView) itemView.findViewById(R.id.userlistItem_catigoryItem_count)).setText(String.valueOf(parentClassIntegerPair.second));

                    ImageView listItem_categoryItem_image = itemView.findViewById(R.id.listItem_categoryItem_image);
                    if (parentClassIntegerPair.first instanceof ParentClass_Tmdb && Utility.stringExists(((ParentClass_Tmdb) parentClassIntegerPair.first).getImagePath())) {
                        listItem_categoryItem_image.setVisibility(View.VISIBLE);
                        String imagePath = ((ParentClass_Tmdb) parentClassIntegerPair.first).getImagePath();
                        Utility.loadUrlIntoImageView(this, listItem_categoryItem_image, (imagePath.contains("https") ? "" : "https://image.tmdb.org/t/p/w92/") + imagePath, (imagePath.contains("https") ? "" : "https://image.tmdb.org/t/p/original/") + imagePath, this::removeFocusFromSearch);
                    } else
                        listItem_categoryItem_image.setVisibility(View.GONE);

                    CheckBox userListItem_categoryItem_check = itemView.findViewById(R.id.userlistItem_catigoryItem_ckeck);
                    userListItem_categoryItem_check.setVisibility(multiSelectMode ? View.VISIBLE : View.GONE);
                    userListItem_categoryItem_check.setChecked(selectedList.contains(parentClassIntegerPair.first));
                })
                .setRowOrColumnCount(columnCount)
                .hideDivider()
                .setOnClickListener((customRecycler, view, parentClassIntegerPair, index) -> {
                    if (!multiSelectMode) {
                        startActivityForResult(new Intent(this, catigory.getSearchIn())
                                        .putExtra(EXTRA_SEARCH, parentClassIntegerPair.first.getName())
                                        .putExtra(EXTRA_SEARCH_CATEGORY, catigory),
                                START_CATIGORY_SEARCH);
                    } else {
                        if (selectedList.contains(parentClassIntegerPair.first))
                            selectedList.remove(parentClassIntegerPair.first);
                        else
                            selectedList.add(parentClassIntegerPair.first);
                        customRecycler.update(index);
                    }
                })
                .setOnLongClickListener((customRecycler, view, item, index) -> {
                    if (!Utility.isOnline(this))
                        return;

                    removeFocusFromSearch();
                    ParentClass parentClass = item.first;

                    CustomDialog.Builder(this)
                            .setTitle(catigory.getSingular() + " Umbenennen, oder Löschen")
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog -> {
                                CustomDialog.Builder(this)
                                        .setTitle("Löschen")
                                        .setText("Wirklich '" + item.first.getName() + "' löschen?")
                                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                            customDialog.dismiss();
                                            removeCatigory(item);
                                        })
                                        .show();
                            }, false)
                            .transformPreviousButtonToImageButton()
                            .alignPreviousButtonsLeft()
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                                if (!Utility.isOnline(this))
                                    return;
                                item.first.setName(((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_name)).getText().toString().trim());
                                if (parentClass instanceof ParentClass_Tmdb) {
                                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim();
                                    ((ParentClass_Tmdb) parentClass).setImagePath(url);
                                }
                                reLoadRecycler();
                                Database.saveAll();
                            })
                            .setView(R.layout.dialog_edit_tmdb_category)
                            .setSetViewContent((customDialog, view1, reload) -> {
                                TextInputLayout dialog_editTmdbCategory_name_layout = view1.findViewById(R.id.dialog_editTmdbCategory_name_layout);
                                dialog_editTmdbCategory_name_layout.getEditText().setText(parentClass.getName());

                                com.finn.androidUtilities.Helpers.TextInputHelper helper =
                                        new com.finn.androidUtilities.Helpers.TextInputHelper(result -> customDialog.getActionButton().setEnabled(result), dialog_editTmdbCategory_name_layout);

                                if (parentClass instanceof ParentClass_Tmdb) {
                                    ImageView dialog_editTmdbCategory_internet = view1.findViewById(R.id.dialog_editTmdbCategory_internet);
                                    if (((ParentClass_Tmdb) parentClass).getTmdbId() != 0) {
                                        dialog_editTmdbCategory_internet.setOnClickListener(v -> Utility.openUrl(this, "https://www.themoviedb.org/person/" + ((ParentClass_Tmdb) parentClass).getTmdbId(), true));
                                        dialog_editTmdbCategory_internet.setVisibility(View.VISIBLE);
                                    }

                                    CustomUtility.setMargins(view1.findViewById(R.id.dialog_editTmdbCategory_nameLayout), -1, -1, -1, 0);
                                    view1.findViewById(R.id.dialog_editTmdbCategory_urlLayout).setVisibility(View.VISIBLE);
                                    TextInputLayout dialog_editTmdbCategory_url_layout = view1.findViewById(R.id.dialog_editTmdbCategory_url_layout);
                                    dialog_editTmdbCategory_url_layout.getEditText().setText(((ParentClass_Tmdb) parentClass).getImagePath());
                                    helper.addValidator(dialog_editTmdbCategory_url_layout).setValidation(dialog_editTmdbCategory_url_layout, (validator, text) -> {
                                        validator.asWhiteList();
                                        if (text.isEmpty() || text.matches(pictureRegexAll))
                                            validator.setValid();
                                        if (text.toLowerCase().contains("http") && !text.toLowerCase().contains("https"))
                                            validator.setInvalid("Die URL muss 'https' sein!");
                                    }).validate();

                                }
                            })
                            .enableDoubleClickOutsideToDismiss(customDialog -> {
                                boolean result = !((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_name)).getText().toString().trim().equals(parentClass.getName());
                                return result || (parentClass instanceof ParentClass_Tmdb && !Utility.boolOr(((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim(), ((ParentClass_Tmdb) parentClass).getImagePath(), ""));
                            })
                            .show();
                })
                .generate();

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
            case SHOW_GENRES:
                database.showGenreMap.remove(parentClass.getUuid());
                for (Show show : database.showMap.values()) {
                    show.getGenreIdList().remove(parentClass.getUuid());
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
        CustomList<Pair<ParentClass, Integer>> filterdDatenObjektPairList = new CustomList<>(sortList(filterList(allDatenObjektPairList)));
        if (filterdDatenObjektPairList.isEmpty()) {
            Toast.makeText(this, "Die Auswahl ist leer", Toast.LENGTH_SHORT).show();
            return;
        }
        removeFocusFromSearch();
        final Pair<ParentClass, Integer>[] randomPair = new Pair[]{filterdDatenObjektPairList.removeRandom()};
        CustomDialog
                .Builder(this)
                .setTitle("Zufall")
                .setText(randomPair[0].first.getName() + " (" + randomPair[0].second + ")")
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Nochmal", customDialog -> {
                    if (filterdDatenObjektPairList.isEmpty()) {
                        Toast.makeText(this, "Alle wurden bereits vorgeschlagen", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    randomPair[0] = filterdDatenObjektPairList.removeRandom();
                    CustomDialog.changeText(customDialog, randomPair[0].first.getName() + " (" + randomPair[0].second + ")");
                }, false)
                .addButton("Suchen", customDialog -> {
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
            case R.id.taskBar_catigory_multiSelect:
                if (selectedList.isEmpty()) {
                    multiSelectMode = !multiSelectMode;
                    reLoadRecycler();
                } else {
                    if (selectedList.size() == 1) {
                        startActivityForResult(new Intent(this, catigory.getSearchIn())
                                        .putExtra(EXTRA_SEARCH, selectedList.get(0).getName())
                                        .putExtra(EXTRA_SEARCH_CATEGORY, catigory),
                                START_CATIGORY_SEARCH);
                        break;
                    }
                    CustomDialog.Builder(this)
                            .setTitle("Aktion Auswählen")
                            .setText("Möchtest du als '&', oder '|' suchen, oder die Auswahl zurücksetzen?")
                            .addButton("Zurücksetzen", customDialog -> {
                                multiSelectMode = false;
                                selectedList.clear();
                                reLoadRecycler();
                            })
                            .alignPreviousButtonsLeft()
                            .addButton("&", customDialog -> {
                                startActivityForResult(new Intent(this, catigory.getSearchIn())
                                                .putExtra(EXTRA_SEARCH, selectedList.stream().map(ParentClass::getName).collect(Collectors.joining(" & ")))
                                                .putExtra(EXTRA_SEARCH_CATEGORY, catigory),
                                        START_CATIGORY_SEARCH);
                            })
                            .colorLastAddedButton()
                            .addButton("|", customDialog -> {
                                startActivityForResult(new Intent(this, catigory.getSearchIn())
                                                .putExtra(EXTRA_SEARCH, selectedList.stream().map(ParentClass::getName).collect(Collectors.joining(" | ")))
                                                .putExtra(EXTRA_SEARCH_CATEGORY, catigory),
                                        START_CATIGORY_SEARCH);
                            })
                            .colorLastAddedButton()
                            .show();
                }
                break;
            case R.id.taskBar_catigory_random:
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

    private void removeFocusFromSearch() {
        catigorys_search.clearFocus();
    }
}
