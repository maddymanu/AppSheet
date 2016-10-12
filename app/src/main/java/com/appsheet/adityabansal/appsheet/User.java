package com.appsheet.adityabansal.appsheet;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by adityabansal on 10/11/16.
 */
public class User {
    public String name;
    public String age;
    public String number;

    public static ArrayList<User> fromJson(JSONArray jsonObjects) {
        ArrayList<User> users = new ArrayList<User>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                users.add(new User(jsonObjects.getJSONObject(i).getString("name"),jsonObjects.getJSONObject(i).getString("age"),jsonObjects.getJSONObject(i).getString("number")) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("USER", users.get(0).age);
        return users;
    }

    public User(String name, String age, String num) {
        this.name = name;
        this.age = age;
        this.number = num;
    }
}