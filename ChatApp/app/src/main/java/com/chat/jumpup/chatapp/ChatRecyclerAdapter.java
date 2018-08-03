package com.chat.jumpup.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_MY_MESSAGE = 0;
    public static final int VIEW_TYPE_YOUR_MESSAGE = 1;
    public static final int VIEW_TYPE_LEAVE_MESSAGE = 2;

    private ArrayList<ChatRecyclerItem> chatRecyclerItems;

    public ChatRecyclerAdapter(ArrayList itemList) {
        chatRecyclerItems = itemList;
    }
    @Override
    public int getItemViewType(int position) {
        if (chatRecyclerItems.get(position).getItemViewType() == 0) {
            return VIEW_TYPE_MY_MESSAGE;
        } else if(chatRecyclerItems.get(position).getItemViewType() == 1){
            return VIEW_TYPE_YOUR_MESSAGE;
        } else {
            return VIEW_TYPE_LEAVE_MESSAGE;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_recycler_my_message, parent, false);
            return new ChatRecyclerMyMessageViewHolder(v);
        } else if (viewType == VIEW_TYPE_YOUR_MESSAGE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_recycler_your_message, parent, false);
            return new ChatRecyclerYourMessageViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_recycler_leave_message, parent, false);
            return new ChatRecyclerLeaveMessageViewHolder(v);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatRecyclerItem chatRecyclerItem = chatRecyclerItems.get(position);
        if (holder instanceof ChatRecyclerMyMessageViewHolder) {
            ((ChatRecyclerMyMessageViewHolder) holder).chatMessageText.setText(chatRecyclerItems.get(position).messageText);
            ((ChatRecyclerMyMessageViewHolder) holder).chatTimeText.setText(chatRecyclerItems.get(position).timeText);
        } else if(holder instanceof ChatRecyclerYourMessageViewHolder){
            ((ChatRecyclerYourMessageViewHolder) holder).chatMessageText.setText(chatRecyclerItems.get(position).messageText);
            ((ChatRecyclerYourMessageViewHolder) holder).chatTimeText.setText(chatRecyclerItems.get(position).timeText);
        } else {
            ((ChatRecyclerLeaveMessageViewHolder) holder).leaveMessageText.setText(chatRecyclerItems.get(position).leaveMessageText);
        }
    }


    @Override
    public int getItemCount() {
        return chatRecyclerItems.size();
    }
}


