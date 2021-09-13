package com.maxMustermannGeheim.linkcollection.Activities.Content.Media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
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
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tree;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.ActivityResultHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.FastScrollRecyclerViewHelper;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import me.zhanghai.android.fastscroll.Predicate;

import static com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity.SHARED_PREFERENCES_DATA;

public class MediaEventActivity extends AppCompatActivity {
    private static final String TAG = "MediaEventActivity";
    public static final int START_SEARCH_MEDIA_EVENT = 1234;
    public static final int START_OPEN_MEDIA_EVENT = 7891;
    private final String ADVANCED_SEARCH_CRITERIA__EVENT = "e";

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
    private MediaEvent parent;
    private Menu menu;

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
                    .addCriteria(helper -> new Helpers.AdvancedQueryHelper.SearchCriteria<MediaEvent, MediaEvent>(ADVANCED_SEARCH_CRITERIA__EVENT, Helpers.AdvancedQueryHelper.PARENT_CLASS_PATTERN)
                            .setCategory(CategoriesActivity.CATEGORIES.MEDIA_EVENT)
                            .setParser(sub -> (MediaEvent) ParentClass_Tree.findObjectByName(CategoriesActivity.CATEGORIES.MEDIA_EVENT, sub, false))
                            .setBuildPredicate(mediaEvent -> {
                                if (mediaEvent == null) {
                                    parent = null;
                                    return null;
                                }
                                parent = mediaEvent;
                                return mediaSelectable -> true;
                            }))
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
    private CustomList<MediaEvent> filterList(CustomList<MediaEvent> mediaEventList) {
        if (!searchQuery.isEmpty()) {
            advancedQueryHelper.filterFull(mediaEventList);
        }
        return mediaEventList;
    }

    private CustomList<MediaEvent> sortList(CustomList<MediaEvent> mediaEventList) {

        mediaEventList.sort((media1, media2) -> {
//            File file1 = new File(media1.getContent().getImagePath());
//            File file2 = new File(media2.getContent().getImagePath());
//            return Long.compare(file1.lastModified(), file2.lastModified()) * -1;
            return media1.getName().compareTo(media2.getName());
        });
        return mediaEventList;
    }

