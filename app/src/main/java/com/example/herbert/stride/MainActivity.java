package com.example.herbert.stride;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.disklrucache.DiskLruCache;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView hoursTextView;
    private TextView minsTextView;
    private TextView secsTextView;
    private TextView tenthsTextView;

    private Button resetButton;
    private Button startStopButton;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get references to the widgets
        hoursTextView = (TextView) findViewById(R.id.textViewHoursValue);
        minsTextView = (TextView) findViewById(R.id.textViewMinsValue);
        secsTextView = (TextView) findViewById(R.id.textViewSecsValue);
        tenthsTextView = (TextView) findViewById(R.id.textViewTenthsValue);

        resetButton = (Button) findViewById(R.id.buttonReset);
        startStopButton = (Button) findViewById(R.id.buttonStartStop);

        //set listeners
        resetButton.setOnClickListener(this);
        startStopButton.setOnClickListener(this);

        //get preferences
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

        //create intents
        //not implemented yet

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
        edit.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        stopwatchOn = prefs.getBoolean("stopwatchOn", false);
        startTimeMillis = prefs.getLong("startTimeMillis", System.currentTimeMillis());
        elapsedTimeMillis = prefs.getLong("elapsedTimeMillis", 0);

        if(stopwatchOn){
            start();
        }else{
            updateViews(elapsedTimeMillis);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

//    create the menu tab
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonStartStop:
                if (stopwatchOn){
                    stop();
                }
                else {
                    start();
                }
                break;
            case R.id.buttonReset:
                reset();
                break;
        }
    }

    //start the stop watch
    private void start() {

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

    //stop the stop watch
    private void stop() {
        //stop timer
        stopwatchOn = false;
        if (timer != null){
            timer.cancel();
        }
        startStopButton.setText("Start");

        stopNotification();

        updateViews(elapsedTimeMillis);
    }



    //reset the stop watch
    private void reset(){
        //stop the timer
        this.stop();

        elapsedTimeMillis = 0;
        updateViews(elapsedTimeMillis);

    }

    private void startNotification() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flags);

        int icon = R.mipmap.ic_launcher;
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

    private void stopNotification() {
        if (notificationManager != null){
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private void updateViews(long elapsedTimeMillis) {
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
}
