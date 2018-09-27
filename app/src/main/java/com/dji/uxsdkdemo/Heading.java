package com.dji.uxsdkdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.gimbal.GimbalMode;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static com.google.maps.android.SphericalUtil.computeHeading;

/**
 * Created by Pearl on 2018-07-26.
 */

public class Heading extends DialogFragment {
    private static final String TAG = "Heading Fragment";

    public interface onInputListener {
        void sendInput(LatLng o, LatLng d, Double h);
    }

    public onInputListener mOnInputListener;
    private FusedLocationProviderClient hFusedLocationProviderClient;
    private AutoCompleteTextView mHeadingOrigin;
    private AutoCompleteTextView mHeadingDest;
    private TextView mHeading, mActionOk, mActionCancel;
    Spinner originSpinner, destSpinner;
    private Gimbal gimbal = null;
    private static LatLng origin;
    private String originStr;
    private static LatLng tie_point;
    private String tie_pointStr;
    private LatLng curLatLon;
    private LatLng droneLatLon;
    private Double headingCalc;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private FlightController mFlightController = null;
    private static String BASE_LOCATION = "Use Current Location";
    private static String SELECT_LOCATION = "Select one";
    private static String DRONE_LOCATION = "Use Drone Location";
    private static final String[] dropdown = new String[]{SELECT_LOCATION, BASE_LOCATION, DRONE_LOCATION};


    //====================================================================================================

    //DATA AND STRUCTURE COMMANDS


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
        originSpinner = view.findViewById(R.id.more_options_origin);
        destSpinner = view.findViewById(R.id.more_options_dest);

        if (isFlightControllerSupported()) {
            mFlightController = ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController();
        }
        if (getGimbalInstance() != null) {
            gimbal = getGimbalInstance();
        }

