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
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class RegisterActivity extends AppCompatActivity {
    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private ProgressBar progressBar;
    private CircleImageView profileImageView;
    private EditText userNameEditText, emailEditText, passwordEditText;
    private Button createAccountButton;
    private TextView haveAnAccountTextView;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private StorageReference rootStorageReference;
    private DatabaseReference rootDatabaseReference;
    private byte[] profileImageByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        toolbarTextView.setText("Create Account");
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        profileImageView.setOnClickListener(new MyClickListener());
        createAccountButton.setOnClickListener(new MyClickListener());
        haveAnAccountTextView.setOnClickListener(new MyClickListener());
        progressBar.setVisibility(View.GONE);
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        rootDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        rootStorageReference = FirebaseStorage.getInstance().getReference().child("all_user_picture");
    }

    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        progressBar = findViewById(R.id.progress_bar);
        profileImageView = findViewById(R.id.profile_image_view);
        userNameEditText = findViewById(R.id.user_name_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        createAccountButton = findViewById(R.id.create_account_button);
        haveAnAccountTextView = findViewById(R.id.already_have_account_text_view);
    }


    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.profile_image_view:
                    openGallery();
                    break;

                case R.id.create_account_button:
                    createAccountWithEmailAndPassword();
                    break;

                case R.id.already_have_account_text_view:
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
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
                    Picasso.get().load(profileImageUri).placeholder(R.drawable.default_user_icon).into(profileImageView);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                     profileImageByteArray = baos.toByteArray();
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




    private void createAccountWithEmailAndPassword(){
        final String userName = userNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(userName)){
            userNameEditText.setError("Enter User Name");
            userNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)){
            emailEditText.setError("Enter Email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)){
            passwordEditText.setError("Enter Password");
            passwordEditText.requestFocus();
            return;
        }else if (password.length()<6){
            passwordEditText.setError("Password must contain at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        createAccountButton.setEnabled(false);
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //account created successfully
                            UserProfile currentUserProfile = new UserProfile(userName,Constant.USER_DEFAULT_STATUS,"default");
                            rootDatabaseReference.child(firebaseAuth.getCurrentUser().getUid()).setValue(currentUserProfile)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                //Upload profile image and thumbnail and store url in database
                                                uploadUserProfileImages();
                                            }else {
                                                Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }else {
                            progressBar.setVisibility(View.GONE);
                            createAccountButton.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void uploadUserProfileImages(){
        if (profileImageByteArray != null){
            //Means user select an image from gallery
            //Upload user profile image to Firebase Storage
            rootStorageReference.child("users_profile_image").child(firebaseAuth.getCurrentUser().getUid()+".jpg")
                    .putBytes(profileImageByteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            progressBar.setVisibility(View.GONE);
                            createAccountButton.setEnabled(true);
                            openHomeActivity();
                            finishAffinity();
                            Uri imageUri = task.getResult();
                            if (imageUri != null){
                                String profileImageUrl = imageUri.toString();
                                Map<String,Object> map = new HashMap<>();
                                map.put("profileImage",profileImageUrl);
                                rootDatabaseReference.child(firebaseAuth.getCurrentUser().getUid()).updateChildren(map);
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    createAccountButton.setEnabled(true);
                    openHomeActivity();
                    finishAffinity();
                }
            });
        }else {
            Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            createAccountButton.setEnabled(true);
            openHomeActivity();
            finishAffinity();
        }
    }


    private void openHomeActivity(){
        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
    }
}