package com.nisith.smartchat.Model;

public class FriendRequest {
    private String requestType;
//    private String date;

    public FriendRequest(){

    }

    public FriendRequest(String requestType) {
        this.requestType = requestType;
//        this.date = date;
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
}
