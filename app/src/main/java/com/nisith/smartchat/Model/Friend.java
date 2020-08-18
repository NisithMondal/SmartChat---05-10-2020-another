package com.nisith.smartchat.Model;

public class Friend {

    private String friendsType; //Means single-friend or group-friend
    private long timeStamp;

    public Friend(){

    }

    public Friend(long timeStamp, String friendsType) {
        this.timeStamp = timeStamp;
        this.friendsType = friendsType;
    }

    public String getFriendsType() {
        return friendsType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}

