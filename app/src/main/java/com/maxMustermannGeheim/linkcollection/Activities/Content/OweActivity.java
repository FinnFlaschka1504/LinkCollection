package com.maxMustermannGeheim.linkcollection.Activities.Content;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Owe;
import com.maxMustermannGeheim.linkcollection.Daten.Owe.Person;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
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

public class OweActivity extends AppCompatActivity {

    enum SORT_TYPE{
        NAME, OWN_OR_OTHER, LATEST, STATUS
    }

    public enum FILTER_TYPE{
        NAME, DESCRIPTION, PERSON, OWN, OTHER, OPEN, CLOSED
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
    private ArrayList<Owe> allOweList;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.DESCRIPTION, FILTER_TYPE.PERSON, FILTER_TYPE.OWN, FILTER_TYPE.OTHER
            , FILTER_TYPE.OPEN, FILTER_TYPE.CLOSED));
    private Dialog detailDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owe);

        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

        CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
        if (extraSearchCategory != null) {
            filterTypeSet.clear();

            switch (extraSearchCategory) {
                case KNOWLEDGE_CATEGORIES:
                    filterTypeSet.add(FILTER_TYPE.PERSON);
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
            setContentView(R.layout.activity_owe);
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
                .setItemLayout(R.layout.list_item_owe)
                .setGetActiveObjectList(() -> {
                    if (searchQuery.equals("")) {
                        allOweList = new ArrayList<>(database.oweMap.values());
                        return sortList(allOweList);
                    }
                    else
                        return filterList(allOweList);
                })
                .setSetItemContent((CustomRecycler.SetItemContent<Owe>) (itemView, owe) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_owe_title)).setText(owe.getName());
                    ((TextView) itemView.findViewById(R.id.listItem_owe_description)).setText(owe.getDescription());


                    ((TextView) itemView.findViewById(R.id.listItem_owe_person)).setText(owe.getItemList().stream()
                            .map(item -> database.personMap.get(item.getPersonId()).getName()).collect(Collectors.joining(", ")));
                    itemView.findViewById(R.id.listItem_owe_person).setSelected(true);

                    if (owe.getItemList().stream().noneMatch(Owe.Item::isOpen))
                        itemView.findViewById(R.id.listItem_owe_check).setVisibility(View.VISIBLE);
                    else
                        itemView.findViewById(R.id.listItem_owe_check).setVisibility(View.GONE);

                    TextView listItem_owe_sum = itemView.findViewById(R.id.listItem_owe_sum);
                    listItem_owe_sum.setText(String.format("(%s)", Utility.formatToEuro(owe.getItemList().stream().mapToDouble(Owe.Item::getAmount).sum())));
                    if (owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OWN)
                        listItem_owe_sum.setTextColor(Color.parseColor("#4CAF50"));
                    else
                        listItem_owe_sum.setTextColor(Color.RED);
//                    if (owe.getRating() > 0) {
//                        itemView.findViewById(R.id.listItem_owe_rating_layout).setVisibility(View.VISIBLE);
//                        ((TextView) itemView.findViewById(R.id.listItem_owe_rating)).setText(String.valueOf(owe.getRating()));
//                    }
//                    else
//                        itemView.findViewById(R.id.listItem_owe_rating_layout).setVisibility(View.GONE);
                })
                .setUseCustomRipple(true)
                .setOnClickListener((customRecycler, view, object, index) -> {
                    TextView listItem_owe_content = view.findViewById(R.id.listItem_owe_description);
                    if (listItem_owe_content.isFocusable()) {
                        listItem_owe_content.setSingleLine(true);
                        listItem_owe_content.setFocusable(false);

                    } else {
                        listItem_owe_content.setSingleLine(false);
                        listItem_owe_content.setFocusable(true);
                    }
//                  openUrl(object, false);
                })
                .addSubOnClickListener(R.id.listItem_owe_details, (customRecycler, view, object, index) -> detailDialog = showDetailDialog((Owe) object), false)
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    addOrEditDialog[0] = showEditOrNewDialog((Owe) object);
                })
                .setShowDivider(false)
                .generateCustomRecycler();
    }

    private List<Owe> filterList(ArrayList<Owe> allOweList) {
        return sortList(allOweList.stream().filter(owe -> Utility.containedInOwe(searchQuery, owe, filterTypeSet)).collect(Collectors.toList()));
    }

    private List<Owe> sortList(List<Owe> oweList) {
        switch (sort_type) {
            case NAME:
                oweList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
                break;
            case OWN_OR_OTHER:
                oweList.sort((o1, o2) -> o1.getOwnOrOther().compareTo(o2.getOwnOrOther()));
                break;
            case LATEST:
                oweList.sort((o1, o2) -> o1.getDate().compareTo(o2.getDate()) * -1);
                break;
            case STATUS:
                oweList.sort((o1, o2) -> Boolean.compare(o1.isOpen(), o2.isOpen()) * -1);
                break;
        }
        if (reverse)
            Collections.reverse(oweList);
        return oweList;
    }

    private void reLoadRecycler() {
        customRecycler_List.reload();
    }

    private Dialog showEditOrNewDialog(Owe owe) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);

        final Owe[] newOwe = {null};
        if (owe != null) {
            newOwe[0] = owe.cloneOwe();
        }
        Dialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(owe == null ? "Neue Schulden" : "Schulden Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_owe)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                .addButton(CustomDialog.SAVE_BUTTON, (customDialog, dialog) -> {
                    String title = ((EditText) dialog.findViewById(R.id.dialog_editOrAdd_owe_Title)).getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    newOwe[0].setName(title);
                    newOwe[0].setDescription(((EditText) dialog.findViewById(R.id.dialog_editOrAdd_owe_description)).getText().toString().trim());

                    newOwe[0].setOwnOrOther(((Spinner) dialog.findViewById(R.id.dialog_editOrAdd_owe_ownOrOther)).getSelectedItemPosition() == 0 ? Owe.OWN_OR_OTHER.OWN : Owe.OWN_OR_OTHER.OTHER);
                  saveOwe(dialog, newOwe, owe);

                }, false)
                .setSetViewContent((customDialog, view) -> {
                    if (newOwe[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAdd_owe_Title)).setText(newOwe[0].getName());
                        ((EditText) view.findViewById(R.id.dialog_editOrAdd_owe_description)).setText(owe.getDescription());
                        ((TextView) view.findViewById(R.id.dialog_editOrAdd_owe_items)).setText(newOwe[0].getItemList().stream()
                                .map(item -> String.format("%s (%s)", database.personMap.get(item.getPersonId()).getName(), Utility.formatToEuro(item.getAmount())))
                                .collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAdd_owe_items).setSelected(true);
                        ((Spinner) view.findViewById(R.id.dialog_editOrAdd_owe_ownOrOther)).setSelection(newOwe[0].getOwnOrOther() == Owe.OWN_OR_OTHER.OWN ? 0 : 1);

                    }
                    else
                        newOwe[0] = new Owe("").setDate(new Date());


                    view.findViewById(R.id.dialog_editOrAdd_owe_editItems).setOnClickListener(view1 ->
                            showItemsDialog(newOwe[0], view.findViewById(R.id.dialog_editOrAdd_owe_items), true));
                })
                .show();
        return returnDialog;
    }

    private Dialog showDetailDialog(Owe owe) {
        setResult(RESULT_OK);
        Dialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_detail_owe)
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Bearbeiten", (customDialog, dialog) ->
                        addOrEditDialog[0] = showEditOrNewDialog(owe), false)
