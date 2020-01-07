package com.example.otus.uzduotis_02;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatRooms extends AppCompatActivity {

    private Button add_room;
    private EditText room_name;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_rooms = new ArrayList<>();
//    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();
    private FirebaseDatabase firebaseDatabase;
   private DatabaseReference root = firebaseDatabase.getInstance().getReference("Rooms");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_rooms);

        add_room = (Button) findViewById(R.id.button);
        room_name = (EditText) findViewById(R.id.editText);
        listView = (ListView) findViewById(R.id.list);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_of_rooms);

        listView.setAdapter(arrayAdapter);


        add_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> map = new HashMap<String, Object>();
                map.put(room_name.getText().toString()," ");
                root.updateChildren(map);
            }
        });




       root.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               Set<String> set = new HashSet<String>();
               Iterator i = dataSnapshot.getChildren().iterator();
               while (i.hasNext()) {

                   set.add(((DataSnapshot) i.next()).getKey());
               }
               list_of_rooms.clear();
               list_of_rooms.addAll(set);

               arrayAdapter.notifyDataSetChanged();
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {


           }

       });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent intent = new Intent(getApplicationContext(),MessageActivity.class );
                    intent.putExtra("room_name", ((TextView)view).getText().toString());
                   // Intent.putExtra("user_name", name);
                    startActivity(intent);
                }
            });







    }
}
