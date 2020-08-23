package com.nisith.smartchat.Model;

import android.os.SystemClock;

import com.google.firebase.database.ServerValue;

import java.util.Map;

public class FriendRequest {
    private String requestType;
    //Note if i use 'isGroup' constant instead of 'group' or any other constant  is not get actual value.
    // If i write 'isGroup' instead of 'group' firebase give me wrong answer. I don't know why it happend.
    //But remember this thing...
    private boolean group;
    private String groupKey;// key may be friend key or group key
    private  String senderOrReceiverUid;
    private long timeStamp;
    private boolean read;// User seen the received friend request or not...

    public FriendRequest(){

    }

    public FriendRequest(String requestType, boolean isGroup, String groupKey, String senderOrReceiverUid, long timeStamp, boolean read) {
        this.requestType = requestType;
        this.group = isGroup;
        this.groupKey = groupKey;
        this.senderOrReceiverUid = senderOrReceiverUid;
        this.timeStamp = timeStamp;
        this.read = read;

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

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public boolean equals(Object object) {
        //This method is called when we remove items from our friendRequestList.
        boolean result = false;
        if (object != null) {
            FriendRequest obj2 = (FriendRequest) object;
            if (this.getSenderOrReceiverUid().equals(obj2.getSenderOrReceiverUid())){
                if (this.getGroupKey().equals(obj2.getGroupKey())) {
                    result = true;
                }
            }
        }
        return result;
    }



}
