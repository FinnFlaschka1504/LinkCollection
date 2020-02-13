package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finn.androidUtilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.R;
import com.finn.androidUtilities.CustomDialog;
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
import java.util.stream.Collectors;

public class JokeActivity extends AppCompatActivity {

    enum SORT_TYPE{
        NAME, RATING, LATEST
    }

    public enum FILTER_TYPE{
        NAME("Aufbau"), CATEGORY("Kategorie"), PUNCHLINE("Punch-Line");

        String name;

        FILTER_TYPE() {
        }

        FILTER_TYPE(String name) {
            this.name = name;
        }

        public boolean hasName() {
            return  name != null;
        }

        public String getName() {
            return name;
        }
    }


    private boolean reverse;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    Database database = Database.getInstance();
    private CustomRecycler<CustomRecycler.Expandable<Joke>> customRecycler_List;
    private CustomDialog[] addOrEditDialog = new CustomDialog[]{null};
    private String searchQuery = "";
    private SharedPreferences mySPR_daten;
    private SearchView.OnQueryTextListener textListener;
    private SearchView joke_search;
    private ArrayList<Joke> allJokeList;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.CATEGORY, FILTER_TYPE.PUNCHLINE));
    private CustomDialog detailDialog;
    private boolean isDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!(isDialog = Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHOW_AS_DIALOG)))
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_joke);

        Settings.startSettings_ifNeeded(this);
        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();
    }


    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
            setContentView(R.layout.activity_joke);
            loadRecycler();

            joke_search = findViewById(R.id.search);
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
            joke_search.setOnQueryTextListener(textListener);

            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHORTCUT))
                addOrEditDialog[0] = showEditOrNewDialog(null);

