package com.maxMustermannGeheim.linkcollection.Activities.Content.Videos;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomRecycler;
import com.finn.androidUtilities.CustomUtility;
import com.finn.androidUtilities.Helpers;
import com.finn.androidUtilities.ParentClass;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Collection;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Video;
import com.maxMustermannGeheim.linkcollection.R;
import com.maxMustermannGeheim.linkcollection.Utilities.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CollectionActivity extends AppCompatActivity {
    enum SORT_TYPE {
        NAME, VIEWS, RATING, LATEST
    }

    public enum FILTER_TYPE {
        NAME("Titel"), ACTOR("Darsteller"), GENRE("Genre"), STUDIO("Studio");

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
    private boolean scrolling = true;
    private SORT_TYPE sort_type = SORT_TYPE.LATEST;
    private HashSet<FILTER_TYPE> filterTypeSet = new HashSet<>(Arrays.asList(FILTER_TYPE.NAME, FILTER_TYPE.ACTOR, FILTER_TYPE.GENRE, FILTER_TYPE.STUDIO));
    private SearchView.OnQueryTextListener textListener;
    private TextView elementCount;
    private String searchQuery = "";
    private boolean reverse = false;
    private SearchView searchView;
    private CustomRecycler<Collection> customRecycler;
    private CustomDialog editDialog;
    private CustomDialog detailDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (((database = Database.getInstance()) == null))
            setContentView(R.layout.loading_screen);
        else
            setContentView(R.layout.activity_collection);

        mySPR_daten = getSharedPreferences(MainActivity.SHARED_PREFERENCES_DATA, MODE_PRIVATE);

        loadDatabase();
    }

    private void loadDatabase() {
        @SuppressLint("RestrictedApi") Runnable whenLoaded = () -> {
//            for (Video video : database.videoMap.values()) {
//                if (video.getDateList().isEmpty() && !video.isUpcoming() && !Utility.getWatchLaterList().contains(video))
//                    video.setWatchLater(true);
////                    database.watchLaterList.add(video.getUuid());
//            }
            String BREAKPOINT = null;
            // ToDo: Gelöschte Filme entfernen

            setContentView(R.layout.activity_collection);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            elementCount = findViewById(R.id.elementCount);

            AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
            View noItem = findViewById(R.id.no_item);
            LinearLayout search_layout = findViewById(R.id.search_layout);
            appBarLayout.measure(0, 0);
            toolbar.measure(0, 0);
            search_layout.measure(0, 0);
            float maxOffset = -(appBarLayout.getMeasuredHeight() - (toolbar.getMeasuredHeight() + search_layout.getMeasuredHeight()));
            float distance = 118;
            appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
                float alpha = 1f - ((verticalOffset - maxOffset) / distance);
                noItem.setAlpha(alpha > 0f ? alpha : 0f);
            });


            searchView = findViewById(R.id.search);

            loadRecycler();

            textListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    this.onQueryTextChange(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    searchQuery = query;
                    reLoadRecycler();
                    return true;
                }
            };
            searchView.setOnQueryTextListener(textListener);

            if (database.genreMap.isEmpty() && Settings.getSingleSetting_boolean(this, Settings.SETTING_VIDEO_ASK_FOR_GENRE_IMPORT)) {
                CustomDialog.Builder(this)
                        .setTitle("Genres Importieren")
                        .setText("Es wurde bisher noch kein Genre hinzugefügt. Sollen die Genres aus der TMDb importiert werden?\nDies kann auch jederzeit in den Einstellungen getan werden.")
                        .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.YES_NO)
                        .addButton("Nicht erneut Fragen", customDialog -> {
                            Settings.changeSetting(Settings.SETTING_VIDEO_ASK_FOR_GENRE_IMPORT, "false");
                            Toast.makeText(this, "Du wirst nicht erneut gefragt", Toast.LENGTH_SHORT).show();
                        })
                        .alignPreviousButtonsLeft()
                        .addButton(CustomDialog.BUTTON_TYPE.YES_BUTTON, customDialog -> {
                            Utility.importTmdbGenre(this, true);
                            setResult(RESULT_OK);
                        })
                        .show();
                return;
            }


