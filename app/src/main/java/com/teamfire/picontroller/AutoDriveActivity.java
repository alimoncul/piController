package com.teamfire.picontroller;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;


public class AutoDriveActivity extends AppCompatActivity {

    Button btn_manualDrive, btn_camera;
    WebView wb_liveFeed;
    EditText ipAddress;
    MapView map = null;
    public static String wifiModuleIP;
    public static int wifiModulePort;
    public String newUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autodrive);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        btn_manualDrive = findViewById(R.id.btn_manualDrive);
        btn_camera = findViewById(R.id.btn_camera2);
        ipAddress = findViewById(R.id.ipAddress2);
        wb_liveFeed = findViewById(R.id.wb_liveFeed2);

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
                setResult(RESULT_OK, new Intent());
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
        IMapController mapController = map.getController();
        mapController.setZoom(7);
        GeoPoint startPoint = new GeoPoint(39.1667, 35.6667);
        mapController.setCenter(startPoint);

    }

    public void getIPandPort() {
        String iPandPort = ipAddress.getText().toString();
        String temp[] = iPandPort.split(":");
        wifiModuleIP = temp[0];
        wifiModulePort = Integer.valueOf(temp[1]);
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
}
