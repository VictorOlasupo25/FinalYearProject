package com.example.streamer.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.streamer.Helper.PostHelper;
import com.example.streamer.Models.Post;
import com.example.streamer.Models.User;
import com.example.streamer.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.CustomViewHolder> {

    List<User> users;
    Context context;


    public static   onItemClickListener mListener;

    public  interface onItemClickListener{
        void  onItemClick(int position,View view);

    }
    public  void setOnItemClickListener(onItemClickListener listener){
          mListener=listener;
     }

     public static class  CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
     TextView userName;
     ImageView profileImage;

     public CustomViewHolder(View itemView, final onItemClickListener listener) {
         super(itemView);
         userName=itemView.findViewById(R.id.username);
         profileImage=itemView.findViewById(R.id.profile_pic);

         itemView.setOnClickListener(this);

     }

         @Override
         public void onClick(View v) {

            mListener.onItemClick(getAdapterPosition(),v);
         }
     }


    public UserAdapter(List<User> users, Context context) {
        this.users= users;
        this.context = context;


    }
    @Override
    public int getItemViewType(int position) {
            return R.layout.recycler_view_user_item;
    }
    @Override
    public int getItemCount() {
        return  users.size();
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(viewType, parent, false),mListener);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {



        User user=users.get(position);
        holder.userName.setText(user.getUserName());


        Picasso.get().load(user.getImageUrl()).
                placeholder(R.drawable.default_profile).
                into(holder.profileImage);



    }



}
