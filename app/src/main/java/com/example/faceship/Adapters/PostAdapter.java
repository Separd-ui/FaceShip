package com.example.faceship.Adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.faceship.CommentsActivity;
import com.example.faceship.Constans;
import com.example.faceship.Models.Post;
import com.example.faceship.Models.User;
import com.example.faceship.ProfileActivity;
import com.example.faceship.R;
import com.example.faceship.SendNotification;
import com.example.faceship.ShowStatisticsActivity;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {
    private List<Post> postList;
    private Context context;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
        firebaseDatabase=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.post_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolderData holder, int position) {
        holder.setData(postList.get(position));

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
         ImageView image;
         LinearLayout profile_layout;
         TextView text_likes,text_coms,text_username,text_desc;
         ImageButton b_like,b_com,b_save;
         //PlayerView video;
         VideoView video;
         SimpleExoPlayer simpleExoPlayer;
         CircleImageView img_profile;

        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            b_save=itemView.findViewById(R.id.post_b_save);
            profile_layout=itemView.findViewById(R.id.post_layout);
            video=itemView.findViewById(R.id.post_video);
            image=itemView.findViewById(R.id.post_img);
            img_profile=itemView.findViewById(R.id.post_img_profile);
            text_coms=itemView.findViewById(R.id.post_number_com);
            text_likes=itemView.findViewById(R.id.post_number_likes);
            b_com=itemView.findViewById(R.id.post_b_com);
            b_like=itemView.findViewById(R.id.post_b_like);
            text_username=itemView.findViewById(R.id.post_username);
            text_desc=itemView.findViewById(R.id.post_desc);

        }
        private void setData(Post post)
        {
            getUserInfo(post.getPublisher(),img_profile,text_username);
            if(!post.getImageid().equals("empty"))
            {
                if(!post.isVideo())
                {
                    image.setVisibility(View.VISIBLE);
                    Picasso.get().load(post.getImageid()).placeholder(R.drawable.avatar).into(image);
                }
                else if(post.isVideo())
                {
                    video.setVisibility(View.VISIBLE);
                    MediaController mediaController=new MediaController(context);
                    mediaController.setAnchorView(video);
                    video.setMediaController(mediaController);
                    video.setVideoURI(Uri.parse(post.getImageid()));
                    video.requestFocus();
                    video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            //mp.start();
                        }
                    });
                    video.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                            switch (what)
                            {
                                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                                {
                                    return true;
                                }
                                case MediaPlayer.MEDIA_INFO_BUFFERING_START:{
                                    return true;
                                }
                                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                                {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });

                    video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                }
            }


            text_desc.setText(post.getDescription());
            isLiked(b_like,post.getKey(),text_likes);
            isSaved(b_save,post.getKey());
            getNumberComs(text_coms,post.getKey());

            b_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(b_like.getTag().equals("Нравится"))
                    {
                        DatabaseReference databaseReference=firebaseDatabase.getReference("Likes").child(post.getKey());
                        databaseReference.child(auth.getUid()).setValue(true);
                        DatabaseReference databaseReference1=firebaseDatabase.getReference("AllLikes").child(post.getPublisher());
                        databaseReference1.push().setValue(true);
                        sendNotif(post.getPublisher(),post.getKey());
                    }
                    else
                    {
                        DatabaseReference databaseReference=firebaseDatabase.getReference("Likes").child(post.getKey()).child(auth.getUid());
                        databaseReference.removeValue();
                    }
                }
            });
            b_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(b_save.getTag().equals("Сохранить"))
                    {
                        DatabaseReference databaseReference=firebaseDatabase.getReference("Save").child(auth.getUid()).child(post.getKey());
                        databaseReference.setValue(true);
                    }
                    else
                    {
                        DatabaseReference databaseReference=firebaseDatabase.getReference("Save").child(auth.getUid()).child(post.getKey());
                        databaseReference.removeValue();
                    }
                }
            });
            text_likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(context, ShowStatisticsActivity.class);
                    i.putExtra(Constans.POSTKEY,post.getKey());
                    context.startActivity(i);
                }
            });
            text_coms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(context, CommentsActivity.class);
                    i.putExtra(Constans.USERUI,post.getPublisher());
                    i.putExtra(Constans.POSTKEY,post.getKey());
                    context.startActivity(i);
                }
            });

            profile_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(context, ProfileActivity.class);
                    i.putExtra(Constans.USERUI,post.getPublisher());
                    context.startActivity(i);
                }
            });
            b_com.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(context, CommentsActivity.class);
                    i.putExtra(Constans.USERUI,post.getPublisher());
                    i.putExtra(Constans.POSTKEY,post.getKey());
                    context.startActivity(i);
                }
            });
        }
    }

    private void sendNotif(String userUI,String postKey)
    {
        SendNotification.sendNotification("Понравилась ваша публикация",postKey,true,userUI);
    }
    private void getUserInfo(String userUI,ImageView imageView,TextView textView)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Users").child(userUI);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(user.getImageid().equals("empty"))
                    imageView.setImageResource(R.drawable.avatar);
                else if(user.getImageid().equals("1"))
                    imageView.setImageResource(R.drawable.avatar1);
                else if(user.getImageid().equals("2"))
                    imageView.setImageResource(R.drawable.avatar2);
                else if(user.getImageid().equals("3"))
                    imageView.setImageResource(R.drawable.avatar3);
                else
                    Picasso.get().load(user.getImageid()).into(imageView);
                textView.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void isLiked(ImageButton b_like,String postkey,TextView numberLikes)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Likes").child(postkey);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(auth.getUid()).exists())
                {
                    b_like.setTag("Понравилось");
                    b_like.setImageResource(R.drawable.ic_like_f);
                }
                else
                {
                    b_like.setTag("Нравится");
                    b_like.setImageResource(R.drawable.ic_like);
                }
                numberLikes.setText("Понравилось:"+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNumberComs(TextView number_coms,String postKey)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Comments").child(postKey);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                number_coms.setText("Всего комментариев:"+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void isSaved(ImageButton b_save,String postKey)
    {
        DatabaseReference databaseReference=firebaseDatabase.getReference("Save").child(auth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postKey).exists())
                {
                    b_save.setTag("Сохранено");
                    b_save.setImageResource(R.drawable.ic_save_post_b);
                }
                else
                {
                    b_save.setTag("Сохранить");
                    b_save.setImageResource(R.drawable.ic_save_post_w);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
