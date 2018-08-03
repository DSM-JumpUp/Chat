package com.chat.jumpup.chatapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ChatRecyclerYourMessageViewHolder extends RecyclerView.ViewHolder{
    public TextView chatMessageText, chatTimeText;

    public ChatRecyclerYourMessageViewHolder(View itemView) {
        super(itemView);

        chatMessageText = (TextView)itemView.findViewById(R.id.text_chat_your_message);
        chatTimeText = (TextView)itemView.findViewById(R.id.text_chat_your_time);
    }
}
