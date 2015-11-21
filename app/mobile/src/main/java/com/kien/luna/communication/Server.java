package com.kien.luna.communication;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    protected int          serverPort   = 5119;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    private Context context;

    private static String TAG = Server.class.getSimpleName();
    public static final  String MESSAGE_RECEIVED = "com.kien.luna.MESSAGE_RECEIVED";

    public Server(int port, Context context){
        this.serverPort = port;
        this.context = context;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                Log.d(TAG,"Waiting for clients");
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    Log.d(TAG, "Server Stopped");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            new Thread(new WorkerRunnable(context, clientSocket, "Client connection:" + clientSocket.getInetAddress().getHostAddress())).start();
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port", e);
        }
    }


    private class WorkerRunnable implements Runnable{

        protected Socket clientSocket = null;
        protected String serverText   = null;
        private Context context;

        public WorkerRunnable(Context context, Socket clientSocket, String serverText) {
            this.clientSocket = clientSocket;
            this.serverText   = serverText;
            this.context = context;
        }

        public void run() {
            try {
                Log.e(TAG, "New client:" + clientSocket.getInetAddress());
                InputStream input  = clientSocket.getInputStream();
                String message = readToEnd(input);
                Message msg = new Message(clientSocket.getRemoteSocketAddress().toString(), message);
                Log.d(TAG, msg.toString());
                input.close();
                broadcastMessage(msg);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        public String readToEnd(InputStream in) throws IOException {
            BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = inReader.readLine()) != null) {
                sb.append(line);
            }
            return new String(sb.toString().getBytes(), "UTF-8");
        }

        private void broadcastMessage(Message msg) {
            Log.d(TAG, "Broadcasting message");
            Intent intent = new Intent(Server.MESSAGE_RECEIVED);
            // You can also include some extra data.
            intent.putExtra("message", msg);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }


}


