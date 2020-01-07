package com.example.otus.uzduotis_02;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.view.Gravity.RIGHT;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button buttonSendMessage, buttonShowProfile, buttonBackToRooms;
    private EditText editTextMessage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;
    private long messageId;
    private long currentMessageId;
    private DatabaseReference mRef;
    private LinearLayout mainLinearLayout;
    private ScrollView scrollView;
    private FirebaseUser currentUserId;
    private String gifString = "";

    private String room_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        currentUserId = mAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        buttonSendMessage = (Button) findViewById(R.id.buttonSendMessage);
        buttonShowProfile = (Button) findViewById(R.id.buttonShowProfile);
        buttonBackToRooms = (Button) findViewById(R.id.buttonBack);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        currentMessageId = 0;
        mainLinearLayout = (LinearLayout)findViewById(R.id.linearLayoutMessage);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        room_name = getIntent().getExtras().get("room_name").toString();
        setTitle("Room: " + room_name);

        buttonSendMessage.setOnClickListener(this);
        buttonShowProfile.setOnClickListener(this);
        buttonBackToRooms.setOnClickListener(this);


        mRef = firebaseDatabase.getReference("Rooms").child(room_name);

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editTextMessage.getText().toString().length() > 0) {
                    String test = String.valueOf(editTextMessage.getText().toString().charAt(0));

                    if (test.equals("@")) {

                    }
                    else {

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                messageId = dataSnapshot.getChildrenCount();
                //List<Message> value = dataSnapshot.getValue(ArrayList.class);
                for (long i = currentMessageId; i < messageId; i++){
                    String message = dataSnapshot.child(Long.toString(i)).child("message").getValue(String.class);
                    String userId = dataSnapshot.child(Long.toString(i)).child("userId").getValue(String.class);
                    Log.e("Value", message + " " + userId);
                    addMessage(message, userId);
                    currentMessageId++;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e("ValueError", error.toException().toString());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void addMessage(String message, String userId){

        if(message.length() > 3 && message.substring(0, 4).equals("")) {

            if (userId.equals(currentUserId.getUid())) {
                //user message
                RelativeLayout relativeLayout = new RelativeLayout(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(0, 0, 0, 10);
                relativeLayout.setLayoutParams(layoutParams);
                if (relativeLayout.getParent() != null)
                    ((LinearLayout) relativeLayout.getParent()).removeView(relativeLayout);


                WebView webView = new WebView(this);
                webView.loadUrl(message.substring(4));



                RelativeLayout.LayoutParams _params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                _params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                webView.setLayoutParams(_params);

                if (webView.getParent() != null)
                    ((LinearLayout) webView.getParent()).removeView(webView);
                relativeLayout.addView(webView);
                mainLinearLayout.addView(relativeLayout);
            } else {
                //message of another person
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(linearLayout.HORIZONTAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(0, 0, 0, 10);
                if (linearLayout.getParent() != null)
                    ((LinearLayout) linearLayout.getParent()).removeView(linearLayout);
                final ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams2.setMargins(0, 0, 10, 0);
                imageView.setLayoutParams(layoutParams2);
                imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.image_bg));
                if (imageView.getParent() != null)
                    ((LinearLayout) imageView.getParent()).removeView(imageView);
                //add image
                mStorageRef.child("profileImages/" + userId + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        Picasso.get().load(uri).resize(32, 32).centerCrop().into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Picasso.get().load("https://i.imgur.com/G4m6Op1.jpg").resize(32, 32).centerCrop().into(imageView);
                        //Toast.makeText(getApplicationContext(), "No profile image found", Toast.LENGTH_SHORT).show();
                    }
                });
                //add image end
                linearLayout.addView(imageView);

                WebView webView = new WebView(this);
                webView.loadUrl(message.substring(4));

                webView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                if (webView.getParent() != null)
                    ((LinearLayout) webView.getParent()).removeView(webView);
                linearLayout.addView(webView);
                mainLinearLayout.addView(linearLayout, layoutParams);

            }

        }
        else {
            if (userId.equals(currentUserId.getUid())) {
                //user message
                RelativeLayout relativeLayout = new RelativeLayout(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(0, 0, 0, 10);
                relativeLayout.setLayoutParams(layoutParams);
                if (relativeLayout.getParent() != null)
                    ((LinearLayout) relativeLayout.getParent()).removeView(relativeLayout);
                TextView textView = new TextView(this);
                textView.setText(message);
                textView.setBackground(ContextCompat.getDrawable(this, R.drawable.message2_bg));
                RelativeLayout.LayoutParams _params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                _params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                textView.setLayoutParams(_params);

                if (textView.getParent() != null)
                    ((LinearLayout) textView.getParent()).removeView(textView);
                relativeLayout.addView(textView);
                mainLinearLayout.addView(relativeLayout);
            } else {
                //message of another person
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(linearLayout.HORIZONTAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(0, 0, 0, 10);
                if (linearLayout.getParent() != null)
                    ((LinearLayout) linearLayout.getParent()).removeView(linearLayout);
                final ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams2.setMargins(0, 0, 10, 0);
                imageView.setLayoutParams(layoutParams2);
                imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.image_bg));
                if (imageView.getParent() != null)
                    ((LinearLayout) imageView.getParent()).removeView(imageView);
                //add image
                mStorageRef.child("profileImages/" + userId + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        Picasso.get().load(uri).resize(32, 32).centerCrop().into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.d("ERRORAS", "onFailure: "+exception.getMessage());
                        Picasso.get().load("https://i.imgur.com/G4m6Op1.jpg").resize(32, 32).centerCrop().into(imageView);
                        //Toast.makeText(getApplicationContext(), "No profile image found", Toast.LENGTH_SHORT).show();
                    }
                });
                //add image end
                linearLayout.addView(imageView);
                TextView textView = new TextView(this);
                textView.setText(message);
                textView.setBackground(ContextCompat.getDrawable(this, R.drawable.message_bg));
                if (textView.getParent() != null)
                    ((LinearLayout) textView.getParent()).removeView(textView);
                linearLayout.addView(textView);
                mainLinearLayout.addView(linearLayout, layoutParams);
            }
        }
        //scroll to bottom
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    private void sendMessage(){
        FirebaseUser user = mAuth.getCurrentUser();
        Message message = new Message(editTextMessage.getText().toString(), user.getUid());

        databaseReference.child("Rooms").child(room_name).child(Long.toString(messageId)).setValue(message);        //close keyboard and empty textBox


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        editTextMessage.setText("");

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2) {
            if(data != null) {
                String url = data.getStringExtra("URL");

                FirebaseUser user = mAuth.getCurrentUser();
                Message message = new Message("GIF: " + url  , user.getUid());

                databaseReference.child("Rooms").child(room_name).child(Long.toString(messageId)).setValue(message);


                //close keyboard and empty textBox
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                editTextMessage.setText("");
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonSendMessage){
            //send message


        }
        else if (v == buttonShowProfile){
            // show profile window
            finish();
            startActivity(new Intent(this, ProfileActivity.class));
        }
        else if( v == buttonBackToRooms){
            startActivity(new Intent(this, ChatRooms.class));
            finish();
        }
    }
}
