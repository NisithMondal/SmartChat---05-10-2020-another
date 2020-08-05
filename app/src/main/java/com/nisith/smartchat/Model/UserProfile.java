package com.nisith.smartchat.Model;

public class UserProfile {
    private String userName;
    private String userStatus;
    private String profileImage;
    private String profileImageThumbnail;


    public UserProfile(){
    }

    public UserProfile(String userName, String userStatus, String profileImage, String profileImageThumbnail) {
        this.userName = userName;
        this.userStatus = userStatus;
        this.profileImage = profileImage;
        this.profileImageThumbnail = profileImageThumbnail;

    }

    public String getUserName() {
        return userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getProfileImageThumbnail() {
        return profileImageThumbnail;
    }

    public String getUserStatus() {
        return userStatus;
    }
}
