package com.kien.luna.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kien.luna.R;
import com.kien.luna.adapter.DeviceListAdapter;
import com.kien.luna.communication.Message;
import com.kien.luna.communication.NsdHelper;
import com.kien.luna.model.DeviceItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DevicesFragment extends Fragment {
    private Timer mTimer = new Timer();
    private int counter = 0;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private DeviceListAdapter mDeviceListAdapter;
    private NsdHelper mNsdHelper;

    private final String TAG = DevicesFragment.class.getSimpleName();
    public static final  String DEVICES_FRAGMENT = "com.kien.luna.DEVICES_FRAGMENT";

    private List<DeviceItem> mDevices = new ArrayList<>();

    public DevicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onPause(){
        super.onPause();
        try {
            mNsdHelper.stopDiscovery();
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_devices, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.devices_swipe_refresh_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.devices_recyclerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        addEmptyMessage();
        setupAdapter(mDevices);
        doRefresh();

        mSwipeRefreshLayout.setOnRefreshListener(mRefreshListener);


        // Inflate the layout for this fragment
        return rootView;
    }


    private void setupAdapter(List<DeviceItem> mDevices) {
        mDeviceListAdapter = new DeviceListAdapter(this.getContext(), mDevices);
        mRecyclerView.setAdapter(mDeviceListAdapter);
    }

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    doRefresh();
                }
            });
        }
    };

    private void doRefresh(){
        mSwipeRefreshLayout.setRefreshing(true);
        discoverDevices();
    }

    private void discoverDevices(){
        counter = 0;
        mNsdHelper = new NsdHelper(this.getContext());
        // mNsdHelper.startDiscovery();
        mTimer.schedule(new DiscoveryTick(), 2500);
    }

    private class DiscoveryTick extends TimerTask {
        @Override
        public void run() {
            Log.e(TAG, "Stopping service discovery " + counter);
            try {
                try {
                    counter += 1;
                    //mNsdHelper.stopDiscovery();
                    //stopRefresh();
                } catch (Exception e){
                    Log.e(TAG,e.toString());
                }
            } catch (Throwable t) { //you should always ultimately catch all exceptions in timer tasks.
                Log.e("DiscoveryTick", "SnapshotTick Failed.", t);
            }
        }
    }

    private void stopRefresh() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                populateDeviceList();
                setupAdapter(mDevices);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void addEmptyMessage(){
        DeviceItem emptyDevice = new DeviceItem();
        emptyDevice.setName("No available devices");
        mDevices.add(emptyDevice);
    }

    private void populateDeviceList(){
        mDevices.clear();
        for (NsdServiceInfo serviceInfo : mNsdHelper.mServicesFoundInfo){
            String name = serviceInfo.getServiceName() + " @" + serviceInfo.getHost().getHostAddress();
            DeviceItem deviceItem = new DeviceItem();
            deviceItem.setName(name);
            mDevices.add(deviceItem);
        }
        if (mDevices.size() == 0){
            addEmptyMessage();
        }
    }

    private void broadcastMessage(Message msg) {
        Log.d(TAG, "Broadcasting message");
        Intent intent = new Intent(DEVICES_FRAGMENT);
        // You can also include some extra data.
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this.getContext()).sendBroadcast(intent);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }




}