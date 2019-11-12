package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KnowledgeActivity extends AppCompatActivity {


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
    private CustomDialog detailDialog;

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
        customRecycler_List = CustomRecycler.Builder(this, findViewById(R.id.recycler))
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
                .useCustomRipple()
                .setOnClickListener((customRecycler, view, object, index) -> {
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
                .addSubOnClickListener(R.id.listItem_knowledge_details, (customRecycler, view, object, index) -> detailDialog = showDetailDialog((Knowledge) object), false)
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    addOrEditDialog[0] = showEditOrNewDialog((Knowledge) object);
                })
                .hideDivider()
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
            newKnowledge[0].getSources().forEach(nameUrlPair  -> sourcesNames.add(nameUrlPair.get(0)));
        }
        CustomDialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(knowledge == null ? "Neues Wissen" : "Wissen Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_knowledge)
                .setButtonType(CustomDialog.ButtonType.CUSTOM);

        if (knowledge != null)
            returnDialog.addButton("Löschen", (customDialog, dialog) -> {
                CustomDialog.Builder(this)
                        .setTitle("Löschen")
                        .setText("Willst du wirklich '" + knowledge.getName() + "' löschen?")
                        .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                        .addButton(CustomDialog.OK_BUTTON, (customDialog1, dialog1) -> {
                            database.knowledgeMap.remove(knowledge.getUuid());
                            Database.saveAll();
                            reLoadRecycler();
                        })
                        .show();
            }, false);

        returnDialog
                .addButton("Abbrechen", (customDialog, dialog) -> {})
                .addButton("Speichern", (customDialog, dialog) -> {
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
                .setSetViewContent((customDialog, view) -> {
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
                            Utility.showEditItemDialog(this, addOrEditDialog, newKnowledge[0].getCategoryIdList(), newKnowledge[0], CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES));
                    view.findViewById(R.id.dialog_editOrAddKnowledge_editSources).setOnClickListener(view1 ->
                                    showSourcesDialog(newKnowledge[0], view.findViewById(R.id.dialog_editOrAddKnowledge_sources), true));
//                    view.findViewById(R.id.dialog_editOrAddKnowledge_editStudio).setOnClickListener(view1 ->
//                            Utility.showEditItemDialog(this, addOrEditDialog, newKnowledge[0].getStudioList(), newKnowledge[0], ParentClass.OBJECT_TYPE.STUDIO ));
                })
                .show();
        return returnDialog.getDialog();
    }

    private CustomDialog showDetailDialog(Knowledge knowledge) {
        setResult(RESULT_OK);
        List<String> categoriesNames = new ArrayList<>();
        knowledge.getCategoryIdList().forEach(uuid -> categoriesNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_detail_knowledge)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Bearbeiten", (customDialog, dialog) ->
                        addOrEditDialog[0] = showEditOrNewDialog(knowledge), false)
//                .addButton("Öffnen mit...", dialog -> openUrl(knowledge, true), false)
                .setSetViewContent((customDialog, view) -> {
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_title)).setText(knowledge.getName());
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_content)).setText(knowledge.getContent());
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_categories)).setText(String.join(", ", categoriesNames));
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_sources)).setText(knowledge.getSources().stream().map(strings -> strings.get(0)).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_detailKnowledge_lastChanged)).setText(String.format("%s Uhr", new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(knowledge.getLastChanged())));
                    view.findViewById(R.id.dialog_detailKnowledge_details).setVisibility(View.VISIBLE);
                    ((RatingBar) view.findViewById(R.id.dialog_detailKnowledge_rating)).setRating(knowledge.getRating());

                    view.findViewById(R.id.dialog_detailKnowledge_listSources).setOnClickListener(v -> {
                        showSourcesDialog(knowledge, view.findViewById(R.id.dialog_detailKnowledge_sources),false);
                    });
                })
                .setOnDialogDismiss(customDialog -> detailDialog = null);
        returnDialog.show();
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

        if (detailDialog != null)
            detailDialog.reloadView();
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
                .setSetViewContent((customDialog, view) -> {
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

    //  ----- Sources ----->
    private void showSourcesDialog(Knowledge knowledge, TextView sourcesText, boolean edit) {
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
        Dialog sourcesDialog = CustomDialog.Builder(this)
                .setTitle("Quellen")
                .setView(R.layout.dialog_sources)
                .setSetViewContent((customDialog, view) -> {
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

                    CustomRecycler sources_customRecycler = CustomRecycler.Builder(this, view.findViewById(R.id.dialog_sources_sources))
                            .setItemLayout(R.layout.list_item_source_or_item)
                            .setGetActiveObjectList(() -> {
                                List<List<String>> sources = knowledge.getSources();
                                view.findViewById(R.id.dialog_sources_noSources).setVisibility(sources.isEmpty() ? View.VISIBLE : View.GONE);
                                return sources;
                            })
                            .setSetItemContent((CustomRecycler.SetItemContent<List<String>>)(itemView, nameUrlPair) -> {
                                ((TextView) itemView.findViewById(R.id.listItem_source_name)).setText(nameUrlPair.get(0));
                                ((TextView) itemView.findViewById(R.id.listItem_source_content)).setText(nameUrlPair.get(1));
                            })
                            .hideDivider()
                            .useCustomRipple()
                            .setOnClickListener((customRecycler, itemView, o, index) -> Utility.openUrl(this, ((List<String>) o).get(1), false))
                            .setOnLongClickListener((CustomRecycler.OnLongClickListener<List<String>>)(customRecycler, view1, stringList, index) -> {
                                Dialog dialog = customDialog.getDialog();
                                dialog.findViewById(R.id.dialog_sources_editLayout).setVisibility(View.VISIBLE);
                                dialog.findViewById(buttonId_add).setVisibility(View.GONE);
                                dialog.findViewById(R.id.dialog_sources_delete).setVisibility(View.VISIBLE);
                                dialog_sources_name.getEditText().setText(stringList.get(0));
                                dialog_sources_url.getEditText().setText(stringList.get(1));
                                currentSource[0] = stringList;
                            })
                            .generateCustomRecycler();

                    view.findViewById(R.id.dialog_sources_delete).setOnClickListener(v -> {
                        CustomDialog.Builder(this)
                                .setTitle("Quelle Löschen")
                                .setText("Willst du wirklich die Quelle '" + currentSource[0].get(0) + "' löschen?")
                                .setButtonType(CustomDialog.ButtonType.YES_NO)
                                .addButton(CustomDialog.YES_BUTTON, (customDialog1, dialog) -> {
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
                        }
                        else {
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
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Hinzufügen", (customDialog, dialog) -> {
                    dialog.findViewById(R.id.dialog_sources_editLayout).setVisibility(View.VISIBLE);
                    dialog.findViewById(buttonId_add).setVisibility(View.GONE);
                    dialog.findViewById(R.id.dialog_sources_delete).setVisibility(View.GONE);
                    EditText dialog_sources_url = ((TextInputLayout) dialog.findViewById(R.id.dialog_sources_url)).getEditText();
                    dialog_sources_url.requestFocus();
                    Utility.changeWindowKeyboard(this, dialog_sources_url, true);
                }, buttonId_add, false)
                .addButton("Zurück", (customDialog, dialog) -> {})
                .setObjectExtra(sourcesText)
                .setOnDialogDismiss(customDialog -> ((TextView) customDialog.getObjectExtra()).setText(
                        knowledge.getSources().stream().map(strings -> strings.get(0)).collect(Collectors.joining(", "))
                ))
                .show();
        if (edit)
            sourcesDialog.findViewById(buttonId_add).setVisibility(View.GONE);
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
        if (matcher.find())
        {
            String substring = matcher.group(0); //.substring(3);
            if (shortened) {
                String[] split = substring.split("\\.");
                return split[split.length - 2];
            }
            else
                return substring;
        }
        return null;
    }
//  <----- Sources -----





}
