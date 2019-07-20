package com.sutmobiledev.bluetoothchat;

public class User {
    private static int USER_ID = 0;
    private static String profileAddress = null;
    private static String user_name = new String("Unknown");

    public static void setProfileAddress(String profileAddress) {
        User.profileAddress = profileAddress;
    }

    public static String getUser_name() {
        return user_name;
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

    public static void setUser_name(String user_name) {
        User.user_name = user_name;
    }
}
