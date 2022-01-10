package com.maxMustermannGeheim.linkcollection.Activities.Content.Videos;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
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
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Alias;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.WatchList;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.ImageCropUtility;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    private Map<Integer, ActivityResult> resultCallbacks;

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

        mediaEventList.sort((watchList1, watchList2) -> {
            int result = 0;
            if ((result = Boolean.compare(watchList1._isAllWatched(), watchList2._isAllWatched())) != 0)
                return result;
            if ((result = watchList1.getLastModified().compareTo(watchList2.getLastModified())) != 0)
                return result * -1;

            return watchList1.compareByName(watchList2);
        });
        return mediaEventList;
    }

    CustomList<PorterDuff.Mode> modes = new CustomList<>(PorterDuff.Mode.values());
    PorterDuff.Mode currentMode = modes.get(0);

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
                    CustomUtility.setPadding(recycler1, 4, -1);
                    recycler1.setNestedScrollingEnabled(false);
                    new CustomRecycler<String>(this, recycler1)
                            .setObjectList(CustomList.cast(watchList.getVideoIdList()).sorted(Comparator.comparing(id -> watchList.getWatchedVideoIdList().contains(id))))
                            .setItemLayout(R.layout.list_item_collection_video)
                            .setSetItemContent((customRecycler1, itemView1, videoId, index1) -> {
                                Video video = (Video) Utility.findObjectById(CategoriesActivity.CATEGORIES.VIDEO, videoId);

                                String imagePath = video.getImagePath();
                                ImageView thumbnail = itemView1.findViewById(R.id.listItem_collectionVideo_thumbnail);
                                thumbnail.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
                                if (Utility.stringExists(imagePath)) {
                                    Utility.loadUrlIntoImageView(this, thumbnail,
                                            Utility.getTmdbImagePath_ifNecessary(imagePath, "w500"), null, null, () -> Utility.roundImageView(thumbnail, 8));
                                    thumbnail.setVisibility(View.VISIBLE);
                                } else
                                    thumbnail.setImageResource(R.drawable.ic_no_image);

                                if (watchList.getWatchedVideoIdList().contains(videoId)) {
                                    itemView1.findViewById(R.id.listItem_collectionVideo_check).setVisibility(View.VISIBLE);
                                    int[][] states = {{ android.R.attr.state_enabled}};
                                    int[] colors = {0x66000000};
                                    thumbnail.setImageTintList(new ColorStateList(states, colors));
                                } else {
                                    itemView1.findViewById(R.id.listItem_collectionVideo_check).setVisibility(View.GONE);
                                    int[][] states = {{ android.R.attr.state_enabled}};
                                    int[] colors = {0x00000000};
                                    thumbnail.setImageTintList(new ColorStateList(states, colors));
                                }

                                thumbnail.setOnLongClickListener(v -> {
                                    editWatchList(this, watchList);
                                    return true;
                                });

                                thumbnail.setOnClickListener(v -> showDetailDialog(watchList));

                                CustomUtility.setPadding(itemView1, 4, 0);

                                ((TextView) itemView1.findViewById(R.id.listItem_collectionVideo_text)).setVisibility(View.GONE);
                            })
                            .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                            .setOnClickListener((customRecycler1, itemView1, s, index1) -> showDetailDialog(watchList))
                            .generate();

                })
                .setOnClickListener((customRecycler, itemView, watchList, index) -> showDetailDialog(watchList))
                .setOnLongClickListener((customRecycler, view, mediaEvent, index) -> editWatchList(this, mediaEvent))
                .enableFastScroll()
                .setPadding(16)
                .generate();

    }

    private void reloadRecycler() {
        recycler.reload();
    }
    /**  <------------------------- Recycler -------------------------  */



    /**  ------------------------- Detail ------------------------->  */
    private void showDetailDialog(@NonNull WatchList watchList) {
        removeFocusFromSearch();

        int oldHashCode = watchList.hashCode();

        CustomDialog.Builder(this)
                .setTitle(watchList.getName())
                .setText("Zuletzt Geändert: " + Utility.formatDate("dd.MM.yyyy HH:mm 'Uhr'", watchList.getLastModified()))
                .enableTextAlignmentCenter()
                .setView(customDialog -> new CustomRecycler<Video>(this)
                        .setItemLayout(R.layout.list_item_select)
                        .setGetActiveObjectList(customRecycler -> CustomList.map(watchList.getVideoIdList(), id -> (Video) Utility.findObjectById(CategoriesActivity.CATEGORIES.VIDEO, id)))
                        .enableDivider(12)
                        .setSetItemContent((customRecycler, itemView, video, index) -> {
                            ImageView thumbnail = itemView.findViewById(R.id.selectList_thumbnail);
                            String imagePath;
                            if (CustomUtility.stringExists(imagePath = video.getImagePath())) {
                                Utility.simpleLoadUrlIntoImageView(this, thumbnail, imagePath, imagePath, 4);
                                thumbnail.setVisibility(View.VISIBLE);
                            } else
                                thumbnail.setVisibility(View.GONE);

                            ((TextView) itemView.findViewById(R.id.selectList_name)).setText(video.getName());

                            ((CheckBox) itemView.findViewById(R.id.selectList_selected)).setChecked(watchList.getWatchedVideoIdList().contains(video.getUuid()));
                        })
                        .setOnClickListener((customRecycler, view, parentClass, index) -> {
                            CheckBox checkBox = view.findViewById(R.id.selectList_selected);
                            checkBox.setChecked(!checkBox.isChecked());
                            List<String> watchedVideoIdList = watchList.getWatchedVideoIdList();

                            if (watchedVideoIdList.contains(parentClass.getUuid())) {
                                watchedVideoIdList.remove(parentClass.getUuid());
                                Toast.makeText(this, "Eintrag geöffnet", Toast.LENGTH_SHORT).show();
                            } else {
                                watchedVideoIdList.add(parentClass.getUuid());
                                Toast.makeText(this, "Eintrag abgeschlossen", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setOnLongClickListener((customRecycler, view, video, index) -> {
                            ActivityResultHelper.addGenericRequest(this, appCompatActivity -> {
                                appCompatActivity.startActivityForResult(new Intent(this, VideoActivity.class)
                                                .putExtra(CategoriesActivity.EXTRA_SEARCH, video.getUuid())
                                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO),
                                        CategoriesActivity.START_CATEGORY_SEARCH);
                            }, (requestCode, resultCode, data) -> {
                                if (resultCode == Activity.RESULT_OK)
                                    customRecycler.reload();
                            });
                        })
                        .enableFastScroll(/*(parentClassCustomRecycler, parentClass, integer) -> parentClass.getName().substring(0, 1).toUpperCase()*/)
                        .generateRecyclerView())
                .disableScroll()
                .setDimensionsFullscreen()
                .addOnDialogDismiss(customDialog -> {
                    int newHashCode = watchList.hashCode();

                    if (newHashCode != oldHashCode) {
                        watchList.setLastModified(new Date());

                        Boolean saveResult = Database.saveAll(this, CustomUtility.Triple.create(null, "", null));
                        if (saveResult != null && saveResult)
                            reloadRecycler();
                    }
                })
                .show();
    }
    /**  <------------------------- Detail -------------------------  */



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
                                Database.saveAll(context);
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
                        return !oldWatchList.getName().trim().equals(title) || !oldWatchList.getVideoIdList().equals(editWatchList.getVideoIdList());
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
        RecyclerView recyclerView = view.findViewById(R.id.fragment_selectMediaHelper_selection_recycler);
        CustomUtility.setPadding(recyclerView, -1, -1, 46, -1);
        CustomRecycler<String> recycler = new CustomRecycler<String>(context, recyclerView)
                .setGetActiveObjectList(customRecycler -> {
                    emptyText.setVisibility(selectedVideoIds.isEmpty() ? View.VISIBLE : View.GONE);
                    return selectedVideoIds;
                })
                .setItemLayout(R.layout.list_item_collection_video)
                .setSetItemContent((customRecycler, itemView, videoId, index) -> {
                    Video video = (Video) Utility.findObjectById(CategoriesActivity.CATEGORIES.VIDEO, videoId);

                    String imagePath = video.getImagePath();
                    ImageView thumbnail = itemView.findViewById(R.id.listItem_collectionVideo_thumbnail);
                    thumbnail.setLayoutParams(new FrameLayout.LayoutParams(imageWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
                    if (Utility.stringExists(imagePath)) {
                        Utility.loadUrlIntoImageView(context, thumbnail,
                                Utility.getTmdbImagePath_ifNecessary(imagePath, "w500"), Utility.getTmdbImagePath_ifNecessary(imagePath, true), null, () -> Utility.roundImageView(thumbnail, 8));
                        thumbnail.setVisibility(View.VISIBLE);

                        thumbnail.setOnLongClickListener(v -> {
                            ActivityResultHelper.addGenericRequest(context, appCompatActivity -> {
                                appCompatActivity.startActivityForResult(new Intent(context, VideoActivity.class)
                                                .putExtra(CategoriesActivity.EXTRA_SEARCH, video.getUuid())
                                                .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.VIDEO),
                                        CategoriesActivity.START_CATEGORY_SEARCH);
                            }, (requestCode, resultCode, data) -> {
                                if (resultCode == Activity.RESULT_OK)
                                    customRecycler.reload();
                            });

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

        editWatchList.setName(((EditText) editDialog.findViewById(R.id.dialog_editWatchList_title)).getText().toString().trim());

        if (isAdd) {
            editWatchList.setLastModified(new Date());
            database.watchListMap.put(editWatchList.getUuid(), editWatchList);
        } else {
            editWatchList.getWatchedVideoIdList().removeIf(id -> !editWatchList.getVideoIdList().contains(id));

            if (!editWatchList.equals(oldWatchList))
                editWatchList.setLastModified(new Date());

            oldWatchList.getChangesFrom(editWatchList);
        }

        Database.saveAll(context);

        if (context instanceof WatchListActivity)
            ((WatchListActivity) context).reloadRecycler();
    }
    /**  <------------------------- Edit -------------------------  */


    /** ------------------------- Convenience -------------------------> */
    public static boolean isOpenInWatchList(WatchList watchList, String uuid) {
        return watchList.getVideoIdList().contains(uuid) && !watchList.getWatchedVideoIdList().contains(uuid);
    }

    public static CustomList<WatchList> getOpenInWatchLists(String uuid) {
        return Database.getInstance().watchListMap.values().stream().filter(watchList -> isOpenInWatchList(watchList, uuid)).collect(Collectors.toCollection(CustomList::new));
    }

    public static void checkWatchList(Context context, Video video, Runnable onSuccess) {
        CustomList<WatchList> watchLists = getOpenInWatchLists(video.getUuid());

        if (watchLists.isEmpty())
            onSuccess.run();
        else
            CustomDialog.Builder(context)
                    .setTitle("In WatchList abhaken?")
                    .setText(new Helpers.SpannableStringHelper().append("Das Video befindet sich in " + (watchLists.size() == 1 ? "folgender WatchList" : "folgenden WatchLists") + ":\n").appendBold(watchLists.join(",\n", com.finn.androidUtilities.ParentClass::getName)).append("\n\n" + (watchLists.size() == 1 ? "Soll der Eintrag" : "Sollen die Einträge") + " abgehakt werden?").get())
                    .addButton(CustomDialog.BUTTON_TYPE.NO_BUTTON, customDialog1 -> onSuccess.run())
                    .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog1 -> {
                        watchLists.forEach(watchList -> {
                            watchList.getWatchedVideoIdList().add(video.getUuid());
                            watchList.setLastModified(new Date());
                        });
                        onSuccess.run();
                    })
                    .show();
    }
    /**  <------------------------- Convenience -------------------------  */



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