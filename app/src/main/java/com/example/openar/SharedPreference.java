package com.example.openar;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private static String fileName="UserPhoneNo";

    public static String readSharedSetting(Context cont, String settingName, String defaultValue){
        SharedPreferences sharedPref=cont.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
    }
    public static void saveSharedSetting(Context cont, String settingName, String settingValue){
        SharedPreferences sharedPref=cont.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }
}
