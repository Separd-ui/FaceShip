package com.example.faceship.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.faceship.Adapters.PostAdapter;
import com.example.faceship.Models.Post;
import com.example.faceship.Models.User;
import com.example.faceship.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowPostsFragment extends Fragment {
    private List<String> followingList;
    private List<Post> postList;
    private PostAdapter adapter;
    private List<String> friendList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_follow_posts, container, false);

        RecyclerView recyclerView=view.findViewById(R.id.fol_rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList=new ArrayList<>();
        friendList=new ArrayList<>();
        followingList=new ArrayList<>();
        adapter=new PostAdapter(postList,getContext());
        recyclerView.setAdapter(adapter);
        getUsersFromFriends();
        return view;
    }
    private void getUsersFromFriends()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Friends").child(FirebaseAuth.getInstance().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    friendList.add(ds.getKey());
                }
                getUsersFromFollowings();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getUsersFromFollowings()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Follow").child(FirebaseAuth.getInstance().getUid()).child("followings");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    followingList.add(ds.getKey());
                }
                friendList.addAll(followingList);
                getPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getPosts()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Post");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Post post=ds.getValue(Post.class);
                    for(String ui:friendList)
                    {
                        if(post.getPublisher().equals(ui))
                            postList.add(post);
                    }
                }
                Collections.reverse(postList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}