package com.dji.uxsdkdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.location.Location;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Heading.onInputListener {


    private static final float DEFAULT_ZOOM = 15f;
    public static GoogleMap mMap;
    private static final String TAG = "MapActivity";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private EditText mSearchText;
    private ImageButton mLaunchHeading;
    private ImageButton mChangeMapType;
    public Marker searchMarker;
    public Marker originMarker;
    public Marker destMarker;


    public LatLng headingOrg;
    public LatLng headingDest;
    public Double headingCalc;


    @Override
    public void sendInput(LatLng o, LatLng d, Double h) {
        setHeadingOrg(o);
        setHeadingDest(d);
        setHeadingCalc(h);
        setMarkers();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        mSearchText = findViewById(R.id.input_search);
        mLaunchHeading = findViewById(R.id.ic_heading_launcher);
        mChangeMapType = findViewById(R.id.ic_mapTypes);

        Log.d(TAG, "initMap: initializing map");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker at UAViation Aerial Solutions
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        mMap = googleMap;

        // Add a marker in UAViation and move the camera
        LatLng UAV = new LatLng(49.238074, -122.853361);
        MarkerOptions options = new MarkerOptions()
                .position(UAV).title("UAViation Aerial Solutions")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin1));
        searchMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(UAV));


        getDeviceLocation();
        mMap.setPadding(0, 200, 0, 0);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        init();

    }

    private void init() {
        Log.d(TAG, "init: initializing");

        this.mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    //execute our method for searching
                    geoLocate();
                }

                return false;

            }
        });

        mLaunchHeading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "opening heading fragment");
                Heading headingFragment = new Heading();
                headingFragment.show(getSupportFragmentManager(), "Heading Dialogue");
            }
        });

        mChangeMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Opening Map Type Fragment");

                MapTypeDialog mapTypeFragment = new MapTypeDialog();
                mapTypeFragment.show(getSupportFragmentManager(), "MapType Dialog");
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (marker.equals(originMarker)) {
                    setHeadingOrg(marker.getPosition());
                }

                if (marker.equals(destMarker)) {
                    setHeadingDest(marker.getPosition());
                }
            }
        });
    }


    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = this.mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 5);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));

        }
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        this.mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "My Location");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String t) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!t.equals("My Location")) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(t);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void setMarkers() {

        if (destMarker != null) {
            this.destMarker.remove();
        }

        if (originMarker != null) {
            this.originMarker.remove();
        }

        if (headingDest != null) {
            MarkerOptions destMarkerOptions = new MarkerOptions().position(headingDest).title("Tie-Point").draggable(true);
            this.destMarker = mMap.addMarker(destMarkerOptions);
        }

        if (headingOrg != null) {
            MarkerOptions originMarkerOptions = new MarkerOptions().position(headingOrg).title("Origin")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).draggable(true);
            this.originMarker = mMap.addMarker(originMarkerOptions);
        }
    }


    //====================================================================================================

    //ALL THE GETTERS AND SETTERS

    public LatLng getHeadingOrg() {
        return headingOrg;
    }

    public LatLng getHeadingDest() {
        return headingDest;
    }

    public void setHeadingOrg(LatLng headingOrg) {
        this.headingOrg = headingOrg;
    }

    public void setHeadingDest(LatLng headingDest) {
        this.headingDest = headingDest;
    }

    public void setHeadingCalc(Double headingCalc) {
        this.headingCalc = headingCalc;
    }
}