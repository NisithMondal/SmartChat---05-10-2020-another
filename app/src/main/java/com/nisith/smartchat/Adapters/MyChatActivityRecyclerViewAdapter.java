package com.nisith.smartchat.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.nisith.smartchat.ChatActivity;
import com.nisith.smartchat.Model.Message;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyChatActivityRecyclerViewAdapter extends RecyclerView.Adapter<MyChatActivityRecyclerViewAdapter.MyViewHolder> {

    private List<Message> messageList;
    private String currentUserId, profileImageUrl;
    private ChatActivity chatActivity;

    public MyChatActivityRecyclerViewAdapter(List<Message> messagesList, ChatActivity chatActivity){
        this.messageList = messagesList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.chatActivity = chatActivity;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String currentDate = getCurrentDate();
        Message message = messageList.get(position);
        if (currentDate.equals(message.getDate())){
            //Means today's date
            chatActivity.setMessageDateTextView("today");
        }else {
            chatActivity.setMessageDateTextView(message.getDate());
        }
        String senderId = message.getSenderUid();
        if (senderId.equals(currentUserId)){
            holder.sendMessageTextView.setText(message.getMessage());
            if (message.isRead()){
                //message is seen by receiver
                holder.messageSeenStatusImageView.setImageResource(R.drawable.ic_read_icon);
            }else {
                //message is still not seen by receiver user
                holder.messageSeenStatusImageView.setImageResource(R.drawable.ic_not_read_icon);
            }
            if (currentDate.equals(message.getDate())) {
                holder.sendMessageTimeTextView.setText(message.getTime());
            }else {
                holder.sendMessageTimeTextView.setText(message.getDate() + " " + message.getTime());
            }
            holder.messageSeenStatusImageView.setVisibility(View.VISIBLE);
            holder.profileImageView.setVisibility(View.GONE);
            holder.sendMessageRootLayout.setVisibility(View.VISIBLE);
            holder.receiveMessageRootLayout.setVisibility(View.GONE);
        }else {
            holder.receiveMessageTextView.setText(message.getMessage());
            if (currentDate.equals(message.getDate())) {
                holder.receiveMessageTimeTextView.setText(message.getTime());
            }else {
                holder.receiveMessageTimeTextView.setText(message.getDate() + " " + message.getTime());
            }
            Picasso.get().load(profileImageUrl).placeholder(R.drawable.default_user_icon).into(holder.profileImageView);
            holder.profileImageView.setVisibility(View.VISIBLE);
            holder.sendMessageRootLayout.setVisibility(View.GONE);
            holder.receiveMessageRootLayout.setVisibility(View.VISIBLE);
            holder.messageSeenStatusImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        int totalItems = 0;
        if (messageList != null){
            totalItems = messageList.size();
        }
        return totalItems;
    }

    public void setProfileImageUrl(String profileImageUrl){
        this.profileImageUrl = profileImageUrl;
    }


    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("MMM dd, yyyy");
        return date.format(calendar.getTime());
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView sendMessageTextView, receiveMessageTextView, sendMessageTimeTextView, receiveMessageTimeTextView;
        RelativeLayout  sendMessageRootLayout, receiveMessageRootLayout;
        CardView messageDateRootLayout;
        CircleImageView profileImageView;
        ImageView messageSeenStatusImageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMessageTextView = itemView.findViewById(R.id.send_message_text_view);
            receiveMessageTextView = itemView.findViewById(R.id.receive_message_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            sendMessageTimeTextView = itemView.findViewById(R.id.send_message_time_text_view);
            receiveMessageTimeTextView = itemView.findViewById(R.id.receive_message_time_text_view);
            sendMessageRootLayout = itemView.findViewById(R.id.send_message_root_layout);
            receiveMessageRootLayout = itemView.findViewById(R.id.receive_message_root_layout);
            messageDateRootLayout = itemView.findViewById(R.id.message_date_root_layout);
            messageSeenStatusImageView = itemView.findViewById(R.id.message_seen_status_image_view);
        }
    }


}
