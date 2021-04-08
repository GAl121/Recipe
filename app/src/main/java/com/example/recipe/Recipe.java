package com.example.recipe;

import android.media.Image;

import java.util.ArrayList;

public class Recipe
{
    public String id;
    public String cat_name;
    public String name;
    public ArrayList<String> ingredients = new ArrayList<String>();
    public ArrayList<String> instructions = new ArrayList<String>();
    public String icon;
    public ArrayList<String> pics  = new ArrayList<String>();
    public String userId;
    public String userName;


    public Recipe (String id,String cat_name, String name,ArrayList<String> ingredients, ArrayList<String> instructions,String icon, ArrayList<String> pics, String userId , String userName)
    {
        this.id = id;
        this.cat_name = cat_name;
        this.name = name;
        this.ingredients.addAll(ingredients);
        this.instructions.addAll(instructions);
        this.icon = icon;
        this.pics.addAll(pics);
        this.userId = userId;
        this.userName = userName;
    }

    public Recipe (Recipe recipe)
    {
        this.id = recipe.id;
        this.cat_name = recipe.cat_name;
        this.name = recipe.name;
        this.ingredients.addAll(recipe.ingredients);
        this.instructions.addAll(recipe.instructions);
        this.icon = recipe.icon;
        this.pics.addAll(recipe.pics);
        this.userId = recipe.userId;
        this.userName = recipe.userName;
    }
    public Recipe()
    {

    }
}
