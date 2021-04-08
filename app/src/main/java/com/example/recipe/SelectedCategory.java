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
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;

public class SelectedCategory extends AppCompatActivity {

    private TextView categoryName;
    private TextView total_recipes;
    private String category;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    private final StorageReference storageReference = mStorageRef.getReference();
    private ArrayList<Recipe> recipes = new ArrayList<Recipe>();
    private ArrayList<Bitmap> icons = new ArrayList<Bitmap>();
    private ArrayList<String> usersNames = new ArrayList<String>();
    private GridView myGridView;
    private ProgressDialog loading;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024;
    private Context context;
    private int numOfRecipes;


    //--------------------------------------------
    // create grid and set on click listener
    //--------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createGrid()
    {
        myGridView = findViewById(R.id.recipes_gridview);
        RecipesGrid gridAdapter = new RecipesGrid(context, recipes,usersNames, icons);
        myGridView.setAdapter(gridAdapter);

        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //create intent
                Intent intent = new Intent(getApplicationContext(), ShowRecipeActivity.class);
                intent.putExtra("recipeId", recipes.get(position).id );
                intent.putExtra("category" , recipes.get(position).cat_name);
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
                endProgressDialog();
                if (numOfRecipes == recipes.size() && numOfRecipes == icons.size() && numOfRecipes != 0)
                    createGrid();
            }
        });
    }

    //----------------------------------------
    // load and set correct user recipes data
    //----------------------------------------
    private void loadRecipes() {

            DatabaseReference myRef = database.getReference("categories").child(category);
            myRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    numOfRecipes = (int) snapshot.getChildrenCount();
                    if(numOfRecipes == 0)
                    {
                        endProgressDialog();
                        total_recipes.setVisibility(View.VISIBLE);
                        total_recipes.setText("This category doesn't have any recipes yet");
                    }
                    else
                    {
                    Recipe findRecipe;
                    for (DataSnapshot u : snapshot.getChildren()) {
                        findRecipe = u.getValue(Recipe.class);
                        recipes.add(new Recipe(findRecipe));
                        usersNames.add(findRecipe.userName);
                        loadIcons(findRecipe.icon);
                    }
                }
            }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                endProgressDialog();

                }});
        }

    //-----------------------------------------
    // create loading progress dialog
    //-----------------------------------------
    private void createProgressDialog() {
        loading = new ProgressDialog(SelectedCategory.this);
        loading.setMessage("Loading...");
        loading.show();
    }

    //-----------------------------------------
    // end loading progress dialog
    //-----------------------------------------
    private void endProgressDialog() {
        loading.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_category);
        createProgressDialog();
        context = this;
        categoryName= findViewById(R.id.show_category_name);

        Intent intent=getIntent();
        if (intent.getExtras() != null){
            category=intent.getStringExtra("name");
            categoryName.setText(category);

            total_recipes = findViewById(R.id.total_recipes);
            loadRecipes();
        }
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
        Intent intentMyRecipes = new Intent(SelectedCategory.this, MyRecipesActivity.class);
        Intent intentAddNewRecipe = new Intent(SelectedCategory.this, AddNewRecipeActivity.class);
        Intent intentCategories = new Intent(SelectedCategory.this, MainActivity.class);
        Intent intentLogin = new Intent(SelectedCategory.this, LoginActivity.class);
        Intent intentSignUp = new Intent(SelectedCategory.this, SignUpActivity.class);
        Intent intentLogout = new Intent(SelectedCategory.this, LogoutActivity.class);

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