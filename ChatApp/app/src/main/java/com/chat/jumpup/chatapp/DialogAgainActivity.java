package com.chat.jumpup.chatapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class DialogAgainActivity extends Dialog {
    private Button reconnectBtn;
    private ImageButton cancelBtn;
    private int cannotConnectText, findAgaintext;
    private TextView conText, conText2;
    private View.OnClickListener againClick;
    private View.OnClickListener againSearch;

    public DialogAgainActivity(@NonNull Context context, int cannotConnectText, int findAgaintext, View.OnClickListener againCancelClickListener, View.OnClickListener againFindPeople) {
        super(context);
        this.cannotConnectText = cannotConnectText;
        this.findAgaintext = findAgaintext;
        this.againClick = againCancelClickListener;
        this.againSearch = againFindPeople;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_connect_again);

        reconnectBtn = findViewById(R.id.btn_connect_again);
        cancelBtn = findViewById(R.id.btn_dialog_connect_again_cancel);
        conText = findViewById(R.id.text_connect_again_cannot_connect);
        conText2 = findViewById(R.id.text_connect_again_find_again);

        conText.setText(cannotConnectText + "m이내 사람들을 찾지 못하였습니다.");
        conText2.setText(findAgaintext + "m이내로 다시 탐색하겠습니까?");

        cancelBtn.setOnClickListener(againClick);
        reconnectBtn.setOnClickListener(againSearch);
    }
}
