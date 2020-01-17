package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomRecycler;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.Knowledge;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KnowledgeActivity extends AppCompatActivity {


    enum SORT_TYPE {
        NAME, RATING, LATEST
    }

    public enum FILTER_TYPE {
        NAME, CATEGORY, CONTENT
    }


    private boolean reverse;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    Database database = Database.getInstance();
    private CustomRecycler<? extends CustomRecycler.Expandable<Knowledge>> customRecycler_List;
    private String searchQuery = "";
    private SharedPreferences mySPR_daten;
    private SearchView.OnQueryTextListener textListener;
    private SearchView videos_search;
    private ArrayList<Knowledge> allKnowledgeList;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.CATEGORY, FILTER_TYPE.CONTENT));
    private CustomDialog detailDialog;

    // ToDo: Stichpunkte

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

        CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
        if (extraSearchCategory != null) {
            filterTypeSet.clear();

            switch (extraSearchCategory) {
                case KNOWLEDGE_CATEGORIES:
                    filterTypeSet.add(FILTER_TYPE.CATEGORY);
                    break;
            }

            String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
            if (extraSearch != null) {
                videos_search.setQuery(extraSearch, true);
            }
        }

    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
            setContentView(R.layout.activity_knowledge);
            loadRecycler();

            videos_search = findViewById(R.id.search);
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
            videos_search.setOnQueryTextListener(textListener);

            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_ADD))
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
                            .enableDivider()
                            .disableCustomRipple()
                            .setGetActiveObjectList(customRecycler1 -> knowledgeList.stream().map(ParentClass::getName).collect(Collectors.toList()))
                            .setOnClickListener((customRecycler1, itemView, s, index) -> {
                                Knowledge knowledge = knowledgeList.get(index);
                                Pair<CustomDialog,Knowledge> customDialogKnowledgePair = showEditOrNewDialog(knowledge);
                                CustomDialog sourcesDialog = showSourcesDialog(customDialogKnowledgePair.second, customDialogKnowledgePair.first.findViewById(R.id.dialog_editOrAddKnowledge_sources), true);
                                TextInputLayout dialog_sources_url = sourcesDialog.findViewById(R.id.dialog_sources_url);
                                dialog_sources_url.getEditText().setText(finalText);

                                selectDialog.dismiss();
                            });

                    selectDialog
                            .setTitle("Wissen Auswählen")
                            .addButton("Neu", customDialog -> {
                                Pair<CustomDialog,Knowledge> customDialogKnowledgePair = showEditOrNewDialog(null);
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

//            database.knowledgeMap.values().forEach(knowledge -> knowledge.setItemList(Arrays.asList(new Knowledge.Item())));
//            Database.saveAll();
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
        customRecycler_List = new CustomRecycler<CustomRecycler.Expandable<Knowledge>>(this, findViewById(R.id.recycler))
                .setGetActiveObjectList(customRecycler -> {
                    if (searchQuery.equals("")) {
                        allKnowledgeList = new ArrayList<>(database.knowledgeMap.values());
                        return toExpandableList(sortList(allKnowledgeList));
                    } else
                        return toExpandableList(filterList(allKnowledgeList));
                })

                .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper<Knowledge>(R.layout.list_item_knowledge, (customRecycler1, itemView, knowledge, expanded) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_knowledge_title)).setText(knowledge.getName());

                    TextView listItem_knowledge_content = itemView.findViewById(R.id.listItem_knowledge_content);
                    RecyclerView listItem_knowledge_list = itemView.findViewById(R.id.listItem_knowledge_list);
                    if (knowledge.hasContent()) {
                        listItem_knowledge_content.setVisibility(View.VISIBLE);
                        listItem_knowledge_list.setVisibility(View.GONE);
                        listItem_knowledge_content.setText(knowledge.getContent());
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
                            listItem_knowledge_content.setText(knowledge.itemListToString());
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
                .generate();
    }

    private List<CustomRecycler.Expandable<Knowledge>> toExpandableList(List<Knowledge> jokeList) {
        return jokeList.stream().map(joke -> new CustomRecycler.Expandable<>(joke.getName(), joke)).collect(Collectors.toList());
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
    }

    private void reLoadRecycler() {
        customRecycler_List.reload();
    }

    private Pair<CustomDialog,Knowledge> showEditOrNewDialog(Knowledge knowledge) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);

        final Knowledge[] newKnowledge = {null};
        List<String> categoriesNames = new ArrayList<>();
        List<String> sourcesNames = new ArrayList<>();
        if (knowledge != null) {
            newKnowledge[0] = knowledge.clone();
            newKnowledge[0].getCategoryIdList().forEach(uuid -> categoriesNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
            newKnowledge[0].getSources().forEach(nameUrlPair -> sourcesNames.add(nameUrlPair.get(0)));
        }
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle(knowledge == null ? "Neues Wissen" : "Wissen Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_knowledge)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL);

        if (knowledge != null)
            returnDialog.addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog -> {
                com.finn.androidUtilities.CustomDialog.Builder(this)
                        .setTitle("Löschen")
                        .setText("Willst du wirklich '" + knowledge.getName() + "' löschen?")
                        .setButtonConfiguration(com.finn.androidUtilities.CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                        .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                            database.knowledgeMap.remove(knowledge.getUuid());
                            Database.saveAll();
                            reLoadRecycler();
                            if (detailDialog != null)
                                detailDialog.dismiss();
                            customDialog.dismiss();
                            Toast.makeText(this, "Wissen gelöscht", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            }, false)
                    .alignPreviousButtonsLeft();

        returnDialog
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String titel = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddKnowledge_Titel)).getText().toString().trim();
                    if (titel.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String content = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddKnowledge_content)).getText().toString().trim();
