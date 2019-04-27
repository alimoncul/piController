package com.teamfire.picontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoDriveActivity extends AppCompatActivity {

    public static String wifiModuleIP;
    public static int wifiModulePort;
    public String newUrl;
    private boolean isClientRunning = false;
    public static int CMD = 99;
    public static String targetLocation;
    public static int UDP_LocationReceivingPort = 11445;
    Button btn_manualDrive, btn_camera, btn_showCoordinates, btn_GPS;
    WebView wb_liveFeed;
    EditText ipAddress, mapLat, mapLon;
    MapView map = null;
    DatagramSocket ds = null;
    String gpsresponse;


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
        mapController.setZoom(9.0d);
        final GeoPoint startPoint = new GeoPoint(40.1022, 26.5167);
        mapController.setCenter(startPoint);

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
                try {
                    GeoPoint markerPoint = new GeoPoint(Double.parseDouble(mapLat.getText().toString()), Double.parseDouble(mapLon.getText().toString()));
                    startMarker.setPosition(markerPoint);
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    startMarker.setTitle("Target location");
                    map.getOverlays().add(startMarker);
                    map.invalidate();

                    getIPandPort();
                    new SendTargetAsyncTask().execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btn_GPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isClientRunning) {
                    Log.e("UDP", "onClick: Reading UDP Packet...");
                    new ReceivePiLocation().execute();
                } else {
                    Log.e("UDP", "onClick: A Thread already started and not finished.");
                }
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

    public void setMapView(String result) {
        //parse the result values
        String regex = "(.)*(\\d)(.)*";
        Pattern pattern = Pattern.compile(regex);
        try {
            boolean containsNumber = pattern.matcher(result).matches();
            double lat = 0;
            double lon = 0;
            double header = 0;
            if (containsNumber) {
                String[] coords = result.split("/");
                lat = Double.parseDouble(coords[0]);
                lon = Double.parseDouble(coords[1]);
                String tempHeader = coords[2];
                Pattern p = Pattern.compile("\\d*\\.\\d+");
                Matcher m = p.matcher(tempHeader);
                while (m.find()) {
                    header = Double.parseDouble(m.group());
                }

                final IMapController mapViewController = map.getController();
                mapViewController.setCenter(new GeoPoint(lat, lon));
                mapViewController.setZoom(15.0d);

                //add the gps location marker
                Marker GPSMarker = new Marker(map);
                final GeoPoint markerPointGPS = new GeoPoint(lat, lon);
                GPSMarker.setPosition(markerPointGPS);
                GPSMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                GPSMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.gps_marker));
                GPSMarker.setTitle("Device Location");
                map.getOverlays().add(GPSMarker);
                map.invalidate();
            } else {
                Toast.makeText(getApplicationContext(), "Can't read GPS data.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public class ReceivePiLocation extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            isClientRunning = true;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            byte[] lMsg = new byte[64];
            DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
            try {
                isClientRunning = true;
                ds = new DatagramSocket(UDP_LocationReceivingPort);
                ds.setSoTimeout(7000);
                ds.setReuseAddress(true);
                ds.receive(dp);
                gpsresponse = new String(dp.getData());
                Log.e("UDP", "run: paket alındı!" + gpsresponse);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                    isClientRunning = false;
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean state) {
            if (state) {
                setMapView(gpsresponse);
            }
        }
    }
    public class SendTargetAsyncTask extends AsyncTask<Void, Void, Void> {
        Socket socket;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InetAddress inetAddress = InetAddress.getByName(AutoDriveActivity.wifiModuleIP);
                socket = new java.net.Socket(inetAddress, 10201);
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                targetLocation = mapLat.getText().toString() + "/" + mapLon.getText().toString();
                printStream.print(targetLocation);
                printStream.close();
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}