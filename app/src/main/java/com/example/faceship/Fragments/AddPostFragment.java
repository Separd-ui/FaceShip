package com.example.faceship.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.faceship.Models.Post;
import com.example.faceship.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


public class AddPostFragment extends Fragment {
    private ImageButton b_add_img;
    private ImageView img_post;
    private TextInputEditText ed_desc;
    private Button b_add;
    private long MAX_VIDEO_SIZE=3000000;
    private final int IMAGE_REQ=10;
    private final int CAMERA_REQ=11,CAMERA_VIDEO_REQ=15;
    private final int TYPE_PHOTO=12;
    private final int VIDEO_REQ=14;
    private final  int REQUEST_CAMERARESULT=20;
    private String imageId;
    private ProgressDialog progressDialog;
    private  boolean isImage=false,isVideo=false;
    private Uri imageUri;
    private VideoView video;
    private MediaController mediaController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_add_post, container, false);
        b_add=view.findViewById(R.id.post_b_post);
        ed_desc=view.findViewById(R.id.post_add_desc);
        img_post=view.findViewById(R.id.post_add_image);
        b_add_img=view.findViewById(R.id.post_add_img);
        video=view.findViewById(R.id.post_add_video);

        mediaController=new MediaController(getContext());
        video.setMediaController(mediaController);
        mediaController.setAnchorView(video);
        video.start();

        imageId="empty";
        b_add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
        b_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPost();
            }
        });
        //createDirectory();

        return view;
    }
    private void uploadPost()
    {
        if(!imageId.equals("empty") && isImage)
        {
            uploadImage();
        }
        else if(!imageId.equals("empty") && isVideo)
        {
            uploadVideo();
        }
        else{
            saveInf();
        }
    }
    private void uploadImage()
    {
        progressDialog=new ProgressDialog(getContext());
        progressDialog.setMessage("Идёт загрузка...");
        progressDialog.show();

        Bitmap bitmap=null;
        try {
            bitmap= MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(imageId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,out);
        byte[] bytes=out.toByteArray();
        StorageReference storageReference= FirebaseStorage.getInstance().getReference("Post").child(System.currentTimeMillis()+"_Image");
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
    private void uploadVideo()
    {
            progressDialog=new ProgressDialog(getContext());
            progressDialog.setMessage("Идёт загрузка...");
            progressDialog.show();
            StorageReference storageReference=FirebaseStorage.getInstance().getReference("Video").child(System.currentTimeMillis()+"_video");
            storageReference.putFile(Uri.parse(imageId)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    imageId=uriTask.getResult().toString();
                    if(uriTask.isSuccessful())
                    {
                        saveInf();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Не получилось загрузить видео...", Toast.LENGTH_SHORT).show();
                }
            });
    }


    private void showPopupMenu()
    {
        PopupMenu popupMenu=new PopupMenu(getContext(),b_add_img);
        popupMenu.inflate(R.menu.camera_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.make_photo)
                {
                    openCamera();
                }
                if(item.getItemId()==R.id.take_photo)
                {
                    Intent i=new Intent();
                    i.setType("image/*");
                    i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    startActivityForResult(i,IMAGE_REQ);
                }
                if(item.getItemId()==R.id.video)
                {
                    chooseVideo();
                }
                if(item.getItemId()==R.id.video_make)
                {
                    makeVideo();
                }
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();

    }

    private void saveInf()
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Post");
        String key=databaseReference.push().getKey();
        Post post=new Post();
        post.setDescription(ed_desc.getText().toString());
        post.setImageid(imageId);
        post.setKey(key);
        if(isVideo)
        {
            post.setVideo(true);
        }
        else{
            post.setVideo(false);
        }
        post.setPublisher(FirebaseAuth.getInstance().getUid());
        databaseReference.child(key).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    if(isImage || isVideo)
                    {
                        progressDialog.dismiss();
                        isImage=false;
                        isVideo=false;
                        img_post.setVisibility(View.GONE);
                        video.setVisibility(View.GONE);
                    }
                    ed_desc.setText("");
                    imageId="empty";
                    Toast.makeText(getContext(), "Пост был успешно опубликован", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getContext(), "Что-то пошло не так.Попробуйте заново", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void openCamera()
    {
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"New Picture");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"From camera");
        imageUri=getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(i,CAMERA_REQ);
    }
    private void chooseVideo()
    {
        Intent i=new Intent();
        i.setType("video/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i,VIDEO_REQ);
    }
    private void makeVideo()
    {
        Intent i=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(i,CAMERA_VIDEO_REQ);
    }

    private void checkPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(getActivity().checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
            && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
            {
                showPopupMenu();
            }
            else
            {
                if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                {
                    Toast.makeText(getContext(), "Вы должны разрешить использовать камеру.", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},REQUEST_CAMERARESULT);
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
        if(requestCode==REQUEST_CAMERARESULT)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
            }
            else
            {
                Toast.makeText(getContext(), "Резрешение отклонено...", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void setVideo(Uri videoUri)
    {
        MediaController mediaController=new MediaController(getContext());
        mediaController.setAnchorView(video);

        video.setMediaController(mediaController);
        video.setVideoURI(videoUri);
        video.requestFocus();
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                video.pause();
            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                video.start();
            }
        });
    }
    private long getVideoSize(Uri videoUri)
    {
        Cursor cursor=getContext().getContentResolver().query(videoUri,null,null,null,null);
        int size=cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        return cursor.getLong(size);
    }
    private void alertDialogSize()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Размер видео");
        builder.setMessage("Вы не можете загрузить видео размером более 3 мб");
        builder.setNeutralButton("Понятно", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==IMAGE_REQ && data!=null && data.getData()!=null)
            {
                img_post.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
                imageId=data.getData().toString();
                img_post.setImageURI(data.getData());
                isImage=true;
                isVideo=false;
            }
            if(requestCode==VIDEO_REQ && data!=null && data.getData()!=null)
            {
                if(getVideoSize(data.getData())>MAX_VIDEO_SIZE)
                {
                    alertDialogSize();
                }
                else
                {
                    getVideoSize(data.getData());
                    video.setVisibility(View.VISIBLE);
                    img_post.setVisibility(View.GONE);
                    setVideo(data.getData());
                    //video.setVideoURI(data.getData());
                    imageId=data.getData().toString();
                    isVideo=true;
                    isImage=false;
                }
            }
            if(requestCode==CAMERA_VIDEO_REQ && data!=null && data.getData()!=null)
            {
                if(getVideoSize(data.getData())>MAX_VIDEO_SIZE)
                {
                    alertDialogSize();
                }
                else
                {
                    video.setVisibility(View.VISIBLE);
                    img_post.setVisibility(View.GONE);
                    setVideo(data.getData());
                    //video.setVideoURI(data.getData());
                    imageId=data.getData().toString();
                    isVideo=true;
                    isImage=false;
                }
            }
            if(requestCode==CAMERA_REQ)
            {
                video.setVisibility(View.GONE);
                img_post.setVisibility(View.VISIBLE);
                img_post.setImageURI(imageUri);
                imageId=imageUri.toString();
                isImage=true;
                isVideo=false;
            }


        }
    }
}