//                .addButton("Öffnen mit...", dialog -> openUrl(owe, true), false)
                .setSetViewContent((customDialog, view) -> {
                    view.findViewById(R.id.dialog_detail_owe_details).setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_title)).setText(owe.getName());
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_description)).setText(owe.getDescription());
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_items)).setText(String.join(", ", owe.getItemList().stream()
                            .map(item -> String.format("%s (%s)", database.personMap.get(item.getPersonId()).getName(), Utility.formatToEuro(item.getAmount())))
                            .collect(Collectors.joining(", "))));
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_ownOrOther)).setText(owe.getOwnOrOther().getName());
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_lastChanged)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(owe.getDate()));

                    view.findViewById(R.id.dialog_detail_owe_listItems).setOnClickListener(v -> {
                        showItemsDialog(owe, view.findViewById(R.id.dialog_detail_owe_items),false);
                    });
                })
                .show();
        returnDialog.setOnDismissListener(dialogInterface -> detailDialog = null);
        return returnDialog;
    }

    private void saveOwe(Dialog dialog, Owe[] newOwe, Owe owe) {

        if (owe == null)
            owe = newOwe[0];

        owe.setName(newOwe[0].getName());
        owe.setDescription(newOwe[0].getDescription());
        owe.setItemList(newOwe[0].getItemList());
        owe.setOwnOrOther(newOwe[0].getOwnOrOther());

        database.oweMap.put(owe.getUuid(), owe);
        reLoadRecycler();
        dialog.dismiss();

        Database.saveAll();

        Utility.showCenterdToast(this, "Schulden gespeichert");

        if (detailDialog == null)
            return;

        detailDialog.dismiss();
        detailDialog = showDetailDialog(owe);

    }

