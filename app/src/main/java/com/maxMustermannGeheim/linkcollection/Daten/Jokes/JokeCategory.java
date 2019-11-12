package com.maxMustermannGeheim.linkcollection.Daten.Jokes;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class JokeCategory extends ParentClass {

    public JokeCategory() {
    }

    public JokeCategory(String name) {
        uuid = "jokeCategory_" + UUID.randomUUID().toString();
        this.name = name;
    }
}
