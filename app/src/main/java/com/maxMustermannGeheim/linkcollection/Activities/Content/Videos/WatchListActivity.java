package com.maxMustermannGeheim.linkcollection.Activities.Content.Videos;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WatchListActivity extends AppCompatActivity {

    /**
     * Zufällig listen als WatchList speichern
     * Benennen und Beschreiben können
     * Benutzerdefinierte reihenfolgen bilden können
     */


    private static final String TAG = "AppCompatActivity";

    private Database database;
    private SharedPreferences mySPR_data;
    private String singular;
    private String plural;
//    private CustomDialog editDialog;
    private Runnable setToolbarTitle;
    private String searchQuery = "";
    private SearchView.OnQueryTextListener textListener;
    private Helpers.AdvancedQueryHelper<WatchList> advancedQueryHelper;
    private CustomRecycler<WatchList> recycler;

    private TextView elementCount;
    private SearchView searchView;

    /**  <------------------------- Start -------------------------  */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_list);

        Settings.startSettings_ifNeeded(this);
        singular = CategoriesActivity.CATEGORIES.WATCH_LIST.getSingular();
        plural = CategoriesActivity.CATEGORIES.WATCH_LIST.getPlural();

        database = Database.getInstance();
        if (database == null)
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_watch_list);
        mySPR_data = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

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

            advancedQueryHelper = new Helpers.AdvancedQueryHelper<WatchList>(this, searchView)
                    .setRestFilter((searchQuery, watchLists) -> {
//                        if (searchQuery.contains("|")) {
//                            selectableMediaList.filterOr(searchQuery.split("\\|"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
//                        } else {
//                            selectableMediaList.filterAnd(searchQuery.split("&"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
//                        }
                        watchLists.filter(mediaEvent -> mediaEvent.getName().contains(searchQuery), true);
                    })