    private void loadRecycler() {
        // ToDo: Versuchen Recycler diskret zu scrollen
        RecyclerView recyclerView = findViewById(R.id.recycler);

        final List<Integer>[] heightList = new List[]{new ArrayList<>()};
        final int[] scrollRange = {0};

        eventRecycler = new CustomRecycler<MediaEvent>(this, recyclerView)
                .setItemLayout(R.layout.list_item_media_event)
                .setGetActiveObjectList(customRecycler -> {
                    CustomList<MediaEvent> filteredList = filterList(new CustomList<>(database.mediaEventMap.values()));

                    if (parent == null) {
                        sortList(filteredList);
                        setToolbarButtons();
                    } else {
                        setToolbarButtons();
                        filteredList = new CustomList<>();
                        CustomList<MediaEvent> finalFilteredList = filteredList;
                        parent.getChildren().forEach(parentClass_tree -> finalFilteredList.add((MediaEvent) parentClass_tree));
                        sortList(filteredList);
                        if (!parent.getMediaIdList().isEmpty())
                            filteredList.add(new MediaEvent("Sonstige: " + parent.getName())
                                    ._enableDummy()
                                    .setDescription(parent.getDescription())
                                    .setBeginning(parent.getBeginning())
                                    .setEnd(parent.getEnd())
                                    .setMediaIdList(parent.getMediaIdList()));
                    }

                    TextView noItem = findViewById(R.id.no_item);
                    String text = searchView.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
//                    String viewsCountText = (views > 1 ? views + " Episoden" : (views == 1 ? "Eine" : "Keine") + " Episode") + " angesehen";
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText).append("\n", new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    elementCount.setText(builder);

                    heightList[0] = filteredList.stream().map(MediaEvent::_getHeight).collect(Collectors.toList());
                    scrollRange[0] = heightList[0].stream().mapToInt(Integer::intValue).sum();
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



                    Utility.GenericReturnInterface<MediaEvent, List<ParentClass>> flattenMediaEvent = event -> {
                        List<ParentClass> list = new ArrayList<>();
                        if (!event.getChildren().isEmpty())
                            event.getChildren().forEach(parentClass_tree -> list.add((ParentClass) parentClass_tree));
                        if (!event.getMediaIdList().isEmpty())
                            list.addAll(Utility.idToParentClassList(CategoriesActivity.CATEGORIES.MEDIA, event.getMediaIdList()));
                        return list;
                    };

                    Utility.TripleGenericInterface<ImageView, ParentClass,Pair<Integer,Integer>> applyImage = (imageView, parentClass, dimensions) -> {
                        imageView.setVisibility(View.VISIBLE);
                        RequestOptions myOptions = new RequestOptions()
                                .override(CustomUtility.dpToPx(dimensions.first), CustomUtility.dpToPx(dimensions.second))
                                .centerCrop();
                        String imagePath = "";
                        if (parentClass instanceof Media)
                            imagePath = ((Media) parentClass).getImagePath();
                        else if (parentClass instanceof MediaEvent) {
                            imagePath = ((MediaEvent) parentClass)._getThumbnailPath();
                        } else {
                            Glide.with(imageView.getContext())
                                    .load(R.drawable.ic_no_image)
                                    .error(R.drawable.ic_broken_image)
                                    .apply(myOptions)
                                    .into(imageView);
                            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(Color.WHITE));
                            return;
                        }


                        Glide.with(imageView.getContext())
                                .load(Uri.fromFile(new File(imagePath)))
                                .error(R.drawable.ic_broken_image)
                                .apply(myOptions)
                                .into(imageView);

                    };

                    RecyclerView contentRecycler = itemView.findViewById(R.id.listItem_mediaEvent_content);
                    contentRecycler.setNestedScrollingEnabled(false);
                    new CustomRecycler<ParentClass>(this, contentRecycler)
                            .setItemLayout(R.layout.empty_layout)
                            .setObjectList(flattenMediaEvent.run(mediaEvent))
                            .setSetItemContent((customRecycler1, itemView1, parentClass) -> {
                                View view = null;
                                if (parentClass instanceof Media) {
                                    view = LayoutInflater.from(this).inflate(R.layout.list_item_image, (ViewGroup) itemView1, false);
                                    Media media = (Media) parentClass;
                                    MediaActivity.SelectMediaHelper.loadPathIntoImageView(media.getImagePath(), view, 99);
                                } else if (parentClass instanceof MediaEvent) {
                                    view = LayoutInflater.from(this).inflate(R.layout.list_item_media_event_compact, (ViewGroup) itemView1, false);
                                    List<ParentClass> list = flattenMediaEvent.run((MediaEvent) parentClass);
                                    switch (list.size()) {
                                        case 0:
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image1), null, Pair.create(95, 95));
                                            view.findViewById(R.id.listItem_mediaEventCompact_image2).setVisibility(View.GONE);
                                            view.findViewById(R.id.listItem_mediaEventCompact_image3).setVisibility(View.GONE);
                                            view.findViewById(R.id.listItem_mediaEventCompact_image4).setVisibility(View.GONE);
                                            break;
                                        case 1:
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image1), list.get(0), Pair.create(95, 95));
                                            view.findViewById(R.id.listItem_mediaEventCompact_image2).setVisibility(View.GONE);
                                            view.findViewById(R.id.listItem_mediaEventCompact_image3).setVisibility(View.GONE);
                                            view.findViewById(R.id.listItem_mediaEventCompact_image4).setVisibility(View.GONE);
                                            break;
                                        case 2:
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image1), list.get(0), Pair.create(46, 95));
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image2), list.get(1), Pair.create(46, 95));
                                            view.findViewById(R.id.listItem_mediaEventCompact_image3).setVisibility(View.GONE);
                                            view.findViewById(R.id.listItem_mediaEventCompact_image4).setVisibility(View.GONE);
                                            break;
                                        case 3:
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image1), list.get(0), Pair.create(46, 46));
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image2), list.get(1), Pair.create(46, 46));
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image3), list.get(2), Pair.create(95, 46));
                                            view.findViewById(R.id.listItem_mediaEventCompact_image4).setVisibility(View.GONE);
                                            break;
                                        default:
                                        case 4:
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image1), list.get(0), Pair.create(46, 46));
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image2), list.get(1), Pair.create(46, 46));
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image3), list.get(2), Pair.create(46, 46));
                                            applyImage.run(view.findViewById(R.id.listItem_mediaEventCompact_image4), list.get(3), Pair.create(46, 46));
                                            break;
                                    }
                                }
                                itemView1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                ((LinearLayout) itemView1).removeAllViews();
                                ((LinearLayout) itemView1).addView(view);
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
                })
                .setOnClickListener((customRecycler, itemView, mediaEvent, index) -> {
                    if (mediaEvent.getChildren().isEmpty()) {
                        if (mediaEvent._isDummy() && parent != null)
                            mediaEvent = parent;
                        startActivityForResult(new Intent(this, MediaActivity.class)
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH, CategoriesActivity.escapeForSearchExtra(mediaEvent.getName()))
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_EVENT),
                                START_SEARCH_MEDIA_EVENT);
                    } else {
                        startActivityForResult(new Intent(this, MediaEventActivity.class)
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH, CategoriesActivity.escapeForSearchExtra(mediaEvent.getName()))
                                        .putExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY, CategoriesActivity.CATEGORIES.MEDIA_EVENT),
                                START_OPEN_MEDIA_EVENT);
                    }
                })
                .setOnLongClickListener((customRecycler, view, mediaEvent, index) -> showEditDialog(mediaEvent._isDummy() ? parent : mediaEvent))
                .generate();

        FastScroller[] fastScroller = {null};
        FastScrollerBuilder fastScrollerBuilder = new FastScrollerBuilder(recyclerView)
                .setThumbDrawable(Objects.requireNonNull(getDrawable(R.drawable.fast_scroll_thumb)))
                .setTrackDrawable(Objects.requireNonNull(getDrawable(R.drawable.fast_scroll_track)));
