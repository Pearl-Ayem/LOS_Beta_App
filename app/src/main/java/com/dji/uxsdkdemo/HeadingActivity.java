//package com.dji.uxsdkdemo;
//
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.inputmethod.EditorInfo;
//import android.widget.AutoCompleteTextView;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//
//import static com.google.maps.android.SphericalUtil.computeHeading;
//
//public class HeadingActivity extends AppCompatActivity {
//
//    private Marker originMarker;
//    private Marker destMarker;
//    private  TextView mHeading;
//
//    static EditText mHeadingOrigin;
//    static EditText mHeadingDest;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_heading);
//        mHeadingOrigin = (EditText) findViewById(R.id.origin);
//        mHeadingDest = (EditText) findViewById(R.id.dest);
//        mHeading = (TextView) findViewById(R.id.heading);
//        initHeading();
//    }
//
//    private void initHeading(){
//        HeadingActivity.mHeadingOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (i == EditorInfo.IME_NULL
//                        || i == EditorInfo.IME_ACTION_DONE
//                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
//                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
//                    //execute our method for searching
//                    showHeading(textView);
//                }
//                return false;
//            }
//        });
//
//        HeadingActivity. mHeadingDest.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                if (i == EditorInfo.IME_NULL
//                        || i == EditorInfo.IME_ACTION_DONE
//                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
//                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
//                    //execute our method for searching
//                    showHeading(textView);
//                }
//                return false;
//            }
//        });
//    }
//
//    private double getHeading(String origin, String dest) {
//        addOriginMarker(origin);
//        addDestMarker(dest);
//        return computeHeading(convertoLatLon(origin), convertoLatLon(dest));
//    }
//
//    private void showHeading(View v) {
//        try {
//            String org = mHeadingOrigin.getText().toString();
//            String desti = mHeadingDest.getText().toString();
//            mHeading.setText(" Heading: " + getHeading(org, desti));
//        } catch (NumberFormatException e) {
//            mHeading.setText("Heading: ");
//        }
//    }
//
//    private LatLng convertoLatLon(String input) {
//        String[] latlonString = input.split(",");
//        double lat = Double.parseDouble(latlonString[0]);
//        double lon = Double.parseDouble(latlonString[1]);
//        LatLng latlon = new LatLng(lat, lon);
//        return latlon;
//    }
//
//    private void addOriginMarker(String originStr) {
//        if (originMarker != null) {
//            originMarker.remove();
//        }
//        LatLng origCoords = convertoLatLon(originStr);
//        MarkerOptions options = new MarkerOptions()
//                .position(origCoords)
//                .title("Origin")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
//                .draggable(true);
//        originMarker = MapsActivity.mMap.addMarker(options);
//    }
//
//    private void addDestMarker(String destStr) {
//        if (destMarker != null) {
//            destMarker.remove();
//        }
//
//        LatLng destCoords = convertoLatLon(destStr);
//        MarkerOptions options = new MarkerOptions()
//                .position(destCoords)
//                .title("Tie Point")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//                .draggable(true);
//        destMarker = MapsActivity.mMap.addMarker(options);
//    }
//}
