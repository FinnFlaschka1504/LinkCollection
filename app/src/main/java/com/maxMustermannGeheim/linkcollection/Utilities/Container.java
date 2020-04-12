package com.maxMustermannGeheim.linkcollection.Utilities;

import java.util.HashMap;
import java.util.Map;

public class Container<T> {
    private T payload;

    public Container(T payload) {
        this.payload = payload;
    }

    public Container() {
    }

    public T getPayload() {
        return payload;
    }

    public Container<T> setPayload(T payload) {
        this.payload = payload;
        return this;
    }
}
