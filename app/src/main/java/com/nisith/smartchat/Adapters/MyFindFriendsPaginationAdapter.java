package com.nisith.smartchat.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.firebase.auth.FirebaseAuth;
import com.nisith.smartchat.Model.UserProfile;
import com.nisith.smartchat.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyFindFriendsPaginationAdapter extends FirebaseRecyclerPagingAdapter<UserProfile, MyFindFriendsPaginationAdapter.MyViewHolder> {

    public interface OnCardItemClickListener{
        void onCardItemClick(View view, String userProfileId);
    }

    private OnCardItemClickListener cardItemClickListener;

    public MyFindFriendsPaginationAdapter(@NonNull DatabasePagingOptions<UserProfile> options, AppCompatActivity appCompatActivity) {
        super(options);
        this.cardItemClickListener = (OnCardItemClickListener) appCompatActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_appearence_for_find_friends_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position, @NonNull UserProfile userProfile) {
            viewHolder.userNameTextView.setText(userProfile.getUserName());
            viewHolder.userStatusTextView.setText(userProfile.getUserStatus());
            String imageUrl = userProfile.getProfileImage();
            if (!imageUrl.equalsIgnoreCase("default")) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.user_icon).into(viewHolder.profileImageView);
            } else {
                Picasso.get().load(R.drawable.user_icon).placeholder(R.drawable.user_icon).into(viewHolder.profileImageView);
            }
    }

    @Override
    protected void onLoadingStateChanged(@NonNull LoadingState state) {

    }



    class MyViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImageView;
        TextView userNameTextView, userStatusTextView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            userNameTextView = itemView.findViewById(R.id.user_name_text_view);
            userStatusTextView = itemView.findViewById(R.id.user_status_text_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userProfileId = getRef(getAbsoluteAdapterPosition()).getKey();
                    cardItemClickListener.onCardItemClick(v, userProfileId);
                }
            });

            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userProfileId = getRef(getAbsoluteAdapterPosition()).getKey();
                    cardItemClickListener.onCardItemClick(v, userProfileId);
                }
            });
        }
    }
}
