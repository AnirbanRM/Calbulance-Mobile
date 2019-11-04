package com.arm.calbulance;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.security.acl.Permission;

import javax.xml.transform.Result;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomePage extends AppCompatActivity {

    BottomNavigationView nav_bar;
    FrameLayout body;
    public String EMAIL,NAME,CONTACT,ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage_activity);
        Intent i = getIntent();
        EMAIL = i.getStringExtra("EMAIL");
        NAME = i.getStringExtra("NAME");
        CONTACT = i.getStringExtra("CONTACT");
        ID = i.getStringExtra("ID");

        nav_bar = findViewById(R.id.bottomnav);
        body = findViewById(R.id.frame);
        nav_bar.setOnNavigationItemSelectedListener(nav_item_clicked);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame,new Ambulance()).commit();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

    BottomNavigationView.OnNavigationItemSelectedListener nav_item_clicked = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment f = new Fragment();
            Bundle email_bundle;
            switch(menuItem.getItemId()){
                case R.id.call_ambulance_nav_button:
                    f = new Ambulance();
                    break;

                case R.id.book_appointment_nav_button:
                    f = new Appointment();
                    break;

                case R.id.history_nav_button:
                    f = new History();
                    email_bundle = new Bundle();
                    email_bundle.putString("EMAIL",EMAIL);
                    f.setArguments(email_bundle);
                    break;

                case R.id.account_nav_button:
                    f = new Profile();
                    email_bundle = new Bundle();
                    email_bundle.putString("EMAIL",EMAIL);
                    email_bundle.putString("NAME",NAME);
                    email_bundle.putString("CONTACT",CONTACT);
                    email_bundle.putString("ID",ID);
                    f.setArguments(email_bundle);
                    break;
            }

            if(f!=null){
                getSupportFragmentManager().beginTransaction().replace(R.id.frame,f).commit();
                return true;
            }
            return false;
        }
    };
}


