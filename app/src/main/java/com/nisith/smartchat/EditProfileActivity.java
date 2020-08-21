package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nisith.smartchat.Model.UserProfile;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private ProgressBar progressBar;
    private CircleImageView profileImageView;
    private ImageView cameraImageView, nameEditIcon, statusEditIcon, infoEditIcon;
    private EditText userNameEditText, userStatusEditText, userInfoEditText;
    private Button updateProfileButton;
    private boolean isInfoEditTextValueChange = false;// 'true' if about info edit text is enabled else 'false'
    //Firebase
    private DatabaseReference databaseRef, aboutMeDatabaseRef;
    private StorageReference rootStorageReference;
    private String currentUserId;
    private ValueEventListener valueEventListener, aboutMeValueEventListener;
    private String userName;
    private String profileImageUrl;
    private byte[] profileImageByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        toolbarTextView.setText("Update Profile");
        progressBar.setVisibility(View.GONE);
        //Firebase
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        rootStorageReference = FirebaseStorage.getInstance().getReference().child("all_user_picture").child("users_profile_image");
        aboutMeDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users_detail_info").child(currentUserId).child("aboutMe");
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        profileImageView.setOnClickListener(new MyClickListener());
        cameraImageView.setOnClickListener(new MyClickListener());
        nameEditIcon.setOnClickListener(new MyClickListener());
        statusEditIcon.setOnClickListener(new MyClickListener());
        infoEditIcon.setOnClickListener(new MyClickListener());
