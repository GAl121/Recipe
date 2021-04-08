package com.example.recipe;

import java.util.ArrayList;


public class User {
    public String u_id;
    public String u_name;
    public String u_pass;
    public String u_phone;
    public String u_email;
    private String u_modification = "-1";
    public ArrayList<String> u_recipe_id = new ArrayList<String>(); ;

    public User(String id, String name, String pass, String phone, String email) {
        u_id = id;
        u_name = name;
        u_pass = pass;
        u_phone = phone;
        u_email = email;
    }

    public User(User user) {
        u_id = user.u_id;
        u_name = user.u_name;
        u_pass = user.u_pass;
        u_phone = user.u_phone;
        u_email = user.u_email;
        u_modification = user.getU_modification();
        if (!u_recipe_id.isEmpty())
            u_recipe_id.clear();
            u_recipe_id.addAll(user.u_recipe_id);

    }

    public User() {
    }

    public void setU_modification(String num) {
        this.u_modification = num;
    }

    public String getU_modification() {
        return u_modification;
    }
}
