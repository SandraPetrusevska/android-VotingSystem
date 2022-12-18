package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AdminView extends AppCompatActivity {
    SQLiteDatabase db;
    TextView text;
    EditText etext;
    ListView listViewQ;
    Button b;
    Button logO;
    ArrayList<String> list;
    ArrayAdapter<String> adapter;
    ConstraintLayout parentLayout;
    EditText add;
    int img = R.drawable.poll;
    int hint = 0;
    int numberPolls;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);

        text = (TextView) findViewById(R.id.adminView);
        etext = (EditText) findViewById(R.id.add);
        listViewQ = (ListView) findViewById(R.id.listViewQ);
        b= (Button) findViewById(R.id.newVoting);
        logO = (Button) findViewById(R.id.logO);

        list = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(AdminView.this, R.layout.list_row, list);
        parentLayout = (ConstraintLayout)findViewById(R.id.parentLayout);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        //String password = intent.getStringExtra("password");
        String hello = "Hello " + username + "!";
        text.setText(hello);
        listViewQ.setAdapter(adapter);

        numberPolls = 0;
        Cursor allPolls = db.rawQuery("SELECT * FROM polls", null);
        if(allPolls.getCount()>0){
            if (allPolls.moveToFirst() ){
                do {
                    numberPolls++;
                    list.add(allPolls.getString(1));
                } while (allPolls.moveToNext());
            }
        }

        listViewQ.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText( AdminView.this, "Poll: " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PollView.class);
                String poll = adapter.getItem(position);
                String pos = toString().valueOf(position+1);
                intent.putExtra("poll", poll);
                intent.putExtra("position", pos);
                startActivity(intent);
            }
        });
        logO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        onClickButton();
    }
    public Boolean checkpoll(String name){
        Cursor cursor = db.rawQuery("SELECT * FROM polls WHERE name = ?", new String[] {name});
        if (cursor.getCount()>0) {
            return true;
        }
        else {
            return false;
        }
    }

    public void onClickButton(){
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 String res = etext.getText().toString();
                if (etext.getText().toString().trim().length() == 0) {
                    Toast.makeText(AdminView.this, "Please enter poll name!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Boolean checkp = checkpoll(res);
                    if (checkp==false) {
                        //db.execSQL("INSERT INTO polls (name) VALUES('" + etext.getText().toString() + "' );");
                       // list.add(res);
                       // adapter.notifyDataSetChanged();
                        Intent intent = new Intent(getApplicationContext(), AdminPoll.class);
                        intent.putExtra("poll", res);
                        intent.putExtra("username", username);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(AdminView.this, "Poll already exists!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    int j=0;
    public void input_poll(View view) {
        add = (EditText) findViewById(R.id.add);
        String addP = add.getText().toString();
        if (add.getText().toString().trim().length() == 0) {
            Toast.makeText(this, "Please enter poll name!", Toast.LENGTH_SHORT).show();
        }
        else {
            Boolean checkp = checkpoll(addP);
            if (checkp==true) {
                Intent intent = new Intent (getApplicationContext(), UserView.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
            }
        }



    }
}
