package com.example.spaces.spaces;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spaces.spaces.models.StudyLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Owen on 4/19/18.
 */

public class SelectLocationActivity extends BaseActivity {

    private EditText nameEntry;
    private Button nextButton;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_location);

        nameEntry = findViewById(R.id.field_select_location);
        nextButton = findViewById(R.id.button_next);
        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String name = nameEntry.getText().toString();

                //final DatabaseReference reviews = mDatabase.child("reviews");
                final DatabaseReference locationRef = mDatabase.child("locations").child(name);

                locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // try to pull the location from the database
                        StudyLocation location = snapshot.getValue(StudyLocation.class);
                        if (location != null) {
                            // review the preexisting location
                            review(name);
                        }
                        else {
                            //Toast.makeText(SelectLocationActivity.this, "", Toast.LENGTH_LONG).show();
                            // allow user to create the location they want to review
                            newSpace(name);
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError de) {
                        de.toException().printStackTrace();
                    }
                });



            }
        });
    }

    private void review(String location) {
        Intent i = new Intent(this, ReviewActivity.class);
        // open the add review page for the location specified
        startActivity(i.putExtra("name", location));
    }

    private void newSpace(String location) {
        Intent i = new Intent(this, AddSpaceActivity.class);
        // open the add review page for the location specified
        startActivity(i.putExtra("name", location));

    }



}