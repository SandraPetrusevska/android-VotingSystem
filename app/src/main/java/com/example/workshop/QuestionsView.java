package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
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
    ArrayList<String> answers =  new ArrayList<String>();
    int id;
    private TextView mTimer;
    private EditText mSetTime;
    private Button mStart;
    private Button mSet;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    String username;
    String pollName;
    String pos;
    Button back;
    ArrayList<String> pollNames = new ArrayList<String>();
    ArrayList<Integer> pollIDs = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        mTimer = (TextView) findViewById(R.id.time);
        list = new ArrayList<>();
        Intent intent = getIntent();
        pos = intent.getStringExtra("position");
        pollName = intent.getStringExtra("pollName");
       // int id = Integer.parseInt(pos);
        username = intent.getStringExtra("username");
        String ans = intent.getStringExtra("ans");

        if( ans != null) {
            answers.add(ans);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel= new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Cursor c = db.rawQuery("SELECT * FROM polls WHERE name =" + "\""+pollName+"\"", null);
        if(c.getCount()>0){
            c.moveToFirst();
            id = c.getInt(0);
        }

        c.close();
        Cursor allQ = db.rawQuery("SELECT * FROM questions WHERE pollId =" +id, null);
        if(allQ.getCount()>0){
            if (allQ.moveToFirst() ){
                do {
                    list.add(allQ.getString(2));
                    // idList.add(allQ.getInt(0));
                } while (allQ.moveToNext());
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(QuestionsView.this));
        adapter = new myAdapter(QuestionsView.this, list);
        adapter.setClickListener((myAdapter.ItemClickListener) QuestionsView.this);
        recyclerView.setAdapter(adapter);

        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent (getApplicationContext(), UserView.class);
                intent2.putExtra("username", username);
                startActivity(intent2);
            }
        });

       //  startTimer();
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), AnswersView.class);
        intent.putExtra("quest", adapter.getItem(position));
        String idPol = toString().valueOf(id);
        intent.putExtra("pollID", idPol);
        String pos = toString().valueOf(position+1);
        intent.putExtra("position", pos);
        intent.putExtra("username", username);
       // newNotification();
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
               // Intent intent = getIntent();
               // String position = intent.getStringExtra("position");
               // int id = Integer.parseInt(position);
                Cursor Id = db.rawQuery("SELECT * FROM polls WHERE name = "+ "\""+pollName+"\"", null);
                Id.moveToFirst();
                id = Id.getInt(0);
                Id.close();
                String status = "noactive";
                db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                Cursor c = db.rawQuery("UPDATE polls SET status =" + "\""+status+"\"" + " WHERE id ="+id, null);
                c.moveToFirst();
                c.close();

                Cursor c1 = db.rawQuery("SELECT username FROM users",null);
                if(c1.moveToFirst()){
                    do{
                        String notification2 ="Poll: "+pollName+" has ended";
                       // i++;
                        Cursor c2 = db.rawQuery("UPDATE notifications SET status = " + 0 + " , content = "+ "\""+notification2+"\"" +" WHERE username = "+"\""+username+"\"" + " AND pollID = "+ id , null);
                        //   Cursor c2 = db.rawQuery("DELETE FROM notifications WHERE username = "+"\""+username+"\"", null);
                        c2.moveToFirst();
                        c2.close();
                    } while(c1.moveToNext());
                    c1.close();
                }
                newNotification();
                Intent intent1 = new Intent(QuestionsView.this, UserView.class);
                String idPol = toString().valueOf(id);
                intent1.putExtra("pollID", idPol);
                pos = toString().valueOf(id);
                intent1.putExtra("position", pos);
                intent1.putExtra("username", username);
                startActivity(intent1);
               // mTimerRunning = false;
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
       // Intent intent = getIntent();
       // String position = intent.getStringExtra("position");
       // int id = Integer.parseInt(position);
        Cursor Id = db.rawQuery("SELECT * FROM polls WHERE name = "+ "\""+pollName+"\"", null);
        Id.moveToFirst();
        int id = Id.getInt(0);
        Id.close();
        mStartTimeInMillis = prefs.getLong("startTimeInMillis"+id, 600000);
        mTimeLeftInMillis = prefs.getLong("millisLeft"+id, mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning"+id, false);

        updateCountDownText();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime"+id, 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                String status = "noactive";
                db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
                Cursor c = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+id, null);
                c.moveToFirst();
                c.close();
                mTimerRunning = false;
                updateCountDownText();
            } else {
                startTimer();
            }
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        outState.putStringArrayList("answers", answers );

        super.onSaveInstanceState(outState);

    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        answers = savedInstanceState.getStringArrayList("answers");
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
                NotificationCompat.Builder builder = new NotificationCompat.Builder(QuestionsView.this, "My notification");
                builder.setContentTitle("POLL");
                builder.setContentText(c.getString(1));
                builder.setSmallIcon(R.drawable.poll);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(QuestionsView.this);
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

