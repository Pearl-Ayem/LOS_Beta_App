package com.dji.uxsdkdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dji.mapkit.maps.DJIMap;
import com.google.android.gms.maps.GoogleMap;

import static com.dji.uxsdkdemo.R.drawable.default_map_grey;
import static com.dji.uxsdkdemo.R.drawable.default_map_yellow;
import static com.dji.uxsdkdemo.R.drawable.satellite_map_type_grey;
import static com.dji.uxsdkdemo.R.drawable.satellite_map_type_yellow;
import static com.dji.uxsdkdemo.R.drawable.terrain_map_grey;
import static com.dji.uxsdkdemo.R.drawable.terrain_map_yellow;

/**
 * Created by Pearl on 2018-07-31.
 */

public class MapTypeDialog extends DialogFragment {
    private static final String TAG = "MapTypeDialog";

    //widgets
    private ImageButton defaultMap;
    private ImageButton satelliteMap;
    private ImageButton terrainMap;
    private TextView defaultText;
    private TextView satelliteText;
    private TextView terrainText;
    int yellow;
    int gray;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_types_dialog, container, false);

        defaultMap = view.findViewById(R.id.defaultMapType);
        satelliteMap = view.findViewById(R.id.satelliteMapType);
        terrainMap = view.findViewById(R.id.terrainMapType);
        defaultText = view.findViewById(R.id.defaultMap);
        satelliteText = view.findViewById(R.id.satelliteMap);
        terrainText = view.findViewById(R.id.terrainMap);
        yellow = getResources().getColor(R.color.reply_yellow);
        gray = getResources().getColor(R.color.pallett_gray);


        defaultMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                int type = GoogleMap.MAP_TYPE_NORMAL;
                defaultMap.setImageResource(default_map_yellow);
                terrainMap.setImageResource(terrain_map_grey);
                satelliteMap.setImageResource(satellite_map_type_grey);
                defaultText.setTextColor(yellow);
                terrainText.setTextColor(gray);
                satelliteText.setTextColor(gray);
                MapsActivity.mMap.setMapType(type);
            }
        });

        satelliteMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                defaultMap.setImageResource(default_map_grey);
                terrainMap.setImageResource(terrain_map_grey);
                satelliteMap.setImageResource(satellite_map_type_yellow);
                defaultText.setTextColor(gray);
                terrainText.setTextColor(gray);
                satelliteText.setTextColor(yellow);
                MapsActivity.mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        terrainMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                defaultMap.setImageResource(default_map_grey);
                terrainMap.setImageResource(terrain_map_yellow);
                satelliteMap.setImageResource(satellite_map_type_grey);
                defaultText.setTextColor(gray);
                terrainText.setTextColor(yellow);
                satelliteText.setTextColor(gray);
                MapsActivity.mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });




        return view;
    }

    public void onResume()
    {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 500);
        window.setGravity(Gravity.CENTER);
    }
}
