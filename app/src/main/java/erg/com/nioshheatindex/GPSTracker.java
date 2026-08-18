package erg.com.nioshheatindex;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener {

    private final Context context;
    private boolean canGetLocation = false;

    private double latitude;
    private double longitude;

    private Location location;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    //private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60;

    public GPSTracker(Context context) {
        this.context = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                canGetLocation = true;
                if (isNetworkEnabled) {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    } catch (SecurityException e) {
                        showToastNoService();
                    }
                }
            }
            try {
                int ctr = 0;
                while (latitude <= 0 || ctr < 3) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        ctr = ctr + 1;
                    }
                }
            } catch (SecurityException e) {
                showToastNoService();
            }

            if (isGPSEnabled) {
                if (location == null) {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        try {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        } catch (SecurityException e) {
                            showToastNoService();
                        }
                    } catch (SecurityException e) {
                        showToastNoService();
                    }
                }
            }
        } catch (Exception e) {
            showToastNoService();
        }
        return location;
    }

    private void showToastNoService() {
        Context context = getApplicationContext();
        CharSequence text = getString(R.string.hi_noservice_message);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        final Intent launchMain = new Intent(this, MainActivity.class);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.app_name);
        alert.setMessage(R.string.hi_noservice_message);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }

    @SuppressLint("MissingPermission")
    public double getLatitude() {
        int ctr = 0;
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        while (latitude <= 0 || ctr < 3) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null){
                    latitude = location.getLatitude();
                    ctr = ctr + 1;
                }
        }

//        if(location != null)
//        {
//            int ctr = 0;
//            while (latitude <= 0 || ctr < 3) {
//                latitude = location.getLatitude();
//                ctr = ctr + 1;
//            }
//        }
        return latitude;
    }

    public double getLongitude()
    {
        if(location != null)
        {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
