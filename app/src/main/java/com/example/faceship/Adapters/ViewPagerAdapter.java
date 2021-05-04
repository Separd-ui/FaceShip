package com.example.faceship.Adapters;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.faceship.Fragments.AddPostFragment;
import com.example.faceship.Fragments.AllPostsFragment;
import com.example.faceship.Fragments.AllUsersFragment;
import com.example.faceship.Fragments.ChatWithFragment;
import com.example.faceship.Fragments.FollowPostsFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private boolean isChat;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context,boolean isChat) {
        super(fm, behavior);
        this.isChat=isChat;
        this.context=context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment=null;
        if(!isChat)
        {
            if(position==0)
            {
                fragment=new AllPostsFragment();
            }
            else if(position==1)
            {
                fragment=new FollowPostsFragment();
            }
            else if(position==2)
            {
                fragment=new AddPostFragment();
            }
        }
        else
        {
            if (position==0)
            {
                fragment=new ChatWithFragment();
            }
            if(position==1)
            {
                fragment=new AllUsersFragment();
            }
        }
        return fragment;
    }

    @Override
    public int getCount() {
        if(isChat)
            return 2;
        return 3;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if(!isChat)
        {
            if(position==0)
            {
                title="Рекомендации";
            }
            else if(position==1)
            {
                title="Подписки";
            }
            else if(position==2)
            {
                title="Новый пост";
            }
        }
        else
        {
            if (position==0)
            {
                title="Чаты";
            }
            if(position==1)
            {
                title="Пользователи";
            }
        }
        return title;
    }
}
