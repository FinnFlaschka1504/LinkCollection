package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finn.androidUtilities.CustomRecycler;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.Joke;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomDialog;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomPopupWindow;
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
        NAME, CATEGORY, PUNCHLINE
    }


    private boolean reverse;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    Database database = Database.getInstance();
    private CustomRecycler<CustomRecycler.Expandable<Joke>> customRecycler_List;
    private CustomDialog[] addOrEditDialog = new CustomDialog[]{null};
    private String searchQuery = "";
    private SharedPreferences mySPR_daten;
    private SearchView.OnQueryTextListener textListener;
    private SearchView videos_search;
    private ArrayList<Joke> allJokeList;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.CATEGORY, FILTER_TYPE.PUNCHLINE));
    private CustomDialog detailDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joke);

        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();

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
                videos_search.setQuery(extraSearch, true);
            }
        }

    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
            setContentView(R.layout.activity_joke);
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
                    if (searchQuery.equals("")) {
                        allJokeList = new ArrayList<>(database.jokeMap.values());
                        return toExpandableList(sortList(allJokeList));
                    }
                    else
                        return toExpandableList(filterList(allJokeList));
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
        return sortList(allJokeList.stream().filter(joke -> Utility.containedInJoke(searchQuery, joke, filterTypeSet)).collect(Collectors.toList()));
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
            returnDialog.addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog -> {
                CustomDialog.Builder(this)
                        .setTitle("Löschen")
                        .setText("Willst du wirklich '" + joke.getName() + "' löschen?")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                        .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog1 -> {
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
                    if (newJoke[0] != null) {
                        ((EditText) view.findViewById(R.id.dialog_editOrAddJoke_Titel)).setText(newJoke[0].getName());
                        ((EditText) view.findViewById(R.id.dialog_editOrAddJoke_punchLine)).setText(joke.getPunchLine());
                        ((TextView) view.findViewById(R.id.dialog_editOrAddJoke_categories)).setText(String.join(", ", categoriesNames));
                        view.findViewById(R.id.dialog_editOrAddJoke_categories).setSelected(true);
                        ((RatingBar) view.findViewById(R.id.dialog_editOrAddJoke_rating)).setRating(newJoke[0].getRating());
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
                .setOnDialogDismiss(customDialog -> detailDialog = null);
        returnDialog.show();
        return returnDialog;
    }

    private void saveJoke(CustomDialog customDialog, String titel, String punchLine, Joke[] newJoke, Joke joke) {

        if (joke == null)
            joke = newJoke[0];

        joke.setName(titel);
        joke.setPunchLine(punchLine);
        joke.setCategoryIdList(newJoke[0].getCategoryIdList());
        joke.setRating(((RatingBar) customDialog.findViewById(R.id.dialog_editOrAddJoke_rating)).getRating());

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
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
                .addButton("", customDialog -> {
                    RatingBar ratingBar = new RatingBar(this);
                    ratingBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    ratingBar.setMax(5);
                    ratingBar.setStepSize(0.5f);
                    ratingBar.setNumStars(5);
                    ratingBar.setRating(randomJoke[0].getRating());
                    ratingBar.setBackground(getDrawable(R.drawable.tile_background));
                    CustomPopupWindow customPopupWindow = CustomPopupWindow.Builder(customDialog.getButton(ratingButtonId).getButton(), ratingBar).setPositionRelativeToAnchor(CustomPopupWindow.POSITION_RELATIVE_TO_ANCHOR.TOP).show();
                    ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
                        randomJoke[0].setRating(rating);
                        Database.saveAll();
                        customDialog.reloadView();
                        reLoadRecycler();
                        customPopupWindow.dismiss();
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
                .setSetViewContent((customDialog, view, reload) -> {
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_title_label)).setText(randomJoke[0].getPunchLine() == null || randomJoke[0].getPunchLine().isEmpty() ? "Witz:" : "Aufbau:");
                    view.findViewById(R.id.dialog_detailJoke_punchLine_layout).setVisibility(randomJoke[0].getPunchLine() == null || randomJoke[0].getPunchLine().isEmpty() ? View.GONE : View.VISIBLE);

                    ((TextView) view.findViewById(R.id.dialog_detailJoke_title)).setText(randomJoke[0].getName());
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_punchLine)).setText(randomJoke[0].getPunchLine());
                    ((TextView) view.findViewById(R.id.dialog_detailJoke_categories)).setText(
                            randomJoke[0].getCategoryIdList().stream().map(uuid -> database.jokeCategoryMap.get(uuid).getName()).collect(Collectors.joining(", ")));

                    customDialog.getButton(ratingButtonId).getButton().setText(randomJoke[0].getRating() != -1f && randomJoke[0].getRating() != -0f ? randomJoke[0].getRating() + " ☆" : "☆");

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
                break;

            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }

    //  ----- Sources ----->
//    private void showSourcesDialog(Joke joke, TextView sourcesText, boolean edit) {
//        int buttonId_add = View.generateViewId();
//        TextValidation nameValidation = (textInputLayout, changeErrorMessage) -> {
//            String text = textInputLayout.getEditText().getName().toString().trim();
//
//            if (text.isEmpty()) {
//                if (changeErrorMessage)
//                    textInputLayout.setError("Das Feld darf nicht leer sein!");
//                return false;
//            } else {
//                if (changeErrorMessage)
//                    textInputLayout.setError(null);
//                return true;
//            }
//        };
//        TextValidation urlValidation = (textInputLayout, changeErrorMessage) -> {
//            String text = textInputLayout.getEditText().getName().toString().trim();
//
//            if (text.isEmpty()) {
//                textInputLayout.setError("Das Feld darf nicht leer sein!");
//                return false;
//            } else if (!text.matches("(?i)^(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$")) {
//                textInputLayout.setError("Eine valide URL eingeben!");
//                return false;
//
//            } else {
//                textInputLayout.setError(null);
//                return true;
//            }
//
//        };
//        final List<String>[] currentSource = new List[]{null};
//        Dialog sourcesDialog = CustomDialog.Builder(this)
//                .setTitle("Quellen")
//                .setView(R.layout.dialog_sources)
//                .setSetViewContent((customDialog, view) -> {
//                    LinearLayout dialog_sources_editLayout = view.findViewById(R.id.dialog_sources_editLayout);
//                    TextInputLayout dialog_sources_name = view.findViewById(R.id.dialog_sources_name);
//                    TextInputLayout dialog_sources_url = view.findViewById(R.id.dialog_sources_url);
//                    if (edit) {
//                        dialog_sources_editLayout.setVisibility(View.VISIBLE);
//                        dialog_sources_url.getEditText().requestFocus();
//                        Utility.changeWindowKeyboard(customDialog.getDialog().getWindow(), true);
//                    }
//                    Button dialog_sources_save = view.findViewById(R.id.dialog_sources_save);
//                    dialog_sources_name.getEditText().addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            validation(nameValidation, urlValidation, dialog_sources_name, dialog_sources_url, true, false, dialog_sources_save);
//                        }
//                    });
//                    dialog_sources_url.getEditText().addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            if (urlValidation.runTextValidation(dialog_sources_url, true)) {
//                                if (dialog_sources_name.getEditText().getName().toString().isEmpty()) {
//                                    String domainName = getDomainFromUrl(s.toString(), true);
//                                    dialog_sources_name.getEditText().setName(domainName);
//                                }
//                            }
//                            validation(nameValidation, urlValidation, dialog_sources_name, dialog_sources_url, false, true, dialog_sources_save);
//                        }
//                    });
//                    Runnable hideEdit = () -> {
//                        dialog_sources_name.getEditText().setName("");
//                        dialog_sources_url.getEditText().setName("");
//                        dialog_sources_name.setError(null);
//                        dialog_sources_url.setError(null);
//                        dialog_sources_editLayout.setVisibility(View.GONE);
//                        Utility.changeWindowKeyboard(this, dialog_sources_url.getEditText(), false);
//                        dialog_sources_url.getEditText().clearFocus();
//                        customDialog.getDialog().findViewById(buttonId_add).setVisibility(View.VISIBLE);
//                        currentSource[0] = null;
//                    };
//
//
//                    view.findViewById(R.id.dialog_sources_cancel).setOnClickListener(v -> hideEdit.run());
//
//                    CustomRecycler sources_customRecycler = new CustomRecycler<>(this, view.findViewById(R.id.dialog_sources_sources))
//                            .setItemLayout(R.layout.list_item_source_or_item)
//                            .setGetActiveObjectList(() -> {
////                                List<List<String>> sources = joke.getSources();
//                                view.findViewById(R.id.dialog_sources_noSources).setVisibility(sources.isEmpty() ? View.VISIBLE : View.GONE);
//                                return sources;
//                            })
//                            .setSetItemContent((CustomRecycler.SetItemContent<List<String>>)(itemView, nameUrlPair) -> {
//                                ((TextView) itemView.findViewById(R.id.listItem_source_name)).setName(nameUrlPair.get(0));
//                                ((TextView) itemView.findViewById(R.id.listItem_source_content)).setName(nameUrlPair.get(1));
//                            })
//                            .hideDivider()
//                            .useCustomRipple()
//                            .setOnClickListener((customRecycler, itemView, o, index) -> Utility.openUrl(this, ((List<String>) o).get(1), false))
//                            .setOnLongClickListener((CustomRecycler.OnLongClickListener<List<String>>)(customRecycler, view1, stringList, index) -> {
//                                Dialog dialog = customDialog.getDialog();
//                                dialog.findViewById(R.id.dialog_sources_editLayout).setVisibility(View.VISIBLE);
//                                dialog.findViewById(buttonId_add).setVisibility(View.GONE);
//                                dialog.findViewById(R.id.dialog_sources_delete).setVisibility(View.VISIBLE);
//                                dialog_sources_name.getEditText().setName(stringList.get(0));
//                                dialog_sources_url.getEditText().setName(stringList.get(1));
//                                currentSource[0] = stringList;
//                            })
//                            .generate();
//
//                    view.findViewById(R.id.dialog_sources_delete).setOnClickListener(v -> {
//                        CustomDialog.Builder(this)
//                                .setTitle("Quelle Löschen")
//                                .setName("Willst du wirklich die Quelle '" + currentSource[0].get(0) + "' löschen?")
//                                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
//                                .addButton(CustomDialog.YES_BUTTON, (customDialog1, dialog) -> {
//                                    joke.getSources().remove(currentSource[0]);
//                                    hideEdit.run();
//                                    sources_customRecycler.reload();
//                                    Database.saveAll();
//                                })
//                                .show();
//                    });
//
//                    dialog_sources_save.setOnClickListener(v -> {
//                        if (!nameValidation.runTextValidation(dialog_sources_name, true) | !urlValidation.runTextValidation(dialog_sources_url, true))
//                            return;
//                        if (currentSource[0] == null) {
//                            List<String> newSource = Arrays.asList(dialog_sources_name.getEditText().getName().toString().trim()
//                                    , dialog_sources_url.getEditText().getName().toString().trim());
//                            joke.getSources().add(newSource);
//                        }
//                        else {
//                            currentSource[0].clear();
//                            currentSource[0].add(dialog_sources_name.getEditText().getName().toString().trim());
//                            currentSource[0].add(dialog_sources_url.getEditText().getName().toString().trim());
//                        }
//                        Database.saveAll();
//                        sources_customRecycler.reload();
//                        hideEdit.run();
//                        currentSource[0] = null;
//                    });
//                })
//                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.CUSTOM)
//                .addButton("Hinzufügen", (customDialog, dialog) -> {
//                    dialog.findViewById(R.id.dialog_sources_editLayout).setVisibility(View.VISIBLE);
//                    dialog.findViewById(buttonId_add).setVisibility(View.GONE);
//                    dialog.findViewById(R.id.dialog_sources_delete).setVisibility(View.GONE);
//                    EditText dialog_sources_url = ((TextInputLayout) dialog.findViewById(R.id.dialog_sources_url)).getEditText();
//                    dialog_sources_url.requestFocus();
//                    Utility.changeWindowKeyboard(this, dialog_sources_url, true);
//                }, buttonId_add, false)
//                .addButton("Zurück", (customDialog, dialog) -> {})
//                .setObjectExtra(sourcesText)
//                .setOnDialogDismiss(customDialog -> ((TextView) customDialog.getObjectExtra()).setName(
//                        joke.getSources().stream().map(strings -> strings.get(0)).collect(Collectors.joining(", "))
//                ))
//                .show();
//        if (edit)
//            sourcesDialog.findViewById(buttonId_add).setVisibility(View.GONE);
//    }
//
//    private boolean validation(TextValidation nameValidation, TextValidation urlValidation, TextInputLayout dialog_sources_name
//            , TextInputLayout dialog_sources_url, boolean changeError_name, boolean changeError_url, Button dialog_sources_save) {
//        if (!nameValidation.runTextValidation(dialog_sources_name, changeError_name) | !urlValidation.runTextValidation(dialog_sources_url, changeError_url)) {
//            dialog_sources_save.setEnabled(false);
//            return false;
//        } else {
//            dialog_sources_save.setEnabled(true);
//            return true;
//        }
//    }
//
//    interface TextValidation {
//        boolean runTextValidation(TextInputLayout textInputLayout, boolean changeErrorMessage);
//    }
//
//    private String getDomainFromUrl(String url, boolean shortened) {
//        Pattern pattern = Pattern.compile("(?<=://)[^/]*");
//        Matcher matcher = pattern.matcher(url);
//        if (matcher.find())
//        {
//            String substring = matcher.group(0); //.substring(3);
//            if (shortened) {
//                String[] split = substring.split("\\.");
//                return split[split.length - 2];
//            }
//            else
//                return substring;
//        }
//        return null;
//    }
//  <----- Sources -----





}
