package com.example.elke.healthh;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends AppCompatActivity implements View.OnClickListener{

    Button bLogin;
    EditText etUsername, etPassword;
    TextView tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        tvRegisterLink = (TextView) findViewById(R.id.tvRegisterLink);


        bLogin.setOnClickListener(this);
        tvRegisterLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.bLogin: {
                String name = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if(name.length()==0)
                {
                    etUsername.requestFocus();
                    etUsername.setError("Please don't leave the name field empty.");
                }
                else if(password.length()==0)
                {
                    etPassword.requestFocus();
                    etPassword.setError("Please don't leave the password field empty.");
                }
                else{
                    startActivity(new Intent(this, Main.class));
                    break;
                }
                break;

            }
            case R.id.tvRegisterLink:
                startActivity(new Intent(this,Register.class));
                break;
        }
    }
}
