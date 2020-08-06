package com.nisith.smartchat.Model;

public class UserProfile {
    private String userName;
    private String userStatus;
    private String profileImage;



    public UserProfile(){
    }

    public UserProfile(String userName, String userStatus, String profileImage) {
        this.userName = userName;
        this.userStatus = userStatus;
        this.profileImage = profileImage;

    }

    public String getUserName() {
        return userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getUserStatus() {
        return userStatus;
    }
}
