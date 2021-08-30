package com.maxMustermannGeheim.linkcollection.Activities.Content.Media;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.transition.TransitionManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.dragselectrecyclerview.DragSelectReceiver;
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.ParentClass;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.common.collect.ArrayListMultimap;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.BuildConfig;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaCategory;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomGlideImageLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomVideoLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.veinhorn.scrollgalleryview.HackyViewPager;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import org.apmem.tools.layouts.FlowLayout;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class MediaActivity extends AppCompatActivity {
    public static final String EXTRA_SELECT_MODE = "EXTRA_SELECT_MODE";
    private final String ADVANCED_SEARCH_CRITERIA__PERSON = "p";
    private final String ADVANCED_SEARCH_CRITERIA__CATEGORY = "c";
    private final String ADVANCED_SEARCH_CRITERIA__TAG = "t";


    public enum FILTER_TYPE {
        PERSON("Person"), CATEGORY("Kategorie");

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

    private final int REQUEST_CODE_STORAGE_MANAGER = 123;

    private Database database;
    private SharedPreferences mySPR_daten;
    private String singular;
    private String plural;
    private CustomList<Media> allMediaList = new CustomList<>();
    private CustomDialog editDialog;
    private Runnable setToolbarTitle;
    private String searchQuery = "";
    private SearchView.OnQueryTextListener textListener;
    private CustomRecycler<MultiSelectHelper.Selectable<Media>> mediaRecycler;
    private MultiSelectHelper<Media> selectHelper;
    private boolean selectMode;
    private Menu toolBarMenu;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.PERSON, FILTER_TYPE.CATEGORY));
    private VideoView currentVideoPreview;
    public boolean isVideoPreviewSoundOn = false;
    public Map<VideoView, MediaPlayer> currentMediaPlayerMap = new HashMap<>();
    private Helpers.AdvancedQueryHelper<MultiSelectHelper.Selectable<Media>> advancedQueryHelper;
    private Utility.OnSwipeTouchListener onSwipeTouchListener;
    private boolean scrollGalleryShown;
    private CustomUtility.Triple<Integer, Integer, Integer> recyclerMetrics;

    private TextView elementCount;
    private SearchView media_search;
    private ScrollGalleryView scrollGalleryView;


    //  ------------------------- Start ------------------------->
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.startSettings_ifNeeded(this);
        String stringExtra = Settings.getSingleSetting(this, Settings.SETTING_SPACE_NAMES_ + Settings.Space.SPACE_MEDIA);
        if (stringExtra != null) {
            String[] singPlur = stringExtra.split("\\|");

            singular = singPlur[0];
            plural = singPlur[1];
            setTitle(plural);
        }

        database = Database.getInstance();
        if (database == null || !checkAndRequestStoragePermission())
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_media);
        mySPR_daten = getSharedPreferences(SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        if (hasStoragePermission())
            loadDatabase();
    }

    private boolean hasStoragePermission() {
        return Environment.isExternalStorageManager();
    }

    private boolean checkAndRequestStoragePermission() {
        boolean storagePermission = hasStoragePermission();
        if (storagePermission)
            return true;

        final boolean[] ignore = {false};
        CustomDialog.Builder(this)
                .setTitle("Speicher Berechtigung")
                .setText("Für dieses Feature braucht die App vollen Zugriff auf deinen Speicher")
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.OK_CANCEL)
                .addButton(CustomDialog.BUTTON_TYPE.OK_BUTTON, customDialog -> {
                    Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri), REQUEST_CODE_STORAGE_MANAGER);
                    ignore[0] = true;
                })
                .addOnDialogDismiss(customDialog -> {
                    if (!hasStoragePermission() && !ignore[0])
                        finish();
                })
                .show();
        return false;
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {

            selectMode = getIntent().hasExtra(EXTRA_SELECT_MODE);

            setContentView(R.layout.activity_media);

            setupScrollGalleryView();

            onSwipeTouchListener = new Utility.OnSwipeTouchListener(this) {
                @Override
                public boolean onSwipeBottom() {
                    View view = getCurrentViewFromViewPager(scrollGalleryView.getViewPager());
                    if (view != null) {
                        PhotoView photo = view.findViewById(R.id.photoView);
                        float scale = photo.getScale();
                        if (scale == 1)
                            hideScrollGallery();
                    } else
                        hideScrollGallery();
                    return false;
//                    return super.onSwipeBottom();
                }
            };

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(plural);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, plural);

            media_search = findViewById(R.id.search);
