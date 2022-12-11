package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserView extends AppCompatActivity  {
    SQLiteDatabase db;
    TextView text;
    RecyclerView mRecyclerView;
    myAdapter adapter;
    ArrayList<String> list;
    ArrayList<Integer> idList;
    ListView polls;
    ArrayAdapter<String> adapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        text = (TextView) findViewById(R.id.userView);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");
        String hello = "Welcome " + username + "!";
        text.setText(hello);

        polls = (ListView) findViewById(R.id.listPolls);
        list = new ArrayList<String>();
        adapters = new ArrayAdapter<String>(UserView.this, android.R.layout.simple_list_item_1, list);
        polls.setAdapter(adapters);

        String status="active";
        Cursor allPolls = db.rawQuery("SELECT * FROM polls WHERE status ="+ "\""+status+"\"", null);
        if(allPolls.getCount()>0){
            if (allPolls.moveToFirst() ){
                do {
                    list.add(allPolls.getString(1));
                } while (allPolls.moveToNext());
            }
        }

        polls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText( UserView.this, "Poll: " + adapters.getItem(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), QuestionsView.class);
                String poll = adapters.getItem(position);
                String pos = toString().valueOf(position+1);
                intent.putExtra("poll", poll);
                intent.putExtra("position", pos);
                startActivity(intent);
            }
        });

    }
}
