package com.example.elke.healthh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Login extends AppCompatActivity implements View.OnClickListener{

    Button bLogin;
    EditText etUsername, etPassword;
    TextView tvRegisterLink;
    String userName;

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
                    LoginAttemps loginattempts = new LoginAttemps();
                    loginattempts.execute(name, password);
                    break;
                }
                break;

            }
            case R.id.tvRegisterLink:
                startActivity(new Intent(this,Register.class));
                break;
        }
    }
    private class LoginAttemps extends AsyncTask<String, Void, String> {

        ProgressDialog pdLoading = new ProgressDialog(Login.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdLoading.setMessage("\tLoading...");
            pdLoading.setIndeterminate(false);
            pdLoading.setCancelable(true);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String login_url = "http://healthdata.netau.net/login.php";
            String login_name = params[0];
            String login_pass = params[1];
            try {
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                String data = URLEncoder.encode("login_name", "UTF-8")+"="+URLEncoder.encode(login_name,"UTF-8")+"&"+
                        URLEncoder.encode("login_pass","UTF-8")+"="+URLEncoder.encode(login_pass,"UTF-8");
                userName = URLEncoder.encode(login_name,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine())!=null)
                {
                    response.append(line);
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return response.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String result) {
            pdLoading.dismiss();


            if(result.equalsIgnoreCase("Success  "))
            {
                Toast.makeText(Login.this,"login "+result, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Login.this,Main.class);
                intent.putExtra("USERNAME",userName);
                startActivity(intent);
                Login.this.finish();

            }else if (result.equalsIgnoreCase("Failed  ")){

                Toast.makeText(Login.this,"Username or password incorrect, "+result, Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(Login.this, "OOPs! Something went wrong. Connection Problem.", Toast.LENGTH_LONG).show();

            }
        }
    }
}