//            media_search.setQuery(new com.finn.androidUtilities.Helpers.SpannableStringHelper().appendColor("Test", Color.RED).appendBold(" Dick").appendItalic(" Schräg").get(), false);

            advancedQueryHelper = new Helpers.AdvancedQueryHelper<MultiSelectHelper.Selectable<Media>>(this, media_search)
                    .setRestFilter((searchQuery, selectableMediaList) -> {
                        if (searchQuery.contains("|")) {
                            selectableMediaList.filterOr(searchQuery.split("\\|"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
                        } else {
                            selectableMediaList.filterAnd(searchQuery.split("&"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
                        }
                    })
                    .addCriteria_defaultName()
                    .enableColoration()
                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__PERSON, CategoriesActivity.CATEGORIES.MEDIA_PERSON, mediaSelectable -> mediaSelectable.getContent().getPersonIdList())
                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, mediaSelectable -> mediaSelectable.getContent().getCategoryIdList())
                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__TAG, CategoriesActivity.CATEGORIES.MEDIA_TAG, mediaSelectable -> mediaSelectable.getContent().getTagIdList());

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
            media_search.setOnQueryTextListener(textListener);


            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHORTCUT))
                showEditMultipleDialog(null, false);

            if (getIntent().getAction() != null && CustomUtility.boolOr(getIntent().getAction(), "android.intent.action.SEND", "android.intent.action.SEND_MULTIPLE"))
                handleIncomingShare();


            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
                    if (!advancedQueryHelper.wrapAndSetExtraSearch(extraSearchCategory, extraSearch))
                        media_search.setQuery(extraSearch, true);
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
    //  <------------------------- Start -------------------------


    //  ------------------------- Recycler ------------------------->
    private CustomList<MultiSelectHelper.Selectable<Media>> filterList(CustomList<MultiSelectHelper.Selectable<Media>> selectableMediaList) {
        if (!searchQuery.isEmpty()) {
            advancedQueryHelper.filterFull(selectableMediaList);
        }
        return selectableMediaList;
    }

    private CustomList<MultiSelectHelper.Selectable<Media>> sortList(CustomList<MultiSelectHelper.Selectable<Media>> mediaSelectableList) {

        mediaSelectableList.sort((media1, media2) -> {
            File file1 = new File(media1.getContent().getImagePath());
            File file2 = new File(media2.getContent().getImagePath());
            return Long.compare(file1.lastModified(), file2.lastModified()) * -1;
        });
        return mediaSelectableList;
    }

    class MyDragSelectReceiver implements DragSelectReceiver {

        @Override
        public int getItemCount() {
            return mediaRecycler.getObjectList().size();
        }

        @Override
        public boolean isIndexSelectable(int i) {
            return true;
        }

        @Override
        public boolean isSelected(int i) {
            return get(i).selected;
        }

        @Override
        public void setSelected(int i, boolean b) {
            selectHelper.setSelected(i, b);
        }

        private MultiSelectHelper.Selectable<Media> get(int i) {
            return mediaRecycler.getObjectList().get(i);
        }
    }

    static class MultiSelectHelper<T> {
        CustomRecycler<Selectable<T>> customRecycler;
        CustomList<Selectable<T>> contentList;
        boolean activeSelection;
        DragSelectTouchListener dragSelectTouchListener;
        MediaActivity context;
        Menu toolBarMenu;
        boolean allowEmpty;
        CustomUtility.GenericInterface<Boolean> onChangeSelectionStatus;
        Utility.DoubleGenericInterface<MultiSelectHelper<T>, CustomList<Selectable<T>>> applyInitialSelection;

        //  ------------------------- Constructor ------------------------->
        public MultiSelectHelper(MediaActivity context) {
            this.context = context;
        }
        //  <------------------------- Constructor -------------------------


        //  ------------------------- Getter & Setter ------------------------->
        public CustomList<Selectable<T>> getContentList() {
            return contentList;
        }

        public MultiSelectHelper<T> setContentList(CustomList<Selectable<T>> contentList) {
            this.contentList = contentList;
            return this;
        }

        public boolean isActiveSelection() {
            return activeSelection;
        }

        public MultiSelectHelper<T> setActiveSelection(boolean activeSelection) {
            this.activeSelection = activeSelection;
            return this;
        }
        //  <------------------------- Getter & Setter -------------------------


        //  ------------------------- Convenience ------------------------->
        public MultiSelectHelper<T> updateFromList(List<T> list, boolean keepSelection) {
            CustomList<Selectable<T>> customList = list.stream().map(Selectable::new).collect(Collectors.toCollection(CustomList::new));

            if (contentList == null) {
                contentList = customList;
                Utility.runDoubleGenericInterface(applyInitialSelection, this, contentList);
                return this;
            }

            if (keepSelection) {
                customList.forEach(tSelectable -> {
                    contentList.stream().filter(tSelectable1 -> Objects.equals(tSelectable.content, tSelectable1.content)).findFirst().ifPresent(tSelectable1 -> tSelectable.selected = tSelectable1.selected);
                });
            }

            contentList.replaceWith(customList.toArrayList());
            return this;
        }

        public void startSelection(int index) {
            activeSelection = true;
            if (index != -1)
                dragSelectTouchListener.setIsActive(true, index);

            CustomUtility.runGenericInterface(onChangeSelectionStatus, true);

            customRecycler.getAdapter().notifyDataSetChanged();
        }

        public CustomList<Selectable<T>> stopSelection() {
            activeSelection = false;
            CustomList<Selectable<T>> allSelected = getAllSelected();
            allSelected.forEach(tSelectable -> tSelectable.selected = false);

            CustomUtility.runGenericInterface(onChangeSelectionStatus, false);

            customRecycler.getAdapter().notifyDataSetChanged();
            return allSelected;
        }

        public MultiSelectHelper<T> clearSelection() {
            contentList.forEach(tSelectable -> tSelectable.setSelected(false));
            customRecycler.getAdapter().notifyDataSetChanged();
            updateToolbarTitle();
            return this;
        }

        public MultiSelectHelper<T> resetSelection() {
            if (applyInitialSelection != null) {
                applyInitialSelection.run(this, contentList);
                customRecycler.getAdapter().notifyDataSetChanged();
                updateToolbarTitle();
            } else
                clearSelection();
            return this;
        }

        public CustomList<Selectable<T>> getAllSelected() {
            return contentList.filter(Selectable::isSelected, false);
        }

        public CustomList<T> getAllSelectedContent() {
            return getAllSelected().map(Selectable::getContent);
        }

        public int getAllSelectedCount() {
            return getAllSelected().size();
        }


        public MultiSelectHelper<T> toggleSelection(Selectable<Media> mediaSelectable, int index) {
            mediaSelectable.toggleSelection();
            if (!checkSelectionStatusAndUpdateToolbarTitle())
                return this;
            customRecycler.getAdapter().notifyItemChanged(index);
            return this;
        }

        public void setSelected(int index, boolean selected) {
            Selectable<T> selectable = customRecycler.getObjectList().get(index);
            if (selectable.selected != selected) {
                selectable.selected = selected;
                customRecycler.getAdapter().notifyItemChanged(index);
                updateToolbarTitle();
            }
        }

        public boolean checkSelectionStatusAndUpdateToolbarTitle() {
            if (!allowEmpty && getAllSelected().isEmpty()) {
                stopSelection();
                return false;
            }
            updateToolbarTitle();
            return true;
        }

        public void updateToolbarTitle() {
            ((Toolbar) context.findViewById(R.id.toolbar)).setTitle(String.format(Locale.getDefault(), "Auswählen (%d)", getAllSelectedCount()));
        }
        //  <------------------------- Convenience -------------------------

        public static class Selectable<P> {
            public boolean selected;
            public P content;

            //  ------------------------- Constructor ------------------------->
            public Selectable(P content) {
                this.content = content;
            }
            //  <------------------------- Constructor -------------------------

            //  ------------------------- Getter & Setter ------------------------->
            public boolean isSelected() {
                return selected;
            }

            public Selectable<P> setSelected(boolean selected) {
                this.selected = selected;
                return this;
            }

            public P getContent() {
                return content;
            }

            public Selectable<P> setContent(P content) {
                this.content = content;
                return this;
            }
            //  <------------------------- Getter & Setter -------------------------


            //  ------------------------- Convenience ------------------------->
            public Selectable<P> toggleSelection() {
                selected = !selected;
                return this;
            }
            //  <------------------------- Convenience -------------------------
        }
    }

    private void setRecyclerMetrics() {
        boolean isPortrait = Utility.isPortrait(this);
        int width = Utility.getScreenSize(this).first - (isPortrait ? 0 : 100) - Utility.dpToPx(4);
        int columnCount = isPortrait ? 3 : 5;
        int sizePx = width / columnCount;
        int sizeDp = CustomUtility.pxToDp(sizePx);
        recyclerMetrics = CustomUtility.Triple.create(columnCount, sizePx, sizeDp);
    }

    private void loadRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recycler);

        setRecyclerMetrics();

        DragSelectTouchListener dragSelectTouchListener = DragSelectTouchListener.Companion.create(this, new MyDragSelectReceiver(), null);
        selectHelper = new MultiSelectHelper<>(this);
        selectHelper.dragSelectTouchListener = dragSelectTouchListener;
        selectHelper.allowEmpty = selectMode;
        selectHelper.onChangeSelectionStatus = select -> {
            ((CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout)).setTitleEnabled(!select);
            toolBarMenu.findItem(R.id.taskBar_media_add).setVisible(!select);

            if (selectMode) {
                toolBarMenu.findItem(R.id.taskBar_media_confirm).setVisible(select);
            } else {
                toolBarMenu.findItem(R.id.taskBar_media_edit).setVisible(select);
                toolBarMenu.findItem(R.id.taskBar_media_share).setVisible(select);
            }
        };
        selectHelper.applyInitialSelection = (mediaMultiSelectHelper, selectables) -> {
            ArrayList<String> selectedIds = getIntent().getStringArrayListExtra(EXTRA_SELECT_MODE);
            if (selectedIds != null && !selectedIds.isEmpty()) {
                selectables.forEach(mediaSelectable -> mediaSelectable.setSelected(selectedIds.contains(mediaSelectable.getContent().getUuid())));
            } else
                selectables.forEach(mediaSelectable -> mediaSelectable.setSelected(false));
        };

        mediaRecycler = new CustomRecycler<MultiSelectHelper.Selectable<Media>>(this, recyclerView)
                .addOptionalModifications(customRecycler -> selectHelper.customRecycler = customRecycler)
                .setItemLayout(R.layout.list_item_image)
                .setGetActiveObjectList(customRecycler -> {
                    allMediaList = new CustomList<>(database.mediaMap.values());
                    selectHelper.updateFromList(allMediaList, true);
                    CustomList<MultiSelectHelper.Selectable<Media>> filteredList = sortList(filterList(selectHelper.getContentList()));

                    TextView noItem = findViewById(R.id.no_item);
                    String text = media_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
//                    String viewsCountText = (views > 1 ? views + " Episoden" : (views == 1 ? "Eine" : "Keine") + " Episode") + " angesehen";
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText).append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    elementCount.setText(builder);

                    return filteredList;
                })
                .setSetItemContent((customRecycler, itemView, mediaSelectable) -> {
                    itemView.setLayoutParams(new FrameLayout.LayoutParams(recyclerMetrics.second, recyclerMetrics.second));
                    SelectMediaHelper.loadPathIntoImageView(mediaSelectable.content.getImagePath(), itemView, recyclerMetrics.third);

                    itemView.findViewById(R.id.listItem_image_selected).setVisibility(mediaSelectable.isSelected() ? View.VISIBLE : View.GONE);
                    View fullScreenButton = itemView.findViewById(R.id.listItem_image_fullScreen);
                    fullScreenButton.setVisibility(selectHelper.isActiveSelection() ? View.VISIBLE : View.GONE);
                    fullScreenButton.setOnClickListener(v -> setMediaScrollGalleryAndShow(Arrays.asList(mediaSelectable.getContent()), mediaRecycler.getObjectList().indexOf(mediaSelectable) * -1));
                })
                .setOnClickListener((customRecycler, view, mediaSelectable, index) -> {
                    if (selectHelper.isActiveSelection()) {
                        selectHelper.toggleSelection(mediaSelectable, index);
                    } else {
                        setMediaScrollGalleryAndShow(customRecycler.getObjectList().stream().map(MultiSelectHelper.Selectable::getContent).collect(Collectors.toList()), index);
                    }
                })
                .setOnLongClickListener((customRecycler, view, mediaSelectable, index) -> selectHelper.startSelection(index))
                .setRowOrColumnCount(recyclerMetrics.first)
                .generate();


        recyclerView.addOnItemTouchListener(dragSelectTouchListener);
    }

    private void reLoadRecycler() {
        mediaRecycler.reload();
    }

    private int indexOfMedia(Media media) {
        if (media == null)
            return -1;
        List<MultiSelectHelper.Selectable<Media>> list = mediaRecycler.getObjectList();
        Optional<MultiSelectHelper.Selectable<Media>> optional = list.stream().filter(mediaSelectable -> mediaSelectable.getContent() == media).findFirst();
        if (optional.isPresent()) {
            MultiSelectHelper.Selectable<Media> mediaSelectable = optional.get();
            return list.indexOf(mediaSelectable);
        }
        return -1;
    }
    //  <------------------------- Recycler -------------------------


    //  ------------------------- Edit ------------------------->
    private CustomDialog showEditMultipleDialog(CustomList<Media> oldMedia, boolean isShared) {
        if (!Utility.isOnline(this))
            return null;

        setResult(RESULT_OK);
        removeFocusFromSearch();

        boolean isAdd = oldMedia == null || oldMedia.isEmpty() || isShared;
        CustomList<Media> newMedia = isAdd && !isShared ? new CustomList<>() : oldMedia.stream().map(Media::clone).collect(Collectors.toCollection(CustomList::new));

        CustomList<String> mediaPersonIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
        CustomList<String> mediaCategoryIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);
        CustomList<String> mediaTagIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_TAG);
        CustomList<Media> subSelectionList = new CustomList<>();

        String dialogTitle = (oldMedia != null && oldMedia.size() == 1 ? ("Ein " + singular) : ("Mehrere " + plural)) + (isAdd ? " Hinzufügen" : " Bearbeiten");
        return CustomDialog.Builder(this)
                .setTitle(dialogTitle)
                .setView(R.layout.dialog_edit_media)
                .setSetViewContent((customDialog, view, reload) -> {
                    FrameLayout selectedMediaParent = view.findViewById(R.id.dialog_editMedia_selectParent);
                    SelectMediaHelper.Builder(this, newMedia)
                            .enableSubSelection(subSelectionList, subSelectedMedia -> {
                                customDialog.setTitle(subSelectedMedia.isEmpty() ? dialogTitle : String.format(Locale.getDefault(), "%s (Sub: %d)", dialogTitle, subSelectedMedia.size()));
                            })
                            .setParentView(selectedMediaParent)
                            .setHideAddButton(!isAdd)
                            .build();

                    view.findViewById(R.id.dialog_editMedia_editPersons).setOnClickListener(v -> {
                        Utility.showEditItemDialog(this, mediaPersonIdList, CategoriesActivity.CATEGORIES.MEDIA_PERSON, (customDialog1, selectedIds) -> {
                            mediaPersonIdList.replaceWith(selectedIds);
                            customDialog.reloadView();
                        });
                    });
                    view.findViewById(R.id.dialog_editMedia_editCategories).setOnClickListener(v -> {
                        Utility.showEditTreeItemDialog(this, mediaCategoryIdList, newList -> {
                            mediaCategoryIdList.replaceWith(newList);
                            customDialog.reloadView();
//                            ((TextView) view.findViewById(R.id.dialog_editMedia_categories)).setText(String.join(", ", mediaCategoryIdList.map(id -> MediaCategory.findObjectById(id).getName())));
                        }, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);
                    });
                    view.findViewById(R.id.dialog_editMedia_editTags).setOnClickListener(v -> {
                        Utility.showEditItemDialog(this, mediaTagIdList, CategoriesActivity.CATEGORIES.MEDIA_TAG, (customDialog1, selectedIds) -> {
                            mediaTagIdList.replaceWith(selectedIds);
                            customDialog.reloadView();
                        });
                    });

//                    view.findViewById(R.id.dialog_editMedia_editCategories).setOnLongClickListener(v -> {
//                        MediaCategory freizeit = new MediaCategory("Freizeit");
//                        freizeit.addChildren(Arrays.asList(new MediaCategory("Sport"), new MediaCategory("Spiele"), new MediaCategory("Werken")));
//                        MediaCategory feiern = new MediaCategory("Feiern");
//                        feiern.addChildren(Arrays.asList(new MediaCategory("Geburtstag"), new MediaCategory("Firma"), new MediaCategory("Silvester"), new MediaCategory("Vatertag")));
//                        MediaCategory reisen = new MediaCategory("Reisen");
//                        database.mediaCategoryMap.put(freizeit.getUuid(), freizeit);
//                        database.mediaCategoryMap.put(feiern.getUuid(), feiern);
//                        database.mediaCategoryMap.put(reisen.getUuid(), reisen);
//                        Database.saveAll();
//                        return true;
//                    });


                    ((TextView) view.findViewById(R.id.dialog_editMedia_persons)).setText(CategoriesActivity.joinCategoriesIds(mediaPersonIdList, CategoriesActivity.CATEGORIES.MEDIA_PERSON));
                    ((TextView) view.findViewById(R.id.dialog_editMedia_categories)).setText(CategoriesActivity.joinCategoriesIds(mediaCategoryIdList, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY));
                    ((TextView) view.findViewById(R.id.dialog_editMedia_tags)).setText(CategoriesActivity.joinCategoriesIds(mediaTagIdList, CategoriesActivity.CATEGORIES.MEDIA_TAG));
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addOptionalModifications(customDialog -> {
                    if (!isAdd) {
                        customDialog
                                .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                                    newMedia.forEach(media -> database.mediaMap.remove(media.getUuid()));
                                    reLoadRecycler();
                                    selectHelper.checkSelectionStatusAndUpdateToolbarTitle();
                                    Toast.makeText(this, (Database.saveAll_simple() ? plural : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();
                                })
                                .alignPreviousButtonsLeft()
                                .transformPreviousButtonToImageButton()
                                .addConfirmationDialogToLastAddedButton("", "", customDialog1 -> {
                                    String singOrPlur = newMedia.size() > 1 ? plural : singular;
                                    customDialog1
                                            .setTitle(singOrPlur + " Löschen")
                                            .setText("Möchstest du wirklich " + newMedia.size() + " " + singOrPlur + " löschen?");
                                });
                    }
                })
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> saveMultipleMedia(customDialog, oldMedia, newMedia, mediaPersonIdList, mediaCategoryIdList, mediaTagIdList, isShared))
                .addOnDialogShown(customDialog -> editDialog = customDialog)
                .addOnDialogDismiss(customDialog -> editDialog = null)
                .enableDoubleClickOutsideToDismiss(customDialog -> {
                    if (isAdd)
                        return !newMedia.isEmpty() || !mediaPersonIdList.isEmpty() || !mediaCategoryIdList.isEmpty() || !mediaTagIdList.isEmpty();
                    else {
                        CustomList<String> originalMediaPersonIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
                        CustomList<String> originalMediaCategoryIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);
                        CustomList<String> originalMediaTagIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_TAG);
                        return !originalMediaPersonIdList.equals(mediaPersonIdList) || !originalMediaCategoryIdList.equals(mediaCategoryIdList) || !originalMediaTagIdList.equals(mediaTagIdList);
                    }

                })
                // ToDo: ^^
                .show();
    }

    private void saveMultipleMedia(CustomDialog editDialog, CustomList<Media> oldMedia, List<Media> newMedia, CustomList<String> mediaPersonIdList, CustomList<String> mediaCategoryIdList, CustomList<String> mediaTagIdList, boolean isShared) {
        boolean isAdd = oldMedia == null || oldMedia.isEmpty() || isShared;
        int size = newMedia.size();

        if (isAdd) {
            newMedia.forEach(media -> media.getPersonIdList().addAll(mediaPersonIdList));
            newMedia.forEach(media -> media.getCategoryIdList().addAll(mediaCategoryIdList));
            newMedia.forEach(media -> media.getTagIdList().addAll(mediaTagIdList));

            newMedia.removeIf(media -> {
                Optional<Media> first = database.mediaMap.values().stream().filter(media1 -> media.getImagePath().equals(media1.getImagePath())).findFirst();
                if (first.isPresent()) {
                    Media media1 = first.get();
                    Utility.addAllDistinct(media.getPersonIdList(), media1.getPersonIdList());
                    Utility.addAllDistinct(media.getCategoryIdList(), media1.getCategoryIdList());
                    return true;
                } else
                    return false;
            });

            database.mediaMap.putAll(newMedia.stream().collect(Collectors.toMap(Media::getUuid, o -> o)));
        } else {
            // ToDo: Hier ansetzen
            applyNewIdList(newMedia, mediaPersonIdList, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
            applyNewIdList(newMedia, mediaCategoryIdList, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);
            applyNewIdList(newMedia, mediaTagIdList, CategoriesActivity.CATEGORIES.MEDIA_TAG);

            oldMedia.forEachCount((media, count) -> {
                media.getChangesFrom(newMedia.get(count));
            });

            if (CustomUtility.stringExists(searchQuery) && advancedQueryHelper.istExtraSearch(searchQuery)) {
                selectHelper.getAllSelected().forEach(mediaSelectable -> {
                    if (!Utility.containedInMedia(searchQuery, mediaSelectable.getContent(), filterTypeSet))
                        mediaSelectable.setSelected(false);
                });
                selectHelper.checkSelectionStatusAndUpdateToolbarTitle();
            }
        }

        Toast.makeText(this, (Database.saveAll_simple() ? size + " " + (size > 1 ? plural : singular) : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();

        reLoadRecycler();
    }

    private void applyNewIdList(List<Media> newMedia, CustomList<String> newIdList, CategoriesActivity.CATEGORIES category) {
        Utility.GenericReturnInterface<Media, List<String>> getCategoryList;
        switch (category) {
            default:
            case MEDIA_PERSON:
                getCategoryList = Media::getPersonIdList;
                break;
            case MEDIA_CATEGORY:
                getCategoryList = Media::getCategoryIdList;
                break;
            case MEDIA_TAG:
                getCategoryList = Media::getTagIdList;
                break;
        }

        CustomList<String> originalIdList = CategoriesActivity.getCategoriesIntersection(newMedia, category);
        CustomList<String> deletedPersonsIds = new CustomList<>(originalIdList);
        CustomList<String> addedPersonsIds = new CustomList<>(newIdList);
        deletedPersonsIds.removeAll(newIdList);
        addedPersonsIds.removeAll(originalIdList);

        newMedia.forEach(media -> {
            List<String> idList = getCategoryList.runGenericInterface(media);
            idList.removeAll(deletedPersonsIds);
            CustomList<String> customList = new CustomList<>(idList).addAllDistinct(addedPersonsIds);
            idList.clear();
            idList.addAll(customList);
        });
    }

    // --------------- SelectMediaHelper

    public static class SelectMediaHelper {
        private AppCompatActivity context;
        private ViewGroup parentView;
        private CustomList<Media> selectedMedia;
        private CustomList<Media> subSelectedMedia;
        private CustomUtility.GenericInterface<CustomList<Media>> onSubSelectionChanged;
        private CustomRecycler<Media> selectedRecycler;
        private boolean hideAddButton;
        private OverrideSelectionDialog overrideSelectionDialog;

        //  ------------------------- Constructor ------------------------->
        public SelectMediaHelper(AppCompatActivity context, CustomList<Media> selectedMedia) {
            this.context = context;
            this.selectedMedia = selectedMedia;
        }
        //  <------------------------- Constructor -------------------------


        //  ------------------------- Getter & Setter ------------------------->
        public SelectMediaHelper enableSubSelection(@Nullable CustomList<Media> subSelectedMedia, CustomUtility.GenericInterface<CustomList<Media>> onSubSelectionChanged) {
            this.subSelectedMedia = subSelectedMedia == null ? new CustomList<>() : subSelectedMedia;
            this.onSubSelectionChanged = onSubSelectionChanged;
            return this;
        }

        public SelectMediaHelper setParentView(ViewGroup parentView) {
            this.parentView = parentView;
            return this;
        }

        public SelectMediaHelper setSelectedRecycler(CustomRecycler<Media> selectedRecycler) {
            this.selectedRecycler = selectedRecycler;
            return this;
        }

        public SelectMediaHelper setHideAddButton(boolean hideAddButton) {
            this.hideAddButton = hideAddButton;
            return this;
        }

        public SelectMediaHelper enableHideAddButton() {
            hideAddButton = true;
            return this;
        }

        public SelectMediaHelper setOverrideSelectionDialog(OverrideSelectionDialog overrideSelectionDialog) {
            this.overrideSelectionDialog = overrideSelectionDialog;
            return this;
        }
        //  <------------------------- Getter & Setter -------------------------


        //  ------------------------- Convenience ------------------------->
        public void showSelectDialog(boolean replace) {
            ActivityResultHelper.addMultiFileChooserRequest(context, "image/* video/*", intent -> {
                CustomList<String> pathList = new CustomList<>();
                if (intent.getClipData() != null) {
                    int count = intent.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = intent.getClipData().getItemAt(i).getUri();
                        String path = ActivityResultHelper.getPath(context, imageUri);
                        pathList.add(path);
                    }
                } else if (intent.getData() != null) {
                    String imagePath = ActivityResultHelper.getPath(context, intent.getData());
                    pathList.add(imagePath);
                }
                if (replace)
                    selectedMedia.replaceWith(pathList.map(Media::new));
                else {
                    pathList.removeIf(s -> selectedMedia.stream().anyMatch(media -> media.getImagePath().equals(s)));
                    selectedMedia.addAll(pathList.map(Media::new));
                }

                if (selectedRecycler != null)
                    selectedRecycler.reload();
            });
        }

        public static void loadPathIntoImageView(String path, View view, int sizeDp) {
            Uri imageUri = Uri.fromFile(new File(path));
            RequestOptions myOptions = new RequestOptions()
                    .override(CustomUtility.dpToPx(sizeDp), CustomUtility.dpToPx(sizeDp))
                    .centerCrop();

            ImageView imageView = view.findViewById(R.id.listItem_image_imgaeView);
            Glide.with(imageView.getContext())
                    .load(imageUri)
                    .error(R.drawable.ic_broken_image)
                    .apply(myOptions)
                    .into(imageView);

            ImageView videoIndicator = view.findViewById(R.id.listItem_image_videoIndicator);
            if (Media.isVideo(path))
                videoIndicator.setVisibility(View.VISIBLE);
            else
                videoIndicator.setVisibility(View.GONE);


        }
        //  <------------------------- Convenience -------------------------


        //  ------------------------- Builder ------------------------->
        public static SelectMediaHelper Builder(AppCompatActivity context, CustomList<Media> selectedMedia) {
            return new SelectMediaHelper(context, selectedMedia);
        }

        public void build() {
            if (parentView != null)
                buildFragment();
        }

        public void buildAndSelect() {
            build();
            showSelectDialog(true);
        }

        private void buildFragment() {
            FrameLayout view = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.fragment_select_media_helper_selection, parentView, false);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentView.addView(view);
            selectedRecycler = new CustomRecycler<Media>(context, view.findViewById(R.id.fragment_selectMediaHelper_selection_recycler))
                    .setObjectList(selectedMedia)
                    .setItemLayout(R.layout.list_item_image)
                    .setSetItemContent((customRecycler, itemView, media) -> {
                        loadPathIntoImageView(media.getImagePath(), itemView, 120);
                        itemView.findViewById(R.id.listItem_image_selected).setVisibility(subSelectedMedia != null && subSelectedMedia.contains(media) ? View.VISIBLE : View.GONE);
                    })
                    .enableSwiping((objectList, direction, media) -> {
                        selectedMedia.remove(media);
                        if (subSelectedMedia != null) {
                            subSelectedMedia.remove(media);
                            onSubSelectionChanged.runGenericInterface(subSelectedMedia);
                        }
                    }, true, false)
                    .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                    .addOptionalModifications(customRecycler -> {
                        if (subSelectedMedia != null) {
                            customRecycler
                                    .setOnClickListener((customRecycler1, itemView, media, index) -> {
                                        if (!subSelectedMedia.remove(media))
                                            subSelectedMedia.add(media);
                                        customRecycler.getAdapter().notifyItemChanged(index);
                                        onSubSelectionChanged.runGenericInterface(subSelectedMedia);
                                    });
                        }
                    })
                    .generate();

            if (hideAddButton)
                view.findViewById(R.id.fragment_selectMediaHelper_selection_add).setVisibility(View.GONE);
            else {
                view.findViewById(R.id.fragment_selectMediaHelper_selection_add).setOnClickListener(v -> {
                    if (overrideSelectionDialog != null) {
                        overrideSelectionDialog.run(this, selectedMedia, false, () -> {
                            if (selectedRecycler != null)
                                selectedRecycler.reload();
                        });
                    } else
                        showSelectDialog(false);
                });
                view.findViewById(R.id.fragment_selectMediaHelper_selection_add).setOnLongClickListener(v -> {
                    if (overrideSelectionDialog != null) {
                        overrideSelectionDialog.run(this, selectedMedia, true,() -> {
                            if (selectedRecycler != null)
                                selectedRecycler.reload();
                        });
                    } else
                        showSelectDialog(true);
                    return true;
                });
            }
        }
        //  <------------------------- Builder -------------------------


        public interface OverrideSelectionDialog {
            void run(SelectMediaHelper helper, CustomList<Media> selectedMedia, boolean override, Runnable onSelected);
        }
    }
    //  <------------------------- Edit -------------------------


    //  ------------------------- ScrollGallery ------------------------->
    private void setupScrollGalleryView() {
        scrollGalleryView = findViewById(R.id.scroll_gallery_view);
        ((HackyViewPager) findViewById(com.veinhorn.scrollgalleryview.R.id.viewPager)).setOffscreenPageLimit(3);

        scrollGalleryView
                .setThumbnailSize(200)
                .setZoom(true)
                .withHiddenThumbnails(false)
                .hideThumbnailsOnClick(true)
                .addOnImageClickListener((position) -> {
                    toggleDescriptionAndButtonVisibility(this);
                })
                .setFragmentManager(getSupportFragmentManager());

        scrollGalleryView
                .addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        setCustomDescription(indexOfMedia(getCurrentGalleryMedia()));
                        View currentView = getCurrentViewFromViewPager(scrollGalleryView.getViewPager());
                        if (currentView != null) {
                            VideoView videoView = currentView.findViewById(R.id.imageFragment_video);
                            if (currentVideoPreview != null && currentVideoPreview != videoView)
                                currentVideoPreview.pause();
                            if (videoView.getVisibility() == View.VISIBLE) {
                                videoView.start();
                                currentVideoPreview = videoView;
                            }
                        }
                    }
                });

