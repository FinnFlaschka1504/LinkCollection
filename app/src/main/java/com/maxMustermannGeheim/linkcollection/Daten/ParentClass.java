package com.maxMustermannGeheim.linkcollection.Daten;

import com.maxMustermannGeheim.linkcollection.Activities.Main.CategoriesActivity;
import com.maxMustermannGeheim.linkcollection.Daten.Knowledge.KnowledgeCategory;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Darsteller;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Genre;
import com.maxMustermannGeheim.linkcollection.Daten.Videos.Studio;

public class ParentClass {
//    public enum OBJECT_TYPE{
//        DARSTELLER, STUDIO, GENRE, KNOWLEDGE_CATEGORY
//    }
    protected String uuid;
    protected String name;

    public ParentClass() {
    }

//    public ParentClass(OBJECT_TYPE object_type, String name) {
//        switch (object_type) {
//            default:
////            case DARSTELLER:
////                uuid = "darsteller_" + UUID.randomUUID().toString();
////                break;
////            case STUDIO:
////                uuid = "studio_" + UUID.randomUUID().toString();
////                break;
////            case GENRE:
////                uuid = "genre_" + UUID.randomUUID().toString();
////                break;
//        }
//        this.name = name;
//    }

//    public ParentClass(OBJECT_TYPE object_type) {
//        switch (object_type) {
//            default:
//            case DARSTELLER:
//                uuid = "darsteller_" + UUID.randomUUID().toString();
//                break;
//            case STUDIO:
//                uuid = "studio_" + UUID.randomUUID().toString();
//                break;
//            case GENRE:
//                uuid = "genre_" + UUID.randomUUID().toString();
//                break;
//        }
//    }

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
        }
        return null;
    }
}
