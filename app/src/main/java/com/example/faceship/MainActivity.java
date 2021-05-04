package com.example.faceship;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.faceship.Models.Chat;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickProfile(View view) {
        Intent i=new Intent(MainActivity.this,ProfileActivity.class);
        i.putExtra(Constans.USERUI, FirebaseAuth.getInstance().getUid());
        startActivity(i);
    }

    public void onClickTape(View view) {
        startActivity(new Intent(MainActivity.this,PostActivity.class));
    }

    public void onClickChat(View view) {
        startActivity(new Intent(MainActivity.this, ChatActivity.class));
    }

    public void onClickUsers(View view) {
        startActivity(new Intent(MainActivity.this,UserSearchActivity.class));
    }

    public void onClickUs(View view) {
        dialogAuthor();
    }

    public void onClickNot(View view) {
        startActivity(new Intent(MainActivity.this,NotificationActivity.class));
    }
    private void dialogAuthor(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.diaolog_author,null);
        builder.setView(view);

        AlertDialog alertDialog=builder.create();
        alertDialog.show();

        Button button=view.findViewById(R.id.b_ok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 alertDialog.dismiss();
            }
        });


    }
}