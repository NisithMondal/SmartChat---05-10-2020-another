package com.nisith.smartchat.Notification.Model;

public class Notification {
    private String title;
    private String body;
    public String click_action;
    private String image;
    public Notification(){

    }

    public Notification(String title, String body,String click_action, String image) {
        this.title = title;
        this.body = body;
        this.image = image;
        this.click_action = click_action;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImage() {
        return image;
    }

    public String getClick_action() {
        return click_action;
    }
}
