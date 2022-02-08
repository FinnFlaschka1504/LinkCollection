package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.UrlParser;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.ExternalCode;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.finn.androidUtilities.CustomDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KnowledgeActivity extends AppCompatActivity {
    private final String ADVANCED_SEARCH_CRITERIA_CATEGORY = "c";


    enum SORT_TYPE {
        NAME, RATING, LATEST
    }

    public enum FILTER_TYPE {
        NAME("Titel"), CATEGORY("Kategorie"), CONTENT("Inhalt");

        String name;

        FILTER_TYPE() {
        }

        FILTER_TYPE(String name) {
            this.name = name;
        }

        public boolean hasName() {
            return name != null;
        }

        public String getName() {
            return name;
        }
    }


    private boolean reverse;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    Database database = Database.getInstance();
    private CustomRecycler<CustomRecycler.Expandable<Knowledge>> customRecycler_List;
    private String searchQuery = "";
    private SharedPreferences mySPR_daten;
    private SearchView.OnQueryTextListener textListener;
    private TextView elementCount;
    private SearchView knowledge_search;
    private ArrayList<Knowledge> allKnowledgeList;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.CATEGORY, FILTER_TYPE.CONTENT));
    private CustomDialog detailDialog;
    private boolean isDialog;
    private Runnable setToolbarTitle;
    private Helpers.AdvancedQueryHelper<Knowledge> advancedQueryHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!(isDialog = Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHOW_AS_DIALOG)))
            setTheme(R.style.AppTheme_NoTitle);

        super.onCreate(savedInstanceState);

        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_knowledge);

        Settings.startSettings_ifNeeded(this);
        ExternalCode.initialize_ifNecessary(this);
        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
            setContentView(R.layout.activity_knowledge);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, toolbar.getTitle().toString());

            knowledge_search = findViewById(R.id.search);
            advancedQueryHelper = new Helpers.AdvancedQueryHelper<Knowledge>(this, knowledge_search)
                    .setRestFilter((restQuery, knowledgeList) -> {
                        if (searchQuery.contains("|"))
                            knowledgeList.filterOr(searchQuery.split("\\|"), (knowledge, s) -> Utility.containedInKnowledge(s.trim(), knowledge, filterTypeSet), true);
                        else
                            knowledgeList.filterAnd(searchQuery.split("&"), (knowledge, s) -> Utility.containedInKnowledge(s.trim(), knowledge, filterTypeSet), true);
                    })
                    .addCriteria_defaultName()
                    .enableColoration()
                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA_CATEGORY, CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, Knowledge::getCategoryIdList);


            loadRecycler();

            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchQuery = s.trim().toLowerCase();
                    reLoadRecycler();
                    return true;
                }
            };
            knowledge_search.setOnQueryTextListener(textListener);

            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHORTCUT))
                showEditOrNewDialog(null);

            if (getIntent().getAction() != null && getIntent().getAction().equals("android.intent.action.SEND")) {
                CharSequence text;
                text = getIntent().getClipData().getItemAt(0).getText();
                if (text == null) {
                    text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
                }

                if (text != null) {
                    List<Knowledge> knowledgeList = database.knowledgeMap.values().stream().sorted((o1, o2) -> o1.getLastChanged().compareTo(o2.getLastChanged()) * -1).collect(Collectors.toList());
                    com.finn.androidUtilities.CustomDialog selectDialog = com.finn.androidUtilities.CustomDialog.Builder(this);
                    CharSequence finalText = text;
                    CustomRecycler<String> customRecycler = new CustomRecycler<String>(this)
                            .enableDivider(12)
                            .disableCustomRipple()
                            .setGetActiveObjectList(customRecycler1 -> knowledgeList.stream().map(ParentClass::getName).collect(Collectors.toList()))
                            .setOnClickListener((customRecycler1, itemView, s, index) -> {
                                Knowledge knowledge = knowledgeList.get(index);
                                Pair<CustomDialog, Knowledge> customDialogKnowledgePair = showEditOrNewDialog(knowledge);
                                CustomDialog sourcesDialog = showSourcesDialog(customDialogKnowledgePair.second, customDialogKnowledgePair.first.findViewById(R.id.dialog_editOrAddKnowledge_sources), true);
                                TextInputLayout dialog_sources_url = sourcesDialog.findViewById(R.id.dialog_sources_url);
                                dialog_sources_url.getEditText().setText(finalText);

                                selectDialog.dismiss();
                            });

                    selectDialog
                            .setTitle("Wissen Auswählen")
                            .addButton("Neu", customDialog -> {
                                Pair<CustomDialog, Knowledge> customDialogKnowledgePair = showEditOrNewDialog(null);
                                CustomDialog sourcesDialog = showSourcesDialog(customDialogKnowledgePair.second, customDialogKnowledgePair.first.findViewById(R.id.dialog_editOrAddKnowledge_sources), true);
                                TextInputLayout dialog_sources_url = sourcesDialog.findViewById(R.id.dialog_sources_url);
                                dialog_sources_url.getEditText().setText(finalText);
                            })
                            .colorLastAddedButton()
                            .alignPreviousButtonsLeft()
                            .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.BACK_BUTTON)
                            .setView(customRecycler.generateRecyclerView())
                            .disableScroll()
                            .show();
                    //                    isShared = true;
//                    if (Utility.isUrl(text.toString())) {
//                        String url = text.toString();
//                        Video video = new Video("").setUrl(url);
//                        if (url.contains("lookmovie")) {
//                            String last = new CustomList<>(url.split("/")).getLast();
//                            if (last != null) {
//                                CustomList<String> list = new CustomList<>(last.split("-"));
//                                if (list.getLast().matches("\\d+"))
//                                    list.removeLast();
//                                video.setName(String.join(" ", list));
//                            }
//                        }
//                        showEditOrNewDialog(video);
//                    } else
//                        showEditOrNewDialog(new Video(text.toString()));
                }
            }

            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null)
                    advancedQueryHelper.wrapAndSetExtraSearch(extraSearchCategory, extraSearch);
            }
            setSearchHint();

            if (isDialog) {
                findViewById(R.id.recycler).setVisibility(View.GONE);
                knowledge_search.setVisibility(View.GONE);
                findViewById(R.id.divider).setVisibility(View.GONE);
                findViewById(R.id.appBarLayout).setVisibility(View.GONE);
                if (getIntent().getBooleanExtra(MainActivity.EXTRA_SHOW_RANDOM, false))
                    showRandomDialog();
            }

        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();

    }

    private void loadRecycler() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        customRecycler_List = new CustomRecycler<CustomRecycler.Expandable<Knowledge>>(this, findViewById(R.id.recycler))
                .setGetActiveObjectList(customRecycler -> {
                    List<CustomRecycler.Expandable<Knowledge>> filteredList;
                    if (searchQuery.equals("")) {
                        allKnowledgeList = new ArrayList<>(database.knowledgeMap.values());
                        filteredList = toExpandableList(sortList(allKnowledgeList));
                    } else
                        filteredList = toExpandableList(filterList(allKnowledgeList));

                    TextView noItem = findViewById(R.id.no_item);
                    String text = knowledge_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
                    elementCount.setText(elementCountText);
                    return filteredList;
                })
                .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper<Knowledge>(R.layout.list_item_knowledge, (customRecycler1, itemView, knowledge, expanded, index) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_knowledge_title)).setText(knowledge.getName());

                    TextView listItem_knowledge_content = itemView.findViewById(R.id.listItem_knowledge_content);
                    RecyclerView listItem_knowledge_list = itemView.findViewById(R.id.listItem_knowledge_list);
                    if (knowledge.hasContent()) {
                        listItem_knowledge_content.setVisibility(View.VISIBLE);
                        listItem_knowledge_list.setVisibility(View.GONE);
                        listItem_knowledge_content.setText(applyFormatting_text(knowledge.getContent()));
                    } else if (knowledge.hasItems()) {
                        if (expanded) {
                            listItem_knowledge_list.setVisibility(View.VISIBLE);
                            listItem_knowledge_content.setVisibility(View.GONE);
                            generateItemTextRecycler(listItem_knowledge_list, knowledge.getItemList(), itemView).getRecycler().setOnTouchListener((v, event) -> itemView.onTouchEvent(event));
//                            new CustomRecycler<Knowledge.Item>(this, listItem_knowledge_list)
//                                    .setObjectList(knowledge.getItemList())
//                                    .setItemLayout(R.layout.list_item_knowledge_list_text)
//                                    .setSetItemContent((customRecycler2, itemView1, item) -> ((TextView) itemView1.findViewById(R.id.listItem_knowledgeList_text)).setText(item.getName()))
//                                    .generateRecyclerView().setOnTouchListener((v, event) -> itemView.onTouchEvent(event));
                        } else {
                            listItem_knowledge_content.setVisibility(View.VISIBLE);
                            listItem_knowledge_list.setVisibility(View.GONE);
                            listItem_knowledge_content.setText(applyFormatting_text(knowledge.itemListToString()));
                        }
                    } else {
                        listItem_knowledge_content.setVisibility(View.VISIBLE);
                        listItem_knowledge_list.setVisibility(View.GONE);
                        listItem_knowledge_content.setText("");
                    }
                    listItem_knowledge_content.setSingleLine(!expanded);


                    ((TextView) itemView.findViewById(R.id.listItem_knowledge_categories)).setText(String.join(", ",
                            knowledge.getCategoryIdList().stream().map(categoryId -> database.knowledgeCategoryMap.get(categoryId).getName()).collect(Collectors.toList())));
                    itemView.findViewById(R.id.listItem_knowledge_categories).setSelected(true);

                    if (knowledge.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_knowledge_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_knowledge_rating)).setText(String.valueOf(knowledge.getRating()));
                    } else
                        itemView.findViewById(R.id.listItem_knowledge_rating_layout).setVisibility(View.GONE);


                }))
                .addSubOnClickListener(R.id.listItem_knowledge_details, (customRecycler, view, knowledgeExpandable, index) -> detailDialog = showDetailDialog(knowledgeExpandable.getObject()), false)
                .setOnLongClickListener((customRecycler, view, knowledgeExpandable, index) -> {
                    showEditOrNewDialog(knowledgeExpandable.getObject());
                })
                .enableFastScroll(knowledgeExpandable -> knowledgeExpandable.getHeight(297), (expandableCustomRecycler, knowledgeExpandable, integer) -> {
                    switch (sort_type) {
                        case NAME:
                            return knowledgeExpandable.getObject().getName().substring(0, 1);
                        case RATING:
                            Float rating = knowledgeExpandable.getObject().getRating();
                            if (rating > 0)
                                return rating + " ☆";
                            else
                                return "Keine Bewertung";
                        case LATEST:
                            return dateFormat.format(knowledgeExpandable.getObject().getLastChanged());
                        default:
                            return null;
                    }
                })
                .setPadding(16)
                .generate();
    }

    private List<CustomRecycler.Expandable<Knowledge>> toExpandableList(List<Knowledge> knowledgeList) {
        return new CustomRecycler.Expandable.ToExpandableList<Knowledge, Knowledge>().keepExpandedState(customRecycler_List).runToExpandableList(knowledgeList, null);
    }

    private List<Knowledge> filterList(ArrayList<Knowledge> allKnowledgeList) {
        CustomList<Knowledge> customList = new CustomList<>(allKnowledgeList);

        if (!searchQuery.isEmpty()) {
            advancedQueryHelper.filterFull(customList);
        }
        return sortList(customList);
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
    }

    private void reLoadRecycler() {
        customRecycler_List.reload();
    }

    private Pair<CustomDialog, Knowledge> showEditOrNewDialog(Knowledge knowledge) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);
        removeFocusFromSearch();

        final Knowledge[] editKnowledge = {null};
        List<String> categoriesNames = new ArrayList<>();
        List<String> sourcesNames = new ArrayList<>();
        if (knowledge != null) {
            editKnowledge[0] = knowledge.clone();
            editKnowledge[0].getCategoryIdList().forEach(uuid -> categoriesNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
            editKnowledge[0].getSources().forEach(nameUrlPair -> sourcesNames.add(nameUrlPair.get(0)));
        }
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(knowledge == null ? "Neues Wissen" : "Wissen Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_knowledge)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL);

        if (knowledge != null) {
            returnDialog
                    .addButton(R.drawable.ic_delete, customDialog -> {
                        CustomDialog.Builder(this)
                                .setTitle("Löschen")
                                .setText("Willst du wirklich '" + knowledge.getName() + "' löschen?")
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                    database.knowledgeMap.remove(knowledge.getUuid());
                                    allKnowledgeList.remove(knowledge);
                                    Database.saveAll();
                                    reLoadRecycler();
                                    if (detailDialog != null) {
                                        Object payload = detailDialog.getPayload();
                                        if (payload != null) {
                                            ((CustomDialog) payload).dismiss();
                                        }
                                        detailDialog.dismiss();
                                    }
                                    customDialog.dismiss();
                                    Toast.makeText(this, "Wissen gelöscht", Toast.LENGTH_SHORT).show();
                                })
                                .show();
                    }, false)
                    .alignPreviousButtonsLeft();
        }

        returnDialog
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String titel = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddKnowledge_Titel)).getText().toString().trim();
                    if (titel.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String content = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddKnowledge_content)).getText().toString().trim();
