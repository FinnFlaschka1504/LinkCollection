package com.maxMustermannGeheim.linkcollection.Activities.Content;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

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
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomGlideImageLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomScrollGallary.CustomVideoLoader;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.veinhorn.scrollgalleryview.HackyViewPager;
import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class MediaActivity extends AppCompatActivity {

    public enum FILTER_TYPE {
        PERSON("Person");

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
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.PERSON));

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
            allMediaList = new CustomList<>(database.mediaMap.values());
            sortList(allMediaList);


            scrollGalleryView = findViewById(R.id.scroll_gallery_view);
//            new ScrollGalleryView(this, )
//            Utility.replaceView(findViewById(R.id.scroll_gallery_view), );
            ((HackyViewPager) findViewById(com.veinhorn.scrollgalleryview.R.id.viewPager)).setOffscreenPageLimit(3);

            scrollGalleryView
                    .setThumbnailSize(200)
                    .setZoom(true)
                    .withHiddenThumbnails(false)
                    .hideThumbnailsOnClick(true)
                    .addOnImageClickListener((position) -> {
                        Log.i(getClass().getName(), "You have clicked on image #" + position);
                    })
                    .setFragmentManager(getSupportFragmentManager());

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(plural);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            TextView noItem = findViewById(R.id.no_item);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
            setToolbarTitle = Utility.applyExpendableToolbar_recycler(this, findViewById(R.id.recycler), toolbar, appBarLayout, collapsingToolbarLayout, noItem, plural);

            media_search = findViewById(R.id.search);

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
                filterTypeSet.clear();

                switch (extraSearchCategory) {
                    case MEDIA_PERSON:
                        filterTypeSet.add(FILTER_TYPE.PERSON);
                        break;
//                    case EPISODE:
//                        String episode_string = getIntent().getStringExtra(EXTRA_EPISODE);
//                        if (episode_string != null) {
//                            Show.Episode episode = new Gson().fromJson(episode_string, Show.Episode.class);
//                            findEpisode(episode, () -> {
//                                Show.Episode oldEpisode = database.showMap.get(episode.getShowId()).getSeasonList().get(episode.getSeasonNumber())
//                                        .getEpisodeMap().get(episode.getUuid());
//
//                                if (oldEpisode != null)
//                                    showEpisodeDetailDialog(null, oldEpisode, true);
//                                else
//                                    apiSeasonRequest(database.showMap.get(episode.getShowId()), episode.getSeasonNumber(), () ->
//                                            showEpisodeDetailDialog(null, database.tempShowSeasonEpisodeMap.get(database.showMap.get(episode.getShowId())).get(episode.getSeasonNumber())
//                                                    .get(episode.getUuid()), true));
//                            });
//                        }
                }
