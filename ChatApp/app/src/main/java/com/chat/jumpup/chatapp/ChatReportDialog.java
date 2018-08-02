package com.chat.jumpup.chatapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class ChatReportDialog extends Dialog {

    public static EditText chatReportEdit;
    private Button chatReportBtn;
    private ImageButton chatReportCancelBtn;
    private View.OnClickListener mReportClickListener, mCancelClickListener;


    public ChatReportDialog(@NonNull Context context, View.OnClickListener reportClickListener, View.OnClickListener cancelClickListener) {
        super(context);
        this.mReportClickListener = reportClickListener;
        this.mCancelClickListener = cancelClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.dialog_chat_report);

        chatReportEdit = (EditText)findViewById(R.id.edit_chat_report);
        chatReportBtn = (Button)findViewById(R.id.btn_chat_report);
        chatReportCancelBtn = (ImageButton)findViewById(R.id.btn_chat_report_cancel);


        if (mReportClickListener != null) {
            chatReportBtn.setOnClickListener(mReportClickListener);
        }
        if(mCancelClickListener != null){
            chatReportCancelBtn.setOnClickListener(mCancelClickListener);
        }

    }
}