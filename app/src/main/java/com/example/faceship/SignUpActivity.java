package com.example.faceship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.faceship.Models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;

public class SignUpActivity extends AppCompatActivity implements sendAvatar {
    private CircleImageView img_profile;
    private TextInputEditText ed_mail,ed_pas,ed_username,ed_fullname,ed_bio;
    private Button b_sign_up;
    private String imageId;
    private FirebaseAuth auth;
    private DialogAvatar dialogAvatar;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private ImageView b_avatar;
    private int avatar_ch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();

    }
    private void init()
    {
        imageId="empty";
        auth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        img_profile=findViewById(R.id.sign_img);
        ed_mail=findViewById(R.id.post_add_desc);
        ed_pas=findViewById(R.id.sign_pas);
        ed_bio=findViewById(R.id.sign_bio);
        ed_fullname=findViewById(R.id.sign_fullname);
        ed_username=findViewById(R.id.sign_username);
        b_sign_up=findViewById(R.id.sign_b_reg);
        b_avatar=findViewById(R.id.sign_avatars);
        dialogAvatar=new DialogAvatar(this,this);
        b_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAvatar.show();
            }
        });
        b_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                b_sign_up.setEnabled(false);
                if(!TextUtils.isEmpty(ed_mail.getText().toString()) && !TextUtils.isEmpty(ed_pas.getText().toString()) && !TextUtils.isEmpty(ed_fullname.getText().toString()))
                {
                    signUp(ed_mail.getText().toString(),ed_pas.getText().toString());
                }
                else
                {
                    b_sign_up.setEnabled(true);
                    Toast.makeText(SignUpActivity.this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void uploadImage()
    {

        if(!imageId.equals("empty") && !imageId.equals("1") && !imageId.equals("2") && !imageId.equals("3"))
        {
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
            StorageReference storageReference=firebaseStorage.getReference("ImageUser").child(auth.getUid()+"_Image");
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
    private void saveInf()
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(auth.getUid());
        User user=new User();
        user.setImageid(imageId);
        user.setSearch(ed_username.getText().toString().toLowerCase());
        user.setUsername(ed_username.getText().toString());
        user.setUi(auth.getUid());
        user.setStatus("offline");
        user.setBio(ed_bio.getText().toString());
        user.setFullname(ed_fullname.getText().toString());
        databaseReference.setValue(user);

        Toast.makeText(this, "Вы вошли как"+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SignUpActivity.this,MainActivity.class));
        finish();
    }
    private void signUp(String mail,String pas)
    {
        auth.createUserWithEmailAndPassword(mail,pas).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    uploadImage();
                }
                else
                {
                    b_sign_up.setEnabled(true);
                    Toast.makeText(SignUpActivity.this, "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== CROP_IMAGE_ACTIVITY_REQUEST_CODE )
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            assert result != null;
            imageId=result.getUri().toString();
            img_profile.setImageURI(result.getUri());
        }
    }

    public void onClickImageSet(View view) {
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