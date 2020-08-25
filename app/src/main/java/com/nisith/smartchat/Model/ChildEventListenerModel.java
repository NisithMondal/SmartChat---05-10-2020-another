package com.nisith.smartchat.Model;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;

public class ChildEventListenerModel {
    private String key;
    private ChildEventListener childEventListener;

    public ChildEventListenerModel(String key, ChildEventListener childEventListener) {
        this.key = key;
        this.childEventListener = childEventListener;
    }

    public String getKey() {
        return key;
    }

    public ChildEventListener getChildEventListener() {
        return childEventListener;
    }
}
