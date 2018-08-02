package com.chat.jumpup.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ChatRecyclerViewHodler> {

    public static final int VIEW_TYPE_MY_MESSAGE = 0;
    public static final int VIEW_TYPE_YOUR_MESSAGE = 1;
    public static final int VIEW_TYPE_LEAVE_MESSAGE = 2;

    private ArrayList<ChatRecyclerItem> chatRecyclerItems;

    public ChatRecyclerAdapter(ArrayList itemList) {
        chatRecyclerItems = itemList;
    }


    @NonNull
    @Override
    public ChatRecyclerViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = -1;

        switch (viewType){
            case VIEW_TYPE_MY_MESSAGE: // 내 채팅
                layout = R.layout.item_chat_recycler_my_message; break;
            case VIEW_TYPE_YOUR_MESSAGE: // 상대방 채팅
                layout = R.layout.item_chat_recycler_your_message; break;
            case VIEW_TYPE_LEAVE_MESSAGE: // 상대방에 채팅을 나갔을 때
                layout = R.layout.item_chat_recycler_leave_message; break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ChatRecyclerViewHodler(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRecyclerViewHodler holder, int position) {
        ChatRecyclerItem chatRecyclerItem = chatRecyclerItems.get(position);
        holder.chatMessage.setText(chatRecyclerItem.getMessageText());
        holder.chatTime.setText(chatRecyclerItem.getTimeText());
    }

    @Override
    public int getItemCount() {
        return chatRecyclerItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatRecyclerItems.get(position).getItemViewType();
    }

    class ChatRecyclerViewHodler extends RecyclerView.ViewHolder {
        TextView chatMessage;
        TextView chatTime;

        public ChatRecyclerViewHodler(View itemView) {
            super(itemView);
            chatMessage = itemView.findViewById(R.id.text_chat_message);
            chatTime = itemView.findViewById(R.id.text_chat_time);
        }
    }
}
