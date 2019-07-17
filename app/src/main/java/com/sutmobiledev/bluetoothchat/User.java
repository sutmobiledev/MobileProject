package com.sutmobiledev.bluetoothchat;

public class User {
    public static int USER_ID = 0;
    public static String profileAddress = null;
    public  static String user_name = new String("Unknown");

    public static void setProfileAddress(String profileAddress) {
        User.profileAddress = profileAddress;
    }
    public User(){
        USER_ID = 0;
        profileAddress = null;
        user_name = new String("Unknown");

    }
    public  void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public static void setUserId(int userId) {
        USER_ID = userId;
    }

    public static int getUserId() {
        return USER_ID;
    }

    public static String getProfileAddress() {
        return profileAddress;
    }

    public  String getUser_name() {
        return user_name;
    }
}
