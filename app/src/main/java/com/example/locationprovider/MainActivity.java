package com.example.locationprovider;

import android.*;
import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView mTextView;
    private View mParentLayout;
    public static final int COARSE_LOC_REQUEST = 1;
    public static final int FINE_LOC_REQUEST = 2;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textview1);
        mParentLayout = findViewById(R.id.root_view);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000);     // 1 second, in milliseconds

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void requestFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(mParentLayout, "Fine Location Access is Required", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    FINE_LOC_REQUEST
                            );
                        }
                    })
                    .show();
        } else {
            // Permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOC_REQUEST
            );
        }

        return;
    }

    private void requestPermission(final String permission, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Snackbar.make(mParentLayout, permission + " Access is Required", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{permission},
                                    requestCode
                            );
                        }
                    })
                    .show();
        } else {
            // Permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{permission},
                    requestCode
            );
        }

        return;
    }

    private void checkPermissions() {
        // Marshmallow+ requires we ask for unmet permissions one-by-one at runtime
        if (Build.VERSION.SDK_INT >= 23) {
            boolean hasFineLocPermission =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            boolean hasCoarseLocPermission =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            if (!hasFineLocPermission) {
                Log.i(TAG, "Fine Location Permission Missing");
                requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOC_REQUEST);
            }

            if (!hasCoarseLocPermission) {
                Log.i(TAG, "Coarse Location Permission Missing");
                requestPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION, COARSE_LOC_REQUEST);
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == FINE_LOC_REQUEST) {
            Log.i(TAG, "Received response for fine location permission request.");
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mTextView.setText("FINE PERMISSION GRANTED");
            } else {
                mTextView.setText("FINE PERMISSION DENIED (CLICK HERE TO TRY AGAIN)");
            }
        } else if (requestCode == COARSE_LOC_REQUEST) {
            Log.i(TAG, "Received response for coarse location permission request.");
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mTextView.setText("COARSE PERMISSION GRANTED");
            } else {
                mTextView.setText("COARSE PERMISSION DENIED (CLICK HERE TO TRY AGAIN)");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    /**
     * TODO: 8===D  ({})
     * Coarse + Fine not tested yet – probably don't work together.
     * Remove all the mTextView shit.
     * Permission results should be Log.i'd.
     * There should be a single button that, when pressed, checks permissions and
     *requests missing permissions one-by-one from the user.
     */

    public void recheckPermissions(View view) {
        mTextView = (TextView) view;
        mTextView.setText("");
        checkPermissions();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Log.i(TAG, "Location was null. Requesting new updates.");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            Log.i(TAG, "Location was not null! Using location.");
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}
