package com.maxMustermannGeheim.linkcollection.Daten.Owe;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class Person extends ParentClass {

    public Person() {
    }

    public Person(String name) {
        uuid = "person_" + UUID.randomUUID().toString();
        this.name = name;
    }
}
