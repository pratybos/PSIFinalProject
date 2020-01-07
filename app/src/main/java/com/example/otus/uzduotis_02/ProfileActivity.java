package com.example.otus.uzduotis_02;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PICK_IMAGE_REQUEST = 172;
    private Button buttonLogout;
    private TextView textViewWelcome;
    private FirebaseAuth mAuth;

    private Button buttonSaveProfile;
    private EditText editTextUsername;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private Button buttonChoose;
    private Button buttonUpload;
    private TextView textViewPath;
    private ImageView imageViewProfilePicture;
    private Uri filePath;
    private StorageReference mStorageRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        buttonSaveProfile = (Button) findViewById(R.id.buttonSaveProfile);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);

        textViewWelcome = (TextView) findViewById(R.id.textViewWelcome);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        textViewPath = (TextView) findViewById(R.id.textViewPath);
        imageViewProfilePicture = (ImageView) findViewById(R.id.imageView);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        textViewWelcome.setText(mAuth.getCurrentUser().getEmail());
        downloadImage();

        progressDialog = new ProgressDialog(this);

        buttonLogout.setOnClickListener(this);
        buttonSaveProfile.setOnClickListener(this);
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
    }

    private void saveUserInformation(){
        if (!editTextUsername.getText().toString().equals("")){
            String name = editTextUsername.getText().toString().trim();

            UserInformation userInformation = new UserInformation(name);

            FirebaseUser user = mAuth.getCurrentUser();
            databaseReference.child(user.getUid()).setValue(userInformation);

            Toast.makeText(this, "Saving information...", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadImage(){
        mStorageRef.child("profileImages/" + mAuth.getCurrentUser().getUid() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Picasso.get().load(uri).into(imageViewProfilePicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Picasso.get().load("https://i.imgur.com/G4m6Op1.jpg").into(imageViewProfilePicture);
                //Toast.makeText(getApplicationContext(), "No profile image found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE_REQUEST);
    }
    
    private void uploadImage(){
        StorageReference riversRef = mStorageRef.child("profileImages/" + mAuth.getCurrentUser().getUid() + ".jpg");

        riversRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        downloadImage();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.setMessage("Uploading image...");
                    }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            textViewPath.setText(filePath.toString());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonLogout){
            mAuth.signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
        else if (v == buttonSaveProfile){
            saveUserInformation();
            finish();
            startActivity(new Intent(this, MessageActivity.class));
        }
        else if (v == buttonChoose){
            //select image
            showFileChooser();
        }
        else if (v == buttonUpload){
            //upload image
            uploadImage();
        }
    }
}
