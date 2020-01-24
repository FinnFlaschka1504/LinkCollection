package com.maxMustermannGeheim.linkcollection.Daten.Owe;

import androidx.annotation.NonNull;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Owe extends ParentClass {
    public enum OWN_OR_OTHER {
        OWN("Eigene"), OTHER("Fremde");

        String name;

        OWN_OR_OTHER() {
        }

        OWN_OR_OTHER(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public OWN_OR_OTHER setName(String name) {
            this.name = name;
            return this;
        }
    }

    private String description;
    private OWN_OR_OTHER ownOrOther;
    private List<Item> itemList = new ArrayList<>();
    private Date date;

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

    public OWN_OR_OTHER getOwnOrOther() {
        return ownOrOther;
    }

    public Owe setOwnOrOther(OWN_OR_OTHER ownOrOther) {
        this.ownOrOther = ownOrOther;
        return this;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public Owe setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    public boolean isOpen() {
        return itemList.stream().anyMatch(Item::isOpen);
    }

    public Date getDate() {
        return date;
    }

    public Owe setDate(Date date) {
        this.date = date;
        return this;
    }

    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(description)) description = AESCrypt.encrypt(key, description);
            if (!itemList.isEmpty()) itemList.forEach(item -> item.encrypt(key));
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean decrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
            if (Utility.stringExists(description)) description = AESCrypt.decrypt(key, description);
            if (!itemList.isEmpty()) itemList.forEach(item -> item.decrypt(key));
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------


    public static class Item extends ParentClass{
        private double amount;
        private String personId;
        private boolean open = true;

        public Item() {
        }

        public Item(String personId, double amount) {
            this.amount = amount;
            this.personId = personId;
        }

        public double getAmount() {
            return amount;
        }

        public Item setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public String getPersonId() {
            return personId;
        }

        public Item setPersonId(String personId) {
            this.personId = personId;
            return this;
        }

        public boolean isOpen() {
            return open;
        }

        public Item setOpen(boolean open) {
            this.open = open;
            return this;
        }
    }

//    public Owe cloneOwe() {
//        Owe owe = new Owe();
//        owe.name = this.name;
//        owe.uuid = this.uuid;
//        owe.description = this.description;
//        owe.itemList = new ArrayList<>(this.itemList);
//        owe.date = this.date;
//        owe.ownOrOther = this.ownOrOther;
//        return owe;
//    }

    @Override
    public Owe clone() {
        return (Owe) super.clone();
    }
}
