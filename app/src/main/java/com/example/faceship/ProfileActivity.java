package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.faceship.Adapters.PhotoAdapter;
import com.example.faceship.Adapters.PostAdapter;
import com.example.faceship.Models.Post;
import com.example.faceship.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ProfileActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private String userUI;
    private ImageView img_profile;
    private RecyclerView recyclerView,recyclerView_saved;
    private List<Post> postList,postList_saved;
    private PhotoAdapter adapter,adapter_saved;
    private LinearLayout linearLayout;
    private List<String> listOfSaved;
    private Button button;
    private Boolean isUs=false;
    private ProgressBar progressBar;
    private TextView username,fullname,bio,number_posts,number_likes,number_friends,number_followers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        init();
        GetIntent();
        getUserInfo();
    }

    private void init()
    {
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        toolbar=findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar=findViewById(R.id.profile_progress);
        recyclerView_saved=findViewById(R.id.profile_rec_view_saved);
        recyclerView_saved.setVisibility(View.GONE);
        linearLayout=findViewById(R.id.linearLayout6);
        listOfSaved=new ArrayList<>();
        img_profile=findViewById(R.id.profile_img);
        username=findViewById(R.id.profile_username);
        fullname=findViewById(R.id.profile_full_name);
        bio=findViewById(R.id.profile_bio);
        button=findViewById(R.id.profile_b);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button.getText().toString().equals("Подписаться"))
                {
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(userUI).child("followers");
                    databaseReference.child(auth.getUid()).setValue(true);
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(auth.getUid());
                    databaseReference1.child("followings").child(userUI).setValue(true);

                    SendNotification.sendNotification("Подписался на вас","empty",false,userUI);
                }
                else if(button.getText().toString().equals("Добавить"))
                {
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow");
                    databaseReference1.child(auth.getUid()).child("followers").child(userUI).removeValue();
                    DatabaseReference databaseReference3=firebaseDatabase.getReference("Follow").child(userUI);
                    databaseReference3.child("followings").child(auth.getUid()).removeValue();
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Friends");
                    databaseReference.child(auth.getUid()).child(userUI).setValue(true);
                    databaseReference.child(userUI).child(auth.getUid()).setValue(true);

                    SendNotification.sendNotification("Добавил вас в друзья","empty",false,userUI);
                }
                else if(button.getText().toString().equals("Друзья"))
                {
                    alertDeleteFriend();
                }
                else if(button.getText().toString().equals("Подписаны"))
                {
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(userUI);
                    databaseReference.child("followers").child(auth.getUid()).removeValue();
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(auth.getUid());
                    databaseReference1.child("followings").child(userUI).removeValue();
                }
            }
        });
        number_followers=findViewById(R.id.profile_followers);
        number_friends=findViewById(R.id.profile_friends);
        number_likes=findViewById(R.id.profile_likes);
        number_posts=findViewById(R.id.profile_posts);

        recyclerView=findViewById(R.id.profile_rec_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        postList=new ArrayList<>();
        adapter=new PhotoAdapter(postList,this);
        recyclerView.setAdapter(adapter);



    }
    private void getPosts()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Post");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post=ds.getValue(Post.class);
                    if(post.getPublisher().equals(userUI))
                        postList.add(post);
                }
                Collections.reverse(postList);
                adapter.notifyDataSetChanged();
                if(!isUs)
                    progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void alertDeleteFriend()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Удаление");
        builder.setMessage("Вы действительно хотите удалить данного пользователя со своего списка друзей?");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference databaseReference=firebaseDatabase.getReference("Friends");
                databaseReference.child(userUI).child(auth.getUid()).removeValue();
                databaseReference.child(auth.getUid()).child(userUI).removeValue();
                DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(auth.getUid()).child("followers");
                databaseReference1.child(userUI).setValue(true);
                DatabaseReference databaseReference2=firebaseDatabase.getReference("Follow").child(userUI).child("followings");
                databaseReference2.child(auth.getUid()).setValue(true);
            }
        });
        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(userUI.equals(auth.getUid()))
            getMenuInflater().inflate(R.menu.menu_profile,menu);
        return true;
    }

    private void getListSaved()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Save").child(auth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOfSaved.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    listOfSaved.add(ds.getKey());
                }
                getSaved();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getSaved()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Post");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList_saved.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post=ds.getValue(Post.class);
                    for(String key:listOfSaved)
                    {
                        if(key.equals(post.getKey()))
                            postList_saved.add(post);
                    }
                }
                adapter_saved.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void areFriends()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(auth.getUid()).child("followers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(userUI).exists())
                {
                    button.setText("Добавить");
                }
                else {
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(userUI).child("followers");
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.child(auth.getUid()).exists())
                            {
                                button.setText("Подписаны");
                            }
                            else
                            {
                                DatabaseReference databaseReference2=firebaseDatabase.getReference("Friends").child(auth.getUid());
                                databaseReference2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.child(userUI).exists()){
                                            button.setText("Друзья");
                                        }
                                        else
                                        {
                                            button.setText("Подписаться");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void GetIntent()
    {
        Intent i=getIntent();
        if(i!=null)
        {
            userUI=i.getStringExtra(Constans.USERUI);
            if(userUI.equals(auth.getUid())){
                toolbar.setTitle("Ваш профиль");
                button.setVisibility(View.GONE);
                isUs=true;

                recyclerView_saved.setLayoutManager(new GridLayoutManager(this,3));
                postList_saved=new ArrayList<>();
                adapter_saved=new PhotoAdapter(postList_saved,this);
                recyclerView_saved.setAdapter(adapter_saved);
                getPosts();
                getListSaved();
            }
            else{
                toolbar.setTitle("Профиль пользователя");
                areFriends();
                linearLayout.setVisibility(View.GONE);
                getPosts();
            }
        }
    }
    private void getUserInfo()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(user.getImageid().equals("empty"))
                {
                    img_profile.setImageResource(R.drawable.avatar);
                }
                else if(user.getImageid().equals("1"))
                    img_profile.setImageResource(R.drawable.avatar1);
                else if(user.getImageid().equals("2"))
                    img_profile.setImageResource(R.drawable.avatar2);
                else if(user.getImageid().equals("3"))
                    img_profile.setImageResource(R.drawable.avatar3);
                else
                {
                    Picasso.get().load(user.getImageid()).placeholder(R.drawable.avatar).into(img_profile);
                }
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                if(user.getBio().equals(""))
                {
                    bio.setText("Здесь пока пусто(");
                }
                else
                {
                    bio.setText(user.getBio());
                }
                getNumberLikes();
                getNumberFriends();
                getNumberFollowers();
                getNumberPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNumberLikes()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("AllLikes").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                number_likes.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNumberFriends()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Friends").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                number_friends.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNumberPosts()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Post");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count=0;
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post=ds.getValue(Post.class);
                    if(post.getPublisher().equals(userUI))
                        count++;
                }
                number_posts.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNumberFollowers()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(userUI).child("followers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                number_followers.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void transition()
    {
        Bundle bundle=null;
        View view=findViewById(R.id.profile_img);
        if(view!=null)
        {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(this,view,this.getString(R.string.transition_act));
                bundle=options.toBundle();
            }
        }
        Intent i=new Intent(ProfileActivity.this,EditProfileActivity.class);
        if(bundle==null)
            startActivity(i);
        else
            startActivity(i,bundle);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==android.R.id.home)
        {
            finish();
        }
        if(id==R.id.sign_out)
        {
            auth.signOut();
            startActivity(new Intent(ProfileActivity.this,StartActivity.class));
            finish();
        }
        if(id==R.id.edit_profile)
        {
            //transition();
            startActivity(new Intent(ProfileActivity.this,EditProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickFriends(View view) {
        Intent i=new Intent(ProfileActivity.this,ShowStatisticsActivity.class);
        i.putExtra(Constans.FRIENDS,userUI);
        startActivity(i);
    }

    public void onClickFollowers(View view) {
        Intent i=new Intent(ProfileActivity.this,ShowStatisticsActivity.class);
        i.putExtra(Constans.FOLLOWERS,userUI);
        startActivity(i);
    }

    public void onClickPosts(View view) {
        recyclerView_saved.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void onClickSaved(View view) {
        recyclerView_saved.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}