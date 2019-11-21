package com.maxMustermannGeheim.linkcollection.Activities.Content;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Utilitys.Database;
import com.maxMustermannGeheim.linkcollection.Utilitys.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class OweActivity extends AppCompatActivity {
    public static final String EXTRA_OWN_OR_OTHER = "EXTRA_OWN_OR_OTHER";
    public static final String EXTRA_OPEN = "EXTRA_OPEN";

    enum SORT_TYPE{
        NAME, OWN_OR_OTHER, LATEST, STATUS
    }

    public enum FILTER_TYPE{
        NAME, DESCRIPTION, PERSON, OWN, OTHER, OPEN, CLOSED
    }


    private boolean reverse;
    private SORT_TYPE sort_type = SORT_TYPE.STATUS;
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
    private CustomDialog detailDialog;
    private boolean fireSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owe);

        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
        if (extraSearchCategory != null) {
            filterTypeSet.remove(FILTER_TYPE.NAME);
            filterTypeSet.remove(FILTER_TYPE.DESCRIPTION);

            if (extraSearchCategory == CategoriesActivity.CATEGORIES.KNOWLEDGE_CATEGORIES) {
                filterTypeSet.add(FILTER_TYPE.PERSON);
            }

            String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
            if (extraSearch != null) {
                fireSearch = true;
                searchQuery = extraSearch;
            }
        }
        Owe.OWN_OR_OTHER ownOrOther = (Owe.OWN_OR_OTHER) getIntent().getSerializableExtra(EXTRA_OWN_OR_OTHER);
        if (ownOrOther != null) {
            fireSearch = true;
            switch (ownOrOther) {
                case OWN:
                    filterTypeSet.remove(FILTER_TYPE.OTHER);
                    break;
                case OTHER:
                    filterTypeSet.remove(FILTER_TYPE.OWN);
                    break;
            }
        }
        Boolean open = (Boolean) getIntent().getSerializableExtra(EXTRA_OPEN);
        if (open != null) {
            if (open)
                filterTypeSet.remove(FILTER_TYPE.CLOSED);
            else
                filterTypeSet.remove(FILTER_TYPE.OPEN);
        }


        loadDatabase();
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
            if (fireSearch)
                videos_search.setQuery(searchQuery, true);
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
        TextView noItem = findViewById(R.id.no_item);
        customRecycler_List = new CustomRecycler<Owe>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_owe)
                .setGetActiveObjectList(() -> {
                    allOweList = new ArrayList<>(database.oweMap.values());
                    List<Owe> newOweList;
                    if (searchQuery.equals("") && filterTypeSet.contains(FILTER_TYPE.OWN) && filterTypeSet.contains(FILTER_TYPE.OTHER) && filterTypeSet.contains(FILTER_TYPE.OPEN) && filterTypeSet.contains(FILTER_TYPE.CLOSED)) {
                        newOweList = allOweList;
                        sortList(newOweList);
                        if (newOweList.isEmpty()) {
                            noItem.setText("Keine Einträge");
                            noItem.setVisibility(View.VISIBLE);
                        }
                        else
                            noItem.setVisibility(View.GONE);
                    } else {
                        newOweList = filterList(allOweList);
                        if (newOweList.isEmpty()) {
                            noItem.setText("Kein Eintrag für die Suche");
                            noItem.setVisibility(View.VISIBLE);
                        }
                        else
                            noItem.setVisibility(View.GONE);
                    }
                    return newOweList;
                })
                .setSetItemContent((itemView, owe) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_owe_title)).setText(owe.getName());
                    ((TextView) itemView.findViewById(R.id.listItem_owe_description)).setText(owe.getDescription());
