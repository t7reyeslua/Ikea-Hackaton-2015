package com.kien.luna.activity;

import com.estimote.sdk.SystemRequirementsChecker;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.kien.luna.R;
import com.kien.luna.beacons.BeaconMonitoring;
import com.kien.luna.communication.Server;
import com.kien.luna.login.GoogleLoginManager;
import com.kien.luna.login.SignInActivity;
import com.kien.luna.login.UserPreferences;
import com.kien.luna.stepCounter.StepCounter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends GoogleLoginManager implements FragmentDrawer.FragmentDrawerListener, ServiceConnection {

    private static String TAG = MainActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private Server server;
    private View coordinatorLayoutView;
    private BeaconMonitoring beaconEstimote;

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private SensorManager mSensorManager;
    private Messenger mServiceMessenger = null;
    private boolean mIsBound;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private int total_steps = 0;
    private ServiceConnection mConnection = this;

    private String mUsername;
    private Person currentUser;
    private TextView mUsernamePic;
    private ImageView imgProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUsername = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(UserPreferences.USERNAME, null);
//        if (mUsername == null) {
//            showLoginScreen();
//        }
        setContentView(R.layout.activity_main);

//        if (savedInstanceState != null) {
//            mSignInProgress = savedInstanceState
//                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
//        }
//        mGoogleApiClient = buildGoogleApiClient();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        coordinatorLayoutView = findViewById(R.id.coordinatorLayout);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(0);
        configureSensing();
        registerReceivers();
        beaconEstimote = new BeaconMonitoring(this);
        doBindService();
        this.startService(new Intent(this, StepCounter.class));
    }

    private void showLoginScreen(){
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    /* onConnected is called when our Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Reaching onConnected means we consider the user signed in.
        Log.i(TAG, "onConnected to Google Play Services");

        // Retrieve some profile information to personalize our app for the user.
        currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        Log.e(TAG, currentUser.toString());
        getProfileInformation();


        // Indicate that the sign in process is complete.
        mSignInProgress = STATE_DEFAULT;
    }

    @Override
    public void onSignedOut() {
        //Return to login screen
        showLoginScreen();
    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                currentUser = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentUser.getDisplayName();
                String personPhotoUrl = currentUser.getImage().getUrl();
                String personGooglePlusProfile = currentUser.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                String personId = currentUser.getId();

                Log.e(TAG, "Name: " + personName + ", Id: " + personId +  ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);


                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERNAME, personName).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERID, personId).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERMAIL, email).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERPIC, personPhotoUrl).commit();

                Log.e(TAG, personName);
//                mUsernamePic = (TextView) findViewById(R.id.textViewUser);
//                mUsernamePic.setText(" " + personName + " ");
                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + 560;

                //imgProfilePic = (ImageView) findViewById(R.id.logo_id);
                //new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }


    }

    public void configureSensing() {
        // Get the SensorManager
        mSensorManager= (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        // register this class as a listener for the accelerometer sensors
        List<Sensor> listSensor
                = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        List<String> listSensorType = new ArrayList<String>();
        for(int i=0; i<listSensor.size(); i++){
            Log.e("SENSORS", listSensor.get(i).getName() + "|" + listSensor.get(i).getStringType());
            listSensorType.add(listSensor.get(i).getName());
        }
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Server.MESSAGE_RECEIVED));
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//                new IntentFilter(StepCounter.STEP_COUNT_EVENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(BeaconMonitoring.REGION_EVENT));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(DevicesFragment.DEVICES_FRAGMENT));
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d(TAG, "Got message: " + message);
            if (intent.getAction().equals(BeaconMonitoring.REGION_EVENT)) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayoutView, message, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
    };

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        try {
            doUnbindService();
            this.stopService(new Intent(this, StepCounter.class));
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
//            case 0:
//                fragment = new DevicesFragment();
//                title = getString(R.string.title_devices);
//                break;
//            case 0:
//                fragment = new RemoteControlFragment();
//                title = getString(R.string.title_remote);
//                break;
            case 0:
                fragment = new ConfigurationFragment();
                title = getString(R.string.title_configuration);
                break;
            case 1:
                fragment = new AboutFragment();
                title = getString(R.string.title_about);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    private void automaticBind() {
        if (StepCounter.isRunning()) {
            doBindService();
        }
    }
    private void doBindService() {
        this.bindService(new Intent(this, StepCounter.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.e(TAG, "doBindService");
    }

    private void doUnbindService() {
        if (mIsBound) {

            Log.d(TAG, "Unbinding from service");
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mServiceMessenger != null) {
                try {
                    android.os.Message msg = android.os.Message.obtain(null, StepCounter.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            this.unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        try {
            android.os.Message msg = android.os.Message.obtain(null, StepCounter.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
            Log.e(TAG, "onServiceConnected - SUCCESS");
        }
        catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
        Log.e(TAG, "onServiceConnected");
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
        mServiceMessenger = null;
    }

    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    android.os.Message msg = android.os.Message
                            .obtain(null, StepCounter.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Log.d(TAG, "IncomingHandler:handleMessage");
            switch (msg.what) {
                case StepCounter.MSG_SET_INT_VALUE:
                    total_steps = msg.arg1;
                    break;
                case StepCounter.MSG_SET_STRING_VALUE:
                    break;
                case StepCounter.MSG_SET_LIST_VALUE:
                    break;
                case StepCounter.MSG_SET_BOOLEAN_VALUE:
                    boolean inHand = msg.getData().getBoolean("bool1");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}