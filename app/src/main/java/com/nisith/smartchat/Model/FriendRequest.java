package com.nisith.smartchat.Model;

import java.util.Objects;

public class FriendRequest {
    private String requestType;
    //friend key is optional
    private String friendKey;
//    private String date;

    public FriendRequest(){

    }

    public FriendRequest(String requestType) {
        this.requestType = requestType;
//        this.date = date;
    }

    public String getFriendKey() {
        return friendKey;
    }

    public void setFriendKey(String friendKey) {
        this.friendKey = friendKey;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

//    public String getDate() {
//        return date;
//    }

//    public void setDate(String date) {
//        this.date = date;
//    }


    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object != null) {
            FriendRequest obj2 = (FriendRequest) object;
            if (this.getFriendKey().equals(obj2.getFriendKey())){
                result = true;
            }
        }
       return result;
    }


}
