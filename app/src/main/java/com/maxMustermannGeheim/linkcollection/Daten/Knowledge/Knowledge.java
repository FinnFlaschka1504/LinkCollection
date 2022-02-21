package com.maxMustermannGeheim.linkcollection.Daten.Knowledge;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Ratable;
import com.maxMustermannGeheim.linkcollection.Utilities.Utility;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Knowledge extends ParentClass_Ratable {

    private String content;
    private List<List<String>> sources = new ArrayList<>();
    private List<String> categoryIdList = new ArrayList<>();
    private Date lastChanged;
    private List<Item> itemList = new ArrayList<>(Arrays.asList(new Item()));

    public Knowledge() {
    }

    public Knowledge(String name) {
        uuid = "knowledge_" + UUID.randomUUID().toString();
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<List<String>> getSources() {
        return sources;
    }

    public void setSources(List<List<String>> sources) {
        this.sources = sources;
    }

    public List<String> getCategoryIdList() {
        return categoryIdList;
    }

    public void setCategoryIdList(List<String> categoryIdList) {
        this.categoryIdList = categoryIdList;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public Knowledge setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
        return this;
    }

    //  ------------------------- Encryption ------------------------->
    @Override
    public boolean encrypt(String key) {
        try {
            if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
            if (Utility.stringExists(content)) content = AESCrypt.encrypt(key, content);
            for (List<String> list : sources) {
                for (int i = 0; i < list.size(); i++) {
                    list.set(i, AESCrypt.encrypt(key, list.get(i)));
                }
            }
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
            if (Utility.stringExists(content)) content = AESCrypt.decrypt(key, content);
            for (List<String> list : sources) {
                for (int i = 0; i < list.size(); i++) {
                    list.set(i, AESCrypt.decrypt(key, list.get(i)));
                }
            }
            if (!itemList.isEmpty()) itemList.forEach(item -> item.decrypt(key));
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    //  <------------------------- Encryption -------------------------



    //  ------------------------- ItemList ------------------------->
    public List<Item> getItemList() {
        return itemList;
    }


    //       -------------------- ListToString -------------------->
    public String itemListToString() {
        return itemList.stream().map(item -> "• " + item.getName()).collect(Collectors.joining("\n\n"));
    }

    public String itemListToString_complete(boolean simple) {
        if (simple)
            return itemList.stream().map(this::subItemToString_simple).collect(Collectors.joining("\n"));
        else
            return itemList.stream().map(item -> subItemToString(item, 0)).collect(Collectors.joining("\n\n"));
    }

    private String subItemToString_simple(Item item) {
        String result = "";

        result += item.getName();

        if (item.hasChild_real())
            result += "\n" + item.getChildrenList().stream().map(this::subItemToString_simple).collect(Collectors.joining("\n"));

        return result;
    }
    private String subItemToString(Item item, int depth) {
        String result = "";

        result += Utility.SwitchExpression.setInput(depth % 4)
                .addCase(0, integer -> "• ")
                .addCase(1, integer -> " → ")
                .addCase(2, integer -> "  ► ")
                .addCase(3, integer -> "   ➤ ")
                .addCase(4, integer -> "    » ")
                .addCase(5, integer -> "     > ")
                .evaluate() +
                item.getName();

        if (item.hasChild_real())
            result += "\n" + item.getChildrenList().stream().map(subItem -> subItemToString(subItem, depth + 1)).collect(Collectors.joining("\n"));

        return result;
    }
    //       <-------------------- ListToString --------------------

    public Knowledge setItemList(List<Item> itemList) {
        this.itemList = itemList;
        return this;
    }

    public static Item newItem(List<Item> itemList, Item previousItem) {
        Item newItem = new Item();
        itemList.add(itemList.indexOf(previousItem) + 1, newItem);
        return newItem;
    }

    public static Item previousItem(List<Item> itemList, Item item) {
        int index = itemList.indexOf(item);
        if (index == 0)
            return null;
        else
            return itemList.get(index - 1);
    }

    public boolean hasItems() {
        return itemList.stream().anyMatch(item -> !item.getName().isEmpty());
    }

    public void clearItemList() {
        itemList.clear();
        itemList.add(new Item());
    }

    public static class Item extends ParentClass{
        private List<Item> childrenList = new ArrayList<>();
//        private Item parent;

        public Item() {
            uuid = "knowledgeItem_" + UUID.randomUUID().toString();
            name = "";
        }

        public Item(String name) {
            uuid = "knowledgeItem_" + UUID.randomUUID().toString();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Item setName(String name) {
            this.name = name;
            return this;
        }

//        public Item _getParent() {
//            return parent;
//        }
//
//        public Item _setParent(Item parent) {
//            this.parent = parent;
//            return this;
//        }

        public List<Item> getChildrenList() {
            return childrenList;
        }

        public Item setChildrenList(List<Item> childrenList) {
            this.childrenList = childrenList;
            return this;
        }

        public Item addChild(Item item) {
            if (item == null)
                item = new Item();
            else
                item.setName(item.getName().trim());

            childrenList.add(item);
            return item;
        }

        public boolean hasChild_real() {
            return childrenList.stream().anyMatch(item -> !item.getName().isEmpty());
        }

        public boolean hasChild() {
            return !childrenList.isEmpty();
        }

        //  ------------------------- Encryption ------------------------->
        @Override
        public boolean encrypt(String key) {
            try {
                if (Utility.stringExists(name)) name = AESCrypt.encrypt(key, name);
                if (!childrenList.isEmpty()) childrenList.forEach(item -> item.encrypt(key));
                return true;
            } catch (GeneralSecurityException e) {
                return false;
            }
        }

        @Override
        public boolean decrypt(String key) {
            try {
                if (Utility.stringExists(name)) name = AESCrypt.decrypt(key, name);
                if (!childrenList.isEmpty()) childrenList.forEach(item -> item.decrypt(key));
                return true;
            } catch (GeneralSecurityException e) {
                return false;
            }
        }
        //  <------------------------- Encryption -------------------------


        @Override
        public Item clone() {
            Item clone = (Item) super.clone();
            if (!childrenList.isEmpty())
                clone.setChildrenList(childrenList.stream().map(Item::clone).collect(Collectors.toList()));
            return clone;
        }
    }
    //  <------------------------- ItemList -------------------------

    @Override
    public Knowledge clone() {
        Knowledge clone = (Knowledge) super.clone();
        clone.setItemList(itemList.stream().map(Item::clone).collect(Collectors.toList()));
        return clone;
    }
}
