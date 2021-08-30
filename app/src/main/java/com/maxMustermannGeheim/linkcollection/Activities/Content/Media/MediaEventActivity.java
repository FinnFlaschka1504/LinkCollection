package com.maxMustermannGeheim.linkcollection.Activities.Content.Media;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectTouchListener;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaEvent;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class MediaEventActivity extends AppCompatActivity {
    public enum FILTER_TYPE {
        ;

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

    private Database database;
    private SharedPreferences mySPR_daten;
    private String singular;
    private String plural;
    private CustomDialog editDialog;
    private Runnable setToolbarTitle;
    private String searchQuery = "";
    private SearchView.OnQueryTextListener textListener;
    private Helpers.AdvancedQueryHelper<MediaEvent> advancedQueryHelper;
    private CustomRecycler<MediaEvent> eventRecycler;

    private TextView elementCount;
    private SearchView searchView;

    /**
     * <------------------------- Start -------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.startSettings_ifNeeded(this);
        singular = "Event";
        plural = "Events";

        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_media_event);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {

            setContentView(R.layout.activity_media);

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(plural);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, plural);

            searchView = findViewById(R.id.search);
//            media_search.setQuery(new com.finn.androidUtilities.Helpers.SpannableStringHelper().appendColor("Test", Color.RED).appendBold(" Dick").appendItalic(" Schräg").get(), false);

            advancedQueryHelper = new Helpers.AdvancedQueryHelper<MediaEvent>(this, searchView)
                    .setRestFilter((searchQuery, mediaEventList) -> {
//                        if (searchQuery.contains("|")) {
//                            selectableMediaList.filterOr(searchQuery.split("\\|"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
//                        } else {
//                            selectableMediaList.filterAnd(searchQuery.split("&"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
//                        }
                        mediaEventList.filter(mediaEvent -> mediaEvent.getName().contains(searchQuery), true);
                    })
                    .addCriteria_defaultName()
                    .enableColoration();
//                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__PERSON, CategoriesActivity.CATEGORIES.MEDIA_PERSON, mediaSelectable -> mediaSelectable.getContent().getPersonIdList())
//                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, mediaSelectable -> mediaSelectable.getContent().getCategoryIdList())
//                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__TAG, CategoriesActivity.CATEGORIES.MEDIA_TAG, mediaSelectable -> mediaSelectable.getContent().getTagIdList());

            loadRecycler();

            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    removeFocusFromSearch();
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
            searchView.setOnQueryTextListener(textListener);


            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
                    if (!advancedQueryHelper.wrapAndSetExtraSearch(extraSearchCategory, extraSearch))
                        searchView.setQuery(extraSearch, true);
                }
            }
            setSearchHint();
        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }
    /**  ------------------------- Start ------------------------->  */


    /**
     * ------------------------- Recycler ------------------------->
     */
    private CustomList<MediaEvent> filterList(CustomList<MediaEvent> selectableMediaList) {
        if (!searchQuery.isEmpty()) {
            advancedQueryHelper.filterFull(selectableMediaList);
        }
        return selectableMediaList;
    }

    private CustomList<MediaEvent> sortList(CustomList<MediaEvent> mediaSelectableList) {

        mediaSelectableList.sort((media1, media2) -> {
//            File file1 = new File(media1.getContent().getImagePath());
//            File file2 = new File(media2.getContent().getImagePath());
//            return Long.compare(file1.lastModified(), file2.lastModified()) * -1;
            return 0;
        });
        return mediaSelectableList;
    }

    private void loadRecycler() {
        eventRecycler = new CustomRecycler<MediaEvent>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_media_event)
                .setGetActiveObjectList(customRecycler -> {
                    CustomList<MediaEvent> filteredList = sortList(filterList(new CustomList<>(database.mediaEventMap.values())));

                    TextView noItem = findViewById(R.id.no_item);
                    String text = searchView.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
//                    String viewsCountText = (views > 1 ? views + " Episoden" : (views == 1 ? "Eine" : "Keine") + " Episode") + " angesehen";
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText).append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    elementCount.setText(builder);

                    return filteredList;
                })
                .setSetItemContent((customRecycler, itemView, mediaEvent) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_mediaEvent_title)).setText(mediaEvent.getName());

                    String dateString = null;
                    if (mediaEvent.getBeginning() != null && mediaEvent.getEnd() != null)
                        dateString = String.format(Locale.getDefault(), "%s - %s", Utility.formatDate(Utility.DateFormat.DATE_DOT, mediaEvent.getBeginning()), Utility.formatDate(Utility.DateFormat.DATE_DOT, mediaEvent.getEnd()));
                    else if (mediaEvent.getBeginning() != null)
                        dateString = Utility.formatDate(Utility.DateFormat.DATE_DOT, mediaEvent.getBeginning());
                    ((TextView) itemView.findViewById(R.id.listItem_mediaEvent_date)).setText(dateString);

                    TextView descriptionTextView = (TextView) itemView.findViewById(R.id.listItem_mediaEvent_description);
                    if (CustomUtility.stringExists(mediaEvent.getDescription())) {
                        descriptionTextView.setVisibility(View.VISIBLE);
                        descriptionTextView.setText(mediaEvent.getDescription());
                    } else
                        descriptionTextView.setVisibility(View.GONE);


                    RecyclerView contentRecycler = itemView.findViewById(R.id.listItem_mediaEvent_content);
                    new CustomRecycler<ParentClass>(this, contentRecycler)
                            .setItemLayout(true ? R.layout.list_item_image : 0)
                            .setObjectList(true ? (Collection<ParentClass>) Utility.idToParentClassList(CategoriesActivity.CATEGORIES.MEDIA, mediaEvent.getMediaIdList()) : new CustomList<>())
                            .setSetItemContent((customRecycler1, itemView1, parentClass) -> {
                                if (parentClass instanceof Media) {
                                    Media media = (Media) parentClass;
                                    MediaActivity.SelectMediaHelper.loadPathIntoImageView(media.getImagePath(), itemView1, 100);
                                }
                            })
                            .addOptionalModifications(customRecycler1 -> {
                                try {
                                    Field onClickListener = customRecycler1.getClass().getDeclaredField("onClickListener");
                                    Field onLongClickListener = customRecycler1.getClass().getDeclaredField("onLongClickListener");
                                    onClickListener.setAccessible(true);
                                    onLongClickListener.setAccessible(true);
                                    customRecycler1
                                            .setOnClickListener((customRecycler2, itemView1, parentClass, index) -> {
                                                try {
                                                    CustomRecycler.OnClickListener<ParentClass> listener = (CustomRecycler.OnClickListener<ParentClass>) onClickListener.get(customRecycler);
                                                    if (listener != null)
                                                        listener.runOnClickListener(null, itemView, mediaEvent, -1);
                                                } catch (IllegalAccessException ignored) {

                                                }
                                            })
                                            .setOnLongClickListener((customRecycler2, view, parentClass, index) -> {
                                                try {
                                                    CustomRecycler.OnLongClickListener<ParentClass> listener = (CustomRecycler.OnLongClickListener<ParentClass>) onLongClickListener.get(customRecycler);
                                                    if (listener != null)
                                                        listener.runOnLongClickListener(null, itemView, mediaEvent, -1);
                                                } catch (IllegalAccessException ignored) {
                                                }
                                            });
                                } catch (NoSuchFieldException ignored) {
                                }
                            })
                            .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                            .generate();

                    String BREAKPOINT = null;

