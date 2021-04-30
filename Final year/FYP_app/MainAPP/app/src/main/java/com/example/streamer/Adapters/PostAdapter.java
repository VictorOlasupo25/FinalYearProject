package com.example.streamer.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.streamer.Helper.PostHelper;
import com.example.streamer.Models.Post;
import com.example.streamer.R;
import com.example.streamer.Models.User;
import com.example.streamer.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.CustomViewHolder> {

    List<Post> posts;
    Context context;
    DatabaseReference UsersReference;
    boolean userPost=false;
    SharedPrefs sharedPrefs;

    public static   onItemClickListener mListener;

    public  interface onItemClickListener{
        void  onItemClick(int position,View view);
        void  onAudioClick(int position, SeekBar seekBar, ImageView playBtn);

    }
    public  void setOnItemClickListener(onItemClickListener listener){
          mListener=listener;
     }

     public static class  CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        SeekBar audioSeekBar;

     TextView textViewCaption,userName,likedCounts;
     RelativeLayout audioLayout,likeBtn;
     ImageView imageViewPostImage,profileImage,deleteBtn,playBtn,likeThumb;

     public CustomViewHolder(View itemView, final onItemClickListener listener) {
         super(itemView);
         textViewCaption=itemView.findViewById(R.id.textViewCaption);
         userName=itemView.findViewById(R.id.username);
         imageViewPostImage=itemView.findViewById(R.id.imageViewPostImage);
         profileImage=itemView.findViewById(R.id.profile_pic);
         likeBtn=itemView.findViewById(R.id.like);
         likeThumb=itemView.findViewById(R.id.likeImage);
         likedCounts=itemView.findViewById(R.id.likedCount);
         deleteBtn=itemView.findViewById(R.id.imageViewDelete);
         playBtn=itemView.findViewById(R.id.playBtn);
         audioLayout=itemView.findViewById(R.id.audio_layout);
         audioSeekBar=itemView.findViewById(R.id.seekBar);

         deleteBtn.setOnClickListener(this);
         likeBtn.setOnClickListener(this);

         playBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 mListener.onAudioClick(getAdapterPosition(),itemView.findViewWithTag(getAdapterPosition()),(ImageView) v);
             }
         });

     }

         @Override
         public void onClick(View v) {

            mListener.onItemClick(getAdapterPosition(),v);
         }
     }


    public PostAdapter(List<Post> posts, Context context,boolean userPost) {
        this.posts= posts;
        this.context = context;
        this.userPost=userPost;
        UsersReference=FirebaseDatabase.getInstance().getReference("Users");
        sharedPrefs=new SharedPrefs(context);
    }
    @Override
    public int getItemViewType(int position) {
            return R.layout.recycler_view_post_item;
    }
    @Override
    public int getItemCount() {
        return  posts.size();
    }
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false),mListener);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

        if(userPost){holder.deleteBtn.setVisibility(View.VISIBLE);
        }


        holder.audioSeekBar.setTag(position);

        Post post=posts.get(position);

        if(!post.getAudioUrl().isEmpty()){holder.audioLayout.setVisibility(View.VISIBLE);}
        else{holder.audioLayout.setVisibility(View.GONE);}



        if(post.getCaption().isEmpty()){
            holder.textViewCaption.setVisibility(View.GONE);
        }
        else
        {
            holder.textViewCaption.setVisibility(View.VISIBLE);
            holder.textViewCaption.setText(post.getCaption());
        }

        if(!post.getImageUrl().isEmpty()) {
            Picasso.get().load(post.getImageUrl()).
                    placeholder(R.drawable.simple_placeholder).
                    into(holder.imageViewPostImage);

            holder.imageViewPostImage.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.imageViewPostImage.setVisibility(View.GONE);
        }

        holder.likedCounts.setText(String.valueOf(post.getLikesList().size()));

      if(post.getLikesList().contains(sharedPrefs.userId())) {

          holder.likeThumb.setImageResource(R.drawable.ic_like_filled);
      }
      else
      {
          holder.likeThumb.setImageResource(R.drawable.ic_like);
      }


          mGetUserInfo(post, holder.profileImage, holder.userName);


    }


    private void mGetUserInfo(Post post, ImageView profilePic,TextView userName){

        UsersReference.
                child(post.getUserId()).
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
}
