package com.example.streamer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.streamer.Models.ChatMessage;
import com.example.streamer.R;
import com.example.streamer.SharedPrefs;
import com.squareup.picasso.Picasso;

import java.util.List;


public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int RIGHT_ITEM = 1;
    private final int LEFT_ITEM = 2;
    List<ChatMessage> chatMessageList;
    Context context;
    SharedPrefs sharedPrefs;


    public static   onItemClickListener mListener;

    public  interface onItemClickListener{
        void  onItemClick(int position,View view);
        void  onAudioClick(int position, SeekBar seekBar, ImageView playBtn);

    }
    public  void setOnItemClickListener(onItemClickListener listener){
          mListener=listener;
     }

     public  class RightItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
     TextView rightMessage,rightImageMessage;
     ImageView rightImage,rightPlayBtn;
     CardView rightCard,rightAudioCard;
     SeekBar rightSeekBar;

     public RightItemViewHolder(View itemView) {
         super(itemView);

         rightImageMessage=itemView.findViewById(R.id.rightImageText);
         rightMessage=itemView.findViewById(R.id.rightMessage);
         rightImage=itemView.findViewById(R.id.rightImageView);
         rightCard=itemView.findViewById(R.id.rightImageCard);
         rightAudioCard=itemView.findViewById(R.id.rightAudioCard);
         rightSeekBar=itemView.findViewById(R.id.rightSeekBar);
         rightPlayBtn=itemView.findViewById(R.id.rightPlayBtn);


         rightPlayBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 mListener.onAudioClick(getAdapterPosition(),rightSeekBar,(ImageView) v);
             }
         });


     }

         @Override
         public void onClick(View v) {

            mListener.onItemClick(getAdapterPosition(),v);
         }
     }


    public  class LeftItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView leftMessage,leftImageMessage;
        ImageView leftImage,leftPlayBtn;
        CardView leftCard,leftAudioCard;
        SeekBar leftSeekBar;


        public LeftItemViewHolder(View itemView) {
            super(itemView);

            leftImageMessage=itemView.findViewById(R.id.leftImageText);
            leftMessage=itemView.findViewById(R.id.leftMessage);
            leftImage=itemView.findViewById(R.id.leftImageView);
            leftCard=itemView.findViewById(R.id.leftImageCard);
            leftAudioCard=itemView.findViewById(R.id.leftAudioCard);
            leftSeekBar=itemView.findViewById(R.id.leftSeekBar);
            leftPlayBtn=itemView.findViewById(R.id.leftPlayBtn);


            leftPlayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onAudioClick(getAdapterPosition(),leftSeekBar,(ImageView) v);
                }
            });

        }

        @Override
        public void onClick(View v) {

            mListener.onItemClick(getAdapterPosition(),v);
        }
    }


    public MessagesAdapter(List<ChatMessage> chatMessageList, Context context) {
        this.chatMessageList = chatMessageList;
        this.context = context;

        sharedPrefs=new SharedPrefs(context);

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if (viewType == RIGHT_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
            return new RightItemViewHolder(view);

        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
            return new LeftItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatMessage chatMessage = chatMessageList.get(position);

        if(getItemViewType(position)==RIGHT_ITEM){


            if(chatMessage.getImageUrl().isEmpty()&&chatMessage.getAudioUrl().isEmpty()){

                ((RightItemViewHolder)holder).rightAudioCard.setVisibility(View.GONE);
                ((RightItemViewHolder)holder).rightCard.setVisibility(View.GONE);
                ((RightItemViewHolder)holder).rightMessage.setVisibility(View.VISIBLE);
                ((RightItemViewHolder)holder).rightMessage.setText(chatMessage.getMessage());
            }
            else if(!chatMessage.getImageUrl().isEmpty())
            {

                ((RightItemViewHolder)holder).rightAudioCard.setVisibility(View.GONE);


                if(chatMessage.getMessage().isEmpty()){
                    ((RightItemViewHolder)holder).rightImageMessage.setVisibility(View.GONE);
                }
                else
                {
                    ((RightItemViewHolder)holder).rightImageMessage.setVisibility(View.VISIBLE);
                    ((RightItemViewHolder)holder).rightImageMessage.setText(chatMessage.getMessage());
                }

                ((RightItemViewHolder)holder).rightCard.setVisibility(View.VISIBLE);
                ((RightItemViewHolder)holder).rightMessage.setVisibility(View.GONE);


                Picasso.get().load(chatMessage.getImageUrl()).
                        placeholder(R.drawable.simple_placeholder).
                        into(((RightItemViewHolder) holder).rightImage);
            }
            else if(!chatMessage.getAudioUrl().isEmpty()){

                ((RightItemViewHolder)holder).rightAudioCard.setVisibility(View.VISIBLE);
                ((RightItemViewHolder)holder).rightCard.setVisibility(View.GONE);
                ((RightItemViewHolder)holder).rightMessage.setVisibility(View.GONE);


            }





        }
        else
        {

            if(chatMessage.getImageUrl().isEmpty()&&chatMessage.getAudioUrl().isEmpty()){

                ((LeftItemViewHolder)holder).leftAudioCard.setVisibility(View.GONE);

                ((LeftItemViewHolder)holder).leftCard.setVisibility(View.GONE);
                ((LeftItemViewHolder)holder).leftMessage.setVisibility(View.VISIBLE);
                ((LeftItemViewHolder)holder).leftMessage.setText(chatMessage.getMessage());
            }
            else if(!chatMessage.getImageUrl().isEmpty())
            {

                ((LeftItemViewHolder)holder).leftAudioCard.setVisibility(View.GONE);


                if(chatMessage.getMessage().isEmpty()){
                    ((LeftItemViewHolder)holder).leftImageMessage.setVisibility(View.GONE);
                }
                else
                {
                    ((LeftItemViewHolder)holder).leftImageMessage.setVisibility(View.VISIBLE);
                    ((LeftItemViewHolder)holder).leftImageMessage.setText(chatMessage.getMessage());
                }

                ((LeftItemViewHolder)holder).leftCard.setVisibility(View.VISIBLE);
                ((LeftItemViewHolder)holder).leftMessage.setVisibility(View.GONE);


                Picasso.get().load(chatMessage.getImageUrl()).
                        placeholder(R.drawable.simple_placeholder).
                        into(((LeftItemViewHolder) holder).leftImage);
            }
            else if(!chatMessage.getAudioUrl().isEmpty()){

                ((LeftItemViewHolder)holder).leftAudioCard.setVisibility(View.VISIBLE);
                ((LeftItemViewHolder)holder).leftCard.setVisibility(View.GONE);
                ((LeftItemViewHolder)holder).leftMessage.setVisibility(View.GONE);


            }
        }






    }




    @Override
    public int getItemViewType(int position) {

        if(chatMessageList.get(position).getSenderId().equals(sharedPrefs.userId())){
            return RIGHT_ITEM; }
        else { return LEFT_ITEM; }

    }


    @Override
    public int getItemCount() {
        return  chatMessageList.size();
    }


}