//                    itemView.findViewById(R.id.listItem_owe_description).setSelected(true);

                    setItemText(itemView.findViewById(R.id.listItem_owe_person), owe);
                    itemView.findViewById(R.id.listItem_owe_person).setSelected(true);

                    ImageView listItem_owe_check = itemView.findViewById(R.id.listItem_owe_check);
                    if (owe.getItemList().stream().noneMatch(Owe.Item::isOpen)) {
                        listItem_owe_check.setColorFilter(getColor(R.color.colorGreen), PorterDuff.Mode.SRC_IN);
                        listItem_owe_check.setAlpha(1f);
                    } else {
                        listItem_owe_check.setColorFilter(getColor(R.color.colorDrawable), PorterDuff.Mode.SRC_IN);
                        listItem_owe_check.setAlpha(0.2f);
                    }

                    TextView listItem_owe_sum = itemView.findViewById(R.id.listItem_owe_sum);
                    double allSum = owe.getItemList().stream().mapToDouble(Owe.Item::getAmount).sum();
                    double sum = owe.getItemList().stream().filter(item -> !item.isOpen()).mapToDouble(Owe.Item::getAmount).sum();
                    String text = "";
                    if (sum != 0 && sum != allSum)
                        text += Utility.formatToEuro(sum);
                    if (!text.isEmpty())
                        text += " / ";
                    text += Utility.formatToEuro(allSum);
                    listItem_owe_sum.setText(text);
                    if (owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OTHER)
                        listItem_owe_sum.setTextColor(getColor(R.color.colorGreen));
                    else
                        listItem_owe_sum.setTextColor(Color.RED);
//                    if (owe.getRating() > 0) {
//                        itemView.findViewById(R.id.listItem_owe_rating_layout).setVisibility(View.VISIBLE);
//                        ((TextView) itemView.findViewById(R.id.listItem_owe_rating)).setText(String.valueOf(owe.getRating()));
//                    }
//                    else
//                        itemView.findViewById(R.id.listItem_owe_rating_layout).setVisibility(View.GONE);
                })
//                .setOnClickListener((customRecycler, view, object, index) -> {
//                    TextView listItem_owe_content = view.findViewById(R.id.listItem_owe_description);
//                    if (listItem_owe_content.isFocusable()) {
//                        listItem_owe_content.setSingleLine(true);
//                        listItem_owe_content.setFocusable(false);
//
//                    } else {
//                        listItem_owe_content.setSingleLine(false);
//                        listItem_owe_content.setFocusable(true);
//                    }
////                  openUrl(object, false);
//                })
                .setOnClickListener((customRecycler, view, object, index) -> detailDialog = showDetailDialog(object))
                .setOnLongClickListener((customRecycler, view, object, index) -> {
                    addOrEditDialog[0] = showEditOrNewDialog(object);
                })
                .hideDivider()
                .generate();
    }

    private List<Owe> filterList(ArrayList<Owe> allOweList) {
        return sortList(allOweList.stream().filter(owe -> Utility.containedInOwe(searchQuery, owe, filterTypeSet)).collect(Collectors.toList()));
    }

    private List<Owe> sortList(List<Owe> oweList) {
        switch (sort_type) {
            case NAME:
                oweList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()) * (reverse ? -1 : 1));
                break;
            case OWN_OR_OTHER:
                oweList.sort((o1, o2) -> {
                    int compare;
                    if ((compare = o1.getOwnOrOther().compareTo(o2.getOwnOrOther())) != 0)
                        return compare * (reverse ? -1 : 1);
                    else if ((compare = Boolean.compare(o1.isOpen(), o2.isOpen()) * -1) != 0)
                        return compare;
                    else
                        return o1.getName().compareTo(o2.getName());
                });
                break;
            case LATEST:
                oweList.sort((o1, o2) -> {
                    int compare;
                    if ((compare = o1.getDate().compareTo(o2.getDate()) * -1) != 0)
                        return compare * (reverse ? -1 : 1);
                    else
                        return o1.getName().compareTo(o2.getName());
                });
                break;
            case STATUS:
                oweList.sort((o1, o2) -> {
                    int compare;
                    if ((compare = Boolean.compare(o1.isOpen(), o2.isOpen()) * -1) != 0)
                        return compare * (reverse ? -1 : 1);
                    else
                        return o1.getName().compareTo(o2.getName());
                });
                break;
        }
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
            newOwe[0] = owe.clone();
        }
        CustomDialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(owe == null ? "Neue Schulden" : "Schulden Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_owe)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL);

        if (owe != null)
            returnDialog.addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog -> {
                if (!Utility.isOnline(this))
                    return;

                CustomDialog.Builder(this)
                        .setTitle("Löschen")
                        .setText("Willst du wirklich '" + owe.getName() + "' löschen?")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                        .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                            database.oweMap.remove(owe.getUuid());
                            Database.saveAll();
                            reLoadRecycler();
                            customDialog1.dismiss();
                            setResult(RESULT_OK);
                        })
                        .show();
            }, false)
                    .alignPreviousButtonsLeft();

        returnDialog
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String title = ((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_owe_Title)).getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    newOwe[0].setName(title);
                    newOwe[0].setDescription(((EditText) customDialog.findViewById(R.id.dialog_editOrAdd_owe_description)).getText().toString().trim());

                    newOwe[0].setOwnOrOther(((Spinner) customDialog.findViewById(R.id.dialog_editOrAdd_owe_ownOrOther)).getSelectedItemPosition() == 0 ? Owe.OWN_OR_OTHER.OTHER : Owe.OWN_OR_OTHER.OWN);
                  saveOwe(customDialog, newOwe, owe);

                }, false)
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view) -> {
                    new Helpers.TextInputHelper().defaultDialogValidation(customDialog).addValidator(view.findViewById(R.id.dialog_editOrAdd_owe_Title_layout));
                    if (newOwe[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAdd_owe_Title)).setText(newOwe[0].getName());
                        ((EditText) view.findViewById(R.id.dialog_editOrAdd_owe_description)).setText(owe.getDescription());
                        ((TextView) view.findViewById(R.id.dialog_editOrAdd_owe_items)).setText(newOwe[0].getItemList().stream()
                                .map(item -> String.format("%s (%s)", database.personMap.get(item.getPersonId()).getName(), Utility.formatToEuro(item.getAmount())))
                                .collect(Collectors.joining(", ")));
                        view.findViewById(R.id.dialog_editOrAdd_owe_items).setSelected(true);
                        ((Spinner) view.findViewById(R.id.dialog_editOrAdd_owe_ownOrOther)).setSelection(newOwe[0].getOwnOrOther() == Owe.OWN_OR_OTHER.OTHER ? 0 : 1);

                    }
                    else
                        newOwe[0] = new Owe("").setDate(new Date());


                    view.findViewById(R.id.dialog_editOrAdd_owe_editItems).setOnClickListener(view1 ->
                            showItemsDialog(newOwe[0], view.findViewById(R.id.dialog_editOrAdd_owe_items), true));
                })
                .show();
        return returnDialog.getDialog();
    }

    private CustomDialog showDetailDialog(Owe owe) {
        setResult(RESULT_OK);
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Deteil Ansicht")
                .setView(R.layout.dialog_detail_owe)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Bearbeiten", customDialog ->
                        addOrEditDialog[0] = showEditOrNewDialog(owe), false)
