package com.example.elke.healthh;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Register extends AppCompatActivity implements View.OnClickListener{
    Button bRegister;
    EditText etName,etBirthday, etUsername, etPassword,etMail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("REGISTER");
        setContentView(R.layout.activity_register);
        etName = (EditText) findViewById(R.id.etName);
        etBirthday = (EditText) findViewById(R.id.etBirthday);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etMail=(EditText) findViewById(R.id.etMail);
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
                String email = etMail.getText().toString();

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
                }else if(password.length()==0)
                {
                    etPassword.requestFocus();
                    etPassword.setError("Please don't leave the password field empty.");
                }

                else{

                    Toast.makeText(getApplicationContext(), "Registration success",
                            Toast.LENGTH_SHORT).show();

                    BackgroundTask backgroundTask=new BackgroundTask();
                    backgroundTask.execute(name,username,password,birthdayText,email);
                    finish();
                    startActivity(new Intent(this,Login.class));
                    break;
                }
                break;
        }
    }
    class BackgroundTask extends AsyncTask<String,Void,String>
    {
        String add_info_url;
        @Override
        protected void onPreExecute() {
            add_info_url="http://healthdata.netau.net/dataupload.php";
            super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... args) {
            String name,username,password,birthdayText,email;
            name=args[0];
            username=args[1];
            password=args[2];
            birthdayText=args[3];
            email=args[4];

            try {
                URL url=new URL(add_info_url);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream =httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter =new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data_string = URLEncoder.encode("account", "UTF-8") + "=" + URLEncoder.encode( username, "UTF-8") + "&" +
                        URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8") + "&" +
                        URLEncoder.encode("first_name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8")+"&" +
                        URLEncoder.encode("birthday", "UTF-8") + "=" + URLEncoder.encode( birthdayText, "UTF-8")+"&" +
                        URLEncoder.encode(" email", "UTF-8") + "=" + URLEncoder.encode( email, "UTF-8");
                bufferedWriter.write(data_string);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream=httpURLConnection.getInputStream();
                inputStream.close();
                httpURLConnection.disconnect();
                return "Successful!!";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}