package com.nisith.smartchat.Model;

public class UnreadMessagesModel {
    private String key; //key may be friend's key or group key
    private int totalUnreadMessages;

    public UnreadMessagesModel(String key, int totalUnreadMessages) {
        this.key = key;
        this.totalUnreadMessages = totalUnreadMessages;
    }

    public String getKey() {
        return key;
    }

    public int getTotalUnreadMessages() {
        return totalUnreadMessages;
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object != null) {
            UnreadMessagesModel model = (UnreadMessagesModel) object;
            if (this.getKey().equals(model.getKey())){
                result = true;
            }
        }
        return result;
    }


}
