package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Locale;

public class QuestionsView extends AppCompatActivity implements myAdapter.ItemClickListener  {
    SQLiteDatabase db;
    TextView text;
    RecyclerView mRecyclerView;
    myAdapter adapter;
    ArrayList<String> list;
    ArrayList<Integer> idList;
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
        setContentView(R.layout.activity_questions_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        mTimer = (TextView) findViewById(R.id.time);
        list = new ArrayList<>();
        Intent intent = getIntent();
        String pos = intent.getStringExtra("position");
        int id = Integer.parseInt(pos);
        Cursor allQ = db.rawQuery("SELECT * FROM questions WHERE pollId =" +id, null);
        if(allQ.getCount()>0){
            if (allQ.moveToFirst() ){
                do {
                    list.add(allQ.getString(2));
                   // idList.add(allQ.getInt(0));
                } while (allQ.moveToNext());
            }
        }

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(QuestionsView.this));
        adapter = new myAdapter(QuestionsView.this, list);
        adapter.setClickListener((myAdapter.ItemClickListener) QuestionsView.this);
        recyclerView.setAdapter(adapter);
        startTimer();
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), AnswersView.class);
        intent.putExtra("quest", adapter.getItem(position));
        String pos = toString().valueOf(position+1);
        intent.putExtra("position", pos);
        startActivity(intent);
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
                //send notification
                Intent intent = getIntent();
                String position = intent.getStringExtra("position");
                int id = Integer.parseInt(position);
                String status = "noactive";
                db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                Cursor c = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+id, null);
                c.moveToFirst();
                c.close();
                mTimerRunning = false;
            }
        }.start();
        mTimerRunning = true;
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

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime"+id, 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                updateCountDownText();
            } else {
                startTimer();
            }
        }
    }

}
