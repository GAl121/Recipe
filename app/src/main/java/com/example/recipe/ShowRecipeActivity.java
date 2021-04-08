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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ShowRecipeActivity extends AppCompatActivity {

    //for image switcher
    private ImageSwitcher imageSwitcher;
    public ArrayList<Uri> uriImages = new ArrayList<Uri>();
    private ArrayList<Bitmap> images = new ArrayList<>();
    private ImageButton btPrevious, btNext;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024;
    private int currentIndex = 0;
    private int location = 0;

    private Recipe thisRecipe;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    private StorageReference storageReference = mStorageRef.getReference();
    private ProgressDialog loading;

    private ArrayList<File> tmpFile = new ArrayList<>();


    //-----------------------------------------
    // update recipe ingredients
    //-----------------------------------------
    private void updateInstructions() {
        String instructions = "";
        LinearLayout instructionLayout = findViewById(R.id.instructions_layout);
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.activity_row, null);

        int i = 1;
        for (String inst : thisRecipe.instructions)
            instructions = instructions + i++ + ". " + inst + "\n";

        TextView textOut = addView.findViewById(R.id.textout);
        Button buttonRemove = addView.findViewById(R.id.remove);
        buttonRemove.setVisibility(View.GONE);
        textOut.setText(instructions);
        instructionLayout.addView(addView);
    }

    //-----------------------------------------
    // update recipe ingredients
    //-----------------------------------------
    private void updateIngredients() {
        String ingredients = "";
        LinearLayout ingredientLayout = findViewById(R.id.ingredients_layout);
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.activity_row, null);

        int i = 1;
        for (String ing : thisRecipe.ingredients)
            ingredients = ingredients + i++ + ". " + ing + "\n";


        TextView textOut = addView.findViewById(R.id.textout);
        Button buttonRemove = addView.findViewById(R.id.remove);
        buttonRemove.setVisibility(View.GONE);
        textOut.setText(ingredients);
        ingredientLayout.addView(addView);
    }

    //-----------------------------------------
    // update Recipe name
    //-----------------------------------------
    private void updateRecipeName() {
        TextView recName = findViewById(R.id.showRecipe_recipe_name);
        recName.setText(thisRecipe.name);
    }

    //-----------------------------------------
    // update writer name
    //-----------------------------------------
    private void updateWriterName() {
        TextView byName = findViewById(R.id.By_writer);
        byName.setText("By: " + thisRecipe.userName);
    }

    //-----------------------------------------
    // create loading progress dialog
    //-----------------------------------------
    private void createProgressDialog() {
        loading = new ProgressDialog(ShowRecipeActivity.this);
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
    // load and set recipes icons
    //----------------------------------------

    private void loadImages() {
        String name = "";

        int i = 0;
        location = 0;
        for (String path : thisRecipe.pics) {
            name = "tmp" + i;
            try {
                tmpFile.add(File.createTempFile(name, "jpg"));
                StorageReference islandRef = storageReference.child(path);
                islandRef.getFile(tmpFile.get(i++)).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        uriImages.add(Uri.fromFile(tmpFile.get(location++)));
                        if (uriImages.size() == thisRecipe.pics.size()) {
                            setImageSwitcher();
                            endProgressDialog();
                            updateRecipeName();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //----------------------------------------
    // load and set correct recipe data
    //----------------------------------------
    private void loadRecipe() {

        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        String category = (String) b.get("category");
        String id = (String) b.get("recipeId");

        DatabaseReference myRef = database.getReference("categories").child(category).child("recipe_" + id);
        myRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe findRecipe;
                findRecipe = snapshot.getValue(Recipe.class);
                thisRecipe = new Recipe(findRecipe);
                loadImages();
                updateIngredients();
                updateInstructions();
                updateWriterName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //--------------------------------------------------------
    // set images Switcher functions
    //--------------------------------------------------------
    private void setImageSwitcher() {
        //image switcher
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.FILL_PARENT));
                return imageView;
            }
        });

        imageSwitcher.setImageURI(uriImages.get(0));

        btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSwitcher.setInAnimation(ShowRecipeActivity.this, R.anim.from_right);
                imageSwitcher.setOutAnimation(ShowRecipeActivity.this, R.anim.to_left);
                --currentIndex;
                if (currentIndex < 0)
                    currentIndex = uriImages.size() - 1;
                imageSwitcher.setImageURI(uriImages.get(currentIndex));
            }
        });
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSwitcher.setInAnimation(ShowRecipeActivity.this, R.anim.from_left);
                imageSwitcher.setOutAnimation(ShowRecipeActivity.this, R.anim.to_right);
                currentIndex++;
                if (currentIndex == uriImages.size())
                    currentIndex = 0;
                imageSwitcher.setImageURI(uriImages.get(currentIndex));
            }
        });

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
        Intent intentMyRecipes = new Intent(ShowRecipeActivity.this, MyRecipesActivity.class);
        Intent intentAddNewRecipe = new Intent(ShowRecipeActivity.this, AddNewRecipeActivity.class);
        Intent intentCategories = new Intent(ShowRecipeActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(ShowRecipeActivity.this, LoginActivity.class);
        Intent intentSignUp = new Intent(ShowRecipeActivity.this, SignUpActivity.class);
        Intent intentLogout = new Intent(ShowRecipeActivity.this, LogoutActivity.class);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_recipe);

        createProgressDialog();
        imageSwitcher = findViewById(R.id.show_recipes_image_switcher);
        btNext = findViewById(R.id.showRecipe_bt_next);
        btPrevious = findViewById(R.id.showRecipe_bt_previous);

        loadRecipe();

    }
}