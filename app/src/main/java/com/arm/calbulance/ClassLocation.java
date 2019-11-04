package com.arm.calbulance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.security.Permission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.location.LocationManager.GPS_PROVIDER;

public class ClassLocation extends Service {

    Context appcontext;
    Location loc;
    double latitude;
    double longitude;
    LocationManager locationManager;

    public ClassLocation(Context context) {
        this.appcontext = context;
        latitude = 0;
        longitude = 0;
        getLocation(appcontext);
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @SuppressLint("MissingPermission")
    private void getLocation(Context appcontext) {
        locationManager = (LocationManager) appcontext.getSystemService(Service.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, listener);

        if (locationManager != null) {
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}