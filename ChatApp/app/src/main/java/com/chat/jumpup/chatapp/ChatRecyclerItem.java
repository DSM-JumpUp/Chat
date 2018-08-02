package com.chat.jumpup.chatapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ChatRecyclerItem {
    String messageText, timeText, leaveMessageText;
    private int itemViewType;

    public String getMessageText() {
        return messageText;
    }

    public String getTimeText() {
        return timeText;
    }

    public String getLeaveMessageText() {
        return leaveMessageText;
    }

    public int getItemViewType() {
        return itemViewType;
    }

    public static class Builder{
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        public ChatRecyclerItem Build(int itemViewType, String message) {
            ChatRecyclerItem chatRecyclerItem = new ChatRecyclerItem();
            chatRecyclerItem.itemViewType = itemViewType;
            chatRecyclerItem.messageText = message;
            chatRecyclerItem.timeText = dateFormat.format(cal.getTime());
            return chatRecyclerItem;
        }
    }

}
