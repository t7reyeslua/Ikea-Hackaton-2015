package com.kien.luna.stepCounter;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityMonitor extends Service implements SensorEventListener {
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    private Timer mTimer = new Timer();
    private int counter = 0;
    private boolean inHand = true;
    public static boolean pause;
    private SensorManager mSensorManager;

    private static boolean isRunning = false;
    private static boolean allowNotification;

    // Keeps track of all current registered clients.
    private List<Messenger> mClients = new ArrayList<Messenger>();
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    public static final int MSG_SET_LIST_VALUE = 5;
    public static final int MSG_SET_BOOLEAN_VALUE = 6;

    // Target we publish for clients to send messages to IncomingHandler.
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private static final String LOGTAG = "ActivityMonitor";

    public ActivityMonitor() {
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


        configureNotifications();
        configureSensing();
        mTimer.scheduleAtFixedRate(new SnapshotTick(), 0, 10000);
        isRunning = true;
    }

    public void configureSensing() {
        // Get the SensorManager
        mSensorManager= (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        // register this class as a listener for the accelerometer sensors
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_NORMAL);
        pause = false;
    }

    public void configureNotifications() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        allowNotification = sharedPrefs.getBoolean("enable_notification", true);
        if (allowNotification)
            showNotification();
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
                case Sensor.TYPE_ACCELEROMETER:
                    //Log.i(LOGTAG, "Sensed data.");
                    break;
                case Sensor.TYPE_PROXIMITY:
                    if (event.values[0] > 0){
                        inHand = true;
                    } else {
                        inHand = false;
                    }
                    //Log.i(LOGTAG, "Sensed data.");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Display a notification in the notification bar.
     */
    private void showNotification() {

        Log.d(LOGTAG, "NOTIFICATION");

    }

    /**
     * Update a notification in the notification bar.
     */
    private void updateNotification(String s) {
        mBuilder.setContentText(s);
        mNotificationManager.notify(0, mBuilder.build());

    }

    public void buildNotificationMessage(String s, double max, double min, double std, int activity){
        if (allowNotification) {
            String msg;

            String act = intToActivity(activity);
            msg =   "Max: " + String.format("%1$,.2f", max) +
                    " Min: " + String.format("%1$,.2f", min) +
                    " Std: " + String.format("%1$,.2f", std) +
                    " Act: " + act;
            updateNotification(msg);
        }

    }

    public void takeSnapshot(){
        pause = true;
//        Log.i(LOGTAG, "Paused sensing data: " + snapshot.norm.size());
//
//        int activity = snapshot.classifierB();
//        double max = snapshot.getMax();
//        double min = snapshot.getMin();
//        double std = snapshot.getStd();
//        double stdStd = snapshot.getStdStd();
//        int steps = 0;
//
//
//        if (activity == TrainingActivity.WALKING) {
//
//            steps = snapshot.getSteps(this.getApplicationContext(), false, inHand);
//            reportStepstoUI(steps);
//        }
//
//        sendMessageToUI(1, activity, std, stdStd);
//
//        snapshot.clearSnapshot();
//        Log.i(LOGTAG, "Unpaused sensing data: " + snapshot.norm.size());
//        pause = false;
//
//        buildNotificationMessage("", max, min, (double) steps, activity);
    }

    public String intToActivity(int n){
        String activity =  String.valueOf(n);
//        switch (n){
//            case TrainingActivity.WALKING:
//                activity = "Walking";
//                break;
//            case TrainingActivity.RUNNING:
//                activity = "Running";
//                break;
//            case TrainingActivity.JOGGING:
//                activity = "Jogging";
//                break;
//            case TrainingActivity.IDLE:
//                activity = "Idle";
//                break;
//            case TrainingActivity.BIKING:
//                activity = "Biking";
//                break;
//            case TrainingActivity.JUMPING:
//                activity = "Jumping";
//                break;
//            default:
//                activity = "Unknown";
//                break;
//        }
        return activity;
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
     * Send the data to all clients.
     * @param intvaluetosend The value to send.
     */
    private void sendMessageToUI(int id, int intvaluetosend, double std, double stdstd) {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext()) {
            Messenger messenger = messengerIterator.next();
            try {
                if (id == 0) {
                    takeSnapshot();
                }
                if (id == 1) {
                    // Send data as an Integer
                    //int steps = (int) (std * 2);
                    //messenger.send(Message.obtain(null, MSG_SET_INT_VALUE, steps, 0));

                    // Send data as a String

                    if (intvaluetosend < 6) {
                        Bundle bundle = new Bundle();
                        String activity = intToActivity(intvaluetosend);

                        double[] sValues = new double[]{intvaluetosend, std, stdstd};

                        Bundle bundle2 = new Bundle();
                        bundle2.putDoubleArray("sensedValues", sValues);
                        Message msg2 = Message.obtain(null, MSG_SET_LIST_VALUE);
                        msg2.setData(bundle2);
                        messenger.send(msg2);

                        bundle.putString("str1", activity);
                        Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                        msg.setData(bundle);
                        messenger.send(msg);

                        bundle.putBoolean("bool1", inHand);
                        Message msg3 = Message.obtain(null, MSG_SET_BOOLEAN_VALUE);
                        msg3.setData(bundle);
                        messenger.send(msg3);
                    }


                }

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {mTimer.cancel();}
        counter=0;
        if (allowNotification)
            mNotificationManager.cancelAll(); // Cancel the persistent notification.
        Log.i(LOGTAG, "Service Stopped.");
        isRunning = false;
        mSensorManager.unregisterListener(this);
        Log.i(LOGTAG, "Unregistered Sensor Listener.");
    }

    //////////////////////////////////////////
    // Nested classes
    /////////////////////////////////////////

    private class SnapshotTick extends TimerTask {
        @Override
        public void run() {
            Log.i(LOGTAG, "Taking Snapshot " + counter);
            try {
                counter += 1;
                sendMessageToUI(0, counter, 0, 0);

            } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
                Log.e("SnapshotTick", "SnapshotTick Failed.", t);
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



}
