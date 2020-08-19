package com.nisith.smartchat.Model;

public class UserStatus {

    private boolean online;// true if the user is open the app else false
    private long lastSeen;

    public UserStatus(){

    }

    public UserStatus(boolean online, long lastSeen) {
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public boolean isOnline() {
        return online;
    }

    public long getLastSeen() {
        return lastSeen;
    }
}
