package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

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
import com.iceteck.silicompressorr.SiliCompressor;
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
    private ProgressBar progressBar;
    private CircleImageView profileImageView;
    private EditText userNameEditText, emailEditText, passwordEditText;
    private Button createAccountButton;
    private TextView haveAnAccountTextView;
    //Firebase
    private FirebaseAuth firebaseAuth;
    private StorageReference rootStorageReference;
    private DatabaseReference rootDatabaseReference;
    private Uri profileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeViews();
        setSupportActionBar(appToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileImageView.setOnClickListener(new MyClickListener());
        createAccountButton.setOnClickListener(new MyClickListener());
        haveAnAccountTextView.setOnClickListener(new MyClickListener());
        progressBar.setVisibility(View.GONE);
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        rootDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        rootStorageReference = FirebaseStorage.getInstance().getReference().child("user_profile_picture");
    }

    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
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
                profileImageUri = result.getUri();
                Picasso.get().load(profileImageUri).into(profileImageView);
//                File thumbnailFile = new File(Objects.requireNonNull(profileImageUri.getPath()));
////                String filePath = SiliCompressor.with(getApplicationContext()).compress(profileImageUri.getPath(),null);
//                try {
//                    Bitmap thumbBitmap = SiliCompressor.with(getApplicationContext()).getCompressBitmap(profileImageUri.getPath());
//                    Log.d("ABCD","thumbBitmap = "+thumbBitmap);
//                    Uri uri = getImageUri(thumbBitmap, Bitmap.CompressFormat.PNG,70);
//
//                    Log.d("ABCD","uri = "+uri);
//                } catch (IOException e) {
//                    Log.d("ABCD","error = "+e);
//                    e.printStackTrace();
//                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Not Selected", Toast.LENGTH_SHORT).show();
            }
        }


    }


    public Uri getImageUri(Bitmap src, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        src.compress(format, quality, os);

        String path = MediaStore.Images.Media.insertImage(getContentResolver(), src, "title", null);
        return Uri.parse(path);
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
                            UserProfile currentUserProfile = new UserProfile(userName,Constant.USER_DEFAULT_STATUS,"default","default");
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
                            Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void uploadUserProfileImages(){
        if (profileImageUri != null){
            //Means user select an image from gallery
            //Upload user profile image to Firebase Storage
            rootStorageReference.child("user_profile_image").child(firebaseAuth.getCurrentUser().getUid()+".jpg")
                    .putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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