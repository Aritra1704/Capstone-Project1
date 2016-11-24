package com.arpaul.geocare;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arpaul.geocare.adapter.ChatAdapter;
import com.arpaul.geocare.dataObject.MessageDO;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Aritra on 14-11-2016.
 */

public class ChatActivity extends BaseActivity {

    private View llChatActivity;
    private static final String TAG = "ChatActivity";
    private static final int RC_SIGN_IN = 9001;
    private RecyclerView rvChat;
    private EditText edtMessage;
    private Button btnSend;
    private ProgressBar pbLoader;
    private ImageButton imgbPhotoPicker;

    private ChatAdapter adapter;
    private ArrayList<MessageDO> arrMessage = new ArrayList<>();
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private String mUsername;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDtabaseRef;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final String ANONYMOUS = "anonymous";
    public static final String MESSAGES_CHILD = "messages";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    //https://classroom.udacity.com/courses/ud0352/lessons/daa58d76-0146-4c52-b5d8-45e32a3dfb08/concepts/d053c636-9d48-43a6-ba05-5db4781dc562

    @Override
    public void initialize() {
        llChatActivity = baseInflater.inflate(R.layout.activity_chat,null);
        llBody.addView(llChatActivity, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        initialiseControls();

        bindControls();
    }

    private void bindControls() {

        mUsername = ANONYMOUS;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mMessageDtabaseRef = mFirebaseDatabase.getReference().child("messages");

        // Initialize progress bar
        pbLoader.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        imgbPhotoPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
            }
        });

        // Enable Send button when there's text to send
        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    btnSend.setEnabled(true);
                } else {
                    btnSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        edtMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MessageDO friendlyMessage = new MessageDO(mUsername, edtMessage.getText().toString());
                mMessageDtabaseRef.child(MESSAGES_CHILD).push().setValue(friendlyMessage);

                // Clear input box
                edtMessage.setText("");
                mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    Toast.makeText(ChatActivity.this, "You are logged in", Toast.LENGTH_LONG).show();
                    signedInitialize(user.getDisplayName());
                } else {
                    signedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()/*,
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()*/))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        dettachReadListener();
        adapter.refresh(new ArrayList<MessageDO>());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(ChatActivity.this, "Signed In!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ChatActivity.this, "Signed In canceled", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void signedInitialize(String userName) {
        mUsername = userName;
        attachReadListener();
    }

    private void signedOutCleanup() {
        mUsername = ANONYMOUS;
        adapter.refresh(new ArrayList<MessageDO>());
        dettachReadListener();
    }

    private void attachReadListener() {
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    MessageDO objFriendlyMessage = dataSnapshot.getValue(MessageDO.class);
                    arrMessage.add(objFriendlyMessage);
                    adapter.refresh(arrMessage);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessageDtabaseRef.addChildEventListener(mChildEventListener);
        }
    }

    private void dettachReadListener() {
        if(mChildEventListener != null) {
            mMessageDtabaseRef.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void initialiseControls() {
        rvChat = (RecyclerView) llChatActivity.findViewById(R.id.rvChat);
        edtMessage = (EditText) llChatActivity.findViewById(R.id.edtMessage);
        btnSend = (Button) llChatActivity.findViewById(R.id.btnSend);
        pbLoader = (ProgressBar) llChatActivity.findViewById(R.id.pbLoader);
        imgbPhotoPicker = (ImageButton) llChatActivity.findViewById(R.id.imgbPhotoPicker);

        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<MessageDO>());
        rvChat.setAdapter(adapter);
    }
}


/*
Real Time database
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
* */