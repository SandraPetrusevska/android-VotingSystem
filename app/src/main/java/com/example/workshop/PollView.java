package com.example.workshop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class PollView extends AppCompatActivity {
    SQLiteDatabase db;
    TextView text;
    TextView quest;
    ArrayList<String> list;
    Button map;
    int id;
    //private long START_TIME_IN_MILLIS;
    private TextView mTimer;
    private EditText mSetTime;
    private Button mStart;
    private Button mSet;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    LinkedHashMap<String, Integer> allans = new LinkedHashMap<String, Integer>();
    LinkedHashMap<String, LinkedHashMap<String, Integer>> numAns = new LinkedHashMap<String,LinkedHashMap<String, Integer>>();
    String pollName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        text = (TextView) findViewById(R.id.pollView);
        quest = (TextView) findViewById(R.id.questions);
        list = new ArrayList<String>();
        map = (Button) findViewById(R.id.map);

        Intent intent = getIntent();
        pollName = intent.getStringExtra("poll");
        String position = intent.getStringExtra("position");
        //int id = Integer.parseInt(position);
        String poll = "Poll name: " + intent.getStringExtra("poll");
        text.setText(poll);

        Cursor Id = db.rawQuery("SELECT * FROM polls WHERE name = "+ "\""+pollName+"\"", null);
        Id.moveToFirst();
        id = Id.getInt(0);
        Id.close();

        Cursor Q = db.rawQuery("SELECT * FROM questions WHERE pollID = "+id , null);
        if (Q.moveToFirst() ){
            do {
                Cursor A = db.rawQuery("SELECT * FROM answers WHERE questID = " + Q.getString(0) , null);
                if(A.moveToFirst()){
                    do {
                        allans.put(A.getString(0), 0);
                        numAns.put(Q.getString(0), allans);
                    } while (A.moveToNext());
                    A.close();
                }

            } while (Q.moveToNext());
            Q.close();
        }

        Cursor c =  db.rawQuery("SELECT * FROM userAnswers WHERE pollID = " + id, null);
        if (c.moveToFirst()) {
            do {
                Cursor c1 = db.rawQuery("SELECT * FROM questions WHERE pollID = " + id,null);
                if(c1.moveToFirst()){
                    do{
                        Cursor c2 = db.rawQuery("SELECT * FROM answers WHERE questID = " + c1.getString(0), null);
                        if(c2.moveToFirst()) {
                            do {
                                //allans.put(c2.getString(0), 0);
                                //numAns.put(c1.getString(0), allans);
                                if (c.getString(1).equals(c1.getString(0))) {
                                    if (c.getString(0).equals(c2.getString(0))) {
                                        int a = allans.get(c2.getString(0));
                                        //int a2 = Integer.valueOf(a);
                                        a++;
                                        allans.put(c2.getString(0), a);
                                        numAns.put(c1.getString(0), allans);
                                    }
                                }
                            }while (c2.moveToNext());
                            c2.close();
                        }
                    } while (c1.moveToNext());
                    c1.close();
                }
            } while(c.moveToNext());
            c.close();
        }

        Cursor allQ = db.rawQuery("SELECT * FROM questions WHERE pollID = "+id , null);
        String buffer = "";
        //allQ.moveToFirst();
        //text.setText(allQ.getString(2));
        //allQ.close();
        int i=1;
        if (allQ.moveToFirst() ){
            do {
                //text.setText(allQ.getString(2));
                buffer = buffer + i +") " + allQ.getString(2) +"\n";
                i++;
                Cursor allA = db.rawQuery("SELECT * FROM answers WHERE questID = " + allQ.getString(0) , null);
                    if(allA.moveToFirst()){
                        do {
                            buffer = buffer + "  ◦" + allA.getString(2)+ "   ▻    "  + numAns.get(allQ.getString(0)).get(allA.getString(0))+"\n";
                        } while (allA.moveToNext());
                        buffer = buffer +"\n";
                    }
                    allA.close();
            } while (allQ.moveToNext());
            allQ.close();
        }
        quest.setText(buffer);
        quest.setTextSize(25);

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor c = db.rawQuery("SELECT status FROM polls WHERE name = " +  "\""+pollName+"\"",null);
                c.moveToFirst();
                if(c.getString(0).equals("noactive")) {
                    Intent intent2 = new Intent(getApplicationContext(), MapsActivity.class);
                    intent2.putExtra("pollName", pollName);
                    intent2.putExtra("position", position);
                    startActivity(intent2);
                }
                else {
                    Toast.makeText(PollView.this, "Poll " + pollName + " is active", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mTimer = (TextView) findViewById(R.id.timer);
        mStart = (Button) findViewById(R.id.start);
        mSetTime = (EditText) findViewById(R.id.editTime);
        mSet = (Button) findViewById(R.id.set);

        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mSetTime.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(PollView.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(PollView.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
                setTime(millisInput);
                mSetTime.setText("");
            }
        });

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String position = intent.getStringExtra("position");
                int id = Integer.parseInt(position);
                String status = "active";
                db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                Cursor c = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+id, null);
                c.moveToFirst();
                c.close();

                String notification = "Hello from VotingSystem. The poll "+pollName +" is now available";
                Cursor u = db.rawQuery("SELECT * FROM users", null);
                if(u.moveToFirst()) {
                    do {
                        String username = u.getString(0);
                        db.execSQL("INSERT INTO notifications(content, username, status, pollID) VALUES('" + notification + "','" + username + "','" + 1 + "','" + id +"' );");
                    } while (u.moveToNext());
                    u.close();
                }

                startTimer();
            }
        });
    }
    private void setTime(long milliseconds) {
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }
    private void startTimer(){
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                  String status = "noactive";
                  db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                  Cursor c = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+id, null);
                  c.moveToFirst();
                   c.close();
                updateWatchInterface();
            }
        }.start();
        mTimerRunning = true;
        updateWatchInterface();
    }
    private void resetTimer() {
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }
    private void updateCountDownText(){
        int hours = (int) (mTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((mTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (mTimeLeftInMillis/1000) % 60;
        String timeLeft;
        if (hours > 0) {
            timeLeft = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeft = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        mTimer.setText(timeLeft);
    }
    private void updateWatchInterface() {
        if (mTimerRunning) {
            mSetTime.setVisibility(View.INVISIBLE);
            mSet.setVisibility(View.INVISIBLE);
        } else {
            mSetTime.setVisibility(View.VISIBLE);
            mSet.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
       // Intent intent = getIntent();
       // String position = intent.getStringExtra("position");
       // int id = Integer.parseInt(position);
        Cursor Id = db.rawQuery("SELECT * FROM polls WHERE name = "+ "\""+pollName+"\"", null);
        Id.moveToFirst();
        int id = Id.getInt(0);
        Id.close();
        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis"+id, mStartTimeInMillis);
        editor.putLong("millisLeft"+id, mTimeLeftInMillis);
        editor.putBoolean("timerRunning"+id, mTimerRunning);
        editor.putLong("endTime"+id, mEndTime);
        editor.apply();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
     //   Intent intent = getIntent();
     //   String position = intent.getStringExtra("position");
     //   int id = Integer.parseInt(position);
        Cursor Id = db.rawQuery("SELECT * FROM polls WHERE name = "+ "\""+pollName+"\"", null);
        Id.moveToFirst();
        int id = Id.getInt(0);
        Id.close();
        mStartTimeInMillis = prefs.getLong("startTimeInMillis"+id, 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft"+id, mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning"+id, false);

        updateCountDownText();
        updateWatchInterface();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime"+id, 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                String status = "noactive";
                db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                Cursor c = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+id, null);
                c.moveToFirst();
                c.close();
                String notification2 ="Poll: "+pollName+" has ended";
                Cursor c1 = db.rawQuery("SELECT username FROM users",null);
                int i=0;
                if(c1.moveToFirst()){
                    do{
                        String username = c1.getString(0);
                    //    db.execSQL("INSERT INTO notifications(content, username, status, pollID) VALUES('" + notification2 + "','" + username + "','" + 0 + "','" + id +"' );");
                        i++;
                        Cursor c2 = db.rawQuery("UPDATE notifications SET status = " + 0 + " , content = "+ "\""+notification2+"\"" +" WHERE username = "+"\""+username+"\"" + " AND pollID = "+ id , null);
                     //   Cursor c2 = db.rawQuery("DELETE FROM notifications WHERE username = "+"\""+username+"\"", null);
                        c2.moveToFirst();
                        c2.close();
                    } while (c1.moveToNext());
                    c1.close();
                }
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }
}