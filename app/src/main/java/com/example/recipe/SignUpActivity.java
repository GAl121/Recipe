package com.example.recipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;


public class SignUpActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+!=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");
    private EditText username;
    private EditText password;
    private EditText ver_password;
    private EditText email;
    private EditText phone;
    private boolean flag;
    private ArrayList<User> users = new ArrayList<>();

    //----------------------------------------------------------
    // load users for email validation
    //----------------------------------------------------------

    private void loadUsers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

        myRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User findUser;
                for (DataSnapshot u : snapshot.getChildren()) {
                    findUser = u.getValue(User.class);
                    users.add(findUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //----------------------------------------------------------
    // start validations block
    //----------------------------------------------------------
    private boolean validateEmail() {
        flag = true;
        String emailInput = email.getText().toString().trim();
        if (emailInput.isEmpty()) {
            email.setError("Field can't be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please enter a valid email address");
            return false;
        } else {
            if (!users.isEmpty()) {
                for (User u : users) {
                    if (u.u_email.equals(emailInput)) {
                        email.setError("this email address all ready exist! choose another  one!");
                        return false;
                    }
                }
            }
        }
            return true;
    }


    private boolean validateUsername() {
        String usernameInput = username.getText().toString().trim();
        if (usernameInput.isEmpty()) {
            username.setError("Field can't be empty");
            return false;
        } else if (usernameInput.length() > 15) {
            username.setError("Username contains more than 15 characters");
            return false;
        } else {
            username.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = password.getText().toString().trim();
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

    private boolean validate_valPassword() {
        String passwordInput = password.getText().toString().trim();
        String password_var_Input = ver_password.getText().toString().trim();
        if (!(password_var_Input.equals(passwordInput))) {
            ver_password.setError("The password you entered does not match");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    private boolean isValidMobile() {
        String phoneInput = phone.getText().toString().trim();
        if (phoneInput.equals("")) {
            phone.setError("phone number cannot be empty! ");
            return false;
        }
        if (!Patterns.PHONE.matcher(phoneInput).matches()) return false;
        return true;
    }

    public void confirmInput(View v) {
        if (!validateUsername() || !validatePassword() || !validate_valPassword() || !isValidMobile() || !validateEmail()) {
            if (!flag)
                email.setError("this email address all ready exist! choose another  one!");
            return;
        }
        createUser();
        Toast.makeText(this, "You sign up successfully!", LENGTH_SHORT).show();
    }
    //----------------------------------------------------------
    // end validations block
    //----------------------------------------------------------

    //----------------------------------------------------------
    //create new user and save him on firebase
    //----------------------------------------------------------
    public void createUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("Users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            // check the last user id and create new user and save it on firebase
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int last_id = (int) dataSnapshot.getChildrenCount() + 1;
                User myUser = new User(String.valueOf(last_id), username.getText().toString(), password.getText().toString(), phone.getText().toString(), email.getText().toString());
                DatabaseReference users = database.getReference("Users").child("user_" + last_id);
                users.child("Users").child("user_" + last_id);
                try {
                    users.setValue(myUser);
                    Toast.makeText(getApplicationContext(), "Your sign up have save successfully", Toast.LENGTH_SHORT).show();
                    finish();

                } catch (Exception e) {
                    String str = e.getMessage();
                    System.out.println(str);
                }
                Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //------------------------------------------------
    // guest menu block start
    //------------------------------------------------
    // create app menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.guest_menu, menu);

        return true;
    }

    // set the app menu options
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intentLogin = new Intent(SignUpActivity.this, LoginActivity.class);
        Intent intentSignUp = new Intent(SignUpActivity.this, SignUpActivity.class);

        switch (item.getItemId()) {

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
    // guest menu block end
    //------------------------------------------------

    public boolean checkSharedPreference() {
        SharedPreferences sp = getSharedPreferences("Recipe", 0);

        return sp.getInt("User_id", -1) != -1;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        loadUsers();
        //set main activity properties;

        username = findViewById(R.id.signup_username);
        password = findViewById(R.id.signup_password);
        ver_password = findViewById(R.id.signup_password_verification);
        email = findViewById(R.id.signup_email);
        phone = findViewById(R.id.signup_phone);


        //----------------------------
        // on click listener for signup button call to confirmInput and create user
        //----------------------------

        findViewById(R.id.signup_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                confirmInput(v);
            }
        });


    }
}