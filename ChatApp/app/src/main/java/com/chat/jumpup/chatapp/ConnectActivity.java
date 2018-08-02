package com.chat.jumpup.chatapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ConnectActivity extends AppCompatActivity {
    SocketApplication socketApp;
    Socket mSocket;
    private DialogConActivity dialogConActivity;
    private DialogFailureActivity dialogFailureActivity;
    private DialogAgainActivity dialogAgainActivity;
    private Handler mHandler;
    private int cannot, findAgain;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private double lat, lng;
    private int length;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;
    private JSONObject data;

    private GPSInfo gps;
    private Button startConnectButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        gps = new GPSInfo(ConnectActivity.this);
        data = new JSONObject();

        if (gps.isGetLocation()) {
            gps.getLocation();
            lat = gps.getLat();
            lng = gps.getLng();
        } else {
            gps.showSettingsAlert();
        }

        mHandler = new Handler();

        socketApp = (SocketApplication) getApplication();

        mSocket = socketApp.getSocket();
        mSocket.connect();

        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect timeout");
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect error");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("disconnect");
            }
        });


        startConnectButton = findViewById(R.id.btn_connect_start);
        startConnectButton.setOnClickListener(clickFind);

        mSocket.on("join", joinData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.emit("disconnect");
    }

    private Emitter.Listener joinData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject JO = (JSONObject) args[0];

            try {
                final String peerName = JO.getString("name");
                final String roomName = JO.getString("roomName");

                Log.i("join", peerName + " : " + roomName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ConnectActivity.this, ChatActivity.class);
                        intent.putExtra("peerName", peerName);
                        intent.putExtra("roomName", roomName);
                        dialogConActivity.dismiss();
                        mHandler.removeMessages(0);
                        startActivity(intent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private View.OnClickListener clickFind = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            try {
                data.put("ip", getLocalIpAddress());
                Log.d("Debug", "ip : " + getLocalIpAddress());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Server로 ip check전송
            mSocket.emit("ip check", data);
            //Server로 부터 block 받았을 경우 block메서드 실행
            mSocket.on("block", block);
            gps.getLocation();
            lat = gps.getLat();
            lng = gps.getLng();
            length = 100;

            try {
                ViewOnConDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cannot = 100;
            findAgain = 200;
        }
    };

    private void ViewOnConDialog() throws JSONException {
        dialogConActivity = new DialogConActivity(ConnectActivity.this, cancelClickListener, length);
        dialogConActivity.setCancelable(true);
        dialogConActivity.getWindow().setGravity(Gravity.CENTER);
        dialogConActivity.show();
        mSocket.emit("search", setGPSData(lat, lng, length));

        Runnable TimeOver = new Runnable() {
            @Override
            public void run() {
                if (cannot <= 200 && findAgain <= 400) {
                    mSocket.emit("pop queue");
                    dialogConActivity.dismiss();
                    connectAgainDialog(cannot, findAgain);
                } else {
                    mSocket.emit("pop queue");
                    dialogConActivity.dismiss();
                    connectFailDialog();
                }
            }
        };
        mHandler.postDelayed(TimeOver, 10000);
    }

    private void connectAgainDialog(int cannotFind, int againFind) {
        dialogAgainActivity = new DialogAgainActivity(ConnectActivity.this, cannotFind, againFind, againCancelClickListener, againFindPeople);
        dialogAgainActivity.setCancelable(true);
        dialogAgainActivity.getWindow().setGravity(Gravity.CENTER);
        dialogAgainActivity.show();

        cannot = cannot * 2;
        findAgain = findAgain * 2;
    }

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSocket.emit("pop queue");
            dialogConActivity.dismiss();
            mHandler.removeMessages(0);
        }
    };

    private View.OnClickListener againCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSocket.emit("pop queue");
            dialogAgainActivity.dismiss();
        }
    };

    private View.OnClickListener againFindPeople = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialogAgainActivity.dismiss();
            length = length * 2;
            try {
                ViewOnConDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private void connectFailDialog() {
        dialogFailureActivity = new DialogFailureActivity(ConnectActivity.this, failCancelClickListener);
        dialogFailureActivity.setCancelable(true);
        dialogFailureActivity.getWindow().setGravity(Gravity.CENTER);
        dialogFailureActivity.show();
        mSocket.emit("pop queue");
    }

    private View.OnClickListener failCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogFailureActivity.dismiss();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    private JSONObject setGPSData(double lat, double lng, int length) throws JSONException {
        JSONObject GPSData = new JSONObject();

        GPSData.put("lat", lat);
        GPSData.put("lng", lng);
        GPSData.put("length", length);

        Log.d("GPSData", "lat : " + lat + " ,lng" + lng);

        return GPSData;
    }

    //block된 유저일 경우 실행하는 메서드
    private Emitter.Listener block = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "당신은 블록된 유저입니다.", Toast.LENGTH_SHORT).show();
                    Runnable mMyTask = new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    };
                    mHandler.postDelayed(mMyTask, 1500);

                }
            });
        }
    };

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}