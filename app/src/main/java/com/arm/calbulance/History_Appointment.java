package com.arm.calbulance;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;

public class History_Appointment extends Fragment {

    String EMAIL;
    ListView list;

    List<String> ID = new ArrayList<String>();
    List<String> Hospital = new ArrayList<String>();
    List<String> AppointmentDateTime = new ArrayList<String>();
    List<String> PatientName = new ArrayList<String>();
    List<String> Doctor = new ArrayList<String>();
    List<String> Doctor_Type = new ArrayList<String>();
    List<String> Status = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_history__appointment,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        History h = (History) getParentFragment();
        EMAIL = h.EMAIL;
        list = view.findViewById(R.id.appointment_list);
        getList();
    }

    private void getList(){
        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;

            @Override
            public void run() {
                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/get_history_appointment.php");
                    c =  (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "client="+ URLEncoder.encode(EMAIL);
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) !=null)
                        response.append(line);

                    try {
                        JSONObject obj = new JSONObject(response.toString());
                        JSONArray history_ambulance_array = obj.getJSONArray("Prev_Appointments");
                        for(int i = 0;i<history_ambulance_array.length();i++) {
                            JSONObject temp = new JSONObject(history_ambulance_array.get(i).toString());
                            ID.add(temp.getString("ID"));
                            Hospital.add(temp.getString("HospitalName"));
                            Doctor.add(temp.getString("Doctor"));
                            Doctor_Type.add(temp.getString("DoctorType"));
                            PatientName.add(temp.getString("PatientName"));
                            AppointmentDateTime.add(temp.getString("AppDateTime"));
                            Status.add(temp.getString("Status"));
                        }
                    }
                    catch(JSONException e){ }
                }
                catch(Exception e) {
                    try {
                        Toast t = Toast.makeText(getActivity().getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
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
                if(getActivity()!=null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final StringBuilder e = response;
                            Prev_Appointment adapter = new Prev_Appointment();
                            list.setAdapter(adapter);
                        }
                    });
            }
        });
        network_thread.start();
    }


    class Prev_Appointment extends BaseAdapter {
        @Override
        public int getCount() {
            return ID.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.history_appointment_design, null);

            TextView dt = convertView.findViewById(R.id.Appdatetime);
            TextView hname = convertView.findViewById(R.id.hospital_name);
            TextView DoctorName = convertView.findViewById(R.id.doc_name);
            TextView DoctorType = convertView.findViewById(R.id.doc_type);
            TextView Patient = convertView.findViewById(R.id.pat_name);
            TextView ref = convertView.findViewById(R.id.ID);
            ImageView status_icon = convertView.findViewById(R.id.status_icon);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity().getApplicationContext(),history_appointment_about.class);
                    i.putExtra("ID",ID.get(position));
                    startActivity(i);
                }
            });

            dt.setText(AppointmentDateTime.get(position));
            hname.setText(Hospital.get(position));
            Patient.setText(PatientName.get(position));
            DoctorName.setText(Doctor.get(position));
            DoctorType.setText(Doctor_Type.get(position));
            ref.setText(ID.get(position));
            if (Status.get(position).equals("A"))
                status_icon.setImageResource(R.drawable.tick);
            else if (Status.get(position).equals("D"))
                status_icon.setImageResource(R.drawable.cross);

            return convertView;
        }
    }
}
