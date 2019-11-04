package com.arm.calbulance;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Appointment extends Fragment {

    double X=0,Y=0;
    ListView hospital_list;
    ClassLocation location;

    List<String> HospitalID = new ArrayList<String>();
    List<String> Hospital = new ArrayList<String>();
    List<String> Rating = new ArrayList<String>();
    List<String> Time = new ArrayList<String>();



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, 10);
            if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, 10);
        }
        return inflater.inflate(R.layout.layout_appointment,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hospital_list = view.findViewById(R.id.hospital_list_view_appointment);

        if(ActivityCompat.checkSelfPermission(getContext(),ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            location = new ClassLocation(getContext());
            while(X==0.0) {
                X = location.latitude;
                Y = location.longitude;
            }
        }

        populatelist();
    }
    private void populatelist() {
        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;

            @Override
            public void run() {
                final StringBuilder response = new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/get_hospitals.php");
                    c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "X=" + URLEncoder.encode(String.valueOf(X)) + "&Y=" + URLEncoder.encode(String.valueOf(Y));
                    writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                    writer.write(param);
                    writer.flush();

                    reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null)
                        response.append(line);


                    try {
                        JSONObject obj = new JSONObject(response.toString());
                        JSONArray hospital_array = obj.getJSONArray("Hospitals");
                        for (int i = 0; i < hospital_array.length(); i++) {
                            JSONObject temp = new JSONObject(hospital_array.get(i).toString());
                            HospitalID.add(temp.getString("ID"));
                            Hospital.add(temp.getString("Name"));
                            Rating.add("5");
                            Time.add("9:00 - 19:00");
                        }
                    } catch (JSONException e) {
                        Log.e("Tag", String.valueOf(e.getMessage()));
                    }
                } catch (Exception e) {
                    try {
                        Toast t = Toast.makeText(getActivity().getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    } catch (Exception e1) {
                    }
                } finally {
                    try {
                        if (writer != null && reader != null) {
                            writer.close();
                            reader.close();
                        }
                        c.disconnect();
                    } catch (Exception e) {
                    }
                }
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final StringBuilder e = response;
                            Appointment.hospitallistadapter adapter = new Appointment.hospitallistadapter();
                            hospital_list.setAdapter(adapter);
                            hospital_list.setOnItemClickListener(temp);
                        }
                    });
            }
        });
        network_thread.start();
    }

    ListView.OnItemClickListener temp = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(id==0) {
                Log.e("TAG",HospitalID.get(position));
                Intent i = new Intent(getActivity(), book_appointment.class);
                i.putExtra("HospitalID",HospitalID.get(position));
                startActivity(i);
            }
        }
    };

    class hospitallistadapter extends BaseAdapter {
        @Override
        public int getCount() {
            return Hospital.size();
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
        public View getView(final int position, View view, final ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.hospital_list_item_design_appointment, null);

            TextView hosp_name = (TextView) view.findViewById(R.id.Hospital_Name);
            TextView timing = (TextView) view.findViewById(R.id.Timing);
            TextView rating = (TextView) view.findViewById(R.id.rating);
            ImageView dp = (ImageView) view.findViewById(R.id.hospital_logo);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ListView) parent).performItemClick(v,position,0);
                }
            });

            hosp_name.setText(Hospital.get(position));
            timing.setText(Time.get(position));
            rating.setText(Rating.get(position));
            dp.setImageResource(R.drawable.tag_ambulance);

            return view;
        }
    }

}
