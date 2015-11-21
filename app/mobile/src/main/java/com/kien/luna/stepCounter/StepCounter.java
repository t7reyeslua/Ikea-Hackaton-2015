package com.kien.luna.stepCounter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StepCounter extends Service implements SensorEventListener {
    private static final String LOGTAG = "StepCounter";
    private SensorManager mSensorManager;
    // Keeps track of all current registered clients.
    private List<Messenger> mClients = new ArrayList<>();
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    public static final int MSG_SET_LIST_VALUE = 5;
    public static final int MSG_SET_BOOLEAN_VALUE = 6;

    // Target we publish for clients to send messages to IncomingHandler.
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    public static boolean pause;
    private static boolean isRunning = false;

    public static final  String STEP_COUNT_EVENT = "com.kien.luna.STEP_COUNT";
    public StepCounter() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOGTAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOGTAG, "Service Started.");
        configureSensing();
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOGTAG, "Service Stopped.");
        isRunning = false;
        mSensorManager.unregisterListener(this);
        Log.i(LOGTAG, "Unregistered Sensor Listener.");
    }

    public void configureSensing() {
        // Get the SensorManager
        mSensorManager= (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        // register this class as a listener for the accelerometer sensors
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
        pause = false;
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOGTAG, "Received start id " + startId + ": " + intent);
        return START_STICKY; // Run until explicitly stopped.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!pause){
            //TRAIN SENSOR READINGS if flag is enabled
            switch ( event.sensor.getType()) {
                case Sensor.TYPE_STEP_COUNTER:
                    Log.i(LOGTAG, "TYPE_STEP_COUNTER data. " + String.valueOf(event.values[0]));
                    break;
                case Sensor.TYPE_STEP_DETECTOR:
                    Log.i(LOGTAG, "TYPE_STEP_DETECTOR data. " + String.valueOf(event.values[0]));
                    broadcastMessage(1);
                    break;
                default:
                    break;
            }
        }
    }

    private void reportStepstoUI(int steps) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {
                // Send data as an Integer
                messenger.send(Message.obtain(null, MSG_SET_INT_VALUE, steps, 0));

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }

    /**
     * Handle incoming messages from MainActivity
     */
    private class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d(LOGTAG,"handleMessage: " + msg.what);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_INT_VALUE:
                    //incrementBy = msg.arg1;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void broadcastMessage(int steps) {
        Log.d(LOGTAG, "Broadcasting message");
        Intent intent = new Intent(STEP_COUNT_EVENT);
        // You can also include some extra data.
        intent.putExtra("message", steps);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(intent);
    }

}
