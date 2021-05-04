package com.example.faceship.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.faceship.Constans;
import com.example.faceship.Models.Notification;
import com.example.faceship.Models.Post;
import com.example.faceship.Models.User;
import com.example.faceship.PostDetailActivity;
import com.example.faceship.ProfileActivity;
import com.example.faceship.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter  extends RecyclerView.Adapter<NotificationAdapter.ViewHolderData> {
    private Context context;
    private List<Notification> notificationList;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.notification_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(notificationList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notificationList.get(position).isPost())
                {
                    Intent i=new Intent(context, PostDetailActivity.class);
                    i.putExtra(Constans.POSTKEY,notificationList.get(position).getPostKey());
                    context.startActivity(i);
                }
                else
                {
                    Intent i=new Intent(context, ProfileActivity.class);
                    i.putExtra(Constans.USERUI,notificationList.get(position).getSender());
                    context.startActivity(i);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        TextView username,message;
        CircleImageView img_profile;
        ImageView image;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.not_username);
            message=itemView.findViewById(R.id.not_message);
            image=itemView.findViewById(R.id.not_img);
            img_profile=itemView.findViewById(R.id.not_img_profile);
        }
        private void setData(Notification notification)
        {
            if(notification.isPost()){
                getImagePost(notification.getPostKey(),image);
            }
            getUserInfo(notification.getSender(),img_profile,username);
            message.setText(notification.getMessage());

        }

    }
    private void getUserInfo(String userUI,CircleImageView img,TextView username)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(user.getImageid().equals("empty"))
                    img.setImageResource(R.drawable.avatar);
                else if(user.getImageid().equals("1"))
                    img.setImageResource(R.drawable.avatar1);
                else if(user.getImageid().equals("2"))
                    img.setImageResource(R.drawable.avatar2);
                else if(user.getImageid().equals("3"))
                    img.setImageResource(R.drawable.avatar3);
                else
                    Picasso.get().load(user.getImageid()).placeholder(R.drawable.avatar).into(img);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getImagePost(String postKey,ImageView image)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Post").child(postKey);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post=snapshot.getValue(Post.class);
                if(!post.getImageid().equals("empty"))
                {
                    image.setVisibility(View.VISIBLE);
                    if(post.isVideo())
                        image.setImageResource(R.drawable.video);
                    else
                        Picasso.get().load(post.getImageid()).placeholder(R.drawable.avatar).into(image);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}
