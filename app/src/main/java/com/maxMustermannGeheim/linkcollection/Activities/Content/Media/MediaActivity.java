package com.maxMustermannGeheim.linkcollection.Activities.Content.Media;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.transition.TransitionManager;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.BuildConfig;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaEvent;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomGlideImageLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomVideoLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.finn.androidUtilities.FastScrollRecyclerViewHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.veinhorn.scrollgalleryview.HackyViewPager;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
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
import java.util.stream.Collectors;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class MediaActivity extends AppCompatActivity {
    private static final String TAG = "MediaActivityCustom";

    public static final String EXTRA_SELECT_MODE = "EXTRA_SELECT_MODE";
    private final String ADVANCED_SEARCH_CRITERIA__PERSON = "p";
    private final String ADVANCED_SEARCH_CRITERIA__CATEGORY = "c";
    private final String ADVANCED_SEARCH_CRITERIA__TAG = "t";
    private final String ADVANCED_SEARCH_CRITERIA__EVENT = "e";


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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Toast.makeText(this, "Die Android Version ist zu alt", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
                public boolean onSwipeTop() {
                    View view = getCurrentViewFromViewPager(scrollGalleryView.getViewPager());
                    if (view != null) {
                        PhotoView photo = view.findViewById(R.id.photoView);
                        if (photo.getScale() <= 1.0002f)
                            showDetailsDialog(getCurrentGalleryMedia());
                    } else
                        showDetailsDialog(getCurrentGalleryMedia());
                    return false;
                }

                @Override
                public boolean onSwipeBottom() {
                    View view = getCurrentViewFromViewPager(scrollGalleryView.getViewPager());
                    if (view != null) {
                        PhotoView photo = view.findViewById(R.id.photoView);
                        if (photo.getScale() <= 1.0002f)
                            hideScrollGallery();
                    } else
                        hideScrollGallery();
                    return false;
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
                    .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<MultiSelectHelper.Selectable<Media>, MediaEvent>(ADVANCED_SEARCH_CRITERIA__EVENT, Helpers.AdvancedQueryHelper.PARENT_CLASS_PATTERN)
                            .setCategory(CategoriesActivity.CATEGORIES.MEDIA_EVENT)
                            .setParser(sub -> (MediaEvent) ParentClass_Tree.findObjectByName(CategoriesActivity.CATEGORIES.MEDIA_EVENT, sub, false))
                            .setBuildPredicate(mediaEvent -> {
                                if (mediaEvent == null)
                                    return null;
                                return mediaSelectable -> mediaEvent.getMediaIdList().contains(mediaSelectable.getContent().getUuid());
                            }))
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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
        /*
        allMediaList = new CustomList<>(database.mediaMap.values());
        CustomList<MultiSelectHelper.Selectable<Media>> collect = allMediaList.stream().map(MultiSelectHelper.Selectable::new).collect(Collectors.toCollection(CustomList::new));

        CustomList<String> urls = new CustomList<>("https://www.anti-bias.eu/wp-content/uploads/2015/01/shutterstock_92612287-e1420280083718.jpg",
                "https://povodu.ru/wp-content/uploads/2016/04/pochemu-korabl-derzitsa-na-vode.jpg",
                "https://www.fotomagazin.de/sites/www.fotomagazin.de/files/styles/landing_lead_mobile/public/fm/2019/aufmacher_fotowettbewerb_haida_landschaft.jpg?itok=yk9rEzGY&timestamp=1562245157",
                "https://upload.wikimedia.org/wikipedia/commons/a/af/Landschaft_in_Sachsen%2C_Bernsdorf..2H1A4651%D0%A6%D0%A8.jpg",
                "https://i.pinimg.com/originals/22/7e/36/227e36c82a5341e4efdc9654e802dcdb.jpg",
                "https://www.reisebüro-sasbachwalden.de/wp-content/uploads/2019/09/Indien1-1024x640.jpg",
                "https://www.stuttgarter-nachrichten.de/media.media.eb344091-b684-4128-9817-606016cd9179.original1024.jpg",
                "https://www.zg.ch/behoerden/baudirektion/arv/natur-landschaft/landschaft_block_1/@@images/cd95975d-c541-4d75-aefd-5e05bb252e68.jpeg"
        );

        mediaRecycler = new CustomRecycler<MultiSelectHelper.Selectable<Media>>(this, recyclerView)
                .setItemLayout(R.layout.list_item_image)
                .setObjectList(collect)
                .setSetItemContent((customRecycler, itemView, player, index) -> {
                    ImageView imageView = itemView.findViewById(R.id.listItem_image_imageView);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(475, 475);
                    itemView.setLayoutParams(params);

                    int index = customRecycler.getObjectList().indexOf(player);
                    RequestOptions myOptions = new RequestOptions()
                            .override(475, 275)
                            .centerCrop();

                    Glide.with(this)
                            .load(urls.get(index % (urls.size())))
//                            .apply(myOptions)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imageView);
                })
                .setRowOrColumnCount(3)
                .generate();

        mediaRecycler.getLayoutManager().scrollToPositionWithOffset(CustomUtility.randomInteger(25, 50), 0);

        if (true)
            return;
        */

        final int[] size = new int[1];
        mediaRecycler = new CustomRecycler<MultiSelectHelper.Selectable<Media>>(this, recyclerView)
                .addOptionalModifications(customRecycler -> selectHelper.customRecycler = customRecycler)
                .setItemLayout(R.layout.list_item_image)
                .setGetActiveObjectList(customRecycler -> {
                    allMediaList = new CustomList<>(database.mediaMap.values());
                    selectHelper.updateFromList(allMediaList, true);
                    CustomList<MultiSelectHelper.Selectable<Media>> filteredList = sortList(filterList(selectHelper.getContentList()));

                    TextView noItem = findViewById(R.id.no_item);
                    String text = media_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    size[0] = filteredList.size();

                    noItem.setText(size[0] == 0 ? text : "");
                    String elementCountText = size[0] > 1 ? size[0] + " Elemente" : (size[0] == 1 ? "Ein" : "Kein") + " Element";
//                    String viewsCountText = (views > 1 ? views + " Episoden" : (views == 1 ? "Eine" : "Keine") + " Episode") + " angesehen";
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText).append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    elementCount.setText(builder);

                    return filteredList;
                })
                .setSetItemContent((customRecycler, itemView, mediaSelectable, index) -> {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(recyclerMetrics.second, recyclerMetrics.second);
                    if (index < recyclerMetrics.first)
                        params.setMargins(0, 7, 0, 0);
                    else if (index + 1 >= size[0] - ((size[0] - 1) % recyclerMetrics.first))
                        params.setMargins(0, 0, 0, CustomUtility.dpToPx(25));
                    itemView.setLayoutParams(params);

                    SelectMediaHelper.loadPathIntoImageView(mediaSelectable.content.getImagePath(), itemView, recyclerMetrics.second, 2d/3d);

                    itemView.findViewById(R.id.listItem_image_selected).setVisibility(mediaSelectable.isSelected() ? View.VISIBLE : View.GONE);
                    View fullScreenButton = itemView.findViewById(R.id.listItem_image_fullScreen);
                    fullScreenButton.setVisibility(selectHelper.isActiveSelection() ? View.VISIBLE : View.GONE);
                    fullScreenButton.setOnClickListener(v -> setMediaScrollGalleryAndShow(Arrays.asList(mediaSelectable.getContent()), mediaRecycler.getObjectList().indexOf(mediaSelectable) * -1));
//                    CustomUtility.logD(TAG, "loadRecycler: %d | %d | %d", index, customRecycler.getLayoutManager().findFirstVisibleItemPosition(), customRecycler.getLayoutManager().findLastVisibleItemPosition());
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
                .enableFastScroll(null, false, Pair.create(7, CustomUtility.dpToPx(25)), (customRecycler, mediaSelectable, integer) -> {
                    long lastModified = new File(mediaSelectable.getContent().getImagePath()).lastModified();
                    if (lastModified != 0)
                        return dateFormat.format(new Date(lastModified));
                    return "Fehler";
                })
                .generate();

        ArrayList<String> selectedIds = getIntent().getStringArrayListExtra(EXTRA_SELECT_MODE);
        if (selectedIds != null && !selectedIds.isEmpty()) {
            mediaRecycler.getObjectList().stream().filter(mediaSelectable -> selectedIds.contains(mediaSelectable.getContent().getUuid())).findFirst().ifPresent(mediaSelectable -> {
                int index = mediaRecycler.getObjectList().indexOf(mediaSelectable);
                    mediaRecycler.getLayoutManager().scrollToPositionWithOffset(index, 0);
            });
        }

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
                                .transformLastAddedButtonToImageButton()
                                .addConfirmationDialogToLastAddedButton("", "", customDialog1 -> {
                                    String singOrPlur = newMedia.size() > 1 ? plural : singular;
                                    customDialog1
                                            .setTitle(singOrPlur + " Löschen")
                                            .setText("Möchtest du wirklich " + newMedia.size() + " " + singOrPlur + " löschen?");
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
            List<String> idList = getCategoryList.run(media);
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


        public static void loadPathIntoImageView(String path, View view, int sizePx, double scale) {
            ImageView imageView = view.findViewById(R.id.listItem_image_imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            sizePx *= scale;

            Uri imageUri = Uri.fromFile(new File(path));
            RequestOptions myOptions = new RequestOptions()
                    .override(sizePx, sizePx)
                    .centerCrop();

            Glide.with(imageView.getContext())
                    .load(imageUri)
                    .error(R.drawable.ic_broken_image)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
                    .apply(myOptions)
                    .into(imageView);
//                    .into(new SimpleTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(@NotNull Bitmap resource, Transition<? super Bitmap> transition) {
//                            imageView.setImageBitmap(resource);
//                        }
//                    });
//                    .listener(new RequestListener<Drawable>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
////                            if (resource instanceof BitmapDrawable)
////                                imageView.setImageBitmap(((BitmapDrawable) resource).getBitmap());
////                            else if (resource instanceof GifDrawable)
////                                imageView.setImageBitmap(((GifDrawable) resource).getFirstFrame());
//////                            imageView.setImageDrawable(resource);
//                            return false;
//                        }
//                    })
//                    .into(new DrawableImageViewTarget(imageView) {
//                        @Override
//                        protected void setResource(@Nullable Drawable resource) {
//                            super.setResource(resource);
//                            Log.d(TAG, String.format("setResource: "));
//                        }
//                    });

/*
            Glide.with(imageView.getContext())
                    .load(imageUri)
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .apply(myOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setImageBitmap(BitmapFactory.decodeResource(imageView.getContext().getResources(), R.drawable.simpsons_movie_poster));
//                            imageView.setImageDrawable(resource);
                            return true;
                        }
                    })
                    .into(new DrawableImageViewTarget(new ImageView(imageView.getContext())) {
                        @Override
                        protected void setResource(@Nullable Drawable resource) {
//                            super.setResource(resource);
                        }
                    });
*/
//                    .into(imageView);
//            new Handler(Looper.myLooper()).postDelayed(() -> {
//
//
//                // ToDo: pro reingeladenen Foto wird nach oben gescrollt
//
//            }, 2000);

            ImageView videoIndicator = view.findViewById(R.id.listItem_image_videoIndicator);
            if (Media.isVideo(path))
                videoIndicator.setVisibility(View.VISIBLE);
            else
                videoIndicator.setVisibility(View.GONE);


        }

        public static int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(pathName, options);
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
            TextView emptyText = view.findViewById(R.id.fragment_selectMediaHelper_selection_empty);
            parentView.addView(view);
            selectedRecycler = new CustomRecycler<Media>(context, view.findViewById(R.id.fragment_selectMediaHelper_selection_recycler))
                    .setGetActiveObjectList(customRecycler -> {
                        emptyText.setVisibility(selectedMedia.isEmpty() ? View.VISIBLE : View.GONE);
                        return selectedMedia;
                    })
                    .setItemLayout(R.layout.list_item_image)
                    .setSetItemContent((customRecycler, itemView, media, index) -> {
                        loadPathIntoImageView(media.getImagePath(), itemView, CustomUtility.dpToPx(120), 1);
                        itemView.findViewById(R.id.listItem_image_selected).setVisibility(subSelectedMedia != null && subSelectedMedia.contains(media) ? View.VISIBLE : View.GONE);
                    })
                    .enableSwiping((customRecycler, objectList, direction, media, index) -> {
                        selectedMedia.remove(media);
                        emptyText.setVisibility(selectedMedia.isEmpty() ? View.VISIBLE : View.GONE);
                        if (subSelectedMedia != null) {
                            subSelectedMedia.remove(media);
                            onSubSelectionChanged.run(subSelectedMedia);
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
                                        onSubSelectionChanged.run(subSelectedMedia);
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


    /**  ------------------------- Details ------------------------->  */
    private void showDetailsDialog(Media media) {
        if (new File(media.getImagePath()).lastModified() == 0)
            return;
        CustomDialog.Builder(this)
                .setView(R.layout.dialog_detail_media)
                .setTitle("Details")
                .setSetViewContent((customDialog, view, reload) -> {
                    ImageView imageView = view.findViewById(R.id.dialog_detailMedia_poster);
                    Utility.loadUrlIntoImageView(this, imageView, media.getImagePath(), null, null, () -> Utility.roundImageView(imageView, 4));
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_PERSON, view.findViewById(R.id.dialog_detailMedia_parson), media.getPersonIdList());
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, view.findViewById(R.id.dialog_detailMedia_category), media.getCategoryIdList());
                    Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_TAG, view.findViewById(R.id.dialog_detailMedia_tag), media.getTagIdList());
                    List<String> mediaEventIdList = MediaEventActivity.getEventsContaining(media).map(ParentClass::getUuid);
                    if (!mediaEventIdList.isEmpty()) {
                        view.findViewById(R.id.dialog_detailMedia_event_layout).setVisibility(View.VISIBLE);
                        Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_EVENT, view.findViewById(R.id.dialog_detailMedia_event), mediaEventIdList);
                    }
                    ((TextView) view.findViewById(R.id.dialog_detailMedia_date)).setText(Utility.formatDate("dd.MM.yyyy   HH:mm:ss 'Uhr'", new Date(new File(media.getImagePath()).lastModified())));
                    ((TextView) view.findViewById(R.id.dialog_detailMedia_path)).setText(media.getImagePath());
                })
                .show();
    }
    /**  <------------------------- Details -------------------------  */


    //  ------------------------- ScrollGallery ------------------------->
    private void setupScrollGalleryView() {
        scrollGalleryView = findViewById(R.id.scroll_gallery_view);
        HackyViewPager viewPager = (HackyViewPager) findViewById(com.veinhorn.scrollgalleryview.R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);

        final View[] currentView = {null};

        HackyViewPager hackyViewPager = new HackyViewPager(this) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (currentView[0] != null || (currentView[0] = getCurrentViewFromViewPager(scrollGalleryView.getViewPager())) != null) {
                    PhotoView photoView = currentView[0].findViewById(R.id.photoView);
                    if (photoView.getScale() > 1.0002f)
                        return false;
                }
                return super.onInterceptTouchEvent(ev);
            }
        };

        CustomUtility.replaceView(viewPager, hackyViewPager, (source, target) -> target.setId(source.getId()));


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
                        currentView[0] = getCurrentViewFromViewPager(scrollGalleryView.getViewPager());
                        if (currentView[0] != null) {
                            VideoView videoView = currentView[0].findViewById(R.id.imageFragment_video);
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

        findViewById(R.id.scrollGalleryView_share).setOnClickListener(v -> shareMedia(new CustomList<>(getCurrentGalleryMedia())));

        View changeRotationButton = findViewById(R.id.scrollGalleryView_rotate);
        setupThumbnailMap();
        applyChangeRotationButton(this, changeRotationButton);
        setDisplayProps();
//        LinearLayout thumbnailContainer = findViewById(R.id.thumbnails_container);
//        HorizontalScrollView thumbnailScrollView = findViewById(R.id.thumbnails_scroll_view);
//        thumbnailContainer.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(View v) {
//                Log.d(TAG, String.format("onViewAttachedToWindow: %d | %d", thumbnailScrollView.getWidth(), thumbnailContainer.getWidth()));
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//
//            }
//        });
//        thumbnailContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Log.d(TAG, String.format("onViewAttachedToWindow: %d | %d", thumbnailScrollView.getWidth(), thumbnailContainer.getWidth()));
//            }
//        });

    }

    private void setDisplayProps() {
        boolean isPortrait = Utility.isPortrait(this);
        Point point = new Point();
        Rect bounds = getWindowManager().getCurrentWindowMetrics().getBounds();
        point.set(bounds.width() + (isPortrait ? 0 : 100), bounds.height());
        Utility.reflectionSet(scrollGalleryView, "displayProps", point);
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

        List<String> mediaEventIdList = MediaEventActivity.getEventsContaining(media).map(ParentClass::getUuid);
        if (!mediaEventIdList.isEmpty()) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_EVENT, textView, mediaEventIdList);
            textView.setText(new SpannableStringBuilder().append("E: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(textView.getText()));
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
        scrollGalleryView.addMedia(shownMediaList.stream().map(media -> {
                    if (Media.isVideo(media))
                        return MediaInfo.mediaLoader(new CustomVideoLoader(media));
                    else
                        return MediaInfo.mediaLoader(new CustomGlideImageLoader(media));
                }).collect(Collectors.toList()));
    }

    private void clearScrollGallery() {
        scrollGalleryView.clearGallery();
        ((HorizontalScrollView) findViewById(R.id.thumbnails_scroll_view)).setScrollX(0);
        thumbnailLoadMap.clear();
    }

    private void showScrollGallery(int index) {
        scrollGalleryShown = true;
        removeFocusFromSearch();

        if (findViewById(com.veinhorn.scrollgalleryview.R.id.thumbnails_scroll_view).getVisibility() == View.GONE) {
            findViewById(R.id.thumbnails_scroll_view).setVisibility(View.VISIBLE);
            findViewById(R.id.customImageDescription).setVisibility(View.VISIBLE);
            findViewById(R.id.scrollGalleryView_edit).setVisibility(View.VISIBLE);
            findViewById(R.id.scrollGalleryView_share).setVisibility(View.VISIBLE);
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        hideSystemUI();
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        View viewPager = findViewById(R.id.viewPager);

        int[] coords = new int[2];
        viewPager.getLocationOnScreen(coords);

//        CustomUtility.logD(TAG, "showScrollGallery: %d | %d", coords[0], coords[1]);


        LinearLayout thumbnailContainer = findViewById(R.id.thumbnails_container);
        HorizontalScrollView thumbnailScrollView = findViewById(R.id.thumbnails_scroll_view);
        thumbnailScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (thumbnailScrollView.getWidth() > 0/* && false*/) {
                    int[] thumbnailCoords = new int[2];
                    thumbnailContainer.getChildAt(Math.max(0, index)).getLocationOnScreen(thumbnailCoords);
                    int thumbnailCenterX = thumbnailCoords[0] + 200 / 2;
                    boolean isPortrait = Utility.isPortrait(MediaActivity.this);
                    int thumbnailDelta = (Utility.getScreenSize(MediaActivity.this).first + (isPortrait ? 0 : 100)) / 2 - thumbnailCenterX;

//                    CustomUtility.logD(TAG, "onLayoutChange: %d | %d | %d", thumbnailScrollView.getWidth(), thumbnailContainer.getWidth(), thumbnailDelta);
//                    CustomUtility.logD(TAG, "onLayoutChange: %d", thumbnailDelta);
                    if (thumbnailDelta != 0)
                        thumbnailScrollView.scrollBy(pendingScroll = -thumbnailDelta, 0);
                    thumbnailContainer.postDelayed(() -> {
                        pendingScroll = -1;
                    }, 400);

                    thumbnailScrollView.removeOnLayoutChangeListener(this);

                }

//                CustomUtility.logD(TAG, "onLayoutChange: %d | %d | %d | %d", left, right, oldLeft, oldRight);


            }
        });
    }

    private void hideScrollGallery() {
        scrollGalleryShown = false;
        scrollGalleryView.setVisibility(View.GONE);
        int currentItem = scrollGalleryView.getCurrentItem();
        if (scrollGalleryView.getViewPager().getChildCount() > 1)
            mediaRecycler.getRecycler().getLayoutManager().scrollToPosition(currentItem);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        showSystemUI();
        clearScrollGallery();
        currentVideoPreview = null;
        pendingScroll = -1;
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> keySet.forEach(setVideoButtonVisibility::run), 100);
//        keySet.forEach(setVideoButtonVisibility::run);

        View view = activity.findViewById(R.id.customImageDescription);
//        TransitionManager.beginDelayedTransition(activity.findViewById(R.id.scrollGalleryView_root));
        TransitionManager.beginDelayedTransition((ViewGroup) view.getParent());
        view.setVisibility(visibility);
        activity.findViewById(R.id.scrollGalleryView_edit).setVisibility(visibility);
        activity.findViewById(R.id.scrollGalleryView_share).setVisibility(visibility);
        if (!isAutoRotationOn(activity))
            activity.findViewById(R.id.scrollGalleryView_rotate).setVisibility(visibility);
        if (parent != null && videoView.getVisibility() == View.VISIBLE)
            setVideoButtonVisibility.run(videoView);
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

    public static Map<Integer, Runnable> thumbnailLoadMap = new HashMap<>();

    int pendingScroll = -1;
    private void setupThumbnailMap() {
        HorizontalScrollView thumbnailScrollView = findViewById(R.id.thumbnails_scroll_view);
        thumbnailScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (pendingScroll != -1 && scrollX != pendingScroll) {
                thumbnailScrollView.scrollTo(pendingScroll, 0);
//                CustomUtility.logD(TAG, "setupThumbnailMap: changed " + ++changedCount);
            } /*else if (scrollX == pendingScroll)
                CustomUtility.logD(TAG, "setupThumbnailMap: equals");*/
            int thumbnailSize = 220;
            int width = thumbnailScrollView.getWidth();
            int offset = width / 2;
            scrollX += offset;
            int left = Math.max(0, scrollX - width);
            int startIndex = left / thumbnailSize - 2;
            int endIndex = (int) Math.ceil(scrollX / (double) thumbnailSize) + 1;
//            CustomUtility.logD(TAG, String.format("setupThumbnailMap: %d | %d | %d | %d", scrollX - offset, startIndex, endIndex, thumbnailLoadMap.size()));
//            CustomUtility.logD(TAG, "setupThumbnailMap: %d | %d | %d || %d | %d || %d", scrollX - offset, width, offset, startIndex, endIndex, thumbnailLoadMap.size());
            for (int i = startIndex; i <= endIndex; i++) {
                Runnable runnable = thumbnailLoadMap.get(i);
                if (runnable != null) {
                    runnable.run();
                    thumbnailLoadMap.remove(i);
                }
            }
        });

    }

    public static void addToThumbnailMap(ImageView imageView, Runnable request){
        int index = ((LinearLayout) imageView.getParent()).indexOfChild(imageView);
        thumbnailLoadMap.put(index, request);
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
        Utility.colorMenuItemIcon(menu, R.id.taskBar_media_edit, Color.WHITE);

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
//                mediaRecycler.getRecycler().stopScroll();
//                mediaRecycler.getLayoutManager().scrollToPositionWithOffset(CustomUtility.randomInteger(25, /*mediaRecycler.getObjectList().size() - 25*/100), 0);
//                if (true)
//                    return true;
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

        Pair<Integer, Integer> screenSize = Utility.getScreenSize(this);
        findViewById(R.id.thumbnails_container).setPadding(screenSize.first / 2, 0, screenSize.first / 2, 0);
        setDisplayProps();
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