        if (((MapsActivity) getActivity()).getHeadingOrg() == null && ((MapsActivity) getActivity()).getHeadingDest() == null) {
            //fragment created for the first time
            pointDroneToTrueNorth();
        } else {
            setOrigin(((MapsActivity) getActivity()).getHeadingOrg());
            setOriginStr(makeLatLonStr(getOrigin()));
            this.mHeadingOrigin.setText(originStr);

            setTie_point(((MapsActivity) getActivity()).getHeadingDest());
            setTie_pointStr(makeLatLonStr(getTie_point()));
            this.mHeadingDest.setText(tie_pointStr);
        }
        updateHeading();


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, dropdown);
        adapter.setDropDownViewResource(R.layout.spinner_view_res);
        originSpinner.setAdapter(adapter);
        originSpinner.setSelection(adapter.getPosition("Select one"));
        originSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                try {
                    switch (position) {
                        case 0:
                            //default do nothing case
                            break;

                        case 1:
                            // Whatever you want to happen when the first item gets selected

                            useCurrentLocationForHeading();
                            setOrigin(curLatLon);
                            setOriginStr(makeLatLonStr(origin));
                            mHeadingOrigin.setText(originStr);
                            updateHeading();
                            pointDroneToTrueNorth();
                            pointGimbalToTiePoint();
                            break;

                        case 2:
                            // Whatever you want to happen when the second item gets selected
                            mHeadingOrigin.setText("");
                            updateDroneLatLon();
                            setOrigin(droneLatLon);
                            setOriginStr(makeLatLonStr(origin));
                            mHeadingOrigin.setText(originStr);
                            updateHeading();
                            Toast.makeText(getContext(), "Drone Location Selected: " + makeLatLonStr(origin), Toast.LENGTH_SHORT).show();
                            pointDroneToTrueNorth();
                            pointGimbalToTiePoint();
                            break;
                    }


                } catch (NullPointerException e) {

                    //do nothing
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, dropdown);
        adapter2.setDropDownViewResource(R.layout.spinner_view_res);
        destSpinner.setSelection(adapter2.getPosition("Select one"));
        destSpinner.setAdapter(adapter2);
        destSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                try {
                    switch (position) {

                        case 0:
                            //empty case do nothing
                            break;

                        case 1:
                            // Whatever you want to happen when the first item gets selected

                            useCurrentLocationForHeading();
                            setTie_point(curLatLon);
                            setTie_pointStr(makeLatLonStr(tie_point));
                            mHeadingDest.setText(tie_pointStr);
                            updateHeading();
                            pointDroneToTrueNorth();
                            pointGimbalToTiePoint();

                            break;
                        case 2:

                            // Whatever you want to happen when the second item gets selected
                            mHeadingDest.setText("");
                            updateDroneLatLon();
                            setTie_point(droneLatLon);
                            setTie_pointStr(makeLatLonStr(tie_point));
                            mHeadingDest.setText(tie_pointStr);
                            updateHeading();
                            Toast.makeText(getContext(), "Drone Location Selected: " + makeLatLonStr(tie_point), Toast.LENGTH_SHORT).show();
                            pointDroneToTrueNorth();
                            pointGimbalToTiePoint();
                            break;
                    }


                } catch (NullPointerException e) {

                    //do nothing
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mHeadingOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    setOrigin(convertoLatLon(textView.getText().toString()));
                    setOriginStr(makeLatLonStr(origin));
                    updateHeading();
                    pointDroneToTrueNorth();
                    pointGimbalToTiePoint();
                    return false;
                }
                return false;
            }
        });

        mHeadingDest.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    setTie_point(convertoLatLon(textView.getText().toString()));
                    setTie_pointStr(makeLatLonStr(tie_point));
                    updateHeading();
                    pointDroneToTrueNorth();
                    pointGimbalToTiePoint();
                    return false;
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
                ((MapsActivity) getActivity()).setHeadingOrg(getOrigin());
                ((MapsActivity) getActivity()).setHeadingDest(getTie_point());
                mOnInputListener.sendInput(getOrigin(), getTie_point(), getHeadingCalc());
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("origin", this.originStr);
        outState.putString("tie_point", this.tie_pointStr);
    }

    private void updateHeading() {
        try {
            double heading = getHeading(origin, tie_point);
            this.mHeading.setText(" Heading: " + heading);
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "updateHeading - NullPointer", Toast.LENGTH_SHORT).show();
        }
    }

    private double getHeading(LatLng o, LatLng dest) {
        setHeadingCalc(computeHeading(origin, tie_point));
        return headingCalc;
    }


    private LatLng convertoLatLon(String input) {
        try {
            String[] latlonString = input.split(",");
            double lat = Double.parseDouble(latlonString[0]);
            double lon = Double.parseDouble(latlonString[1]);
            return new LatLng(lat, lon);
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
        return lat + "," + lon;
    }

    private void useCurrentLocationForHeading() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        this.hFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());


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
                            setCurLatLon(new LatLng(curlat, curlon));
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

    private boolean isFlightControllerSupported() {
        try {
            boolean supported1 = DJISDKManager.getInstance().getProduct() != null;
            boolean supported2 = DJISDKManager.getInstance().getProduct() instanceof Aircraft;
            boolean supported3 = ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController() != null;
            return supported1 && supported2 && supported3;
        } catch (NullPointerException e) {
            return false;
            //do nothing
        }
    }

    private void updateDroneLatLon() {
        if (isFlightControllerSupported()) {
            this.mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState
                                             djiFlightControllerCurrentState) {
                    setDroneLocationLat(djiFlightControllerCurrentState.getAircraftLocation().getLatitude());
                    setDroneLocationLng(djiFlightControllerCurrentState.getAircraftLocation().getLongitude());
                    setDroneLatLon(new LatLng(droneLocationLat, droneLocationLng));
                }
            });
        } else {
            Toast.makeText(getContext(), "FlightControllerNotSupported", Toast.LENGTH_SHORT).show();
        }
    }


    //====================================================================================================

    //ACTUAL GIMBAL AND FLIGHT ROTATE COMMANDS

    private void pointGimbalToTiePoint() {
        try {
            Toast.makeText(getContext(), "In Method pointGimbalToTiePoint", Toast.LENGTH_SHORT).show();

            gimbal.setMode(GimbalMode.YAW_FOLLOW, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    if (error == null) {
                        Toast.makeText(getContext(), "pointGimbalToTiePoint- Gimbal Mode set to YAW_FOLLOW", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "pointGimbalToTiePoint- Gimbal Mode cannot be set to YAW_FOLLOW", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(1);
            builder.roll(0);
            builder.pitch(0);
            Float newYaw = (headingCalc).floatValue();
            builder.yaw(newYaw);
            sendRotateGimbalCommand(builder.build());

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "Null Pointer Found in pointGimbalToTiePoint", Toast.LENGTH_SHORT).show();
        }

    }


    private void sendRotateGimbalCommand(final Rotation rotation) {
        Toast.makeText(getContext(), "In method sendRotateGimbalCommand ", Toast.LENGTH_SHORT).show();
        gimbal.rotate(rotation, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                Toast.makeText(getContext(), "Gimbal rotated to tie-point: " + rotation.getYaw(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Gimbal getGimbalInstance() {
        if (gimbal == null) {
            initGimbal();
        }
        return gimbal;
    }

//    private void initGimbal() {
//        if (DJISDKManager.getInstance() != null) {
//            BaseProduct product = DJISDKManager.getInstance().getProduct();
//            if (product != null) {
//                if (product instanceof Aircraft) {
//                    gimbal = ((Aircraft) product).getGimbals().get(currentGimbalId);
//                } else {
//                    gimbal = product.getGimbal();
//                }
//            }
//        }
//    }

    private void initGimbal() {
        if (DJISDKManager.getInstance() != null) {
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product != null) {
                gimbal = product.getGimbal();
            }
        }
    }

    private float getDroneHeading() {
        return mFlightController.getCompass().getHeading();
    }

    private void pointDroneToTrueNorth() {
        try {
            gimbal.setMode(GimbalMode.YAW_FOLLOW, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    if (error == null) {
                        Toast.makeText(getContext(), "pointToNorth- Gimbal Mode set to YAW_FOLLOW", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "pointToNorth- Gimbal mode cannot be set to YAW_FOLLOW", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            final float droneHeading = getDroneHeading();
            Toast.makeText(getContext(), "pointToNorth- Current AC Heading is: " + droneHeading, Toast.LENGTH_LONG).show();
            final Rotation.Builder rotateToTrueNorthBuilder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(1);
            rotateToTrueNorthBuilder.roll(0);
            rotateToTrueNorthBuilder.pitch(0);
            float rotateTo = droneHeading * (-1);
            Toast.makeText(getContext(), "pointToNorth - AC will rotate to: " + rotateTo, Toast.LENGTH_LONG).show();
            rotateToTrueNorthBuilder.yaw(rotateTo);
            gimbal.rotate(rotateToTrueNorthBuilder.build(), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        Toast.makeText(getContext(), "Drone rotated to true north. Current Drone Heading:  " + getDroneHeading(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Drone could not be rotated to North", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "pointToNorth - Gimbal is Null", Toast.LENGTH_SHORT).show();
        }


    }


    //====================================================================================================

    //ALL THE GETTERS AND SETTERS
    public static LatLng getOrigin() {
        return origin;
    }

    public static LatLng getTie_point() {
        return tie_point;
    }

    public Double getHeadingCalc() {
        return headingCalc;
    }

    public static void setOrigin(LatLng origin) {
        Heading.origin = origin;
    }

    public static void setTie_point(LatLng tie_point) {
        Heading.tie_point = tie_point;
    }

    public void setOriginStr(String originStr) {
        this.originStr = originStr;
    }

    public void setTie_pointStr(String tie_pointStr) {
        this.tie_pointStr = tie_pointStr;
    }

    public void setCurLatLon(LatLng curLatLon) {
        this.curLatLon = curLatLon;
    }

    public void setDroneLatLon(LatLng droneLatLon) {
        this.droneLatLon = droneLatLon;
    }

    public void setHeadingCalc(Double headingCalc) {
        this.headingCalc = headingCalc;
    }

    public void setDroneLocationLat(double droneLocationLat) {
        this.droneLocationLat = droneLocationLat;
    }

    public void setDroneLocationLng(double droneLocationLng) {
        this.droneLocationLng = droneLocationLng;
    }

}
