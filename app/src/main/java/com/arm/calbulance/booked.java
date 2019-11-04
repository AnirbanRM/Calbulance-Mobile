package com.arm.calbulance;

import android.content.Intent;
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

public class booked extends AppCompatActivity {
    String m = "";
    TextView Hospital,Contact, Patient, Driver_name, Ambulance_no, Date ;
    ImageView statusicon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked);

        Hospital = findViewById(R.id.hospital);
        Contact = findViewById(R.id.phone);
        Patient = findViewById(R.id.doc_type);
        Driver_name = findViewById(R.id.driver_name);
        Ambulance_no = findViewById(R.id.ambulance_number);
        Date = findViewById(R.id.Appdatetime);
        statusicon = findViewById(R.id.statusicon);
        statusicon.setImageResource(R.drawable.pend);

        Intent i = getIntent();
        m =  i.getStringExtra("ID");

        get_current_hospitaldetail();
    }

    private void get_current_hospitaldetail() {
        Thread network_thread = new Thread(new Runnable() {
            BufferedReader reader;
            BufferedWriter writer;
            HttpURLConnection c;
            @Override
            public void run() {
                final StringBuilder response= new StringBuilder();
                try {
                    URL url = new URL("http", "3.14.219.83", "APIs/get_request_detail.php");
                    c =  (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("POST");
                    c.setDoOutput(true);
                    c.setDoInput(true);
                    c.setConnectTimeout(3000);
                    String param = "ID="+ URLEncoder.encode(m);
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
                                Hospital.setText(obj.getString("Hospital"));
                                Contact.setText(obj.getString("Contact"));
                                Patient.setText(obj.getString("Patient"));
                                Date.setText(obj.getString("Date"));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Thread update_thd = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                dorefresh();
                                            }
                                        });
                                        update_thd.start();
                                    }
                                });
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
    void dorefresh() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        HttpURLConnection c = null;

        final StringBuilder response = new StringBuilder();
        while (true) {
            try {
                URL url = new URL("http", "3.14.219.83", "APIs/get_request_update.php");

                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setDoOutput(true);
                c.setDoInput(true);
                c.setConnectTimeout(3000);
                String param = "ID=" + URLEncoder.encode(m);
                writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                writer.write(param);
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                String line;
                response.setLength(0);
                while ((line = reader.readLine()) != null)
                    response.append(line);

                Log.e("tag", response.toString());

                if (!response.toString().equals("D") && !response.toString().equals("P")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject obj = new JSONObject(response.toString());
                                Driver_name.setText(obj.getString("Driver"));
                                Ambulance_no.setText(obj.getString("Ambulance"));
                                statusicon.setImageResource(R.drawable.tick);
                            } catch (JSONException e) {
                            }
                        }
                    });
                } else if (response.toString().equals("D")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusicon.setImageResource(R.drawable.cross);
                        }
                    });
                }
            }
        catch(Exception e){
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast t = Toast.makeText(getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    }
                });
            } catch (Exception e1) {
            }
        }
        finally{
            try {
                if (writer != null && reader != null) {
                    writer.close();
                    reader.close();
                }
                c.disconnect();
            } catch (Exception e) {
            }
        }
            try{
            Thread.sleep(3000);}
            catch(InterruptedException e){}
    }





    }
}
