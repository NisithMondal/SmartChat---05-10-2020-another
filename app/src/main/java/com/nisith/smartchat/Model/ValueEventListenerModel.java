package com.nisith.smartchat.Model;

import com.google.firebase.database.ValueEventListener;

public class ValueEventListenerModel {
    private String key;
    private ValueEventListener valueEventListener;

    public ValueEventListenerModel(String key, ValueEventListener valueEventListener) {
        this.key = key;
        this.valueEventListener = valueEventListener;
    }

    public String getKey() {
        return key;
    }

    public ValueEventListener getValueEventListener() {
        return valueEventListener;
    }
}
