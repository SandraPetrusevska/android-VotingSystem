package com.example.workshop;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;


public class AnswersView extends AppCompatActivity {
    SQLiteDatabase db;
    TextView ans;
    RadioGroup rg;
    RadioButton rb;
    Button submit;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int i;
    int id;
    String pollName;
    String check;
    String username;
    String pid;
    int pollid;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    Double longitude, latitude;
    Long time;
    private TextView mTimer;
    private EditText mSetTime;
    private Button mStart;
    private Button mSet;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    ArrayList<String> pollNames = new ArrayList<String>();
    ArrayList<Integer> pollIDs = new ArrayList<Integer>();
    int pollID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = getIntent();
        String quest = intent.getStringExtra("quest");
        pid = intent.getStringExtra("pollID");
        pollid = Integer.valueOf(pid);
        username = intent.getStringExtra("username");
        pollID = Integer.parseInt(pid);
        // String pos = intent.getStringExtra("position");
        //int id = Integer.parseInt(pos);
        Cursor c1 = db.rawQuery("SELECT * FROM polls WHERE id = " + pollID, null);
        if (c1.getCount() > 0) {
            c1.moveToFirst();
            pollName = c1.getString(1);
        }
        c1.close();
        ans = (TextView) findViewById(R.id.ansView);
        ans.setText(quest);

        Cursor c = db.rawQuery("SELECT * FROM questions WHERE content =" + "\"" + quest + "\"" + "AND pollID =" + pollID, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            id = c.getInt(0);
        }
        c.close();
        Cursor allA = db.rawQuery("SELECT * FROM answers WHERE questID =" + id, null);
        rg = (RadioGroup) findViewById(R.id.rg);
        if (allA.getCount() > 0) {
            i = 1;
            if (allA.moveToFirst()) {
                do {
                    RadioButton rb = new RadioButton(this);
                    rb.setTextSize(18);
                    rb.setId(i);
                    i++;
                    rb.setText(allA.getString(2));
                    rg.addView(rb);
                    //list.add(allQ.getString(2));
                } while (allA.moveToNext());
                allA.close();
            }
        }

        submit = (Button) findViewById(R.id.submitAns);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioId = rg.getCheckedRadioButtonId();
                String checked = String.valueOf(radioId);

                rb = findViewById(radioId);
                if( rb == null) {
                        Toast.makeText( AnswersView.this, "Please select answer", Toast.LENGTH_SHORT).show();
                }
                else {
                    Cursor find = db.rawQuery("SELECT * FROM answers WHERE answer = " + "\"" + rb.getText() + "\"" + " AND questID = " + id, null);
                    find.moveToFirst();
                    int ansID = find.getInt(0);
                    find.close();
                    //allA.moveToPosition(v.getId());
                    //  if (allA!= null && allA.getCount() > 0)
                    //  {
                    //       allA.moveToPosition(v.getId());
                    //      check = allA.getString(0);
                    // }

                    getLastLocation();

                    Toast.makeText(AnswersView.this, "Your choice: " + rb.getText() + " on position: " + rb.getId(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), QuestionsView.class);
                    intent.putExtra("pollName", pollName);
                    intent.putExtra("questID", id);
                    intent.putExtra("ans", checked);
                    intent.putExtra("position", pid);
                    intent.putExtra("username", username);

                    Cursor c = db.rawQuery("SELECT * FROM userAnswers WHERE questID =" + id + " AND pollID = " + pollid + " AND username =" + "\"" + username + "\"", null);
                    // Cursor c = db.rawQuery("SELECT * FROM userAnswers WHERE  questID = '" + id + "' AND pollID = '" + pollid + "' AND  username = '" + "\""+username+"\"", null);
                    if (c.getCount() > 0) {
                        c = db.rawQuery("UPDATE userAnswers SET ansID =" + ansID + " WHERE questID =" + id + " AND pollID=" + pollid + " AND username = " + "\"" + username + "\"", null);
                        c.moveToFirst();
                        c.close();
                    } else {
                        db.execSQL("INSERT INTO userAnswers VALUES('" + ansID + "','" + id + "','" + pollid + "','" + username + "','" + latitude + "','" + longitude + "','" + time + "' );");
                    }
                    startActivity(intent);
                    c.close();
                }
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel= new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

