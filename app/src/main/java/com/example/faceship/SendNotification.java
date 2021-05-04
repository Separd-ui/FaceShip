package com.example.faceship;

import com.example.faceship.Models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendNotification {
    public  static void sendNotification(String message,String postKey,Boolean isPost,String receiver)
    {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Notifications");
        String key=databaseReference.push().getKey();
        Notification notification=new Notification();
        notification.setKey(key);
        notification.setMessage(message);
        notification.setPostKey(postKey);
        notification.setSender(FirebaseAuth.getInstance().getUid());
        notification.setPost(isPost);

        databaseReference.child(receiver).child(key).setValue(notification);

    }

}