//
                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
                if (extraSearch != null) {
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
    private CustomList<Media> filterList(CustomList<Media> mediaList) {
        if (!searchQuery.isEmpty()) {
            if (searchQuery.contains("|")) {
                mediaList = mediaList.filterOr(searchQuery.split("\\|"), (media, s) -> Utility.containedInMedia(s.trim(), media, filterTypeSet), false);
            } else {
                mediaList = mediaList.filterAnd(searchQuery.split("&"), (media, s) -> Utility.containedInMedia(s.trim(), media, filterTypeSet), false);
            }
        }
        return mediaList;
    }

    private CustomList<Media> sortList(CustomList<Media> mediaList) {
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

        mediaList.sort((media1, media2) -> {
            File file1 = new File(media1.getImagePath());
            File file2 = new File(media2.getImagePath());
            return Long.compare(file1.lastModified(), file2.lastModified()) * -1;
        });
        return mediaList;
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

//            customRecycler.reload();

            ((CollapsingToolbarLayout) context.findViewById(R.id.collapsingToolbarLayout)).setTitleEnabled(true);
//            Toolbar toolbar = context.findViewById(R.id.toolbar);
//            toolbar.setTitle(context.plural);
//            context.setSupportActionBar(toolbar);
//            toolbar.setVisibility(View.VISIBLE);
//            context.findViewById(R.id.media_selectionToolbar).setVisibility(View.GONE);

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


        public MultiSelectHelper<T> toggleSelection(int index) {
            contentList.get(index).toggleSelection();
            if (getAllSelected().isEmpty()) {
                stopSelection();
                return this;
            }
            customRecycler.getAdapter().notifyItemChanged(index);
            updateToolbarTitle();
            return this;
        }

        public void setSelected(int index, boolean selected) {
            Selectable<T> selectable = contentList.get(index);
            if (selectable.selected != selected) {
                selectable.selected = selected;
                customRecycler.getAdapter().notifyItemChanged(index);
//                customRecycler.update(index);
                updateToolbarTitle();
            }
        }

        private void updateToolbarTitle() {
            ((Toolbar) context.findViewById(R.id.toolbar)).setTitle(String.format(Locale.getDefault(), "Auswählen (%d)", getAllSelectedCount()));
        }
        //  <------------------------- Convenience -------------------------

        static class Selectable<P> {
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

        DragSelectTouchListener dragSelectTouchListener = DragSelectTouchListener.Companion.create(this, new MyDragSelectReceiver(), null);
        selectHelper = new MultiSelectHelper<>(this);
        selectHelper.dragSelectTouchListener = dragSelectTouchListener;

        RecyclerView recyclerView = findViewById(R.id.recycler);
        mediaRecycler = new CustomRecycler<MultiSelectHelper.Selectable<Media>>(this, recyclerView)
                .addOptionalModifications(customRecycler -> selectHelper.customRecycler = customRecycler)
                .setItemLayout(R.layout.list_item_image)
                .setGetActiveObjectList(customRecycler -> {
                    CustomList<Media> filteredList = sortList(filterList(new CustomList<>(database.mediaMap.values())));
                    selectHelper.updateFromList(filteredList, true);

                    TextView noItem = findViewById(R.id.no_item);
                    String text = media_search.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
//                    String viewsCountText = (views > 1 ? views + " Episoden" : (views == 1 ? "Eine" : "Keine") + " Episode") + " angesehen";
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText).append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    elementCount.setText(builder);

                    return selectHelper.getContentList();
                })
                .setSetItemContent((customRecycler, itemView, mediaSelectable) -> {
                    SelectMediaHelper.loadPathIntoImageView(mediaSelectable.content.getImagePath(), itemView, CustomUtility.pxToDp(width / columnCount));

                    if (mediaSelectable.isSelected())
                        itemView.findViewById(R.id.listItem_image_selected).setVisibility(View.VISIBLE);
                    else
                        itemView.findViewById(R.id.listItem_image_selected).setVisibility(View.GONE);
                })
                .setOnClickListener((customRecycler, view, mediaSelectable, index) -> {
                    if (selectHelper.isActiveSelection()) {
                        selectHelper.toggleSelection(index);
                    } else {
                        setMediaScrollGalleryAndShow(customRecycler.getObjectList().stream().map(MultiSelectHelper.Selectable::getContent).collect(Collectors.toList()), index);

//                        CustomDialog.Builder(this)
//                                .setView(R.layout.dialog_scroll_gallery)
//                                .setDimensionsFullscreen()
//                                .setSetViewContent((customDialog, view1, reload) -> {
////                                    ScrollGalleryView scrollGalleryView = view1.findViewById(R.id.scroll_gallery_view);
////
////                                    scrollGalleryView
////                                            .setThumbnailSize(200)
////                                            .setZoom(true)
////                                            .withHiddenThumbnails(false)
////                                            .hideThumbnailsOnClick(true)
//////                .hideThumbnailsAfter(5000)
////                                            .addOnImageClickListener((position) -> {
////                                                Log.i(getClass().getName(), "You have clicked on image #" + position);
////                                            })
////                                            .setFragmentManager(getSupportFragmentManager());
//
////                                    ((HackyViewPager) view1.findViewById(com.veinhorn.scrollgalleryview.R.id.viewPager)).setOffscreenPageLimit(3);
//
////                                    for (String imageUrl : Arrays.asList("/storage/emulated/0/DCIM/Camera/20210701_154709.jpg", "/storage/emulated/0/DCIM/Camera/20210630_163607.jpg", "/storage/emulated/0/DCIM/Camera/20210630_152825.jpg", "/storage/emulated/0/DCIM/Camera/20210630_164032.jpg")) {
////                                        scrollGalleryView.addMedia(MediaInfo.mediaLoader(new CustomPicassoImageLoader(imageUrl), ""));
////                                    }
////                                    scrollGalleryView.addMedia(MediaInfo.mediaLoader(new CustomVideoLoader("/storage/emulated/0/DCIM/Camera/20210718_222030.mp4", R.drawable.simpsons_movie_poster), ""));
//
//                                })
//                                .show();
                    }
                })
                .setOnLongClickListener((customRecycler, view, mediaSelectable, index) -> {
                    selectHelper.startSelection(index);
                })
                .setRowOrColumnCount(columnCount)
                .generate();


        recyclerView.addOnItemTouchListener(dragSelectTouchListener);
    }

    private void reLoadRecycler() {
        mediaRecycler.reload();
    }
    //  <------------------------- Recycler -------------------------


    //  ------------------------- Edit ------------------------->
    private void showEditMultipleDialog(CustomList<Media> oldMedia) {
        if (!Utility.isOnline(this))
            return;

        setResult(RESULT_OK);
        removeFocusFromSearch();

        boolean isAdd = oldMedia == null || oldMedia.isEmpty();
        CustomList<Media> newMedia = isAdd ? new CustomList<>() : oldMedia.stream().map(Media::clone).collect(Collectors.toCollection(CustomList::new));

        CustomList<String> mediaPersonIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
        CustomList<String> mediaCategoryIdList = CategoriesActivity.getCategoriesIntersection(newMedia, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);

        CustomDialog.Builder(this)
                .setTitle("Mehrere " + plural + (isAdd ? " Hinzufügen" : " Bearbeiten"))
                .setView(R.layout.dialog_edit_media)
                .setSetViewContent((customDialog, view, reload) -> {
                    FrameLayout selectedMediaParent = view.findViewById(R.id.dialog_editMedia_selectParent);
                    SelectMediaHelper.Builder(this, newMedia)
                            .setParentView(selectedMediaParent)
                            .setHideAddButton(!isAdd)
                            .build();

                    view.findViewById(R.id.dialog_editMedia_editPersons).setOnClickListener(v -> {
                        Utility.showEditItemDialog(this, customDialog, mediaPersonIdList, mediaPersonIdList, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
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
                        String singOrPlur = oldMedia.size() > 1 ? plural : singular;
                        customDialog
                                .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                                    oldMedia.forEach(media -> database.mediaMap.remove(media.getUuid()));
                                    reLoadRecycler();
                                    Toast.makeText(this, (Database.saveAll_simple() ? plural : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();
                                })
                                .alignPreviousButtonsLeft()
                                .transformPreviousButtonToImageButton()
                                .addConfirmationDialogToLastAddedButton(singOrPlur + " Löschen", "Möchstest du wirklich " + oldMedia.size() + " " + singOrPlur + " löschen?");
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
                    return true;
                } else
                    return false;
            });

            database.mediaMap.putAll(newMedia.stream().collect(Collectors.toMap(Media::getUuid, o -> o)));
        } else {
            applyNewIdList(newMedia, mediaPersonIdList, CategoriesActivity.CATEGORIES.MEDIA_PERSON);
            applyNewIdList(newMedia, mediaCategoryIdList, CategoriesActivity.CATEGORIES.MEDIA_CATEGORY);

            oldMedia.forEachCount((media, count) -> {
                media.getChangesFrom(newMedia.get(count));
            });
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
        private Utility.GenericInterface<CustomList<Media>> onSelectionChange;
        private ViewGroup parentView;
        private boolean multiSelect = true;
        private CustomList<Media> selectedMedia;
        private CustomRecycler<Media> selectedRecycler;
        private boolean hideAddButton;

        //  ------------------------- Constructor ------------------------->
        public SelectMediaHelper(AppCompatActivity context, CustomList<Media> selectedMedia) {
            this.context = context;
            this.selectedMedia = selectedMedia;
        }
        //  <------------------------- Constructor -------------------------


        //  ------------------------- Getter & Setter ------------------------->
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
                    pathList.removeIf(s -> selectedMedia.stream().anyMatch(media -> media.getName().equals(s)));
                    selectedMedia.addAll(pathList.map(Media::new));
                }

                if (selectedRecycler != null)
                    selectedRecycler.reload();

                if (true)
                    return;


                CustomDialog.Builder(context)
                        .setDimensionsFullscreen()
                        .disableScroll()
                        .setView(new CustomRecycler<String>(context)
                                        .setObjectList(pathList)
                                        .setItemLayout(R.layout.list_item_image)
                                        .setSetItemContent((customRecycler, itemView, path) -> {
                                            File imgFile = new File(path);
                                            Uri imageUri = Uri.fromFile(imgFile);
                                            RequestOptions myOptions = new RequestOptions()
                                                    .override(700, 700)
                                                    .centerCrop();

                                            ImageView imageView = itemView.findViewById(R.id.listItem_image_imgaeView);
                                            Glide.with(context)
                                                    .load(imageUri)
                                                    .apply(myOptions)
                                                    .into(imageView);

                                            ImageView videoIndicator = itemView.findViewById(R.id.listItem_image_videoIndicator);

                                            if (path.endsWith(".mp4"))
                                                videoIndicator.setVisibility(View.VISIBLE);
                                            else
                                                videoIndicator.setVisibility(View.GONE);

//                                            imageView.setOnClickListener(v -> {
//                                                context.startActivity(new Intent(context, MyFragmentGallery.class));
////                                        CustomDialog.Builder(this)
////                                                .setView(R.layout.dialog_scroll_gallery)
////                                                .setDimensionsFullscreen()
//////                                                .removeBackground_and_margin()
////                                                .disableScroll()
////                                                .setSetViewContent((customDialog, view, reload) -> {
////                                                    ScrollGalleryView scrollGallery = customDialog.findViewById(R.id.dialog_scrollGallery_view);
////                                                    ScrollGalleryView
////                                                            .from(scrollGallery)
////                                                            .settings(
////                                                                    GallerySettings
////                                                                            .from(getSupportFragmentManager())
////                                                                            .thumbnailSize(100)
////                                                                            .enableZoom(true)
////                                                                            .build()
////                                                            )
//////                                                            .add(image("https://www.anti-bias.eu/wp-content/uploads/2015/01/shutterstock_92612287-e1420280083718.jpg"))
////                                                            .add(MediaInfo.mediaLoader(new CustomGlideImageLoader("https://www.anti-bias.eu/wp-content/uploads/2015/01/shutterstock_92612287-e1420280083718.jpg") {
////
////                                                            }))
//////                                                            .add(image("http://povodu.ru/wp-content/uploads/2016/04/pochemu-korabl-derzitsa-na-vode.jpg"))
//////                                                            .add(video("http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4", R.drawable.placeholder_image))
////                                                            .build();
////                                                })
////                                                .show();
//                                            });

                                        })
                                        .setRowOrColumnCount(2)
                                        .generateRecyclerView()
                        )
                        .show();
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
                    .setGetActiveObjectList(customRecycler -> selectedMedia)
                    .setItemLayout(R.layout.list_item_image)
                    .setSetItemContent((customRecycler, itemView, media) -> {
                        loadPathIntoImageView(media.getImagePath(), itemView, 120);
//                                            imageView.setOnClickListener(v -> {
//                                                context.startActivity(new Intent(context, MyFragmentGallery.class));
////                                        CustomDialog.Builder(this)
////                                                .setView(R.layout.dialog_scroll_gallery)
////                                                .setDimensionsFullscreen()
//////                                                .removeBackground_and_margin()
////                                                .disableScroll()
////                                                .setSetViewContent((customDialog, view, reload) -> {
////                                                    ScrollGalleryView scrollGallery = customDialog.findViewById(R.id.dialog_scrollGallery_view);
////                                                    ScrollGalleryView
////                                                            .from(scrollGallery)
////                                                            .settings(
////                                                                    GallerySettings
////                                                                            .from(getSupportFragmentManager())
////                                                                            .thumbnailSize(100)
////                                                                            .enableZoom(true)
////                                                                            .build()
////                                                            )
//////                                                            .add(image("https://www.anti-bias.eu/wp-content/uploads/2015/01/shutterstock_92612287-e1420280083718.jpg"))
////                                                            .add(MediaInfo.mediaLoader(new CustomGlideImageLoader("https://www.anti-bias.eu/wp-content/uploads/2015/01/shutterstock_92612287-e1420280083718.jpg") {
////
////                                                            }))
//////                                                            .add(image("http://povodu.ru/wp-content/uploads/2016/04/pochemu-korabl-derzitsa-na-vode.jpg"))
//////                                                            .add(video("http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4", R.drawable.placeholder_image))
////                                                            .build();
////                                                })
////                                                .show();
//                                            });
                    })
                    .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
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
        scrollGalleryView.setVisibility(View.VISIBLE);
        scrollGalleryView.setCurrentItem(index);
        LinearLayout thumbnailContainer = findViewById(R.id.thumbnails_container);
        thumbnailContainer.setBackgroundColor(Color.argb(100, 0,0, 0));

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().setDecorFitsSystemWindows(false);

        HorizontalScrollView thumbnailScrollView = findViewById(R.id.thumbnails_scroll_view);
        //1440
        thumbnailContainer.postDelayed(() -> {
            int[] thumbnailCoords = new int[2];
            thumbnailContainer.getChildAt(index).getLocationOnScreen(thumbnailCoords);
            int thumbnailCenterX = thumbnailCoords[0] + 200 / 2;
            int thumbnailDelta = 1440 / 2 - thumbnailCenterX;

            thumbnailScrollView.scrollTo(-thumbnailDelta, 0);
        }, 300);
    }

    private void hideScrollGallery() {
        scrollGalleryView.setVisibility(View.GONE);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        getWindow().setDecorFitsSystemWindows(true);
        clearScrollGallery();
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

//            if (resultCode == RESULT_OK) {
//                if (checkAndRequestStoragePermission())
//                    loadDatabase();
//            } else
//                checkAndRequestStoragePermission();
        }
    }


    @Override
    public void onBackPressed() {
        if (selectHelper.isActiveSelection()) {
            selectHelper.stopSelection();
        } else if (scrollGalleryView.getVisibility() == View.VISIBLE) {
            hideScrollGallery();
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