//
                    if (content.equals("") && !editKnowledge[0].hasItems()) {
                        CustomDialog.Builder(this)
                                .setTitle("Ohne Inhalt speichern?")
                                .setText("Möchtest du wirklich ohne einen Inhalt speichern")
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 ->
                                        saveKnowledge(customDialog, titel, content, editKnowledge[0], knowledge))
                                .show();
                    } else
                        saveKnowledge(customDialog, titel, content, editKnowledge[0], knowledge);

                }, false)
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    CustomRecycler.CustomRecyclerInterface<Knowledge.Item> recyclerInterface = customRecycler -> {
                        RecyclerView recycler = customRecycler.getRecycler();
                        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) recycler.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
                        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(Utility.dpToPx(280), View.MeasureSpec.AT_MOST);
                        recycler.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
                        if (recycler.getHeight() > Utility.dpToPx(280))
                            customRecycler.getRecycler().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utility.dpToPx(280)));
                        else if (recycler.getMeasuredHeight() < Utility.dpToPx(280))
                            customRecycler.getRecycler().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    };
                    CustomRecycler<Knowledge.Item> contentRecycler = new CustomRecycler<Knowledge.Item>(this, view.findViewById(R.id.dialog_editOrAddKnowledge_list))
                            .setOnReload(recyclerInterface)
                            .setOnGenerate(recyclerInterface);

