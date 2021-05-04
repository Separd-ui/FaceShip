package com.example.faceship;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class DialogAvatar extends Dialog {
    private Context context;
    private sendAvatar sendAvatar;
    private ImageView avatar_1,avatar_2,avatar_3;
    private ImageView tick_1,tick_2,tick_3;
    private Button button;
    private int ch_avatar=0;
    public DialogAvatar(@NonNull Context context,sendAvatar sendAvatar) {
        super(context);
        this.context=context;
        this.sendAvatar=sendAvatar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_avatar);
        button=findViewById(R.id.b_save_avatar);
        avatar_1=findViewById(R.id.avatar_1);
        avatar_2=findViewById(R.id.avatar_2);
        avatar_3=findViewById(R.id.avatar_3);
        tick_1=findViewById(R.id.tick_1);
        tick_2=findViewById(R.id.tick_2);
        tick_3=findViewById(R.id.tick_3);

        avatar_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tick_1.setVisibility(View.VISIBLE);
                tick_2.setVisibility(View.GONE);
                tick_3.setVisibility(View.GONE);
                ch_avatar=1;
            }
        });
        avatar_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tick_1.setVisibility(View.GONE);
                tick_2.setVisibility(View.VISIBLE);
                tick_3.setVisibility(View.GONE);
                ch_avatar=2;
            }
        });
        avatar_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tick_1.setVisibility(View.GONE);
                tick_2.setVisibility(View.GONE);
                tick_3.setVisibility(View.VISIBLE);
                ch_avatar=3;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAvatar.sendAvatarId(ch_avatar);
            }
        });
    }
}
