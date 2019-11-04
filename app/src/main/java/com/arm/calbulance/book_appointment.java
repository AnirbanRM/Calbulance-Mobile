package com.arm.calbulance;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class book_appointment extends AppCompatActivity {

    String HospitalID;
    List<String> DoctorName = new ArrayList<String>();
    List<String> DoctorType = new ArrayList<String>();
    TextView hosp_n,date_box,time_box,address;
    EditText patientname,contact;
    ListView doctor_list;
    String Month[] = {"January","February","March","April","May","June","July","August","September","October","November","December"},AM_PM[]={"AM","PM"};
    View current_doc_selected=null;
    Button book_app_but;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        hosp_n = findViewById(R.id.AP_H_N);
        patientname = findViewById(R.id.patient_Name);
        contact = findViewById(R.id.Patient_Contact);
        doctor_list = findViewById(R.id.doctor_lv);
        address = findViewById(R.id.Address);
        Intent i = getIntent();
        HospitalID =  i.getStringExtra("HospitalID");
        get_doctor_list_and_name();
        date_box = findViewById(R.id.date);
        time_box = findViewById(R.id.time);
        book_app_but = findViewById(R.id.book_app_but);
        date_box.setText((String)(Calendar.getInstance().get(Calendar.DATE) + " " +  Month[(int)Calendar.getInstance().get(Calendar.MONTH)] + " " + Calendar.getInstance().get(Calendar.YEAR)));
        time_box.setText((String)(time_pad(String.valueOf(Calendar.getInstance().get(Calendar.HOUR))) + ":" +  time_pad(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE))) + " " + AM_PM[Calendar.getInstance().get(Calendar.AM_PM)]));
        date_box.setOnClickListener(date_box_clicked);
        time_box.setOnClickListener(time_box_clicked);
        book_app_but.setOnClickListener(new_appointment);
    }

    View.OnClickListener new_appointment = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(patientname.getText().toString().equals("") || contact.getText().toString().equals("") || address.getText().toString().equals("") || current_doc_selected==null)
            {Toast.makeText(getApplicationContext(),"One or more fields are empty ! ",Toast.LENGTH_SHORT).show();return;}
            Intent i = new Intent(book_appointment.this,appointment_booked.class);
            i.putExtra("Hospital",hosp_n.getText().toString());
            i.putExtra("HospitalID",HospitalID);
            i.putExtra("PatientName",patientname.getText().toString());
            i.putExtra("PatientContact",contact.getText().toString());
            i.putExtra("PatientAddress",address.getText().toString());
            i.putExtra("DoctorName", ((TextView)current_doc_selected.findViewById(R.id.Doctor_Name)).getText().toString());
            i.putExtra("DoctorType", ((TextView)current_doc_selected.findViewById(R.id.Doctor_Type)).getText().toString());
            i.putExtra("AppointmentDate",date_box.getText().toString());
            i.putExtra("AppointmentTime",time_box.getText().toString());
            startActivity(i);
        }
    };

    View.OnClickListener date_box_clicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            DatePickerDialog dpd = new DatePickerDialog(book_appointment.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date_box.setText((String)(String.valueOf(dayOfMonth) + " " + Month[month] + " " + String.valueOf(year)));
                }
            },Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DATE));
            dpd.show();
        }
    };

    private String time_pad(String a){
        if(a.length()==1)
            return "0"+a;
        return a;
    }

    View.OnClickListener time_box_clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog tpd = new TimePickerDialog(book_appointment.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String AM_PM=hourOfDay>12?"PM":"AM";
                    time_box.setText((String)(time_pad(String.valueOf(hourOfDay%12)) + ":" +  time_pad(String.valueOf(minute)) + " " + AM_PM));
                }
            },Calendar.getInstance().get(Calendar.HOUR_OF_DAY),Calendar.getInstance().get(Calendar.MINUTE),false);
            tpd.show();
        }
    };



    private void get_doctor_list_and_name(){
        Thread thd = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;

            @Override
            public void run() {
                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/get_doctors.php");
                    c =  (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "hos="+ URLEncoder.encode(HospitalID);
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) !=null)
                        response.append(line);
                    Log.e("Tag",response.toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject obj = new JSONObject(response.toString());
                                hosp_n.setText(obj.getString("HName"));
                                JSONArray doctor_array = obj.getJSONArray("Doctors");
                                for(int i = 0;i<doctor_array.length();i++) {
                                    JSONObject temp = new JSONObject(doctor_array.get(i).toString());
                                    DoctorName.add(temp.getString("Name"));
                                    DoctorType.add(temp.getString("Type"));
                                }
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
                runOnUiThread(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        DoctorList dl = new DoctorList();
                        doctor_list.setAdapter(dl);
                        doctor_list.setOnItemClickListener(doctor_list_item_clicked);
                    }
                });
            }
        });
        thd.start();
    }

    ListView.OnItemClickListener doctor_list_item_clicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(current_doc_selected!=null)
                current_doc_selected.setBackground(new ColorDrawable(Color.parseColor("#FFFFFF")));
            current_doc_selected = view;
            view.setBackground(new ColorDrawable(Color.parseColor("#EEEEEE")));
        }
    };

    class DoctorList extends BaseAdapter{
        @Override
        public int getCount() {
            return DoctorName.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.doctor_list_item_design,null);

            TextView dn =  view.findViewById(R.id.Doctor_Name);
            TextView dt = view.findViewById(R.id.Doctor_Type);

            dn.setText(DoctorName.get(position));
            dt.setText(DoctorType.get(position));

            if(current_doc_selected!=null){
                if(((TextView) current_doc_selected.findViewById(R.id.Doctor_Name)).getText().toString().equals(((TextView) view.findViewById(R.id.Doctor_Name)).getText().toString())) {
                    view.setBackground(new ColorDrawable(Color.parseColor("#EEEEEE")));
                    current_doc_selected = view;
                }
            }
            return view;
        }
    }
}