//        Utility.reflectionSet(scrollGalleryView, "useDefaultThumbnailsTransition", false);

        findViewById(R.id.scrollGalleryView_edit).setOnClickListener(v -> {
            int index = indexOfMedia(getCurrentGalleryMedia());
            List<MultiSelectHelper.Selectable<Media>> objectList = mediaRecycler.getObjectList();
            int previousSize = objectList.size();
            CustomDialog customDialog = showEditMultipleDialog(new CustomList<>(objectList.get(index).getContent()), false);
            if (currentVideoPreview != null)
                currentVideoPreview.pause();
            if (customDialog != null) {
                customDialog
                        .addOnDialogDismiss(customDialog1 -> {
                            int nowSize = objectList.size();
                            if (previousSize != nowSize)
                                scrollGalleryView.removeMedia(index);
                            setCustomDescription(index);
                            if (currentVideoPreview != null)
                                currentVideoPreview.start();
                        });
            }
        });

        View changeRotationButton = findViewById(R.id.scrollGalleryView_rotate);
        applyChangeRotationButton(this, changeRotationButton);
    }

    public static void applyChangeRotationButton(AppCompatActivity activity, View changeRotationButton) {
        if (isAutoRotationOn(activity))
            changeRotationButton.setVisibility(View.GONE);
        activity.getContentResolver().registerContentObserver(android.provider.Settings.System.getUriFor(android.provider.Settings.System.ACCELEROMETER_ROTATION), true,
                new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange) {
                        changeRotationButton.setVisibility(isAutoRotationOn(activity) ? View.GONE : View.VISIBLE);
                    }
                });
        changeRotationButton.setOnClickListener(v -> {
            if (Utility.isPortrait(activity)) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });
    }

    public static boolean isAutoRotationOn(AppCompatActivity activity) {
        return android.provider.Settings.System.getInt(activity.getContentResolver(), android.provider.Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
    }

    private void setCustomDescription(int index) {
        Media media = mediaRecycler.getObjectList().get(index).getContent();
        LinearLayout linearLayout = findViewById(R.id.customImageDescription);
        List<TextView> textViewList = new ArrayList<>();

        linearLayout.removeAllViews();

        if (!media.getPersonIdList().isEmpty()) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_PERSON, textView, media.getPersonIdList());
            textView.setText(new SpannableStringBuilder().append("P: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(textView.getText()));
            textViewList.add(textView);
        }

        if (!media.getCategoryIdList().isEmpty()) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, textView, media.getCategoryIdList());
            textView.setText(new SpannableStringBuilder().append("K: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(textView.getText()));
            textViewList.add(textView);
        }

        if (!media.getTagIdList().isEmpty()) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_TAG, textView, media.getTagIdList());
            textView.setText(new SpannableStringBuilder().append("T: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(textView.getText()));
            textViewList.add(textView);
        }

        long lastModified = new File(media.getImagePath()).lastModified();
        if (lastModified > 0) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setText(new SpannableStringBuilder().append("D: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(Utility.formatDate("dd.MM.yyyy", new Date(lastModified))));
            if (textViewList.size() <= 2)
                textViewList.add(textView);
            else
                textViewList.add(textViewList.size() == 3 ? 1 : 2, textView);
        }

        if (textViewList.size() <= 3) {
            textViewList.forEach(linearLayout::addView);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
        } else {
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout childLeft = new LinearLayout(this);
            CustomUtility.setPadding(childLeft, 0, 0, 5, 0);
            childLeft.setOrientation(LinearLayout.VERTICAL);
            LinearLayout childRight = new LinearLayout(this);
            childRight.setOrientation(LinearLayout.VERTICAL);
            if (textViewList.size() == 4) {
                textViewList.subList(0, 2).forEach(childLeft::addView);
                textViewList.subList(2, 4).forEach(childRight::addView);
            } else {
                textViewList.subList(0, 3).forEach(childLeft::addView);
                textViewList.subList(3, 5).forEach(childRight::addView);
            }
            linearLayout.addView(childLeft);
            linearLayout.addView(childRight);
        }
    }

    private void setMediaScrollGalleryAndShow(List<Media> shownMediaList, int index) {
        setMediaScrollGallery(shownMediaList);
        showScrollGallery(index);
    }

    private void setMediaScrollGallery(List<Media> shownMediaList) {
        scrollGalleryView.addMedia(
                shownMediaList.stream().map(media -> {
                    if (Media.isVideo(media))
                        return MediaInfo.mediaLoader(new CustomVideoLoader(media));
                    else
                        return MediaInfo.mediaLoader(new CustomGlideImageLoader(media));
                }).collect(Collectors.toList()));
    }

    private void clearScrollGallery() {
        scrollGalleryView.clearGallery();
    }

    private void showScrollGallery(int index) {
        scrollGalleryShown = true;
        removeFocusFromSearch();

        if (findViewById(com.veinhorn.scrollgalleryview.R.id.thumbnails_scroll_view).getVisibility() == View.GONE) {
            findViewById(R.id.thumbnails_scroll_view).setVisibility(View.VISIBLE);
            findViewById(R.id.customImageDescription).setVisibility(View.VISIBLE);
            findViewById(R.id.scrollGalleryView_edit).setVisibility(View.VISIBLE);
            if (!isAutoRotationOn(this))
                findViewById(R.id.scrollGalleryView_rotate).setVisibility(View.VISIBLE);
            Utility.reflectionSet(scrollGalleryView, "isThumbnailsHidden", false);
//            try {
//                Field field;
//                field = ScrollGalleryView.class.getDeclaredField("isThumbnailsHidden");
//                field.setAccessible(true);
//                field.set(scrollGalleryView, false);
//            } catch (NoSuchFieldException | IllegalAccessException ignored) {
//            }
        }

        setCustomDescription(Math.abs(index));
        scrollGalleryView.setVisibility(View.VISIBLE);
        scrollGalleryView.setCurrentItem(Math.max(0, index));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        LinearLayout thumbnailContainer = findViewById(R.id.thumbnails_container);
        HorizontalScrollView thumbnailScrollView = findViewById(R.id.thumbnails_scroll_view);
        thumbnailContainer.postDelayed(() -> {
            int[] thumbnailCoords = new int[2];
            thumbnailContainer.getChildAt(Math.max(0, index)).getLocationOnScreen(thumbnailCoords);
            int thumbnailCenterX = thumbnailCoords[0] + 200 / 2;
            int thumbnailDelta = 1440 / 2 - thumbnailCenterX;

            thumbnailScrollView.scrollTo(-thumbnailDelta, 0);
        }, 300);
    }

    private void hideScrollGallery() {
        scrollGalleryShown = false;
        scrollGalleryView.setVisibility(View.GONE);
        int currentItem = scrollGalleryView.getCurrentItem();
//        mediaRecycler.scrollTo(currentItem, false);
        if (scrollGalleryView.getViewPager().getChildCount() > 1)
            mediaRecycler.getRecycler().getLayoutManager().scrollToPosition(currentItem);
        // ToDo: Funktioniert nicht wirklich ^^
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        clearScrollGallery();
        currentVideoPreview = null;
    }

    public static void toggleDescriptionAndButtonVisibility(AppCompatActivity activity) {
        int visibility = activity.findViewById(com.veinhorn.scrollgalleryview.R.id.thumbnails_scroll_view).getVisibility();
        CustomUtility.GenericInterface<VideoView> setVideoButtonVisibility = videoView -> {
            FrameLayout viewParent = (FrameLayout) videoView.getParent();
            viewParent.findViewById(R.id.imageFragment_playVideo).setVisibility(visibility);
            viewParent.findViewById(R.id.imageFragment_volumeLayout).setVisibility(visibility);
        };
        ViewGroup parent = (ViewGroup) getCurrentViewFromViewPager(((MediaActivity) activity).scrollGalleryView.getViewPager());
        Set<VideoView> keySet = new HashSet<>((((MediaActivity) activity).currentMediaPlayerMap.keySet()));
        VideoView videoView = null;
        if (parent != null) {
            videoView = parent.findViewById(R.id.imageFragment_video);
            keySet.remove(videoView);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> keySet.forEach(setVideoButtonVisibility::runGenericInterface), 100);
//        keySet.forEach(setVideoButtonVisibility::runGenericInterface);

        View view = activity.findViewById(R.id.customImageDescription);
//        TransitionManager.beginDelayedTransition(activity.findViewById(R.id.scrollGalleryView_root));
        TransitionManager.beginDelayedTransition((ViewGroup) view.getParent());
        view.setVisibility(visibility);
        activity.findViewById(R.id.scrollGalleryView_edit).setVisibility(visibility);
        if (!isAutoRotationOn(activity))
            activity.findViewById(R.id.scrollGalleryView_rotate).setVisibility(visibility);
        if (parent != null && videoView.getVisibility() == View.VISIBLE)
            setVideoButtonVisibility.runGenericInterface(videoView);
    }

    public static void shouldVideoPreviewStart(MediaActivity activity, VideoView videoView, Media media) {
        int currentItem = activity.scrollGalleryView.getCurrentItem();
        if (currentItem == 0 && activity.selectHelper.isActiveSelection()) {
            videoView.start();
            activity.currentVideoPreview = videoView;
        } else if (activity.mediaRecycler.getObjectList().get(currentItem).getContent() == media) {
            videoView.start();
            activity.currentVideoPreview = videoView;
        }
    }

    public static View getCurrentViewFromViewPager(ViewPager viewPager) {
        try {
            final int currentItem = viewPager.getCurrentItem();
            for (int i = 0; i < viewPager.getChildCount(); i++) {
                final View child = viewPager.getChildAt(i);
                final ViewPager.LayoutParams layoutParams = (ViewPager.LayoutParams) child.getLayoutParams();

                Field f = layoutParams.getClass().getDeclaredField("position"); //NoSuchFieldException
                f.setAccessible(true);
                int position = (Integer) f.get(layoutParams); //IllegalAccessException

                if (!layoutParams.isDecor && currentItem == position) {
                    return child;
                }
            }
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ignored) {
        }
        return null;
    }

    private Media getCurrentGalleryMedia() {
        try {
            Field mListOfMedia = ScrollGalleryView.class.getDeclaredField("mListOfMedia");
            mListOfMedia.setAccessible(true);
            MediaInfo mediaInfo = ((ArrayList<MediaInfo>) mListOfMedia.get(scrollGalleryView)).get(scrollGalleryView.getCurrentItem());
            MediaLoader loader = mediaInfo.getLoader();
            if (loader instanceof CustomGlideImageLoader)
                return ((CustomGlideImageLoader) loader).getMedia();
            else if (loader instanceof CustomVideoLoader)
                return ((CustomVideoLoader) loader).getMedia();
            else
                return null;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private boolean isScrollViewVisible() {
        return scrollGalleryView.getVisibility() == View.VISIBLE;
    }
    //  <------------------------- ScrollGallery -------------------------


    /**
     * ------------------------- Share ------------------------->
     */
    private void handleIncomingShare() {
        CustomList<Uri> sharedMediaUris = new CustomList<>();
        if (getIntent().getAction().endsWith("SEND"))
            sharedMediaUris.add(getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
        else
            sharedMediaUris.addAll((ArrayList) getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM));

        CustomList<Media> sharedMedia = sharedMediaUris.map(uri -> new Media(ActivityResultHelper.getPath(this, uri)));

        showEditMultipleDialog(sharedMedia, true);
    }

    private void shareMedia(CustomList<Media> mediaList) {

        if (mediaList.isEmpty()) {
            Toast.makeText(this, "Medien Auswählen", Toast.LENGTH_SHORT).show();
        } else {
            String type = new CustomList<>(
                    mediaList.stream().anyMatch(media -> !Media.isVideo(media)) ? "image/*" : null,
                    mediaList.stream().anyMatch(Media::isVideo) ? "video/*" : null
            ).stream().filter(Objects::nonNull).collect(Collectors.joining(" "));

            Intent shareIntent = new Intent().setType(type);
            ArrayList<Uri> files = new ArrayList<>(mediaList.map(media -> FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider", new File(media.getImagePath()))));
            if (mediaList.size() == 1) {
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, files.get(0));
            } else {
                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            }
            startActivity(Intent.createChooser(shareIntent, "Medien Teilen Mit..."));
        }
    }

    /**
     * <------------------------- Share -------------------------
     */

    //  ------------------------- ToolBar ------------------------->
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_media, menu);
        toolBarMenu = menu;
        selectHelper.toolBarMenu = toolBarMenu;
        menu.findItem(R.id.taskBar_media_edit).setIconTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{Color.WHITE}));

        if (selectMode) {
            selectHelper.startSelection(-1);
            selectHelper.updateToolbarTitle();
        }


        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_media_add:
                showEditMultipleDialog(null, false);
//                SelectMediaHelper.Builder(this).showSelection();
                break;
            case R.id.taskBar_media_edit:
                showEditMultipleDialog(selectHelper.getAllSelectedContent(), false);
                break;
            case R.id.taskBar_media_share:
                shareMedia(selectHelper.getAllSelectedContent());
                break;
            case R.id.taskBar_media_confirm:
//                if (selectHelper.getAllSelectedCount() > 0)
                    setResult(RESULT_OK, new Intent().putExtra(EXTRA_SELECT_MODE, selectHelper.getAllSelectedContent().map(ParentClass::getUuid)));
//                else
//                    setResult(RESULT_CANCELED);
                finish();
                break;

            case android.R.id.home:
                if (selectHelper.isActiveSelection()) {
                    if (selectMode)
                        finish();
                    else
                        selectHelper.stopSelection();
                } else {
                    if (getCallingActivity() == null)
                        startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
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
        media_search.clearFocus();
    }
    //  <------------------------- ToolBar -------------------------


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_STORAGE_MANAGER) {
            if (checkAndRequestStoragePermission())
                loadDatabase();
        } else if (requestCode == CustomVideoLoader.START_ACTIVITY_VIDEO_PLAYER) {
            if (currentVideoPreview != null) {
                currentVideoPreview.start();
                currentVideoPreview.seekTo(Integer.parseInt(data.getExtras().getString(VideoPlayerActivity.EXTRA_TIME)));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentVideoPreview != null && editDialog == null) {
            currentVideoPreview.start();
        }
    }

    @Override
    public void onBackPressed() {
        if (scrollGalleryShown) {
            hideScrollGallery();
        } else if (advancedQueryHelper.handleBackPress(this)) {
            return;
        } else if (selectHelper.isActiveSelection()) {
            if (selectMode) {
                CustomList<MultiSelectHelper.Selectable<Media>> allSelected = selectHelper.getAllSelected();
                if (allSelected.size() > 0) {
                    selectHelper.resetSelection();
                    if (Objects.equals(allSelected, selectHelper.getAllSelected()))
                        finish();
                } else
                    finish();
            } else
                selectHelper.stopSelection();
        } else
            super.onBackPressed();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (scrollGalleryShown && onSwipeTouchListener != null)
            onSwipeTouchListener.onTouch(null, ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setRecyclerMetrics();
        ((GridLayoutManager) mediaRecycler.getRecycler().getLayoutManager()).setSpanCount(recyclerMetrics.first);
        reLoadRecycler();
    }
}

/*
KONZEPT:
• Sammlung von Fotos und Videos

• Kategorien:
    • Personen
    • Tags (Essen, Tiere, Bauwerke, Landschaft, Personen)
    • Gruppen (Automatische Tags?) (Bsp. Feiern, Reisen, Freizeit)
        • Untergruppen
    • Events
        • Untergruppen


Features:
• Mehrere Tags auf mehrere Medien

Libraries:
• https://github.com/VEINHORN/ScrollGalleryView (ScrollGallery)
• https://android-arsenal.com/details/1/7803 (Media Slider)
 */