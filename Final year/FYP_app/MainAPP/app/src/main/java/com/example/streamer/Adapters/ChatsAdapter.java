package com.example.streamer.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.streamer.Models.Chats;
import com.example.streamer.Models.User;
import com.example.streamer.R;
import com.example.streamer.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.CustomViewHolder> {

    List<Chats> chatsList;
    Context context;
    DatabaseReference UsersReference;
    SharedPrefs sharedPrefs;


    public static   onItemClickListener mListener;

    public  interface onItemClickListener{
        void  onItemClick(int position,View view);

    }
    public  void setOnItemClickListener(onItemClickListener listener){
          mListener=listener;
     }

     public static class  CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
     TextView receiverName,lastMessage;
     ImageView profileImage, lastImage;

     public CustomViewHolder(View itemView, final onItemClickListener listener) {
         super(itemView);

         receiverName=itemView.findViewById(R.id.receiverName);
         lastMessage=itemView.findViewById(R.id.lastMessage);
         profileImage=itemView.findViewById(R.id.profile_pic);
         lastImage =itemView.findViewById(R.id.lastImage);

         itemView.setOnClickListener(this);

     }

         @Override
         public void onClick(View v) {

            mListener.onItemClick(getAdapterPosition(),v);
         }
     }


    public ChatsAdapter(List<Chats> chatsList, Context context) {
        this.chatsList= chatsList;
        this.context = context;
        UsersReference= FirebaseDatabase.getInstance().getReference("Users");
        sharedPrefs=new SharedPrefs(context);

    }
    @Override
    public int getItemViewType(int position) {
            return R.layout.recycler_view_chat_item;
    }
    @Override
    public int getItemCount() {
        return  chatsList.size();
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(viewType, parent, false),mListener);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

        Chats chat=chatsList.get(position);


        if(!chat.getLastImage().isEmpty()||!chat.getLastAudio().isEmpty()) {

            holder.lastImage.setVisibility(View.VISIBLE);

            if(!chat.getLastImage().isEmpty()){
                holder.lastImage.setImageResource(R.drawable.place_holder);
            }
            else
            {
                holder.lastImage.setImageResource(R.drawable.ic_mic_gray);
            }

        }
        else
        {
            holder.lastImage.setVisibility(View.GONE);
        }


        if(chat.getLastMessage().isEmpty()){

            if(!chat.getLastImage().isEmpty()){
                holder.lastMessage.setText("Photo");
            }
            else
            {
                holder.lastMessage.setText("Audio");
            }


        }
        else
        {
            holder.lastMessage.setText(chat.getLastMessage());
        }






        mGetUserInfo(mGetReceiverId(chat),holder.profileImage,holder.receiverName);

    }



    private void mGetUserInfo(String id, ImageView profilePic, TextView userName){

        UsersReference.
                child(id).
                addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        User user=snapshot.getValue(User.class);

                        Picasso.get().load(user.getImageUrl()).
                                placeholder(R.drawable.ic_profile).
                                into(profilePic);

                        userName.setText(user.getUserName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private String mGetReceiverId(Chats chat){


        if(sharedPrefs.userId().equals(chat.getUsersList().get(0))){
            return chat.getUsersList().get(1);
        }
        else
        {
            return chat.getUsersList().get(0);
        }
    }

}
