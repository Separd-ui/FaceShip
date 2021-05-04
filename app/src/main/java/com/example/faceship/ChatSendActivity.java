package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.example.faceship.Adapters.ChatAdapter;
import com.example.faceship.Models.Chat;
import com.example.faceship.Models.Notification;
import com.example.faceship.Models.User;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatSendActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Chat> chatList;
    private CircleImageView img_profile;
    private Toolbar toolbar;
    private TextView username;
    private TextInputEditText ed_send;
    private ImageButton b_attach,b_send;
    private ChatAdapter adapter;
    private String userUI;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private ValueEventListener seenListener;
    private final int REQ_PERMISSION=20;
    private final int REQ_CAMERA=21;
    private final int REQ_GET_IMAGE=22;
    private String imageID;
    private Uri imageUri;
    private ImageButton b_update;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_send);

        GetIntent();
        init();
        getUserInfo();
        getMessages();
        seenMesage();

    }
    private void seenMesage()
    {
        databaseReference=firebaseDatabase.getReference("Chats");
        seenListener=databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Chat chat=ds.getValue(Chat.class);
                    if(chat.getReceiver().equals(auth.getUid()) && chat.getSender().equals(userUI) && !chat.isSeen())
                    {
                        HashMap<String,Object> hashMap=new HashMap();
                        hashMap.put("seen",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.removeEventListener(seenListener);
    }

    private void sendNotif()
    {
        SendNotification.sendNotification("Отправил вам сообщение","empty",false,userUI);
    }
    private void checkChats()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("ChatsWith").child(auth.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.child(userUI).exists()){
                    databaseReference.child(userUI).setValue(true);
                    DatabaseReference databaseReference1=firebaseDatabase.getReference("ChatsWith").child(userUI);
                    databaseReference1.child(auth.getUid()).setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendMessage()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");
        String key=databaseReference.push().getKey();
        Chat chat=new Chat();
        chat.setKey(key);
        chat.setMessage(ed_send.getText().toString());
        chat.setReceiver(userUI);
        chat.setSender(auth.getUid());
        chat.setTime(System.currentTimeMillis());
        chat.setSeen(false);
        chat.setImage(false);
        databaseReference.child(key).setValue(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ed_send.setText("");
                    checkChats();
                    sendNotif();
                }
                else
                {
                    Toast.makeText(ChatSendActivity.this, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void makePhoto()
    {
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"New Picture");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"From camera");
        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(i,REQ_CAMERA);
    }
    private void getPhoto()
    {
        Intent i=new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(i,REQ_GET_IMAGE);
    }

    private void showPopupMenu()
    {
        PopupMenu popupMenu=new PopupMenu(this,b_attach);
        popupMenu.inflate(R.menu.menu_attach);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.a_camera)
                {
                    makePhoto();
                }
                if(item.getItemId()==R.id.a_photo)
                {
                    getPhoto();
                }
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==REQ_GET_IMAGE && data!=null)
            {
                imageID=data.getData().toString();
            }
            if(requestCode==REQ_CAMERA )
            {
                imageID=imageUri.toString();
            }
            uploadImage();
        }
    }

    private  void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
            {
                showPopupMenu();
            }
            else
            {
                if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    Toast.makeText(this, "Вы должны разрешить использовать камеру...", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},REQ_PERMISSION);
            }
        }
        else
        {
            showPopupMenu();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQ_PERMISSION)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                showPopupMenu();
            }
            else
            {
                Toast.makeText(this, "Резрешение отклонено...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getMessages()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    Chat chat=ds.getValue(Chat.class);
                    if((chat.getSender().equals(auth.getUid()) && chat.getReceiver().equals(userUI)) ||
                            (chat.getReceiver().equals(auth.getUid()) && chat.getSender().equals(userUI)))
                    {
                        chatList.add(chat);
                    }
                }
                adapter.notifyDataSetChanged();
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
        }
    }
    private void getUserInfo()
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

    private void uploadImage()
    {
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Идёт загрузка");
        progressDialog.show();

        Bitmap bitmap=null;
        try {
            bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream);
        byte[] bytes=outputStream.toByteArray();
        StorageReference storageReference= FirebaseStorage.getInstance().getReference("Chat").child(System.currentTimeMillis()+"_images");
        UploadTask uploadTask=storageReference.putBytes(bytes);
        Task<Uri> task=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful())
                {
                    imageID=task.getResult().toString();
                    DatabaseReference databaseReference=firebaseDatabase.getReference("Chats");
                    String key=databaseReference.push().getKey();
                    Chat chat=new Chat();
                    chat.setSeen(false);
                    chat.setTime(System.currentTimeMillis());
                    chat.setSender(auth.getUid());
                    chat.setReceiver(userUI);
                    chat.setMessage(imageID);
                    chat.setKey(key);
                    chat.setImage(true);
                    databaseReference.child(key).setValue(chat).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                                progressDialog.dismiss();
                            else
                            {
                                Toast.makeText(ChatSendActivity.this, "Не получилось отправить фото.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(ChatSendActivity.this, "Не получилось загрузить фото.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void init()
    {
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.chat_s_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        username=findViewById(R.id.chat_s_username);
        ed_send=findViewById(R.id.chat_ed_send);
        b_attach=findViewById(R.id.chat_attach);
        b_send=findViewById(R.id.chat_send);
        imageID="empty";
        img_profile=findViewById(R.id.chat_s_img_profile);
        b_update=findViewById(R.id.chat_update);

        recyclerView=findViewById(R.id.chat_s_rec_view);
        chatList=new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ChatAdapter(this,chatList,ed_send,b_update,b_send);
        recyclerView.setAdapter(adapter);

        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ed_send.getText().toString().equals(""))
                {
                    sendMessage();
                }
                else
                {
                    Toast.makeText(ChatSendActivity.this, "Вы не можете отправить пустое сообщение", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

    }
}