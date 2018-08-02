package com.chat.jumpup.chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    private ImageButton messageSendButton;
    private JSONObject data;
    private String yourMessage, myMessage;

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
        chatRecyclerItems = new ArrayList();
        data = new JSONObject();
        chatRecycler.setLayoutManager(layoutManager);
        chatRecycler.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerAdapter = new ChatRecyclerAdapter(chatRecyclerItems);
        chatRecycler.setAdapter(chatRecyclerAdapter);

        socket.on("message",receiveMassage);

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
    }

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
}
