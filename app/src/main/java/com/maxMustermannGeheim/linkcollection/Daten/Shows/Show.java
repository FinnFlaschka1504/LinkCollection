package com.maxMustermannGeheim.linkcollection.Daten.Shows;

import android.content.Context;
import android.util.Pair;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.finn.androidUtilities.CustomList;
import com.finn.androidUtilities.CustomUtility;
import com.google.firebase.database.Exclude;
import com.google.gson.Gson;
import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image_I;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.Utilities.Database;
import com.maxMustermannGeheim.linkcollection.Utilities.externalCode.ExternalCode;
import com.maxMustermannGeheim.linkcollection.Utilities.Helpers;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Show extends ParentClass implements ParentClass_Image_I {
    public static final String EMPTY_SEASON = "EMPTY_SEASON";

    public enum REQUEST_IMDB_ID_TYPE {
        TMDB, TRAKT, TVDB, SEASON, PREVIOUS;

        public static int indexOf(REQUEST_IMDB_ID_TYPE type) {
            return new com.finn.androidUtilities.CustomList<>(values()).indexOf(type);
        }

        public static REQUEST_IMDB_ID_TYPE byIndex(int index) {
            return values()[index];
        }
    }


    private List<String> genreIdList = new ArrayList<>();
    private int seasonsCount = -1;
    private int allEpisodesCount = -1;
    private int tmdbId = -1;
    private List<Season> seasonList = new ArrayList<>();
    private Date firstAirDate;
    private boolean inProduction;
    private Date nextEpisodeAir;
    private String latestEpisode;
    private String nextEpisode;
    private String status;
    private Date lastUpdated;
    private boolean notifyNew;
    private List<Episode> alreadyAiredList = new ArrayList<>();
    private String imagePath;
    private String language;
    private String imdbId;
    private REQUEST_IMDB_ID_TYPE requestImdbIdType = REQUEST_IMDB_ID_TYPE.TMDB;
    private int averageRuntime = -1;
    private List<ShowLabel> showLabels = new ArrayList<>();

    public Show(String name) {
        uuid = "show_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public Show() {
    }

    public List<String> getGenreIdList() {
        return genreIdList;
    }

    public Show setGenreIdList(List<String> genreIdList) {
        this.genreIdList = genreIdList;
        return this;
    }

    public int getSeasonsCount() {
        return seasonsCount;
    }

    public Show setSeasonsCount(int seasonsCount) {
        this.seasonsCount = seasonsCount;
        return this;
    }

    public int getAllEpisodesCount() {
        return allEpisodesCount;
    }

    public Show setAllEpisodesCount(int allEpisodesCount) {
        this.allEpisodesCount = allEpisodesCount;
        return this;
    }

    public int getTmdbId() {
        return tmdbId;
    }

    public Show setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
        return this;
    }

    public List<Season> getSeasonList() {
        if (!hasSpecials())
            seasonList.add(0, (Season) new Season(EMPTY_SEASON).setSeasonNumber(0).setUuid(""));
        return seasonList;
    }

    public Show setSeasonList(List<Season> seasonList) {
        this.seasonList = seasonList;
        return this;
    }

    public Date getFirstAirDate() {
        return firstAirDate;
    }

    public Show setFirstAirDate(Date firstAirDate) {
        this.firstAirDate = firstAirDate;
        return this;
    }

    public boolean isUpcoming() {
        if (firstAirDate != null)
            return new Date().before(firstAirDate);
        else
            return false;
    }

    public boolean isInProduction() {
        return inProduction;
    }

    public Show setInProduction(boolean inProduction) {
        this.inProduction = inProduction;
        return this;
    }

    public Date getNextEpisodeAir() {
        return nextEpisodeAir;
    }

    public Show setNextEpisodeAir(Date nextEpisodeAir) {
        this.nextEpisodeAir = nextEpisodeAir;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Show setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Show setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public boolean isNotifyNew() {
        return notifyNew;
    }

    public Show setNotifyNew(boolean notifyNew) {
        this.notifyNew = notifyNew;
        return this;
    }

    public List<Episode> getAlreadyAiredList() {
        return alreadyAiredList;
    }

    public Show setAlreadyAiredList(List<Episode> alreadyAiredList) {
        this.alreadyAiredList = alreadyAiredList;
        return this;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Show setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public boolean hasSpecials() {
        return !seasonList.isEmpty() && seasonList.get(0).getSeasonNumber() == 0;
    }

    public String getLanguage() {
        return language;
    }

    public Show setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getImdbId() {
        return imdbId;
    }

    public Show setImdbId(String imdbId) {
        this.imdbId = imdbId;
        return this;
    }

    public REQUEST_IMDB_ID_TYPE getRequestImdbIdType() {
        return requestImdbIdType;
    }

    public Show setRequestImdbIdType(REQUEST_IMDB_ID_TYPE requestImdbIdType) {
        this.requestImdbIdType = requestImdbIdType;
        return this;
    }

    public int getAverageRuntime() {
        return averageRuntime;
    }

    public boolean hasAverageRuntime() {
        return averageRuntime != -1 && averageRuntime != 0;
    }

    public void setAverageRuntime(int averageRuntime) {
        this.averageRuntime = averageRuntime;
    }

    public String getLatestEpisode() {
        return latestEpisode;
    }

    public Show setLatestEpisode(String latestEpisode) {
        this.latestEpisode = latestEpisode;
        return this;
    }

    public Show setNextEpisode(String nextEpisode) {
        this.nextEpisode = nextEpisode;
        return this;
    }

    public String getNextEpisode() {
        return nextEpisode;
    }

    public List<ShowLabel> getShowLabels() {
        return showLabels;
    }

    public Show setShowLabels(List<ShowLabel> showLabels) {
        this.showLabels = showLabels;
        return this;
    }

    /**  ------------------------- Convenience ------------------------->  */
    public boolean _isBeforeNextEpisodeAir() {
        if (nextEpisodeAir == null)
            return true;
        return nextEpisodeAir.after(new Date());
    }

    public boolean _isNextEpisodeReleasedAndNotWatched() {
        if (_isBeforeNextEpisodeAir())
            return false;
        if (nextEpisode == null)
            return false;
        String[] split = nextEpisode.split("(?<!\\\\)\\|");
        int seasonNumber = Integer.parseInt(split[0]);
        if (seasonList.size() <= seasonNumber)
            return false;
        Episode episode = seasonList.get(seasonNumber).episodeMap.get("E:" + split[1]);
        return episode == null || !episode.isWatched();
    }

    public boolean _isLatestEpisodeWatched() {
        if (latestEpisode == null)
            return false;
        String[] split = latestEpisode.split("(?<!\\\\)\\|");
        int seasonNumber = Integer.parseInt(split[0]); // 6
        if (seasonList.size() <= seasonNumber) // [0,1,2,3,4,5] 6
            return false;
        Episode episode = seasonList.get(seasonNumber).episodeMap.get("E:" + split[1]);
        return episode != null && episode.isWatched();
    }

    public boolean _isLatestSeasonCompleted() {
        Season lastSeason = seasonList.stream().filter(season -> season.airDate != null && !Utility.isUpcoming(season.airDate)).reduce((first, second) -> second).orElse(null);
        if (lastSeason == null)
            return false;
        Episode episode = lastSeason.episodeMap.get("E:" + lastSeason.episodesCount);
        return episode != null && episode.isWatched();
    }

    public Pair<Episode, Boolean> _getEpisode(int seasonNumber, int episodeNumber, boolean includeTemp) {
        Episode episode = null;
        if (seasonNumber < seasonList.size()) {
            Season season = seasonList.get(seasonNumber);
            episode = season.getEpisodeMap().get("E:" + episodeNumber);
        }
        if (episode != null || !includeTemp)
            return Pair.create(episode, true);
        Database database = Database.getInstance();
        Map<Integer, Map<String, Episode>> map = database.tempShowSeasonEpisodeMap.get(uuid);
        if (map != null) {
            Map<String, Episode> episodeMap = map.get(seasonNumber);
            if (episodeMap != null) {
                return Pair.create(episodeMap.get("E:" + episodeNumber), false);
            }
        }
        return Pair.create(null, false);
    }
    /**  <------------------------- Convenience -------------------------  */

    @Override
    public Show clone() {
        return (Show) super.clone();
    }

    @Deprecated
    public void copy(Show parentClass) {
        for (Field field : Show.class.getDeclaredFields()) {
            field.setAccessible(true);
        }
    }

    //  ------------------------- Encryption ------------------------->
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.encrypt(key, imagePath);
            if (!alreadyAiredList.isEmpty()) alreadyAiredList.forEach(episode -> episode.encrypt(key));
            if (!seasonList.isEmpty()) seasonList.forEach(season -> season.encrypt(key));
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            if (Utility.stringExists(imagePath)) imagePath = AESCrypt.decrypt(key, imagePath);
            if (!alreadyAiredList.isEmpty()) alreadyAiredList.forEach(episode -> episode.decrypt(key));
            if (!seasonList.isEmpty()) seasonList.forEach(season -> season.decrypt(key));
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------


    //  ----- Classes ----->
    public static class Season extends ParentClass_Tmdb {
        private int episodesCount;
        private Date airDate;
        private int seasonNumber;
        private Map<String, Episode> episodeMap = new HashMap<>();
        private String showId;
        private String imdbIdBuffer;
//        public int test;

        public Season(String name) {
            uuid = "season_" + UUID.randomUUID().toString();
            this.name = name;
        }

        public Season() {
        }

        public int getEpisodesCount() {
            return episodesCount;
        }

        public Season setEpisodesCount(int episodesCount) {
            this.episodesCount = episodesCount;
            return this;
        }

        @Override
        public Season setTmdbId(int tmdbId) {
            return (Season) super.setTmdbId(tmdbId);
        }

        public Date getAirDate() {
            return airDate;
        }

        public Season setAirDate(Date airDate) {
            this.airDate = airDate;
            return this;
        }

        public int getSeasonNumber() {
            return seasonNumber;
        }

        public Season setSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }

        public Map<String, Episode> getEpisodeMap() {
            return episodeMap;
        }

        public Season setEpisodeMap(Map<String, Episode> episodeMap) {
            this.episodeMap = episodeMap;
            return this;
        }

        public String getShowId() {
            return showId;
        }

        public Season setShowId(String showId) {
            this.showId = showId;
            return this;
        }

        public String getImdbIdBuffer() {
            return imdbIdBuffer;
        }

        public Season setImdbIdBuffer(String imdbIdBuffer) {
            this.imdbIdBuffer = imdbIdBuffer;
            return this;
        }

        public com.finn.androidUtilities.CustomList<String> _getImdbIdBuffer_List() {
            return new com.finn.androidUtilities.CustomList<>(new Gson().fromJson(imdbIdBuffer, String[].class));
        }

        public static com.finn.androidUtilities.CustomList<String> _getImdbIdBuffer_List(String imdbIdBuffer) {
            return new com.finn.androidUtilities.CustomList<>(new Gson().fromJson(imdbIdBuffer, String[].class));
        }


        //  ------------------------- Encryption ------------------------->
        @Override
        public boolean encrypt(String key) {
            try {
                if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
                if (!episodeMap.isEmpty())
                    episodeMap.values().forEach(episode -> episode.encrypt(key));
                return true;
            } catch (GeneralSecurityException e) {
                return false;
            }
        }

        @Override
        public boolean decrypt(String key) {
            try {
                if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
                if (!episodeMap.isEmpty())
                    episodeMap.values().forEach(episode -> episode.decrypt(key));
                return true;
            } catch (GeneralSecurityException e) {
                return false;
            }
        }
        //  <------------------------- Encryption -------------------------
    }

    public static class Episode extends ParentClass_Ratable {
        private int tmdbId;
        private Date airDate;
        private int episodeNumber;
        private boolean watched;
        private String stillPath;
        private List<Date> dateList = new ArrayList<>();
        private String showId;
        private int seasonNumber;
        private String imdbId;
        private int length = -1;
        private String ageRating;
        private List<String> showLabelIds = new ArrayList<>();

        public Episode(String name) {
            uuid = "episode_" + UUID.randomUUID().toString();
            this.name = name;
        }

        public Episode() {
        }

        public int getTmdbId() {
            return tmdbId;
        }

        public Episode setTmdbId(int tmdbId) {
            this.tmdbId = tmdbId;
            return this;
        }

        public Date getAirDate() {
            return airDate;
        }

        public Episode setAirDate(Date airDate) {
            this.airDate = airDate;
            return this;
        }

        public boolean isUpcomming() {
            if (airDate != null)
                return new Date().before(airDate);
            else
                return false;
        }

        public int getEpisodeNumber() {
            return episodeNumber;
        }

        public Episode setEpisodeNumber(int episodeNumber) {
            this.episodeNumber = episodeNumber;
            return this;
        }

        public boolean isWatched() {
            return watched;
        }

        public Episode setWatched(boolean watched) {
            this.watched = watched;
            return this;
        }

        public String getStillPath() { // ToDo: evl. zu Encryption hinzufügen
            return stillPath;
        }

        public Episode setStillPath(String stillPath) {
            this.stillPath = stillPath;
            return this;
        }

        public List<Date> getDateList() {
            return dateList;
        }

        public Episode setDateList(List<Date> dateList) {
            this.dateList = dateList;
            return this;
        }

        public String getShowId() {
            return showId;
        }

        public Episode setShowId(String showId) {
            this.showId = showId;
            return this;
        }

        public int getSeasonNumber() {
            return seasonNumber;
        }

        public Episode setSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
            return this;
        }

        public boolean addDate(Date date, boolean checkTime, Context context) {
            boolean isBefore = false;
            int hourOffset = Settings.getSingleSetting_int(context, Settings.SETTING_MORE_HOUR_OFFSET);
            if (checkTime) {
                Calendar limitTime = Calendar.getInstance();
                limitTime.set(Calendar.HOUR_OF_DAY, hourOffset);
                Calendar givenDate = Calendar.getInstance();
                givenDate.setTime(date);

                isBefore = givenDate.before(limitTime);

                givenDate = Calendar.getInstance();
                givenDate.setTime(date);
                givenDate.add(Calendar.HOUR, -hourOffset);
                date = givenDate.getTime();
            }

            if (!checkTime && date.equals(Utility.removeTime(new Date())))
                date = Utility.shiftTime(new Date(), Calendar.HOUR, -hourOffset);

            this.dateList.add(date);

            return isBefore;
        }

        public void removeDate(Date removeDate) {
            boolean hasRemoved = dateList.removeIf(date -> date.equals(removeDate));
            if (!hasRemoved)
                dateList.removeIf(date -> CustomUtility.removeTime(date).equals(CustomUtility.removeTime(removeDate)));
        }

        public String getImdbId() {
            return imdbId;
        }

        public Episode setImdbId(String imdbId) {
            this.imdbId = imdbId;
            return this;
        }

        public Runnable requestImdbId(Context context, Runnable onFinished, REQUEST_IMDB_ID_TYPE differentRequestType) {
            final Runnable[] destroy = {null};

            Database database = Database.getInstance();
            Show show = database.showMap.get(showId);
            Season season = show.getSeasonList().get(seasonNumber);
            REQUEST_IMDB_ID_TYPE requestImdbIdType = Utility.isNotNullOrElse(differentRequestType, show.requestImdbIdType);
            switch (requestImdbIdType) {
                case TMDB:
                    String requestUrl = "https://api.themoviedb.org/3/tv/" + show.getTmdbId() + "/season/" + seasonNumber + "/episode/" + episodeNumber + "/external_ids?api_key=09e015a2106437cbc33bf79eb512b32d";
                    RequestQueue requestQueue = Volley.newRequestQueue(context);

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl, null, response -> {
                        try {
                            if (!response.isNull("imdb_id"))
                                imdbId = response.getString("imdb_id");
                            Utility.runRunnable(onFinished);

                        } catch (JSONException e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }, error -> {
                        Toast.makeText(context, "Fehler", Toast.LENGTH_SHORT).show();
                    });

                    requestQueue.add(jsonObjectRequest);

                    break;
                case TRAKT:
                    Utility.getImdbIdFromTmdbId(context, tmdbId, "episode", s -> {
                        imdbId = s;
                        Utility.runRunnable(onFinished);
                    });
                    break;
                case TVDB:
                    Toast.makeText(context, "Work in Progress", Toast.LENGTH_SHORT).show();
                    break;
                case SEASON:
                    ExternalCode.CodeEntry codeEntrySeason = ExternalCode.getEntry(ExternalCode.ENTRY.GET_SEASON_IMDB_IDS);
                    new Helpers.WebViewHelper(context, "https://www.imdb.com/title/" + show.getImdbId() + "/episodes?season=" + seasonNumber)
                            .addRequest(codeEntrySeason.getString("getIdsApp"), s -> {
                                        com.finn.androidUtilities.CustomList<String> imdbIdList = Season._getImdbIdBuffer_List(s);
                                        season.setImdbIdBuffer(s);
                                        Map<String, Episode> episodeMap;
                                        Map<Integer, Map<String, Episode>> map = null;
                                        for (Map.Entry<String, Map<Integer, Map<String, Episode>>> entry : database.tempShowSeasonEpisodeMap.entrySet()) {
                                            if (entry.getKey().equals(show.getUuid()))
                                                map = entry.getValue();
                                        }
                                        if (map == null || (episodeMap = map.get(seasonNumber)) == null) {
                                            episodeMap = season.getEpisodeMap();
                                        }
                                        Map<String, Episode> finalEpisodeMap = episodeMap;
                                        imdbIdList.forEachCount((imdbId, count) -> {
                                            Episode episode = finalEpisodeMap.get("E:" + (count + 1));
                                            if (episode != null && !Utility.stringExists(episode.getImdbId()))
                                                episode.setImdbId(imdbId);
                                        });
                                        Utility.runRunnable(onFinished);
                                    })
                            .addOptional(webViewHelper -> destroy[0] = webViewHelper::destroy)
//                            .setDebug(true)
                            .go();
                    break;
                case PREVIOUS:
                    Episode previousEpisode = null;
                    if (episodeNumber > 1) {
                        previousEpisode = season.getEpisodeMap().get("E:" + (episodeNumber - 1));
                    } else if (seasonNumber > 1) {
                        Season previousSeason = show.getSeasonList().get(seasonNumber - 1);
                        previousEpisode = previousSeason.getEpisodeMap().get("E:" + previousSeason.episodesCount);
                    }
                    if (previousEpisode != null && Utility.isImdbId(previousEpisode.getImdbId())) {
                        ExternalCode.CodeEntry codeEntryPrevious = ExternalCode.getEntry(ExternalCode.ENTRY.GET_NEXT_EPISODE_IMDB_ID);
                        new Helpers.WebViewHelper(context, "https://www.imdb.com/title/" + previousEpisode.getImdbId())
                                .addRequest(codeEntryPrevious.getString("getIdApp"), s -> {
                                    Matcher matcher = Pattern.compile(Utility.imdbPattern).matcher(s);
                                    if (matcher.find()) {
                                        imdbId = matcher.group(0);
                                        Utility.runRunnable(onFinished);
                                    } else
                                        Utility.runRunnable(onFinished);
                                })
//                                .setDebug(true)
                                .addOptional(webViewHelper -> destroy[0] = webViewHelper::destroy)
                                .go();
                    } else
                        Utility.runRunnable(onFinished);

                    break;
            }
            return destroy[0];
        }

        public int getLength() {
            return length;
        }

        public Episode setLength(int length) {
            this.length = length;
            return this;
        }

        public boolean hasLength() {
            return length != -1;
        }

        public String getAgeRating() {
            return ageRating;
        }

        public Episode setAgeRating(String ageRating) {
            this.ageRating = ageRating;
            return this;
        }

        public List<String> getShowLabelIds() {
            return showLabelIds;
        }

        @Exclude
        public CustomList<ShowLabel> getShowLabels() {
            return Database.getInstance().showMap.get(showId).getShowLabels().stream().filter(showLabel -> showLabelIds.contains(showLabel.getUuid())).collect(Collectors.toCollection(CustomList::new));
        }

        public Episode setShowLabelIds(List<String> showLabelIds) {
            this.showLabelIds = showLabelIds;
            return this;
        }

        public boolean hasAgeRating() {
            return Utility.stringExists(ageRating);
        }

        public boolean hasAnyExtraDetails() {
            return hasLength() || hasAgeRating();
        }

        public Episode createRaw() {
            return clone().setWatched(false);
        }

        @Override
        public Episode clone() {
            return (Episode) super.clone();
        }


        /** ------------------------- Convenience -------------------------> */
        @Nullable
        public Episode _getPrevious_ifLoaded() {
            // ToDo: wenn nicht gefunden vielleicht auch in tempMap nachgucken
            //  & nextEpisode
            Database database = Database.getInstance();
            Show show = database.showMap.get(showId);
            Season season;
            if (episodeNumber == 1) {
                if (seasonNumber == 1)
                    return null;
                season = show.getSeasonList().get(seasonNumber - 1);
                return season.episodeMap.get("E:" + season.episodesCount);
            } else {
                season = show.getSeasonList().get(seasonNumber);
                return season.episodeMap.get("E:" + (episodeNumber - 1));
            }
        }

        @Exclude
        public int getAbsoluteEpisodeNumber(@Nullable Show show) {
            if (getSeasonNumber() == 0) {
                Season season = show.getSeasonList().get(0);
                return -season.getEpisodesCount() - 1 + getEpisodeNumber();
            }
            if (show == null)
                show = Database.getInstance().showMap.get(showId);
            return show.getSeasonList().subList(1, getSeasonNumber()).stream().mapToInt(Season::getEpisodesCount).sum() + getEpisodeNumber();
        }

        @Exclude
        public Optional<Integer> getAbsoluteEpisodeNumber_withoutSpecials(@Nullable Show show) {
            int episodeNumber = getAbsoluteEpisodeNumber(show);
            return episodeNumber < 0 ? Optional.empty() : Optional.of(episodeNumber);
        }
        /**  <------------------------- Convenience -------------------------  */

        //  ------------------------- Encryption ------------------------->
        @Override
        public boolean encrypt(String key) {
            try {
                if (Utility.stringExists(stillPath)) stillPath = AESCrypt.encrypt(key, stillPath);
                if (Utility.stringExists(imdbId)) imdbId = AESCrypt.encrypt(key, imdbId);
            } catch (GeneralSecurityException e) {
                return false;
            }
            return super.encrypt(key);
        }

        @Override
        public boolean decrypt(String key) {
            try {
                if (Utility.stringExists(stillPath)) stillPath = AESCrypt.decrypt(key, stillPath);
                if (Utility.stringExists(imdbId)) imdbId = AESCrypt.decrypt(key, imdbId);
            } catch (GeneralSecurityException e) {
                return false;
            }
            return super.decrypt(key);
        }
        //  <------------------------- Encryption -------------------------

    }
    //  <----- Classes -----

}
