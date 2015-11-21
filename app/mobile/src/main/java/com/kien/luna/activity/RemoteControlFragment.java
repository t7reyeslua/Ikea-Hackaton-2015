package com.kien.luna.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kien.luna.R;
import com.kien.luna.communication.Message;
import com.kien.luna.communication.SendMessageTask;
import com.kien.luna.communication.Server;


public class RemoteControlFragment extends Fragment {
    private SeekBar mSeekBar;
    private ImageButton mVolDown;
    private ImageButton mVolUp;
    private TextView mVolToolTip;

    private final String TAG = RemoteControlFragment.class.getSimpleName();

    public RemoteControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_remote, container, false);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mVolDown = (ImageButton) rootView.findViewById(R.id.buttonVolDown);
        mVolUp = (ImageButton) rootView.findViewById(R.id.buttonVolUp);
        mVolToolTip = (TextView) rootView.findViewById(R.id.volToolTip);

        configureButtonsListeners();
        configureSeekBarListener();

        registerReceivers();
        requestCurrentVolume();

        // Inflate the layout for this fragment
        return rootView;
    }

    public void configureButtonsListeners(){
        mVolUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifySeekBarValue(true);
            }
        });

        mVolDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifySeekBarValue(false);
            }
        });
    }

    public void configureSeekBarListener(){
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVolToolTip.setVisibility(View.VISIBLE);
                final Animation animationFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                mVolToolTip.startAnimation(animationFadeOut);
                mVolToolTip.setText(String.valueOf(mSeekBar.getProgress()));

                //Get the thumb bound and get its left value
                int x = mSeekBar.getThumb().getBounds().left;
                mVolToolTip.setX(x);
                mVolToolTip.setVisibility(View.INVISIBLE);

                if (fromUser) {
                    //Send command to change volume
                    Double level = mSeekBar.getProgress() * 2.54;
                    String volumeLevel = String.valueOf(level.intValue());
                    String volumeCommand = "music:volume=" + volumeLevel;
                    sendCommand(volumeCommand);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void requestCurrentVolume(){
        String command = "music:status=volume";
        sendCommand(command);
    }

    private void sendCommand(String command){
        //Send command to change volume
        String destination_ip = "192.168.0.26";
        new SendMessageTask().execute(new Message(destination_ip, command));
    }

    public void modifySeekBarValue(boolean increment){
        int deltaStep = 5;
        if (!increment) deltaStep *= -1;
        int currentValue = mSeekBar.getProgress();
        int newValue = currentValue + deltaStep;
        if (newValue < 0) newValue = 0;
        else if (newValue > mSeekBar.getMax()) newValue = mSeekBar.getMax();

        mSeekBar.setProgress(newValue);
    }

    public void modifySeekBarValue(int value){
        try {
            int newValue = value * 100 / 256;
            mSeekBar.setProgress(newValue);
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }


    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(mMessageReceiver,
                new IntentFilter(Server.MESSAGE_RECEIVED));
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Message message = intent.getParcelableExtra("message");
            Log.d(TAG, "Got message: " + message);

            String msg = message.getMessage();
            if (msg.contains("music:volume=")){
                msg = msg.replace("music:volume=","");
                int value = Integer.valueOf(msg);
                modifySeekBarValue(value);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
