package com.arm.calbulance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Profile extends Fragment {
    String EMAIL,NAME,CONTACT,ID;
    TextView name,email,contact,password;
    Button update;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        List<Fragment> f_arr = getFragmentManager().getFragments();
        EMAIL = f_arr.get(0).getArguments().getString("EMAIL");
        NAME = f_arr.get(0).getArguments().getString("NAME");
        CONTACT = f_arr.get(0).getArguments().getString("CONTACT");
        ID = f_arr.get(0).getArguments().getString("ID");
        return inflater.inflate(R.layout.layout_profile,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        email = view.findViewById(R.id.profile_email);
        name = view.findViewById(R.id.profile_name);
        contact = view.findViewById(R.id.profile_contact);
        update = view.findViewById(R.id.update);
        password = view.findViewById(R.id.profile_password);
        update.setOnClickListener(update_clicked);
        email.setText(EMAIL);
        name.setText(NAME);
        contact.setText(CONTACT);
    }

    boolean checkpassword(String password){
        char password_char_array[] = password.toCharArray();
        boolean capital=false,small=false,num=false;
        if(password.length()<6)
            return false;
        for(int i=0;i<password.length()-1;i++) {
            if (password_char_array[i] >= 'A' && password_char_array[i] <= 'Z')
                capital = true;
            if (password_char_array[i] >= 'a' && password_char_array[i] <= 'z')
                small = true;
            if (password_char_array[i] >= '0' && password_char_array[i] <= '9')
                num = true;
        }
        if(capital==false || small==false || num == false)
            return false;
        else return true;
    }

    View.OnClickListener update_clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(name.getText().toString().length()==0||email.getText().toString().length()==0||contact.getText().toString().length()==0){Toast.makeText(getContext(),"One or more fields are blank.",Toast.LENGTH_SHORT).show();return;}
            if(password.getText().toString().length()!=0)
                if(!checkpassword(password.getText().toString())){Toast.makeText(getContext(),"Weak Password",Toast.LENGTH_SHORT);return;}

            Thread network_thread = new Thread(new Runnable() {
                BufferedReader reader;
                BufferedWriter writer;
                HttpURLConnection c;

                @Override
                public void run() {
                    final StringBuilder response= new StringBuilder();
                    try {
                        URL url = new URL("http", "3.14.219.83", "APIs/user_update.php");
                        c = (HttpURLConnection) url.openConnection();
                        c.setRequestMethod("POST");
                        c.getPermission();
                        c.setDoOutput(true);
                        c.setDoInput(true);
                        c.setConnectTimeout(3000);
                        String a;
                        if(password.getText().toString().length()==0)a="-1";else a=password.getText().toString();
                        String param = "ID=" + URLEncoder.encode(ID) + "&NAME=" + URLEncoder.encode(name.getText().toString()) + "&EMAIL=" + URLEncoder.encode(email.getText().toString()) + "&CONTACT=" + URLEncoder.encode(contact.getText().toString()) + "&PASSWORD=" + URLEncoder.encode(a);
                        writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                        writer.write(param);
                        writer.flush();

                        reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                        String line;
                        while ((line = reader.readLine()) != null)
                            response.append(line);
                        if(response.toString().equals("SUCCESS"))
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity().getApplicationContext(),"Successfully Updated !",Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                    catch(Exception e) {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast t = Toast.makeText(getActivity().getApplicationContext(), "Cannot connect to server", Toast.LENGTH_SHORT);
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
            HomePage parent = (HomePage) getActivity();
            parent.NAME = name.getText().toString();
            parent.CONTACT = contact.getText().toString();
            parent.EMAIL = email.getText().toString();

            SharedPreferences sp=getActivity().getSharedPreferences("CalbulanceLogin", MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.clear().commit();
        }
    };
}
