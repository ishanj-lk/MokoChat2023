package com.ishanj.mokochat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Transformations;

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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

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
    private TextView searchWhichListTxt, searchUnhappyText, searchNoRequestsTxt;
    private ImageView searchUnhappyFace, searchMokoLogo;
    private ScrollView search_linear_layout_scroll;

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
                fetchSearchList();
                searchWhichListTxt.setText("Search Results");
                searchMokoLogo.setVisibility(View.GONE);
                searchNoRequestsTxt.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void fetchSearchList() {
        DatabaseReference listRef =  FBdatabase.getReference("users");
        // Retrieve the data from Firebase Realtime Database
        listRef.orderByChild("name4search").startAt(searchUsersETTxt).endAt(searchUsersETTxt+"\uf8ff").limitToFirst(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!isResumed() || !isVisible()) {
                    return;
                }
                linearLayout.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                if(dataSnapshot.exists()){
                    search_linear_layout_scroll.setVisibility(View.VISIBLE);
                    searchUnhappyText.setVisibility(View.GONE);
                    searchUnhappyFace.setVisibility(View.GONE);
                    linearLayout.removeAllViews();
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String childNameGet = childSnapshot.child("name").getValue().toString();
                        String childCityGet = childSnapshot.child("homeTown").getValue().toString();
                        int imageResource = R.drawable.ic_launcher_background;

                        // Create a new instance of the combined layout for each item
                        View itemLayout = inflater.inflate(R.layout.search_list_item_layout, linearLayout, false);
                        String key = childSnapshot.getKey();
                        TextView childNameSet = itemLayout.findViewById(R.id.list_name);
                        TextView cityNameSet = itemLayout.findViewById(R.id.list_city);
                        ImageView imageView = itemLayout.findViewById(R.id.itemImageView);

                        childNameSet.setTextAppearance(getContext(), R.style.SearchListName);
                        cityNameSet.setTextAppearance(getContext(), R.style.SearchListCity);

                        childNameSet.setText(childNameGet);
                        cityNameSet.setText("From, "+childCityGet);
                        String imageUrl="sample";
                        Picasso picasso = Picasso.get();
                        try {
                            imageUrl = childSnapshot.child("imageUrl").getValue().toString();
                            picasso.load(imageUrl).resize(200, 200).
                                    transform(new RoundedTransformation(10, 10)).centerCrop().into(imageView);
                        } catch (Exception e) {
                            picasso.load("https://i.imgur.com/tGbaZCY.jpg").resize(200, 200).
                                    transform(new RoundedTransformation(10, 10)).centerCrop().into(imageView);
                        }

                        itemLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent newUserIntent = new Intent(getContext(), userProfileActivity.class);
                                newUserIntent.putExtra("profileID", childSnapshot.getKey());
                                startActivity(newUserIntent);
                            }
                        });
                        linearLayout.addView(itemLayout);
                    }
                }
                else{
                    linearLayout.removeAllViews();
                    search_linear_layout_scroll.setVisibility(View.GONE);
                    searchUnhappyText.setVisibility(View.VISIBLE);
                    searchUnhappyFace.setVisibility(View.VISIBLE);
                }

                if(TextUtils.isEmpty(searchUsersETTxt)){
                    linearLayout.removeAllViews();
                    searchWhichListTxt.setText("Friend Requests");
                    searchUnhappyText.setVisibility(View.GONE);
                    searchUnhappyFace.setVisibility(View.GONE);
                    fetchRequestList();
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
        searchWhichListTxt = (TextView) rootView.findViewById(R.id.searchWhichListTxt);
        searchUsersET.setText("");
        searchUnhappyFace = (ImageView) rootView.findViewById(R.id.searchUnhappyFace);
        searchUnhappyText = (TextView) rootView.findViewById(R.id.searchUnhappyText);
        search_linear_layout_scroll = (ScrollView) rootView.findViewById(R.id.search_linear_layout_scroll);
        searchNoRequestsTxt = (TextView) rootView.findViewById(R.id.searchNoRequestsTxt);
        searchMokoLogo = (ImageView) rootView.findViewById(R.id.searchMokoLogo);
        //This triggers main actions
        actionTriggers();
        fetchRequestList();
        return rootView;
    }

    private void fetchRequestList() {

            DatabaseReference requestRef =  FBdatabase.getReference("requestReceive");
            requestRef.child(uID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!isResumed() || !isVisible()) {
                        return;
                    }
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){
                        linearLayout.removeAllViews();
                        search_linear_layout_scroll.setVisibility(View.VISIBLE);
                        searchMokoLogo.setVisibility(View.GONE);
                        searchNoRequestsTxt.setVisibility(View.GONE);
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            String childName = child.getKey();
                            DatabaseReference listRef =  FBdatabase.getReference("users");
                            listRef.child(childName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot childSnapshot) {
                                    String childNameGet = childSnapshot.child("name").getValue().toString();
                                    String childCityGet = childSnapshot.child("homeTown").getValue().toString();
                                    int imageResource = R.drawable.ic_launcher_background;

                                    // Create a new instance of the combined layout for each item
                                    View itemLayout = inflater.inflate(R.layout.search_list_item_layout, linearLayout, false);
                                    String key = childSnapshot.getKey();
                                    TextView childNameSet = itemLayout.findViewById(R.id.list_name);
                                    TextView cityNameSet = itemLayout.findViewById(R.id.list_city);
                                    ImageView imageView = itemLayout.findViewById(R.id.itemImageView);

                                    childNameSet.setTextAppearance(getContext(), R.style.SearchListName);
                                    cityNameSet.setTextAppearance(getContext(), R.style.SearchListCity);

                                    childNameSet.setText(childNameGet);
                                    cityNameSet.setText("From, "+childCityGet);
                                    String imageUrl="sample";
                                    Picasso picasso = Picasso.get();
                                    try {
                                        imageUrl = childSnapshot.child("imageUrl").getValue().toString();
                                        picasso.load(imageUrl).resize(200, 200).
                                                transform(new RoundedTransformation(10, 10)).centerCrop().into(imageView);
                                    } catch (Exception e) {
                                        picasso.load("https://i.imgur.com/tGbaZCY.jpg").resize(200, 200).
                                                transform(new RoundedTransformation(10, 10)).centerCrop().into(imageView);
                                    }

                                    itemLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent newUserIntent = new Intent(getContext(), userProfileActivity.class);
                                            newUserIntent.putExtra("profileID", childSnapshot.getKey());
                                            startActivity(newUserIntent);
                                        }
                                    });
                                    if(TextUtils.isEmpty(searchUsersETTxt)) {
                                        linearLayout.addView(itemLayout);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    else{
                        linearLayout.removeAllViews();
                        if(TextUtils.isEmpty(searchUsersETTxt)){
                            search_linear_layout_scroll.setVisibility(View.GONE);
                            searchMokoLogo.setVisibility(View.VISIBLE);
                            searchNoRequestsTxt.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error fetching data", databaseError.toException());
                }
            });
        }


//    @Override
//    public void onResume() {
//        super.onResume();
//        searchUsersET.setText("");
//    }
}