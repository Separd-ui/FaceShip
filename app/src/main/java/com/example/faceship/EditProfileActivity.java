package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class EditProfileActivity extends AppCompatActivity implements sendAvatar {
    private TextView username;
    private TextInputEditText ed_username,ed_fullname,ed_bio;
    private CircleImageView img_profile;
    private FirebaseAuth auth;
    private String imageId,oldImageId;
    private FirebaseDatabase firebaseDatabase;
    private Boolean isImage=false;
    private DialogAvatar dialogAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        init();
        getUserInfo();
    }

    private void init()
    {
        auth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();

        img_profile=findViewById(R.id.edit_img);
        ed_bio=findViewById(R.id.edit_bio);
        ed_fullname=findViewById(R.id.edit_fullname);
        ed_username=findViewById(R.id.edit_username);
        username=findViewById(R.id.edit_username_show);
        Toolbar toolbar=findViewById(R.id.edit_toolbar);
        dialogAvatar=new DialogAvatar(this,this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Редактирование профиля");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button button=findViewById(R.id.edit_b);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImage)
                {
                    uploadImage();
                }
                else
                {
                    saveInf();
                }
            }
        });
    }
    private void  uploadImage()
    {
        if(!imageId.equals("empty") )
        {
            if(imageId.equals(oldImageId))
            {
                saveInf();
                return;
            }
            ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("Идёт загрузка...");
            progressDialog.show();

            Bitmap bitmap=null;
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imageId));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,out);
            byte[] bytes=out.toByteArray();
            final StorageReference storageReference;
            if(oldImageId.equals("1") || oldImageId.equals("2") || oldImageId.equals("3") || imageId.equals("empty"))
            {
                storageReference=FirebaseStorage.getInstance().getReference("ImageUser").child(auth.getUid()+"_Image");
            }
            else
            {
                storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(oldImageId);
            }
            if(storageReference==null)
                return;
            UploadTask uploadTask=storageReference.putBytes(bytes);
            Task<Uri> task=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    imageId=task.getResult().toString();
                    saveInf();
                }
            });
        }
        else
        {
            saveInf();
        }
    }
    private void getUserInfo()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
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
                ed_username.setText(user.getUsername());
                ed_bio.setText(user.getBio());
                ed_fullname.setText(user.getFullname());
                username.setText(user.getUsername());
                imageId=user.getImageid();
                oldImageId=user.getImageid();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveInf()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("imageid",imageId);
        hashMap.put("username",ed_username.getText().toString());
        hashMap.put("fullname",ed_fullname.getText().toString());
        hashMap.put("bio",ed_bio.getText().toString());
        hashMap.put("search",ed_username.getText().toString().toLowerCase());
        databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if(oldImageId.startsWith("http") && (imageId.equals("1") || imageId.equals("2") || imageId.equals("3")))
                    {
                        FirebaseStorage.getInstance().getReferenceFromUrl(oldImageId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    finish();
                                else
                                    Toast.makeText(EditProfileActivity.this, "Не удалось удалить картинку", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        finish();
                    }

                }
                else
                {
                    Toast.makeText(EditProfileActivity.this, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    public void onClickImgChange(View view) {
        dialogAvatar.show();
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            assert result != null;
            isImage=true;
            imageId=result.getUri().toString();
            img_profile.setImageURI(result.getUri());
        }
    }

    public void onClickEditImg(View view) {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(this);
    }

    @Override
    public void sendAvatarId(int ch) {
        if(ch==1)
            img_profile.setImageResource(R.drawable.avatar1);
        else if(ch==2)
            img_profile.setImageResource(R.drawable.avatar2);
        else if(ch==3)
            img_profile.setImageResource(R.drawable.avatar3);
        imageId=String.valueOf(ch);

        dialogAvatar.dismiss();
    }
}