//        fastScrollerBuilder.disableScrollbarAutoHide();
        fastScroller[0] = fastScrollerBuilder
                .setPadding(0, 20, 0, 50)
                .setViewHelper(new FastScrollRecyclerViewHelper(eventRecycler, fastScroller, scrollRange, heightList, false, null))
                .build();

    }

    private void reLoadRecycler() {
        eventRecycler.reload();
    }
    /**  <------------------------- Recycler -------------------------  */



    /**  ------------------------- Edit ------------------------->  */
    private CustomDialog showEditDialog(@Nullable MediaEvent oldEvent) {
        if (!Utility.isOnline(this))
            return null;

        removeFocusFromSearch();

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
//                    CustomList<MediaEvent> mediaEvents = new CustomList<>(database.mediaEventMap.values());
//                    if (oldEvent != null)
//                        mediaEvents.remove(oldEvent);
                    com.finn.androidUtilities.Helpers.TextInputHelper helper = new com.finn.androidUtilities.Helpers.TextInputHelper()
                            .defaultDialogValidation(customDialog)
                            .addValidator(editTitleLayout, editDescriptionLayout)
                            .setValidation(editTitleLayout, (validator, text) -> {
                                ParentClass_Tree object = ParentClass_Tree.findObjectByName(CategoriesActivity.CATEGORIES.MEDIA_EVENT, text, true);
                                if (object != null && object != oldEvent)
                                    validator.setInvalid("Event mit diesem Namen bereits vorhanden");
                            })
                            .warnIfEmpty(editDescriptionLayout)
                            .interceptDialogActionForValidation(customDialog, true, (inputHelper) -> {
                                toast.setText("Warnung: " + inputHelper.getMessage(editDescriptionLayout).get(0) + "\n(Doppel-Click zum Fortfahren)");
                                toast.show();
                            }, t -> toast.cancel());
                    helper
                            .validate(new TextInputLayout[]{null});

                    FrameLayout selectedMediaParent = view.findViewById(R.id.dialog_editMediaEvent_selectParent);
                    MediaActivity.SelectMediaHelper.Builder(this, selectedMediaList)
                            .setParentView(selectedMediaParent)
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
                    // ToDo: Auch möglich machen Kinder zu löschen (nicht wenn selbst kinder)
                    if (!isAdd) {
                        customDialog
                                .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                                    if (parent == null)
                                        database.mediaEventMap.remove(oldEvent.getUuid());
                                    else
                                        parent.getChildren().remove(oldEvent);
                                    reLoadRecycler();
                                    setResult(RESULT_OK);
                                    Database.saveAll(this, CustomUtility.Triple.create("Event Gelöscht", null, "Löschen Fehlgeschlagen"));
                                })
                                .addOnDisabledClickToLastAddedButton(customDialog1 -> {
                                    Toast.makeText(this, "Events mit Kindern können nicht gelöscht werden", Toast.LENGTH_SHORT).show();
                                })
                                .alignPreviousButtonsLeft()
                                .transformPreviousButtonToImageButton()
                                .addConfirmationDialogToLastAddedButton("", "", customDialog1 -> {
                                    customDialog1
                                            .setTitle(singular + " Löschen")
                                            .setText(new Helpers.SpannableStringHelper().append("Möchstest du wirklich '").appendBold(oldEvent.getName()).append("' löschen?").get());
                                });
                        if (!oldEvent.getChildren().isEmpty()) {
//                            Toast.makeText(this, "Ein Event mit Kindern kann nicht gelöscht werden", Toast.LENGTH_SHORT).show();
                            customDialog.disableLastAddedButton();
                        }
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
        newEvent.setDescription(CustomUtility.stringExistsOrElse(description, null)).setName(title);
        newEvent.setMediaIdList(selectedMediaList.map(ParentClass::getUuid));
        newEvent.setBeginning(beginningEnd.first);
        newEvent.setEnd(beginningEnd.second);

        if (isAdd) {
            if (parent == null)
                database.mediaEventMap.put(newEvent.getUuid(), newEvent);
            else
                parent.addChild(newEvent);
        } else {
            oldEvent.getChangesFrom(newEvent);
        }

        if (Database.saveAll_simple(this)) {
            setResult(RESULT_OK);
            editDialog.dismiss();
            reLoadRecycler();
        }
    }
    /**  <------------------------- Edit -------------------------  */



    /**  ------------------------- Convenience ------------------------->  */
    public static CustomList<MediaEvent> getEventsContaining(Media media) {
        CustomList<MediaEvent> list = new CustomList<>();
        Utility.RecursiveGenericInterface<MediaEvent> recursiveInterface = (mediaEvent, childRecursiveInterface) -> {
            if (mediaEvent.getMediaIdList().contains(media.getUuid()))
                list.add(mediaEvent);
            mediaEvent.getChildren().forEach(child -> childRecursiveInterface.run((MediaEvent) child, childRecursiveInterface));
        };
        Database.getInstance().mediaEventMap.values().forEach(child -> recursiveInterface.run(child, recursiveInterface));
        return list;
    }
    /**  <------------------------- Convenience -------------------------  */



    /**
     * ------------------------- Toolbar ------------------------->
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.task_bar_media_event, this.menu);
        Utility.colorMenuItemIcon(menu, R.id.taskBar_mediaEvent_edit, Color.WHITE);
        setToolbarButtons();

        if (setToolbarTitle != null) setToolbarTitle.run();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_mediaEvent_edit:
                showEditDialog(parent);
                break;
            case R.id.taskBar_mediaEvent_add:
                showEditDialog(null);
                break;
            case R.id.taskBar_mediaEvent_sortTree:
                ParentClass_Tree.showReorderTreeDialog(this, CategoriesActivity.CATEGORIES.MEDIA_EVENT, customDialog -> reLoadRecycler());
                break;

            case android.R.id.home:
                if (getCallingActivity() == null)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
        return true;
    }

    private void setToolbarButtons() {
        boolean hasParent = parent != null;
        if (menu != null) {
            menu.findItem(R.id.taskBar_mediaEvent_edit).setVisible(hasParent);
            menu.findItem(R.id.taskBar_mediaEvent_sortTree).setVisible(!hasParent);
        }
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
//        if (requestCode == START_OPEN_MEDIA_EVENT || requestCode == START_OPEN_MEDIA_EVENT)
        if (resultCode == RESULT_OK)
            reLoadRecycler();
    }
}