//    private void showRandomDialog() {
//        List<Owe> filterdOweList = filterList(allOweList);
//        if (filterdOweList.isEmpty()) {
//            Toast.makeText(this, "Nichts zum Zeigen", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        final Owe[] randomOwe = {filterdOweList.get((int) (Math.random() * filterdOweList.size()))};
//        List<String> categoryNames = new ArrayList<>();
//        randomOwe[0].getCategoryIdList().forEach(uuid -> categoryNames.add(database.knowledgeCategoryMap.get(uuid).getName()));
//
//        CustomDialog.Builder(this)
//                .setTitle("Zufällige Schulden")
//                .setView(R.layout.dialog_detail_knowledge)
//                .setButtonType(CustomDialog.ButtonType.CUSTOM)
//                .addButton("Nochmal", (customDialog, dialog) -> {
//                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
//                    randomOwe[0] = filterdOweList.get((int) (Math.random() * filterdOweList.size()));
//                    ((TextView) dialog.findViewById(R.id.dialog_detailOwe_title)).setText(randomOwe[0].getName());
//                    ((TextView) dialog.findViewById(R.id.dialog_detailOwe_content)).setText(randomOwe[0].getContent());
//
//                    List<String> darstellerNames_neu = new ArrayList<>();
//                    randomOwe[0].getCategoryIdList().forEach(uuid -> darstellerNames_neu.add(database.knowledgeCategoryMap.get(uuid).getName()));
//                    ((TextView) dialog.findViewById(R.id.dialog_detailOwe_categories)).setText(String.join(", ", darstellerNames_neu));
//
//
//                }, false)
////                .addButton("Öffnen", (customDialog, dialog) -> openUrl(randomOwe[0], false), false)
//                .setSetViewContent((customDialog, view) -> {
//                    ((TextView) view.findViewById(R.id.dialog_detailOwe_title)).setText(randomOwe[0].getName());
//                    ((TextView) view.findViewById(R.id.dialog_detailOwe_content)).setText(randomOwe[0].getContent());
//                    ((TextView) view.findViewById(R.id.dialog_detailOwe_categories)).setText(String.join(", ", categoryNames));
//
//                })
//                .show();
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_bar_owe, menu);

        Menu subMenu = menu.findItem(R.id.taskBar_filter).getSubMenu();
        subMenu.findItem(R.id.taskBar_owe_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        subMenu.findItem(R.id.taskBar_owe_filterByDescription)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.DESCRIPTION));
        subMenu.findItem(R.id.taskBar_owe_filterByPerson)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.PERSON));
        subMenu.findItem(R.id.taskBar_owe_filterByOwn)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.OWN));
        subMenu.findItem(R.id.taskBar_owe_filterByOther)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.OTHER));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_owe_add:
                addOrEditDialog[0] = showEditOrNewDialog(null);
                break;
