package com.maxMustermannGeheim.linkcollection.Activities.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Content.JokeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.KnowledgeActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.OweActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.ShowActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Content.Videos.VideoActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.Show;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class CategoriesActivity extends AppCompatActivity {
    public static final int START_CATEGORY_SEARCH = 001;
    public static final String EXTRA_SEARCH_CATEGORY = "EXTRA_SEARCH_CATEGORY";
    public static final String EXTRA_SEARCH = "EXTRA_SEARCH";
    public static String pictureRegex = "((https:)|\\/)([^\\s\\\\]|(?<=\\S) (?=\\S))+?\\.(?:jpe?g|png|svg)";//"((https:)|/)[^\\n]+?\\.(?:jpe?g|png|svg)";
//    public static String pictureRegex = "((https:)|/)([,+%&?=()/|.|\\w|\\s|-])+\\.(?:jpe?g|png|svg)";
    public static String pictureRegexAll = pictureRegex.split("\\\\\\.")[0];
    public static final String uuidRegex = "\\b([a-zA-Z]+_)?[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
    private Helpers.SortHelper<Pair<ParentClass, Integer>> sortHelper;
    private boolean reverse = false;
    private Menu toolBarMenu;

    enum SORT_TYPE {
        NAME, COUNT, TIME
    }

    public enum CATEGORIES {
        VIDEO("Video", "Videos", VideoActivity.class), DARSTELLER("Darsteller", "Darsteller", VideoActivity.class), STUDIOS("Studio", "Studios", VideoActivity.class),
        GENRE("Genre", "Genres", VideoActivity.class), COLLECTION("Sammlung", "Sammlungen", VideoActivity.class), KNOWLEDGE_CATEGORIES("Kategorie", "Kategorien", KnowledgeActivity.class),
        PERSON("Person", "Personen", OweActivity.class), JOKE_CATEGORIES("Witz", "Witze", JokeActivity.class), SHOW_GENRES("Genre", "Genres", ShowActivity.class),
        SHOW("Serie", "Serien", ShowActivity.class), EPISODE("Episode", "Episoden", ShowActivity.class);

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
    private CATEGORIES category;
    private SORT_TYPE sort_type = SORT_TYPE.NAME;
    private Database database = Database.getInstance();
    SharedPreferences mySPR_daten;
    private String searchQuery = "";
    private boolean multiSelectMode;
    private List<ParentClass> selectedList = new ArrayList<>();
    private Runnable setToolbarTitle;

    private CustomRecycler customRecycler;
    private SearchView categories_search;
    private SearchView.OnQueryTextListener textListener;
    private TextView elementCount;

    private List<Pair<ParentClass, Integer>> allDatenObjektPairList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_catigorys);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        sortHelper = new Helpers.SortHelper<Pair<ParentClass, Integer>>()
                .addSorter(SORT_TYPE.NAME)
                .changeType(parentClassIntegerPair -> parentClassIntegerPair.first.getName())

                .addSorter(SORT_TYPE.COUNT)
                .changeType(parentClassIntegerPair -> parentClassIntegerPair.second)
                .enableReverseDefaultComparable()

                .addSorter(SORT_TYPE.TIME)
                .changeType(parentClassIntegerPair -> {
                    ParentClass parentClass = parentClassIntegerPair.first;
                    List<Video> allRelatedVideos = new CustomList<>();
                    switch (category) {
                        case DARSTELLER:
                            for (Video video : database.videoMap.values()) {
                                    if (video.getDarstellerList().contains(parentClass.getUuid()))
                                        allRelatedVideos.add(video);
                                }
                            break;
                        case GENRE:
                            for (Video video : database.videoMap.values()) {
                                    if (video.getGenreList().contains(parentClass.getUuid()))
                                        allRelatedVideos.add(video);
                                }
                            break;
                        case STUDIOS:
                            for (Video video : database.videoMap.values()) {
                                    if (video.getStudioList().contains(parentClass.getUuid()))
                                        allRelatedVideos.add(video);
                                }
                            break;
                    }
                    Date date = CustomUtility.concatenateCollections(allRelatedVideos, Video::getDateList).stream().max(Date::compareTo).orElse(null);
                    return date != null ? date : parentClass.getName();

                })
                .addCondition((o1, o2) -> {
                    int result = 0;
                    if (o1 instanceof Date && o2 instanceof Date)
                        result = ((Date) o1).compareTo((Date) o2) * -1;
                    else if (o1 instanceof String && o2 instanceof String)
                        result = ((String) o1).compareTo((String) o2);
                    else
                        result = o1 instanceof Date ? -1 : 1;
                    return result * (reverse ? -1 : 1);
                })
                .enableReverseDefaultComparable()
                .finish();

        category = (CATEGORIES) getIntent().getSerializableExtra(MainActivity.EXTRA_CATEGORY);


        loadDatabase();
    }

    private void loadDatabase() {
        Runnable whenLoaded = () -> {
            setContentView(R.layout.activity_catigorys);

            setDatenObjektIntegerPairList();
            sortList(allDatenObjektPairList);


            setLayout();
        };

        if (Database.isReady() && database != null)
            whenLoaded.run();
        else
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);

    }

    private void setLayout() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(category.getPlural());
        setSupportActionBar(toolbar);
        elementCount = findViewById(R.id.elementCount);

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        TextView noItem = findViewById(R.id.no_item);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, toolbar.getTitle().toString());

        categories_search = findViewById(R.id.search);

        loadRecycler();

        textListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                this.onQueryTextChange(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchQuery = s.trim();

                reLoadRecycler();
                return true;
            }
        };
        categories_search.setOnQueryTextListener(textListener);
        categories_search.setQueryHint(category.getPlural() + " filtern");

        if (getIntent().hasExtra(EXTRA_SEARCH)) {
            categories_search.setQuery(getIntent().getStringExtra(EXTRA_SEARCH), false);
        }
    }

    private List<Pair<ParentClass, Integer>> filterList(List<Pair<ParentClass, Integer>> datenObjektPairList) {
        if (!searchQuery.equals("")) {
            return datenObjektPairList.stream().filter(datenObjektIntegerPair -> ParentClass_Alias.containsQuery(datenObjektIntegerPair.first, searchQuery.toLowerCase())).collect(Collectors.toList());
        } else
            return new ArrayList<>(datenObjektPairList);
    }

    private List<Pair<ParentClass, Integer>> sortList(List<Pair<ParentClass, Integer>> datenObjektPairList) {
        sortHelper
                .setAllReversed(reverse)
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

    private void setDatenObjektIntegerPairList() {
        List<Pair<ParentClass, Integer>> pairList = new ArrayList<>();

        switch (category) {
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
                    String text = categories_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
                    elementCount.setText(elementCountText);
                    return filteredList;

                })
                .setSetItemContent((customRecycler, itemView, parentClassIntegerPair) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_catigoryItem_name)).setText(parentClassIntegerPair.first.getName());

                    if (category == CATEGORIES.PERSON) {
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
                        Utility.loadUrlIntoImageView(this, listItem_categoryItem_image, Utility.getTmdbImagePath_ifNecessary(imagePath, false),
                                Utility.getTmdbImagePath_ifNecessary(imagePath, true),
                                () -> ((ParentClass_Tmdb) parentClassIntegerPair.first).tryUpdateData(this, () ->
                                        customRecycler.update(customRecycler.getRecycler().getChildAdapterPosition(itemView))),
                                () -> Utility.roundImageView(listItem_categoryItem_image, 4), this::removeFocusFromSearch);
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
                        removeFocusFromSearch();
                        startActivityForResult(new Intent(this, category.getSearchIn())
                                        .putExtra(EXTRA_SEARCH, parentClassIntegerPair.first.getName())
                                        .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                START_CATEGORY_SEARCH);
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
                            .setTitle(category.getSingular() + " Umbenennen, oder Löschen")
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
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
                            .enableDynamicWrapHeight(this)
                            .enableAutoUpdateDynamicWrapHeight()
                            .addOptionalModifications(customDialog1 -> {
                                if (parentClass instanceof ParentClass_Tmdb) {
                                    customDialog1
                                            .addButton("Testen", customDialog2 -> {
                                                String url = ((EditText) customDialog2.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim();
                                                ImageView preview = customDialog2.findViewById(R.id.dialog_editTmdbCategory_preview);
                                                if (CustomUtility.stringExists(url)) {
                                                    Utility.loadUrlIntoImageView(this, preview, Utility.getTmdbImagePath_ifNecessary(url, true), null);
                                                    preview.setVisibility(View.VISIBLE);
                                                } else {
                                                    Toast.makeText(this, "Nichts eingegeben", Toast.LENGTH_SHORT).show();
                                                    preview.setVisibility(View.GONE);
                                                }
                                            }, false);
                                }
                            })
                            .alignPreviousButtonsLeft()
                            .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                                if (!Utility.isOnline(this))
                                    return;
                                ParentClass_Alias.applyNameAndAlias(item.first, ((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_name)).getText().toString().trim());
                                if (parentClass instanceof ParentClass_Tmdb) {
                                    String url = ((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim();
                                    ((ParentClass_Tmdb) parentClass).setImagePath(CustomUtility.stringExists(url) ? url : null);
                                }
                                reLoadRecycler();
                                Toast.makeText(this, (Database.saveAll_simple() ? "" : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();

                            })
                            .setView(R.layout.dialog_edit_tmdb_category)
                            .setSetViewContent((customDialog, view1, reload) -> {
                                TextInputLayout dialog_editTmdbCategory_name_layout = view1.findViewById(R.id.dialog_editTmdbCategory_name_layout);
                                dialog_editTmdbCategory_name_layout.getEditText().setText(ParentClass_Alias.combineNameAndAlias(parentClass));

                                com.finn.androidUtilities.Helpers.TextInputHelper helper =
                                        new com.finn.androidUtilities.Helpers.TextInputHelper((Button) customDialog.getActionButton().getButton(), dialog_editTmdbCategory_name_layout);

                                if (parentClass instanceof ParentClass_Alias) {
//                                    helper.setInputType(dialog_editTmdbCategory_name_layout, com.finn.androidUtilities.Helpers.TextInputHelper.INPUT_TYPE.MULTI_LINE);
                                    dialog_editTmdbCategory_name_layout.getEditText().setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                                }

                                if (parentClass instanceof ParentClass_Tmdb) {
                                    ImageView dialog_editTmdbCategory_internet = view1.findViewById(R.id.dialog_editTmdbCategory_internet);
                                    if (((ParentClass_Tmdb) parentClass).getTmdbId() != 0) {
                                        dialog_editTmdbCategory_internet.setOnClickListener(v -> Utility.openUrl(this, "https://www.themoviedb.org/person/" + ((ParentClass_Tmdb) parentClass).getTmdbId(), true));
                                        dialog_editTmdbCategory_internet.setVisibility(View.VISIBLE);
                                    }

                                    CustomUtility.setMargins(view1.findViewById(R.id.dialog_editTmdbCategory_nameLayout), -1, -1, -1, 0);
                                    view1.findViewById(R.id.dialog_editTmdbCategory_urlLayout).setVisibility(View.VISIBLE);
                                    TextInputLayout dialog_editTmdbCategory_url_layout = view1.findViewById(R.id.dialog_editTmdbCategory_url_layout);
                                    String url = ((ParentClass_Tmdb) parentClass).getImagePath();
                                    dialog_editTmdbCategory_url_layout.getEditText().setText(url);
                                    ImageView preview = customDialog.findViewById(R.id.dialog_editTmdbCategory_preview);
                                    if (CustomUtility.stringExists(url)) {
                                        Utility.loadUrlIntoImageView(this, preview, Utility.getTmdbImagePath_ifNecessary(url, true), null);
                                        preview.setVisibility(View.VISIBLE);
                                    }
                                    helper.addValidator(dialog_editTmdbCategory_url_layout).setValidation(dialog_editTmdbCategory_url_layout, (validator, text) -> {
                                        validator.asWhiteList();
                                        if (text.isEmpty() || text.matches(pictureRegexAll) || text.matches(ActivityResultHelper.uriRegex))
                                            validator.setValid();
                                        if (text.toLowerCase().contains("http") && !text.toLowerCase().contains("https"))
                                            validator.setInvalid("Die URL muss 'https' sein!");
                                    }).validate();

                                    view1.findViewById(R.id.dialog_editTmdbCategory_localStorage).setOnClickListener(v -> {
                                        ActivityResultHelper.addFileChooserRequest(this, "image/*", o -> {
                                            dialog_editTmdbCategory_url_layout.getEditText().setText(((Intent) o).getData().toString());
                                        });
                                    });

                                }
                            })
                            .enableDoubleClickOutsideToDismiss(customDialog -> {
                                boolean result = !((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_name)).getText().toString().trim().equals(ParentClass_Alias.combineNameAndAlias(parentClass));
                                return result || (parentClass instanceof ParentClass_Tmdb && !Utility.boolOr(((EditText) customDialog.findViewById(R.id.dialog_editTmdbCategory_url)).getText().toString().trim(), ((ParentClass_Tmdb) parentClass).getImagePath(), ""));
                            })
                            .show();
                })
                .generate();

    }

    private void removeCatigory(Pair<ParentClass, Integer> item) {
        ParentClass parentClass = item.first;
        allDatenObjektPairList.remove(item);
        switch (category) {
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
                                    .putExtra(EXTRA_SEARCH_CATEGORY, category),
                            START_CATEGORY_SEARCH);
                })
                .colorLastAddedButton()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolBarMenu = menu;
        getMenuInflater().inflate(R.menu.task_bar_catigory, toolBarMenu);

        if (setToolbarTitle != null) setToolbarTitle.run();
        menu.findItem(R.id.taskBar_category_sortByTime).setVisible(category.getSearchIn().equals(VideoActivity.class));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_category_multiSelect:
                if (selectedList.isEmpty()) {
                    multiSelectMode = !multiSelectMode;
                    reLoadRecycler();
                } else {
                    if (selectedList.size() == 1) {
                        startActivityForResult(new Intent(this, category.getSearchIn())
                                        .putExtra(EXTRA_SEARCH, selectedList.get(0).getName())
                                        .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                START_CATEGORY_SEARCH);
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
                                startActivityForResult(new Intent(this, category.getSearchIn())
                                                .putExtra(EXTRA_SEARCH, selectedList.stream().map(ParentClass::getName).collect(Collectors.joining(" & ")))
                                                .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                        START_CATEGORY_SEARCH);
                            })
                            .colorLastAddedButton()
                            .addButton("|", customDialog -> {
                                startActivityForResult(new Intent(this, category.getSearchIn())
                                                .putExtra(EXTRA_SEARCH, selectedList.stream().map(ParentClass::getName).collect(Collectors.joining(" | ")))
                                                .putExtra(EXTRA_SEARCH_CATEGORY, category),
                                        START_CATEGORY_SEARCH);
                            })
                            .colorLastAddedButton()
                            .show();
                }
                break;
            case R.id.taskBar_category_random:
                showRandomDialog();
                break;
            case R.id.taskBar_category_sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_category_sortByViews:
                sort_type = SORT_TYPE.COUNT;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_category_sortByTime:
                sort_type = SORT_TYPE.TIME;
                item.setChecked(true);
                reverse = true;
                toolBarMenu.findItem(R.id.taskBar_category_sortReverse).setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_category_sortReverse:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                reverse = checked;
                reLoadRecycler();
                break;

            case R.id.taskBar_category_showAs:
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
            setDatenObjektIntegerPairList();
            textListener.onQueryTextChange(categories_search.getQuery().toString());
            reLoadRecycler();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeFocusFromSearch() {
        categories_search.clearFocus();
    }

    @Override
    public void onBackPressed() {
        if (Utility.stringExists(categories_search.getQuery().toString()) && !Objects.equals(categories_search.getQuery().toString(), getIntent().getStringExtra(EXTRA_SEARCH))) {
            categories_search.setQuery("", false);
            return;
        } else if (multiSelectMode) {
            multiSelectMode = false;
            selectedList.clear();
            reLoadRecycler();
            return;
        }

        super.onBackPressed();
    }
}
