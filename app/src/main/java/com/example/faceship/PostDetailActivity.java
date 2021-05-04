package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.faceship.Adapters.PostAdapter;
import com.example.faceship.Models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class PostDetailActivity extends AppCompatActivity {
    private List<Post> postList;
    private PostAdapter adapter;
    private String postKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Toolbar toolbar=findViewById(R.id.det_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle("Публикация");
        RecyclerView recyclerView=findViewById(R.id.det_rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList=new ArrayList<>();
        adapter=new PostAdapter(postList,this);
        recyclerView.setAdapter(adapter);

        GetIntent();
    }
    private void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            postKey=i.getStringExtra(Constans.POSTKEY);

            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Post").child(postKey);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    postList.clear();
                    Post post=snapshot.getValue(Post.class);
                    postList.add(post);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}