package com.nisith.smartchat.Model;

public class GroupProfile {
    private String groupName;
    private String aboutGroup;
    private int totalGroupFriends;
    private String groupProfileImage;

    public GroupProfile(){

    }

    public GroupProfile(String groupName, String aboutGroup, int totalGroupFriends, String groupProfileImage) {
        this.groupName = groupName;
        this.aboutGroup = aboutGroup;
        this.totalGroupFriends = totalGroupFriends;
        this.groupProfileImage = groupProfileImage;
    }


    public String getGroupName() {
        return groupName;
    }

    public String getAboutGroup() {
        return aboutGroup;
    }

    public int getTotalGroupFriends() {
        return totalGroupFriends;
    }

    public String getGroupProfileImage() {
        return groupProfileImage;
    }
}