//            findViewById(R.id.importJokes).setOnClickListener(v -> {
//                CustomDialog.Builder(this)
//                        .setTitle("Videos Importieren")
//                        .setEdit(new CustomDialog.EditBuilder().setHint("Witze einfügen"))
//                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
//                        .addButton(CustomDialog.OK_BUTTON, (customDialog, dialog) -> {
//                            String text = customDialog.getEditText();
//                            List<Pair<String,String>> witzeList = Arrays.stream(text.split("-{2,}")).map(s -> {
//                                String[] parts = s.split("\\?|-|\\.{2,3}", 2);
//                                return new Pair<>(parts[0].trim(), parts.length > 1 ? parts[1].trim() : null);
//                            }).collect(Collectors.toList());
//                            for (Pair<String, String> pair : witzeList) {
//                                Joke newJoke = new Joke(pair.first);
//                                newJoke.setPunchLine(pair.second);
//                                newJoke.setAddedDate(new Date());
//                                database.jokeMap.put(newJoke.getUuid(), newJoke);
//                            }
//                            Database.saveAll();
//                            reLoadRecycler();
//                        })
//                        .show();
//            });
            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {
                filterTypeSet.clear();

                switch (extraSearchCategory) {
                    case JOKE_CATEGORIES:
                        filterTypeSet.add(FILTER_TYPE.CATEGORY);
                        break;
                }

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
                    joke_search.setQuery(extraSearch, true);
                }
            }
            setSearchHint();

            if (isDialog) {
                findViewById(R.id.recycler).setVisibility(View.GONE);
                joke_search.setVisibility(View.GONE);
                findViewById(R.id.divider).setVisibility(View.GONE);
                if (getIntent().getBooleanExtra(MainActivity.EXTRA_SHOW_RANDOM, false))
                    showRandomDialog();
            }
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
        customRecycler_List = new CustomRecycler<CustomRecycler.Expandable<Joke>>(this, findViewById(R.id.recycler))
                .setGetActiveObjectList(customRecycler -> {
                    List<CustomRecycler.Expandable<Joke>> filteredList;
                    if (searchQuery.equals("")) {
                        allJokeList = new ArrayList<>(database.jokeMap.values());
                        filteredList = toExpandableList(sortList(allJokeList));
                    }
                    else
                        filteredList = toExpandableList(filterList(allJokeList));

                    TextView noItem = findViewById(R.id.no_item);
                    noItem.setText(searchQuery.isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche");
                    noItem.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
                    return filteredList;

                })

                .setExpandableHelper(customRecycler -> customRecycler.new ExpandableHelper<Joke>(R.layout.list_item_joke, (customRecycler1, itemView, joke, expanded) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_joke_title_label)).setText(joke.getPunchLine() == null || joke.getPunchLine().isEmpty() ? "Witz:" : "Aufbau:");
                    itemView.findViewById(R.id.listItem_joke_punchLine_layout).setVisibility(joke.getPunchLine() == null || joke.getPunchLine().isEmpty() ? View.GONE : View.VISIBLE);

                    TextView listItem_joke_title = itemView.findViewById(R.id.listItem_joke_title);
                    TextView listItem_joke_punchLine = itemView.findViewById(R.id.listItem_joke_punchLine);

                    listItem_joke_title.setText(joke.getName());
                    listItem_joke_punchLine.setText(joke.getPunchLine());


                    ((TextView) itemView.findViewById(R.id.listItem_joke_categories)).setText(String.join(", ",
                            joke.getCategoryIdList().stream().map(categoryId -> database.jokeCategoryMap.get(categoryId).getName()).collect(Collectors.toList())));
                    itemView.findViewById(R.id.listItem_joke_categories).setSelected(true);

                    if (joke.getRating() > 0) {
                        itemView.findViewById(R.id.listItem_joke_rating_layout).setVisibility(View.VISIBLE);
                        ((TextView) itemView.findViewById(R.id.listItem_joke_rating)).setText(String.valueOf(joke.getRating()));
                    }
                    else
                        itemView.findViewById(R.id.listItem_joke_rating_layout).setVisibility(View.GONE);


                    if (expanded) {
                        listItem_joke_title.setMaxLines(Integer.MAX_VALUE);
                        listItem_joke_punchLine.setSingleLine(false);
                    } else {
                        listItem_joke_title.setMaxLines(2);
                        listItem_joke_punchLine.setSingleLine(true);

                    }

                }))

