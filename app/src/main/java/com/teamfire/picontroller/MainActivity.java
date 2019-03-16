package com.teamfire.picontroller;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {

    Button btn_down, btn_up, btn_left, btn_right, btn_emergency;
    EditText ipAddress;
    public static String wifiModuleIP;
    public static int wifiModulePort;
    public static int CMD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_down = findViewById(R.id.btn_down);
        btn_up = findViewById(R.id.btn_up);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        btn_emergency = findViewById(R.id.btn_emergencyStop);
        ipAddress = findViewById(R.id.ipAddress);


        btn_emergency.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_emergency.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.emergencyPressed));
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_emergency.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.emergency));
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });

        btn_up.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_up.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.forwardPressed));
                        btn_down.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_up.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.forward));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_down.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 8;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });

        btn_down.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_down.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.backwardPressed));
                        btn_up.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_down.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.backward));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_up.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 2;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });

        btn_right.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_right.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safePressed));
                        btn_left.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_right.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safe));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_left.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 6;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });

        btn_left.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_left.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safePressed));
                        btn_right.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_left.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safe));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_right.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 4;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });
    }

    public void getIPandPort() {
        String iPandPort = ipAddress.getText().toString();
        String temp[] = iPandPort.split(":");
        wifiModuleIP = temp[0];
        wifiModulePort = Integer.valueOf(temp[1]);
    }

    public class SocketAsyncTask extends AsyncTask<Void, Void, Void> {
        Socket socket;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InetAddress inetAddress = InetAddress.getByName(MainActivity.wifiModuleIP);
                socket = new java.net.Socket(inetAddress, MainActivity.wifiModulePort);
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                printStream.print(CMD);
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
