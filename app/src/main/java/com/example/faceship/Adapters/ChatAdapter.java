package com.example.faceship.Adapters;

import android.content.Context;
import android.media.Image;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.faceship.Models.Chat;
import com.example.faceship.Models.User;
import com.example.faceship.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.text.format.DateFormat;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolderData> {
    private Context context;
    private List<Chat> chatList;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private static  final int TYPE_LEFT=1;
    private static  final int TYPE_RIGHT=2;
    private EditText editText;
    private String key;
    private ImageButton b_update,b_send;

    public ChatAdapter(Context context, List<Chat> chatList, EditText editText, ImageButton b_update,ImageButton b_send) {
        this.context = context;
        this.editText=editText;
        this.b_update=b_update;
        this.b_send=b_send;
        this.chatList = chatList;
        auth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=null;
        if(viewType==TYPE_RIGHT)
        {
            view= LayoutInflater.from(context).inflate(R.layout.chat_right,parent,false);
        }
        else
        {
            view=LayoutInflater.from(context).inflate(R.layout.chat_left,parent,false);
        }
        return new ViewHolderData(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(chatList.get(position).getSender().equals(auth.getUid()))
            return TYPE_RIGHT;
        else
            return TYPE_LEFT;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {

        holder.setData(chatList.get(position));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                key=chatList.get(position).getKey();
                if(chatList.get(position).getSender().equals(auth.getUid()))
                    showPopupMenu(holder.itemView,chatList.get(position).getMessage());
                return true;
            }
        });

        b_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editText.getText().toString().equals(""))
                {
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Chats").child(key);
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("message",editText.getText().toString());
                    databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            editText.setText("");
                            b_update.setVisibility(View.GONE);
                            b_send.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        TextView username,left_time,right_time,left_mes,right_mes,seen;
        CircleImageView img_profile;
        LinearLayout left,right;
        ImageView image_left,image_right;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            image_right=itemView.findViewById(R.id.right_image);
            image_left=itemView.findViewById(R.id.left_image);
            seen=itemView.findViewById(R.id.seen);
            username=itemView.findViewById(R.id.left_username);
            left_time=itemView.findViewById(R.id.left_time);
            right_mes=itemView.findViewById(R.id.right_mes);
            right_time=itemView.findViewById(R.id.right_time);
            left_mes=itemView.findViewById(R.id.left_message);
            img_profile=itemView.findViewById(R.id.left_img);
            left=itemView.findViewById(R.id.left_layout);
            right=itemView.findViewById(R.id.right_layout);
        }
        private void setData(Chat chat)
        {
            if(chat.getSender().equals(auth.getUid()))
            {
                if(chat.isImage())
                {
                    image_right.setVisibility(View.VISIBLE);
                    right_mes.setVisibility(View.GONE);
                    Picasso.get().load(chat.getMessage()).placeholder(R.drawable.avatar).into(image_right);
                }
                else
                {
                    right_mes.setVisibility(View.VISIBLE);
                    image_right.setVisibility(View.GONE);
                    right_mes.setText(chat.getMessage());
                }
                right_time.setText(DateFormat.format("HH:mm",chat.getTime()));
                if(!chat.isSeen())
                    seen.setVisibility(View.VISIBLE);
                else
                    seen.setVisibility(View.GONE);
            }
            else
            {
                if(chat.isImage())
                {
                    image_left.setVisibility(View.VISIBLE);
                    left_mes.setVisibility(View.GONE);
                    Picasso.get().load(chat.getMessage()).placeholder(R.drawable.avatar).into(image_left);
                }
                else
                {
                    left_mes.setVisibility(View.VISIBLE);
                    image_left.setVisibility(View.GONE);
                    left_mes.setText(chat.getMessage());
                }
                getUserInfo(username,img_profile,chat.getSender());
                left_time.setText(DateFormat.format("HH:mm",chat.getTime()));
            }

        }
    }
    private void getUserInfo(TextView username,CircleImageView img_profile,String userUI)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
    private void showPopupMenu(View view,String text)
    {
        PopupMenu popupMenu=new PopupMenu(context,view);
        popupMenu.inflate(R.menu.chat_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.chat_edit)
                {
                    editText.setText(text);
                    b_update.setVisibility(View.VISIBLE);
                    b_send.setVisibility(View.GONE);
                }
                if(item.getItemId()==R.id.chat_delete)
                {
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Chats").child(key);
                    databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Сообщение было успешно удалено", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Не получилось удалить сообщение", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();

    }


}
