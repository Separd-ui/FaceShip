package com.example.faceship.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.faceship.Constans;
import com.example.faceship.Models.Comment;
import com.example.faceship.Models.User;
import com.example.faceship.ProfileActivity;
import com.example.faceship.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter  extends RecyclerView.Adapter<CommentAdapter.ViewHolderData> {
    private Context context;
    private List<Comment> commentList;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.com_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(commentList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(context, ProfileActivity.class);
                i.putExtra(Constans.USERUI,commentList.get(position).getPublisher());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        TextView username,commentText;
        CircleImageView img_profile;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.com_user_username);
            img_profile=itemView.findViewById(R.id.com_user_img);
            commentText=itemView.findViewById(R.id.com_com);

        }
        private void setData(Comment comment)
        {
            getUserInfo(comment.getPublisher(),img_profile,username);
            commentText.setText(comment.getComment());
        }

    }
    private void getUserInfo(String userUI,CircleImageView circleImageView,TextView textView)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                textView.setText(user.getUsername());
                if(user.getImageid().equals("empty"))
                    circleImageView.setImageResource(R.drawable.avatar);
                else if(user.getImageid().equals("1"))
                    circleImageView.setImageResource(R.drawable.avatar1);
                else if(user.getImageid().equals("2"))
                    circleImageView.setImageResource(R.drawable.avatar2);
                else if(user.getImageid().equals("3"))
                    circleImageView.setImageResource(R.drawable.avatar3);
                else
                    Picasso.get().load(user.getImageid()).placeholder(R.drawable.avatar).into(circleImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
