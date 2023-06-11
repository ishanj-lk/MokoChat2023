package com.ishanj.mokochat;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link chatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class chatsFragment extends Fragment {

    private String uID;
    private FirebaseDatabase FBdatabase;
    private LinearLayout linearLayout;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public chatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment chatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static chatsFragment newInstance(String param1, String param2) {
        chatsFragment fragment = new chatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



        //Get uID with paper library
        Paper.init(getContext());
        uID = Paper.book().read("uID");
        Toast.makeText(getContext(), uID, Toast.LENGTH_SHORT).show();
        //Firebase Database
        FirebaseApp.initializeApp(getContext());
        FBdatabase = FirebaseDatabase.getInstance();
    }

    private void fetchList() {
        DatabaseReference listRef = FBdatabase.getReference("texts");
        // Retrieve the data from Firebase Realtime Database
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isResumed() || !isVisible()) {
                    return; // Ignore data updates if the fragment is not currently visible
                }
                linearLayout.removeAllViews(); // Clear the existing views

                // Get the layout inflater
                LayoutInflater inflater = LayoutInflater.from(getContext()); // Replace MainActivity with your activity or use 'getContext()' in a fragment
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT).show();
                    String textValue = childSnapshot.getValue(String.class);
                    String editTextValue = ""; // Set the initial value for EditText, modify as needed
                    int imageResource = R.drawable.ic_launcher_background;

                    // Create a new instance of the combined layout for each item
                    View itemLayout = inflater.inflate(R.layout.list_item_layout, linearLayout, false);
                    TextView textView = itemLayout.findViewById(R.id.itemTextView);
                    EditText editText = itemLayout.findViewById(R.id.itemEditText);
                    ImageView imageView = itemLayout.findViewById(R.id.itemImageView);

                    textView.setTextAppearance(getContext(), R.style.ListItemTextView);
                    editText.setTextAppearance(getContext(), R.style.ListItemEditText);

                    textView.setText(textValue);
                    editText.setText(editTextValue);
                    imageView.setImageResource(imageResource);

                    // Add the item layout to the layout container
                    linearLayout.addView(itemLayout);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data", databaseError.toException());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);
        linearLayout = rootView.findViewById(R.id.linearLayout);
        fetchList();
        return rootView;
    }
}