package com.example.herbert.stride;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView hoursTextView;
    private TextView minsTextView;
    private TextView secsTextView;
    private TextView tenthsTextView;
    private TextView milesTextView;
    private TextView milesDecimalTextView;
    private TextView avgPaceTextView;

    private Button resetButton;
    private Button startStopButton;
    private Button mapButton;
    private Button saveButton;

    private long startTimeMillis;
    private long elapsedTimeMillis;

    private int elapsedHours;
    private int elapsedMins;
    private int elapsedSecs;
    private int elapsedTenths;

    private Timer timer;
    private NumberFormat number;

    private SharedPreferences prefs;
    private boolean stopwatchOn;

    private Intent serviceIntent;

    private final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    private boolean runStopped;

    private SensorManager sensorManager;
    private TextView steps;
    boolean activityRunning;
    private int stepsInSensor = 0;
    private int stepsAtReset;

    private List<Location> locationList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get references to the widgets
        hoursTextView = (TextView) findViewById(R.id.textViewHoursValue);
        minsTextView = (TextView) findViewById(R.id.textViewMinsValue);
        secsTextView = (TextView) findViewById(R.id.textViewSecsValue);
        tenthsTextView = (TextView) findViewById(R.id.textViewTenthsValue);
        milesTextView = (TextView) findViewById(R.id.textViewMiles);
        milesDecimalTextView = (TextView) findViewById(R.id.textViewMilesDecimal);
        avgPaceTextView = (TextView) findViewById(R.id.textViewAvgPace);


        //set Buttons
        resetButton = (Button) findViewById(R.id.buttonReset);
        startStopButton = (Button) findViewById(R.id.buttonStartStop);
        mapButton = (Button) findViewById(R.id.buttonMap);
        saveButton = (Button) findViewById(R.id.buttonSave);

        //set listeners
        resetButton.setOnClickListener(this);
        startStopButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
        saveButton.setOnClickListener(null);
        saveButton.setVisibility(View.INVISIBLE);

        //set shared preferences
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

        //set service intents
        serviceIntent = new Intent(this, RunTrackerService.class);

        //boolean to check if run is in progress
        runStopped = false;

        //steps
        steps = (TextView) findViewById(R.id.textViewSteps);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepsAtReset = prefs.getInt("stepsAtReset", 0);


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override

            //firebaseAuth returns the results if a user is logged in or not
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //if the user is not signed in then send the user to the login activity and clear the top so the user won't be able to go back
                if (firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    //user won't be able to go back
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

//        Editor edit = prefs.edit();
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("stopwatchOn", stopwatchOn);
        edit.putLong("startTimeMillis", startTimeMillis);
        edit.putLong("elapsedTimeMillis", elapsedTimeMillis);

        edit.putBoolean("runStopped", runStopped);
        edit.commit();

        //sensor
        activityRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //sensor
        activityRunning = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }else{
            Toast.makeText(this, "Count sensor not available", Toast.LENGTH_LONG).show();
        }

        stopwatchOn = prefs.getBoolean("stopwatchOn", false);
        startTimeMillis = prefs.getLong("startTimeMillis", System.currentTimeMillis());
        elapsedTimeMillis = prefs.getLong("elapsedTimeMillis", 0);

        runStopped = prefs.getBoolean("runstopped", false);

        if(stopwatchOn){
            start();
        }else{
            updateViews(elapsedTimeMillis);
            if (runStopped){
                saveButton.setOnClickListener(this);
                saveButton.setVisibility(View.VISIBLE);
            }
        }



    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    //create the menu tab
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_logout){

            mAuth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    //onClick event listener for buttons
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonStartStop:
                if (stopwatchOn){
                    saveButton.setOnClickListener(this);
                    saveButton.setVisibility(View.VISIBLE);
                    stop();
                }
                else {
                    saveButton.setOnClickListener(null);
                    saveButton.setVisibility(View.INVISIBLE);
                    start();
                }
                break;
            case R.id.buttonReset:
                saveButton.setOnClickListener(null);
                saveButton.setVisibility(View.INVISIBLE);
                reset();
                break;
            case R.id.buttonMap:
                Intent runMap = new Intent(this, RunMapActivity.class);
                startActivity(runMap);
                break;
            case R.id.buttonSave:
                save();
                break;
        }
    }


    /////////////////////////////////////////////////////////
    //Set Private methods
    /////////////////////////////////////////////////////////
    private void save(){
        Toast.makeText(MainActivity.this, "This is the save function", Toast.LENGTH_SHORT).show();
        saveButton.setOnClickListener(null);
        saveButton.setVisibility(View.INVISIBLE);

        this.reset();
//        this.reset();
    }

    //start stopwatch
    private void start() {
        runStopped = false;
        //make sure old timer thread has been cancelled
        if (timer != null){
            timer.cancel();
        }

        //if stopped or reset, set new start time
        if (stopwatchOn == false){
            startTimeMillis = System.currentTimeMillis() - elapsedTimeMillis;
        }

        //update variables and UI
        stopwatchOn = true;
        startStopButton.setText("Stop");
//        startStopButton.setBackgroundColor(Color.RED);


        // if GPS is not enabled, start GPS settings activity
        LocationManager locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "Please activate GPS settings",
                    Toast.LENGTH_LONG).show();
            Intent intent =
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        // start service
        startService(serviceIntent);
        startNotification();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                updateViews(elapsedTimeMillis);
            }
        };

        timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 0, 100);
    }

    //stop stopwatch
    private void stop() {
        runStopped = true;
        //stop timer
        stopwatchOn = false;
        if (timer != null){
            timer.cancel();
        }
        startStopButton.setText("Start");
//        startStopButton.setBackgroundColor(Color.GREEN);

        // stop service
        stopService(serviceIntent);
        stopNotification();

        updateViews(elapsedTimeMillis);
    }

    //reset stopwatch
    private void reset(){
        runStopped = false;
        //stop the timer
        this.stop();

        //set the save button to invisable and unclickable
//        saveButton.setOnClickListener(null);
//        saveButton.setVisibility(View.INVISIBLE);

        // clear the list of locations in the database
        RunTrackerDB db = new RunTrackerDB(this);
        db.deleteLocations();

        elapsedTimeMillis = 0;
        updateViews(elapsedTimeMillis);

        milesTextView.setText("00");
        milesDecimalTextView.setText("00");

        //steps
        stepsAtReset = stepsInSensor;

        SharedPreferences.Editor editor = getSharedPreferences("Prefs", MODE_PRIVATE).edit();
        editor.putInt("stepsAtReset", stepsAtReset);
        editor.commit();
        steps.setText(String.valueOf(0));
        steps.setText(String.valueOf(0));

    }

    //Set notification when run has started
    private void startNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flags);

        int icon = R.mipmap.ic_launcherstride;
        Notification notification=
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(icon)
                        .setTicker(getText(R.string.app_name))
                        .setContentTitle(getText(R.string.app_name))
                        .setContentText(getText(R.string.content_text))
                        .setContentIntent(pendingIntent)
                        .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    //Stop notification when run has stopped
    private void stopNotification() {
        if (notificationManager != null){
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private void updateViews(final long elapsedTimeMillis) {
        elapsedTenths = (int) ((elapsedTimeMillis/100) % 10);
        elapsedSecs = (int) ((elapsedTimeMillis/1000) % 60);
        elapsedMins = (int) ((elapsedTimeMillis/(60*1000)) % 60);
        elapsedHours = (int) (elapsedTimeMillis/(60*60*1000));

        if (elapsedHours > 0){
            updateView(hoursTextView, elapsedHours, 1);
        }

        updateView(minsTextView, elapsedMins, 2);
        updateView(secsTextView, elapsedSecs, 2);
        updateView(tenthsTextView, elapsedTenths, 1);

        RunTrackerDB db = new RunTrackerDB(this);
        locationList =  db.getLocations();
        if (locationList.size() > 0){
            ///loop and make a json object of locations to save to firebase by user
//            Log.d("#####Locations", locationList.toString());

            //total distance
            float totalDistace = calculatedMiles();
            int fullMiles = (int) (totalDistace/1609);
            int fractionMIles = (int)(totalDistace/1609 * 100);

            updateView(milesTextView,fullMiles,2);
            updateView(milesDecimalTextView, fractionMIles,2);

            int paceUpdate = (int) (totalDistace % 100);
//            Log.d("####Pace for total",String.valueOf(totalDistace));
            if (paceUpdate == 0){
                Log.d("####Total Distance",String.valueOf(totalDistace) + "m");
                float pace = (float) elapsedMins/totalDistace;
//                avgPaceTextView.setText(String.valueOf(pace));
                Log.d("####Pace Update", String.valueOf(pace) + "min/mi");
            }



        }
    }

    private void updateView(final TextView textView, final int elapsedTime, final int minIntDigits) {

        //post changes to UI thread
        number = NumberFormat.getNumberInstance();
        textView.post(new Runnable() {
            @Override
            public void run() {
                number.setMinimumIntegerDigits(minIntDigits);
                textView.setText(number.format(elapsedTime));
            }
        });
    }

    private float calculatedMiles(){
        float totalDistance = 0;

        RunTrackerDB db = new RunTrackerDB(this);
        locationList = db.getLocations();

        if (locationList.size() > 0) {
            Location current = locationList.get(0);
            for (Location l : locationList) {
                float distance = current.distanceTo(l);
                totalDistance += distance;
                current = l;
            }
        }

        return totalDistance;
    }


    ///////////////////////////////////////////////////////////
    //Steps sensor functions
    ///////////////////////////////////////////////////////////
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (activityRunning){
            float a = sensorEvent.values[0];
            int b;
            b = (int)a;
//            steps.setText(String.valueOf(sensorEvent.values[0]));
            stepsInSensor = Integer.valueOf(b);
            int stepsSinceReset = stepsInSensor - stepsAtReset;
            steps.setText(String.valueOf(stepsSinceReset));
        }else{
            sensorEvent.values[0] = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //sensor method
    }
}