//                    .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<MediaEvent, MediaEvent>(ADVANCED_SEARCH_CRITERIA__EVENT, Helpers.AdvancedQueryHelper.PARENT_CLASS_PATTERN)
//                            .setCategory(CategoriesActivity.CATEGORIES.MEDIA_EVENT)
//                            .setParser(sub -> (MediaEvent) ParentClass_Tree.findObjectByName(CategoriesActivity.CATEGORIES.MEDIA_EVENT, sub, false))
//                            .setBuildPredicate(mediaEvent -> {
//                                if (mediaEvent == null) {
//                                    parent = null;
//                                    return null;
//                                }
//                                parent = mediaEvent;
//                                return mediaSelectable -> true;
//                            }))
                    .addCriteria_defaultName()
                    .enableColoration();

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
                    reloadRecycler();
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
            Database.getInstance(mySPR_data, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }
    /**  ------------------------- Start ------------------------->  */


    /**  ------------------------- Recycler ------------------------->  */
    private CustomList<WatchList> filterList(CustomList<WatchList> mediaEventList) {
        if (!searchQuery.isEmpty()) {
            advancedQueryHelper.filterFull(mediaEventList);
        }
        return mediaEventList;
    }

    private CustomList<WatchList> sortList(CustomList<WatchList> mediaEventList) {

        mediaEventList.sort(ParentClass::compareByName);
        return mediaEventList;
    }

    private void loadRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recycler);
        int imageWidth = CustomUtility.dpToPx(68);


        recycler = new CustomRecycler<WatchList>(this, recyclerView)
                .setItemLayout(R.layout.list_item_media_event)
                .setGetActiveObjectList(customRecycler -> {
                    CustomList<WatchList> filteredList = filterList(new CustomList<>(database.watchListMap.values()));

                    sortList(filteredList);

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
                .setSetItemContent((customRecycler, itemView, watchList, index) -> {
                    ((TextView) itemView.findViewById(R.id.listItem_mediaEvent_title)).setText(watchList.getName());

                    itemView.findViewById(R.id.listItem_mediaEvent_date).setVisibility(View.GONE);
                    itemView.findViewById(R.id.listItem_mediaEvent_description).setVisibility(View.GONE);

                    RecyclerView recycler1 = itemView.findViewById(R.id.listItem_mediaEvent_content);
                    recycler1.setNestedScrollingEnabled(false);
                    CustomRecycler<String> recycler = new CustomRecycler<String>(this, recycler1)
                            .setObjectList(watchList.getVideoIdList())
                            .setItemLayout(R.layout.list_item_collection_video)
                            .setSetItemContent((customRecycler1, itemView1, videoId, index1) -> {
                                Video video = (Video) Utility.findObjectById(CategoriesActivity.CATEGORIES.VIDEO, videoId);

                                String imagePath = video.getImagePath();
                                ImageView thumbnail = itemView1.findViewById(R.id.listItem_collectionVideo_thumbnail);
                                thumbnail.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
                                if (Utility.stringExists(imagePath)) {
                                    Utility.loadUrlIntoImageView(this, thumbnail,
                                            Utility.getTmdbImagePath_ifNecessary(imagePath, "w500"), Utility.getTmdbImagePath_ifNecessary(imagePath, true), null, () -> Utility.roundImageView(thumbnail, 8));
                                    thumbnail.setVisibility(View.VISIBLE);
                                } else
                                    thumbnail.setImageResource(R.drawable.ic_no_image);

                                thumbnail.setOnLongClickListener(v -> {
                                    editWatchList(this, watchList);
                                    return true;
                                });

                                CustomUtility.setPadding(itemView1, 4, 0);

                                ((TextView) itemView1.findViewById(R.id.listItem_collectionVideo_text)).setVisibility(View.GONE);

//                    loadPathIntoImageView(media.getImagePath(), itemView, CustomUtility.dpToPx(120), 1);
                            })
                            .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
//                .addOptionalModifications(customRecycler -> {
//                    if (subSelectedMedia != null) {
//                        customRecycler
//                                .setOnClickListener((customRecycler1, itemView, media, index) -> {
//                                    if (!subSelectedMedia.remove(media))
//                                        subSelectedMedia.add(media);
//                                    customRecycler.getAdapter().notifyItemChanged(index);
//                                    onSubSelectionChanged.run(subSelectedMedia);
//                                });
//                    }
//                })
                            .generate();

                })
//                .setOnClickListener((customRecycler, itemView, mediaEvent, index) -> {
//                    if (mediaEvent.getChildren().isEmpty()) {
//                        if (mediaEvent._isDummy() && parent != null)
//                            mediaEvent = parent;
//                        startActivityForResult(new Intent(this, MediaActivity.class)
//                                        .putExtra(CategoriesActivity.EXTRA_SEARCH, CategoriesActivity.escapeForSearchExtra(mediaEvent.getName()))
//                                        .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_EVENT),
//                                START_SEARCH_MEDIA_EVENT);
//                    } else {
//                        startActivityForResult(new Intent(this, MediaEventActivity.class)
//                                        .putExtra(CategoriesActivity.EXTRA_SEARCH, CategoriesActivity.escapeForSearchExtra(mediaEvent.getName()))
//                                        .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_EVENT),
//                                START_OPEN_MEDIA_EVENT);
//                    }
//                })
                .setOnLongClickListener((customRecycler, view, mediaEvent, index) -> editWatchList(this, mediaEvent))
//                .enableFastScroll(scrollRange, heightList)
                .enableFastScroll(/*WatchList::_getHeight*/)
                .setPadding(16)
                .generate();

//        FastScroller[] fastScroller = {null};
//        FastScrollerBuilder fastScrollerBuilder = new FastScrollerBuilder(recyclerView)
//                .setThumbDrawable(Objects.requireNonNull(getDrawable(R.drawable.fast_scroll_thumb)))
//                .setTrackDrawable(Objects.requireNonNull(getDrawable(R.drawable.fast_scroll_track)));
////        fastScrollerBuilder.disableScrollbarAutoHide();
//        fastScroller[0] = fastScrollerBuilder
//                .setPadding(0, 20, 0, 50)
//                .setViewHelper(new FastScrollRecyclerViewHelper(eventRecycler, fastScroller, scrollRange, heightList, false, null))
//                .build();

    }

    private void reloadRecycler() {
        recycler.reload();
    }
    /**  <------------------------- Recycler -------------------------  */


    /**  ------------------------- Edit ------------------------->  */
    public static void editWatchList(AppCompatActivity context, @Nullable WatchList oldWatchList) {
        if (!Utility.isOnline(context))
            return;

        context.setResult(RESULT_OK);
        if (context instanceof WatchListActivity)
            ((WatchListActivity) context).removeFocusFromSearch();

        boolean isAdd = oldWatchList == null || oldWatchList.getName() == null;
        WatchList editWatchList = isAdd ? new WatchList("") : (WatchList) oldWatchList.clone();
        if (!isAdd || (oldWatchList != null && !oldWatchList.getVideoIdList().isEmpty()))
            editWatchList.setVideoIdList(new ArrayList<>(oldWatchList.getVideoIdList()));

        boolean isRandomSelection = isAdd && (oldWatchList != null && !oldWatchList.getVideoIdList().isEmpty());
        String randomSelectionText = "Zufällige Auswahl vom " + new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        if (isRandomSelection)
            editWatchList.setName(randomSelectionText);

        CustomDialog.Builder(context)
                .setTitle("WatchList " + (isAdd ? "Hinzufügen" : "Bearbeiten"))
                .setView(R.layout.dialog_edit_watch_list)
                .setSetViewContent((customDialog, view, reload) -> {
                    TextInputLayout editTitleLayout = view.findViewById(R.id.dialog_editWatchList_title_layout);
                    TextInputLayout editDescriptionLayout = view.findViewById(R.id.dialog_editWatchList_description_layout);

                    if (!isAdd || isRandomSelection) {
                        editTitleLayout.getEditText().setText(editWatchList.getName());
//                        editDescriptionLayout.getEditText().setText(editWatchList.getDescription());
                    }

                    Toast toast = Utility.centeredToast(context, "");
//                    CustomList<MediaEvent> mediaEvents = new CustomList<>(database.mediaEventMap.values());
//                    if (oldEvent != null)
//                        mediaEvents.remove(oldEvent);
                    com.finn.androidUtilities.Helpers.TextInputHelper helper = new com.finn.androidUtilities.Helpers.TextInputHelper()
                            .setOnValidationResult(result -> customDialog.getActionButton().setEnabled(result && !editWatchList.getVideoIdList().isEmpty()))
                            .addValidator(editTitleLayout, editDescriptionLayout)
                            .setValidation(editTitleLayout, (validator, text) -> {
                                WatchList object = (WatchList) Utility.findObjectByName(CategoriesActivity.CATEGORIES.WATCH_LIST, text, true);
                                if (object != null && object != oldWatchList)
                                    validator.setInvalid("WatchList mit diesem Namen bereits vorhanden");
                            })
                            .warnIfEmpty(editDescriptionLayout);
//                            .interceptDialogActionForValidation(customDialog, true, (inputHelper) -> {
//                                toast.setText("Warnung: " + inputHelper.getMessage(editDescriptionLayout).get(0) + "\n(Doppel-Click zum Fortfahren)");
//                                toast.show();
//                            }, t -> toast.cancel());
                    if (!isRandomSelection)
                        helper.validate(new TextInputLayout[]{null});
                    else
                        helper.validate();

                    // ---------------

                    ViewGroup parentView = view.findViewById(R.id.dialog_editWatchList_selectParent);
                    videoSelectorFragmentBuilder(context, parentView, editWatchList.getVideoIdList(), videoIds -> {
                        helper.validate(new TextInputLayout[]{null});
                    });

                })
                .addOptionalModifications(customDialog -> {
                    if (isAdd)
                        return;
                    customDialog
                            .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                                Database.getInstance().watchListMap.remove(editWatchList.getUuid());
                                if (context instanceof WatchListActivity)
                                    ((WatchListActivity) context).reloadRecycler();
                                Toast.makeText(context, "WatchList gelöscht", Toast.LENGTH_SHORT).show();
                            })
                            .transformLastAddedButtonToImageButton()
                            .addConfirmationDialogToLastAddedButton("Löschen Bestätigen", Helpers.SpannableStringHelper.Builder(spanBuilder -> spanBuilder.append("Möchtest du wirklich '").appendBold(editWatchList.getName()).append("' löschen?")))
                            .alignPreviousButtonsLeft();
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    saveWatchList(context, customDialog, oldWatchList, editWatchList);
                })
                .disableLastAddedButton()
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    String title = ((TextInputLayout) customDialog.findViewById(R.id.dialog_editWatchList_title_layout)).getEditText().getText().toString().trim();
//                    String description = ((TextInputLayout) editDialog.findViewById(R.id.dialog_editMediaEvent_description_layout)).getEditText().getText().toString().trim();
                    if (isRandomSelection)
                        return !title.equals(randomSelectionText) || !oldWatchList.getVideoIdList().equals(editWatchList.getVideoIdList());
                    else if (isAdd) {
                        return CustomUtility.stringExists(title) || !editWatchList.getVideoIdList().isEmpty();
                    } else {
                        return !oldWatchList.getName().equals(title) || !oldWatchList.getVideoIdList().equals(editWatchList.getVideoIdList());
                    }


                })
                .show();
    }

    private static void videoSelectorFragmentBuilder(AppCompatActivity context, ViewGroup parentView, List<String> selectedVideoIds, Utility.GenericInterface<List<String>> onSelection) {
        FrameLayout view = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.fragment_select_media_helper_selection, parentView, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView emptyText = view.findViewById(R.id.fragment_selectMediaHelper_selection_empty);
        parentView.addView(view);
        int imageWidth = CustomUtility.dpToPx(64);
        CustomRecycler<String> recycler = new CustomRecycler<String>(context, view.findViewById(R.id.fragment_selectMediaHelper_selection_recycler))
                .setGetActiveObjectList(customRecycler -> {
                    emptyText.setVisibility(selectedVideoIds.isEmpty() ? View.VISIBLE : View.GONE);
                    return selectedVideoIds;
                })
                .setItemLayout(R.layout.list_item_collection_video)
                .setSetItemContent((customRecycler, itemView, videoId, index) -> {
                    Video video = (Video) Utility.findObjectById(CategoriesActivity.CATEGORIES.VIDEO, videoId);

                    String imagePath = video.getImagePath();
                    ImageView thumbnail = itemView.findViewById(R.id.listItem_collectionVideo_thumbnail);
                    thumbnail.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
                    if (Utility.stringExists(imagePath)) {
                        Utility.loadUrlIntoImageView(context, thumbnail,
                                Utility.getTmdbImagePath_ifNecessary(imagePath, "w500"), Utility.getTmdbImagePath_ifNecessary(imagePath, true), null, () -> Utility.roundImageView(thumbnail, 8));
                        thumbnail.setVisibility(View.VISIBLE);

                        thumbnail.setOnLongClickListener(v -> {
                            context.startActivityForResult(new Intent(context, VideoActivity.class)
                                            .putExtra(CategoriesActivity.EXTRA_SEARCH, video.getUuid())
                                            .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO),
                                    CategoriesActivity.START_CATEGORY_SEARCH);

                            return true;
                        });
                    } else
                        thumbnail.setImageResource(R.drawable.ic_no_image);

                    CustomUtility.setPadding(itemView, 4, -1);

                    TextView nameTextView = (TextView) itemView.findViewById(R.id.listItem_collectionVideo_text);
                    nameTextView.setText(video.getName());
                    nameTextView.setLines(1);

//                    loadPathIntoImageView(media.getImagePath(), itemView, CustomUtility.dpToPx(120), 1);
                })
                .enableSwiping((customRecycler, objectList, direction, media, index) -> {
                    selectedVideoIds.remove(index);
                    emptyText.setVisibility(selectedVideoIds.isEmpty() ? View.VISIBLE : View.GONE);
                    onSelection.run(selectedVideoIds);
                }, true, false)
                .enableDragAndDrop((customRecycler, objectList) -> CustomList.replace(selectedVideoIds, objectList))
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .generate();

        ImageView editButton = view.findViewById(R.id.fragment_selectMediaHelper_selection_add);
        editButton.setImageResource(R.drawable.ic_edit);

        editButton.setOnClickListener(v -> {
            Utility.showEditItemDialog(context, selectedVideoIds, CategoriesActivity.CATEGORIES.VIDEO, (customDialog, selectedIds) -> {
                selectedVideoIds.clear();
                selectedVideoIds.addAll(selectedIds);
                recycler.reload();
                onSelection.run(selectedIds);
            });
        });
    }

    private static void saveWatchList(AppCompatActivity context, CustomDialog editDialog, WatchList oldWatchList, WatchList editWatchList) {
        Database database = Database.getInstance();

        boolean isAdd = oldWatchList == null || oldWatchList.getName() == null;

        editWatchList.setName(((EditText) editDialog.findViewById(R.id.dialog_editWatchList_title)).getText().toString());

        if (isAdd) {
            database.watchListMap.put(editWatchList.getUuid(), editWatchList);
        } else {
            oldWatchList.getChangesFrom(editWatchList);
        }

        Database.saveAll(context);

        if (context instanceof WatchListActivity)
            ((WatchListActivity) context).reloadRecycler();
    }
    /**  <------------------------- Edit -------------------------  */


    /**  ------------------------- Toolbar ------------------------->  */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_watch_list, menu);

        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_watchList_add:
                editWatchList(this, null);
                break;
            case R.id.taskBar_mediaEvent_add:
//                showEditDialog(null);
                break;
            case R.id.taskBar_mediaEvent_sortTree:
                break;

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

}