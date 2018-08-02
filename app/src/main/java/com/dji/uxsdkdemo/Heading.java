package com.dji.uxsdkdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static com.google.maps.android.SphericalUtil.computeHeading;

/**
 * Created by Pearl on 2018-07-26.
 */

public class Heading extends DialogFragment implements TextWatcher {
    private static final String TAG = "Heading Fragment";

    public interface onInputListener {
        void sendInput(LatLng o, LatLng d, Double h);
    }

    public onInputListener mOnInputListener;
    private FusedLocationProviderClient hFusedLocationProviderClient;


    private AutoCompleteTextView mHeadingOrigin;
    private AutoCompleteTextView mHeadingDest;
    private TextView mHeading, mActionOk, mActionCancel;
    private ImageView moreOrg, moreDest;

    private LatLng origin;
    private LatLng tie_point;
    private LatLng curLatLon;
    private Double headingCalc;
    private static String BASE_LOCATION = "Use Current Location";
    private static String DRONE_LOCATION = "Use Drone Location";
    private static final String[] dropdown = new String[]{BASE_LOCATION, DRONE_LOCATION};

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //do nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //do nothing
    }

    @Override
    public void afterTextChanged(Editable editable) {
        try {
            String input = editable.toString();
            if (input.equals(mHeadingOrigin.getText().toString())) {

                if (input.equals(BASE_LOCATION)) {
                    useCurrentLocationForHeading();
                    editable.replace(0, editable.length(), makeLatLonStr(curLatLon));
                    origin = curLatLon;
                    updateHeading();
                }

                else if (input.equals(DRONE_LOCATION)) {
                    Toast.makeText(getContext(), "Drone Location Selected", Toast.LENGTH_SHORT).show();
                    mHeading.setText("");
                }

                else{
                    origin = convertoLatLon(input);
                    updateHeading();
                }

            }


            if (input.equals(mHeadingDest.getText().toString())) {

                if (input.equals(BASE_LOCATION)) {
                    useCurrentLocationForHeading();
                    editable.replace(0, editable.length(), makeLatLonStr(curLatLon));
                    tie_point = curLatLon;
                    updateHeading();
                }

                else if (input.equals(DRONE_LOCATION)) {
                    Toast.makeText(getContext(), "Drone Location Selected", Toast.LENGTH_SHORT).show();
                    mHeading.setText("");
                }

                else{
                    tie_point = convertoLatLon(input);
                    updateHeading();
                }

            }



        } catch (NullPointerException e) {//do nothing}

        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.heading_fragment, container, false);

        mActionCancel = view.findViewById(R.id.action_cancel);
        mActionOk = view.findViewById(R.id.action_ok);
        mHeadingOrigin = view.findViewById(R.id.origin);
        mHeadingDest = view.findViewById(R.id.dest);
        mHeading = view.findViewById(R.id.heading);
        moreOrg = view.findViewById(R.id.more_options_origin);
        moreDest = view.findViewById(R.id.more_options_dest);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, dropdown);
        mHeadingOrigin.setAdapter(adapter);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, dropdown);
        mHeadingDest.setAdapter(adapter2);

        moreOrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHeadingOrigin.showDropDown();
            }
        });
        moreDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHeadingDest.showDropDown();
            }
        });



        if (((MapsActivity) getActivity()).getHeadingOrg() != null) {
            origin = ((MapsActivity) getActivity()).getHeadingOrg();
            mHeadingOrigin.setText(makeLatLonStr(origin));
        } else {
            origin = null;
        }

        if (((MapsActivity) getActivity()).getHeadingDest() != null) {
            tie_point = ((MapsActivity) getActivity()).getHeadingDest();
            mHeadingDest.setText(makeLatLonStr(tie_point));


        } else {
            tie_point = null;
        }

        updateHeading();

//        headingCalc = null;


        mHeadingOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_NEXT
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    origin = convertoLatLon(textView.getText().toString());
//                    updateHeading();
                    return true;
                }

                return false;
            }
        });

        mHeadingDest.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    tie_point = convertoLatLon(textView.getText().toString());
                    updateHeading();
                    return true;
                }


                return false;
            }
        });

        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
            }
        });


        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capturing input");
//                setMarkers();
//                updateHeading();
                mOnInputListener.sendInput(origin, tie_point, headingCalc);
                getDialog().dismiss();
            }
        });

        mHeadingOrigin.addTextChangedListener(this);
        mHeadingDest.addTextChangedListener(this);

        return view;

    }

    private void updateHeading() {
        try {
            double heading = getHeading(origin, tie_point);
            mHeading.setText(" Heading: " + heading);
        } catch (NullPointerException e) {
            //do nothing
        }
    }

    private double getHeading(LatLng o, LatLng dest) {
        double heading = computeHeading(origin, tie_point);
        headingCalc = heading;
        return heading;
    }


    private LatLng convertoLatLon(String input) {
        try {
            String[] latlonString = input.split(",");
            double lat = Double.parseDouble(latlonString[0]);
            double lon = Double.parseDouble(latlonString[1]);
            LatLng latlon = new LatLng(lat, lon);
            return latlon;
        } catch (NullPointerException e) {
            //do nothing
        } catch (NumberFormatException e) {
            //do nothing
        }

        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnInputListener = (onInputListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    public String makeLatLonStr(LatLng ll) {
        double lat = ll.latitude;
        double lon = ll.longitude;
        String out = lat + "," + lon;
        return out;
    }

    private void useCurrentLocationForHeading() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        hFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());


        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                final Task location = hFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            Double curlat = currentLocation.getLatitude();
                            Double curlon = currentLocation.getLongitude();
                            curLatLon = new LatLng(curlat, curlon);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }


    }

}
