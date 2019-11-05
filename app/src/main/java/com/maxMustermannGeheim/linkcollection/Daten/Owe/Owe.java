package com.maxMustermannGeheim.linkcollection.Daten.Owe;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class Owe extends ParentClass {
    enum STATE {
        TO_ME, FROM_ME
    }

    private String description;
    private STATE state;
    private double amount;
    private String personId;

    public Owe() {
    }

    public Owe(String name) {
        uuid = "owe_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Owe setDescription(String description) {
        this.description = description;
        return this;
    }

    public STATE getState() {
        return state;
    }

    public Owe setState(STATE state) {
        this.state = state;
        return this;
    }

    public double getAmount() {
        return amount;
    }

    public Owe setAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public String getPersonId() {
        return personId;
    }

    public Owe setPersonId(String personId) {
        this.personId = personId;
        return this;
    }
}
