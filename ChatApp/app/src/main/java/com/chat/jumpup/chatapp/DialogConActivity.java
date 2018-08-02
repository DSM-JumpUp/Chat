package com.chat.jumpup.chatapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class DialogConActivity extends Dialog {

    private View.OnClickListener mCancelClick;
    private ImageButton cancelBtn;
    private int length;
    private TextView findingText;

    public DialogConActivity(@NonNull Context context, View.OnClickListener onCancel, int length) {
        super(context);
        this.mCancelClick = onCancel;
        this.length = length;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_connect);

        cancelBtn = findViewById(R.id.btn_dialog_connect_cancel);
        findingText.setText(length + "이내 사람들을 찾는 중입니다.");

        cancelBtn.setOnClickListener((View.OnClickListener) mCancelClick);
    }
}
