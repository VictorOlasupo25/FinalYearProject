package com.example.streamer.Helper;

public class MiscHelper {

    public boolean isEmailValid(String email_string)
    {
      if(email_string.contains("@")){
          return true;
      }else {
          return false;
      }
    }
}
