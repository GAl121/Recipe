package com.example.recipe;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private User user;
    private ArrayList<User> users = new ArrayList<User>();


    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");
    public EditText email;
    public EditText password;


    //------------------------------------------------
    // loadData func save data from  firebase to make the code synchronize
    //------------------------------------------------
    private void loadDate() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

        myRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User findUser;
                for (DataSnapshot u : snapshot.getChildren()) {
                    findUser = u.getValue(User.class);
                    users.add(new User(findUser));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //------------------------------------------------
    // start validations block
    //------------------------------------------------
    private boolean validateEmailAndPass() {

        String emailInput = email.getText().toString().trim();
        if (emailInput.isEmpty()) {
            email.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please enter a valid email address");
            return false;
        } else {
            for (User value : users) {
                if (value.u_email.equals(email.getText().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validatePassword() {
        String passwordInput = password.getText().toString();
        if (passwordInput.isEmpty()) {
            password.setError("Field can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            password.setError("Password must contain at least 4 characters and one special character");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }


    public void confirmInput(View v) {
        if (!validateEmailAndPass() | !validatePassword())
            Toast.makeText(getApplicationContext(), "illegals inputs ", Toast.LENGTH_SHORT).show();
        else {
            login(v);
        }
    }
    //------------------------------------------------
    // end validations block
    //------------------------------------------------

    //------------------------------------------------
    // login func verifications email and password and call createSheredPreference
    //------------------------------------------------
    private void login(View v) {

        for (User value : users) {
            if (value.u_email.equals(email.getText().toString()) && value.u_pass.equals(password.getText().toString())) {
                user = new User(value);
                break;
            }
        }
        if (user != null) {
            createSharedPreferences();
            Toast.makeText(getApplicationContext(), "You sign in successfully!", Toast.LENGTH_SHORT).show();

            // success login move to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else
            Toast.makeText(getApplicationContext(), "Wrong email and/or password", Toast.LENGTH_SHORT).show();

    }


    //------------------------------------------------
    // sanding sms code  for the user phone
    //------------------------------------------------

    private void sendSms(String phoneNo, String msg) {
        try {
            String fixPhone = phoneNo.substring(1);
            fixPhone = "+972" + fixPhone;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(fixPhone, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Verification code have been sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }

    }

    //------------------------------------------------
    // verification for exist phone number and  call to send sms
    //------------------------------------------------

    private void smsVerification(String phone) {

        Random rnd = new Random();
        int rnd_num = rnd.nextInt(1000000);

        for (User u : users) {
            if (u.u_phone.equals(phone)) {
                user = new User(u);
                // set the new value id firebase
                user.setU_modification(String.valueOf(rnd_num));

                String smsMsg = "Your verification code is: " + rnd_num;
                sendSms(u.u_phone, smsMsg);
                return;
            }
        }
        Toast.makeText(getApplicationContext(), "Phone number doesn't exist!", Toast.LENGTH_LONG).show();
    }

    //-----------------------------------------------
    // make  dialog box and ask for user phone number
    //-----------------------------------------------

    private void loginWithPhone() {
        Button button = (Button) findViewById(R.id.login_sms_btn);
        final Context context = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter Your phone number");
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                builder.setView(input);

                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String phone = input.getText().toString();
                        smsVerification(phone);
                        //--------------------------------------------
                        //request from user to verification for code
                        //----------------------------------------------

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Enter verification code");
                        final EditText input = new EditText(context);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        builder.setView(input);

                        builder.setPositiveButton("Verified", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String code = input.getText().toString();

                                //------- check modification code is initialize
                                if (user.getU_modification() != "-1") {
                                    // ----- success login with sms
                                    if (user.getU_modification().equals(code)) {
                                        user.setU_modification("-1");
                                        createSharedPreferences();
                                        Toast.makeText(getApplicationContext(), "You sign in successfully!", Toast.LENGTH_SHORT).show();
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        // success login move to main activity
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "your verification code is wrong!", Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                user.setU_modification("-1");
                            }
                        });
                        builder.show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    // create user shared preference and store his id and name
    public void createSharedPreferences() {
        // create user shared preference
        SharedPreferences sp = getSharedPreferences("Recipe", 0);
        SharedPreferences.Editor sedt = sp.edit();

        //set verification mode and update user firebase
        if (user != null) {
            user.setU_modification("-1");
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference myRef = database.getReference("Users").child("user_" + user.u_id);
            myRef.child("Users").child("user_" + user.u_id);
            myRef.setValue(user);

            sedt.putString("User_Name", this.user.u_name);
            sedt.putInt("User_id", Integer.parseInt(this.user.u_id));
            sedt.apply();
        }

    }

    //check if the user is all ready login

    public boolean checkSharedPreference() {
        SharedPreferences sp = getSharedPreferences("Recipe", 0);

        if (sp.getInt("User_id", -1) != -1)
            return true;
        return false;

    }

    //-----------------------------------------
    // delete shared Prefernce
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

        Intent intentMyRecipes = new Intent(LoginActivity.this, MyRecipesActivity.class);
        Intent intentAddNewRecipe = new Intent(LoginActivity.this, AddNewRecipeActivity.class);
        Intent intentCategories = new Intent(LoginActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(LoginActivity.this, LoginActivity.class);
        Intent intentSignUp = new Intent(LoginActivity.this, SignUpActivity.class);

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
                deleteSheredPreference();
                startActivity(intentLogin);
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
        setContentView(R.layout.activity_login);

        // load data
        loadDate();
        //set this activity variables
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmInput(v);
            }
        });
        loginWithPhone(); //set the login sms dialog

        // on click event to sign up
        findViewById(R.id.askToSignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}
