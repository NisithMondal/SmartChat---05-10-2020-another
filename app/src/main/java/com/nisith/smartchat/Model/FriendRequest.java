package com.nisith.smartchat.Model;

public class FriendRequest {
    private String requestType;
    //Note if i use 'isGroup' constant instead of 'group' or any other constant  is not get actual value.
    // If i write 'isGroup' instead of 'group' firebase give me wrong answer. I don't know why it happend.
    //But remember this thing...
    private boolean group;
    private String groupKey;// key may be friend key or group key
    private  String senderOrReceiverUid;
    private String date;

    public FriendRequest(){

    }

    public FriendRequest(String requestType, boolean isGroup, String groupKey, String senderOrReceiverUid, String date) {
        this.requestType = requestType;
        this.group = isGroup;
        this.groupKey = groupKey;
        this.senderOrReceiverUid = senderOrReceiverUid;
        this.date = date;
    }

    public String getRequestType() {
        return requestType;
    }

    public boolean isGroup() {
        return group;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public String getSenderOrReceiverUid() {
        return senderOrReceiverUid;
    }

    public String getDate() {
        return date;
    }


    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object != null) {
            FriendRequest obj2 = (FriendRequest) object;
            if (this.getGroupKey().equals(obj2.getGroupKey())){
                result = true;
            }
        }
       return result;
    }


}
