package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminPoll extends AppCompatActivity {
    SQLiteDatabase db;
    TextView text;
    List<EditText> content = new ArrayList<EditText>();
    LinkedHashMap<EditText, List<EditText>> QA = new LinkedHashMap<EditText,List<EditText>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_poll);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        text = (TextView) findViewById(R.id.name);
        Intent intent3 = getIntent();
        String poll = "Poll name: " + intent3.getStringExtra("poll");
        text.setText(poll);
    }
    int j=0;
    int i=0;
    int l;
    int first = 1;
    EditText key;
    public void newQuestion(View view) {
        if (first != 1) {
            QA.put(key, List.copyOf(content));
        }
        content.clear();
        LinearLayout newL = (LinearLayout) findViewById(R.id.ll);
        //TextView text = new TextView (this);
        EditText etext = new EditText(this);
        j++;
        //text.setText("Question number "+j);
        //text.setTextSize(18);
        etext.setId(j);
        etext.setHint("Question number "+j);
        //text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        etext.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //newL.addView(text);
        newL.addView(etext);
         /*String qes = etext.getText().toString();
        db.execSQL("INSERT INTO questions (content) VALUES('" + etext.getText().toString() + "' );"); */
        i=0;
        key=etext;
        first=0;
    }
    public void newAnswer(View view) {
        LinearLayout newL = (LinearLayout) findViewById(R.id.ll);
        //TextView text = new TextView (this);
        EditText etext = new EditText(this);
        i++;
        //text.setText("Answer number "+i);
        text.setTextSize(18);
        etext.setId(i);
        etext.setHint("Answer number "+i);
        //text.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        etext.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //newL.addView(text);
        newL.addView(etext);
        content.add(etext);
    }

    public void onSubmit(View view) {
        QA.put(key, List.copyOf(content));
        Set<EditText> q = QA.keySet();
        ContentValues col = new ContentValues();
        Intent intent3 = getIntent();
        String pollName = intent3.getStringExtra("poll");
        col.put("name", pollName);
        col.put("status", "noactive");
        int pid = (int) db.insert("polls", null, col);
        for (EditText pom : q) {
            col = new ContentValues();
            col.put("pollID", pid);
            col.put("content", pom.getText().toString());
            int qid = (int) db.insert("questions", null, col);
            List<EditText> all = QA.get(pom);
            for (l=0; l<all.size(); l++) {
                db.execSQL("INSERT INTO answers (questID, answer) VALUES ('" + qid + "','" + all.get(l).getText().toString() + "');");
            }
        }
        Intent intent = new Intent(getApplicationContext(), AdminView.class);
        startActivity(intent);
    }
}