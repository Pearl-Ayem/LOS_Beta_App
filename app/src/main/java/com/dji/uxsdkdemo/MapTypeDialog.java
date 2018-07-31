package com.dji.uxsdkdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.dji.mapkit.maps.DJIMap;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Pearl on 2018-07-31.
 */

public class MapTypeDialog extends DialogFragment {
    private static final String TAG = "MapTypeDialog";

    //widgets
    private ImageButton defaultMap;
    private ImageButton satelliteMap;
    private ImageButton terrainMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_types_dialog, container, false);

        defaultMap = view.findViewById(R.id.defaultMapType);
        satelliteMap = view.findViewById(R.id.satelliteMapType);
        terrainMap = view.findViewById(R.id.terrainMapType);

        defaultMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int type = GoogleMap.MAP_TYPE_NORMAL;
                MapsActivity.mMap.setMapType(type);
            }
        });

        satelliteMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsActivity.mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        terrainMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapsActivity.mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });




        return view;
    }
}
