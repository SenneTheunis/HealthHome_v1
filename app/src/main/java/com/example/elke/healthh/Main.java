package com.example.elke.healthh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;

public class Main extends AppCompatActivity implements View.OnClickListener {

    Button bResults, bProfile, bConnect, bLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);

        bResults = (Button) findViewById(R.id.bResults);
        bProfile = (Button) findViewById(R.id.bProfile);
        bConnect = (Button) findViewById(R.id.bConnect);
        bLogout = (Button) findViewById(R.id.bLogout);
        bResults.setOnClickListener(this);
        bProfile.setOnClickListener(this);
        bConnect.setOnClickListener(this);
        bLogout.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bResults:
                startActivity(new Intent(this, Results.class));
                break;

            case R.id.bProfile:
                startActivity(new Intent(this, ShowProfile.class));
                break;

            case R.id.bConnect:
                startActivity(new Intent(this, upload.class));
                break;
            case R.id.bLogout:
                dialog();
                break;
        }
    }
    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setMessage("Confirm logout？");
        builder.setTitle("NOTIFICATION");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Main.this.finish();
                final Intent intent = new Intent(Main.this,Login.class);
                startActivity(intent) ;
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
