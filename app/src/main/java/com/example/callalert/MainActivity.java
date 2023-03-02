package com.example.callalert;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private ListView listView;
    ArrayAdapter adapter;
    private ArrayList<Integer> mToggleStates = new ArrayList<>(); // ArrayList to store the toggle state to be used with the listview

    private DatabaseReference mToggleRef; // Firebase database reference

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_main);

        // Initialize the Firebase database reference
        mToggleRef = FirebaseDatabase.getInstance().getReference("toggle");

        listView = findViewById(R.id.list_view);

        // Load the toggle state values ​​from the Firebase server
        mToggleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mToggleStates.clear(); // Clear the old toggle state values
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Boolean toggleState = snapshot.getValue(Boolean.class);
                    mToggleStates.add(toggleState == 1); // Convert the integer value to boolean
                }

                adapter.notifyDataSetChanged(); // Notify the adapter to update the list view
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadPost:onCancelled", error.toException());
            }
        });

        // Initialize the contact list view
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                showContacts();
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    private void showContacts() throws PackageManager.NameNotFoundException {
        ArrayList<Contact> contacts = new ArrayList<>();

        // Get the contact's name and phone number.
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor. moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String ID = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                Contact contact = new Contact(name, number, ID);
                if (!contacts.contains(contact)) { // Add only if there are no duplicate names.
                    contacts. add(contact);
                }
            }
            cursor. close();
        }

        adapter = new ContactAdapter(this, contacts, mToggleStates); // pass mToggleStates
        listView.setAdapter(adapter);
    }


}
