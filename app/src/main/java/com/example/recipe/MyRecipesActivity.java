package com.example.recipe;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MyRecipesActivity extends AppCompatActivity {

    private Context context;
    private User myUser;
    private ArrayList<Recipe> myRecipes = new ArrayList<Recipe>();
    private ArrayList<Bitmap> icons = new ArrayList<Bitmap>();
    private ArrayList<String> categories = new ArrayList<String>();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    private StorageReference storageReference = mStorageRef.getReference();
    private GridView myGridView;
    private int numOfRecipes;
    private ProgressDialog loading;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024;


    //-----------------------------------------
    // create loading progress dialog
    //-----------------------------------------
    private void createProgressDialog() {
        loading = new ProgressDialog(MyRecipesActivity.this);
        loading.setMessage("Loading...");
        loading.show();
    }

    //-----------------------------------------
    // end loading progress dialog
    //-----------------------------------------
    private void endProgressDialog() {
        loading.cancel();
    }

    //----------------------------------------
    // load and set correct user data
    //----------------------------------------
    private void loadUser() {
        // for get user id

        SharedPreferences sp = getSharedPreferences("Recipe", 0);
        int user_id = sp.getInt("User_id", -1);

        DatabaseReference myRef = database.getReference("Users");

        myRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User findUser;
                for (DataSnapshot u : snapshot.getChildren()) {
                    findUser = u.getValue(User.class);
                    if (findUser.u_id.equals(String.valueOf(user_id))) {
                        myUser = new User(findUser);
                        break;
                    }
                }
                loadRecipes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //--------------------------------------------
    // create grid and set on click listener
    //--------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createGrid()
    {
        myGridView = findViewById(R.id.recipes_gridview);
        RecipesGrid gridAdapter = new RecipesGrid(context, myRecipes,categories ,icons);
        myGridView.setAdapter(gridAdapter);

        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    //create intent
                    Intent intent = new Intent(getApplicationContext(), ShowRecipeActivity.class);
                    intent.putExtra("recipeId", myRecipes.get(position).id );
                    intent.putExtra("category" , myRecipes.get(position).cat_name);
                    startActivity(intent);
                }
            });
    }
    //----------------------------------------
    // load and set recipes icons
    //----------------------------------------

    private void loadIcons(String path) {

        StorageReference islandRef = storageReference.child(path);
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                icons.add(bm);
                if (numOfRecipes == myRecipes.size() && numOfRecipes == icons.size()) {
                    endProgressDialog();
                    //create grid
                    createGrid();
                }
            }
        });
    }

    //----------------------------------------
    // load and set correct user recipes data
    //----------------------------------------
    private void loadRecipes() {

        numOfRecipes = myUser.u_recipe_id.size();
        TextView text = findViewById(R.id.my_recipes_msg);
        String msg = text.getText().toString();
        if (numOfRecipes == 0) {
            endProgressDialog();
            text.setVisibility(View.VISIBLE);
            text.setText("You don't have any Recipes!");
        } else {
            String set_cat[], category;
            final String[] recipe_id = {""};

            for (String r : myUser.u_recipe_id) {
                set_cat = r.split("_");
                category = set_cat[0];
                recipe_id[0] = set_cat[2];
                DatabaseReference myRef = database.getReference("categories").child(category).child("recipe_" + recipe_id[0]);
                myRef.orderByKey().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Recipe findRecipe;
                        findRecipe = snapshot.getValue(Recipe.class);
                        if (findRecipe.userId.equals(myUser.u_id)) {
                            myRecipes.add(new Recipe(findRecipe));
                            categories.add(findRecipe.cat_name);
                            loadIcons(findRecipe.icon);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);
        context = this;
        createProgressDialog();
        loadUser();
    }

    //-----------------------------------------
    //check if the user is all ready login
    //-----------------------------------------
    public boolean checkSharedPreference() {
        SharedPreferences sp = getSharedPreferences("Recipe", 0);

        return sp.getInt("User_id", -1) != -1;

    }

    //-----------------------------------------
    // delete share Prefernce
    //-----------------------------------------
    private void deleteSheredPreference() {
        SharedPreferences sp = getSharedPreferences("Recipe", 0);
        sp.edit().clear().apply();

    }


    //------------------------------------------------
    // app menu block start
    //------------------------------------------------

    // create app menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (checkSharedPreference()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_menu, menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.guest_menu, menu);
        }
        return true;
    }

    // set the app menu options
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        final Context context = this;
        Intent intentMyRecipes = new Intent(MyRecipesActivity.this, MyRecipesActivity.class);
        Intent intentAddNewRecipe = new Intent(MyRecipesActivity.this, AddNewRecipeActivity.class);
        Intent intentCategories = new Intent(MyRecipesActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(MyRecipesActivity.this, LoginActivity.class);
        Intent intentSignUp = new Intent(MyRecipesActivity.this, SignUpActivity.class);
        Intent intentLogout = new Intent(MyRecipesActivity.this, LogoutActivity.class);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                AlertDialog alertDialog_exit = builder.create();
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
}