package com.arm.calbulance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import java.util.jar.Attributes;

public class About_Hospital_Ambulance extends AppCompatActivity {

    String HID,CLIENTID;
    TextView hname,address,contact,website,rating,patname,contact_user,address_user;
    Button book_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about__hospital__ambulance);

        Intent i = getIntent();
        HID = i.getStringExtra("HospitalID");

        SharedPreferences sp1 = getSharedPreferences("CalbulanceLogin", MODE_PRIVATE);
        CLIENTID =  sp1.getString("USERNAME", null);

        hname = findViewById(R.id.hname);
        rating = findViewById(R.id.rating);
        contact = findViewById(R.id.phone);
        website = findViewById(R.id.website);
        address = findViewById(R.id.address);
        patname = findViewById(R.id.name);
        contact_user = findViewById(R.id.contact);
        address_user = findViewById(R.id.address_us);
        book_button = findViewById(R.id.book_button);

        book_button.setOnClickListener(book_clicked);

        Toast.makeText(getApplicationContext(),"Getting hospital details",Toast.LENGTH_SHORT).show();
        get_hospital();
    }

    View.OnClickListener book_clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            book_ambulance();
        }
    };

    void book_ambulance(){
        if(patname.getText().toString().length()==0 || address_user.getText().toString().length()==0 || contact_user.getText().toString().length()==0) {
            Toast.makeText(getApplicationContext(), "One or more fields are empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;
            @Override
            public void run() {
                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/book_ambulance.php");
                    c =  (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "HOSPITAL="+ URLEncoder.encode(HID) +"&CLIENT="+ URLEncoder.encode(CLIENTID) + "&PATIENT_NAME="+ URLEncoder.encode(patname.getText().toString()) + "&CONTACT="+ URLEncoder.encode(contact_user.getText().toString()) +"&ADDRESS="+ URLEncoder.encode(address_user.getText().toString());
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) !=null)
                        response.append(line);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(getApplicationContext(), booked.class);
                            i.putExtra("ID",response.toString());
                            startActivity(i);
                        }
                    });
                }
                catch(Exception e) {
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


    void get_hospital(){
        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;
            @Override
            public void run() {
                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/get_individual_hospital.php");
                    c =  (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "HOSPITAL="+ URLEncoder.encode(HID);
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) !=null)
                        response.append(line);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject obj = new JSONObject(response.toString());
                                    hname.setText(obj.getString("Name"));
                                    address.setText(obj.getString("Address"));
                                    contact.setText(obj.getString("Contact"));
                                    if(obj.getString("Rating").equals(""))rating.setText("Not yet rated");
                                    else rating.setText(obj.getString("Rating"));
                                    website.setText(obj.getString("Website"));
                                }
                                catch(JSONException e){}
                            }
                        });
                }
                catch(Exception e) {
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
