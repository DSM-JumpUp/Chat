package com.chat.jumpup.chatapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ChatRecyclerMyMessageViewHolder extends RecyclerView.ViewHolder {
    public TextView chatMessageText, chatTimeText;

    public ChatRecyclerMyMessageViewHolder(View itemView) {
        super(itemView);

        chatMessageText = (TextView) itemView.findViewById(R.id.text_chat_my_message);
        chatTimeText = (TextView) itemView.findViewById(R.id.text_chat_my_time);
    }
}
