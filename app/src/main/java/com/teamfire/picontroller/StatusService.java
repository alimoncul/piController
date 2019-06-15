package com.teamfire.picontroller;

import android.app.IntentService;
import android.content.Intent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class StatusService extends IntentService {

    public StatusService() {
        super("StatusService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            DatagramSocket ds = new DatagramSocket(24999);
            ds.setReuseAddress(true);
            byte[] buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                ds.receive(packet);
                String message = new String(packet.getData()).trim();

                if (message.contains("OBSTACLE")) {
                    Intent intService = new Intent();
                    intService.setAction("com.teamfire.picontroller");
                    intService.putExtra("DATA", "Obstacle Detected please switch to manual control!");
                    sendBroadcast(intService);
                } else if (message.contains("TARGETSEND")) {
                    Intent intService = new Intent();
                    intService.setAction("com.teamfire.picontroller");
                    intService.putExtra("DATA", "Sending target coordinates...OK");
                    sendBroadcast(intService);
                } else if (message.contains("VEHICLESTARTED")) {
                    Intent intService = new Intent();
                    intService.setAction("com.teamfire.picontroller");
                    intService.putExtra("DATA", "Vehicle started moving.");
                    sendBroadcast(intService);
                } else if (message.contains("TURNING")) {
                    Intent intService = new Intent();
                    intService.setAction("com.teamfire.picontroller");
                    intService.putExtra("DATA", "Vehicle started rotating to target!");
                    sendBroadcast(intService);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
