package com.example.recipe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;


public class AddNewRecipeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public Spinner category_spinner;
    private static final int GALLERY_REQUEST_CODE = 123;
    private ImageView iconView;
    private Bitmap icon;
    public Button btnPick;

    //first layout
    private EditText textIn;
    private LinearLayout container;

    //second layout
    private EditText textIn2;
    public Button buttonAdd2;
    private LinearLayout container2;

    // array list for  ingredient and instructions
    private final ArrayList<String> ingredients = new ArrayList<String>();
    private final ArrayList<String> instructions = new ArrayList<String>();

    // for images
    public Button pickImageBtn;
    public ImageButton btPrevious, btNext;
    public ImageSwitcher imageSwitcher;
    public ClipData clipData;
    public ArrayList<Uri> uri = new ArrayList<Uri>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();

    private EditText recipe_name;
    private String category = "";
    private int recipe_id;
    private User myUser;
    private int user_id;


    private int currentIndex = 0;

    //---------------------------------------------------
    // upload annd save new recipe
    //---------------------------------------------------

    private void upload() {

        // create progress bar  when upload the images and update the user
        final ProgressBar p = findViewById(R.id.progressbar);
        p.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


        // compress selected images  and save them on firebase storage
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ArrayList<String> images_ids = new ArrayList<String>();
        StorageReference imageRef;
        String image_id;
        int i = 0;
        byte[] b;

        for (Bitmap img : bitmaps) {
            image_id = category + "/id_" + String.valueOf(recipe_id) + "/userid_" + String.valueOf(user_id) + "/images/" + i + ".jpeg";
            images_ids.add(image_id);
            img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            imageRef = mStorageRef.child(image_id);

            b = stream.toByteArray();
            imageRef.putBytes(b);
            stream.reset();
            i++;

        }

        // set the icon image
        String icon_id = category + "/id_" + String.valueOf(recipe_id) + "/userid_" + String.valueOf(user_id) + "/icon.jpeg";
        icon.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        b = stream.toByteArray();
        StorageReference iconRef = mStorageRef.child(icon_id);
        iconRef.putBytes(b);

        String name = recipe_name.getText().toString();
        // set new  recipe and upload to firebase
        Recipe myRecipe = new Recipe(String.valueOf(recipe_id), category, name, ingredients, instructions, icon_id, images_ids, String.valueOf(user_id), myUser.u_name);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //set user recipes
        DatabaseReference userRef = database.getReference("Users");
        DatabaseReference users = database.getReference("Users").child("user_" + user_id);
        users.child("Users").child("user_" + user_id);
        myUser.u_recipe_id.add(category + "_id_" + recipe_id);
        users.setValue(myUser);

        //save recipe on firebase

        DatabaseReference ref = database.getReference("categories").child(category).child("recipe_" + recipe_id);
        ref.child("categories").child(category).child("recipe_" + recipe_id);

        ref.setValue(myRecipe).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                p.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(AddNewRecipeActivity.this, "New recipe uploaded ", Toast.LENGTH_SHORT).show();
                Intent intentMyRecipes = new Intent(AddNewRecipeActivity.this, MainActivity.class);
                startActivity(intentMyRecipes);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(AddNewRecipeActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }


    //----------------------------------------
    // checking that recipe have instructions
    //----------------------------------------
    private boolean instructionsValidation() {
        if (instructions.isEmpty()) {

            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Instruction Error!");
            alertDialogBuilder.setMessage("New recipe must  contain instructions!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //----------------------------------------
    // checking that recipe have ingredients
    //----------------------------------------
    private boolean ingredientsValidation() {
        if (ingredients.isEmpty()) {

            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Ingredient Error!");
            alertDialogBuilder.setMessage("New recipe must  contain ingredients!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //----------------------------------------
    // checking that recipe have ingredients
    //----------------------------------------
    private boolean categoryValidation() {
        if (category.equals("")) {

            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Category Error!");
            alertDialogBuilder.setMessage("You must select category for  your recipe!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //----------------------------------------
    // checking recipe id is set
    //----------------------------------------
    private boolean idValidation() {
        return recipe_id != -1;
    }

    //----------------------------------------
    // checking if icon image is set
    //----------------------------------------
    private boolean iconValidation() {
        if (iconView.getDrawable() == null) {
            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Icon Error!");
            alertDialogBuilder.setMessage("You must select icon for  your recipe!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //----------------------------------------
    // checking if images are set
    //----------------------------------------
    private boolean imagesValidation() {
        if (uri.isEmpty() && bitmaps.isEmpty()) {
            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Images Error!");
            alertDialogBuilder.setMessage("You must select at lest 1 image for  your recipe!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //----------------------------------------
    // checking if name are set
    //----------------------------------------
    private boolean recipeNameValidation() {
        if (recipe_name.getText().toString().equals("")) {
            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Name Error!");
            alertDialogBuilder.setMessage("You must insert name for your recipe").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    private boolean uploadValidation() {

        return recipeNameValidation() && ingredientsValidation() && instructionsValidation() && categoryValidation() && idValidation() && iconValidation() && imagesValidation();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();

        // set recipe id from his category - last id +1
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("categories").child(category);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipe_id = (int) dataSnapshot.getChildrenCount() + 1;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        category = "";
        recipe_id = -1;
    }


    //----------------------------------------
    // load and set correct user data
    //----------------------------------------
    private void loadUser() {
        // for get user id
        SharedPreferences sp = getSharedPreferences("Recipe", 0);
        user_id = sp.getInt("User_id", -1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_recipe);

        loadUser();
        recipe_name = findViewById(R.id.recipe_name);

        //for categories  spinner
        category_spinner = findViewById(R.id.category_spinner_id);
        //adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(AddNewRecipeActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.categories));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(spinnerAdapter);
        category_spinner.setOnItemSelectedListener(this);

        //String category = category_spinner;
        //  for ingredient layout

        textIn = findViewById(R.id.textinnew);
        Button buttonAdd = findViewById(R.id.addbtn);
        container = findViewById(R.id.container_ingredient);

        // for instructions layout
        textIn2 = findViewById(R.id.textin);
        buttonAdd2 = findViewById(R.id.addbtn_2);
        container2 = findViewById(R.id.container_instructions);


        // on click listener for add  ingredient
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (textIn.getText().toString().trim().isEmpty())
                    textIn.setError("You cant add empty ingredient!");

                else {
                    LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View addView = layoutInflater.inflate(R.layout.activity_row, null);
                    TextView textOut = addView.findViewById(R.id.textout);
                    String ingredient = textIn.getText().toString();
                    textOut.setText(ingredient);
                    ingredients.add(ingredient);
                    textIn.setText("");
                    Button buttonRemove = addView.findViewById(R.id.remove);
                    buttonRemove.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            ((LinearLayout) addView.getParent()).removeView(addView);
                            ingredients.remove(ingredient);
                        }
                    });

                    container.addView(addView);
                }
            }
        });

        // on click listener for add  instructions
        buttonAdd2.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View arg0) {
                if (textIn2.getText().toString().trim().isEmpty())
                    textIn2.setError("You cant add empty ingredient!");

                else {
                    LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View addView = layoutInflater.inflate(R.layout.activity_row, null);
                    TextView textOut = addView.findViewById(R.id.textout);
                    String instruction = textIn2.getText().toString();
                    textOut.setText(instruction);
                    instructions.add(instruction);
                    textIn2.setText("");
                    Button buttonRemove = addView.findViewById(R.id.remove);
                    buttonRemove.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            ((LinearLayout) addView.getParent()).removeView(addView);
                            instructions.remove(instruction);
                        }
                    });

                    container2.addView(addView);
                }
            }
        });

        iconView = findViewById(R.id.myIconView);
        btnPick = findViewById(R.id.btnPickImage);

        //------------------------------------------------------------
        // set on click listener for  choosing icon
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectIconImage();
            }
        });


        // on click  listener for upload  new  recipe button
        Button upload = findViewById(R.id.upload_recipe);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadValidation()) {
                    upload();

                }
            }
        });

        //------------------------------------------------------------------
        // set images
        pickImageBtn = findViewById(R.id.pickImageBtn);
        pickImageBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                if (!bitmaps.isEmpty())
                    bitmaps.clear();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
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

        imageSwitcher.setImageURI(uri.get(0));

        btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSwitcher.setInAnimation(AddNewRecipeActivity.this, R.anim.from_right);
                imageSwitcher.setOutAnimation(AddNewRecipeActivity.this, R.anim.to_left);
                --currentIndex;
                if (currentIndex < 0)
                    currentIndex = uri.size() - 1;
                imageSwitcher.setImageURI(uri.get(currentIndex));
            }
        });
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSwitcher.setInAnimation(AddNewRecipeActivity.this, R.anim.from_left);
                imageSwitcher.setOutAnimation(AddNewRecipeActivity.this, R.anim.to_right);
                currentIndex++;
                if (currentIndex == uri.size())
                    currentIndex = 0;
                imageSwitcher.setImageURI(uri.get(currentIndex));
            }
        });
    }

    //--------------------------------------------------------
    // create dialog for camera or gallery photo for icons
    //--------------------------------------------------------
    private void selectIconImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera, 0);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            //set for icon
            if (requestCode == 0 && resultCode == RESULT_OK) {
                icon = (Bitmap) data.getExtras().get("data");
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageBitmap(icon);
            } else if (requestCode == 1 && resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    icon = bitmap;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageBitmap(icon);
            }else {

                // set for images from gallery
                if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
                    imageSwitcher = findViewById(R.id.image_switcher);
                    imageSwitcher.setVisibility(View.VISIBLE);
                    btNext = findViewById(R.id.showRecipe_bt_next);
                    btNext.setVisibility(View.VISIBLE);
                    btPrevious = findViewById(R.id.showRecipe_bt_previous);
                    btPrevious.setVisibility(View.VISIBLE);
                    //create array of images
                    clipData = data.getClipData();
                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            uri.add(imageUri);
                            try {
                                InputStream is = getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(is);
                                //add the image to array
                                bitmaps.add(bitmap);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Uri imageUri = data.getData();
                        uri.add(imageUri);
                        try {
                            InputStream is = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            bitmaps.add(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    setImageSwitcher();
                }
            }
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
        Intent intentMyRecipes = new Intent(AddNewRecipeActivity.this, MyRecipesActivity.class);
        Intent intentAddNewRecipe = new Intent(AddNewRecipeActivity.this, AddNewRecipeActivity.class);
        Intent intentCategories = new Intent(AddNewRecipeActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(AddNewRecipeActivity.this, LoginActivity.class);
        Intent intentSignUp = new Intent(AddNewRecipeActivity.this, SignUpActivity.class);
        Intent intentLogout = new Intent(AddNewRecipeActivity.this, LogoutActivity.class);

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