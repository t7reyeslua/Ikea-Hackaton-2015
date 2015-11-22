package com.kien.luna.beacons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.kien.luna.activity.DevicesFragment;
import com.kien.luna.activity.MainActivity;
import com.kien.luna.communication.Message;
import com.kien.luna.communication.SendJsonTask;
import com.kien.luna.communication.SendMessageTask;
import com.kien.luna.communication.Server;
import com.kien.luna.stepCounter.StepCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BeaconMonitoring {
    private BeaconManager beaconManager;
    private ArrayList<BeaconObject> beaconRegions = new ArrayList();
    private ArrayList<String> regions = new ArrayList<>();
    private ArrayList<Region> estimoteRegions = new ArrayList<>();
    private ArrayList<String> uuids = new ArrayList<>();
    private ArrayList<Integer> majors = new ArrayList<>();
    private ArrayList<Integer> minors = new ArrayList<>();
    private Long scanPeriodMillis = 15000L;
    private Long waitTimeMillis = 1000L;
    public static final  String REGION_EVENT = "com.kien.luna.REGION_EVENT";
    private int steps_count = 0;
    private int steps_offset = -1;
    private boolean insideOffice = true;
    private Context context;
    String user_id = "1";
    public BeaconMonitoring(Context context) {
        this.context = context;
        beaconManager = new BeaconManager(context);
        defineBeaconRegions();
        startMonitoringRegions();
        registerReceivers();
    }

    private void startMonitoringRegions(){
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                for (Region beaconRegion : estimoteRegions) {
                    beaconManager.startMonitoring(beaconRegion);
                }
                Log.e("BeaconMonitoring", "onServiceReady");
            }
        });

        BeaconManager.MonitoringListener beaconMonitoringListener = new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification(
                        "Enter",
                        region.toString(), region.getMinor()
                );
                user_id = "1";
                postJson(user_id + ";ENTER;" + region.getIdentifier() + ";" + String.valueOf(steps_count));
                broadcastMessage("Entering region " + region.getIdentifier() + "| Total steps: " + String.valueOf(steps_count) );
                steps_count = 0;
            }

            @Override
            public void onExitedRegion(Region region) {
                showNotification(
                        "Exit",
                        region.toString(), region.getMinor());

                user_id = "1";
                postJson(user_id + ";EXIT;" + region.getIdentifier() + ";" + String.valueOf(steps_count));
                steps_count = 0;
                broadcastMessage("Exiting region " + region.getIdentifier() + "| Total steps: " + String.valueOf(steps_count));

            }
        };
        beaconManager.setBackgroundScanPeriod(scanPeriodMillis, waitTimeMillis);
        beaconManager.setMonitoringListener(beaconMonitoringListener);
    }

    private void stopMonitoringRegions(){
        for (Region beaconRegion : estimoteRegions) {
            beaconManager.stopMonitoring(beaconRegion);
        }
    }

    public void postJson(String command){
        String destination_ip = "http://94.143.213.153/Events";
        new SendJsonTask().execute(new Message(destination_ip, command));
    }

    public void showNotification(String title, String message, int id) {
//        Intent notifyIntent = new Intent(this.context, MainActivity.class);
//        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivities(this.context, 0,
//                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = new Notification.Builder(this.context)
//                .setSmallIcon(android.R.drawable.ic_dialog_info)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
//                .build();
//        notification.defaults |= Notification.DEFAULT_SOUND;
//        NotificationManager notificationManager =
//                (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(id, notification);
    }

    private void defineBeaconRegions(){
        regions.add("Beacon 1");
        regions.add("Beacon 2");
        regions.add("Beacon 3");

        uuids.add("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
        uuids.add("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
        uuids.add("B9407F30-F5F8-466E-AFF9-25556B57FE6D");

        majors.add(56923);
        majors.add(65383);
        majors.add(52303);

        minors.add(3849);
        minors.add(64145);
        minors.add(53137);
        for (int i = 0; i < regions.size(); i++) {
            BeaconObject bObject = new BeaconObject(uuids.get(i),
                    majors.get(i), minors.get(i), regions.get(i));
            Region reg = new Region(
                    bObject.getRegion(),
                    UUID.fromString(bObject.getUUID()),
                    bObject.getMajor(),
                    bObject.getMinor());
            beaconRegions.add(bObject);
            estimoteRegions.add(reg);
        }


    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Integer message = intent.getIntExtra("message", 0);
            Log.d("BeaconMonitoring", "Got message: " + message);
            if (intent.getAction().equals(StepCounter.STEP_COUNT_EVENT)) {
                steps_count += 1;
            }
        }
    };

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this.context).registerReceiver(mMessageReceiver,
                new IntentFilter(StepCounter.STEP_COUNT_EVENT));
    }


    private void broadcastMessage(String msg) {
        Log.d("REGION_EVENT", "Broadcasting message");
        Intent intent = new Intent(REGION_EVENT);
        // You can also include some extra data.
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

}