//                    contentRecycler.getRecycler().setNestedScrollingEnabled(false);
//                    contentRecycler.getRecycler().stopScroll();
//                    final boolean[] enabled = {true};
//                    contentRecycler.getRecycler().addOnScrollListener(new RecyclerView.OnScrollListener() {
//                        @Override
//                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                            if (enabled[0])
//                                super.onScrollStateChanged(recyclerView, newState);
//                            else
//                                recyclerView.stopScroll();
//                        }
//
//                        @Override
//                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                            if (enabled[0])
//                                super.onScrolled(recyclerView, dx, dy);
//                            else
//                                recyclerView.stopScroll();
//                        }
//                    });

                    // -------------------------------------

                    Helpers.RatingHelper ratingHelper = new Helpers.RatingHelper(view.findViewById(R.id.customRating_layout));

                    new Helpers.TextInputHelper().defaultDialogValidation(customDialog).addValidator(view.findViewById(R.id.dialog_editOrAddKnowledge_Titel_layout));
                    EditText dialog_editOrAddKnowledge_content = view.findViewById(R.id.dialog_editOrAddKnowledge_content);
                    dialog_editOrAddKnowledge_content.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            applyFormatting_edit(s);
                        }
                    });
                    if (editKnowledge[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAddKnowledge_Titel)).setText(editKnowledge[0].getName());
                        if (Utility.stringExists(editKnowledge[0].getContent())) {
                            dialog_editOrAddKnowledge_content.setText(editKnowledge[0].getContent());
                            applyFormatting_edit(dialog_editOrAddKnowledge_content.getText());
                        }
                        ((TextView) view.findViewById(R.id.dialog_editOrAddKnowledge_categories)).setText(String.join(", ", categoriesNames));
                        view.findViewById(R.id.dialog_editOrAddKnowledge_categories).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddKnowledge_sources)).setText(String.join(", ", sourcesNames));
                        view.findViewById(R.id.dialog_editOrAddKnowledge_sources).setSelected(true);
                        ratingHelper.setRating(editKnowledge[0].getRating());
                    } else
                        editKnowledge[0] = new Knowledge("");

                    generateItemRecycler(editKnowledge[0].getItemList(), contentRecycler, null, contentRecycler, 0, new CustomList[]{null}, new CustomList<>());


                    View layoutContent = view.findViewById(R.id.dialog_editOrAddKnowledge_content_layout);
                    View layoutList = view.findViewById(R.id.dialog_editOrAddKnowledge_list_layout);
                    if (!editKnowledge[0].hasItems()) {
                        layoutContent.setVisibility(View.VISIBLE);
                        layoutList.setVisibility(View.GONE);
                    }
                    view.findViewById(R.id.dialog_editOrAddKnowledge_content_label).setOnClickListener(view1 -> {
                        Runnable change = () -> {
                            layoutContent.setVisibility(View.GONE);
                            Utility.ignoreNull(() -> contentRecycler.getRecycler().getChildAt(0).findViewById(R.id.listItem_knowledgeList_text).requestFocus());
                            layoutList.setVisibility(View.VISIBLE);
                        };


                        final String[] content = {dialog_editOrAddKnowledge_content.getText().toString().trim()};
                        if (!content[0].isEmpty()) {
                            CustomDialog.Builder(this)
                                    .setTitle("Inhalt Vorhanden")
                                    .setText("Das Wissen hat bereits einen Inhalt.\nSoll versucht werden diesen zu übernehmen, oder soll er verworfen werden?")
                                    .addButton(CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                                    .addButton("Verwerfen", customDialog1 -> {
                                        dialog_editOrAddKnowledge_content.setText("");
                                        editKnowledge[0].setContent(null);
                                        change.run();
                                    })
                                    .addButton("Übernehmen", customDialog1 -> {
                                        dialog_editOrAddKnowledge_content.setText("");
                                        editKnowledge[0].setContent(null);
                                        List<Knowledge.Item> itemList = editKnowledge[0].getItemList();
                                        itemList.clear();
                                        if (content[0].startsWith("• "))
                                            content[0] = content[0].substring(2);
                                        for (String s : content[0].split("• "))
                                            itemList.add(new Knowledge.Item(s.trim()));
                                        contentRecycler.reload();
                                        change.run();

                                    })
                                    .colorLastAddedButton()
                                    .show();
                        } else
                            change.run();

                    });
                    view.findViewById(R.id.dialog_editOrAddKnowledge_list_label).setOnClickListener(view1 -> {
                        Runnable change = () -> {
                            layoutList.setVisibility(View.GONE);
                            dialog_editOrAddKnowledge_content.requestFocus();
                            layoutContent.setVisibility(View.VISIBLE);
                        };

                        if (editKnowledge[0].hasItems()) {
                            CustomDialog.Builder(this)
                                    .setTitle("Inhalt Vorhanden")
                                    .setText("Das Wissen hat bereits einen Inhalt.\nSoll versucht werden diesen zu übernehmen, oder soll er verworfen werden?")
                                    .addButton(CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                                    .addButton("Verwerfen", customDialog1 -> {
                                        editKnowledge[0].clearItemList();
                                        contentRecycler.reload();
                                        change.run();
                                    })
                                    .addButton("Übernehmen", customDialog1 -> {
                                        String content = editKnowledge[0].itemListToString();
                                        dialog_editOrAddKnowledge_content.setText(content);
                                        editKnowledge[0].setContent(content);
                                        editKnowledge[0].clearItemList();
                                        contentRecycler.reload();
                                        change.run();
                                    })
                                    .colorLastAddedButton()
                                    .show();
                        } else
                            change.run();
                    });

                    view.findViewById(R.id.dialog_editOrAddKnowledge_editCategories).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, editKnowledge[0].getCategoryIdList(), CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, (customDialog1, selectedIds) -> {
                                editKnowledge[0].setCategoryIdList(selectedIds);
                                ((TextView) customDialog.findViewById(R.id.dialog_editOrAddJoke_categories)).setText(
                                        CategoriesActivity.joinCategoriesIds(selectedIds, CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES));

                            }));
                    view.findViewById(R.id.dialog_editOrAddKnowledge_editSources).setOnClickListener(view1 ->
                            showSourcesDialog(editKnowledge[0], view.findViewById(R.id.dialog_editOrAddKnowledge_sources), true));
                })
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String title = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddKnowledge_Titel)).getText().toString().trim();
                    String content = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddKnowledge_content)).getText().toString().trim();
                    float rating = ((RatingBar) customDialog.findViewById(R.id.customRating_ratingBar)).getRating();
                    if (knowledge == null)
                        return !title.isEmpty() || !Utility.boolOr(rating, -1f, 0f) || !content.isEmpty() || editKnowledge[0].hasItems() || !editKnowledge[0].getSources().isEmpty();
                    else
                        return !title.equals(knowledge.getName()) || !content.equals(knowledge.getContent()) || rating != knowledge.getRating() || !editKnowledge[0].equals(knowledge);
                })
                .show();
        return Pair.create(returnDialog, editKnowledge[0]);
    }

    private void generateItemRecycler(List<Knowledge.Item> itemList, CustomRecycler<Knowledge.Item> itemRecycler, CustomRecycler<Knowledge.Item> parentRecycler,
                                      CustomRecycler<Knowledge.Item> baseRecycler, int depth, CustomList[] focusIndex, CustomList<Integer> posList) {
        itemRecycler
                .setItemLayout(R.layout.list_item_knowledge_list)
                .enableDragAndDrop(R.id.listItem_knowledgeList_marker, (customRecycler, objectList) -> {
                    itemList.clear();
                    itemList.addAll(objectList);
//                    itemRecycler.reload();
                }, true)
                .setSetItemContent((customRecycler, itemView, item, index) -> {
                    CustomList<Integer> thisPosList = new CustomList<>(posList).add(new Integer[]{index});

//                    ((TextView) itemView.findViewById(R.id.listItem_knowledgeList_marker)).setText(thisPosList.stream().map(String::valueOf).collect(Collectors.joining("-")));

                    EditText listItem_knowledgeList_text = itemView.findViewById(R.id.listItem_knowledgeList_text);
                    Utility.removeTextListeners(listItem_knowledgeList_text);
                    listItem_knowledgeList_text.setText(item.getName());
                    applyFormatting_edit(listItem_knowledgeList_text.getText());
                    final View listItem_knowledgeList_remove = itemView.findViewById(R.id.listItem_knowledgeList_remove);
                    listItem_knowledgeList_remove.setVisibility(item.getName().trim().isEmpty() && (index != 0 || depth != 0) ? View.VISIBLE : View.GONE);
                    listItem_knowledgeList_remove.setOnClickListener(v -> {
                        Runnable onDelete = () -> {
                            if (index == 0) {
                                thisPosList.removeLast();
                                focusIndex[0] = thisPosList;
                                itemList.remove(item);
                                parentRecycler.reload();
                            } else {
                                thisPosList.add(thisPosList.removeLast() - 1);
                                focusIndex[0] = thisPosList;
                                itemList.remove(item);
                                customRecycler.reload();
                            }
                        };

                        if (item.hasChild_real()) {
                            CustomDialog.Builder(this)
                                    .setTitle("Stichpunkt löschen")
                                    .setText("Der Stichpunkt besitzt selber noch Unter-Stichpunkte\nWillst du trotzdem löschen?")
                                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                    .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customRecycler1 -> onDelete.run())
                                    .show();
                        } else
                            onDelete.run();


                    });

                    if (focusIndex[0] != null) {
                        if (focusIndex[0].equals(thisPosList)) {
                            listItem_knowledgeList_text.requestFocus();
                            listItem_knowledgeList_text.setSelection(listItem_knowledgeList_text.getText().toString().length());
                            focusIndex[0] = null;
                        }
                    }
                    final boolean[] add = {false};
                    listItem_knowledgeList_text.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            add[0] = before == 0;
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            customRecycler.getObjectList().get(index).setName(s.toString());
                            listItem_knowledgeList_remove.setVisibility(item.getName().trim().isEmpty() && (index != 0 || depth != 0) ? View.VISIBLE : View.GONE);

                            RecyclerView baseRecyclerView = baseRecycler.getRecycler();
//                            RecyclerView baseRecyclerView = itemRecycler.getRecycler();
                            int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) baseRecyclerView.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
                            int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(Utility.dpToPx(280), View.MeasureSpec.AT_MOST);
                            baseRecyclerView.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
                            if (baseRecyclerView.getHeight() > Utility.dpToPx(280)) {
                                if (baseRecyclerView.getId() == R.id.dialog_editOrAddKnowledge_list)
                                    baseRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utility.dpToPx(280)));
                                else if (baseRecyclerView.getId() == R.id.listItem_knowledgeList_recycler)
                                    baseRecyclerView.setLayoutParams(new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utility.dpToPx(280)));
                            } else if (baseRecyclerView.getMeasuredHeight() < Utility.dpToPx(280)) {
                                if (baseRecyclerView.getId() == R.id.dialog_editOrAddKnowledge_list)
                                    baseRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                else if (baseRecyclerView.getId() == R.id.listItem_knowledgeList_recycler)
                                    baseRecyclerView.setLayoutParams(new TableRow.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            }


                            if (add[0] && s.toString().startsWith("   ") && !s.toString().endsWith("\n\n")) {
                                Knowledge.Item previousItem = Knowledge.previousItem(itemList, item);

                                if (previousItem == null)
                                    return;

                                itemList.remove(item);
                                previousItem.addChild(item);

                                focusIndex[0] = thisPosList.add(new Integer[]{thisPosList.removeLast() - 1}).add(new Integer[]{0});

                                customRecycler.reload();

                                return;
                            }
                            if (add[0] && s.toString().endsWith("\n\n")) {
                                focusIndex[0] = thisPosList.add(new Integer[]{thisPosList.removeLast() + 1});

                                Knowledge.newItem(itemList, item);
                                item.setName(item.getName().trim());
                                customRecycler.reload();

                                return;
                            }

                            applyFormatting_edit(s);

                        }
                    });

                    RecyclerView listItem_knowledgeList_recycler = itemView.findViewById(R.id.listItem_knowledgeList_recycler);
                    TableRow listItem_knowledgeList_recycler_row = itemView.findViewById(R.id.listItem_knowledgeList_recycler_row);
                    if (item.hasChild()) {
                        listItem_knowledgeList_recycler_row.setVisibility(View.VISIBLE);

                        generateItemRecycler(item.getChildrenList(), new CustomRecycler<>(this, listItem_knowledgeList_recycler), itemRecycler, baseRecycler,
                                depth + 1, focusIndex, thisPosList);
                    } else
                        listItem_knowledgeList_recycler_row.setVisibility(View.GONE);
                })
                .setGetActiveObjectList(customRecycler -> itemList)
                .generate();
    }

    private CustomDialog showDetailDialog(Knowledge knowledge) {
        setResult(RESULT_OK);
        removeFocusFromSearch();
//        List<String> categoriesNames = new ArrayList<>();
//        knowledge.getCategoryIdList().forEach(uuid -> categoriesNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(knowledge.getName())
                .setView(R.layout.dialog_detail_knowledge)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Teilen", customDialog -> {
                    CustomDialog.Builder(this)
                            .setTitle("Teilen")
                            .setView(R.layout.dialog_share_knowledge)
                            .setSetViewContent((customDialog1, view, reload) -> {
                                if (!knowledge.getName().isEmpty()) {
                                    ((EditText) view.findViewById(R.id.dialog_shareKnowledge_title)).setText(knowledge.getName());
                                    ((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_title_check)).setChecked(true);
                                }

                                EditText dialog_shareKnowledge_content = view.findViewById(R.id.dialog_shareKnowledge_content);
                                if (!knowledge.getContent().isEmpty()) {
                                    dialog_shareKnowledge_content.setText(knowledge.getContent());
                                    ((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_content_check)).setChecked(true);
                                } else if (knowledge.hasItems()) {
                                    dialog_shareKnowledge_content.setText(knowledge.itemListToString_complete(false));
                                    ((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_content_check)).setChecked(true);
                                }
                                applyFormatting_edit(dialog_shareKnowledge_content.getText());
                                dialog_shareKnowledge_content.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        applyFormatting_edit(s);
                                    }
                                });

                                if (!knowledge.getCategoryIdList().isEmpty()) {
                                    ((TextView) view.findViewById(R.id.dialog_shareKnowledge_categories)).setText(knowledge.getCategoryIdList().stream()
                                            .map(uuid -> database.knowledgeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                                    ((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_categories_check)).setChecked(true);
                                }
                                if (!knowledge.getSources().isEmpty()) {
                                    ((TextView) view.findViewById(R.id.dialog_shareKnowledge_sources)).setText(knowledge.getSources().stream()
                                            .map(nameUrlPair -> nameUrlPair.get(0)).collect(Collectors.joining(", ")));
                                    ((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_sources_check)).setChecked(true);
                                }

                                ((Switch) customDialog1.findViewById(R.id.dialog_shareKnowledge_format)).setOnCheckedChangeListener((buttonView, isChecked) ->
                                        customDialog1.findViewById(R.id.dialog_shareKnowledge_keepFormat).setEnabled(isChecked));
                            })
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                boolean format = ((Switch) customDialog1.findViewById(R.id.dialog_shareKnowledge_format)).isChecked();
                                List<String> textList = new ArrayList<>();
                                if (((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_title_check)).isChecked())
                                    textList.add(String.format((format ? "*Titel*:\n%s" : "Titel:\n%s"), ((EditText) customDialog1.findViewById(R.id.dialog_shareKnowledge_title)).getText()));
                                if (((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_content_check)).isChecked()) {
                                    String content = ((EditText) customDialog1.findViewById(R.id.dialog_shareKnowledge_content)).getText().toString();
                                    if (!format || !((Switch) customDialog1.findViewById(R.id.dialog_shareKnowledge_keepFormat)).isChecked()) {
                                        Pattern pattern = Pattern.compile("(?<![^\\W_])(\\*|\\/|\\_|\\^|\\~)([^\\s]|[^\\s].*?[^\\s])\\1(?![^\\W_])");
                                        while (true) {
                                            Matcher matcher = pattern.matcher(content);
                                            if (matcher.find()) {
                                                String match = matcher.group(0);
                                                content = matcher.replaceFirst(Utility.subString(match, 1, -1));
                                            } else
                                                break;
                                        }
                                    } else {
                                        Pattern pattern = Pattern.compile("(?<![^\\W_])(\\*|\\/|\\_|\\^|\\~)([^\\s]|[^\\s].*?[^\\s])\\1(?![^\\W_])");
                                        int progress = 0;
                                        while (true) {
                                            Matcher matcher = pattern.matcher(content);
                                            if (matcher.find(progress)) {
                                                String match = matcher.group(0);
                                                MatchResult matchResult = matcher.toMatchResult();

                                                if (!String.valueOf(content.charAt(matchResult.start() - 1)).matches("\\s|_") || !String.valueOf(content.charAt(matchResult.end() + 1)).matches("\\W|_")) {
                                                    content = Utility.stringReplace(content, matchResult.start(), matchResult.end(), Utility.subString(match, 1, -1));
                                                    progress = matchResult.end() - 1;
                                                    continue;
                                                }


                                                switch (match.substring(0, 1)) {
                                                    case "^":
                                                        content = Utility.stringReplace(content, matchResult.start(), matchResult.end(), "*" + Utility.subString(match, 1, -1) + "*");
                                                        progress = matchResult.end() + 1;
                                                        break;
                                                    case "/":
                                                        content = Utility.stringReplace(content, matchResult.start(), matchResult.end(), "_" + Utility.subString(match, 1, -1) + "_");
                                                        progress = matchResult.end() + 1;
                                                        break;
                                                    default:
                                                        progress = matchResult.end() + 1;
                                                        break;
                                                    case "_":
                                                        content = Utility.stringReplace(content, matchResult.start(), matchResult.end(), "```" + Utility.subString(match, 1, -1) + "```");
                                                        progress = matchResult.end() + 5;
                                                        break;
                                                }

                                            } else
                                                break;
                                        }

                                    }
                                    textList.add(String.format((format ? "*Inhalt*:\n%s" : "Inhalt:\n%s"), content));
                                }
                                if (((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_categories_check)).isChecked())
                                    textList.add(String.format((format ? "*Kategorien*:\n%s" : "Kategorien:\n%s"), knowledge.getCategoryIdList().stream()
                                            .map(uuid -> database.knowledgeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", "))));
                                if (((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_sources_check)).isChecked())
                                    textList.add(String.format((format ? "*Quellen*:\n%s" : "Quellen:\n%s"), knowledge.getSources().stream()
                                            .map(nameUrlPair -> String.format((format ? "_*%s*_:\n%s" : "%s:\n%s"), nameUrlPair.get(0), nameUrlPair.get(1))).collect(Collectors.joining("\n"))));
                                Utility.sendText(this, String.join("\n\n", textList));
                            })
                            .show();
                }, false)
                .addButton("Bearbeiten", customDialog ->
                        showEditOrNewDialog(knowledge), false)
                .setSetViewContent((customDialog, view, reload) -> {
                    if (reload) customDialog.setTitle(knowledge.getName());

                    TextView dialog_detailKnowledge_content = view.findViewById(R.id.dialog_detailKnowledge_content);
                    RecyclerView dialog_detailKnowledge_list = view.findViewById(R.id.dialog_detailKnowledge_list);
                    if (knowledge.hasContent()) {
                        dialog_detailKnowledge_content.setVisibility(View.VISIBLE);
                        dialog_detailKnowledge_list.setVisibility(View.GONE);
                        dialog_detailKnowledge_content.setText(applyFormatting_text(knowledge.getContent()));
                    } else if (knowledge.hasItems()) {
                        dialog_detailKnowledge_list.setVisibility(View.VISIBLE);
                        dialog_detailKnowledge_content.setVisibility(View.GONE);
                        List<Knowledge.Item> itemList = knowledge.getItemList();
                        generateItemTextRecycler(dialog_detailKnowledge_list, itemList, null);
                    } else {
                        dialog_detailKnowledge_content.setVisibility(View.VISIBLE);
                        dialog_detailKnowledge_list.setVisibility(View.GONE);
                        dialog_detailKnowledge_content.setText("");
                    }

                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES, view.findViewById(R.id.dialog_detailKnowledge_categories), knowledge.getCategoryIdList());
//                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_categories)).setText(String.join(", ", categoriesNames));
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_sources)).setText(knowledge.getSources().stream().map(strings -> strings.get(0)).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_lastChanged)).setText(String.format("%s Uhr", new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(knowledge.getLastChanged())));
                    view.findViewById(R.id.dialog_detailKnowledge_details).setVisibility(View.VISIBLE);
                    ((RatingBar) view.findViewById(R.id.dialog_detailKnowledge_rating)).setRating(knowledge.getRating());

                    view.findViewById(R.id.dialog_detailKnowledge_listSources).setOnClickListener(v -> {
                        showSourcesDialog(knowledge, view.findViewById(R.id.dialog_detailKnowledge_sources), false);
                    });
                })
                .setOnDialogDismiss(customDialog -> {
                    detailDialog = null;
                    Utility.ifNotNull(customDialog.getPayload(), o -> ((com.finn.androidUtilities.CustomDialog) o).reloadView());
                });
        returnDialog.show();
        return returnDialog;
    }

    private CustomRecycler<Knowledge.Item> generateItemTextRecycler(RecyclerView recycler, List<Knowledge.Item> itemList, View targetView) {
        return new CustomRecycler<Knowledge.Item>(this, recycler)
                .setObjectList(itemList)
                .hideOverscroll()
                .setItemLayout(R.layout.list_item_knowledge_list_text)
                .setSetItemContent((customRecycler2, itemView1, item, index) -> {

                    ((TextView) itemView1.findViewById(R.id.listItem_knowledgeList_text)).setText(applyFormatting_text(item.getName()));

                    RecyclerView childRecycler = itemView1.findViewById(R.id.listItem_knowledgeList_list);
                    TableRow listItem_knowledgeList_listRow = itemView1.findViewById(R.id.listItem_knowledgeList_listRow);
                    if (item.hasChild_real()) {
                        listItem_knowledgeList_listRow.setVisibility(View.VISIBLE);
                        CustomRecycler<Knowledge.Item> recycler1 = generateItemTextRecycler(childRecycler, item.getChildrenList(), targetView);
                        if (targetView != null) {
                            recycler1.getRecycler().setOnTouchListener((v, event) -> targetView.onTouchEvent(event));
                        }
                    } else
                        listItem_knowledgeList_listRow.setVisibility(View.GONE);
                })
                .generate();
    }

    public static SpannableString applyFormatting_text(CharSequence s) {
        // ^ Überschrift; * fett;  ~ durch;  / kursiv; _ unterstrichen; # monospace
        List<Pair<Integer, Integer>> captionMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> boldMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> strikeMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> italicMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> underlineMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> monospaceMatches = new ArrayList<>();

//        Pattern pattern = Pattern.compile("((?<=[\\s]|)\\^([^\\s].*?[^\\s]|[^\\s])\\^(?![^\\s\\.!\\?,:;]))|((?<=[\\s]|)\\*([^\\s].*?[^\\s]|[^\\s])\\*(?![^\\s\\.!\\?,:;]))|((?<=[\\s]|)\\~([^\\s].*?[^\\s]|[^\\s])\\~(?![^\\s\\.!\\?,:;]))|((?<=[\\s]|)\\/([^\\s].*?[^\\s]|[^\\s])\\/(?![^\\s\\.!\\?,:;]))|((?<=[\\s]|)\\_([^\\s].*?[^\\s]|[^\\s])\\_(?![^\\s\\.!\\?,:;]))");
        Pattern pattern = Pattern.compile("(?<![^\\W_])(\\*|\\/|\\_|\\^|\\~|#)([^\\s]|[^\\s].*?[^\\s])\\1(?![^\\W_])");
        while (true) {
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                MatchResult matchResult = matcher.toMatchResult();
                String match = matcher.group(0);
                switch (match.substring(0, 1)) {
                    case "^":
                        captionMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
                        break;
                    case "*":
                        boldMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
                        break;
                    case "~":
                        strikeMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
                        break;
                    case "/":
                        italicMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
                        break;
                    case "_":
                        underlineMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
                        break;
                    case "#":
                        monospaceMatches.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
                        break;
                }
                s = matcher.replaceFirst(Utility.subString(match, 1, -1));
            } else
                break;
        }


