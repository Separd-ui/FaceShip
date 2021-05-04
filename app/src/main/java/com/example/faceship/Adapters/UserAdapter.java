package com.example.faceship.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.faceship.ChatSendActivity;
import com.example.faceship.Constans;
import com.example.faceship.Models.Chat;
import com.example.faceship.Models.User;
import com.example.faceship.ProfileActivity;
import com.example.faceship.R;
import com.example.faceship.SendNotification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolderData> {
    private Context context;
    private List<User> userList;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private boolean isStatistics;
    private boolean isChat;
    private boolean isSimple;

    public UserAdapter(Context context, List<User> userList,boolean isStatistics,boolean isChat,boolean isSimple) {
        this.context = context;
        this.userList = userList;
        this.isChat=isChat;
        this.isSimple=isSimple;
        this.isStatistics=isStatistics;
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(userList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChat || isSimple)
                {
                    Intent i=new Intent(context, ChatSendActivity.class);
                    i.putExtra(Constans.USERUI,userList.get(position).getUi());
                    context.startActivity(i);
                }
                else
                {
                    Intent i=new Intent(context, ProfileActivity.class);
                    i.putExtra(Constans.USERUI,userList.get(position).getUi());
                    context.startActivity(i);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        private TextView username,fullname,count_mes;
        private Button button;
        private CircleImageView img_profile;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            count_mes=itemView.findViewById(R.id.user_count_unread);
            username=itemView.findViewById(R.id.user_username);
            fullname=itemView.findViewById(R.id.user_fullname);
            button=itemView.findViewById(R.id.user_b);
            img_profile=itemView.findViewById(R.id.user_img_profile);
        }

        private void setData(User user)
        {
            if(isChat)
            {
                countUnreadMes(count_mes,user.getUi(),fullname);
            }
            else
            {
                if(!isStatistics)
                {
                    button.setVisibility(View.VISIBLE);
                    checkFriends(user.getUi());
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(button.getText().toString().equals("Подписаться"))
                            {
                                DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(user.getUi()).child("followers");
                                databaseReference.child(auth.getUid()).setValue(true);
                                DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(auth.getUid());
                                databaseReference1.child("followings").child(user.getUi()).setValue(true);

                                SendNotification.sendNotification("Подписался на вас","empty",false,user.getUi());
                            }
                            else if(button.getText().toString().equals("Добавить"))
                            {
                                DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow");
                                databaseReference1.child(auth.getUid()).child("followers").child(user.getUi()).removeValue();
                                DatabaseReference databaseReference3=firebaseDatabase.getReference("Follow").child(user.getUi());
                                databaseReference3.child("followings").child(auth.getUid()).removeValue();
                                DatabaseReference databaseReference=firebaseDatabase.getReference("Friends");
                                databaseReference.child(auth.getUid()).child(user.getUi()).setValue(true);
                                databaseReference.child(user.getUi()).child(auth.getUid()).setValue(true);

                                SendNotification.sendNotification("Добавил вас в друзья","empty",false,user.getUi());
                            }
                            else if(button.getText().toString().equals("Друзья"))
                            {
                                alertDeleteFriend(user.getUi());
                            }
                            else if(button.getText().toString().equals("Подписаны"))
                            {
                                DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(user.getUi());
                                databaseReference.child("followers").child(auth.getUid()).removeValue();
                                DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(auth.getUid());
                                databaseReference1.child("followings").child(user.getUi()).removeValue();
                            }
                        }
                    });
                }

            }
            username.setText(user.getUsername());
            fullname.setText(user.getFullname());
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



        }
        private void alertDeleteFriend(String userUI)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
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
        private void checkFriends(String userUI)
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

    }
    private void countUnreadMes(TextView count_mes,String userUI,TextView text_last)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count=0;
                String last_mes = null;
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Chat chat=ds.getValue(Chat.class);
                    if(chat.getReceiver().equals(auth.getUid()) && chat.getSender().equals(userUI) )
                    {
                        if(!chat.isSeen())
                            count++;

                    }
                    if((chat.getReceiver().equals(auth.getUid()) && chat.getSender().equals(userUI)) ||
                    chat.getReceiver().equals(userUI) && chat.getSender().equals(auth.getUid()))
                    {
                        if(chat.isImage())
                        {
                            last_mes="Фото";
                        }
                        else
                        {
                            last_mes=chat.getMessage();
                        }
                    }
                }
                if(last_mes!=null)
                {
                    if(last_mes.length()>10)
                    {
                        last_mes=last_mes.substring(0,10)+"...";
                    }
                }
                text_last.setText(last_mes);
                if(count>0)
                {
                    count_mes.setVisibility(View.VISIBLE);
                    count_mes.setText(String.valueOf(count));
                }
                else
                {
                    count_mes.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(userUI);
                        databaseReference1.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.child(auth.getUid()).exists())
                button.setText("Подписаны");
            else
                button.setText("Подписаться");
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });*/
    /*if(button.getText().toString().equals("Подписаться"))
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(auth.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(user.getUi()).exists())
                {
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Friends");
                    databaseReference1.child(user.getUi()).child(auth.getUid()).setValue(true);
                    databaseReference1.child(auth.getUid()).child(user.getUi()).setValue(true);
                    DatabaseReference databaseReference2=firebaseDatabase.getReference("Follow").child(auth.getUid());
                    databaseReference2.child(user.getUi()).removeValue();
                }
                else
                {
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(user.getUi());
                    databaseReference1.child(auth.getUid()).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
                    else if(button.getText().toString().equals("Подписаны"))
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Follow").child(user.getUi());
        databaseReference.child(auth.getUid()).removeValue();
    }
                    else if(button.getText().toString().equals("Друзья"))
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Friends");
        databaseReference.child(user.getUi()).child(auth.getUid()).removeValue();
        databaseReference.child(auth.getUid()).child(user.getUi()).removeValue();
        DatabaseReference databaseReference1=firebaseDatabase.getReference("Follow").child(auth.getUid());
        databaseReference1.child(user.getUi()).setValue(true);
    }*/
}