//            case R.id.taskBar_owe_random:
//                showRandomDialog();
//                break;

            case R.id.taskBar_owe_sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_sortByOwnOrOther:
                sort_type = SORT_TYPE.OWN_OR_OTHER;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_sortByStatus:
                sort_type = SORT_TYPE.STATUS;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_sortByLatest:
                sort_type = SORT_TYPE.LATEST;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_sortReverse:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                reverse = checked;
                reLoadRecycler();
                break;

            case R.id.taskBar_owe_filterByName:
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
            case R.id.taskBar_owe_filterByPerson:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.PERSON);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.PERSON);
                    item.setChecked(true);
                }
                reLoadRecycler();
//                textListener.onQueryTextChange(videos_search.getQuery().toString());
                break;
            case R.id.taskBar_owe_filterByDescription:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.DESCRIPTION);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.DESCRIPTION);
                    item.setChecked(true);
                }
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_filterByOwn:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.OWN);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.OWN);
                    item.setChecked(true);
                }
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_filterByOther:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.OTHER);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.OTHER);
                    item.setChecked(true);
                }
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_filterByOpen:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.OPEN);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.OPEN);
                    item.setChecked(true);
                }
                reLoadRecycler();
                break;
            case R.id.taskBar_owe_filterByClosed:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.CLOSED);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.CLOSED);
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
    private void showItemsDialog(Owe owe, TextView sourcesText, boolean edit) {
        int buttonId_add = View.generateViewId();
        final ArrayList<String> nameList = database.personMap.values().stream().map(ParentClass::getName).collect(Collectors.toCollection(ArrayList::new));
        KnowledgeActivity.TextValidation nameValidation = (textInputLayout, changeErrorMessage) -> {
            String text = textInputLayout.getEditText().getText().toString().trim();

            if (text.isEmpty()) {
                if (changeErrorMessage)
                    textInputLayout.setError("Das Feld darf nicht leer sein!");
                return false;
            } else if (!nameList.stream().anyMatch(s1 -> s1.equals(text))) {
                if (changeErrorMessage)
                    textInputLayout.setError("Person auswählen, oder hinzufügen");
                return false;
            } else {
                if (changeErrorMessage)
                    textInputLayout.setError(null);
                return true;
            }
        };
        KnowledgeActivity.TextValidation urlValidation = (textInputLayout, changeErrorMessage) -> {
            String text = textInputLayout.getEditText().getText().toString().trim();

            if (text.isEmpty()) {
                textInputLayout.setError("Das Feld darf nicht leer sein!");
                return false;
            }
//            else if (!text.matches("(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$")) {
//                textInputLayout.setError("Eine valide URL eingeben!");
//                return false;
//
//            }
            else {
                textInputLayout.setError(null);
                return true;
            }

        };
        final Owe.Item[] currentItem = new Owe.Item[]{null};
        Dialog sourcesDialog = CustomDialog.Builder(this)
                .setTitle("Einträge")
                .setView(R.layout.dialog_items)
                .setSetViewContent((customDialog, view) -> {
                    LinearLayout dialog_items_editLayout = view.findViewById(R.id.dialog_items_editLayout);
                    TextInputLayout dialog_items_name = view.findViewById(R.id.dialog_items_name);
                    TextInputLayout dialog_items_amount = view.findViewById(R.id.dialog_items_amount);
                    if (edit) {
                        dialog_items_editLayout.setVisibility(View.VISIBLE);
                        dialog_items_name.getEditText().requestFocus();
                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
                    }
                    Button dialog_items_save = view.findViewById(R.id.dialog_items_save);
                    final ImageView dialog_items_addNames = view.findViewById(R.id.dialog_items_addNames);
                    dialog_items_name.getEditText().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (!nameList.stream().anyMatch(s1 -> s1.matches(s.toString().trim())) && !s.toString().trim().isEmpty())
                                dialog_items_addNames.setVisibility(View.VISIBLE);
                            else
                                dialog_items_addNames.setVisibility(View.GONE);
                            validation(nameValidation, urlValidation, dialog_items_name, dialog_items_amount, true, false, dialog_items_save);
                        }
                    });
                    dialog_items_amount.getEditText().addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
