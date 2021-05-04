package com.example.faceship.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.faceship.Constans;
import com.example.faceship.Models.Post;
import com.example.faceship.PostDetailActivity;
import com.example.faceship.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PhotoAdapter  extends RecyclerView.Adapter<PhotoAdapter.ViewHolderData> {
    private List<Post> postList;
    private Context context;

    public PhotoAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.photo_item,parent,false);
        return new ViewHolderData(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(postList.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(context, PostDetailActivity.class);
                i.putExtra(Constans.POSTKEY,postList.get(position).getKey());
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolderData(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.photo_img);
        }
        private void setData(Post post)
        {
            if(post.getImageid().equals("empty"))
            {
                imageView.setImageResource(R.drawable.text);
            }
            else
            {
                if(post.isVideo())
                    imageView.setImageResource(R.drawable.video);
                else
                    Picasso.get().load(post.getImageid()).placeholder(R.drawable.avatar).into(imageView);
            }
        }
    }
}
