package com.gaadi.neon.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * @author Pavan
 * @version 1.0
 * @since 12/4/17
 */
public class FindLocations {

    private LocationRequest mLocationRequest;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9 * 1000;
    private static final int REQUEST_CHECK_SETTINGS = 2000;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;
    private static FindLocations self;
    private Activity activity;
    private long UPDATE_INTERVAL = 6 * 1000;  /* 6 secs */
    private long FASTEST_INTERVAL = 5 * 1000; /* 5 secs */
    private Location location;
    private ILocation callBack;
    private FusedLocationProviderClient mFusedLocationClient;

    public static FindLocations getInstance() {
        if (self == null)
            self = new FindLocations();
        return self;
    }
    //private TextView gps_status;
    //eoc

    public Location getLocation() {
        return location;
    }

    public void init(Activity activity) {
        this.activity = activity;
        callBack = (ILocation) activity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        connectClient();
    }

    private void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mFusedLocationClient != null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

            // Create LocationSettingsRequest object using location request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            // Check whether location settings are satisfied
            // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
            SettingsClient settingsClient = LocationServices.getSettingsClient(this.activity);
            settingsClient.checkLocationSettings(locationSettingsRequest);
            onConnected();
        } else {
            Toast.makeText(activity, "Please update the google play services.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onConnected() {
        // Display the connection status
        boolean permissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted && null != mFusedLocationClient) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                callBack.getLocation(location);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("NeonLocation", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    protected void startLocationUpdates() {
        boolean permissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (permissionGranted && null != mFusedLocationClient) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }


    public void onLocationChanged(Location location) {
        this.location = location;
        callBack.getLocation(location);
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(activity, result,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    /* Check Location Permission for Marshmallow Devices */
    public boolean checkPermissions(Activity activity) {
        callBack = (ILocation) activity;
        return checkPermissions(activity, callBack);
    }

    public boolean checkPermissions(Activity activity, ILocation callBack) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission(activity);
                return false;
            } else {
                showSettingDialog(activity, callBack);
                return true;
            }

        } else {
            showSettingDialog(activity, callBack);
            return true;
        }
    }

    /*  Show Popup to access User Permission  */
    private void requestLocationPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);
        }
    }

    /* Show Location Access Dialog */
    private void showSettingDialog(final Activity activity, final ILocation callBack) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);//5 sec Time interval for location update
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Setting priotity of Location request to high
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off
        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
                callBack.getPermissionStatus(true);
            }
        });
        task.addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callBack.getPermissionStatus(false);
            }
        });

    }

    public interface ILocation {
        void getLocation(Location location);

        void getAddress(String locationAddress);

        void getPermissionStatus(Boolean locationPermission);
    }
}
