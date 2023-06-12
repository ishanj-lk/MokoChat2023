package com.ishanj.mokochat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
 * Use the {@link searchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class searchFragment extends Fragment {

    private String uID;
    private FirebaseDatabase FBdatabase;
    private LinearLayout linearLayout;
    private EditText searchUsersET;
    private String searchUsersETTxt;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public searchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment searchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static searchFragment newInstance(String param1, String param2) {
        searchFragment fragment = new searchFragment();
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
        //Firebase Database
        FirebaseApp.initializeApp(getContext());
        FBdatabase = FirebaseDatabase.getInstance();

    }

    private void actionTriggers() {
        searchUsersET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsersETTxt = searchUsersET.getText().toString().toLowerCase().replace(" ", "");
                fetchList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void fetchList() {
        DatabaseReference listRef =  FBdatabase.getReference("users");
        // Retrieve the data from Firebase Realtime Database
        listRef.orderByChild("name4search").startAt(searchUsersETTxt).endAt(searchUsersETTxt+"\uf8ff").limitToFirst(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isResumed() || !isVisible()) {
                    return;
                }
                linearLayout.removeAllViews();

                if(dataSnapshot.exists()){
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String childName = childSnapshot.child("name").getValue().toString();
                        String editTextValue = ""; // Set the initial value for EditText, modify as needed
                        int imageResource = R.drawable.ic_launcher_background;

                        // Create a new instance of the combined layout for each item
                        View itemLayout = inflater.inflate(R.layout.list_item_layout, linearLayout, false);
                        String key = childSnapshot.getKey();
                        TextView textView = itemLayout.findViewById(R.id.itemTextView);
                        EditText editText = itemLayout.findViewById(R.id.itemEditText);
                        ImageView imageView = itemLayout.findViewById(R.id.itemImageView);

                        textView.setTextAppearance(getContext(), R.style.ListItemTextView);
                        editText.setTextAppearance(getContext(), R.style.ListItemEditText);

                        textView.setText(childName);
                        editText.setText(editTextValue);
                        imageView.setImageResource(imageResource);

                        itemLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getContext(), childSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        linearLayout.addView(itemLayout);
                    }
                }
                else{
                    linearLayout.removeAllViews();
                }

                if(TextUtils.isEmpty(searchUsersETTxt)){
                    linearLayout.removeAllViews();
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
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        linearLayout = rootView.findViewById(R.id.searchUsersLayout);
        searchUsersET = (EditText) rootView.findViewById(R.id.searchUsersET);
        //This triggers main actions
        actionTriggers();
        return rootView;
    }
}