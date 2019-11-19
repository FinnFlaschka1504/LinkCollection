package com.maxMustermannGeheim.linkcollection.Daten;

import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Jokes.JokeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Shows.ShowGenre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;

public class ParentClass {
    protected String uuid;
    protected String name;

    public ParentClass() {
    }

    public String getUuid() {
        return uuid;
    }

    public ParentClass setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public ParentClass setName(String name) {
        this.name = name;
        return this;
    }

    public static ParentClass newCategoy(CategoriesActivity.CATEGORIES object_type, String name) {
        switch (object_type) {
            case DARSTELLER:
                return new Darsteller(name);
            case GENRE:
                return new Genre(name);
            case STUDIOS:
                return new Studio(name);
            case KNOWLEDGE_CATEGORIES:
                return new KnowledgeCategory(name);
            case JOKE_CATEGORIES:
                return new JokeCategory(name);
            case SHOW_GENRES:
                return new ShowGenre(name);
        }
        return null;
    }
}
