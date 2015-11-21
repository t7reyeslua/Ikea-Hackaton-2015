package com.kien.luna.communication;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;


public class SendMessageTask extends AsyncTask<Message, Boolean, Boolean> {

    private static final int DESTINATION_IP_PORT = 5118;

    @Override
    protected Boolean doInBackground(Message... msgs) {
        Message msg = msgs[0];
        Boolean result = send_order(msg);
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {

    }

    private Boolean send_order(Message msg)
    {
        int length;
        byte[] b_order;
        Boolean result = false;
        String destination_ip = msg.getRemoteDevice();
        String message = msg.getMessage();
        OutputStream output_stream = null;
        Socket sock = null;
        SocketAddress sockaddr = new InetSocketAddress(destination_ip, DESTINATION_IP_PORT);

        try
        {
            Log.i("LUNA", "Sending a client request to the subwoofer: " + msg.toString());
            sock = new Socket();
            sock.connect(sockaddr, 2000);
            output_stream = sock.getOutputStream();
            message = message + "\n";
            length = message.length();
            b_order = message.getBytes(Charset.forName("UTF-8"));
            output_stream.write(b_order, 0, length);
            sock.close();
            result = true;
        }
        catch (UnknownHostException unknown_host)
        {
            Log.e("LUNA", unknown_host.getMessage());
        }
        catch (IOException io_error)
        {
            Log.e("LUNA", io_error.getMessage());
        }
        return result;
    }
}
