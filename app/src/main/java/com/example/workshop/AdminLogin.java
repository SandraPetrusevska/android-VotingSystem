package com.example.workshop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AdminLogin extends AppCompatActivity {
    SQLiteDatabase db;
    EditText username;
    EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        db=openOrCreateDatabase("votingSystem", MODE_PRIVATE, null);
    }

    public Boolean checkusernamepassword(String username, String password){
        Cursor cursor = db.rawQuery("SELECT * FROM admins WHERE username = ? and password = ?", new String[] {username, password});
        if (cursor.getCount()>0) {
            return true;
        }
        else {
            return false;
        }
    }

    public void login_admin(View view) {
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
                Intent intent = new Intent (getApplicationContext(), AdminView.class);
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