//
                    if (content.equals("") && !newKnowledge[0].hasItems()) {
                        com.finn.androidUtilities.CustomDialog.Builder(this)
                                .setTitle("Ohne Inhalt speichern?")
                                .setText("Möchtest du wirklich ohne einen Inhalt speichern")
                                .setButtonConfiguration(com.finn.androidUtilities.CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 ->
                                        saveKnowledge(customDialog, titel, content, newKnowledge[0], knowledge))
                                .show();
                    } else
                        saveKnowledge(customDialog, titel, content, newKnowledge[0], knowledge);

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

                    // -------------------------------------

                    new Helpers.TextInputHelper().defaultDialogValidation(customDialog).addValidator(view.findViewById(R.id.dialog_editOrAddKnowledge_Titel_layout));
                    EditText dialog_editOrAddKnowledge_content = view.findViewById(R.id.dialog_editOrAddKnowledge_content);
                    if (newKnowledge[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAddKnowledge_Titel)).setText(newKnowledge[0].getName());
                        dialog_editOrAddKnowledge_content.setText(knowledge.getContent());
                        ((TextView) view.findViewById(R.id.dialog_editOrAddKnowledge_categories)).setText(String.join(", ", categoriesNames));
                        view.findViewById(R.id.dialog_editOrAddKnowledge_categories).setSelected(true);
                        ((TextView) view.findViewById(R.id.dialog_editOrAddKnowledge_sources)).setText(String.join(", ", sourcesNames));
                        view.findViewById(R.id.dialog_editOrAddKnowledge_sources).setSelected(true);
                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddKnowledge_rating)).setRating(newKnowledge[0].getRating());
                    } else
                        newKnowledge[0] = new Knowledge("");

                    generateItemRecycler(newKnowledge[0].getItemList(), contentRecycler, null, 0, new CustomList[]{null}, new CustomList<>());


                    View layoutContent = view.findViewById(R.id.dialog_editOrAddKnowledge_content_layout);
                    View layoutList = view.findViewById(R.id.dialog_editOrAddKnowledge_list_layout);
                    if (newKnowledge[0].hasItems()) {
                        layoutContent.setVisibility(View.GONE);
                        layoutList.setVisibility(View.VISIBLE);
                    }
                    view.findViewById(R.id.dialog_editOrAddKnowledge_content_label).setOnClickListener(view1 -> {
                        Runnable change = () -> {
                            layoutContent.setVisibility(View.GONE);
                            layoutList.setVisibility(View.VISIBLE);
                        };

                        final String[] content = {dialog_editOrAddKnowledge_content.getText().toString().trim()};
                        if (!content[0].isEmpty()) {
                            com.finn.androidUtilities.CustomDialog.Builder(this)
                                    .setTitle("Inhalt Vorhanden")
                                    .setText("Das Wissen hat bereits einen Inhalt.\nSoll versucht werden diesen zu übernehmen, oder soll er verworfen werden?")
                                    .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                                    .addButton("Verwerfen", customDialog1 -> {
                                        dialog_editOrAddKnowledge_content.setText("");
                                        newKnowledge[0].setContent(null);
                                        change.run();
                                    })
                                    .addButton("Übernehmen", customDialog1 -> {
                                        dialog_editOrAddKnowledge_content.setText("");
                                        newKnowledge[0].setContent(null);
                                        List<Knowledge.Item> itemList = newKnowledge[0].getItemList();
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
                            layoutContent.setVisibility(View.VISIBLE);
                        };

                        if (newKnowledge[0].hasItems()) {
                            com.finn.androidUtilities.CustomDialog.Builder(this)
                                    .setTitle("Inhalt Vorhanden")
                                    .setText("Das Wissen hat bereits einen Inhalt.\nSoll versucht werden diesen zu übernehmen, oder soll er verworfen werden?")
                                    .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.CANCEL_BUTTON)
                                    .addButton("Verwerfen", customDialog1 -> {
                                        newKnowledge[0].clearItemList();
                                        contentRecycler.reload();
                                        change.run();
                                    })
                                    .addButton("Übernehmen", customDialog1 -> {
                                        String content = newKnowledge[0].itemListToString();
                                        dialog_editOrAddKnowledge_content.setText(content);
                                        newKnowledge[0].setContent(content);
                                        newKnowledge[0].clearItemList();
                                        contentRecycler.reload();
                                        change.run();
                                    })
                                    .colorLastAddedButton()
                                    .show();
                        } else
                            change.run();
                    });

                    view.findViewById(R.id.dialog_editOrAddKnowledge_editCategories).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, customDialog, newKnowledge[0].getCategoryIdList(), newKnowledge[0], CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES));
                    view.findViewById(R.id.dialog_editOrAddKnowledge_editSources).setOnClickListener(view1 ->
                            showSourcesDialog(newKnowledge[0], view.findViewById(R.id.dialog_editOrAddKnowledge_sources), true));
                })
                .show();
        return Pair.create(returnDialog, newKnowledge[0]);
    }

    private void generateItemRecycler(List<Knowledge.Item> itemList, CustomRecycler<Knowledge.Item> itemRecycler, CustomRecycler<Knowledge.Item> parentRecycler, int depth, CustomList[] focusIndex, CustomList<Integer> posList) {
        itemRecycler
                .setItemLayout(R.layout.list_item_knowledge_list)
                .enableDragAndDrop(objectList -> {
                    itemList.clear();
                    itemList.addAll(objectList);
//                    itemRecycler.reload();
                }) // ToDo: funktion zu customRecycler hinzufügen wo per id View zum ziehen übergeben wird
                .setSetItemContent((customRecycler, itemView, item) -> {
                    RecyclerView recycler = customRecycler.getRecycler();
//                    int index = recycler.getChildAdapterPosition(itemView);
                    int index = customRecycler.getObjectList().indexOf(item);
                    CustomList<Integer> thisPosList = new CustomList<>(posList).add(new Integer[]{index});

//                    ((TextView) itemView.findViewById(R.id.listItem_knowledgeList_marker)).setText(thisPosList.stream().map(String::valueOf).collect(Collectors.joining("-")));

                    EditText listItem_knowledgeList_text = itemView.findViewById(R.id.listItem_knowledgeList_text);
                    Utility.removeTextListeners(listItem_knowledgeList_text);
                    listItem_knowledgeList_text.setText(item.getName());
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
                            com.finn.androidUtilities.CustomDialog.Builder(this)
                                    .setTitle("Stichpunkt löschen")
                                    .setText("Der Stichpunkt besitzt selber noch Unter-Spichpunkte\nWillst du trotzdem löschen?")
                                    .setButtonConfiguration(com.finn.androidUtilities.CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                    .addButton(com.finn.androidUtilities.CustomDialog.BUTTON_TYPE.YES_BUTTON, customRecycler1 -> onDelete.run())
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
                    listItem_knowledgeList_text.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            customRecycler.getObjectList().get(index).setName(s.toString());
                            listItem_knowledgeList_remove.setVisibility(item.getName().trim().isEmpty() && (index != 0 || depth != 0) ? View.VISIBLE : View.GONE);

                            if (depth == 0) {
                                int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) recycler.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
                                int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(Utility.dpToPx(280), View.MeasureSpec.AT_MOST);
                                recycler.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
                                if (recycler.getHeight() > Utility.dpToPx(280))
                                    customRecycler.getRecycler().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utility.dpToPx(280)));
                                else if (recycler.getMeasuredHeight() < Utility.dpToPx(280))
                                    customRecycler.getRecycler().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            }

                            if (before == 0 && s.toString().startsWith("   ") && !s.toString().endsWith("\n\n")) {
                                Knowledge.Item previousItem = Knowledge.previousItem(itemList, item);

                                if (previousItem == null)
                                    return;

                                itemList.remove(item);
                                previousItem.addChild(item);
                                String BREAKPOINT = null;


                                focusIndex[0] = thisPosList.add(new Integer[]{thisPosList.removeLast() - 1}).add(new Integer[]{0});


                                customRecycler.reload();
                            }
                            if (before == 0 && s.toString().endsWith("\n\n")) {

                                focusIndex[0] = thisPosList.add(new Integer[]{thisPosList.removeLast() + 1});


                                Knowledge.newItem(itemList, item);
                                item.setName(item.getName().trim());
                                customRecycler.reload();
                            }

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });

                    RecyclerView listItem_knowledgeList_recycler = itemView.findViewById(R.id.listItem_knowledgeList_recycler);
                    TableRow listItem_knowledgeList_recycler_row = itemView.findViewById(R.id.listItem_knowledgeList_recycler_row);
                    if (item.hasChild()) {
                        listItem_knowledgeList_recycler_row.setVisibility(View.VISIBLE);

                        generateItemRecycler(item.getChildrenList(), new CustomRecycler<>(this, listItem_knowledgeList_recycler), itemRecycler, depth + 1,
                                focusIndex, thisPosList);
                    }
                    else
                        listItem_knowledgeList_recycler_row.setVisibility(View.GONE);
                })
                .setGetActiveObjectList(customRecycler -> itemList)
                .generate();
    }

    private CustomDialog showDetailDialog(Knowledge knowledge) {
        setResult(RESULT_OK);
        List<String> categoriesNames = new ArrayList<>();
        knowledge.getCategoryIdList().forEach(uuid -> categoriesNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Detail Ansicht")
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
                                if (!knowledge.getContent().isEmpty()) {
                                    ((EditText) view.findViewById(R.id.dialog_shareKnowledge_content)).setText(knowledge.getContent());
                                    ((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_content_check)).setChecked(true);
                                }
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
                            })
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                boolean format = ((Switch) customDialog1.findViewById(R.id.dialog_shareKnowledge_format)).isChecked();
                                List<String> textList = new ArrayList<>();
                                if (((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_title_check)).isChecked())
                                    textList.add(String.format((format ? "*Titel*:\n%s" : "Titel:\n%s"), ((EditText) customDialog1.findViewById(R.id.dialog_shareKnowledge_title)).getText()));
                                if (((CheckBox) customDialog1.findViewById(R.id.dialog_shareKnowledge_content_check)).isChecked())
                                    textList.add(String.format((format ? "*Inhalt*:\n%s" : "Inhalt:\n%s"), ((EditText) customDialog1.findViewById(R.id.dialog_shareKnowledge_content)).getText()));
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
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_title)).setText(knowledge.getName());

                    TextView dialog_detailKnowledge_content = view.findViewById(R.id.dialog_detailKnowledge_content);
                    RecyclerView dialog_detailKnowledge_list = view.findViewById(R.id.dialog_detailKnowledge_list);
                    if (knowledge.hasContent()) {
                        dialog_detailKnowledge_content.setVisibility(View.VISIBLE);
                        dialog_detailKnowledge_list.setVisibility(View.GONE);
                        dialog_detailKnowledge_content.setText(knowledge.getContent());
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



                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_categories)).setText(String.join(", ", categoriesNames));
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_sources)).setText(knowledge.getSources().stream().map(strings -> strings.get(0)).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_lastChanged)).setText(String.format("%s Uhr", new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(knowledge.getLastChanged())));
                    view.findViewById(R.id.dialog_detailKnowledge_details).setVisibility(View.VISIBLE);
                    ((RatingBar) view.findViewById(R.id.dialog_detailKnowledge_rating)).setRating(knowledge.getRating());

                    view.findViewById(R.id.dialog_detailKnowledge_listSources).setOnClickListener(v -> {
                        showSourcesDialog(knowledge, view.findViewById(R.id.dialog_detailKnowledge_sources), false);
                    });
                })
                .setOnDialogDismiss(customDialog -> detailDialog = null);
        returnDialog.show();
        return returnDialog;
    }

    private CustomRecycler<Knowledge.Item> generateItemTextRecycler(RecyclerView recycler, List<Knowledge.Item> itemList, View targetView) {
        return new CustomRecycler<Knowledge.Item>(this, recycler)
                .setObjectList(itemList)
                .hideOverscroll()
                .setItemLayout(R.layout.list_item_knowledge_list_text)
                .setSetItemContent((customRecycler2, itemView1, item) -> {
                    ((TextView) itemView1.findViewById(R.id.listItem_knowledgeList_text)).setText(item.getName());

                    RecyclerView childRecycler = itemView1.findViewById(R.id.listItem_knowledgeList_list);
                    TableRow listItem_knowledgeList_listRow = itemView1.findViewById(R.id.listItem_knowledgeList_listRow);
                    if (item.hasChild_real()) {
                        listItem_knowledgeList_listRow.setVisibility(View.VISIBLE);
                        CustomRecycler<Knowledge.Item> recycler1 = generateItemTextRecycler(childRecycler, item.getChildrenList(), targetView);
                        if (targetView != null) {
                            recycler1.getRecycler().setOnTouchListener((v, event) -> targetView.onTouchEvent(event));
                        }
                    }
                    else
                        listItem_knowledgeList_listRow.setVisibility(View.GONE);
                })
                .generate();
    }


    private void saveKnowledge(CustomDialog dialog, String titel, String content, Knowledge newKnowledge, Knowledge knowledge) {

        if (knowledge == null)
            knowledge = newKnowledge;

        // ToDo: deepCopy item list und dann hier kürzen und zurückschreiben

        knowledge.setName(titel);
        knowledge.setContent(content);
        knowledge.setCategoryIdList(newKnowledge.getCategoryIdList());
        knowledge.setSources(newKnowledge.getSources());
        knowledge.setRating(((RatingBar) dialog.findViewById(R.id.dialog_editOrAddKnowledge_rating)).getRating());
        knowledge.setLastChanged(new Date());
        knowledge.setItemList(newKnowledge.getItemList().stream().filter(item -> {
            item.setName(item.getName().trim());
            return !item.getName().isEmpty();
        }).collect(Collectors.toList()));

        database.knowledgeMap.put(knowledge.getUuid(), knowledge);
        reLoadRecycler();
        dialog.dismiss();

        Database.saveAll();

        Utility.showCenteredToast(this, "Wissen gespeichert");

        if (detailDialog != null)
            detailDialog.reloadView();
    }

    private void showRandomDialog() {
        CustomList<Knowledge> filterdKnowledgeList = new CustomList<>(filterList(allKnowledgeList));
        if (filterdKnowledgeList.isEmpty()) {
            Toast.makeText(this, "Nichts zum Zeigen", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomDialog.Builder(this)
                .setTitle("Zufälliges Wissen")
                .setView(R.layout.dialog_detail_knowledge)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Nochmal", customDialog -> {
                    if (filterdKnowledgeList.isEmpty()) {
                        Toast.makeText(this, "Wissen erschöpft", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    customDialog.reloadView();

                }, false)
                .setSetViewContent((customDialog, view, reload) -> {
                    Knowledge randomKnowledge = filterdKnowledgeList.removeRandom();

                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_title)).setText(randomKnowledge.getName());

                    TextView listItem_knowledge_content = view.findViewById(R.id.dialog_detailKnowledge_content);
                    RecyclerView listItem_knowledge_list = view.findViewById(R.id.dialog_detailKnowledge_list);
                    if (randomKnowledge.hasContent()) {
                        listItem_knowledge_content.setVisibility(View.VISIBLE);
                        listItem_knowledge_list.setVisibility(View.GONE);
                        listItem_knowledge_content.setText(randomKnowledge.getContent());
                    } else if (randomKnowledge.hasItems()) {
                        listItem_knowledge_list.setVisibility(View.VISIBLE);
                        listItem_knowledge_content.setVisibility(View.GONE);
                        generateItemTextRecycler(listItem_knowledge_list, randomKnowledge.getItemList(), null);
                    } else {
                        listItem_knowledge_content.setVisibility(View.VISIBLE);
                        listItem_knowledge_list.setVisibility(View.GONE);
                        listItem_knowledge_content.setText("");
                    }

                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_categories)).setText(
                            randomKnowledge.getCategoryIdList().stream().map(uuid -> database.knowledgeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", ")));

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
                                    String domainName = getDomainFromUrl(s.toString(), true);
                                    dialog_sources_name.getEditText().setText(domainName);
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
                            .setSetItemContent((customRecycler, itemView, nameUrlPair) -> {
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
                .setObjectExtra(sourcesText)
                .disableScroll()
                .setOnDialogDismiss(customDialog -> ((TextView) customDialog.getObjectExtra()).setText(
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

    private String getDomainFromUrl(String url, boolean shortened) {
        Pattern pattern = Pattern.compile("(?<=://)[^/]*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String substring = matcher.group(0); //.substring(3);
            if (shortened) {
                String[] split = substring.split("\\.");
                return split[split.length - 2];
            } else
                return substring;
        }
        return null;
    }
//  <----- Sources -----
}
