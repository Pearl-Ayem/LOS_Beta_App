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

import static com.dji.uxsdkdemo.ToastUtils.showToast;
import static com.google.maps.android.SphericalUtil.computeHeading;

/**
 * Created by Pearl on 2018-07-26.
 */

public class Heading extends DialogFragment {
    private static final String TAG = "Heading Fragment";

    public interface onInputListener {
        void sendInput(LatLng o, LatLng d, Double h, Float gp, Float gr, Float gy);
    }

    public onInputListener mOnInputListener;
    private FusedLocationProviderClient hFusedLocationProviderClient;


    private AutoCompleteTextView mHeadingOrigin;
    private AutoCompleteTextView mHeadingDest;
    private TextView mHeading, mActionOk, mActionCancel;
    Spinner originSpinner, destSpinner;
    private Gimbal gimbal = null;


    private LatLng origin;
    private LatLng tie_point;
    private LatLng curLatLon;
    private LatLng droneLatLon;
    private Double headingCalc;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private FlightController mFlightController = null;
    private static String BASE_LOCATION = "Use Current Location";
    private static String DRONE_LOCATION = "Use Drone Location";
    private static String DUMMY = "";  //dummy spinner object
    private static final String[] dropdown = new String[]{DUMMY, BASE_LOCATION, DRONE_LOCATION};
    private Float gPitch;
    private Float gRoll;
    private Float gYaw;


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

        if (((MapsActivity) getActivity()).getHeadingOrg() != null) {
            origin = ((MapsActivity) getActivity()).getHeadingOrg();
            mHeadingOrigin.setText(makeLatLonStr(origin));
        }

        if (((MapsActivity) getActivity()).getHeadingDest() != null) {
            tie_point = ((MapsActivity) getActivity()).getHeadingDest();
            mHeadingDest.setText(makeLatLonStr(tie_point));
        }

        gYaw = ((MapsActivity) getActivity()).getGimYaw();
        gPitch = ((MapsActivity) getActivity()).getGimPitch();
        gRoll = ((MapsActivity) getActivity()).getGimRoll();

        updateHeading();


        if (gYaw == null && gPitch == null && gYaw == null) {
            pointDroneToTrueNorth();
            if (mFlightController != null) {
                Toast.makeText(getContext(), "Drone rotated to true north. Current Drone Heading:  " + mFlightController.getCompass().getHeading(), Toast.LENGTH_LONG).show();
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dropdown);
        originSpinner.setAdapter(adapter);
        originSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                try {
                    switch (position) {
                        case 0:
                            //do nothing
                            break;

                        case 1:
                            // Whatever you want to happen when the first item gets selected

                            useCurrentLocationForHeading();
                            origin = curLatLon;
                            mHeadingOrigin.setText(makeLatLonStr(origin));
                            updateHeading();
//                            pointDroneToTrueNorth();
//                            pointGimbalToTiePoint();

                            break;
                        case 2:
                            // Whatever you want to happen when the second item gets selected
                            mHeadingOrigin.setText("");
                            updateDroneLatLon();
                            origin = droneLatLon;
                            mHeadingOrigin.setText(makeLatLonStr(origin));
                            updateHeading();
                            Toast.makeText(getContext(), "Drone Location Selected: " + makeLatLonStr(origin), Toast.LENGTH_SHORT).show();
//                            pointDroneToTrueNorth();
//                            pointGimbalToTiePoint();
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


        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, dropdown);
        destSpinner.setAdapter(adapter2);
        destSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                try {
                    switch (position) {
                        case 0:
                            //do nothing
                            break;
                        case 1:
                            // Whatever you want to happen when the first item gets selected

                            useCurrentLocationForHeading();
                            mHeadingDest.setText(makeLatLonStr(curLatLon));
                            tie_point = curLatLon;
                            updateHeading();
//                            pointDroneToTrueNorth();
//                            pointGimbalToTiePoint();

                            break;
                        case 2:

                            // Whatever you want to happen when the second item gets selected
                            mHeadingDest.setText("");
                            updateDroneLatLon();
                            tie_point = droneLatLon;
                            mHeadingDest.setText(makeLatLonStr(tie_point));
                            updateHeading();
                            Toast.makeText(getContext(), "Drone Location Selected: " + makeLatLonStr(tie_point), Toast.LENGTH_SHORT).show();
//                            pointDroneToTrueNorth();
//                            pointGimbalToTiePoint();
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
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_NEXT
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    origin = convertoLatLon(textView.getText().toString());
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
                mOnInputListener.sendInput(origin, tie_point, headingCalc, gPitch, gRoll, gYaw);
                getDialog().dismiss();
            }
        });


        return view;
    }


    private void updateHeading() {
        try {
            double heading = getHeading(origin, tie_point);
            mHeading.setText(" Heading: " + heading);
            pointDroneToTrueNorth();
            pointGimbalToTiePoint();
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "updateHeading - NullPointer", Toast.LENGTH_SHORT).show();
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
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState
                                             djiFlightControllerCurrentState) {
                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    LatLng droneloc = new LatLng(droneLocationLat, droneLocationLng);
                    droneLatLon = droneloc;
                }
            });
        } else {
            Toast.makeText(getContext(), "FlightControllerNotSupported", Toast.LENGTH_SHORT).show();
        }
    }

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
            gRoll = builder.build().getRoll();

            builder.pitch(0);
            gPitch = builder.build().getPitch();

            Float newYaw = (headingCalc).floatValue();
            builder.yaw(newYaw);
            gYaw = builder.build().getYaw();

            sendRotateGimbalCommand(builder.build());

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "Null Pointer Found in pointGimbalToTiePoint", Toast.LENGTH_SHORT).show();
        }

    }


    private void sendRotateGimbalCommand(Rotation rotation) {
        Toast.makeText(getContext(), "In method sendRotateGimbalCommand ", Toast.LENGTH_SHORT).show();
        gimbal.rotate(rotation, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                Toast.makeText(getContext(), "Gimbal rotated to tie-point: " + gYaw, Toast.LENGTH_SHORT).show();
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
        float compassHeading = mFlightController.getCompass().getHeading();
        return compassHeading;
    }

    private void pointDroneToTrueNorth() {
        try {

            gimbal.setMode(GimbalMode.YAW_FOLLOW, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    if (error == null) {
                        Toast.makeText(getContext(), "pointDroneToTrueNorth- Gimbal Mode set to YAW_FOLLOW", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "pointDroneToTrueNorth- Gimbal mode cannot be set to YAW_FOLLOW", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            final float droneHeading = getDroneHeading();
            Toast.makeText(getContext(), "pointDroneToTrueNorth- Original AC Heading is: " + droneHeading, Toast.LENGTH_LONG).show();


            final Rotation.Builder rotateToTrueNorthBuilder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(1);

            rotateToTrueNorthBuilder.roll(0);
            rotateToTrueNorthBuilder.pitch(0);
            float rotateTo = droneHeading * (-1);
            Toast.makeText(getContext(), "pointDroneToTrueNorth- rotate this for North: " + rotateTo, Toast.LENGTH_LONG).show();
            rotateToTrueNorthBuilder.yaw(rotateTo);


            gimbal.rotate(rotateToTrueNorthBuilder.build(), new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        Toast.makeText(getContext(), "Point North success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Drone could not be rotated to North", Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "Gimbal is Null", Toast.LENGTH_SHORT).show();
        }


    }


}
