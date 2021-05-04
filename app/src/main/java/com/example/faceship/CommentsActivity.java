package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faceship.Adapters.CommentAdapter;
import com.example.faceship.Models.Comment;
import com.example.faceship.Models.Post;
import com.example.faceship.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Comment> commentList;
    private CommentAdapter adapter;
    private TextView username,desc;
    private CircleImageView img_profile;
    private String postKey;
    private String userUI;
    private TextInputEditText ed_com;
    private ImageButton b_send;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar=findViewById(R.id.com_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        toolbar.setTitle("Комментарии");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        img_profile=findViewById(R.id.com_publisher_img);
        desc=findViewById(R.id.com_desc);
        username=findViewById(R.id.com_username);
        ed_com=findViewById(R.id.com_add_com);
        b_send=findViewById(R.id.com_send);
        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCom();
            }
        });
        GetIntent();
        recyclerView=findViewById(R.id.com_rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentList=new ArrayList<>();
        adapter=new CommentAdapter(this,commentList);
        recyclerView.setAdapter(adapter);

        getComments();
    }
    private void getComments()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Comments").child(postKey);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Comment comment=ds.getValue(Comment.class);
                    commentList.add(comment);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendCom()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Comments").child(postKey);
        Comment comment=new Comment();
        comment.setComment(ed_com.getText().toString());
        comment.setPublisher(FirebaseAuth.getInstance().getUid());
        databaseReference.push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    String message=ed_com.getText().toString();
                    if(message.length()>5)
                        message=message.substring(0,5)+"...";
                    ed_com.setText("");
                    SendNotification.sendNotification("Оставил комментарий:"+message,postKey,true,userUI);
                }
                else
                {
                    Toast.makeText(CommentsActivity.this, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private  void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            if(i.getStringExtra(Constans.POSTKEY)!=null)
            {
                postKey=i.getStringExtra(Constans.POSTKEY);
                userUI=i.getStringExtra(Constans.USERUI);
                getDesc();
                getUserInfo();
            }
        }
    }
    private void getDesc()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Post").child(postKey);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post=snapshot.getValue(Post.class);
                desc.setText(post.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserInfo()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(user.getImageid().equals("empty"))
                    img_profile.setImageResource(R.drawable.avatar);
                else if(user.getImageid().equals("1"))
                    img_profile.setImageResource(R.drawable.avatar1);
                else if(user.getImageid().equals("2"))
                    img_profile.setImageResource(R.drawable.avatar2);
                else if(user.getImageid().equals("3"))
                    img_profile.setImageResource(R.drawable.avatar3);
                else
                    Picasso.get().load(user.getImageid()).placeholder(R.drawable.avatar).into(img_profile);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}