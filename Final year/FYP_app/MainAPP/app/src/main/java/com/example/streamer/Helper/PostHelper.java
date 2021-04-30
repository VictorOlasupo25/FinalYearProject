package com.example.streamer.Helper;

public class PostHelper {




    public static boolean isLiked(String value){

        return value.equals("true");
    }

    public static String setLiked(boolean liked){

        if(liked){return "true"; }
        else{return "false";}
    }

}