//        userNameEditText.setOnClickListener(new MyClickListener());
//        userStatusEditText.setOnClickListener(new MyClickListener());
        updateProfileButton.setOnClickListener(new MyClickListener());


    }

    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        progressBar = findViewById(R.id.progress_bar);
        profileImageView = findViewById(R.id.profile_image_view);
        cameraImageView = findViewById(R.id.camera_image_view);
        nameEditIcon = findViewById(R.id.name_edit_icon);
        statusEditIcon = findViewById(R.id.status_edit_icon);
        userNameEditText = findViewById(R.id.user_name_edit_text);
        userStatusEditText = findViewById(R.id.user_status_edit_text);
        userInfoEditText = findViewById(R.id.user_info_edit_text);
        infoEditIcon = findViewById(R.id.user_info_edit_icon);
        updateProfileButton = findViewById(R.id.update_profile_button);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Firebase
        showUserProfile();
        fetchAboutUserDetailsInfo();
    }

    private void showUserProfile(){
        progressBar.setVisibility(View.VISIBLE);
        updateProfileButton.setVisibility(View.GONE);
        profileImageView.setEnabled(false);
        cameraImageView.setEnabled(false);
        userNameEditText.setEnabled(false);
        userStatusEditText.setEnabled(false);
        userInfoEditText.setEnabled(false);
        valueEventListener = databaseRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                UserProfile userProfile = snapshot.getValue(UserProfile.class);
                if (userProfile != null){
                    profileImageView.setEnabled(true);
                    cameraImageView.setEnabled(true);
                    userName = userProfile.getUserName();
                    userNameEditText.setText(userName);
                    profileImageUrl = userProfile.getProfileImage();
                    userStatusEditText.setText(userProfile.getUserStatus());

                    if (! profileImageUrl.equalsIgnoreCase("default")){
                        //means profileImage value is not default.
                        Picasso.get().load(profileImageUrl).placeholder(R.drawable.default_user_icon).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfileActivity.this, "Data Not Loaded", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void fetchAboutUserDetailsInfo(){
        aboutMeValueEventListener = aboutMeDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String aboutMe = snapshot.getValue().toString();
                    userInfoEditText.setText(aboutMe);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.profile_image_view:
                    displayProfileImage();
                    break;

                case R.id.camera_image_view:
                    openGallery();
//                    updateProfileButton.setVisibility(View.VISIBLE);
                    break;

                case R.id.name_edit_icon:
                    if (! userNameEditText.isEnabled()){
                        userNameEditText.setEnabled(true);
                    }
                    updateProfileButton.setVisibility(View.VISIBLE);
                    break;

                case R.id.status_edit_icon:
                    if (! userStatusEditText.isEnabled()){
                        userStatusEditText.setEnabled(true);
                    }
                    updateProfileButton.setVisibility(View.VISIBLE);
                    break;

                case R.id.user_info_edit_icon:
                    if (! userInfoEditText.isEnabled()){
                        userInfoEditText.setEnabled(true);
                    }
                    updateProfileButton.setVisibility(View.VISIBLE);
                    break;

                case R.id.update_profile_button:
                    updateUserProfile();
                    break;
            }

        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (valueEventListener != null && databaseRef != null){
            //Remove value event listener
            databaseRef.child(currentUserId).removeEventListener(valueEventListener);
        }
        if (aboutMeDatabaseRef != null){
            aboutMeDatabaseRef.removeEventListener(aboutMeValueEventListener);
        }

    }

    private void openGallery(){
        //Open Crop Image Activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                //Crop image uri
                Uri profileImageUri = result.getUri();
                Bitmap profileImageBitmap = getCompressImageBitmap(profileImageUri);
                if (profileImageBitmap != null) {
                    Picasso.get().load(profileImageUri).placeholder(R.drawable.default_user_icon).into(profileImageView);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    profileImageByteArray = baos.toByteArray();
                    updateProfileButton.setVisibility(View.VISIBLE);
                    Log.d("ZXCVB","updateProfileButton");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void updateUserProfile(){
        userNameEditText.setEnabled(false);
        userStatusEditText.setEnabled(false);
        updateProfileButton.setVisibility(View.GONE);
        String inputName = userNameEditText.getText().toString();
        String inputStatus = userStatusEditText.getText().toString();
        String userInfo = userInfoEditText.getText().toString();
        if (profileImageByteArray == null){
            //means user not select any image from gallery. So only update user name and status
           updateUserProfileDataToDatabase(inputName,inputStatus,profileImageUrl);

        }else {
            //means user select a image from gallery. So only update user name status and profile image.
            updateUserProfileWithImage(inputName,inputStatus);
        }

        if (userInfoEditText.isEnabled()){
            //If userInfoEditText is enabled means user edit his/her about info field. So update the value on firebase
            aboutMeDatabaseRef.setValue(userInfo);
        }

    }





    private void displayProfileImage(){
        Intent intent = new Intent(EditProfileActivity.this,ImageDisplayActivity.class);
        intent.putExtra(Constant.USER_NAME,userName);
        intent.putExtra(Constant.PROFILE_IMAGE_URL,profileImageUrl);
        startActivity(intent);
    }


    private Bitmap getCompressImageBitmap(Uri imageUri){
        //this method compress the image and return as a bitmap format
        Bitmap imageBitmap = null;
        File imageFile = new File(Objects.requireNonNull(imageUri.getPath()));
        try {
            imageBitmap = new Compressor(this)
                    .setMaxWidth(260)
                    .setMaxHeight(260)
                    .setQuality(75)
                    .compressToBitmap(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageBitmap;
    }


    private void updateUserProfileWithImage(final String userName, final String userStatus){
        if (profileImageByteArray != null){
            //Means user select an image from gallery
            //Upload user profile image to Firebase Storage
            progressBar.setVisibility(View.VISIBLE);
            rootStorageReference.child(currentUserId+".jpg")
                    .putBytes(profileImageByteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            progressBar.setVisibility(View.GONE);
                            Uri imageUri = task.getResult();
                            if (imageUri != null){
                                String newImageUrl = imageUri.toString();
                                //new uploaded image url is not null
                                updateUserProfileDataToDatabase(userName,userStatus,newImageUrl);
                                profileImageByteArray = null;
                            }else {
                                //new uploaded image url is null. So use previous profile image url
                                updateUserProfileDataToDatabase(userName,userStatus,profileImageUrl);
                            }
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }


    private void updateUserProfileDataToDatabase(String userName, String userStatus, String imageUrl){
        UserProfile userProfile = new UserProfile(userName,userStatus,imageUrl);
        Map<String,Object> map = new HashMap<>();
        map.put(currentUserId,userProfile);
        //Note that *** Do not call setValue() here. Because it will delete all the child data node present of that parent node except the new node. ***
        // *** Show be Careful ***
        databaseRef.updateChildren(map);
    }




}