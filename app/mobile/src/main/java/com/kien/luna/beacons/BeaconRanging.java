package com.kien.luna.beacons;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.kien.luna.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeaconRanging {
    private BeaconManager beaconManager;
    private ArrayList<BeaconObject> beaconRegions = new ArrayList();
    private ArrayList<String> regions = new ArrayList<>();
    private ArrayList<Region> estimoteRegions = new ArrayList<>();
    private ArrayList<String> uuids = new ArrayList<>();
    private ArrayList<Integer> majors = new ArrayList<>();
    private ArrayList<Integer> minors = new ArrayList<>();
    private Long scanPeriodMillis = 5000L;
    private Long waitTimeMillis = 1000L;

    private Context context;
    public BeaconRanging(Context context) {
        this.context = context;
        beaconManager = new BeaconManager(context);
        defineBeaconRegions();
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    for (Beacon beacon : list) {
                        identifyDiscoveredBeacon(beacon);
                    }
                }
            }
        });
        startRangingRegions();
    }

    private boolean identifyDiscoveredBeacon(Beacon beacon){
        for (BeaconObject beaconObject : beaconRegions) {
            Log.w("TEST", beacon.getProximityUUID().toString() + "|" + beaconObject.getUUID());
            if (beaconObject.compareBeacon(beacon.getProximityUUID().toString().toUpperCase(),
                    beacon.getMajor(), beacon.getMinor())) {
                Log.e("BeaconRangingDiscovered", beaconObject.getRegion() + "|" + beacon.toString());
                return true;
            }
        }
        return false;

    }

    private void startRangingRegions(){
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                for (Region beaconRegion : estimoteRegions) {
                    beaconManager.startRanging(beaconRegion);
                    Log.e("BeaconRanging", beaconRegion.toString());
                }
                Log.e("BeaconRanging", "onServiceReady");
            }
        });
    }

    private void stopRangingRegions(){
        for (Region beaconRegion : estimoteRegions) {
            beaconManager.stopRanging(beaconRegion);
        }
    }

    public void showNotification(String title, String message, int id) {
        Intent notifyIntent = new Intent(this.context, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this.context, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this.context)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    private void defineBeaconRegions(){
        regions.add("B1");
        regions.add("B2");
        regions.add("B3");

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
}
