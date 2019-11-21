package com.maxMustermannGeheim.linkcollection.Daten.Shows;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Utilitys.CustomList;
import com.maxMustermannGeheim.linkcollection.Utilitys.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Show extends ParentClass{
    private List<String> genreIdList = new ArrayList<>();
    private int seasonsCount = -1;
    private int allEpisodesCount = -1;
    private int tmdbId = -1;
    private List<Season> seasonList = new ArrayList<>();
    private Date firstAirDate;
    private boolean inProduction;
    private Date nextEpisodeAir;
    private String status;

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

    public boolean isUpcomming() {
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

//    public Show cloneShow() {
//        Show newShow = new Show();
//        newShow.setName(this.name);
//        newShow.setUuid(this.uuid);
//        newShow.setAllEpisodesCount(this.allEpisodesCount);
//        newShow.setGenreIdList(new ArrayList<>(this.genreIdList));
//        newShow.setSeasonsCount(this.seasonsCount);
//        newShow.setTmdbId(this.tmdbId);
//        newShow.setSeasonList(new ArrayList<>(this.seasonList));
//        newShow.setFirstAirDate(this.firstAirDate);
//        return newShow;
//    }


    @Override
    public Show clone() {
        return (Show) super.clone();
    }

    //  ----- Classes ----->
    public static class Season extends ParentClass{
        private int episodesCount;
        private int tmdbId;
        private Date airDate;
        private int seasonNumber;
        private Map<String,Episode> episodeMap = new HashMap<>();
        private String showId;

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

        public int getTmdbId() {
            return tmdbId;
        }

        public Season setTmdbId(int tmdbId) {
            this.tmdbId = tmdbId;
            return this;
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

        public Map<String,Episode> getEpisodeMap() {
            return episodeMap;
        }

        public Season setEpisodeMap(Map<String,Episode> episodeMap) {
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
    }

    public static class Episode extends ParentClass{
        private int tmdbId;
        private Date airDate;
        private int episodeNumber;
        private boolean watched;
        private Float rating = -1f;
        private List<Date> dateList = new ArrayList<>();
        private String showId;
        private int seasonNumber;

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

        public Float getRating() {
            return rating;
        }

        public Episode setRating(Float rating) {
            this.rating = rating;
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

        public boolean addDate(Date date, boolean checkTime) {
            boolean isBefore = false;
            if (checkTime) {
                Calendar limitTime = Calendar.getInstance();
                limitTime.set(Calendar.HOUR_OF_DAY, 6);
                Calendar givenDate = Calendar.getInstance();
                givenDate.setTime(date);

                isBefore = givenDate.before(limitTime);
                if (isBefore)
                    givenDate.add(Calendar.DATE, -1);
                date = givenDate.getTime();
            }
            this.dateList.add(date);
//            this.dateList.add(Utility.removeTime(date));

            return isBefore;
        }

        public void removeDate(Date removeDate_withTime) {
            Date removeDate = Utility.removeTime(removeDate_withTime);

            CustomList<Date> dateListCopy = new CustomList<>(dateList);
            dateListCopy.forEachCount((date, count) -> {
                if (!Utility.removeTime(date).equals(removeDate))
                    return false;
                dateList.remove(count);
                return true;
            });
        }

    }
    //  <----- Classes -----

}
