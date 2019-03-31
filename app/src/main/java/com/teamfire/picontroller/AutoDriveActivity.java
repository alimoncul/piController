package com.teamfire.picontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class AutoDriveActivity extends AppCompatActivity {

    public static String wifiModuleIP;
    public static int wifiModulePort;
    public String newUrl;
    Button btn_manualDrive, btn_camera, btn_showCoordinates, btn_GPS;
    WebView wb_liveFeed;
    EditText ipAddress, mapLat, mapLon;
    MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autodrive);

        btn_manualDrive = findViewById(R.id.btn_manualDrive);
        btn_camera = findViewById(R.id.btn_camera);
        btn_showCoordinates = findViewById(R.id.btn_ShowCoordinates);
        btn_GPS = findViewById(R.id.btn_GPS);
        mapLat = findViewById(R.id.mapLat);
        mapLon = findViewById(R.id.mapLon);
        ipAddress = findViewById(R.id.ipAddress);
        wb_liveFeed = findViewById(R.id.wb_liveFeed);
        ipAddress.setText(getIntent().getStringExtra("IP_ADDRESS"));

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIPandPort();
                wb_liveFeed.getSettings().setJavaScriptEnabled(true);
                wb_liveFeed.getSettings().setUseWideViewPort(true);
                wb_liveFeed.getSettings().setLoadWithOverviewMode(true);
                wb_liveFeed.setWebViewClient(new WebViewClient());
                newUrl = "http://" + wifiModuleIP + ":8000/index.html";
                wb_liveFeed.loadData("<iframe src='" + newUrl + "' style='border: 0; width: 100%; height: 100%'></iframe>", "text/html; charset=utf-8", "UTF-8");
            }
        });

        btn_manualDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredIPAddress = ipAddress.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("IP_ADDRESS_FROM_AUTODRIVE", enteredIPAddress);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        //inflate and create the map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        //default map view point
        final IMapController mapController = map.getController();
        mapController.setZoom(9);
        final GeoPoint startPoint = new GeoPoint(40.1022, 26.5167);
        mapController.setCenter(startPoint);

        //GPS location marker
        Marker GPSMarker = new Marker(map);
        final GeoPoint markerPointGPS = new GeoPoint(40.15311283875724, 26.41108989715576);
        GPSMarker.setPosition(markerPointGPS);
        GPSMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        GPSMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.gps_marker));
        GPSMarker.setTitle("Device Location");
        map.getOverlays().add(GPSMarker);
        map.invalidate();

        //marker placement
        final Marker startMarker = new Marker(map);
        final MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                mapLat.setText(String.valueOf(p.getLatitude()));
                mapLon.setText(String.valueOf(p.getLongitude()));
                GeoPoint markerPoint = new GeoPoint(p.getLatitude(), p.getLongitude());
                startMarker.setPosition(markerPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle("Target location");
                map.getOverlays().add(startMarker);
                map.invalidate();
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        map.getOverlays().add(new MapEventsOverlay(mReceive));

        btn_showCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapLat.getText() != null && mapLon.getText() != null) {
                    GeoPoint markerPoint = new GeoPoint(Double.parseDouble(mapLat.getText().toString()), Double.parseDouble(mapLon.getText().toString()));
                    startMarker.setPosition(markerPoint);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    startMarker.setTitle("Target location");
                    map.getOverlays().add(startMarker);
                    map.invalidate();
                }
            }
        });

        btn_GPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.setCenter(markerPointGPS);
            }
        });
    }

    public void getIPandPort() {
        String iPandPort = ipAddress.getText().toString();
        String temp[] = iPandPort.split(":");
        wifiModuleIP = temp[0];
        wifiModulePort = Integer.valueOf(temp[1]);
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getSharedPreferences("IP", MODE_PRIVATE).edit();
        editor.putString("IP", ipAddress.getText().toString());
        editor.commit();
    }

}
