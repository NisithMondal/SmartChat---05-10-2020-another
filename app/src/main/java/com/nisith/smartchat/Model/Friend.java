package com.nisith.smartchat.Model;

public class Friend {

    private String friendsType; //Means single-friend or group-friend
    private String searchName; //This will contains friend name. This is useful for searching.
    private long timeStamp;

    public Friend(){

    }

    public Friend(String friendsType, String searchName, long timeStamp ) {
        this.friendsType = friendsType;
        this.searchName = searchName;
        this.timeStamp = timeStamp;


    }

    public String getSearchName() {
        return searchName;
    }

    public String getFriendsType() {
        return friendsType;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}

