package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AnswersView extends AppCompatActivity {
    SQLiteDatabase db;
    TextView ans;
    RadioGroup rb;
    RadioGroup rg;
    int i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answers_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String quest = intent.getStringExtra("quest");
        String pos = intent.getStringExtra("position");
        int id = Integer.parseInt(pos);

        ans = (TextView) findViewById(R.id.ansView);
        ans.setText(quest);

        i=1;
        Cursor allA = db.rawQuery("SELECT * FROM answers WHERE questID =" +id, null);
        rg = (RadioGroup) findViewById(R.id.rg);
        if(allA.getCount()>0){
            if (allA.moveToFirst() ){
                do {
                    RadioButton rb = new RadioButton(this);
                    rb.setTextSize(18);
                    rb.setId(i);
                    i++;
                    rb.setText(allA.getString(2));
                    rg.addView(rb);
                    //list.add(allQ.getString(2));
                } while (allA.moveToNext());
            }
        }
    }
}