//            CategoriesActivity.CATEGORIES extraSearchCategory = (CategoriesActivity.CATEGORIES) getIntent().getSerializableExtra(CategoriesActivity.EXTRA_SEARCH_CATEGORY);
//            if (extraSearchCategory != null) {
//                if (!extraSearchCategory.equals(CategoriesActivity.CATEGORIES.VIDEO)) {
//                    filterTypeSet.clear();
//
//                    switch (extraSearchCategory) {
//                        case DARSTELLER:
//                            filterTypeSet.add(VideoActivity.FILTER_TYPE.ACTOR);
//                            break;
//                        case GENRE:
//                            filterTypeSet.add(VideoActivity.FILTER_TYPE.GENRE);
//                            break;
//                        case STUDIOS:
//                            filterTypeSet.add(VideoActivity.FILTER_TYPE.STUDIO);
//                            break;
//                    }
//                }
//
//                String extraSearch = getIntent().getStringExtra(CategoriesActivity.EXTRA_SEARCH);
//                if (extraSearch != null) {
//                    if (extraSearch.equals(SEEN_SEARCH))
//                        mode = VideoActivity.MODE.SEEN;
//                    else if (extraSearch.equals(WATCH_LATER_SEARCH))
//                        mode = VideoActivity.MODE.LATER;
//                    else if (extraSearch.equals(UPCOMING_SEARCH))
//                        mode = VideoActivity.MODE.UPCOMING;
//                    else
//                        searchView.setQuery(extraSearch, false);
//                    textListener.onQueryTextSubmit(searchView.getQuery().toString());
//                }
//            }
//            setSearchHint();


        };

        if (database == null || !Database.isReady()) {
            Database.getInstance(mySPR_daten, newDatabase -> {
                database = newDatabase;
                whenLoaded.run();
            }, false);
        } else
            whenLoaded.run();
    }

    //  ------------------------- List ------------------------->
    private CustomList<Collection> getAllCollections() {
        return new CustomList<>(database.collectionMap.values());
    }

    private CustomList<Collection> filterList(CustomList<Collection> collectionList) {
        CustomList<Collection> filteredVideoList = new CustomList<>(collectionList);
//        if (mode.equals(VideoActivity.MODE.SEEN)) {
//            filteredVideoList = allVideoList.stream().filter(video -> !video.getDateList().isEmpty()).collect(Collectors.toCollection(CustomList::new));
//        } else if (mode.equals(VideoActivity.MODE.LATER)) {
//            filteredVideoList = Utility.getWatchLaterList();
//        } else if (mode.equals(VideoActivity.MODE.UPCOMING)) {
//            filteredVideoList = allVideoList.stream().filter(Video::isUpcoming).collect(Collectors.toCollection(CustomList::new));
//        }
//        if (!searchQuery.trim().equals("")) {
//
//
//            String subQuery = searchQuery;
//            if (searchQuery.contains("*")) {
//
//                Matcher matcher = pattern.matcher(searchQuery);
//                if (matcher.find()) {
//                    String[] range = matcher.group(0).replaceAll("\\*", "").replaceAll(",", ".").split("-");
//                    float min = Float.parseFloat(range[0]);
//                    float max = Float.parseFloat(range.length < 2 ? range[0] : range[1]);
//                    filteredVideoList.filter(video -> video.getRating() >= min && video.getRating() <= max, true);
//                    subQuery = matcher.replaceFirst("");
//                }
//            }
//
//            if (subQuery.contains("|")) {
//                filteredVideoList = filteredVideoList.filterOr(subQuery.split("\\|"), (video, s) -> Utility.containedInVideo(s.trim(), video, filterTypeSet), true);
//            } else {
//                filteredVideoList.filterAnd(subQuery.split("&"), (video, s) -> Utility.containedInVideo(s.trim(), video, filterTypeSet), true);
//            }
//        }
        return filteredVideoList;
    }

    private CustomList<Collection> sortList(CustomList<Collection> collectionList) {
//        switch (sort_type) {
//            case NAME:
//                collectionList.sort((video1, video2) -> video1.getName().compareTo(video2.getName()));
//                if (reverse)
//                    Collections.reverse(collectionList);
//                break;
//            case VIEWS:
//                collectionList.sort((video1, video2) -> {
//                    if (video1.getDateList().size() == video2.getDateList().size())
//                        return video1.getName().compareTo(video2.getName());
//                    else
//                        return Integer.compare(video1.getDateList().size(), video2.getDateList().size()) * (reverse ? 1 : -1);
//                });
//                break;
//            case RATING:
//                collectionList.sort((video1, video2) -> {
//                    if (video1.getRating().equals(video2.getRating()))
//                        return video1.getName().compareTo(video2.getName());
//                    else
//                        return video1.getRating().compareTo(video2.getRating()) * (reverse ? 1 : -1);
//                });
//                break;
//            case LATEST:
//                collectionList.sort((video1, video2) -> {
//                    if (video1.getDateList().isEmpty() && video2.getDateList().isEmpty()) {
//                        if (video1.isUpcoming() && video2.isUpcoming())
//                            return video1.getRelease().compareTo(video2.getRelease());
//                        else if (!video1.isUpcoming() && !video2.isUpcoming())
//                            return video1.getName().compareTo(video2.getName());
//                        else if (video1.isUpcoming())
//                            return reverse ? -1 : 1;
//                        else if (video2.isUpcoming())
//                            return reverse ? 1 : -1;
//                    } else if (video1.getDateList().isEmpty())
//                        return reverse ? -1 : 1;
//                    else if (video2.getDateList().isEmpty())
//                        return reverse ? 1 : -1;
//
//                    Date new1 = Collections.max(video1.getDateList());
//                    Date new2 = Collections.max(video2.getDateList());
//                    if (new1.equals(new2))
//                        return video1.getName().compareTo(video2.getName());
//                    else
//                        return new1.compareTo(new2) * (reverse ? 1 : -1);
//                });
//                break;
//
//        }
        return collectionList;
    }
    //  <------------------------- List -------------------------


    //  ------------------------- Recycler ------------------------->
    private void loadRecycler() {
        customRecycler = new CustomRecycler<Collection>(this, findViewById(R.id.recycler))
                .setItemLayout(R.layout.list_item_collection)
                .setGetActiveObjectList(customRecycler -> {
                    CustomList<Collection> filteredList = sortList(filterList(getAllCollections()));
                    TextView noItem = findViewById(R.id.no_item);
                    String text = searchView.getQuery().toString().isEmpty() ? "Keine Einträge" : "Kein Eintrag für diese Suche";
                    int size = filteredList.size();

                    noItem.setText(size == 0 ? text : "");
                    String elementCountText = size > 1 ? size + " Elemente" : (size == 1 ? "Ein" : "Kein") + " Element";
                    SpannableStringBuilder builder = new SpannableStringBuilder().append(elementCountText);
//                    if (size > 0) {
//                        int viewSum = filteredList.stream().mapToInt(video -> video.getDateList().size()).sum();
//                        String viewSumText = viewSum > 1 ? viewSum + " Ansichten" : (viewSum == 1 ? "Eine" : "Keine") + " Ansicht";
//                        builder.append("\n").append(viewSumText, new RelativeSizeSpan(0.5f), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
//
//                        double averageRating = filteredList.stream().filter(video -> video.getRating() > 0).mapToDouble(ParentClass_Ratable::getRating).average().orElse(-1);
//                        if (averageRating != -1)
//                            builder.append("  |  ").append(String.format(Locale.getDefault(), "Ø %.2f ☆", averageRating).replaceAll("([.,]?0*)(?= ☆)", ""));
//                    }
                    elementCount.setText(builder);
                    return filteredList;

                })
                .setSetItemContent((customRecycler, itemView, collection) -> {
                    String imagePath = collection.getImagePath();
                    ImageView thumbnail = itemView.findViewById(R.id.listItem_collection_thumbnail);
                    if (Utility.stringExists(imagePath)) {
                        Utility.loadUrlIntoImageView(this, thumbnail, (imagePath.contains("https") ? "" : "https://image.tmdb.org/t/p/w92/") + imagePath,
                                (imagePath.contains("https") ? "" : "https://image.tmdb.org/t/p/original/") + imagePath);
                        thumbnail.setVisibility(View.VISIBLE);
                    } else
                        thumbnail.setVisibility(View.GONE);


                    if (!collection.getFilmIdList().isEmpty()) {
                        ((TextView) itemView.findViewById(R.id.listItem_collection_videos_count)).setText(String.valueOf(collection.getFilmIdList().size()));
                        itemView.findViewById(R.id.listItem_collection_videos_count_layout).setVisibility(View.VISIBLE);
                    } else
                        itemView.findViewById(R.id.listItem_collection_videos_count_layout).setVisibility(View.GONE);
                    ((TextView) itemView.findViewById(R.id.listItem_collection_Titel)).setText(/*(Utility.stringExists(collection.getImagePath()) ? "" : "• ") + */collection.getName());
//                    if (!collection.getDateList().isEmpty()) {
//                        itemView.findViewById(R.id.listItem_collection_Views_layout).setVisibility(View.VISIBLE);
//                        ((TextView) itemView.findViewById(R.id.listItem_collection_Views)).setText(String.valueOf(collection.getDateList().size()));
//                    } else
//                        itemView.findViewById(R.id.listItem_collection_Views_layout).setVisibility(View.GONE);
//
//                    itemView.findViewById(R.id.listItem_collection_later).setVisibility(Utility.getWatchLaterList().contains(collection) ? View.VISIBLE : View.GONE);
//                    itemView.findViewById(R.id.listItem_collection_upcoming).setVisibility(collection.isUpcoming() ? View.VISIBLE : View.GONE);
//
//                    List<String> darstellerNames = new ArrayList<>();
//                    collection.getDarstellerList().forEach(uuid -> darstellerNames.add(database.darstellerMap.get(uuid).getName()));
//                    ((TextView) itemView.findViewById(R.id.listItem_collection_Darsteller)).setText(String.join(", ", darstellerNames));
//                    itemView.findViewById(R.id.listItem_collection_Darsteller).setSelected(scrolling);
//
//                    if (collection.getRating() > 0) {
//                        itemView.findViewById(R.id.listItem_collection_rating_layout).setVisibility(View.VISIBLE);
//                        ((TextView) itemView.findViewById(R.id.listItem_collection_rating)).setText(String.valueOf(collection.getRating()));
//                    } else
//                        itemView.findViewById(R.id.listItem_collection_rating_layout).setVisibility(View.GONE);
//
//                    List<String> studioNames = new ArrayList<>();
//                    collection.getStudioList().forEach(uuid -> studioNames.add(database.studioMap.get(uuid).getName()));
//                    ((TextView) itemView.findViewById(R.id.listItem_collection_Studio)).setText(String.join(", ", studioNames));
//                    itemView.findViewById(R.id.listItem_collection_Studio).setSelected(scrolling);
//
                    TextView videoNames = itemView.findViewById(R.id.listItem_collection_videos);
                    videoNames.setText(collection.getFilmIdList().stream().map(uuid -> database.videoMap.get(uuid).getName()).collect(Collectors.joining(", ")));
                    videoNames.setSelected(scrolling);
                })
                .setOnClickListener((customRecycler, view, collection, index) -> {
                    showDetailDialog(collection);
                })
                .addSubOnClickListener(R.id.listItem_collection_search, (customRecycler, view, collection, index) -> {
                }, false)
                .setOnLongClickListener((customRecycler, view, collection, index) -> {
                    showEditDialog(collection);
                })
                .generate();
    }

    private void reLoadRecycler() {
        customRecycler.reload();
    }
    //  <------------------------- Recycler -------------------------


    //  ------------------------- Detail ------------------------->
    private CustomDialog showDetailDialog(Collection collection) {
        final boolean[] hasChanged = {false};
        detailDialog = CustomDialog.Builder(this)
                .setTitle(collection.getName())
                .setView(R.layout.dialog_detail_collection)
                .setSetViewContent((customDialog, view, reload) -> {
                    CustomRecycler<Video> videoRecycler = new CustomRecycler<Video>(this, customDialog.findViewById(R.id.dialogDetail_collection_videos))
                            .setItemLayout(R.layout.list_item_collection_video)
                            .setGetActiveObjectList(customRecycler -> collection.getFilmIdList().stream().map(s -> database.videoMap.get(s)).collect(Collectors.toList()))
                            .setSetItemContent((customRecycler1, itemView, video) -> {
                                String imagePath = video.getImagePath();
                                ImageView thumbnail = itemView.findViewById(R.id.listItem_collectionVideo_thumbnail);
                                if (Utility.stringExists(imagePath)) {
                                    imagePath = Utility.getTmdbImagePath_ifNecessary(imagePath, true);
                                    Utility.loadUrlIntoImageView(this, thumbnail,
                                            imagePath, imagePath, null, () -> Utility.roundImageView(thumbnail, 8));
                                    thumbnail.setVisibility(View.VISIBLE);
                                } else
                                    thumbnail.setImageResource(R.drawable.ic_no_image);

                                // ToDo: collection Sortieren

                                ((TextView) itemView.findViewById(R.id.listItem_collectionVideo_text)).setText(video.getName());
                            })
                            .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                            .enableDragAndDrop(R.id.listItem_collectionVideo_text, (customRecycler1, objectList) -> {
                                collection.setFilmIdList(objectList.stream().map(ParentClass::getUuid).collect(Collectors.toList()));
                                hasChanged[0] = true;
                            }, true)
                            .generate();

                    view.findViewById(R.id.dialogDetail_collection_edtiVideos).setOnClickListener(v -> {
                        Utility.showEditItemDialog(this, null, collection.getFilmIdList(), collection, CategoriesActivity.CATEGORIES.COLLECTION)
                                .addOnDialogDismiss(customDialog1 -> {
                                    videoRecycler.reload();
                                    Database.saveAll();
                                });
                    });
                })
                .addOnDialogDismiss(customDialog -> {
                    if (hasChanged[0]) {
                        Database.saveAll();
                        reLoadRecycler();
                    }
                })
                .show();
        return detailDialog;
    }
    //  <------------------------- Detail -------------------------


    //  ------------------------- Edit ------------------------->
    private Collection showEditDialog(@Nullable Collection oldCollection) {
        Collection editCollection = oldCollection == null ? new Collection(null) : oldCollection.clone();

        CustomUtility.isOnline(this, () -> {
            removeFocusFromSearch();

            editDialog = CustomDialog.Builder(this)
                    .setTitle("Sammlung " + (oldCollection == null ? "Hinzufügen" : "Bearbeiten"))
                    .setView(R.layout.dialog_edit_or_add_collection)
                    .setSetViewContent((customDialog, view, reload) -> {
                        TextInputLayout editTitle_layout = view.findViewById(R.id.dialog_editOrAddCollection_Title_layout);
                        EditText editTitle = editTitle_layout.getEditText();

                        Helpers.TextInputHelper helper = new Helpers.TextInputHelper((Button) customDialog.getActionButton().getButton(), editTitle_layout);

                        if (editCollection.getName() != null) {
                            editTitle.setText(editCollection.getName());
                        }

                        setThumbnailButton(editCollection, customDialog);

                        ImageView thumbnailButton = view.findViewById(R.id.dialog_editOrAddCollection_thumbnail);
                        thumbnailButton.setOnClickListener(v -> {
                            int showButtonId = View.generateViewId();
                            ImageView imageView = new ImageView(this);
                            Utility.setDimensions(imageView, true, false);
                            imageView.setAdjustViewBounds(true);
                            imageView.setPadding(Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16), Utility.dpToPx(16));

                            CustomDialog.Builder(this)
                                    .setTitle("Thumbnail-URL Bearbeiten")
                                    .setEdit(new CustomDialog.EditBuilder()
                                            .setShowKeyboard(false)
                                            .setRegEx(CategoriesActivity.pictureRegexAll + "|")
                                            .setText(Utility.stringExistsOrElse(editCollection.getImagePath(), "").toString())
                                            .setHint("TMDb-Pfad, oder Bild-Url (https:...(.jpg / .png / .svg / ...))"))
                                    .addButton("Testen", CustomDialog::reloadView, showButtonId, false)
                                    .alignPreviousButtonsLeft()
                                    .setView(imageView)
                                    .setSetViewContent((customDialog1, view1, reload1) -> {
                                        String url = customDialog1.getEditText();

                                        if (!customDialog1.getActionButton().getButton().isEnabled()) {
                                            if (!url.isEmpty())
                                                Toast.makeText(this, "Ungültige Eingabe", Toast.LENGTH_SHORT).show();
                                            imageView.setVisibility(View.GONE);
                                            return;
                                        }

                                        if (Utility.stringExists(url)) {
                                            imageView.setVisibility(View.VISIBLE);
                                            Utility.loadUrlIntoImageView(this, imageView, (url.contains("https") ? "" : "https://image.tmdb.org/t/p/original/") + url, null);
                                        } else
                                            imageView.setVisibility(View.GONE);
                                    })
                                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                                    .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog1 -> {
                                        String url = customDialog1.getEditText();
                                        if (url.isEmpty())
                                            url = null;

                                        editCollection.setImagePath(url);
                                        setThumbnailButton(editCollection, customDialog);
                                    })
                                    .show();
                        });
                        setThumbnailButton(editCollection, editDialog);

                        ((TextView) customDialog.findViewById(R.id.dialog_editOrAddCollection_films)).setText(
                                editCollection.getFilmIdList().stream().map(uuid -> database.videoMap.get(uuid).getName()).collect(Collectors.joining(", ")));

                        ImageView editFilms = customDialog.findViewById(R.id.dialog_editOrAddCollection_editFilms);
                        editFilms.setOnClickListener(v -> {
                            Utility.showEditItemDialog(this, customDialog, editCollection.getFilmIdList(), editCollection, CategoriesActivity.CATEGORIES.COLLECTION);
                        });

                        customDialog.findViewById(R.id.dialog_editOrAddCollection_autoImportToCollection).setOnClickListener(v -> {
                            String title = editTitle.getText().toString().trim();
                            if (!Utility.stringExists(title))
                                Toast.makeText(this, "Erst einen Namen eingeben", Toast.LENGTH_SHORT).show();
                            apiSearchRequest(title, customDialog, editCollection);
                        });
                    })
                    .addOptionalModifications(customDialog -> {
                        if (oldCollection != null)
                            customDialog
                                    .addButton(CustomDialog.BUTTON_TYPE.DELETE_BUTTON, customDialog1 -> {
                                        Toast.makeText(this, "Löschen", Toast.LENGTH_SHORT).show();
                                    }, false)
                                    .transformPreviousButtonToImageButton()
                                    .alignPreviousButtonsLeft();
                    })
                    .setButtonConfiguration(CustomDialog.BUTTON_CONFIGURATION.SAVE_CANCEL)
                    .addButton(CustomDialog.BUTTON_TYPE.SAVE_BUTTON, customDialog -> {
                        CustomUtility.isOnline(this, () -> {
                            saveCollection(customDialog, oldCollection, editCollection);
                        });
                    }, false)
                    .disableLastAddedButton()
                    .enableDoubleClickOutsideToDismiss(customDialog -> false)
                    .show();
        });

        return editCollection;
    }

    private void setThumbnailButton(Collection collection, CustomDialog customDialog) {
        if (collection == null || customDialog == null)
            return;
        String path = collection.getImagePath();
        boolean stringExists = Utility.stringExists(path);
        ImageView thumbnail = customDialog.findViewById(R.id.dialog_editOrAddCollection_thumbnail);
        CustomUtility.ifNotNull(thumbnail, view -> {
            view.setAlpha(stringExists ? 1f : 0.4f);
        });
        if (stringExists)
            Utility.loadUrlIntoImageView(this, thumbnail, (path.contains("https") ? "" : "https://image.tmdb.org/t/p/w92/") + path, null);
    }

    private void saveCollection(CustomDialog editDialog, @Nullable Collection oldCollection, Collection editCollection) {
        boolean isNew = oldCollection == null;
        if (isNew)
            oldCollection = editCollection;
        else
            oldCollection.getChangesFrom(editCollection);

        oldCollection.setName(((TextInputLayout) editDialog.findViewById(R.id.dialog_editOrAddCollection_Title_layout)).getEditText().getText().toString().trim());

        database.collectionMap.put(oldCollection.getUuid(), oldCollection);

        reLoadRecycler();
        Toast.makeText(this, (Database.saveAll() ? "Sammlung" : "Nichts") + " Gespeichert", Toast.LENGTH_SHORT).show();
        editDialog.dismiss();

        if (editDialog.getPayload() != null)
            ((CustomDialog) editDialog.getPayload()).reloadView();
    }
    //  <------------------------- Edit -------------------------


    //  ------------------------- API-Call ------------------------->
    private void apiSearchRequest(String queue, CustomDialog customDialog, Collection collection) {
        if (!Utility.isOnline(this))
            return;

        String requestUrl = "https://api.themoviedb.org/3/search/multi?api_key=09e015a2106437cbc33bf79eb512b32d&language=de&query=" +
                queue +
                "&page=1&include_adult=false";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        Toast.makeText(this, "Einen Moment bitte..", Toast.LENGTH_SHORT).show();



        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
            JSONArray results;
            try {
                results = response.getJSONArray("results");

                if (results.length() == 0) {
                    Toast.makeText(this, "Keine Ergebnisse gefunden", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> foundList = new ArrayList<>();

                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);

                    int id = object.getInt("id");

                    database.videoMap.values().stream().filter(video -> video.getTmdId() == id).findFirst().ifPresent(video -> foundList.add(video.getUuid()));

                }

                foundList.removeAll(collection.getFilmIdList());
                collection.getFilmIdList().addAll(foundList);
                customDialog.reloadView();
                Toast.makeText(this, foundList.isEmpty() ? "Nichts hinzugefügt" : (foundList.size() == 1 ? "Einen" : foundList.size()) + " hinzugefügt", Toast.LENGTH_SHORT).show();

            } catch (JSONException ignored) {
            }

        }, error -> Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonArrayRequest);

    }
    //  <------------------------- API-Call -------------------------


    //  ------------------------- Convenience ------------------------->
    private boolean commitSearch() {
        return textListener.onQueryTextChange(searchView.getQuery().toString());
    }

    private void removeFocusFromSearch() {
        searchView.clearFocus();
    }
