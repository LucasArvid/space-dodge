package su.ju.arlu1695.projectgame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import su.ju.arlu1695.projectgame.utils.Constants;
import su.ju.arlu1695.projectgame.R;
import su.ju.arlu1695.projectgame.utils.Util;


public class GameModeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView tv_auth_status;
    private Button logOutButton;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_mode);
        Constants.startMediaPlayer(0);

        firebaseAuth = FirebaseAuth.getInstance();
        tv_auth_status = (TextView) findViewById(R.id.tv_auth_status);
        logOutButton = (Button) findViewById(R.id.b_game_mode_logout);

        getAuthStatus();

        getSupportActionBar().hide();


    }

    public void offlineButtonClicked(View view) {
        Intent intent = new Intent(this, LevelSelectActivity.class)
                .putExtra("me", "offline");
        finish();
        startActivity(intent);
    }

    public void soloButtonClicked(View view) {
        Intent intent = new Intent (this, LoginActivity.class)
                .putExtra("mode","solo");
        finish();
        startActivity(intent);
    }

   public void duelButtonClicked(View view) {
        Intent intent = new Intent(this, LoginActivity.class)
                .putExtra("mode","duel");
        startActivity(intent);
    }

    public void gameModelogoutButtonClicked(View view) {
        Util.unSubscribeFromTopic(); // Disables user notifications
        FirebaseDatabase.getInstance().getReference().child("User").child(Util.getCurrentUserId()).child("online").setValue("false");
        firebaseAuth.signOut();
        getAuthStatus();
    }

    // Change text and hide/show logout button dependent on if the user is logged in or not
    private void getAuthStatus() {

        if (firebaseAuth.getCurrentUser() != null) {
            tv_auth_status.setText(getResources().getString(R.string.currently_logged_in));
            logOutButton.setVisibility(View.VISIBLE);

        }else {
            tv_auth_status.setText(getResources().getString(R.string.currently_offline));
            logOutButton.setVisibility(View.INVISIBLE);
        }
    }


    /*@Override
    public void onPause() {
        super.onPause();
        Constants.pauseMediaPlayer();
    } */

    @Override
    public void onResume() {
        super.onResume();
        Constants.startMediaPlayer(0);
        getAuthStatus();
    }

}
