package com.example.recipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    final Context context = this;
    public GridView gridView;
    public String[] category;
    public int [] imageCategory={
            R.drawable.bread_category_image,
            R.drawable.cheese_category_image,
            R.drawable.salad_category_image,
            R.drawable.meat_category_image,
            R.drawable.chicken_category_image,
            R.drawable.fish_categoey_image,
            R.drawable.rice_category_image,
            R.drawable.pizza_category_image,
            R.drawable.pasta_category_image,
            R.drawable.soup_category_image,
            R.drawable.dessert_category_image,
            R.drawable.other_category_image};


    //-----------------------------------------
    //check if the user is all ready login
    //-----------------------------------------
    public boolean checkSharedPreference()
    {
        SharedPreferences sp = getSharedPreferences ("Recipe", 0) ;

        if( sp.getInt("User_id",-1) != -1)
            return true;
        return false;

    }

    //-----------------------------------------
    // delete share Prefernce
    //-----------------------------------------
    private void deleteSheredPreference()
    {
        SharedPreferences sp = getSharedPreferences ("Recipe", 0) ;
        sp.edit().clear().apply();

    }


    //------------------------------------------------
    // app menu block start
    //------------------------------------------------

    // create app menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(checkSharedPreference()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_menu, menu);
        }
        else
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.guest_menu,menu);
        }
        return true;
    }
    // set the app menu options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intentMyRecipes = new Intent(MainActivity.this, MyRecipesActivity.class);
        Intent intentAddNewRecipe = new Intent(MainActivity.this, AddNewRecipeActivity.class);
        Intent intentCategories= new Intent(MainActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
        Intent intentSignUp= new Intent(MainActivity.this, SignUpActivity.class);
        Intent intentLogout= new Intent(MainActivity.this, LogoutActivity.class);

        switch (item.getItemId()) {

            // for user menu------------
            // intent for my Recipes
            case R.id.user_menu_myRecipes:
                startActivity(intentMyRecipes);
                break;

            // intent for add new recipe
            case R.id.user_menu_addNewRecipe:
                startActivity(intentAddNewRecipe);
                break;

            // intent for my categories
            case R.id.user_menu_categories:
                startActivity(intentCategories);

                break;
            // intent for login page after do logout
            case R.id.user_menu_logout:
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Exit alert dialog");

                builder.setMessage("Are you sure you want to Exit?")
                        .setCancelable(false)
                        //yes button
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                deleteSheredPreference();
                                startActivity(intentLogout);

                            }
                        })
                        //cancel button
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();

                            }
                        });
                AlertDialog alertDialog_exit=builder.create();
                alertDialog_exit.show();
                break;

            // for guest menu---------
            // intent for signup
            case R.id.guest_menu_signup:
                startActivity(intentSignUp);

                break;
            // intent for login page after do logout
            case R.id.guest_menu_login:
                startActivity(intentLogin);
                break;

        }

        return super.onOptionsItemSelected(item);
    }
    //------------------------------------------------
    // app menu block end
    //------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create an ArrayAdapter that will contain all list items
        ArrayAdapter<String> stringAdapter;
        category=getResources().getStringArray(R.array.categories);
        /* Assign the name array to that adapter and also choose a simple layout for the list items */
        stringAdapter=new ArrayAdapter<String>(this,R.layout.single_item,category);


        gridView=findViewById(R.id.gridview);
        GridAdapter gridAdapter= new GridAdapter(this,category,imageCategory);
        gridView.setAdapter(gridAdapter);

        //on click listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(checkSharedPreference()) {
                    String selectedCategory = category[position];
                    //create intent
                    startActivity(new Intent(getApplicationContext(), SelectedCategory.class).putExtra("name", selectedCategory));
                }
                else
                {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("Login Alert");

                    builder.setMessage("You must Login before you go on ")
                            .setCancelable(false)
                            //yes button
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                                    startActivity(intent);

                                }
                            });
                    AlertDialog alertDialog_exit=builder.create();
                    alertDialog_exit.show();
                }
            }
        });
    }

}