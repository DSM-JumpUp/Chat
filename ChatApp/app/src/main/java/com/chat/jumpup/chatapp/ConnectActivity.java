package com.chat.jumpup.chatapp;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ConnectActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private Socket mSocket;
    private Button connectStartButton;
    private GPSInfo gpsInfo;

    private double lat;
    private double lng;
    private int length;

    private int cannot;
    private int findAgain;

    private Handler mHandler;

    private DialogConActivity dialogConActivity;
    private DialogAgainActivity dialogAgainActivity;
    private DialogFailureActivity dialogFailureActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mHandler = new Handler();

        PermissionCheck();
        gpsInfo = new GPSInfo(ConnectActivity.this);

        mSocket = SocketApplication.getSocket();
        mSocket.connect();

        connectStartButton = (Button) findViewById(R.id.btn_connect_start);
        connectStartButton.setOnClickListener(connectStart);

        mSocket.on("join", joinData);

    }

    private View.OnClickListener connectStart = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(gpsInfo.getLocation() != null) {
                Log.e("GPS", gpsInfo.getLatitude() + " " + gpsInfo.getLongitude());
                lat = gpsInfo.getLatitude();
                lng = gpsInfo.getLongitude();
                length = 100;

                try {
                    ViewOnConDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                cannot = 100;
                findAgain = 200;
            }
        }
    };

    private void PermissionCheck(){
        if(!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION)){
            EasyPermissions.requestPermissions(this, "앱에 필요한 권한을 부여해야 합니다!",
                    0, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions, grantResults, this);
    }

    private void ViewOnConDialog() throws JSONException {
        dialogConActivity = new DialogConActivity(ConnectActivity.this, cancelClickListener, length);
        dialogConActivity.setCancelable(true);
        dialogConActivity.getWindow().setGravity(Gravity.CENTER);
        dialogConActivity.show();
        mSocket.emit("search", setGPSData(lat, lng, length));

        Runnable TimeOver = new Runnable() {
            @Override
            public void run() {
                if (length <= 400) {
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

    private JSONObject setGPSData(double lat, double lng, int length) throws JSONException {
        JSONObject GPSData = new JSONObject();
        GPSData.put("lat", lat);
        GPSData.put("lng", lng);
        GPSData.put("length", length);

        return GPSData;
    };

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mSocket.emit("pop queue");
            dialogConActivity.dismiss();
            mHandler.removeMessages(0);
        }
    };

    private void connectAgainDialog(int cannotFind, int againFind) {
        dialogAgainActivity = new DialogAgainActivity(ConnectActivity.this, cannotFind, againFind, againCancelClickListener, againFindPeople);
        dialogAgainActivity.setCancelable(true);
        dialogAgainActivity.getWindow().setGravity(Gravity.CENTER);
        dialogAgainActivity.show();

        cannot = cannot * 2;
        findAgain = findAgain * 2;
    }

    private View.OnClickListener againCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSocket.emit("pop queue");
            dialogAgainActivity.dismiss();
        }
    };

    private  View.OnClickListener againFindPeople = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialogAgainActivity.dismiss();
            length = length * 2;
            try {
                ViewOnConDialog();
            } catch(JSONException e) {
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
        public void onClick(View view) {
            dialogFailureActivity.dismiss();
        }
    };

    private Emitter.Listener joinData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject JO = (JSONObject) args[0];

            try {
                final String peerName = JO.getString("name");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ConnectActivity.this, ChatActivity.class);
                        intent.putExtra("peerName", peerName);
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            new AppSettingsDialog.Builder(this).build().show();

    }
}
