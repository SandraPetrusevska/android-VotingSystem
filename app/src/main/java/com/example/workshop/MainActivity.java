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
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    SQLiteDatabase db;
    EditText username;
    EditText password;
    private boolean mTimerRunning;
    private long mStartTimeInMillis;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    String usr;
    ArrayList <String> pollNames = new ArrayList<String>();
    ArrayList <Integer> pollIDs = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
        db.execSQL("DROP TABLE IF EXISTS admins;");
        db.execSQL("CREATE TABLE IF NOT EXISTS admins( username VARCHAR, password VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS users(username VARCHAR PRIMARY KEY, identification VARCHAR, password VARCHAR);");
        db.execSQL("INSERT INTO admins VALUES('sandra','sandra123');");
        //db.execSQL("INSERT INTO users VALUES('sandrap','123','123');");
        //db.execSQL("DROP TABLE IF EXISTS polls;");
        //db.execSQL("DROP TABLE IF EXISTS questions;");
        //db.execSQL("DROP TABLE IF EXISTS answers;");
        // db.execSQL("DROP TABLE IF EXISTS userAnswers;");
        //db.execSQL("DROP TABLE IF EXISTS notifications;");

        db.execSQL("CREATE TABLE IF NOT EXISTS polls(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, status VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS questions(id INTEGER PRIMARY KEY AUTOINCREMENT, pollID INTEGER, content VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS answers(id INTEGER PRIMARY KEY AUTOINCREMENT, questID INTEGER,  answer VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS userAnswers(ansID INTEGER, questID INTEGER, pollID INTEGER, username VARCHAR, latitude DOUBLE, longitude DOUBLE, time LONG);");
        db.execSQL("CREATE TABLE IF NOT EXISTS notifications(id INTEGER PRIMARY KEY AUTOINCREMENT, content VARCHAR, username VARCHAR, status INTEGER, pollID INTEGER);");


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel= new NotificationChannel("My notification", "My notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void onCLickRegister(View view) {
        Intent intent = null;
        intent = new Intent (this, Registration.class);
        startActivity(intent);
    }

    public void onCLickAdmin(View view) {
        Intent intent = null;
        intent = new Intent (this, AdminLogin.class);
        startActivity(intent);
    }

    public Boolean checkusernamepassword(String username, String password){
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ? and password = ?", new String[] {username, password});
        if (cursor.getCount()>0) {
            return true;
        }
        else {
            return false;
        }
    }

    public void login_input(View view) {
        username = (EditText) findViewById(R.id.username);
        usr = username.getText().toString();
        password =  (EditText) findViewById(R.id.password);
        String pw = password.getText().toString();
        if (username.getText().toString().trim().length() == 0 || password.getText().toString().trim().length() == 0 ) {
            Toast.makeText(this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
        }
        else {
            Boolean checkuserpw = checkusernamepassword(usr, pw);
            if (checkuserpw==true) {
                int i=0;
              /*  Cursor c = db.rawQuery("SELECT * FROM notifications WHERE username = " + "\""+usr+"\"" + " AND status = " + 1, null);
                if(c.getCount()>0) {
                    if (c.moveToFirst()) {
                        do {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My notification");
                            builder.setContentTitle("NEW POLL");
                            // builder.setContentText("Hello from VotingSystem. Poll is available");
                            builder.setContentText(c.getString(1));
                            builder.setSmallIcon(R.drawable.poll);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
                            managerCompat.notify(i, builder.build());
                            i++;
                        } while (c.moveToNext());
                        c.close();
                        c = db.rawQuery("DELETE FROM notifications WHERE username = " + "\""+usr+"\"" + " AND status = " + 1, null );
                        c.moveToFirst();
                        c.close();
                    }
                } */
                Toast.makeText(this, "Log in successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent (getApplicationContext(), UserView.class);
                intent.putExtra("username", usr);
                intent.putExtra("password", pw);
                newNotification();
                startActivity(intent);
            }
            else {
                Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void newNotification() {
      //  username = (EditText) findViewById(R.id.username);
      //  usr = username.getText().toString();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        Cursor c = db.rawQuery("SELECT * FROM notifications, polls WHERE username = " + "\""+usr+"\"" + " AND notifications.pollID = polls.id", null);
        if(c.moveToFirst()){
            do {
                pollIDs.add(c.getInt(4));
                pollNames.add(c.getString(6));
            } while (c.moveToNext());
            c.close();

            int i;
            for( i=0; i<pollIDs.size(); i++){
                mEndTime = prefs.getLong("endTime" + pollIDs.get(i), 0);
                mStartTimeInMillis = prefs.getLong("startTimeInMillis" + pollIDs.get(i), 600000);
                mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

                if( mTimeLeftInMillis < 0) {
                    mTimeLeftInMillis = 0;
                    mTimerRunning = false;
                    String status = "noactive";
                    Cursor c2 = db.rawQuery("UPDATE polls SET status ="+ "\""+status+"\""+" WHERE id ="+pollIDs.get(i), null);
                    c2.moveToFirst();
                    c2.close();
                    c2 = db.rawQuery("SELECT username FROM users WHERE username = "+ "\""+usr+"\"", null);
                    c2.moveToFirst();
                    String notification2 ="Poll: "+pollNames.get(i)+" has ended";
                    //db.execSQL("INSERT INTO notifications(content, username, status, pollID) VALUES('" + notification2 + "','" + usr + "','" + 0 + "','" + pollIDs.get(i) +"' );");
                    Cursor c3 = db.rawQuery("UPDATE notifications SET status = " + 0 + " , content = "+ "\""+notification2+"\"" +" WHERE username = "+"\""+usr+"\"" + " AND pollID = "+ pollIDs.get(i) , null);
                    c3.moveToFirst();
                    c3.close();
                }
            }
        }
        c = db.rawQuery("SELECT * FROM notifications WHERE username = " + "\""+usr+"\"" +" AND (status = "+ 1 + " OR status= " + 0 +")", null);
        int j=0;
        if(c.moveToFirst()){
            do{
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "My notification");
                builder.setContentTitle("POLL");
                // builder.setContentText("Hello from VotingSystem. Poll is available");
                builder.setContentText(c.getString(1));
                builder.setSmallIcon(R.drawable.poll);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
                managerCompat.notify(j, builder.build());
                j++;

              /*  if(c.getInt(3) == 1) {
                    Cursor c3 = db.rawQuery("UPDATE notifications SET status = " + 0 + "WHERE username = "+"\""+usr+"\"" + " AND pollID = "+ c.getString(4) + " AND status = " + 1, null);
                    c3.moveToFirst();
                    c3.close();
                }
                else {
                    Cursor c3 = db.rawQuery("DELETE FROM notifications WHERE username = "+"\""+usr+"\"" + " AND pollID = "+ c.getString(4) + " AND status = " + 0, null);
                    c3.moveToFirst();
                    c3.close();
                } */
            } while(c.moveToNext());
            c.close();
            Cursor c3 = db.rawQuery("UPDATE notifications SET status = "  + 2 + " WHERE username = "+"\""+usr+"\"" +  " AND status = " + 1, null);
            c3.moveToFirst();
            c3.close();

            Cursor c4 = db.rawQuery("DELETE FROM notifications WHERE username = "+"\""+usr+"\"" + " AND status = " + 0, null);
            c4.moveToFirst();
            c4.close();
        }
    }
}