      /*  sharedPreferences = getSharedPreferences("pref", 0);
        int btn = sharedPreferences.getInt("btn", 3 );
        editor = sharedPreferences.edit();

        if(btn == 1){
            rb.setChecked(true);
        }
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                 if(checkedId == rb.getId()) {
                     editor.putInt("btn", 1);
                 }
                 editor.commit();
            }
        }); */


    }


    public void checkButton(View v) {
        int radioId = rg.getCheckedRadioButtonId();
        rb = findViewById(radioId);
        Toast.makeText(this, "Selected: " +  rb.getText(), Toast.LENGTH_SHORT);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {

            if (isLocationEnabled()) {

                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            time= System.currentTimeMillis();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {

            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            time= System.currentTimeMillis();

        }
    };
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
    private void startTimer(){
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                // Intent intent = getIntent();
                // String position = intent.getStringExtra("position");
                // int id = Integer.parseInt(position);
                String status = "noactive";
                db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                Cursor c = db.rawQuery("UPDATE polls SET status =" + "\""+status+"\"" + " WHERE id ="+pollID, null);
                c.moveToFirst();
                c.close();

                Cursor c1 = db.rawQuery("SELECT username FROM users",null);
                if(c1.moveToFirst()){
                    do{
                        String notification2 ="Poll: "+pollName+" has ended";
                        // i++;
                        Cursor c2 = db.rawQuery("UPDATE notifications SET status = " + 0 + " , content = "+ "\""+notification2+"\"" +" WHERE username = "+"\""+username+"\"" + " AND pollID = "+ pollID , null);
                        //   Cursor c2 = db.rawQuery("DELETE FROM notifications WHERE username = "+"\""+username+"\"", null);
                        c2.moveToFirst();
                        c2.close();
                    } while(c1.moveToNext());
                    c1.close();
                }
                newNotification();
                Intent intent = new Intent (AnswersView.this, UserView.class);
                intent.putExtra("pollName", pollName);
                intent.putExtra("questID", id);
                //intent.putExtra("ans", checked);
                intent.putExtra("position", pid);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        }.start();
        mTimerRunning = true;
    }

    @Override
    protected void onStop(){
        super.onStop();
       // Intent intent = getIntent();
       // String position = intent.getStringExtra("position");
       // int id = Integer.parseInt(position);
        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis"+pollID, mStartTimeInMillis);
        editor.putLong("milisLeft"+pollID, mTimeLeftInMillis);
        editor.putBoolean("timerRunning"+pollID, mTimerRunning);
        editor.putLong("endTime"+pollID, mEndTime);
        editor.apply();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
      //  Intent intent = getIntent();
       // String position = intent.getStringExtra("position");
       // int id = Integer.parseInt(position);
        mStartTimeInMillis = prefs.getLong("startTimeInMillis"+pollID, 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft"+pollID, mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning"+pollID, false);

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime"+pollID, 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                //  String status = "noactive";
                //  db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                //  Cursor c = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+id, null);
                //  c.moveToFirst();
                //   c.close();
                mTimerRunning = false;
            } else {
                startTimer();
            }
        }
    }
    public void newNotification() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        Cursor c = db.rawQuery("SELECT * FROM notifications, polls WHERE username = " + "\"" + username + "\"" + " AND notifications.pollID = polls.id", null);
        if (c.moveToFirst()) {
            do {
                pollIDs.add(c.getInt(4));
                pollNames.add(c.getString(6));
            } while (c.moveToNext());
            c.close();

            int i;
            for (i = 0; i < pollIDs.size(); i++) {
                mEndTime = prefs.getLong("endTime" + pollIDs.get(i), 0);
                mStartTimeInMillis = prefs.getLong("startTimeInMillis" + pollIDs.get(i), 600000);
                mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

                if (mTimeLeftInMillis < 0) {
                    mTimeLeftInMillis = 0;
                    mTimerRunning = false;
                    String status = "noactive";
                    Cursor c2 = db.rawQuery("UPDATE polls SET status =" + "\"" + status + "\"" + " WHERE id =" + pollIDs.get(i), null);
                    c2.moveToFirst();
                    c2.close();
                    c2 = db.rawQuery("SELECT username FROM users WHERE username = " + "\"" + username + "\"", null);
                    c2.moveToFirst();
                    String notification2 = "Poll " + pollNames.get(i) + " has ended";
                    // db.execSQL("INSERT INTO notifications(content, username, status, pollID) VALUES('" + notification2 + "','" + username + "','" + 0 + "','" + pollIDs.get(i) +"' );");
                    Cursor c3 = db.rawQuery("UPDATE notifications SET status = " + 0 + ", content = "   + "\"" + notification2 + "\"" + " WHERE username = " + "\"" + username + "\"" + " AND pollID = " + pollIDs.get(i) , null);
                    // Cursor c4 = db.rawQuery("UPDATE notifications SET content = '" + notification2 + "', status=0 WHERE username ='" + Integer.valueOf(Uid) + "' AND pid='" + Integer.valueOf(PIDs.get(k)) + "'", null);

                    c3.moveToFirst();
                    c3.close();
                }

            }
        }
        int i=0;
        c = db.rawQuery("SELECT * FROM notifications WHERE username = " + "\"" + username + "\"" + " AND status= " + 0, null);
        if (c.moveToFirst()) {
            do {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(AnswersView.this, "My notification");
                builder.setContentTitle("POLL");
                builder.setContentText(c.getString(1));
                builder.setSmallIcon(R.drawable.poll);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(AnswersView.this);
                managerCompat.notify(i, builder.build());
                i++;

            } while (c.moveToNext());
            c.close();

            Cursor c4 = db.rawQuery("DELETE FROM notifications WHERE username = " + "\"" + username + "\"" + " AND status = " + 0, null);
            c4.moveToFirst();
            c4.close();
        }
    }


}