package com.arm.calbulance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.spec.EncodedKeySpec;
import java.util.Base64;
import java.util.Dictionary;
import java.util.Objects;
import static android.Manifest.permission.INTERNET;

public class createacc extends AppCompatActivity {

    ImageView picadd, pic;
    TextView name, password, cpassword, email;
    Button create_button;
    ConstraintLayout layout;
    String PROFILE_PIC_URI_String;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createacc);

        picadd = findViewById(R.id.profilepicadd);
        pic = findViewById(R.id.profilepic);
        name = findViewById(R.id.crea_name_box);
        email = findViewById(R.id.crea_email_box2);
        password = findViewById(R.id.crea_password_box);
        cpassword = findViewById(R.id.crea_password_box2);
        create_button = findViewById(R.id.new_acc_but);
        layout = findViewById(R.id.crea_acc_layout);

        picadd.setOnClickListener(add_img);
        create_button.setOnClickListener(new_acc_clicked);

    }

    boolean checkpassword(String password) {
        char password_char_array[] = password.toCharArray();
        boolean capital = false, small = false, num = false;
        if (password.length() < 6)
            return false;
        for (int i = 0; i < password.length() - 1; i++) {
            if (password_char_array[i] >= 'A' && password_char_array[i] <= 'Z')
                capital = true;
            if (password_char_array[i] >= 'a' && password_char_array[i] <= 'z')
                small = true;
            if (password_char_array[i] >= '0' && password_char_array[i] <= '9')
                num = true;
        }

        if (capital == false || small == false || num == false)
            return false;
        else return true;
    }

    View.OnClickListener add_img = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(createacc.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            CropImage.activity().setAspectRatio(1, 1).start(createacc.this);
        }
    };

    View.OnClickListener new_acc_clicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (String.valueOf(password.getText()).equals(String.valueOf(cpassword.getText()))) ;
            else {
                Toast t = Toast.makeText(getApplicationContext(), "Password's don't match", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                return;
            }
            if (!checkpassword(password.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Weak Password", Toast.LENGTH_SHORT).show();
                return;
            }

            Thread network_thread = new Thread(new Runnable() {
                BufferedReader reader;
                BufferedWriter writer;
                HttpURLConnection c;

                @Override
                public void run() {
                    final StringBuilder response = new StringBuilder();
                    try {
                        URL url = new URL("http", "3.14.219.83", "APIs/create_account.php");
                        c = (HttpURLConnection) url.openConnection();
                        c.setRequestMethod("POST");
                        c.setDoOutput(true);
                        c.setDoInput(true);
                        c.setConnectTimeout(3000);
                        String param = "NAME=" + URLEncoder.encode(name.getText().toString()) + "&EMAIL=" + URLEncoder.encode(email.getText().toString()) + "&PASSWORD=" + URLEncoder.encode(password.getText().toString());
                        writer = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                        writer.write(param);
                        writer.flush();

                        reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

                        String line;
                        while ((line = reader.readLine()) != null)
                            response.append(line);
                    } catch (Exception e) {
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
                    SharedPreferences sp = getSharedPreferences("CalbulanceLogin", MODE_PRIVATE);
                    SharedPreferences.Editor Ed = sp.edit();
                    Ed.putString("USERNAME", email.getText().toString());
                    Ed.putString("PASSWORD", password.getText().toString());
                    Ed.commit();
                    Intent i = new Intent(getApplicationContext(), HomePage.class);
                    i.putExtra("EMAIL", email.getText().toString());
                    i.putExtra("NAME", name.getText().toString());
                    i.putExtra("CONTACT", "");
                    i.putExtra("ID", response.toString());
                    startActivity(i);
                }
            });
            network_thread.start();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            CropImage.ActivityResult res = CropImage.getActivityResult(data);
            PROFILE_PIC_URI_String = res.getUri().toString();
            pic.setImageURI(res.getUri());
            }
        }
}



