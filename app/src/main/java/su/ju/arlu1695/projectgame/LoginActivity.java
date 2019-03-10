package su.ju.arlu1695.projectgame;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import static su.ju.arlu1695.projectgame.Util.getCurrentUserId;
import static su.ju.arlu1695.projectgame.Util.saveNickName;
import static su.ju.arlu1695.projectgame.Util.savePushToken;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignin;

    private String mode;

    User myUser;

    private ProgressDialog progressDialog;
    private Dialog nickNameDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        nickNameDialog = new Dialog(this);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");



        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = firebaseAuth.getCurrentUser();

        if (firebaseAuth.getCurrentUser() != null){
            setNotificationTopic();
            finish();
            if(mode.equals("solo"))
                startActivity(new Intent(this,LevelSelectActivity.class)
                                .putExtra("me","solo"));
            else if(mode.equals("duel"))
                startActivity(new Intent(this,OnlineActivity.class));
        }

        progressDialog = new ProgressDialog(this);

        buttonRegister = (Button) findViewById(R.id.emailCreateAccountButton);

        editTextEmail = (EditText) findViewById(R.id.fieldEmail);
        editTextPassword = (EditText) findViewById(R.id.fieldPassword);

        textViewSignin = (TextView) findViewById(R.id.emailSignInButton);



    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)) {
            // email empty
            Toast.makeText(this,getResources().getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)) {
            // password empty
            Toast.makeText(this,getResources().getString(R.string.please_enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        // register user

        progressDialog.setMessage(getResources().getString(R.string.registering_user));
        progressDialog.show();


        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            setNickName();
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.registered_successfully),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_when_registering),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            // email empty
            Toast.makeText(this, getResources().getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            // password empty
            Toast.makeText(this, getResources().getString(R.string.please_enter_password), Toast.LENGTH_SHORT).show();
            return;

        }

        progressDialog.setMessage(getResources().getString(R.string.logging_in_please_wait));
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            setNotificationTopic();
                            finish();
                            if(mode.equals("solo"))
                                startActivity(new Intent(LoginActivity.this,LevelSelectActivity.class)
                                        .putExtra("me","solo"));
                            else if(mode.equals("duel"))
                                startActivity(new Intent(LoginActivity.this,OnlineActivity.class));
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_successfull),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_unsuccessfull),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signinOrRegisterClicked(View view) {
        if(view == buttonRegister) {
            registerUser();
        }

        else if (view == textViewSignin) {
            userLogin();
        }
    }

    public void loginButtonClicked(View view) {
        userLogin();
    }


    public void setNickName() {
        Button saveUsername;
        nickNameDialog.setContentView(R.layout.nickname_popup);
        saveUsername = (Button) nickNameDialog.findViewById(R.id.b_save_username);


        saveUsername.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                EditText setNickName = (EditText) nickNameDialog.findViewById(R.id.et_nickname);
                                                final String nickname = setNickName.getText().toString().trim();
                                                myUser = new User(nickname);
                                                final FirebaseUser user = firebaseAuth.getCurrentUser();
                                                databaseReference.child("User").child(user.getUid()).setValue(LoginActivity.this.myUser);
                                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                                                    @Override
                                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                                        String newToken = instanceIdResult.getToken();
                                                        Log.e("newToken",newToken);
                                                        savePushToken(newToken, user.getUid());
                                                        setNotificationTopic();

                                                    }
                                                });
                                                setupFirebaseUser();
                                                nickNameDialog.dismiss();
                                                finish();
                                                if(mode.equals("solo"))
                                                    startActivity(new Intent(LoginActivity.this,LevelSelectActivity.class)
                                                            .putExtra("me","solo"));
                                                else if(mode.equals("duel"))
                                                    startActivity(new Intent(LoginActivity.this,OnlineActivity.class));
                                            }
                                        });

        nickNameDialog.show();

    }

    public void setNotificationTopic () {
        FirebaseUser topicUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference.child("User").child(topicUser.getUid()).child("nickname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getKey().equals("nickname")) {
                    String topic = dataSnapshot.getValue().toString();
                    Constants.thisUser.setNickname(topic);
                    FirebaseMessaging.getInstance().subscribeToTopic(topic);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void nicknameExitButtonClicked(View view) {
        nickNameDialog.dismiss();
    }

    private void setupFirebaseUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid()).child("highscore").child("level1").setValue(0);
        FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid()).child("highscore").child("level2").setValue(0);
        FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid()).child("highscore").child("level3").setValue(0);
        FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid()).child("highscore").child("level4").setValue(0);
        FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid()).child("highscore").child("level5").setValue(0);

    }

    @Override
    public void onPause() {
        super.onPause();
        Constants.pauseMediaPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Constants.startMediaPlayer(0);
    }


}