//                            if (urlValidation.runTextValidation(dialog_items_amount, true)) {
//                                if (dialog_items_name.getEditText().getText().toString().isEmpty()) {
//                                    String domainName = getDomainFromUrl(s.toString(), true);
//                                    dialog_items_name.getEditText().setText(domainName);
//                                }
//                            }
                            validation(nameValidation, urlValidation, dialog_items_name, dialog_items_amount, false, true, dialog_items_save);
                        }
                    });
                    Runnable hideEdit = () -> {
                        dialog_items_name.getEditText().setText("");
                        dialog_items_amount.getEditText().setText("");
                        dialog_items_name.setError(null);
                        dialog_items_amount.setError(null);
                        dialog_items_editLayout.setVisibility(View.GONE);
                        Utility.changeWindowKeyboard(this, dialog_items_amount.getEditText(), false);
                        dialog_items_amount.getEditText().clearFocus();
                        customDialog.getDialog().findViewById(buttonId_add).setVisibility(View.VISIBLE);
                        currentItem[0] = null;
                    };


                    view.findViewById(R.id.dialog_items_cancel).setOnClickListener(v -> hideEdit.run());

                    CustomRecycler sources_customRecycler = CustomRecycler.Builder(this, view.findViewById(R.id.dialog_items_sources))
                            .setItemLayout(R.layout.list_item_source_or_item)
                            .setGetActiveObjectList(() -> {
                                List<Owe.Item> sources = owe.getItemList();
                                view.findViewById(R.id.dialog_items_noSources).setVisibility(sources.isEmpty() ? View.VISIBLE : View.GONE);
                                return sources;
                            })
                            .setSetItemContent((CustomRecycler.SetItemContent<Owe.Item>)(itemView, item) -> {
                                ((TextView) itemView.findViewById(R.id.listItem_source_name)).setText(database.personMap.get(item.getPersonId()).getName());
                                ((TextView) itemView.findViewById(R.id.listItem_source_url)).setText(Utility.formatToEuro(item.getAmount()));
                                itemView.findViewById(R.id.listItem_source_check).setVisibility(item.isOpen() ? View.GONE : View.VISIBLE);
                            })
                            .setShowDivider(false)
                            .setUseCustomRipple(true)
                            .setOnClickListener((CustomRecycler.OnClickListener<Owe.Item>)(customRecycler, itemView, item, index) -> {
                                item.setOpen(!item.isOpen());
                                Database.saveAll();
                                customRecycler.reload();
                            })
                            .setOnLongClickListener((CustomRecycler.OnLongClickListener<Owe.Item>)(customRecycler, view1, item, index) -> {
                                Dialog dialog = customDialog.getDialog();
                                dialog.findViewById(R.id.dialog_items_editLayout).setVisibility(View.VISIBLE);
                                dialog.findViewById(buttonId_add).setVisibility(View.GONE);
                                dialog.findViewById(R.id.dialog_items_delete).setVisibility(View.VISIBLE);
                                dialog_items_name.getEditText().setText(database.personMap.get(item.getPersonId()).getName());
                                dialog_items_amount.getEditText().setText(String.valueOf(item.getAmount()));
                                currentItem[0] = item;
                            })
                            .generateCustomRecycler();

                    view.findViewById(R.id.dialog_items_delete).setOnClickListener(v -> {
                        CustomDialog.Builder(this)
                                .setTitle("Eintrag Löschen")
                                .setText("Willst du wirklich den Eintrag von '" + database.personMap.get(currentItem[0].getPersonId()).getName() + "' löschen?")
                                .setButtonType(CustomDialog.ButtonType.YES_NO)
                                .addButton(CustomDialog.YES_BUTTON, (customDialog1, dialog) -> {
                                    owe.getItemList().remove(currentItem[0]);
                                    hideEdit.run();
                                    sources_customRecycler.reload();
                                    Database.saveAll();
                                })
                                .show();
                    });

                    dialog_items_save.setOnClickListener(v -> {
                        if (!nameValidation.runTextValidation(dialog_items_name, true) | !urlValidation.runTextValidation(dialog_items_amount, true))
                            return;
                        if (currentItem[0] == null) {
                            String personName = dialog_items_name.getEditText().getText().toString().trim();
                            String personId = database.personMap.values().stream().filter(person -> person.getName().equals(personName)).findFirst().get().getUuid();
                            Owe.Item newItem = new Owe.Item(personId, Double.parseDouble(dialog_items_amount.getEditText().getText().toString().trim()));
                            owe.getItemList().add(newItem);
                        }
                        else {
                            String personName = dialog_items_name.getEditText().getText().toString().trim();
                            String personId = database.personMap.values().stream().filter(person -> person.getName().equals(personName)).findFirst().get().getUuid();
                            currentItem[0].setPersonId(personId);
                            currentItem[0].setAmount(Double.parseDouble(dialog_items_amount.getEditText().getText().toString().trim()));
                        }
                        Database.saveAll();
                        sources_customRecycler.reload();
                        hideEdit.run();
                        currentItem[0] = null;
                    });

                    AutoCompleteTextView autoCompleteName = (AutoCompleteTextView) dialog_items_name.getEditText();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameList);
                    autoCompleteName.setAdapter(adapter);

                    dialog_items_addNames.setOnClickListener(v -> {
                        int saveButtonId = View.generateViewId();
                        CustomDialog.Builder(this)
                                .setTitle("Person Hinzufügen")
                                .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                                .setEdit(new CustomDialog.EditBuilder().setText(autoCompleteName.getText().toString()).setHint("Personen-Name").setFireButtonOnOK(saveButtonId))
                                .addButton(CustomDialog.OK_BUTTON, (customDialog1, dialog) -> {
                                    String name = CustomDialog.getEditText(dialog).trim();
                                    if (nameList.contains(name)) {
                                        Toast.makeText(this, "Keine zwei Personen dürfen gleich heißen", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Person newPerson = new Person(name);
                                    database.personMap.put(newPerson.getUuid(), newPerson);
                                    Database.saveAll();
                                    nameList.add(name);
                                    adapter.add(name);
                                    dialog.dismiss();
                                }, saveButtonId, false)
                                .show();
                    });
                })
                .setButtonType(CustomDialog.ButtonType.CUSTOM)
                .addButton("Hinzufügen", (customDialog, dialog) -> {
                    dialog.findViewById(R.id.dialog_items_editLayout).setVisibility(View.VISIBLE);
                    dialog.findViewById(buttonId_add).setVisibility(View.GONE);
                    dialog.findViewById(R.id.dialog_items_delete).setVisibility(View.GONE);
                    EditText dialog_items_name_edit = ((TextInputLayout) dialog.findViewById(R.id.dialog_items_name)).getEditText();
                    dialog_items_name_edit.requestFocus();
                    Utility.changeWindowKeyboard(this, dialog_items_name_edit, true);
                }, buttonId_add, false)
                .addButton("Zurück", (customDialog, dialog) -> {})
                .setObjectExtra(sourcesText)
                .setOnDialogDismiss(customDialog -> {
                    ((TextView) customDialog.getObjectExtra()).setText(
                            owe.getItemList().stream()
                                    .map(item -> String.format("%s (%s)", database.personMap.get(item.getPersonId()).getName(), Utility.formatToEuro(item.getAmount())))
                                    .collect(Collectors.joining(", ")));
                    reLoadRecycler();
                })
                .show();
        if (edit)
            sourcesDialog.findViewById(buttonId_add).setVisibility(View.GONE);
    }

    private boolean validation(KnowledgeActivity.TextValidation nameValidation, KnowledgeActivity.TextValidation urlValidation, TextInputLayout dialog_items_name
            , TextInputLayout dialog_items_url, boolean changeError_name, boolean changeError_url, Button dialog_items_save) {
        if (!nameValidation.runTextValidation(dialog_items_name, changeError_name) | !urlValidation.runTextValidation(dialog_items_url, changeError_url)) {
            dialog_items_save.setEnabled(false);
            return false;
        } else {
            dialog_items_save.setEnabled(true);
            return true;
        }
    }

    interface TextValidation {
        boolean runTextValidation(TextInputLayout textInputLayout, boolean changeErrorMessage);
    }

    private String getDomainFromUrl(String url, boolean shortened) {
        Pattern pattern = Pattern.compile(":\\/\\/.[^\\/]*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find())
        {
            String substring = matcher.group(0).substring(3);
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
