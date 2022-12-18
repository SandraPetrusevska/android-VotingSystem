package com.example.workshop;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class UserView extends AppCompatActivity {
    SQLiteDatabase db;
    TextView text;
    TextView text2;
    RecyclerView mRecyclerView;
    myAdapter adapter;
    ArrayList<String> list;
    ArrayList<Integer> idList;
    ListView polls;
    Button logO;
    ArrayAdapter<String> adapters;
    Button res;
    String CHANNEL_ID = "channel";
    String username;
    String poll;
    int id;

    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    ArrayList<String> pollNames = new ArrayList<String>();
    ArrayList<Integer> pollIDs = new ArrayList<Integer>();

    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        text = (TextView) findViewById(R.id.userView);
        text2 = (TextView) findViewById(R.id.activePolls);
        res = (Button) findViewById(R.id.myResults);
        logO = (Button) findViewById(R.id.logO);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        //String password = intent.getStringExtra("password");
        String hello = "Welcome " + username + "!";
        text.setText(hello);

        polls = (ListView) findViewById(R.id.listPolls);
        list = new ArrayList<String>();
        adapters = new ArrayAdapter<String>(UserView.this, R.layout.list_row, list);
        polls.setAdapter(adapters);

        String status = "active";
        Cursor allPolls = db.rawQuery("SELECT * FROM polls WHERE status =" + "\"" + status + "\"", null);
        if (allPolls.getCount() > 0) {
            if (allPolls.moveToFirst()) {
                do {
                    list.add(allPolls.getString(1));
                } while (allPolls.moveToNext());
            }
        } else {
            text2.setText("No active polls");
        }

        res.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), Results.class);
                intent1.putExtra("username", username);
                startActivity(intent1);
            }
        });
      /*  polls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(newNotification()){
                    finish();
                    startActivity(getIntent());
                }
                else {
                    Toast.makeText(UserView.this, "Poll: " + adapters.getItem(position), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), QuestionsView.class);
                    String poll = adapters.getItem(position);
                   // String pos = toString().valueOf(position + 1);
                    id = position + 1;
                    String pos = toString().valueOf(id);
                    intent.putExtra("pollName", poll);
                    intent.putExtra("position", pos);
                    intent.putExtra("username", username);
                    //intent.putExtra("ans", "no");
                    //newNotification();
                    startActivity(intent);

                }
            }
        }); */

        polls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(newNotification()){
                    finish();
                    startActivity(getIntent());
                }
                else {
                    Toast.makeText(UserView.this, "Poll: " + adapters.getItem(position), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), QuestionsView.class);
                    String poll = list.get(position);
                    // String pos = toString().valueOf(position + 1);
                    id = position + 1;
                    String pos = toString().valueOf(id);
                    intent.putExtra("pollName", poll);
                    intent.putExtra("position", pos);
                    intent.putExtra("username", username);
                    //intent.putExtra("ans", "no");
                    //newNotification();
                    startActivity(intent);

                }
            }
        });

        logO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }


    public boolean newNotification() {
        boolean retr = false;
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
                    String notification2 = "Poll: " + pollNames.get(i) + " has ended";
                    // db.execSQL("INSERT INTO notifications(content, username, status, pollID) VALUES('" + notification2 + "','" + username + "','" + 0 + "','" + pollIDs.get(i) +"' );");
                    Cursor c3 = db.rawQuery("UPDATE notifications SET status = "+ 0 + ", content = " + "\"" + notification2 + "\"" + " WHERE username = " + "\"" + username + "\"" + " AND pollID = " + pollIDs.get(i) , null);
                    c3.moveToFirst();
                    c3.close();

                    retr = true;
                }
                else {
                    retr = false;
                }

            }
        }
        c = db.rawQuery("SELECT * FROM notifications WHERE username = " + "\"" + username + "\"" + " AND status= " + 0, null);
        if (c.moveToFirst()) {
            do {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(UserView.this, "My notification");
                builder.setContentTitle("POLL");
                builder.setContentText(c.getString(1));
                builder.setSmallIcon(R.drawable.poll);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(UserView.this);
                managerCompat.notify(i, builder.build());
                i++;

            } while (c.moveToNext());
            c.close();

            Cursor c4 = db.rawQuery("DELETE FROM notifications WHERE username = " + "\"" + username + "\"" + " AND status = " + 0, null);
            c4.moveToFirst();
            c4.close();
        }
        return retr;
    }
}


