package com.example.elke.healthh;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity implements View.OnClickListener{
    Button bRegister;
    EditText etName,etBirthday, etUsername, etPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("REGISTER");
        setContentView(R.layout.activity_register);
        etName = (EditText) findViewById(R.id.etName);
        etBirthday = (EditText) findViewById(R.id.etBirthday);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etUsername = (EditText) findViewById(R.id.etUsername);
        bRegister = (Button) findViewById(R.id.bRegister);

        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bRegister:
                String name = etName.getText().toString();
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String birthdayText = etBirthday.getText().toString();

                if(name.length()==0)
                {
                    etName.requestFocus();
                    etName.setError("Please don't leave the name field empty.");
                }
                else if(birthdayText.length()==0)
                {
                    etBirthday.requestFocus();
                    etBirthday.setError("Please don't leave the birthday field empty.");
                }
                else if(username.length()==0)
                {
                    etUsername.requestFocus();
                    etUsername.setError("Please don't leave the username field empty.");
                }else if(password.length()==0)
                {
                    etPassword.requestFocus();
                    etPassword.setError("Please don't leave the password field empty.");
                }
                else{

                    Toast.makeText(getApplicationContext(), "Registration success",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this,Login.class));
                    break;
                }
                break;
        }
    }

}