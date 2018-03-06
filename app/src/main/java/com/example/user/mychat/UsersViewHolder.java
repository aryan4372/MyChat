package com.example.user.mychat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by USER on 28-01-2018.
 */

public class UsersViewHolder extends RecyclerView.ViewHolder {

    public View mView;
    public UsersViewHolder(View itemView) {
        super(itemView);
        mView=itemView;
    }

    public void setName(String name)
    {
        TextView userName=(TextView)mView.findViewById(R.id.users_name);
        userName.setText(name);
    }

    public void setStatus(String status)
    {
        TextView userStatus=(TextView)mView.findViewById(R.id.users_status);
        userStatus.setText(status);
    }

    public void setImage(String thumb_image, Context ctx)
    {
        CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.users_image);
        Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.images).into(userImageView);
    }
}
