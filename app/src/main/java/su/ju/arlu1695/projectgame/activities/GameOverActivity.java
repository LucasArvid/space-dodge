package su.ju.arlu1695.projectgame.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import su.ju.arlu1695.projectgame.utils.Constants;
import su.ju.arlu1695.projectgame.R;
import su.ju.arlu1695.projectgame.utils.Util;

import static android.view.View.GONE;

public class GameOverActivity extends AppCompatActivity {

    private String gameId;
    private String me;
    private int score;
    private String opponentScore;
    private String wonOrLost;


    private Button restartButton;

    private DatabaseReference firebaseRef;

    private TextView tv_score;

    private TextView tv_wonOrLoss;
    private TextView tv_hsUpdate;

    private ImageView iv_alien;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        getSupportActionBar().hide();

        firebaseRef =  FirebaseDatabase.getInstance().getReference();

        // getting Intent extras
        Bundle extrasBundle = getIntent().getExtras();
        gameId = extrasBundle.getString("gameId");
        me = extrasBundle.getString("me");
        score = extrasBundle.getInt("score");
        wonOrLost = extrasBundle.getString("wonOrLost");
        opponentScore = extrasBundle.getString("opponentScore");

        tv_score = (TextView) findViewById(R.id.tv_my_score);
        tv_wonOrLoss = (TextView) findViewById(R.id.tv_game_won_lost);
        tv_hsUpdate = (TextView) findViewById(R.id.tv_highscore_update);

        restartButton = (Button) findViewById(R.id.b_gameOverRestart);

        // ImageView connection to display different art depending on if winner or loser (Only in DUEL mode)
        iv_alien = (ImageView) findViewById(R.id.iv_gameover_alien);

        if(me.equals("solo"))
            handleSoloPlay();
        else if (me.equals("offline"))
            handleOfflinePlay();
        else
            handleOnlinePlay();


    }


    public void handleOfflinePlay() {
        tv_score.setText(String.format(
                "%s: %s",
                Constants.GAME_CONTEXT.getResources().getString(R.string.final_score),
                score
        ));
        tv_hsUpdate.setText(Constants.GAME_CONTEXT.getResources().getString(R.string.highscores_only_updated_in_solo));
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

    }


    public void handleSoloPlay() {
        tv_score.setText(String.format(
                "%s: %s",
                Constants.GAME_CONTEXT.getResources().getString(R.string.final_score),
                score
        ));
        tv_hsUpdate.setText(" ");

        udpateScore();

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
    }


    public void handleOnlinePlay() {
        boolean retry = true;
        while (retry) {
            try {
                Constants.thread.setRunning(false);
                Constants.thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }

        FirebaseDatabase.getInstance().getReference().child("Games").child(gameId).child(me).child("Score").setValue(score);
        if(wonOrLost.equals("lost"))
            FirebaseDatabase.getInstance().getReference().child("Games").child(gameId).child(me).child("Dead").setValue("true");
        else if(wonOrLost.equals("won")) {
            // FirebaseDatabase.getInstance().getReference().child("Games").child(gameId).setValue(null); // remove game from database
            FirebaseDatabase.getInstance().getReference().child("User").child(Util.getCurrentUserId()).child("wins").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        int prevScore = dataSnapshot.getValue(Integer.class);
                        FirebaseDatabase.getInstance().getReference().child("User").child(Util.getCurrentUserId()).child("wins").setValue(prevScore + 1); // Add 1 to current wins
                    }
                    else
                        FirebaseDatabase.getInstance().getReference().child("User").child(Util.getCurrentUserId()).child("wins").setValue(score); // First win
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        restartButton.setVisibility(GONE);

        tv_score.setText(String.format(
                "%s: %s",
                Constants.GAME_CONTEXT.getResources().getString(R.string.final_score),
                score
        ));


        if(wonOrLost.equals("won")) {
            tv_wonOrLoss.setText(Constants.GAME_CONTEXT.getResources().getString(R.string.hurray_opponent_died));
            iv_alien.setImageResource(R.drawable.aliengreen);
            tv_hsUpdate.setText(Constants.GAME_CONTEXT.getResources().getString(R.string.duel_wins_updated));
        }
        else if (wonOrLost.equals("lost")) {
            tv_wonOrLoss.setText(Constants.GAME_CONTEXT.getResources().getString(R.string.bitter_defeat));
            tv_hsUpdate.setText(Constants.GAME_CONTEXT.getResources().getString(R.string.highscores_do_not_update_in_duel));

        }



    }

    /*
        Updates the users score on the database if it is a new personal highscore
     */
    private void udpateScore() {
        final String format = String.format("level%d",
                (Constants.LEVEL_SELECTED+1)); //  +1 since levels start from index 0, database starts from 1
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseRef.child("User").child(currentUser.getUid()).child("highscore")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(format).getValue(Integer.class) < score || dataSnapshot.child(format).getValue() == null ) {
                            firebaseRef.child("User").child(currentUser.getUid()).child("highscore").child(format).setValue(score);
                            tv_hsUpdate.setText(Constants.GAME_CONTEXT.getResources().getString(R.string.new_highscore));
                            updateLeaderboard();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    /*
        Updates the leaderboard with the new personal highscore.
        This function is only reached if it actually is a new personal highscore
     */
    private void updateLeaderboard() {
        final String format = String.format("level%d",
                (Constants.LEVEL_SELECTED+1)); // levels start from index 0, database starts from 1
        firebaseRef.child("leaderboard").child(format).child(format).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Constants.currentUser).exists()) {
                    firebaseRef.child("leaderboard").child(format).child(Constants.currentUser).setValue(null);
                    firebaseRef.child("leaderboard").child(format).child(Constants.currentUser).setValue(score);
                } else
                    firebaseRef.child("leaderboard").child(format).child(Constants.currentUser).setValue(score);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void gameOverExitButtonClicked(View view) {
        finish();
        Intent intent = new Intent("finish_game");
        sendBroadcast(intent);
        Constants.stopMediaPlayer();
        Constants.startMediaPlayer(R.raw.soft_and_furious_06_and_never_come_back); // stops level music and starts main menu music
    }
}
