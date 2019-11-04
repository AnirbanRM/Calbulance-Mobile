package com.arm.calbulance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;

public class appointment_booked extends AppCompatActivity {
    String HospitalID,Hospital,PatientName,PatientContact,PatientAddress,DoctorName,DoctorType,AppDat,AppTim;

    ImageView status_icon;
    ConstraintLayout status_panel;
    TextView hos_n,curr_date_time,pn,pc,pa,dn,dt,ap_dt,status_msg;
    Dictionary<String,Integer> Month_to_Int = new Hashtable<String, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booked);

        Month_to_Int.put("January",1);
        Month_to_Int.put("February",2);
        Month_to_Int.put("March",3);
        Month_to_Int.put("April",4);
        Month_to_Int.put("May",5);
        Month_to_Int.put("June",6);
        Month_to_Int.put("July",7);
        Month_to_Int.put("August",8);
        Month_to_Int.put("September",9);
        Month_to_Int.put("October",10);
        Month_to_Int.put("November",11);
        Month_to_Int.put("December",12);

        hos_n = findViewById(R.id.hospital_name);
        curr_date_time = findViewById(R.id.current_date_time);
        pn = findViewById(R.id.patient_name);
        pc = findViewById(R.id.contact);
        pa = findViewById(R.id.address);
        dn = findViewById(R.id.doctor_name);
        dt = findViewById(R.id.doctor_typ);
        ap_dt = findViewById(R.id.app_date_time);
        status_icon = findViewById(R.id.status_icon);
        status_panel = findViewById(R.id.status_panel);
        status_msg = findViewById(R.id.status_msg);

        Intent i = getIntent();
        HospitalID =  i.getStringExtra("HospitalID");
        Hospital =  i.getStringExtra("Hospital");
        PatientName = i.getStringExtra("PatientName");
        PatientContact = i.getStringExtra("PatientContact");
        PatientAddress = i.getStringExtra("PatientAddress");
        DoctorName = i.getStringExtra("DoctorName");
        DoctorType = i.getStringExtra("DoctorType");
        AppDat = i.getStringExtra("AppointmentDate");
        AppTim = i.getStringExtra("AppointmentTime");

        hos_n.setText(Hospital);
        curr_date_time.setText((String)(Calendar.getInstance().get(Calendar.DATE)+"/"+Calendar.getInstance().get(Calendar.MONTH)+"/"+Calendar.getInstance().get(Calendar.YEAR) + " "+  Calendar.getInstance().get(Calendar.HOUR_OF_DAY)  +":"+Calendar.getInstance().get(Calendar.MINUTE)));
        pn.setText(PatientName);
        pc.setText(PatientContact);
        pa.setText(PatientAddress);
        dn.setText(DoctorName);
        dt.setText(DoctorType);
        ap_dt.setText((String)(AppDat+" "+AppTim));
        register();
    }

    private String date(String a){
        int t= 0;
        String temp[]={"","",""};
        for(int i = 0;i<a.length();i++) {
            if(a.toCharArray()[i]==' '){t++;continue;}
            temp[t] = temp[t] + a.toCharArray()[i]; }
        return temp[2]+"-"+Month_to_Int.get(temp[1])+"-"+temp[0];
    }

    private String time(String a){
        char t[] = a.toCharArray();
        int hr =  Integer.parseInt(String.valueOf(t[0])+String.valueOf(t[1]));
        int min = Integer.parseInt(String.valueOf(t[3])+String.valueOf(t[4]));
        String AM_PM = String.valueOf(String.valueOf(t[6])+String.valueOf(t[7]));
        if(AM_PM.equals("PM")) hr+=12;
        return String.valueOf(hr)+":"+String.valueOf(min)+":00";
    }

    private void register() {
        SharedPreferences sp1 = getSharedPreferences("CalbulanceLogin", MODE_PRIVATE);
        final String CLIENTID =  sp1.getString("USERNAME", null);

        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;

            @Override
            public void run() {
                try{ Thread.sleep(1000);} catch(InterruptedException e){}
                final StringBuilder response = new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/book_appointment.php");
                    c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "clientID=" + URLEncoder.encode(CLIENTID) + "&HospitalID=" + URLEncoder.encode(HospitalID) + "&PatientName=" + URLEncoder.encode(PatientName)+ "&PatientContact=" + URLEncoder.encode(PatientContact) + "&PatientAddress=" + URLEncoder.encode(PatientAddress) + "&DoctorName=" + URLEncoder.encode(DoctorName) + "&DoctorType=" + URLEncoder.encode(DoctorType) + "&AppDate=" + URLEncoder.encode(date(AppDat)) + "&AppTime=" + URLEncoder.encode(time(AppTim));
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null)
                        response.append(line);

                    if(response.toString().equals("SUCCESS")){
                        runOnUiThread(new Runnable() {
                            @SuppressLint("NewApi")
                            @Override
                            public void run() {
                                status_icon.setImageResource(R.drawable.tick);
                                status_panel.setBackground(new ColorDrawable(Color.parseColor("#252CE71F")));
                                status_icon.setForegroundTintList(ColorStateList.valueOf(Color.parseColor("#252CE71F")));
                                status_msg.setText("Appointment Booked !\nGo to Profile>Appointments for confirmation from Hospital");
                            }
                        });
                    }
                }

                catch (Exception e) {
                    try {
                        Toast t = Toast.makeText(getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    } catch (Exception e1) {}
                } finally {
                    try {
                        if (writer != null && reader != null) {
                            writer.close();
                            reader.close();
                        }
                        c.disconnect();
                    } catch (Exception e) {}
                }
            }
        });
        network_thread.start();

    }
}
