package com.chat.jumpup.chatapp;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ChatRecyclerLeaveMessageViewHolder extends RecyclerView.ViewHolder {
    public TextView leaveMessageText;

    public ChatRecyclerLeaveMessageViewHolder(View itemView) {
        super(itemView);

        leaveMessageText = (TextView) itemView.findViewById(R.id.text_chat_message_nickname);
    }
}
