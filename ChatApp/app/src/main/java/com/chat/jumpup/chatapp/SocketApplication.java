package com.chat.jumpup.chatapp;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketApplication extends Application {

    private static Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://amuguna.herokuapp.com/");
        } catch (URISyntaxException ue) {
            ue.printStackTrace();
        }
    }
    public static Socket getSocket() {
        return mSocket;
    }
}
