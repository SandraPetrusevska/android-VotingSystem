package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {
    SQLiteDatabase db;
    EditText username;
    EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
        db.execSQL("DROP TABLE IF EXISTS admins;");
        db.execSQL("CREATE TABLE IF NOT EXISTS admins( username VARCHAR, password VARCHAR);");
        /* db.execSQL("DROP TABLE IF EXISTS users;"); */
        db.execSQL("CREATE TABLE IF NOT EXISTS users(username VARCHAR, identification VARCHAR, password VARCHAR);");
        db.execSQL("INSERT INTO admins VALUES('sandra','sandra123');");
        db.execSQL("INSERT INTO users VALUES('sandrap','123','123');");
        //db.execSQL("DROP TABLE IF EXISTS polls;");
        //db.execSQL("DROP TABLE IF EXISTS questions;");
        //db.execSQL("DROP TABLE IF EXISTS answers;");


        db.execSQL("CREATE TABLE IF NOT EXISTS polls(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, status VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS questions(id INTEGER PRIMARY KEY AUTOINCREMENT, pollID INTEGER, content VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS answers(id INTEGER PRIMARY KEY AUTOINCREMENT, questID INTEGER,  answer VARCHAR);");

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
        Cursor cursor = db.rawQuery("SELECT * FROM USERS WHERE username = ? and password = ?", new String[] {username, password});
        if (cursor.getCount()>0) {
            return true;
        }
        else {
            return false;
        }
    }

    public void login_input(View view) {
        username = (EditText) findViewById(R.id.username);
        String usr = username.getText().toString();
        password =  (EditText) findViewById(R.id.password);
        String pw = password.getText().toString();
        if (username.getText().toString().trim().length() == 0 || password.getText().toString().trim().length() == 0 ) {
            Toast.makeText(this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
        }
        else {
            Boolean checkuserpw = checkusernamepassword(usr, pw);
            if (checkuserpw==true) {
                Toast.makeText(this, "Log in successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent (getApplicationContext(), UserView.class);
                intent.putExtra("username", usr);
                intent.putExtra("password", pw);
                startActivity(intent);
            }
            else {
                Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}