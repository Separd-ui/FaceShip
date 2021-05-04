package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.example.faceship.Adapters.UserAdapter;
import com.example.faceship.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowStatisticsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private String key;
    private Boolean isUser;
    private Toolbar toolbar;
    private List<String> uiList;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_statistics);

        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        userList=new ArrayList<>();
        uiList=new ArrayList<>();
        recyclerView=findViewById(R.id.stat_rec_view);
        toolbar=findViewById(R.id.stat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapter=new UserAdapter(this,userList,true,false,false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        GetIntent();
        fillList();

    }
    private void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            if(i.getStringExtra(Constans.FRIENDS)!=null)
            {
                isUser=true;
                key=i.getStringExtra(Constans.FRIENDS);
                toolbar.setTitle("Друзья");
            }
            else if(i.getStringExtra(Constans.FOLLOWERS)!=null)
            {
                isUser=true;
                key=i.getStringExtra(Constans.FOLLOWERS);
                toolbar.setTitle("Подписчики");
            }
            else
            {
                isUser=false;
                key=i.getStringExtra(Constans.POSTKEY);
                toolbar.setTitle("Понравилось");
            }
        }
    }
    private  void fillList()
    {
        if(isUser)
        {
            if(toolbar.getTitle().equals("Друзья"))
            {
                DatabaseReference databaseReference=firebaseDatabase.getReference("Friends").child(key);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        uiList.clear();
                        for (DataSnapshot ds:snapshot.getChildren())
                        {
                            uiList.add(ds.getKey());
                        }
                        readUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else if(toolbar.getTitle().equals("Подписчики"))
            {
                DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(key).child("followers");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        uiList.clear();
                        for(DataSnapshot ds:snapshot.getChildren())
                        {
                            uiList.add(ds.getKey());
                        }
                        readUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        else
        {
            DatabaseReference databaseReference=firebaseDatabase.getReference("Likes").child(key);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    uiList.clear();
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        uiList.add(ds.getKey());
                    }
                    readUsers();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    private void readUsers()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    User user=ds.getValue(User.class);
                    for(String ui:uiList)
                    {
                        if(user.getUi().equals(ui))
                            userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}