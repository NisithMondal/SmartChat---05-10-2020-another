package com.nisith.smartchat.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.nisith.smartchat.Model.UserNotification;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyNotificationFragmentRecyclerAdapter extends FirebaseRecyclerAdapter<UserNotification, MyNotificationFragmentRecyclerAdapter.MyViewHolder> {

    public interface NotificationItemClickListener{
        void onNotificationItemClick(View view, String senderUid, String groupId, String notificationKey);
    }


    private NotificationItemClickListener notificationItemClickListener;

    public MyNotificationFragmentRecyclerAdapter(@NonNull FirebaseRecyclerOptions<UserNotification> options, NotificationItemClickListener notificationItemClickListener) {
        super(options);
        this.notificationItemClickListener = notificationItemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_notification_layout, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull UserNotification notification) {
        holder.titleTextView.setText(notification.getTitle());
        holder.bodyTextView.setText(notification.getBody());
        holder.dateTimeTextView.setText(notification.getDateTime());
        String profileImageUrl = notification.getImageUrl();
        if (!profileImageUrl.equalsIgnoreCase("default")) {
            Picasso.get().load(profileImageUrl).placeholder(R.drawable.user_icon).into(holder.profileImageView);
        } else {
            Picasso.get().load(R.drawable.user_icon).placeholder(R.drawable.user_icon).into(holder.profileImageView);
        }

    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView, bodyTextView, dateTimeTextView;
        ImageView deleteImageView;
        CircleImageView profileImageView;
        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            bodyTextView = itemView.findViewById(R.id.body_text_view);
            dateTimeTextView = itemView.findViewById(R.id.date_text_view);
            deleteImageView = itemView.findViewById(R.id.delete_image_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notificationItemClickListener.onNotificationItemClick(v, getItem(getAbsoluteAdapterPosition()).getSenderUid(),
                            getItem(getAbsoluteAdapterPosition()).getGroupUid(), getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notificationItemClickListener.onNotificationItemClick(v, getItem(getAbsoluteAdapterPosition()).getSenderUid(),
                            getItem(getAbsoluteAdapterPosition()).getGroupUid(), getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });

            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notificationItemClickListener.onNotificationItemClick(v, getItem(getAbsoluteAdapterPosition()).getSenderUid(),
                            getItem(getAbsoluteAdapterPosition()).getGroupUid(), getRef(getAbsoluteAdapterPosition()).getKey());
                }
            });


        }
    }
}
