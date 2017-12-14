package com.rwth.medit.meditapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import helper.ramotion.CircleMenuView;


public class HomeActivity extends AppCompatActivity {

    private String serverIP = "";
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //prefs.edit().putString("server_ip", "").apply();

        final CircleMenuView menu = findViewById(R.id.circle_menu);

        menu.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onMenuOpenAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationStart");
            }

            @Override
            public void onMenuOpenAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuOpenAnimationEnd");
            }

            @Override
            public void onMenuCloseAnimationStart(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationStart");
            }

            @Override
            public void onMenuCloseAnimationEnd(@NonNull CircleMenuView view) {
                Log.d("D", "onMenuCloseAnimationEnd");
            }

            @Override
            public void onButtonClickAnimationStart(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonClickAnimationStart| index: " + index);
            }

            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                Log.d("D", "onButtonClickAnimationEnd| index: " + index);

                serverIP = prefs.getString("server_ip", "");
                if(serverIP.equals("")){
                    Toast.makeText(HomeActivity.this, "Please enter Server IP in settings.", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent i = new Intent(HomeActivity.this, ECGActivity.class);
                HomeActivity.this.startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverIP = prefs.getString("server_ip", "");

        if(serverIP.equals("")){
            Toast.makeText(HomeActivity.this, "Please enter Server IP in settings.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