//        s = findAndReplace("^", s, captionMatches);
//        s = findAndReplace("*", s, boldMatches);
//        s = findAndReplace("~", s, strikeMatches);
//        s = findAndReplace("/", s, italicMatches);
//        s = findAndReplace("_", s, underlineMatches);

        SpannableString resultSpan = new SpannableString(s);

        captionMatches.forEach(pair -> resultSpan.setSpan(new StyleSpan(Typeface.BOLD), pair.first, pair.second, Spannable.SPAN_COMPOSING));
        captionMatches.forEach(pair -> resultSpan.setSpan(new UnderlineSpan(), pair.first, pair.second, Spannable.SPAN_COMPOSING));

        boldMatches.forEach(pair -> resultSpan.setSpan(new StyleSpan(Typeface.BOLD), pair.first, pair.second, Spannable.SPAN_COMPOSING));

        strikeMatches.forEach(pair -> resultSpan.setSpan(new StrikethroughSpan(), pair.first, pair.second, Spannable.SPAN_COMPOSING));

        italicMatches.forEach(pair -> resultSpan.setSpan(new StyleSpan(Typeface.ITALIC), pair.first, pair.second, Spannable.SPAN_COMPOSING));

        underlineMatches.forEach(pair -> resultSpan.setSpan(new UnderlineSpan(), pair.first, pair.second, Spannable.SPAN_COMPOSING));

        monospaceMatches.forEach(pair -> resultSpan.setSpan(new TypefaceSpan("monospace"), pair.first, pair.second, Spannable.SPAN_COMPOSING));

        return resultSpan;
    }

    private void applyFormatting_edit(Editable s) {
        // ^ Überschrift; * fett;  ~ durch;  / kursiv; _ unterstrichen
        new CustomList<>(s.getSpans(0, s.length(), StyleSpan.class)).forEach(s::removeSpan);
        new CustomList<>(s.getSpans(0, s.length(), UnderlineSpan.class)).forEach(s::removeSpan);
        new CustomList<>(s.getSpans(0, s.length(), StrikethroughSpan.class)).forEach(s::removeSpan);
        new CustomList<>(s.getSpans(0, s.length(), ForegroundColorSpan.class)).forEach(s::removeSpan);

        List<Pair<Integer, Integer>> captionMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> boldMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> strikeMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> italicMatches = new ArrayList<>();
        List<Pair<Integer, Integer>> underlineMatches = new ArrayList<>();

        Pattern pattern = Pattern.compile("(?<![^\\W_])(\\*|\\/|\\_|\\^|\\~)([^\\s]|[^\\s].*?[^\\s])\\1(?![^\\W_])");
        Matcher matcher = pattern.matcher(s);
        while (true) {
            if (matcher.find()) {
                MatchResult matchResult = matcher.toMatchResult();
                String match = matcher.group(0);
                switch (match.substring(0, 1)) {
                    case "^":
                        captionMatches.add(new Pair<>(matchResult.start() + 1, matchResult.end() - 1));
                        break;
                    case "*":
                        boldMatches.add(new Pair<>(matchResult.start() + 1, matchResult.end() - 1));
                        break;
                    case "~":
                        strikeMatches.add(new Pair<>(matchResult.start() + 1, matchResult.end() - 1));
                        break;
                    case "/":
                        italicMatches.add(new Pair<>(matchResult.start() + 1, matchResult.end() - 1));
                        break;
                    case "_":
                        underlineMatches.add(new Pair<>(matchResult.start() + 1, matchResult.end() - 1));
                        break;
                }
            } else
                break;
        }


        captionMatches.forEach(pair -> s.setSpan(new StyleSpan(Typeface.BOLD), pair.first, pair.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));
        captionMatches.forEach(pair -> s.setSpan(new UnderlineSpan(), pair.first, pair.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        boldMatches.forEach(pair -> s.setSpan(new StyleSpan(Typeface.BOLD), pair.first, pair.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        strikeMatches.forEach(pair -> s.setSpan(new StrikethroughSpan(), pair.first, pair.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        italicMatches.forEach(pair -> s.setSpan(new StyleSpan(Typeface.ITALIC), pair.first, pair.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        underlineMatches.forEach(pair -> s.setSpan(new UnderlineSpan(), pair.first, pair.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE));

        int color = Utility.setAlphaOfColor(getColor(R.color.colorText), 0x80);
        Utility.concatenateCollections(captionMatches, boldMatches, strikeMatches, italicMatches, underlineMatches).forEach(pair -> {
            s.setSpan(new ForegroundColorSpan(color), pair.first - 1, pair.first, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new ForegroundColorSpan(color), pair.second, pair.second + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        });
    }

//    @NotNull
//    private String findAndReplace(String delimiter, String s, List<Pair<Integer, Integer>> resultList) {
//        while (s.contains(delimiter)) {
//            Pattern pattern = Pattern.compile(String.format("(?<![^\\s])\\%s([^\\s].*?[^\\s]|[^\\s])\\%s(?![^\\s\\.!\\?,:;])", delimiter, delimiter));
//            Matcher matcher = pattern.matcher(s);
//            if (matcher.find()) {
//                MatchResult matchResult = matcher.toMatchResult();
//                resultList.add(new Pair<>(matchResult.start(), matchResult.end() - 2));
//                String match = matcher.group(0);
//                s = matcher.replaceFirst(Utility.subString(match,1, -1));
//            }
//            else
//                break;
//        }
//        return s;
//    }

    private void saveKnowledge(CustomDialog dialog, String titel, String content, Knowledge newKnowledge, Knowledge knowledge) {

        if (knowledge == null)
            knowledge = newKnowledge;


        knowledge.setName(titel);
        knowledge.setContent(content);
        knowledge.setCategoryIdList(newKnowledge.getCategoryIdList());
        knowledge.setSources(newKnowledge.getSources());
        knowledge.setRating(((RatingBar) dialog.findViewById(R.id.customRating_ratingBar)).getRating());
        knowledge.setLastChanged(new Date());
        knowledge.setItemList(newKnowledge.getItemList().stream().filter(item -> {
            item.setName(item.getName().trim());
            return !item.getName().isEmpty();
        }).collect(Collectors.toCollection(CustomList::new)).executeIf(ArrayList::isEmpty, customList ->
                customList.add(new Knowledge.Item())));

        if (!database.knowledgeMap.containsValue(knowledge))
            allKnowledgeList.add(knowledge);

        database.knowledgeMap.put(knowledge.getUuid(), knowledge);
        reLoadRecycler();
        dialog.dismiss();

        Database.saveAll();

        Utility.showCenteredToast(this, "Wissen gespeichert");

        if (detailDialog != null)
            detailDialog.reloadView();
    }

    private void showRandomDialog() {
        removeFocusFromSearch();
        CustomList<Knowledge> filteredKnowledgeList = new CustomList<>(filterList(allKnowledgeList));
        if (filteredKnowledgeList.isEmpty()) {
            Toast.makeText(this, "Nichts zum Zeigen", Toast.LENGTH_SHORT).show();
            return;
        }
        final Knowledge[] randomKnowledge = {filteredKnowledgeList.removeRandom()};

        CustomDialog.Builder(this)
                .setTitle(randomKnowledge[0].getName())
                .setView(R.layout.dialog_detail_knowledge)
                .addButton(R.drawable.ic_info, customDialog -> detailDialog = showDetailDialog(randomKnowledge[0]).setPayload(customDialog), false)
                .alignPreviousButtonsLeft()
                .addButton("Nochmal", customDialog -> {
                    if (filteredKnowledgeList.isEmpty()) {
                        Toast.makeText(this, "Wissen erschöpft", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomKnowledge[0] = filteredKnowledgeList.removeRandom();
                    customDialog.reloadView();

                }, false)
                .colorLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {

                    if (reload)
                        customDialog.setTitle(randomKnowledge[0].getName());


                    TextView listItem_knowledge_content = view.findViewById(R.id.dialog_detailKnowledge_content);
                    RecyclerView listItem_knowledge_list = view.findViewById(R.id.dialog_detailKnowledge_list);
                    if (randomKnowledge[0].hasContent()) {
                        listItem_knowledge_content.setVisibility(View.VISIBLE);
                        listItem_knowledge_list.setVisibility(View.GONE);
                        listItem_knowledge_content.setText(applyFormatting_text(randomKnowledge[0].getContent()));
                    } else if (randomKnowledge[0].hasItems()) {
                        listItem_knowledge_list.setVisibility(View.VISIBLE);
                        listItem_knowledge_content.setVisibility(View.GONE);
                        generateItemTextRecycler(listItem_knowledge_list, randomKnowledge[0].getItemList(), null);
                    } else {
                        listItem_knowledge_content.setVisibility(View.VISIBLE);
                        listItem_knowledge_list.setVisibility(View.GONE);
                        listItem_knowledge_content.setText("");
                    }

                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_categories)).setText(
                            randomKnowledge[0].getCategoryIdList().stream().map(uuid -> database.knowledgeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", ")));

                })
                .addOnDialogDismiss(customDialog -> {
                    if (isDialog)
                        finish();
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

        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!Database.isReady())
            return true;

        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_knowledge_add:
                showEditOrNewDialog(null);
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
//                textListener.onQueryTextChange(knowledge_search.getQuery().toString());
                setSearchHint();
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
//                textListener.onQueryTextChange(knowledge_search.getQuery().toString());
                setSearchHint();
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
                setSearchHint();
                break;

            case android.R.id.home:
                if (getCallingActivity() == null)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
                break;

        }
        return true;
    }

    //  ----- Sources ----->
    private CustomDialog showSourcesDialog(Knowledge knowledge, TextView sourcesText, boolean edit) {
        int buttonId_add = View.generateViewId();
        TextValidation nameValidation = (textInputLayout, changeErrorMessage) -> {
            String text = textInputLayout.getEditText().getText().toString().trim();

            if (text.isEmpty()) {
                if (changeErrorMessage)
                    textInputLayout.setError("Das Feld darf nicht leer sein!");
                return false;
            } else {
                if (changeErrorMessage)
                    textInputLayout.setError(null);
                return true;
            }
        };
        TextValidation urlValidation = (textInputLayout, changeErrorMessage) -> {
            String text = textInputLayout.getEditText().getText().toString().trim();

            if (text.isEmpty()) {
                textInputLayout.setError("Das Feld darf nicht leer sein!");
                return false;
            } else if (!text.matches("(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$")) {
                textInputLayout.setError("Eine valide URL eingeben!");
                return false;

            } else {
                textInputLayout.setError(null);
                return true;
            }

        };
        final List<String>[] currentSource = new List[]{null};
        CustomDialog sourcesDialog = new CustomDialog(this)
                .setTitle("Quellen")
                .setView(R.layout.dialog_sources)
                .setSetViewContent((customDialog, view, reload) -> {
                    LinearLayout dialog_sources_editLayout = view.findViewById(R.id.dialog_sources_editLayout);
                    TextInputLayout dialog_sources_name = view.findViewById(R.id.dialog_sources_name);
                    TextInputLayout dialog_sources_url = view.findViewById(R.id.dialog_sources_url);
                    if (edit) {
                        dialog_sources_editLayout.setVisibility(View.VISIBLE);
                        dialog_sources_url.getEditText().requestFocus();
                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
                    }
                    Button dialog_sources_save = view.findViewById(R.id.dialog_sources_save);
                    dialog_sources_name.getEditText().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            validation(nameValidation, urlValidation, dialog_sources_name, dialog_sources_url, true, false, dialog_sources_save);
                        }
                    });
                    dialog_sources_url.getEditText().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (urlValidation.runTextValidation(dialog_sources_url, true)) {
                                if (dialog_sources_name.getEditText().getText().toString().isEmpty()) {
                                    getDomainFromUrl(s.toString(), dialog_sources_name.getEditText(), true);
                                }
                            }
                            validation(nameValidation, urlValidation, dialog_sources_name, dialog_sources_url, false, true, dialog_sources_save);
                        }
                    });
                    Runnable hideEdit = () -> {
                        dialog_sources_name.getEditText().setText("");
                        dialog_sources_url.getEditText().setText("");
                        dialog_sources_name.setError(null);
                        dialog_sources_url.setError(null);
                        dialog_sources_editLayout.setVisibility(View.GONE);
                        Utility.changeWindowKeyboard(this, dialog_sources_url.getEditText(), false);
                        dialog_sources_url.getEditText().clearFocus();
                        customDialog.getDialog().findViewById(buttonId_add).setVisibility(View.VISIBLE);
                        currentSource[0] = null;
                    };


                    view.findViewById(R.id.dialog_sources_cancel).setOnClickListener(v -> hideEdit.run());

                    CustomRecycler sources_customRecycler = new CustomRecycler<List<String>>(this, view.findViewById(R.id.dialog_sources_sources))
                            .setItemLayout(R.layout.list_item_source_or_item)
                            .setGetActiveObjectList(customRecycler -> {
                                List<List<String>> sources = knowledge.getSources();
                                view.findViewById(R.id.dialog_sources_noSources).setVisibility(sources.isEmpty() ? View.VISIBLE : View.GONE);
                                return sources;
                            })
                            .setSetItemContent((customRecycler, itemView, nameUrlPair, index) -> {
                                ((TextView) itemView.findViewById(R.id.listItem_source_name)).setText(nameUrlPair.get(0));
                                ((TextView) itemView.findViewById(R.id.listItem_source_content)).setText(nameUrlPair.get(1));
                            })
                            .setOnClickListener((customRecycler, itemView, o, index) -> Utility.openUrl(this, o.get(1), false))
                            .setOnLongClickListener((customRecycler, view1, stringList, index) -> {
                                Dialog dialog = customDialog.getDialog();
                                dialog.findViewById(R.id.dialog_sources_editLayout).setVisibility(View.VISIBLE);
                                dialog.findViewById(buttonId_add).setVisibility(View.GONE);
                                dialog.findViewById(R.id.dialog_sources_delete).setVisibility(View.VISIBLE);
                                dialog_sources_name.getEditText().setText(stringList.get(0));
                                dialog_sources_url.getEditText().setText(stringList.get(1));
                                currentSource[0] = stringList;
                            })
                            .generate();

                    view.findViewById(R.id.dialog_sources_delete).setOnClickListener(v -> {
                        CustomDialog.Builder(this)
                                .setTitle("Quelle Löschen")
                                .setText("Willst du wirklich die Quelle '" + currentSource[0].get(0) + "' löschen?")
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                                    knowledge.getSources().remove(currentSource[0]);
                                    hideEdit.run();
                                    sources_customRecycler.reload();
                                    Database.saveAll();
                                })
                                .show();
                    });

                    dialog_sources_save.setOnClickListener(v -> {
                        if (!nameValidation.runTextValidation(dialog_sources_name, true) | !urlValidation.runTextValidation(dialog_sources_url, true))
                            return;
                        if (currentSource[0] == null) {
                            List<String> newSource = Arrays.asList(dialog_sources_name.getEditText().getText().toString().trim()
                                    , dialog_sources_url.getEditText().getText().toString().trim());
                            knowledge.getSources().add(newSource);
                        } else {
                            currentSource[0].clear();
                            currentSource[0].add(dialog_sources_name.getEditText().getText().toString().trim());
                            currentSource[0].add(dialog_sources_url.getEditText().getText().toString().trim());
                        }
                        Database.saveAll();
                        sources_customRecycler.reload();
                        hideEdit.run();
                        currentSource[0] = null;
                    });
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Hinzufügen", customDialog -> {
                    customDialog.findViewById(R.id.dialog_sources_editLayout).setVisibility(View.VISIBLE);
                    customDialog.findViewById(buttonId_add).setVisibility(View.GONE);
                    customDialog.findViewById(R.id.dialog_sources_delete).setVisibility(View.GONE);
                    EditText dialog_sources_url = ((TextInputLayout) customDialog.findViewById(R.id.dialog_sources_url)).getEditText();
                    dialog_sources_url.requestFocus();
                    Utility.changeWindowKeyboard(this, dialog_sources_url, true);
                }, buttonId_add, false)
                .addButton("Zurück", customDialog -> {
                })
                .setPayload(sourcesText)
                .disableScroll()
                .setOnDialogDismiss(customDialog -> ((TextView) customDialog.getPayload()).setText(
                        knowledge.getSources().stream().map(strings -> strings.get(0)).collect(Collectors.joining(", "))
                ))
                .show();
        if (edit)
            sourcesDialog.findViewById(buttonId_add).setVisibility(View.GONE);
        return sourcesDialog;
    }

    private boolean validation(TextValidation nameValidation, TextValidation urlValidation, TextInputLayout dialog_sources_name
            , TextInputLayout dialog_sources_url, boolean changeError_name, boolean changeError_url, Button dialog_sources_save) {
        if (!nameValidation.runTextValidation(dialog_sources_name, changeError_name) | !urlValidation.runTextValidation(dialog_sources_url, changeError_url)) {
            dialog_sources_save.setEnabled(false);
            return false;
        } else {
            dialog_sources_save.setEnabled(true);
            return true;
        }
    }

    interface TextValidation {
        boolean runTextValidation(TextInputLayout textInputLayout, boolean changeErrorMessage);
    }

    private String getDomainFromUrl(String url, EditText editText, boolean shortened) {
        final String[] result = {""};
        Runnable onFound = () -> {
            editText.setText(result[0]);
        };

        Pattern pattern = Pattern.compile("(?<=://)[^/]*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String substring = matcher.group(0); //.substring(3);
            if (shortened) {
                String[] split = substring.split("\\.");
                result[0] = split[split.length - 2];
            } else
                result[0] = substring;
        }

        UrlParser urlParser = UrlParser.getMatchingParser(url);
        if (urlParser != null) {
            urlParser.parseUrl(this, url, s -> {
                result[0] = s;
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
                onFound.run();
            }, s -> {
            });
        } else
            onFound.run();
        return result[0];
    }
//  <----- Sources -----

    private void removeFocusFromSearch() {
        knowledge_search.clearFocus();
    }

    private void setSearchHint() {
        String join = filterTypeSet.stream().filter(FILTER_TYPE::hasName).sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).map(FILTER_TYPE::getName).collect(Collectors.joining(", "));
        knowledge_search.setQueryHint(join.isEmpty() ? "Kein Filter ausgewählt!" : join + " ('&' als 'und'; '|' als 'oder')");
        Utility.applyToAllViews(knowledge_search, View.class, view -> view.setEnabled(!join.isEmpty()));
    }

    @Override
    public void onBackPressed() {
        if (advancedQueryHelper.handleBackPress(this))
            return;

        super.onBackPressed();
    }
}
