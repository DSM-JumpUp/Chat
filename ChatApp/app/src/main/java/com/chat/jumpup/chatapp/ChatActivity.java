package com.chat.jumpup.chatapp;

import android.content.Intent;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.chat.jumpup.chatapp.ChatRecyclerAdapter.VIEW_TYPE_MY_MESSAGE;
import static com.chat.jumpup.chatapp.ChatRecyclerAdapter.VIEW_TYPE_YOUR_MESSAGE;

public class ChatActivity extends AppCompatActivity {

    private Socket socket;
    private TextView nicknameTextView;
    private EditText messageEditText;
    private RecyclerView chatRecycler;
    private LinearLayoutManager layoutManager;
    private ChatRecyclerAdapter chatRecyclerAdapter;
    private ArrayList<ChatRecyclerItem> chatRecyclerItems = null;
    private ImageButton messageSendButton, plusButton, cancelButton, leaveRoomButton, reportButton;
    private ConstraintLayout bottomSheetLayout;
    private JSONObject data;
    private String yourMessage, myMessage;
    private Intent intent;
    private ChatReportDialog chatReportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SocketApplication app = (SocketApplication) getApplication();
        socket = app.getSocket();
        nicknameTextView = (TextView) findViewById(R.id.text_chat_nickname);
        messageEditText = (EditText) findViewById(R.id.edit_chat_message);
        chatRecycler = (RecyclerView)findViewById(R.id.recycler_chat);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messageSendButton = (ImageButton) findViewById(R.id.btn_chat_send_message);
        plusButton = (ImageButton)findViewById(R.id.btn_chat_plus);
        cancelButton = (ImageButton)findViewById(R.id.btn_chat_cancel);
        leaveRoomButton = (ImageButton)findViewById(R.id.btn_chat_leave);
        reportButton = (ImageButton)findViewById(R.id.btn_chat_report);
        bottomSheetLayout = (ConstraintLayout)findViewById(R.id.layout_bottom_sheet);
        chatRecyclerItems = new ArrayList();
        data = new JSONObject();
        chatRecycler.setLayoutManager(layoutManager);
        chatRecycler.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerAdapter = new ChatRecyclerAdapter(chatRecyclerItems);
        chatRecycler.setAdapter(chatRecyclerAdapter);
        intent = getIntent();

        socket.on("message",receiveMassage);
        socket.on("ip",ip);

        nicknameTextView.setText(intent.getStringExtra("peerName"));

        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(!TextUtils.isEmpty(messageEditText.getText().toString().trim())) {
                        myMessage = messageEditText.getText().toString();
                        data.put("message",myMessage);
                        socket.emit("message",data);
                        chatRecyclerItems.add(new ChatRecyclerItem.Builder().Build(VIEW_TYPE_MY_MESSAGE, myMessage));
                        //채팅 목록 갱신
                        chatRecyclerAdapter.notifyDataSetChanged();
                        //채팅 목록이 가장 최근 메시지를 가르키도록 설정
                        chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                        messageEditText.getText().clear();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //plusButton을 클릭하였을 경우 숨겨진 버튼들이 보여짐
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetLayout.setVisibility(View.VISIBLE);
                plusButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });

        //cancelButton을 클릭하였을 경우 보여졌던 버튼들이 다시 숨겨짐
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetLayout.setVisibility(View.GONE);
                plusButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
            }
        });

        //채팅방 나가기 버튼을 클릭하였을 경우 Server로 leave room전송 후 ConnectActivity로 이동
        leaveRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("leave room");
                Log.d("Debug","leave Button clicked");
                Intent intent = new Intent(ChatActivity.this, ConnectActivity.class);
                startActivity(intent);
            }
        });

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatReportDialog = new ChatReportDialog(ChatActivity.this, reportClickListener, cancelClickListener);
                chatReportDialog.setCancelable(true);
                chatReportDialog.getWindow().setGravity(Gravity.CENTER);
                chatReportDialog.show();
            }
        });
    }

    private View.OnClickListener reportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("siren");
            Toast.makeText(getApplicationContext(),"신고가 완료되었습니다.",Toast.LENGTH_SHORT).show();
            chatReportDialog.dismiss();
        }
    };

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            chatReportDialog.dismiss();
        }
    };


    private Emitter.Listener receiveMassage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0]; //서버로부터 받는 JSON 데이터
                    try {
                        //yourMessage에 서버로부터 받은 데이터에서 message를 얻어와 채팅 목록에 띄워주기
                        yourMessage = data.getString("message");
                        chatRecyclerItems.add(new ChatRecyclerItem.Builder().Build(VIEW_TYPE_YOUR_MESSAGE, yourMessage));
                        //채팅 목록 갱신
                        chatRecyclerAdapter.notifyDataSetChanged();
                        //채팅 목록이 가장 최근 메시지를 가르키도록 설정
                        chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener ip = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject ipData = new JSONObject();
                    Log.d("Debug","on ip");
                    try {
                        ipData.put("ip",getLocalIpAddress());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    socket.emit("ip",ipData);
                }
            });
        }
    };
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
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
