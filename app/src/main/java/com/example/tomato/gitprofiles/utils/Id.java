package com.example.tomato.gitprofiles.utils;

public class Id {
    private static String id = null;

    public static String getId() {
        if (id == null){
            return id = "0";
        }else return id;
    }

    public static void setId(String id1) {
        id = id1;
    }
}
