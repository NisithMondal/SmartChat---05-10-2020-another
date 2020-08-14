package com.nisith.smartchat.Model;

public class Friend {

    private String friendsType; //Means single-friend or group-friend
    private String timeStamp;

    public Friend(){

    }

    public Friend(String timeStamp, String friendsType) {
        this.timeStamp = timeStamp;
        this.friendsType = friendsType;
    }

    public String getFriendsType() {
        return friendsType;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}