//                .setSetItemContent((itemView, joke) -> {
//                })
//                .setOnClickListener((customRecycler, view, object, index) -> {
////                  openUrl(object, false);
//                })


                .addSubOnClickListener(R.id.listItem_joke_details, (customRecycler, view, jokeExpandable, index) -> detailDialog = showDetailDialog(jokeExpandable.getObject()), false)
                .setOnLongClickListener((customRecycler, view, jokeExpandable, index) -> addOrEditDialog[0] = showEditOrNewDialog(jokeExpandable.getObject()))
                .generate();
    }

    private List<CustomRecycler.Expandable<Joke>> toExpandableList(List<Joke> jokeList) {
        return new CustomRecycler.Expandable.ToExpandableList<Joke,Joke>().keepExpandedState(customRecycler_List).runToExpandableList(jokeList, null);
    }

    private List<Joke> filterList(ArrayList<Joke> allJokeList) {
        CustomList<Joke> customList = new CustomList<>(allJokeList);

        if (!searchQuery.isEmpty()) {
            if (searchQuery.contains("|"))
                customList.filterOr(searchQuery.split("\\|"), (joke, s) -> Utility.containedInJoke(s.trim(), joke, filterTypeSet), true);
            else
                customList.filterAnd(searchQuery.split("&"), (joke, s) -> Utility.containedInJoke(s.trim(), joke, filterTypeSet), true);
        }
        return sortList(customList);

//        return sortList(allJokeList.stream().filter(joke -> Utility.containedInJoke(searchQuery, joke, filterTypeSet)).collect(Collectors.toList()));
    }

    private List<Joke> sortList(List<Joke> jokeList) {
        switch (sort_type) {
            case NAME:
                jokeList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
                break;
            case RATING:
                jokeList.sort((o1, o2) -> o1.getRating().compareTo(o2.getRating()) * -1);
                break;
            case LATEST:
                jokeList.sort((o1, o2) -> o1.getAddedDate().compareTo(o2.getAddedDate()) * -1);
                break;
        }
        if (reverse)
            Collections.reverse(jokeList);
        return jokeList;
    }

    private void reLoadRecycler() {
        customRecycler_List.reload();
    }

    private CustomDialog showEditOrNewDialog(Joke joke) {
        if (!Utility.isOnline(this))
            return null;
        setResult(RESULT_OK);
        removeFocusFromSearch();

        final Joke[] newJoke = {null};
        List<String> categoriesNames = new ArrayList<>();
        if (joke != null) {
            newJoke[0] = joke.clone();
            newJoke[0].getCategoryIdList().forEach(uuid -> categoriesNames.add(database.jokeCategoryMap.get(uuid).getName()));
        }
        CustomDialog returnDialog =  CustomDialog.Builder(this)
                .setTitle(joke == null ? "Neuer Witz" : "Witz Bearbeiten")
                .setView(R.layout.dialog_edit_or_add_joke)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL);

        if (joke != null)
            returnDialog.addButton(R.drawable.ic_delete, customDialog -> {
                CustomDialog.Builder(this)
                        .setTitle("Löschen")
                        .setText("Willst du wirklich '" + joke.getName() + "' löschen?")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                            database.jokeMap.remove(joke.getUuid());
                            Database.saveAll();
                            reLoadRecycler();
                            returnDialog.getDialog().dismiss();
                        })
                        .show();
            }, false)
                    .alignPreviousButtonsLeft();

        returnDialog
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    String titel = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddJoke_Titel)).getText().toString().trim();
                    if (titel.isEmpty()) {
                        Toast.makeText(this, "Einen Titel eingeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String content = ((EditText) customDialog.findViewById(R.id.dialog_editOrAddJoke_punchLine)).getText().toString().trim();
                    saveJoke(customDialog, titel, content, newJoke, joke);

                }, false)
                .disableLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    new Helpers.TextInputHelper().defaultDialogValidation(customDialog).addValidator(view.findViewById(R.id.dialog_editOrAddJoke_titel_layout));

                    Helpers.RatingHelper ratingHelper = new Helpers.RatingHelper(view.findViewById(R.id.customRating_layout));

                    if (newJoke[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAddJoke_Titel)).setText(newJoke[0].getName());
                        ((EditText) view.findViewById(R.id.dialog_editOrAddJoke_punchLine)).setText(joke.getPunchLine());
                        ((TextView) view.findViewById(R.id.dialog_editOrAddJoke_categories)).setText(String.join(", ", categoriesNames));
                        view.findViewById(R.id.dialog_editOrAddJoke_categories).setSelected(true);
                        ratingHelper.setRating(newJoke[0].getRating());
                    }
                    else
                        newJoke[0] = new Joke("").setAddedDate(new Date());


                    view.findViewById(R.id.dialog_editOrAddJoke_editCategories).setOnClickListener(view1 ->
                            Utility.showEditItemDialog(this, addOrEditDialog[0], newJoke[0].getCategoryIdList(), newJoke[0], CategoriesActivity.CATEGORIES.JOKE_CATEGORIES));
                })
                .show();
        return returnDialog;
    }

    private CustomDialog showDetailDialog(Joke joke) {
        setResult(RESULT_OK);
        CustomDialog returnDialog = CustomDialog.Builder(this)
                .setTitle("Detail Ansicht")
                .setView(R.layout.dialog_detail_joke)
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("Bearbeiten", customDialog -> addOrEditDialog[0] = showEditOrNewDialog(joke), false)
                .setSetViewContent((customDialog, view, reload) -> {
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_title_label)).setText(joke.getPunchLine() == null || joke.getPunchLine().isEmpty() ? "Witz:" : "Aufbau:");
                    view.findViewById(R.id.dialog_detailJoke_punchLine_layout).setVisibility(joke.getPunchLine() == null || joke.getPunchLine().isEmpty() ? View.GONE : View.VISIBLE);
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_title)).setText(joke.getName());
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_punchLine)).setText(joke.getPunchLine());
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_categories)).setText(
                            joke.getCategoryIdList().stream().map(uuid -> database.jokeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_addedDate)).setText(String.format("%s Uhr", new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(joke.getAddedDate())));
                    view.findViewById(R.id.dialog_detailJoke_details).setVisibility(View.VISIBLE);
                    ((RatingBar) view.findViewById(R.id.dialog_detailJoke_rating)).setRating(joke.getRating());
                })
                .setOnDialogDismiss(customDialog -> {
                    detailDialog = null;
                    Utility.ifNotNull(customDialog.getPayload(), o -> ((com.finn.androidUtilities.CustomDialog) o).reloadView());
                });
        returnDialog.show();
        return returnDialog;
    }

    private void saveJoke(CustomDialog customDialog, String titel, String punchLine, Joke[] newJoke, Joke joke) {

        if (joke == null)
            joke = newJoke[0];

        joke.setName(titel);
        joke.setPunchLine(punchLine);
        joke.setCategoryIdList(newJoke[0].getCategoryIdList());
        joke.setRating(((RatingBar) customDialog.findViewById(R.id.customRating_ratingBar)).getRating());

        database.jokeMap.put(joke.getUuid(), joke);
//        customRecycler_List.reload(customRecycler_List.getObjectList().indexOf(joke));
        reLoadRecycler();
        customDialog.dismiss();

        Database.saveAll();

        Utility.showCenteredToast(this, "Witz gespeichert");

        if (detailDialog != null)
            detailDialog.reloadView();
    }

    private void showRandomDialog() {
        removeFocusFromSearch();
        CustomList<Joke> randomJokeList = new CustomList<>(filterList(allJokeList));
        if (randomJokeList.isEmpty()) {
            Toast.makeText(this, "Nichts zum Zeigen", Toast.LENGTH_SHORT).show();
            return;
        }
        final Joke[] randomJoke = {randomJokeList.removeRandom()};
        int ratingButtonId = View.generateViewId();

        CustomDialog.Builder(this)
                .setTitle("Zufälliger Witz")
                .setView(R.layout.dialog_detail_joke)
                .addButton(R.drawable.ic_info, customDialog -> detailDialog = showDetailDialog(randomJoke[0]).setPayload(customDialog), false)
                .alignPreviousButtonsLeft()
                .addButton("", customDialog -> {
                    ParentClass_Ratable.showRatingDialog(this, randomJoke[0], customDialog.getButton(ratingButtonId).getButton(), true, () -> {
                        Database.saveAll();
                        customDialog.reloadView();
                        reLoadRecycler();
                    });
                }, ratingButtonId, false)
                .addButton("Nochmal", customDialog -> {
                    if (randomJokeList.isEmpty()) {
                        Toast.makeText(this, "Kein neuer Witz vorhanden", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, "Neu", Toast.LENGTH_SHORT).show();
                    randomJoke[0] = randomJokeList.removeRandom();
//                    ((TextView) customDialog.findViewById(R.id.dialog_detailJoke_title_label)).setName(randomJoke[0].getPunchLine() == null || randomJoke[0].getPunchLine().isEmpty() ? "Witz:" : "Aufbau:");
//                    customDialog.findViewById(R.id.dialog_detailJoke_punchLine_layout).setVisibility(randomJoke[0].getPunchLine() == null || randomJoke[0].getPunchLine().isEmpty() ? View.GONE : View.VISIBLE);
//
//                    ((TextView) customDialog.findViewById(R.id.dialog_detailJoke_title)).setName(randomJoke[0].getName());
//                    ((TextView) customDialog.findViewById(R.id.dialog_detailJoke_punchLine)).setName(randomJoke[0].getPunchLine());
//
//                    ((TextView) customDialog.findViewById(R.id.dialog_detailJoke_categories)).setName(
//                            randomJoke[0].getCategoryIdList().stream().map(uuid -> database.jokeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    customDialog.reloadView();
                }, false)
                .colorLastAddedButton()
                .setSetViewContent((customDialog, view, reload) -> {
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_title_label)).setText(randomJoke[0].getPunchLine() == null || randomJoke[0].getPunchLine().isEmpty() ? "Witz:" : "Aufbau:");
                    view.findViewById(R.id.dialog_detailJoke_punchLine_layout).setVisibility(randomJoke[0].getPunchLine() == null || randomJoke[0].getPunchLine().isEmpty() ? View.GONE : View.VISIBLE);

                    ((TextView) view.findViewById(R.id.dialog_detailJoke_title)).setText(randomJoke[0].getName());
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_punchLine)).setText(randomJoke[0].getPunchLine());
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_categories)).setText(
                            randomJoke[0].getCategoryIdList().stream().map(uuid -> database.jokeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", ")));

                    ((Button) customDialog.getButton(ratingButtonId).getButton()).setText(randomJoke[0].getRating() != -1f && randomJoke[0].getRating() != -0f ? randomJoke[0].getRating() + " ☆" : "☆");

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
        getMenuInflater().inflate(R.menu.task_bar_joke, menu);

        Menu subMenu = menu.findItem(R.id.taskBar_filter).getSubMenu();
        subMenu.findItem(R.id.taskBar_joke_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        subMenu.findItem(R.id.taskBar_joke_filterByCategory)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.CATEGORY));
        subMenu.findItem(R.id.taskBar_joke_filterByContent)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.PUNCHLINE));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_joke_add:
                addOrEditDialog[0] = showEditOrNewDialog(null);
                break;
            case R.id.taskBar_joke_random:
                showRandomDialog();
                break;

            case R.id.taskBar_joke_sortByName:
                sort_type = SORT_TYPE.NAME;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_joke_sortByRating:
                sort_type = SORT_TYPE.RATING;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_joke_sortByLatest:
                sort_type = SORT_TYPE.LATEST;
                item.setChecked(true);
                reLoadRecycler();
                break;
            case R.id.taskBar_joke_sortReverse:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                reverse = checked;
                reLoadRecycler();
                break;

            case R.id.taskBar_joke_filterByName:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.NAME);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.NAME);
                    item.setChecked(true);
                }
                reLoadRecycler();
                setSearchHint();
                break;
            case R.id.taskBar_joke_filterByCategory:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.CATEGORY);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.CATEGORY);
                    item.setChecked(true);
                }
                reLoadRecycler();
                setSearchHint();
                break;
            case R.id.taskBar_joke_filterByContent:
                if (item.isChecked()) {
                    filterTypeSet.remove(FILTER_TYPE.PUNCHLINE);
                    item.setChecked(false);
                } else {
                    filterTypeSet.add(FILTER_TYPE.PUNCHLINE);
                    item.setChecked(true);
                }
                reLoadRecycler();
                setSearchHint();
                break;

            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }

    private void removeFocusFromSearch() {
        joke_search.clearFocus();
    }

    private void setSearchHint() {
        String join = filterTypeSet.stream().filter(FILTER_TYPE::hasName).sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).map(FILTER_TYPE::getName).collect(Collectors.joining(", "));
        joke_search.setQueryHint(join.isEmpty() ? "Kein Filter ausgewählt!" : join + " ('&' als 'und'; '|' als 'oder')");
        Utility.applyToAllViews(joke_search, View.class, view -> view.setEnabled(!join.isEmpty()));
    }
}
