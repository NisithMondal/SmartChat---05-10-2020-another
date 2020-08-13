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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nisith.smartchat.Model.FriendsGroup;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateGroupActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private ProgressBar progressBar;
    private CircleImageView groupProfileImageView;
    private EditText groupNameEditText, aboutGroupEditView;
    private Button createGroupButton;

    //Firebase
    private DatabaseReference groupDatabaseRef;
    private StorageReference rootStorageReference;
    private byte[] groupProfileImageByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        initializeViews();
        progressBar.setVisibility(View.GONE);
        groupProfileImageView.setOnClickListener(new MyClickListener());
        createGroupButton.setOnClickListener(new MyClickListener());
        //Firebase
        groupDatabaseRef = FirebaseDatabase.getInstance().getReference().child("groups");
        rootStorageReference = FirebaseStorage.getInstance().getReference().child("all_user_picture").child("groups_profile_image");
    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        progressBar = findViewById(R.id.progress_bar);
        groupProfileImageView = findViewById(R.id.group_profile_image_view);
        groupNameEditText = findViewById(R.id.group_name_edit_text);
        aboutGroupEditView = findViewById(R.id.about_group_edit_text);
        createGroupButton = findViewById(R.id.create_group_button);


    }


    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.group_profile_image_view:
                    openGallery();
                    break;

                case R.id.create_group_button:
                    createGroup();
                    break;

            }

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
                    Picasso.get().load(profileImageUri).placeholder(R.drawable.default_user_icon).placeholder(R.drawable.group_icon).into(groupProfileImageView);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    groupProfileImageByteArray = baos.toByteArray();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
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


    private void createGroup(){
        String groupName = groupNameEditText.getText().toString().trim();
        String aboutGroup = aboutGroupEditView.getText().toString().trim();
        if (TextUtils.isEmpty(groupName)){
            groupNameEditText.setError("Enter Group Name");
            groupNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(aboutGroup)){
            aboutGroupEditView.setError("Write something about this group");
            aboutGroupEditView.requestFocus();
            return;
        }
        //Firebase
        final String groupKey = groupDatabaseRef.push().getKey();
        if (groupKey != null) {
            progressBar.setVisibility(View.VISIBLE);
            FriendsGroup friendsGroup = new FriendsGroup(groupName,aboutGroup,"default");
            groupDatabaseRef.child(groupKey).setValue(friendsGroup)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //group created successfully
                               //upload group image icon
                                uploadGroupProfileImage(groupKey);
                            }else {
                                //group not created successfully
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(CreateGroupActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }else {
            //if push key is not generated
            Toast.makeText(this, "Group not create. Try again...", Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadGroupProfileImage(final String groupKey){
        if (groupProfileImageByteArray != null){
            //Means user select an image from gallery
            //Upload user profile image to Firebase Storage
            rootStorageReference.child(groupKey+".jpg")
                    .putBytes(groupProfileImageByteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            progressBar.setVisibility(View.GONE);
                            Uri imageUri = task.getResult();
                            if (imageUri != null){
                                String imageUrl = imageUri.toString();
                                Map<String, Object> map = new HashMap<>();
                                map.put("groupProfileImage", imageUrl);
                                groupDatabaseRef.child(groupKey).updateChildren(map);
                            }
                            Toast.makeText(CreateGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                            //Open group profile Activity
                            openGroupProfileActivity();
                            finish();
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //only image not upload so,
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CreateGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                            //Open group profile Activity
                            openGroupProfileActivity();
                            finish();
                        }
                    });
        }else {
            // if group profile icon is default. Means user not selected any icon
            progressBar.setVisibility(View.GONE);
            Toast.makeText(CreateGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
            //Open group profile Activity
            openGroupProfileActivity();
            finish();
        }
    }


    private void openGroupProfileActivity(){
        Intent intent = new Intent(CreateGroupActivity.this, GroupProfileActivity.class);
        startActivity(intent);
    }

}