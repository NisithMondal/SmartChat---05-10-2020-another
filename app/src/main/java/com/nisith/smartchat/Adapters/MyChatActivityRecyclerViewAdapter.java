package com.nisith.smartchat.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nisith.smartchat.ChatActivity;
import com.nisith.smartchat.Constant;
import com.nisith.smartchat.Model.Message;
import com.nisith.smartchat.Model.UserProfile;
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
    private String friendType; //one to one chat or Group chat
    private DatabaseReference usersProfileRootDatabaseRef;

    public MyChatActivityRecyclerViewAdapter(List<Message> messagesList, ChatActivity chatActivity){
        this.messageList = messagesList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.chatActivity = chatActivity;
        usersProfileRootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (friendType != null) {
            String currentDate = getCurrentDate();
            Message message = messageList.get(position);
            if (currentDate.equals(message.getDate())) {
                //Means today's date
                chatActivity.setMessageDateTextView("today");
            } else {
                chatActivity.setMessageDateTextView(message.getDate());
            }
            if (friendType.equals(Constant.SINGLE_FRIEND)){
                bindMessagesForOneToOneChat(holder, position, currentDate, message);

            }else if (friendType.equals(Constant.GROUP_FRIEND)){
                bindMessagesForGroupChat(holder, position, currentDate, message);
            }
        }

    }

    private void bindMessagesForOneToOneChat(MyViewHolder holder, int position, String currentDate, Message message){
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
            holder.groupMessageSenderNameTextView.setVisibility(View.GONE);
        }
    }

    private void bindMessagesForGroupChat(MyViewHolder holder, int position, String currentDate, Message message){
        String senderId = message.getSenderUid();
        if (senderId.equals(currentUserId)){
            //current user send group messages
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
            //current user receives group messages
            getFriendProfileData(message.getSenderUid(), holder);
            holder.receiveMessageTextView.setText(message.getMessage());
            if (currentDate.equals(message.getDate())) {
                holder.receiveMessageTimeTextView.setText(message.getTime());
            }else {
                holder.receiveMessageTimeTextView.setText(message.getDate() + " " + message.getTime());
            }
            holder.profileImageView.setVisibility(View.VISIBLE);
            holder.sendMessageRootLayout.setVisibility(View.GONE);
            holder.receiveMessageRootLayout.setVisibility(View.VISIBLE);
            holder.messageSeenStatusImageView.setVisibility(View.GONE);

        }
    }

    private void getFriendProfileData(String friendKey,final MyViewHolder holder){
        usersProfileRootDatabaseRef.child(friendKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserProfile userProfile = snapshot.getValue(UserProfile.class);
                            if (userProfile != null){
                                String senderName = userProfile.getUserName();
                                holder.groupMessageSenderNameTextView.setText(senderName);
                                String imageUrl = userProfile.getProfileImage();
                                Picasso.get().load(imageUrl).placeholder(R.drawable.default_user_icon).into(holder.profileImageView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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

    public void setFriendType(String friendType){
        this.friendType = friendType;
    }


    private String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("MMM dd, yyyy");
        return date.format(calendar.getTime());
    }

   static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView sendMessageTextView, receiveMessageTextView, sendMessageTimeTextView, receiveMessageTimeTextView, groupMessageSenderNameTextView;
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
            groupMessageSenderNameTextView = itemView.findViewById(R.id.group_message_sender_name_text_view);
            sendMessageRootLayout = itemView.findViewById(R.id.send_message_root_layout);
            receiveMessageRootLayout = itemView.findViewById(R.id.receive_message_root_layout);
            messageDateRootLayout = itemView.findViewById(R.id.message_date_root_layout);
            messageSeenStatusImageView = itemView.findViewById(R.id.message_seen_status_image_view);
        }
    }


}
