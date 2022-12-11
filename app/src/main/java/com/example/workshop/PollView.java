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
import java.util.Locale;

public class PollView extends AppCompatActivity {
    SQLiteDatabase db;
    TextView text;
    TextView quest;
    ArrayList<String> list;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        text = (TextView) findViewById(R.id.pollView);
        quest = (TextView) findViewById(R.id.questions);
        list = new ArrayList<String>();

        Intent intent = getIntent();
        String pollName = intent.getStringExtra("poll");
        String position = intent.getStringExtra("position");
        int id = Integer.parseInt(position);
        String poll = "Poll name: " + intent.getStringExtra("poll");
        text.setText(poll);

        Cursor Id = db.rawQuery("SELECT * FROM polls WHERE name = "+ "\""+pollName+"\"", null);

        Id.moveToFirst();
        id = Id.getInt(0);
        Id.close();
        Cursor allQ = db.rawQuery("SELECT * FROM questions WHERE pollID = "+id , null);
        String buffer = "";
        //allQ.moveToFirst();
        //text.setText(allQ.getString(2));
        //allQ.close();
        if (allQ.moveToFirst() ){
            do {
                //text.setText(allQ.getString(2));
                buffer = buffer + allQ.getString(2) +"\n";
                Cursor allA = db.rawQuery("SELECT * FROM answers WHERE questID = " + allQ.getString(0) , null);
                    if(allA.moveToFirst()){
                        do {
                            buffer = buffer + allA.getString(2)+ "\n";
                        } while (allA.moveToNext());
                    }

            } while (allQ.moveToNext());
        }
        quest.setText(buffer);
        quest.setTextSize(25);

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
                updateWatchInterface();
                //send notification
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
        Intent intent = getIntent();
        String position = intent.getStringExtra("position");
        int id = Integer.parseInt(position);
        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis"+id, mStartTimeInMillis);
        editor.putLong("milisLeft"+id, mTimeLeftInMillis);
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
        Intent intent = getIntent();
        String position = intent.getStringExtra("position");
        int id = Integer.parseInt(position);
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
                //String status = "noactive";
                //db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                //Cursor c = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+id, null);
                //c.moveToFirst();
                //c.close();
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }
}