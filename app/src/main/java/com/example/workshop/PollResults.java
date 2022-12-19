package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import java.util.LinkedHashMap;

public class PollResults extends AppCompatActivity {
    SQLiteDatabase db;
    LinkedHashMap<String, Integer> allans = new LinkedHashMap<String, Integer>();
    LinkedHashMap<String, LinkedHashMap<String, Integer>> numAns = new LinkedHashMap<String,LinkedHashMap<String, Integer>>();
    String username, pollName;
    int id;
    TextView txt;
    TextView res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_results);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        txt = (TextView) findViewById(R.id.res);
        res = (TextView) findViewById(R.id.pollResults);

        Intent intent = getIntent();
        pollName = intent.getStringExtra("pollName");
        username = intent.getStringExtra("username");

        res.setText("Results for: "+pollName);

        Cursor p = db.rawQuery("SELECT * FROM polls WHERE name = " +"\""+pollName+"\"", null);
        p.moveToFirst();
        id = p.getInt(0);
        p.close();

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
        int i=1;
        if (allQ.moveToFirst() ){
            do {
                buffer = buffer +i +") "+ allQ.getString(2) +"\n";
                i++;
                Cursor allA = db.rawQuery("SELECT * FROM answers WHERE questID = " + allQ.getString(0) , null);
                if(allA.moveToFirst()){
                    do {
                        buffer = buffer + "  ◦"+ allA.getString(2)+ "   ▻    " + numAns.get(allQ.getString(0)).get(allA.getString(0))+"\n";
                    } while (allA.moveToNext());
                    buffer = buffer +"\n";
                }
                allA.close();
            } while (allQ.moveToNext());
            allQ.close();
        }
        txt.setText(buffer);
        txt.setTextSize(25);
    }
}