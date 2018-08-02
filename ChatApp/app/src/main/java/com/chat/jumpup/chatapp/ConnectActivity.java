package com.chat.jumpup.chatapp;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONException;
import org.json.JSONObject;

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
    private int length;    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    private GPSInfo gps;



    Button button;

    @Override
    protected void onRestart() {
        super.onRestart();
//        GPSPermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        gps = new GPSInfo(ConnectActivity.this);

        if(gps.isGetLocation()) {
            gps.getLocation();
            lat = gps.getLat();
            lng = gps.getLng();
        } else {
            gps.showSettingsAlert();
        }

        mHandler = new Handler();

//        GPSPermission();

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


        button = findViewById(R.id.btn_connect_start);
        button.setOnClickListener(clickFind);

        mSocket.on("join", joinData);
        mSocket.on("exit", exitRoom);
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
        if(requestCode == PERMISSIONS_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
        } else if( requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
            isAccessCoarseLocation = true;
        }

        if(isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }


//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(resultCode == Activity.RESULT_OK && requestCode == 1) {
//            if(EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//            }
//        } else {
//            EasyPermissions.onRequestPermissionsResult(this, getString(R.string.needToPermissions), );
//        }
//    }

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        boolean allowed = true;
//
//        switch (requestCode) {
//            case 0:
//                for (int res: grantResults) {
//                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
//                }
//                break;
//
//            default:
//                allowed = false;
//                break;
//        }
//        if(!allowed) {
//            Toast.makeText(getApplicationContext(), "권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
//        }
//    }

    private JSONObject setGPSData(double lat, double lng, int length) throws JSONException {
        JSONObject GPSData = new JSONObject();

        GPSData.put("lat", lat);
        GPSData.put("lng", lng);
        GPSData.put("length", length);

        Log.d("GPSData", "lat : " + lat + " ,lng" + lng );

        return GPSData;
    }
    private Emitter.Listener exitRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "상대방이 방을 떠났습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        };
    };

//    public void GPSPermission() {
//        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
//            if (hasPermissions()) {
////                Toast.makeText(getApplicationContext(), "이미 허용함.", Toast.LENGTH_LONG).show();
//            } else {
//                requestPerms();
//            }
//        } else {
////            Toast.makeText(getApplicationContext(), "이용할 수 없음.", Toast.LENGTH_LONG).show();
//        }
//    }
}