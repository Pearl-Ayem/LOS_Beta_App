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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
    private ImageView moreOrg, moreDest;
    private static final String[] dropdown = new String[]{"Base Location", "Drone Location"};

    private LatLng origin;
    private LatLng tie_point;
    private Double headingCalc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.heading_fragment, container, false);

        mActionCancel = view.findViewById(R.id.action_cancel);
        mActionOk = view.findViewById(R.id.action_ok);
        mHeadingOrigin = view.findViewById(R.id.origin);
        mHeadingDest =  view.findViewById(R.id.dest);
        mHeading = view.findViewById(R.id.heading);
        moreOrg = view.findViewById(R.id.more_options_origin);
        moreDest = view.findViewById(R.id.more_options_dest);

        ArrayAdapter<String>adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line, dropdown);
        mHeadingOrigin.setAdapter(adapter);

        ArrayAdapter<String>adapter2 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line, dropdown);
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



        if (((MapsActivity) getActivity()).getHeadingOrg()!= null) {
            origin = ((MapsActivity) getActivity()).getHeadingOrg();
            mHeadingOrigin.setText(makeLatLonStr(origin));
        } else {
            origin = null;
        }

        if (((MapsActivity) getActivity()).getHeadingDest() !=null) {
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
                mOnInputListener.sendInput(origin,tie_point, headingCalc);
                getDialog().dismiss();
            }
        });

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
        double heading = computeHeading(origin,tie_point);
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

    public String makeLatLonStr(LatLng ll){
        double lat = ll.latitude;
        double lon = ll.longitude;
        String out = lat + "," + lon;
        return out;
    }

}
