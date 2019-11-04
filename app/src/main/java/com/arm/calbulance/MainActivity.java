package com.arm.calbulance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static android.Manifest.permission.INTERNET;

public class MainActivity extends AppCompatActivity {
    TextView creaaccbut,id,password;
    Intent i;
    Button LoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        SharedPreferences sp=getSharedPreferences("CalbulanceLogin", MODE_PRIVATE);
        if(sp.contains("USERNAME"))login(sp.getString("USERNAME","0"),sp.getString("PASSWORD","0"));
        else{
        setContentView(R.layout.activity_main);
        creaaccbut = (TextView) findViewById(R.id.createaccbut);
        LoginButton = findViewById(R.id.login_login_button);
        id = findViewById(R.id.login_email_box);
        password = findViewById(R.id.login_password_box);

        LoginButton.setOnClickListener(login_clicked);
        creaaccbut.setOnClickListener(createacc_clicked);}
    }

    View.OnClickListener createacc_clicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            i = new Intent(getApplicationContext() ,createacc.class);
            startActivity(i);
        }
    };

    View.OnClickListener login_clicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            login(id.getText().toString(),password.getText().toString());
        }
    };

    private void login(final String a,final String b){
        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;

            @Override
            public void run() {

                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/user_login.php");
                    c =  (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.getPermission();
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    SharedPreferences sp=getSharedPreferences("CalbulanceLogin", MODE_PRIVATE);
                    String param = "ID="+ URLEncoder.encode(a) + "&PWD="+ URLEncoder.encode(b);
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) !=null)
                        response.append(line);

                    Log.e("TAG",response.toString());

                    if(!response.toString().equals("Failure")) {
                        if(!sp.contains("USERNAME")){
                        SharedPreferences.Editor Ed=sp.edit();
                        Ed.putString("USERNAME",id.getText().toString() );
                        Ed.putString("PASSWORD",password.getText().toString());
                        Ed.commit();}

                        JSONObject obj = new JSONObject(response.toString());
                        Intent i = new Intent(getApplicationContext(), HomePage.class);
                        i.putExtra("EMAIL",obj.getString("Email"));
                        i.putExtra("NAME",obj.getString("Name"));
                        i.putExtra("CONTACT",obj.getString("Contact"));
                        i.putExtra("ID",obj.getString("ID"));
                        startActivity(i);
                    }
                    else runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Invalid credentials",Toast.LENGTH_SHORT).show();
                            SharedPreferences sp=getSharedPreferences("CalbulanceLogin", MODE_PRIVATE);
                            SharedPreferences.Editor e = sp.edit();
                            e.clear().commit();
                        }
                    });
                }
                catch(Exception e) {
                    Log.e("TAG",e.getMessage());
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast t = Toast.makeText(getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.CENTER, 0, 0);
                                t.show();
                            }
                        });
                    } catch(Exception e1){}
                }
                finally {
                    try {
                        if(writer!=null && reader !=null){
                            writer.close();
                            reader.close();}
                        c.disconnect();
                    }
                    catch (Exception e){}
                }
            }
        });
        network_thread.start();

    }
}
