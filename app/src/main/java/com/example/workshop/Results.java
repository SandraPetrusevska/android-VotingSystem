package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Results extends AppCompatActivity {
    SQLiteDatabase db;
    String username;
    ArrayList<String> list;
    ListView polls;
    ArrayAdapter<String> adapters;
    TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        polls = (ListView) findViewById(R.id.listPolls);
        list = new ArrayList<String>();
        adapters = new ArrayAdapter<String>(Results.this, R.layout.list_row, list);
        polls.setAdapter(adapters);

        String status = "noactive";

        Cursor myPolls = db.rawQuery("SELECT * FROM userAnswers, polls  WHERE userAnswers.username ="+ "\"" + username + "\"" + " AND userAnswers.pollID = polls.id AND polls.status = " +"\"" + status + "\""  , null);
        if(myPolls.getCount()>0){
            if (myPolls.moveToFirst() ){
                do {
                        Cursor c = db.rawQuery("SELECT * FROM polls where id = "+myPolls.getString(2), null);
                        c.moveToFirst();
                    if(!list.contains(c.getString(1))) {
                        list.add(c.getString(1));
                    }
                } while (myPolls.moveToNext());
            }
        }

        polls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText( Results.this, "Poll: " + adapters.getItem(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PollResults.class);
                String poll = adapters.getItem(position);
                String pos = toString().valueOf(position+1);
                intent.putExtra("pollName", poll);
                intent.putExtra("position", pos);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

    }
}