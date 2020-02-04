package com.maxMustermannGeheim.linkcollection.Daten.Videos;

import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;

import java.util.UUID;

public class UrlParser extends ParentClass {
    private String code;
    private String exampleUrl;

    public UrlParser() {
    }

    public UrlParser(String name) {
        uuid = "urlParser_" + UUID.randomUUID().toString();
        this.name = name;
    }

    //  ------------------------- Getter & Setter ------------------------->
    public String getCode() {
        return code;
    }

    public UrlParser setCode(String code) {
        this.code = code;
        return this;
    }

    public String getExampleUrl() {
        return exampleUrl;
    }

    public UrlParser setExampleUrl(String exampleUrl) {
        this.exampleUrl = exampleUrl;
        return this;
    }
    //  <------------------------- Getter & Setter -------------------------


    @Override
    public UrlParser clone() {
        return (UrlParser) super.clone();
    }
}