//                .addButton("Öffnen mit...", dialog -> openUrl(owe, true), false)
                .setSetViewContent((customDialog, view) -> {
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_title)).setText(owe.getName());
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_description)).setText(owe.getDescription());
                    setItemText(view.findViewById(R.id.dialog_detail_owe_items), owe);
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_ownOrOther)).setText(owe.getOwnOrOther().getName());
                    ((TextView) view.findViewById(R.id.dialog_detail_owe_lastChanged)).setText(new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(owe.getDate()));

                    view.findViewById(R.id.dialog_detail_owe_listItems).setOnClickListener(v -> {
                        showItemsDialog(owe, view.findViewById(R.id.dialog_detail_owe_items),false);
                    });
                })
                .setOnDialogDismiss(customDialog -> detailDialog = null)
                .show();
        return returnDialog;
    }

    private void saveOwe(CustomDialog dialog, Owe[] newOwe, Owe owe) {

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

        if (detailDialog != null)
            detailDialog.reloadView();
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
//                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
//                .addButton("Nochmal", customDialog -> {
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
////                .addButton("Öffnen", customDialog -> openUrl(randomOwe[0], false), false)
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
        subMenu.findItem(R.id.taskBar_owe_filterByOpen)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.OPEN));
        subMenu.findItem(R.id.taskBar_owe_filterByClosed)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.CLOSED));

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
        final ArrayList<String> nameList = database.personMap.values().stream().map(ParentClass::getName).sorted(String::compareTo).collect(Collectors.toCollection(ArrayList::new));
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
                if (changeErrorMessage)
                    textInputLayout.setError("Das Feld darf nicht leer sein!");
                return false;
            } else if (!text.matches("^\\d*(\\.\\d{1,2})?$")) {
                if (changeErrorMessage)
                    textInputLayout.setError("Eine valide Zahl eingeben!");
                return false;

            } else {
                textInputLayout.setError(null);
                return true;
            }

        };
        final Owe.Item[] currentItem = new Owe.Item[]{null};
        CustomDialog sourcesDialog = new CustomDialog(this)
                .setTitle("Einträge")
                .setView(R.layout.dialog_items)
                .setSetViewContent((customDialog, view) -> {
                    LinearLayout dialog_items_editLayout = view.findViewById(R.id.dialog_items_editLayout);
                    TextInputLayout dialog_items_name = view.findViewById(R.id.dialog_items_name);
                    TextInputLayout dialog_items_amount = view.findViewById(R.id.dialog_items_amount);
                    Button dialog_items_save = view.findViewById(R.id.dialog_items_save);
                    dialog_items_amount.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                        boolean handled = false;
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            dialog_items_save.callOnClick();
                            handled = true;
                        }
                        return handled;
                    });
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
                            String BREAKPOINT = null;
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (before < count && s.toString().trim().matches("^\\.$")) {
                                dialog_items_amount.getEditText().setText("0.");
                                dialog_items_amount.getEditText().setSelection(2);
                            } else {
                                validation(nameValidation, urlValidation, dialog_items_name, dialog_items_amount, false, true, dialog_items_save);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
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

                    CustomRecycler sources_customRecycler = new CustomRecycler<Owe.Item>(this, view.findViewById(R.id.dialog_items_sources))
                            .setItemLayout(R.layout.list_item_source_or_item)
                            .setGetActiveObjectList(() -> {
                                List<Owe.Item> sources = owe.getItemList();
                                view.findViewById(R.id.dialog_items_noSources).setVisibility(sources.isEmpty() ? View.VISIBLE : View.GONE);
                                return sources;
                            })
                            .setSetItemContent((itemView, item) -> {
                                ((TextView) itemView.findViewById(R.id.listItem_source_name)).setText(database.personMap.get(item.getPersonId()).getName());
                                ((TextView) itemView.findViewById(R.id.listItem_source_content)).setText(Utility.formatToEuro(item.getAmount()));
                                itemView.findViewById(R.id.listItem_source_check).setVisibility(item.isOpen() ? View.GONE : View.VISIBLE);
                            })
                            .hideDivider()
                            .setOnClickListener((customRecycler, itemView, item, index) -> {
                                item.setOpen(!item.isOpen());
                                Database.saveAll();
                                customRecycler.reload();
                            })
                            .setOnLongClickListener((customRecycler, view1, item, index) -> {
                                Dialog dialog = customDialog.getDialog();
                                dialog.findViewById(R.id.dialog_items_editLayout).setVisibility(View.VISIBLE);
                                dialog.findViewById(buttonId_add).setVisibility(View.GONE);
                                dialog.findViewById(R.id.dialog_items_delete).setVisibility(View.VISIBLE);
                                dialog_items_name.getEditText().setText(database.personMap.get(item.getPersonId()).getName());
                                dialog_items_amount.getEditText().setText(String.valueOf(item.getAmount()));
                                currentItem[0] = item;
                            })
                            .generate();

                    view.findViewById(R.id.dialog_items_delete).setOnClickListener(v -> {
                        CustomDialog.Builder(this)
                                .setTitle("Eintrag Löschen")
                                .setText("Willst du wirklich den Eintrag von '" + database.personMap.get(currentItem[0].getPersonId()).getName() + "' löschen?")
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                                .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
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
                        String amount_text = dialog_items_amount.getEditText().getText().toString().trim();
                        if (currentItem[0] == null) {
                            String personName = dialog_items_name.getEditText().getText().toString().trim();
                            String personId = database.personMap.values().stream().filter(person -> person.getName().equals(personName)).findFirst().get().getUuid();
                            Owe.Item newItem = new Owe.Item(personId, Double.parseDouble(amount_text));
                            owe.getItemList().add(newItem);
                        } else {
                            String personName = dialog_items_name.getEditText().getText().toString().trim();
                            String personId = database.personMap.values().stream().filter(person -> person.getName().equals(personName)).findFirst().get().getUuid();
                            currentItem[0].setPersonId(personId);
                            currentItem[0].setAmount(Double.parseDouble(amount_text));
                        }
                        Database.saveAll();
                        sources_customRecycler.reload();
                        hideEdit.run();
                        currentItem[0] = null;
                    });

                    AutoCompleteTextView autoCompleteName = (AutoCompleteTextView) dialog_items_name.getEditText();
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameList);
                    autoCompleteName.setAdapter(adapter);

                    if (edit) {
                        dialog_items_editLayout.setVisibility(View.VISIBLE);
                        dialog_items_name.getEditText().requestFocus();
                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
                        autoCompleteName.showDropDown();
                    }

                    dialog_items_addNames.setOnClickListener(v -> {
                        CustomDialog.Builder(this)
                                .setTitle("Person Hinzufügen")
                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                                .setEdit(new CustomDialog.EditBuilder().setText(autoCompleteName.getText().toString()).setHint("Personen-Name"))
                                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
                                    String name = customDialog.getEditText().trim();
                                    if (nameList.contains(name)) {
                                        Toast.makeText(this, "Keine zwei Personen dürfen gleich heißen", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Person newPerson = new Person(name);
                                    database.personMap.put(newPerson.getUuid(), newPerson);
                                    Database.saveAll();
                                    nameList.add(name);
                                    adapter.add(name);
                                    customDialog1.dismiss();
                                    validation(nameValidation, urlValidation, dialog_items_name, dialog_items_amount, true, false, dialog_items_save);
                                }, false)
                                .show();
                    });
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Hinzufügen", customDialog -> {
                    customDialog.findViewById(R.id.dialog_items_editLayout).setVisibility(View.VISIBLE);
                    customDialog.findViewById(buttonId_add).setVisibility(View.GONE);
                    customDialog.findViewById(R.id.dialog_items_delete).setVisibility(View.GONE);
                    AutoCompleteTextView dialog_items_name_edit = (AutoCompleteTextView) ((TextInputLayout) customDialog.findViewById(R.id.dialog_items_name)).getEditText();
                    dialog_items_name_edit.requestFocus();
                    Utility.changeWindowKeyboard(this, dialog_items_name_edit, true);
                    dialog_items_name_edit.showDropDown();
                }, buttonId_add, false)
                .addButton("Zurück", customDialog -> {})
                .setObjectExtra(sourcesText)
                .setOnDialogDismiss(customDialog -> {
                    setItemText((TextView) customDialog.getObjectExtra(), owe);
                    reLoadRecycler();
                })
                .show();
        if (edit)
            sourcesDialog.findViewById(buttonId_add).setVisibility(View.GONE);
    }

    private void setItemText(TextView textView, Owe owe) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        owe.getItemList().forEach(item -> {
            String text = String.format("%s (%s)", database.personMap.get(item.getPersonId()).getName(), Utility.formatToEuro(item.getAmount()));
            if (!stringBuilder.toString().isEmpty())
                stringBuilder.append(", ");

            if (item.isOpen())
                stringBuilder.append(text);
            else
                stringBuilder.append(text, new StrikethroughSpan(), Spannable.SPAN_COMPOSING);
        });
        if (stringBuilder.toString().isEmpty())
            stringBuilder.append("<Keine Einträge>", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        textView.setText(stringBuilder);
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
//  <----- Sources -----

    public static void showPopupwindow(AppCompatActivity context, View view) {
        String ownOrOther = "Eigen/Fremd";
        String status = "Status";

        CustomMenu.Builder(context, view.findViewById(R.id.main_owe_filter_label))
                .setMenus((customMenu, items) -> {
                    items.add(new CustomMenu.MenuItem(ownOrOther).setSubMenus(customMenu, (customMenu1, items1) -> {
                        items1.add(new CustomMenu.MenuItem(Owe.OWN_OR_OTHER.OWN.getName(), Owe.OWN_OR_OTHER.OWN));
                        items1.add(new CustomMenu.MenuItem(Owe.OWN_OR_OTHER.OTHER.getName(), Owe.OWN_OR_OTHER.OTHER));
                        customMenu1.setOnClickListener((customRecycler, itemView, item, index) -> {
                            customMenu1.getContext().startActivityForResult(new Intent(customMenu1.getContext(), OweActivity.class).putExtra(EXTRA_OWN_OR_OTHER, (Owe.OWN_OR_OTHER) item.getContent()), MainActivity.START_OWE);
                        });
                        customMenu1.setDynamicSubMenus(items1, (item, subItems) -> {
                            subItems.add(new CustomMenu.MenuItem(item.getName() + " & " + "Offene", new Pair<>(item.getContent(), true)));
                            subItems.add(new CustomMenu.MenuItem(item.getName() + " & " + "Abgeschlosene", new Pair<>(item.getContent(), false)));
                        }, (customRecycler, itemView, item, index) -> {
                            Pair<Owe.OWN_OR_OTHER, Boolean> contentPair = (Pair) item.getContent();
                            context.startActivityForResult(new Intent(context, OweActivity.class).putExtra(EXTRA_OWN_OR_OTHER, contentPair.first)
                                    .putExtra(EXTRA_OPEN, contentPair.second), MainActivity.START_OWE);
                        });
                    }));
                    items.add(new CustomMenu.MenuItem(status).setSubMenus(customMenu, (customMenu1, items1) -> {
                        items1.add(new CustomMenu.MenuItem("Offene", true));
                        items1.add(new CustomMenu.MenuItem("Abgeschlosene", false));
                        customMenu1.setOnClickListener((customRecycler, itemView, item, index) -> {
                            customMenu1.getContext().startActivityForResult(new Intent(customMenu1.getContext(), OweActivity.class).putExtra(EXTRA_OPEN, (Boolean) item.getContent()), MainActivity.START_OWE);
                        });
                        customMenu1.setDynamicSubMenus(items1, (item, subItems) -> {
                            subItems.add(new CustomMenu.MenuItem(item.getName() + " & " + Owe.OWN_OR_OTHER.OWN.getName(), new Pair<>(item.getContent(), Owe.OWN_OR_OTHER.OWN)));
                            subItems.add(new CustomMenu.MenuItem(item.getName() + " & " + Owe.OWN_OR_OTHER.OTHER.getName(), new Pair<>(item.getContent(), Owe.OWN_OR_OTHER.OTHER)));
                        }, (customRecycler, itemView, item, index) -> {
                            Pair<Boolean, Owe.OWN_OR_OTHER> contentPair = (Pair) item.getContent();
                            context.startActivityForResult(new Intent(context, OweActivity.class).putExtra(EXTRA_OWN_OR_OTHER, contentPair.second)
                                    .putExtra(EXTRA_OPEN, contentPair.first), MainActivity.START_OWE);
                        });
                    }));
                })
                .show();
    }
//    public static void showPopupwindow(AppCompatActivity context, View anchor, boolean mip) {
//        String ownOrOther = "Eigen/Fremd";
//        String status = "Status";
//        CustomRecycler baseRecycler = new CustomRecycler<>(context)
//                .setItemLayout(R.layout.popup_standard_list)
//                .setObjectList(Arrays.asList(ownOrOther, status))
//                .hideDivider()
//                .hideOverscroll()
//                .setSetItemContent((itemView0, o0) -> {
//                    ((TextView) itemView0.findViewById(R.id.popup_standardList_text)).setText(o0.toString());
//                    View popup_standardList_sub = itemView0.findViewById(R.id.popup_standardList_sub);
//                    popup_standardList_sub.setVisibility(View.VISIBLE);
//                    popup_standardList_sub.setClickable(false);
//                    popup_standardList_sub.setForeground(null);
//                })
//                .setOnClickListener((customRecycler0, itemView0, o0, index0) -> {
//                    CustomRecycler recycler = new CustomRecycler<>(context)
//                            .setItemLayout(R.layout.popup_standard_list);
//                    if (o0.equals(ownOrOther))
//                        recycler.setObjectList(Arrays.asList(Owe.OWN_OR_OTHER.OWN, Owe.OWN_OR_OTHER.OTHER));
//                    else
//                        recycler.setObjectList(Arrays.asList(true, false));
//
//                    recycler
//                            .hideDivider()
//                            .hideOverscroll()
//                            .setSetItemContent((itemView, o) -> {
//                                if (o instanceof Owe.OWN_OR_OTHER)
//                                    ((TextView) itemView.findViewById(R.id.popup_standardList_text)).setText(((Owe.OWN_OR_OTHER) o).getName());
//                                else
//                                    ((TextView) itemView.findViewById(R.id.popup_standardList_text)).setText((boolean) o ? "Offene" : "Abgeschlosene");
//                                itemView.findViewById(R.id.popup_standardList_sub).setVisibility(View.VISIBLE);
//                                itemView.findViewById(R.id.popup_standardList_divider).setVisibility(View.VISIBLE);
//                            })
//                            .setOnClickListener((customRecycler, itemView, o, index) -> {
//                                if (o instanceof Owe.OWN_OR_OTHER)
//                                    context.startActivityForResult(new Intent(context, OweActivity.class).putExtra(EXTRA_OWN_OR_OTHER, (Owe.OWN_OR_OTHER) o), MainActivity.START_OWE);
//                                else
//                                    context.startActivityForResult(new Intent(context, OweActivity.class).putExtra(EXTRA_OPEN, (Boolean) o), MainActivity.START_OWE);
//                            })
//                            .addSubOnClickListener(R.id.popup_standardList_sub, (customRecycler, itemView, o, index) -> {
//                                String name;
//                                if (o instanceof Owe.OWN_OR_OTHER)
//                                    name = ((Owe.OWN_OR_OTHER) o).getName();
//                                else
//                                    name = (boolean) o ? "Offene" : "Abgeschlosene";
//                                CustomRecycler subRecycler = new CustomRecycler<>(context)
//                                        .setItemLayout(R.layout.popup_standard_list);
//
//                                if (o instanceof Owe.OWN_OR_OTHER)
//                                    subRecycler.setObjectList(Arrays.asList(true, false));
//                                else
//                                    subRecycler.setObjectList(Arrays.asList(Owe.OWN_OR_OTHER.OWN, Owe.OWN_OR_OTHER.OTHER));
//
//                                subRecycler
//                                        .hideDivider()
//                                        .hideOverscroll()
//                                        .setSetItemContent((itemView2, o2) -> {
//                                            if (o instanceof Owe.OWN_OR_OTHER)
//                                                ((TextView) itemView2.findViewById(R.id.popup_standardList_text)).setText(String.format("%s & %s", name, (boolean) o2 ? "Offene" : "Abgeschlosene"));
//                                            else
//                                                ((TextView) itemView2.findViewById(R.id.popup_standardList_text)).setText(String.format("%s & %s", name, ((Owe.OWN_OR_OTHER) o2).getName()));
//                                        })
//                                        .setOnClickListener((customRecycler2, itemView2, o2, index2) -> {
//                                            if (o instanceof Owe.OWN_OR_OTHER)
//                                                context.startActivityForResult(new Intent(context, OweActivity.class).putExtra(EXTRA_OWN_OR_OTHER, (Owe.OWN_OR_OTHER) o)
//                                                    .putExtra(EXTRA_OPEN, Boolean.valueOf((boolean) o2)), MainActivity.START_OWE);
//                                            else
//                                                context.startActivityForResult(new Intent(context, OweActivity.class).putExtra(EXTRA_OWN_OR_OTHER, (Owe.OWN_OR_OTHER) o2)
//                                                    .putExtra(EXTRA_OPEN, Boolean.valueOf((boolean) o)), MainActivity.START_OWE);
//                                        });
//
//                                Utility.showPopupWindow(context, itemView, subRecycler.generateRecyclerView());
//
//                            }, false);
//                            Utility.showPopupWindow(context, itemView0, recycler.generateRecyclerView());
//
//                });
//
//        Utility.showPopupWindow(context, anchor.findViewById(R.id.main_owe_filter_label), baseRecycler.generateRecyclerView());
//    }

    public static void showTradeOffDialog(AppCompatActivity activity, View view) {
        Database database = Database.getInstance();
        List<Utility.Triple<Person, Double, Double>> list = new ArrayList<>();
        for (Person person : database.personMap.values()) {
            final double[] allOwn = {0};
            final double[] allOther = {0};
            database.oweMap.values().forEach(owe -> owe.getItemList().forEach(item -> {
                if (item.isOpen()) {
                    if (item.getPersonId().equals(person.getUuid())) {
                        if (owe.getOwnOrOther() == Owe.OWN_OR_OTHER.OWN)
                            allOwn[0] += item.getAmount();
                        else
                            allOther[0] += item.getAmount();
                    }
                }
            }));
            if (allOwn[0] != 0 && allOther[0] != 0) {
                list.add(new Utility.Triple<>(person, allOwn[0], allOther[0]));
            }
        }

        if (list.isEmpty()) {
            Toast.makeText(activity, "Es gibt nix zum Ausgleichen", Toast.LENGTH_SHORT).show();
            return;
        }
        CustomDialog tradeOff_customDialog = CustomDialog.Builder(activity);
        CustomRecycler tradeOff_customRecycler = new CustomRecycler<Utility.Triple<Person, Double, Double>>(activity)
                .setItemLayout(R.layout.list_item_trade_off)
                .setObjectList(list)
                .hideDivider()
                .setSetItemContent((itemView, triple) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_tradeOff_name)).setText(triple.first.getName());
                    ((TextView) itemView.findViewById(R.id.listItem_tradeOff_own)).setText(Utility.formatToEuro(triple.second));
                    ((TextView) itemView.findViewById(R.id.listItem_tradeOff_other)).setText(Utility.formatToEuro(triple.third));

                    TextView listItem_tradeOff_difference = itemView.findViewById(R.id.listItem_tradeOff_difference);
                    listItem_tradeOff_difference.setText(Utility.formatToEuro(
                            Math.abs(triple.second - triple.third)));
                    listItem_tradeOff_difference.setTextColor(triple.second > triple.third ? Color.RED : activity.getColor(R.color.colorGreen));
                })
                .setOnClickListener((customRecycler, itemView, triple, index) -> {
                    double difference = triple.second - triple.third;
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    CustomDialog.Builder(activity)
                            .setTitle("Ausgleich Erstellen")
                            .setText(builder.append("Einen Ausgleich für '").append(triple.first.getName(), new StyleSpan(Typeface.BOLD), Spannable.SPAN_COMPOSING).append("' anlegen?")
                                    .append("\nRestbetrag: ").append(Utility.formatToEuro(Math.abs(difference))
                                            , (difference == 0 ? null : new ForegroundColorSpan(difference < 0 ? activity.getColor(R.color.colorGreen) : Color.RED )), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                                    .append(difference < 0 ? " (Fremd)" : difference > 0 ? " (Eigen)" : "", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_COMPOSING))
                            .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                            .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                                Owe newOwe = new Owe("Ausgleich: " + triple.first.getName())
                                        .setDate(new Date())
                                        .setDescription("Eigene: " + Utility.formatToEuro(triple.second) + "\nFremde: " + Utility.formatToEuro(triple.third)
                                                + "\nDifferenz: " + Utility.formatToEuro(difference))
                                        .setOwnOrOther(difference > 0 ? Owe.OWN_OR_OTHER.OWN : Owe.OWN_OR_OTHER.OTHER)
                                        .setItemList(Arrays.asList(new Owe.Item(triple.first.getUuid(), Math.abs(difference))));

                                database.oweMap.values().forEach(owe -> owe.getItemList().forEach(item -> {
                                    if (item.isOpen() && item.getPersonId().equals(triple.first.getUuid()))
                                        item.setOpen(false);
                                }));
                                database.oweMap.put(newOwe.getUuid(), newOwe);
                                Database.saveAll();

                                list.remove(triple);
                                if (list.isEmpty())
                                    tradeOff_customDialog.getDialog().dismiss();
                                else
                                    customRecycler.reload();
                                Toast.makeText(activity, "Ausgleich erstellt", Toast.LENGTH_SHORT).show();
                                MainActivity.setCounts();
                            })
                            .show();
                });

        tradeOff_customDialog
                .setTitle("Ausgleiche Verfügbar Für")
                .setView(tradeOff_customRecycler.generateRecyclerView())
                .show();
//        Utility.showPopupWindow(activity, view.findViewById(R.id.main_owe_tradeOff_label), tradeOff_customRecycler);
    }

}
