package com.nisith.smartchat.Model;

public class UserNotification {
    private String title;
    private String body;
    private long timeStamp; //for shorting all the notification
    private String dateTime;
    private String imageUrl;
    private String senderUid;
    private String groupUid;

    public UserNotification(){ }

    public UserNotification(String title, String body, long timeStamp, String dateTime, String imageUrl, String senderUid, String groupUid) {
        this.title = title;
        this.body = body;
        this.timeStamp = -timeStamp;
        this.dateTime = dateTime;
        this.imageUrl = imageUrl;
        this.senderUid = senderUid;
        this.groupUid = groupUid;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getGroupUid() {
        return groupUid;
    }
}
