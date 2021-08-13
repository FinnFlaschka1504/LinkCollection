package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.transition.TransitionManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.BuildConfig;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaCategory;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomMenu;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomPopupWindow;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomGlideImageLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomVideoLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.veinhorn.scrollgalleryview.HackyViewPager;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.loader.MediaLoader;

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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class MediaActivity extends AppCompatActivity {
    public static final String ADVANCED_SEARCH__PERSON = "ADVANCED_SEARCH__PERSON";
    private final String ADVANCED_SEARCH_CRITERIA__PERSON = "p";
    private final String ADVANCED_SEARCH_CRITERIA__CATEGORY = "c";


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
    private Menu toolBarMenu;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.PERSON, FILTER_TYPE.CATEGORY));
    private VideoView currentVideoPreview;
    public boolean isVideoPreviewSoundOn = false;
    public Map<VideoView, MediaPlayer> currentMediaPlayerMap = new HashMap<>();
    private Helpers.AdvancedQueryHelper<MultiSelectHelper.Selectable<Media>> advancedQueryHelper;

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

            setContentView(R.layout.activity_media);

            setupScrollGalleryView();

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(plural);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, plural);

            media_search = findViewById(R.id.search);

            advancedQueryHelper = new Helpers.AdvancedQueryHelper<MultiSelectHelper.Selectable<Media>>(this, media_search)
                    .setRestFilter((searchQuery, selectableMediaList) -> {
                        if (searchQuery.contains("|")) {
                            selectableMediaList.filterOr(searchQuery.split("\\|"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
                        } else {
                            selectableMediaList.filterAnd(searchQuery.split("&"), (mediaSelectable, s) -> Utility.containedInMedia(s.trim(), mediaSelectable.getContent(), filterTypeSet), true);
                        }
                    })
                    .addCriteria_defaultName()
                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__PERSON, CategoriesActivity.CATEGORIES.MEDIA_PERSON, mediaSelectable -> mediaSelectable.getContent().getPersonIdList())
                    .addCriteria_ParentClass(ADVANCED_SEARCH_CRITERIA__CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, mediaSelectable -> mediaSelectable.getContent().getCategoryIdList());

            loadRecycler();

            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    searchQuery = s.trim();
//                    if (!s.trim().equals("")) {
//                        if (s.trim().equals(WATCH_LATER_SEARCH)) {
//                            List<String> unableToFindList = new ArrayList<>();
//                            for (String showUuid : database.watchLaterList) {
//                                Show show = database.showMap.get(showUuid);
//                                if (show == null)
//                                    unableToFindList.add(showUuid);
//                                else
//                                    filterdShowList.add(show);
//                            }
//                            if (!unableToFindList.isEmpty()) {
//                                CustomDialog.Builder(that)
//                                        .setTitle("Problem beim Laden der Liste!")
//                                        .setName((unableToFindList.size() == 1 ? "Ein " + singular + " konnte" : unableToFindList.size() + " " + plural + " konnten") + " nicht gefunden werden")
//                                        .setObjectExtra(unableToFindList)
//                                        .addButton("Ignorieren", null)
//                                        .addButton("Entfernen", customDialog -> {
//                                            database.watchLaterList.removeAll(((ArrayList<String>) customDialog.getObjectExtra()));
//                                            Toast.makeText(that, "Entfernt", Toast.LENGTH_SHORT).show();
//                                            Database.saveAll();
//                                            setResult(RESULT_OK);
//                                        })
//                                        .show();
//                            }
//                            reLoadRecycler();
//                            return true;
//                        }
////                        if (s.trim().equals(UPCOMING_SEARCH)) {
////                            filterdShowList = allShowList.stream().filter(Show::isUpcoming).collect(Collectors.toList());
////                            reLoadRecycler();
////                            return true;
////                        }
//
//                        for (String subQuery : s.split("\\|")) {
//                            subQuery = subQuery.trim();
//                            List<Show> subList = new ArrayList<>(filterdShowList);
//                            for (Show show : subList) {
//                                if (!Utility.containedInShow(subQuery, show, filterTypeSet))
//                                    filterdShowList.remove(show);
//                            }
//                        }
//                    }
                    reLoadRecycler();
                    return true;
                }
            };
            media_search.setOnQueryTextListener(textListener);


            if (Objects.equals(getIntent().getAction(), MainActivity.ACTION_SHORTCUT))
                showEditMultipleDialog(null);


            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
            if (extraSearchCategory != null) {

                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
                    if (!advancedQueryHelper.wrapExtraSearch(extraSearchCategory, extraSearch))
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
//        Map<Show, List<Show.Episode>> showEpisodeMap = mediaList.stream().collect(Collectors.toMap(show -> show, this::getEpisodeList));
//
//        new Helpers.SortHelper<>(mediaList)
//                .setAllReversed(reverse)
//                .addSorter(ShowActivity.SORT_TYPE.NAME, (show1, show2) -> show1.getName().compareTo(show2.getName()) * (reverse ? -1 : 1))
//
//                .addSorter(ShowActivity.SORT_TYPE.VIEWS)
//                .changeType(show -> getViews(showEpisodeMap.get(show)))
//                .enableReverseDefaultComparable()
//
//                .addSorter(ShowActivity.SORT_TYPE.RATING/*, (show1, show2) -> {
//                    List<Show.Episode> episodeList1 = showEpisodeMap.get(show1);
//                    List<Show.Episode> episodeList2 = showEpisodeMap.get(show2);
//
//                    double rating1 = getRating(episodeList1);
//                    double rating2 = getRating(episodeList2);
//
//                    if (rating1 == rating2)
//                        return show1.getName().compareTo(show2.getName());
//                    else
//                        return Double.compare(rating1, rating2) * (reverse ? 1 : -1);
//                }*/)
//                .changeType(show -> getRating(showEpisodeMap.get(show)))
//                .enableReverseDefaultComparable()
//
//                .addSorter(ShowActivity.SORT_TYPE.LATEST/*, (show1, show2) -> {
//                    List<Show.Episode> episodeList1 = showEpisodeMap.get(show1);
//                    List<Show.Episode> episodeList2 = showEpisodeMap.get(show2);
//
//                    Date latest1 = getLatest(episodeList1);
//                    Date latest2 = getLatest(episodeList2);
//
//                    if (latest1 == null && latest2 == null)
//                        return show1.getName().compareTo(show2.getName());
//                    else if (latest1 == null)
//                        return reverse ? -1 : 1;
//                    else if (latest2 == null)
//                        return reverse ? 1 : -1;
//
//                    if (latest1.equals(latest2))
//                        return show1.getName().compareTo(show2.getName());
//                    else
//                        return latest1.compareTo(latest2) * (reverse ? 1 : -1);
//                }*/)
//                .changeType(show -> getLatest(showEpisodeMap.get(show)))
//                .enableReverseDefaultComparable()
//
//                .finish()
//                .sort(() -> sort_type);

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
            dragSelectTouchListener.setIsActive(true, index);

            ((CollapsingToolbarLayout) context.findViewById(R.id.collapsingToolbarLayout)).setTitleEnabled(false);
//            Toolbar toolbar = context.findViewById(R.id.media_selectionToolbar);
//            context.setSupportActionBar(toolbar);
//            toolbar.setVisibility(View.VISIBLE);
//
//            Toolbar defaultToolbar = (Toolbar) context.findViewById(R.id.toolbar);
//            defaultToolbar.inflateMenu(R.menu.task_bar_joke);
//            defaultToolbar.setVisibility(View.GONE);
            toolBarMenu.findItem(R.id.taskBar_media_add).setVisible(false);
            toolBarMenu.findItem(R.id.taskBar_media_edit).setVisible(true);

        }

        public CustomList<Selectable<T>> stopSelection() {
            activeSelection = false;
            CustomList<Selectable<T>> allSelected = getAllSelected();
            contentList.forEach(tSelectable -> tSelectable.selected = false);
            customRecycler.getAdapter().notifyDataSetChanged();

            ((CollapsingToolbarLayout) context.findViewById(R.id.collapsingToolbarLayout)).setTitleEnabled(true);

            toolBarMenu.findItem(R.id.taskBar_media_add).setVisible(true);
            toolBarMenu.findItem(R.id.taskBar_media_edit).setVisible(false);

            return allSelected;
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
            if (getAllSelected().isEmpty()) {
                stopSelection();
                return false;
            }
            updateToolbarTitle();
            return true;
        }

        private void updateToolbarTitle() {
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

    private void loadRecycler() {
        int width = Utility.getScreenSize(this).first;
        int columnCount = 3;
        int sizePx = width / columnCount;
        int sizeDp = CustomUtility.pxToDp(sizePx);

        DragSelectTouchListener dragSelectTouchListener = DragSelectTouchListener.Companion.create(this, new MyDragSelectReceiver(), null);
        selectHelper = new MultiSelectHelper<>(this);
        selectHelper.dragSelectTouchListener = dragSelectTouchListener;

        RecyclerView recyclerView = findViewById(R.id.recycler);
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
                    itemView.setLayoutParams(new FrameLayout.LayoutParams(sizePx, sizePx));
                    SelectMediaHelper.loadPathIntoImageView(mediaSelectable.content.getImagePath(), itemView, sizeDp);

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
                .setOnLongClickListener((customRecycler, view, mediaSelectable, index) -> {
                    selectHelper.startSelection(index);
                    customRecycler.getAdapter().notifyDataSetChanged();
                })
                .setRowOrColumnCount(columnCount)
                .generate();


        recyclerView.addOnItemTouchListener(dragSelectTouchListener);
    }

    private void reLoadRecycler() {
        mediaRecycler.reload();
    }

    private int indexOfMedia(Media media) {
        if (media ==null)
            return -1;
        List<MultiSelectHelper.Selectable<Media>> list = mediaRecycler.getObjectList();
        Optional<MultiSelectHelper.Selectable<Media>> optional = list.stream().filter(mediaSelectable -> mediaSelectable.getContent() == media).findFirst();
        if (optional.isPresent()) {
            MultiSelectHelper.Selectable<Media> mediaSelectable = optional.get();
            return list.indexOf(mediaSelectable);
        }
        return  -1;
    }
    //  <------------------------- Recycler -------------------------


    //  ------------------------- Edit ------------------------->
    private CustomDialog showEditMultipleDialog(CustomList<Media> oldMedia) {
        if (!Utility.isOnline(this))
            return null;

        setResult(RESULT_OK);
        removeFocusFromSearch();

        boolean isAdd = oldMedia == null || oldMedia.isEmpty();
        CustomList<Media> newMedia = isAdd ? new CustomList<>() : oldMedia.stream().map(Media::clone).collect(Collectors.toCollection(CustomList::new));

        CustomList<String> mediaPersonIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
        CustomList<String> mediaCategoryIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);
        CustomList<Media> subSelectionList = new CustomList<>();

        String dialogTitle = "Mehrere " + plural + (isAdd ? " Hinzufügen" : " Bearbeiten");
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
                            ((TextView) view.findViewById(R.id.dialog_editMedia_categories)).setText(String.join(", ", mediaCategoryIdList.map(id -> MediaCategory.findObjectById(id).getName())));
                        }, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);
                    });

                    view.findViewById(R.id.dialog_editMedia_editCategories).setOnLongClickListener(v -> {
                        MediaCategory freizeit = new MediaCategory("Freizeit");
                        freizeit.addChildren(Arrays.asList(new MediaCategory("Sport"), new MediaCategory("Spiele"), new MediaCategory("Werken")));
                        MediaCategory feiern = new MediaCategory("Feiern");
                        feiern.addChildren(Arrays.asList(new MediaCategory("Geburtstag"), new MediaCategory("Firma"), new MediaCategory("Silvester"), new MediaCategory("Vatertag")));
                        MediaCategory reisen = new MediaCategory("Reisen");
                        database.mediaCategoryMap.put(freizeit.getUuid(), freizeit);
                        database.mediaCategoryMap.put(feiern.getUuid(), feiern);
                        database.mediaCategoryMap.put(reisen.getUuid(), reisen);
                        Database.saveAll();
                        return true;
                    });

                    ((TextView) view.findViewById(R.id.dialog_editMedia_persons)).setText(String.join(", ", mediaPersonIdList.map(id -> database.mediaPersonMap.get(id).getName())));
                    ((TextView) view.findViewById(R.id.dialog_editMedia_categories)).setText(String.join(", ", mediaCategoryIdList.map(id -> MediaCategory.findObjectById(id).getName())));
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
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> saveMultipleMedia(customDialog, oldMedia, newMedia, mediaPersonIdList, mediaCategoryIdList))
                .show();
    }

    private void saveMultipleMedia(CustomDialog editDialog, CustomList<Media> oldMedia, List<Media> newMedia, CustomList<String> mediaPersonIdList, CustomList<String> mediaCategoryIdList) {
        boolean isAdd = oldMedia == null || oldMedia.isEmpty();
        int size = newMedia.size();

        if (isAdd) {
            newMedia.forEach(media -> media.getPersonIdList().addAll(mediaPersonIdList));
            newMedia.forEach(media -> media.getCategoryIdList().addAll(mediaCategoryIdList));

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

    static class SelectMediaHelper {
        private AppCompatActivity context;
        private ViewGroup parentView;
        private CustomList<Media> selectedMedia;
        private CustomList<Media> subSelectedMedia;
        private CustomUtility.GenericInterface<CustomList<Media>> onSubSelectionChanged;
        private CustomRecycler<Media> selectedRecycler;
        private boolean hideAddButton;

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
            if (path.endsWith(".mp4"))
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
                view.findViewById(R.id.fragment_selectMediaHelper_selection_add).setOnClickListener(v -> showSelectDialog(false));
                view.findViewById(R.id.fragment_selectMediaHelper_selection_add).setOnLongClickListener(v -> {
                    showSelectDialog(true);
                    return true;
                });
            }
        }
        //  <------------------------- Builder -------------------------
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

        findViewById(R.id.scrollGalleryView_edit).setOnClickListener(v -> {
            int index = indexOfMedia(getCurrentGalleryMedia());
            CustomDialog customDialog = showEditMultipleDialog(new CustomList<>(mediaRecycler.getObjectList().get(index).getContent()));
            if (customDialog != null) {
                customDialog
                        .addOnDialogDismiss(customDialog1 -> {
                            setCustomDescription(index);
                        });
            }
        });
    }

    private void setCustomDescription(int index) {
        Media media = mediaRecycler.getObjectList().get(index).getContent();
        LinearLayout linearLayout = findViewById(R.id.customImageDescription);

        linearLayout.removeAllViews();

        if (!media.getPersonIdList().isEmpty()) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_PERSON, textView, media.getPersonIdList());
            textView.setText(new SpannableStringBuilder().append("P: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(textView.getText()));
            linearLayout.addView(textView);
        }

        if (!media.getCategoryIdList().isEmpty()) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            Utility.applyCategoriesLink(this, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY, textView, media.getCategoryIdList());
            textView.setText(new SpannableStringBuilder().append("K: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(textView.getText()));
            linearLayout.addView(textView);
        }

        long lastModified = new File(media.getImagePath()).lastModified();
        if (lastModified > 0) {
            TextView textView = new TextView(new ContextThemeWrapper(this, R.style.TextWithShadow));
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setText(new SpannableStringBuilder().append("D: ", new StyleSpan(Typeface.ITALIC), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE).append(Utility.formatDate("dd.MM.yyyy", new Date(lastModified))));
            linearLayout.addView(textView);
        }
    }

    private void setMediaScrollGalleryAndShow(List<Media> shownMediaList, int index) {
        setMediaScrollGallery(shownMediaList);
        showScrollGallery(index);
    }

    private void setMediaScrollGallery(List<Media> shownMediaList) {
        scrollGalleryView.addMedia(
                shownMediaList.stream().map(media -> {
                    if (media.getImagePath().endsWith(".mp4"))
                        return MediaInfo.mediaLoader(new CustomVideoLoader(media));
                    else
                        return MediaInfo.mediaLoader(new CustomGlideImageLoader(media));
                }).collect(Collectors.toList()));
    }

    private void clearScrollGallery() {
        scrollGalleryView.clearGallery();
    }

    private void showScrollGallery(int index) {
        removeFocusFromSearch();

        if (findViewById(com.veinhorn.scrollgalleryview.R.id.thumbnails_scroll_view).getVisibility() == View.GONE) {
            findViewById(R.id.thumbnails_scroll_view).setVisibility(View.VISIBLE);
            findViewById(R.id.customImageDescription).setVisibility(View.VISIBLE);
            findViewById(R.id.scrollGalleryView_edit).setVisibility(View.VISIBLE);
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
        thumbnailContainer.setBackgroundColor(Color.argb(100, 0, 0, 0));
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
        Set<VideoView> keySet = ((MediaActivity) activity).currentMediaPlayerMap.keySet();
        VideoView videoView = null;
        if (parent != null) {
            videoView = parent.findViewById(R.id.imageFragment_video);
            keySet.remove(videoView);
        }
        keySet.forEach(setVideoButtonVisibility::runGenericInterface);

        View view = activity.findViewById(R.id.customImageDescription);
        TransitionManager.beginDelayedTransition((ViewGroup) view.getParent());
        view.setVisibility(visibility);
        activity.findViewById(R.id.scrollGalleryView_edit).setVisibility(visibility);
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
    //  <------------------------- ScrollGallery -------------------------


    //  ------------------------- ToolBar ------------------------->
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_media, menu);
        toolBarMenu = menu;
        selectHelper.toolBarMenu = toolBarMenu;
        menu.findItem(R.id.taskBar_media_edit).setIconTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}}, new int[]{Color.WHITE}));

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
            case R.id.taskBar_media_add:
                showEditMultipleDialog(null);
//                SelectMediaHelper.Builder(this).showSelection();
                break;

            case R.id.taskBar_media_edit:
                showEditMultipleDialog(selectHelper.getAllSelectedContent());
                break;

            case android.R.id.home:
                if (selectHelper.isActiveSelection()) {
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
            if (currentVideoPreview != null)
                currentVideoPreview.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentVideoPreview != null)
            currentVideoPreview.start();
    }

    @Override
    public void onBackPressed() {
        if (scrollGalleryView.getVisibility() == View.VISIBLE) {
            hideScrollGallery();
        } else if (advancedQueryHelper.handleBackPress(this)) {
            return;
        } else if (selectHelper.isActiveSelection()) {
            selectHelper.stopSelection();
        } else
            super.onBackPressed();
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