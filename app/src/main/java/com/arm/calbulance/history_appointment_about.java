package com.arm.calbulance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class history_appointment_about extends AppCompatActivity {

    String ID;
    ImageView status_icon;
    ConstraintLayout status_panel;
    TextView hos_n,booked_date_time,pn,pc,pa,dn,dt,ap_dt,status_msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_appointment_about);
        Intent i = getIntent();
        ID = i.getStringExtra("ID");
        hos_n = findViewById(R.id.hospital_name);
        booked_date_time = findViewById(R.id.booked_date_time);
        pn = findViewById(R.id.patient_name);
        pc = findViewById(R.id.contact);
        pa = findViewById(R.id.address);
        dn = findViewById(R.id.doctor_name);
        dt = findViewById(R.id.doctor_typ);
        ap_dt = findViewById(R.id.app_date_time);
        status_icon = findViewById(R.id.status_icon);
        status_panel = findViewById(R.id.status_panel);
        status_msg = findViewById(R.id.status_msg);
        get_further_info();
    }

    private void get_further_info(){
        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;

            @SuppressLint({"NewApi", "SetTextI18n"})
            @Override
            public void run() {
                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/get_history_appointment_detail.php");
                    c =  (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "ID="+ URLEncoder.encode(ID);
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) !=null)
                        response.append(line);

                    Log.e("TAG",response.toString());


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject obj = new JSONObject(response.toString());
                                hos_n.setText(obj.getString("Hospital"));
                                booked_date_time.setText(obj.getString("Booked_DT"));
                                pn.setText(obj.getString("Pat_Name"));
                                pc.setText(obj.getString("Pat_Con"));
                                pa.setText(obj.getString("Pat_Addr"));
                                dn.setText(obj.getString("Doctor_Name"));
                                dt.setText(obj.getString("Doctor_Type"));

                                if(obj.getString("Status").equals("A")){
                                    status_icon.setImageResource(R.drawable.tick);
                                    status_panel.setBackground(new ColorDrawable(Color.parseColor("#252CE71F")));
                                    status_icon.setForegroundTintList(ColorStateList.valueOf(Color.parseColor("#252CE71F")));
                                    status_msg.setText("Appointment Approved");
                                }

                                else if(obj.getString("Status").equals("D")){
                                    status_icon.setImageResource(R.drawable.cross);
                                    status_panel.setBackground(new ColorDrawable(Color.parseColor("#25E71F1F")));
                                    status_icon.setForegroundTintList(ColorStateList.valueOf(Color.parseColor("#25E71F1F")));
                                    status_msg.setText("Sorry !\nYour Appointment has been declined by the hospital.");
                                }

                                ap_dt.setText(format_date_time(obj.getString("AP_DT")));
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
    private String format_date_time(String a){
        return a;
    }
}
