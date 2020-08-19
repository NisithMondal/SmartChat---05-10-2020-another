package com.nisith.smartchat.Model;

public class UserDetailInfo {


    private String aboutMe;
    private UserStatus userStatus;

    public UserDetailInfo(){

    }

    public UserDetailInfo(String aboutMe, UserStatus userStatus) {
        this.aboutMe = aboutMe;
        this.userStatus = userStatus;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public String getAboutMe() {
        return aboutMe;
    }
}
