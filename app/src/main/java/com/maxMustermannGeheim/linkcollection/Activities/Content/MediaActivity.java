package com.maxMustermannGeheim.linkcollection.Activities.Content;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectReceiver;
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.ParentClass;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.BuildConfig;
import com.maxMustermannGeheim.linkcollection.Daten.Media.Media;
import com.maxMustermannGeheim.linkcollection.Daten.Media.MediaPerson;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class MediaActivity extends AppCompatActivity {

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

    private TextView elementCount;
    private SearchView media_search;


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


//            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
//            if (extraSearchCategory != null) {
//                filterTypeSet.clear();
//
//                switch (extraSearchCategory) {
//                    case SHOW_GENRES:
//                        filterTypeSet.add(ShowActivity.FILTER_TYPE.GENRE);
//                        break;
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
//                }
//
//                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
//                if (extraSearch != null) {
//                    media_search.setQuery(extraSearch, true);
//                }
//            }
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
    // ToDo: https://github.com/afollestad/drag-select-recyclerview
    private CustomList<Media> filterList(CustomList<Media> mediaList) {
//        if (!searchQuery.isEmpty()) {
//            if (searchQuery.contains("|")) {
//                mediaList = mediaList.filterOr(searchQuery.split("\\|"), (show, s) -> Utility.containedInShow(s.trim(), show, filterTypeSet), false);
//            } else {
//                mediaList = mediaList.filterAnd(searchQuery.split("&"), (show, s) -> Utility.containedInShow(s.trim(), show, filterTypeSet), false);
//            }
//        }
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

        return mediaList;
    }

    class MyDragSelectReceiver implements DragSelectReceiver  {

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
            Toolbar toolbar = context.findViewById(R.id.media_selectionToolbar);
            context.setSupportActionBar(toolbar);
            toolbar.setVisibility(View.VISIBLE);

            Toolbar defaultToolbar = (Toolbar) context.findViewById(R.id.toolbar);
            defaultToolbar.inflateMenu(R.menu.task_bar_joke);
            defaultToolbar.setVisibility(View.GONE);
            defaultToolbar.getMenu().findItem(R.id.taskBar_media_add).setVisible(false);
            defaultToolbar.getMenu().findItem(R.id.taskBar_media_edit).setVisible(true);
        }

        public CustomList<Selectable<T>> stopSelection() {
            activeSelection = false;
            CustomList<Selectable<T>> allSelected = getAllSelected();
            contentList.forEach(tSelectable -> tSelectable.selected = false);
            customRecycler.reload();

            ((CollapsingToolbarLayout) context.findViewById(R.id.collapsingToolbarLayout)).setTitleEnabled(true);
            Toolbar toolbar = context.findViewById(R.id.toolbar);
            toolbar.setTitle(context.plural);
            context.setSupportActionBar(toolbar);
            toolbar.setVisibility(View.VISIBLE);
            context.findViewById(R.id.media_selectionToolbar).setVisibility(View.GONE);

            return allSelected;
        }

        public CustomList<Selectable<T>> getAllSelected() {
            return contentList.filter(Selectable::isSelected, false);
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
            customRecycler.update(index);
            updateToolbarTitle();
            return this;
        }

        public void setSelected(int index, boolean selected) {
            contentList.get(index).selected = selected;
            customRecycler.update(index);
            updateToolbarTitle();
        }

        private void updateToolbarTitle() {
            ((Toolbar) context.findViewById(R.id.media_selectionToolbar)).setTitle(String.format(Locale.getDefault(), "Auswählen (%d)", getAllSelectedCount()));
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
    private void showEditMultipleDialog(List<Media> oldMedia) {
        if (!Utility.isOnline(this))
            return;

        setResult(RESULT_OK);
        removeFocusFromSearch();

        boolean isAdd = oldMedia == null || oldMedia.isEmpty();
        CustomList<Media> newMedia = isAdd ? new CustomList<>() : oldMedia.stream().map(Media::clone).collect(Collectors.toCollection(CustomList::new));
        CustomList<String> mediaPersonIdList;
        if (newMedia.isEmpty()) {
            mediaPersonIdList = new CustomList<>();
        } else {
            mediaPersonIdList = new CustomList<>(newMedia.get(0).getPersonIdList());
            if (newMedia.size() > 1)
                newMedia.forEach(media -> mediaPersonIdList.retainAll(media.getPersonIdList()));
        }

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

                    ((TextView) view.findViewById(R.id.dialog_editMedia_persons)).setText(String.join(", ", mediaPersonIdList.map(id -> database.mediaPersonMap.get(id).getName())));
                })
                .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                .addOptionalModifications(customDialog -> {
                    if (!isAdd)
                        customDialog.addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                            oldMedia.forEach(media -> database.mediaMap.remove(media.getUuid()));
                            Toast.makeText(this, (Database.saveAll_simple() ? plural : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();
                        })
                        .alignPreviousButtonsLeft()
                        .addConfirmationDialogToLastAddedButton(plural + " Löschen", "Möchstest du wirklich " + oldMedia.size() + (oldMedia.size() > 1 ? plural : singular) + " löschen?");
                })
                .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                    saveMultipleMedia(customDialog, oldMedia, newMedia, mediaPersonIdList);
                })
                .show();
    }

    private void saveMultipleMedia(CustomDialog editDialog, List<Media> oldMedia, List<Media> newMedia, CustomList<String> mediaPersonIdList) {
        boolean isAdd = oldMedia == null || oldMedia.isEmpty();

        if (isAdd) {
            newMedia.forEach(media -> media.getPersonIdList().addAll(mediaPersonIdList));
            database.mediaMap.putAll(newMedia.stream().collect(Collectors.toMap(Media::getUuid, o -> o)));
        }

        Toast.makeText(this, (Database.saveAll_simple() ? newMedia.size() + " " + (newMedia.size() > 1 ? plural : singular) : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();

        reLoadRecycler();
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
            ActivityResultHelper.addMultiFileChooserRequest(context, "image/* video/*",intent -> {
                CustomList<String> pathList = new CustomList<>();
                if(intent.getClipData() != null) {
                    int count = intent.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = intent.getClipData().getItemAt(i).getUri();
                        String path = ActivityResultHelper.getPath(context, imageUri);
                        pathList.add(path);
                    }
                } else if(intent.getData() != null) {
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

                                            ImageView imageView = (ImageView) itemView.findViewById(R.id.listItem_image_imgaeView);
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

            ImageView imageView = (ImageView) view.findViewById(R.id.listItem_image_imgaeView);
            Glide.with(imageView.getContext())
                    .load(imageUri)
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


    //  ------------------------- ToolBar ------------------------->
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_media, menu);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
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
        } else
            super.onBackPressed();
    }
}

/*
KONZEPT:
• Sammlung von Fotos und Videos

• Kategorien:
    • Personen
    • Tags
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