//                    contentRecycler.setOnTouchListener((v, event) -> {
//                        String BREAKPOINT = null;
////                        return false;
//                        itemView.onTouchEvent(event);
//                        return false;
//                    });
//                    itemView.setLayoutParams(new FrameLayout.LayoutParams(recyclerMetrics.second, recyclerMetrics.second));
//                    MediaActivity.SelectMediaHelper.loadPathIntoImageView(mediaSelectable.content.getImagePath(), itemView, recyclerMetrics.third);
//
//                    itemView.findViewById(R.id.listItem_image_selected).setVisibility(mediaSelectable.isSelected() ? View.VISIBLE : View.GONE);
//                    View fullScreenButton = itemView.findViewById(R.id.listItem_image_fullScreen);
//                    fullScreenButton.setVisibility(selectHelper.isActiveSelection() ? View.VISIBLE : View.GONE);
//                    fullScreenButton.setOnClickListener(v -> setMediaScrollGalleryAndShow(Arrays.asList(mediaSelectable.getContent()), mediaRecycler.getObjectList().indexOf(mediaSelectable) * -1));
                })
                .setOnClickListener((customRecycler, itemView, mediaEvent, index) -> {})
                .setOnLongClickListener((customRecycler, view, mediaEvent, index) -> showEditDialog(mediaEvent))
                .generate();
    }

    private void reLoadRecycler() {
        eventRecycler.reload();
    }
    /**  <------------------------- Recycler -------------------------  */


    /**  ------------------------- Edit ------------------------->  */
    private CustomDialog showEditDialog(@Nullable MediaEvent oldEvent) {
        if (!Utility.isOnline(this))
            return null;

        setResult(RESULT_OK);
        removeFocusFromSearch();


//        if (true)
//            return null;

        boolean isAdd = oldEvent == null;
        MediaEvent newEvent = isAdd ? new MediaEvent("") : (MediaEvent) oldEvent.clone();
        CustomList<Media> selectedMediaList =  newEvent.getMediaIdList().isEmpty() ? new CustomList<>() : (CustomList<Media>) Utility.idToParentClassList(CategoriesActivity.CATEGORIES.MEDIA, newEvent.getMediaIdList());

        Date from[] = {newEvent.getBeginning()};
        Date to[] = {newEvent.getEnd()};
        long timeZoneOffset = Utility.getTimezoneOffsetMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        String dialogTitle = "Ein " + singular + (isAdd ? " Hinzufügen" : " Bearbeiten");
        return CustomDialog.Builder(this)
                .setTitle(dialogTitle)
                .setView(R.layout.dialog_edit_media_event)
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout editTitleLayout = view.findViewById(R.id.dialog_editMediaEvent_title_layout);
                    TextInputLayout editDescriptionLayout = view.findViewById(R.id.dialog_editMediaEvent_description_layout);

                    if (!isAdd) {
                        editTitleLayout.getEditText().setText(newEvent.getName());
                        editDescriptionLayout.getEditText().setText(newEvent.getDescription());
                    }

                    Toast toast = Utility.centeredToast(this, "");
                    com.finn.androidUtilities.Helpers.TextInputHelper helper = new com.finn.androidUtilities.Helpers.TextInputHelper(result -> {
                        customDialog.getActionButton().setEnabled(!selectedMediaList.isEmpty() && result);
                    }, editTitleLayout, editDescriptionLayout)
                            .warnIfEmpty(editDescriptionLayout);
                    helper
                            .interceptDialogActionForValidation(customDialog, () -> {
                                toast.setText("Warnung: " + helper.getMessage(editDescriptionLayout).get(0) + "\n(Doppel-Click zum Fortfahren)");
                                toast.show();
                            }, toast::cancel)
                            .validate(new TextInputLayout[]{null});

                    FrameLayout selectedMediaParent = view.findViewById(R.id.dialog_editMediaEvent_selectParent);
                    MediaActivity.SelectMediaHelper.Builder(this, selectedMediaList)
                            .setParentView(selectedMediaParent)
//                            .setHideAddButton(!isAdd)
                            .setOverrideSelectionDialog((helper1, selectedMedia, override, onSelected) -> {
                                ActivityResultHelper.addGenericRequest(this, activity -> {
                                    Intent intent = new Intent(activity, MediaActivity.class).putExtra(MediaActivity.EXTRA_SELECT_MODE, override ? null : selectedMedia.map(ParentClass::getUuid));
                                    activity.startActivityForResult(intent, 0);
                                }, (requestCode, resultCode, data) -> {
                                    if (data != null) {
                                        ArrayList<String> list = data.getStringArrayListExtra(MediaActivity.EXTRA_SELECT_MODE);
                                        if (list != null) {
                                            CustomList<Media> mediaList = list.stream().map(s -> (Media) Utility.findObjectById(CategoriesActivity.CATEGORIES.MEDIA, s)).collect(Collectors.toCollection(CustomList::new));
                                            selectedMedia.replaceWith(mediaList);
                                            onSelected.run();
                                            helper.validate(new TextInputLayout[]{null});
                                        }
                                    }
                                });
                            })
                            .build();

                    // ---------------
                    TextView dateText = customDialog.findViewById(R.id.dialog_editMediaEvent_date_text);
                    Runnable setDateRangeTextView = () -> {
                        if (from[0] != null && to[0] != null) {
                            dateText.setText(String.format("%s - %s", dateFormat.format(from[0]), dateFormat.format(to[0])));
                        } else if (from[0] != null) {
                            dateText.setText(dateFormat.format(from[0]));
                        } else {
                            dateText.setText("Nicht ausgewählt");
                        }
                    };
                    setDateRangeTextView.run();

                    MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
                    builder.setTitleText("Zeitraum Auswählen");
                    if (from[0] != null && to[0] != null) {
                        builder.setSelection(Pair.create(from[0].getTime() - timeZoneOffset, to[0].getTime() - timeZoneOffset));
                    } else if (from[0] != null) {
                        builder.setSelection(Pair.create(from[0].getTime() - timeZoneOffset, from[0].getTime() - timeZoneOffset));
                    }
                    MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

                    picker.addOnPositiveButtonClickListener(selection -> {
                        String BREAKPOINT = null;
                        from[0] = new Date(selection.first + timeZoneOffset);
                        if (!Objects.equals(selection.first, selection.second))
                            to[0] = new Date(selection.second + timeZoneOffset);
                        else
                            to[0] = null;
                        setDateRangeTextView.run();
                    });


                    view.findViewById(R.id.dialog_editMediaEvent_date_select).setOnClickListener(v -> {
                        picker.show(((AppCompatActivity) this).getSupportFragmentManager(), picker.toString());
                    });
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addOptionalModifications(customDialog -> {
                    if (!isAdd) {
                        customDialog
                                .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                                    database.mediaEventMap.remove(oldEvent.getUuid());
                                    reLoadRecycler();
                                    Toast.makeText(this, (Database.saveAll_simple() ? plural : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();
                                })
                                .alignPreviousButtonsLeft()
                                .transformPreviousButtonToImageButton()
                                .addConfirmationDialogToLastAddedButton("", "", customDialog1 -> {
                                    customDialog1
                                            .setTitle(singular + " Löschen")
                                            .setText(new Helpers.SpannableStringHelper().append("Möchstest du wirklich '").appendBold(oldEvent.getName()).append("' löschen?").get());
                                });
                    }
                })
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> saveMultipleMedia(customDialog, isAdd, oldEvent, newEvent, selectedMediaList, Pair.create(from[0], to[0])), false)
                .disableLastAddedButton()
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String title = ((TextInputLayout) editDialog.findViewById(R.id.dialog_editMediaEvent_title_layout)).getEditText().getText().toString().trim();
                    String description = ((TextInputLayout) editDialog.findViewById(R.id.dialog_editMediaEvent_description_layout)).getEditText().getText().toString().trim();
                    if (isAdd) {
                        return CustomUtility.stringExists(title) || CustomUtility.stringExists(description) || !selectedMediaList.isEmpty();
                    } else {
                        return !oldEvent.getName().equals(title) || !CustomUtility.stringExistsOrElse(oldEvent.getDescription(), "").equals(description) || !oldEvent.getMediaIdList().equals(selectedMediaList.map(com.finn.androidUtilities.ParentClass::getUuid));
                    }


                })
                .addOnDialogShown(customDialog -> editDialog = customDialog)
                .addOnDialogDismiss(customDialog -> editDialog = null)
                .show();

    }

    private void saveMultipleMedia(CustomDialog editDialog, boolean isAdd, MediaEvent oldEvent, MediaEvent newEvent, CustomList<Media> selectedMediaList, Pair<Date, Date> beginningEnd) {
        if (!Utility.isOnline(this))
            return;

        String title = ((TextInputLayout) editDialog.findViewById(R.id.dialog_editMediaEvent_title_layout)).getEditText().getText().toString().trim();
        String description = ((TextInputLayout) editDialog.findViewById(R.id.dialog_editMediaEvent_description_layout)).getEditText().getText().toString().trim();
        newEvent.setDescription(description).setName(title);
        newEvent.setMediaIdList(selectedMediaList.map(ParentClass::getUuid));
        newEvent.setBeginning(beginningEnd.first);
        newEvent.setEnd(beginningEnd.second);

        if (isAdd) {
            database.mediaEventMap.put(newEvent.getUuid(), newEvent);
        } else {
            oldEvent.getChangesFrom(newEvent);
        }

//        Boolean aBoolean = Database.saveAll();

        ;

//        Toast.makeText(this, CustomUtility.isNullReturnOrElse(Database.saveAll(), "Speichern Fehlgeschlagen", aBoolean -> (aBoolean ? "" : "Nichts ") + "Gespeichert"), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, (aBoolean ? "" : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();

        if (Database.saveAll_simple(this)) {
            editDialog.dismiss();
            reLoadRecycler();
        }
    }
    /**  <------------------------- Edit -------------------------  */


    /**
     * ------------------------- Toolbar ------------------------->
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_media_event, menu);
//        menu.findItem(R.id.taskBar_media_share).setIconTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{Color.WHITE}));

//        Menu subMenu = menu.findItem(R.id.taskBar_filter).getSubMenu();
//        subMenu.findItem(R.id.taskBar_show_filterByName)
//                .setChecked(filterTypeSet.contains(ShowActivity.FILTER_TYPE.NAME));
//        subMenu.findItem(R.id.taskBar_show_filterByGenre)
//                .setChecked(filterTypeSet.contains(ShowActivity.FILTER_TYPE.GENRE));
//
        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_mediaEvent_add:
                showEditDialog(null);
//                SelectMediaHelper.Builder(this).showSelection();
                break;
//            case R.id.taskBar_media_edit:
//                showEditMultipleDialog(selectHelper.getAllSelectedContent(), false);
//                break;
//            case R.id.taskBar_media_share:
//                shareMedia(selectHelper.getAllSelectedContent());
//                break;

            case android.R.id.home:
                if (getCallingActivity() == null)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
        return true;
    }

    // --------------- Search

    private void setSearchHint() {
//        String join = filterTypeSet.stream().filter(ShowActivity.FILTER_TYPE::hasName).sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).map(ShowActivity.FILTER_TYPE::getName).collect(Collectors.joining(", "));
//        shows_search.setQueryHint(join.isEmpty() ? "Kein Filter ausgewählt!" : join + " ('&' als 'und'; '|' als 'oder')");
//        Utility.applyToAllViews(shows_search, View.class, view -> view.setEnabled(!join.isEmpty()));
    }

    private void removeFocusFromSearch() {
        searchView.clearFocus();
    }

    /**
     * <------------------------- Toolbar -------------------------
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}