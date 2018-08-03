package com.chat.jumpup.chatapp;


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

    ChatRecyclerItem(int itemViewType, String messageText, String timeText) {
        this.itemViewType = itemViewType;
        this.messageText = messageText;
        this.timeText = timeText;
    }
    ChatRecyclerItem(int itemViewType, String leaveMessageText) {
        this.itemViewType = itemViewType;
        this.leaveMessageText = leaveMessageText;
    }

}