//  <------------------------- Convenience -------------------------


    //  ------------------------- ToolBar ------------------------->
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_bar_collection, menu);

        menu.findItem(R.id.taskBar_collection_filterByName)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.NAME));
        menu.findItem(R.id.taskBar_collection_filterByDarsteller)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.ACTOR));
        menu.findItem(R.id.taskBar_collection_filterByStudio)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.STUDIO));
        menu.findItem(R.id.taskBar_collection_filterByGenre)
                .setChecked(filterTypeSet.contains(FILTER_TYPE.GENRE));

        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (!Database.isReady())
            return true;

        int id = item.getItemId();
        switch (id) {
            case R.id.taskBar_collection_add:
                showEditDialog(null);
                break;
//            case R.id.taskBar_collection_random:
//                showRandomDialog();
//                break;
//            case R.id.taskBar_collection_scroll:
//                if (item.isChecked()) {
//                    item.setChecked(false);
//                    scrolling = false;
//                } else {
//                    item.setChecked(true);
//                    scrolling = true;
//                }
//                customRecycler.reload();
//                break;
//            case R.id.taskBar_collection_delete:
//                if (database.videoMap.isEmpty()) {
//                    Toast.makeText(this, "Keine " + plural, Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                if (delete) {
//                    videos_confirmDelete.setVisibility(View.GONE);
//                } else {
//                    videos_confirmDelete.setVisibility(View.VISIBLE);
//                }
//                customRecycler.reload();
//                delete = !delete;
//                break;
//
//            case R.id.taskBar_collection_sortByName:
//                sort_type = VideoActivity.SORT_TYPE.NAME;
//                item.setChecked(true);
//                reLoadRecycler();
//                break;
//            case R.id.taskBar_collection_sortByViews:
//                sort_type = VideoActivity.SORT_TYPE.VIEWS;
//                item.setChecked(true);
//                reLoadRecycler();
//                break;
//            case R.id.taskBar_collection_sortByRating:
//                sort_type = VideoActivity.SORT_TYPE.RATING;
//                item.setChecked(true);
//                reLoadRecycler();
//                break;
//            case R.id.taskBar_collection_sortByLatest:
//                sort_type = VideoActivity.SORT_TYPE.LATEST;
//                item.setChecked(true);
//                reLoadRecycler();
//                break;
//            case R.id.taskBar_collection_sortReverse:
//                boolean checked = !item.isChecked();
//                item.setChecked(checked);
//                reverse = checked;
//                reLoadRecycler();
//                break;
//
//            case R.id.taskBar_collection_filterByName:
//                if (item.isChecked()) {
//                    filterTypeSet.remove(VideoActivity.FILTER_TYPE.NAME);
//                    item.setChecked(false);
//                } else {
//                    filterTypeSet.add(VideoActivity.FILTER_TYPE.NAME);
//                    item.setChecked(true);
//                }
//                commitSearch();
//                setSearchHint();
//                break;
//            case R.id.taskBar_collection_filterByDarsteller:
//                if (item.isChecked()) {
//                    filterTypeSet.remove(VideoActivity.FILTER_TYPE.ACTOR);
//                    item.setChecked(false);
//                } else {
//                    filterTypeSet.add(VideoActivity.FILTER_TYPE.ACTOR);
//                    item.setChecked(true);
//                }
//                commitSearch();
//                setSearchHint();
//                break;
//            case R.id.taskBar_collection_filterByGenre:
//                if (item.isChecked()) {
//                    filterTypeSet.remove(VideoActivity.FILTER_TYPE.GENRE);
//                    item.setChecked(false);
//                } else {
//                    filterTypeSet.add(VideoActivity.FILTER_TYPE.GENRE);
//                    item.setChecked(true);
//                }
//                commitSearch();
//                setSearchHint();
//                break;
//            case R.id.taskBar_collection_filterByStudio:
//                if (item.isChecked()) {
//                    filterTypeSet.remove(VideoActivity.FILTER_TYPE.STUDIO);
//                    item.setChecked(false);
//                } else {
//                    filterTypeSet.add(VideoActivity.FILTER_TYPE.STUDIO);
//                    item.setChecked(true);
//                }
//                commitSearch();
//                setSearchHint();
//                break;
//            case R.id.taskBar_collection_filterByRating:
//                Utility.showRangeSelectDialog(this, searchView);
//                break;
//
//            case R.id.taskBar_collection_modeAll:
//                mode = VideoActivity.MODE.ALL;
//                item.setChecked(true);
//                commitSearch();
//                break;
//            case R.id.taskBar_collection_modeSeen:
//                mode = VideoActivity.MODE.SEEN;
//                item.setChecked(true);
//                commitSearch();
//                break;
//            case R.id.taskBar_collection_modeLater:
//                mode = VideoActivity.MODE.LATER;
//                item.setChecked(true);
//                commitSearch();
//                break;
//            case R.id.taskBar_collection_modeUpcoming:
//                mode = VideoActivity.MODE.UPCOMING;
//                item.setChecked(true);
//                commitSearch();
//                break;

            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
    //  <------------------------- ToolBar -------------------------
}
