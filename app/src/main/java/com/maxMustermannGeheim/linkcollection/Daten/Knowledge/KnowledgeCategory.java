package com.maxMustermannGeheim.linkcollection.Daten.Knowledge;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class KnowledgeCategory extends ParentClass {

    public KnowledgeCategory() {
    }

    public KnowledgeCategory(String name) {
        uuid = "wissensKategorie_" + UUID.randomUUID().toString();
        this.name = name;
    }
}
