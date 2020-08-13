package com.nisith.smartchat.Model;

public class FriendsGroup {
    private String groupName;
    private String aboutGroup;
    private String groupProfileImage;

    public FriendsGroup(){

    }

    public FriendsGroup(String groupName, String aboutGroup, String groupProfileImage) {
        this.groupName = groupName;
        this.aboutGroup = aboutGroup;
        this.groupProfileImage = groupProfileImage;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getAboutGroup() {
        return aboutGroup;
    }

    public String getGroupProfileImage() {
        return groupProfileImage;
    }
}
