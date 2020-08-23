package com.nisith.smartchat.Model;

public class UnreadChatsModel {
    private String key; //key may be friend's key or group key

    public UnreadChatsModel(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }


    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object != null) {
            UnreadChatsModel model = (UnreadChatsModel) object;
            if (this.getKey().equals(model.getKey())){
                result = true;
            }
        }
        return result;
    }


}
