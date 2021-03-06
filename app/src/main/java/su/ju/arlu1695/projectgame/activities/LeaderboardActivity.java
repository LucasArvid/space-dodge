package su.ju.arlu1695.projectgame.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import su.ju.arlu1695.projectgame.utils.Constants;
import su.ju.arlu1695.projectgame.R;
import su.ju.arlu1695.projectgame.utils.Util;


public class LeaderboardActivity extends AppCompatActivity {

    // Firebase connections. User and database reference
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // Progress message
    private ProgressBar progressBar;

    // Popup layout
    private Dialog leaderboardDialog;

    // Layout connections for displaying lists.
    private ListView leaderBoardListView;
    private ArrayList<String> mLeaderBoardList = new ArrayList<>();

    // Layout connections for TV
    private TextView tvLevelSelected;
    private TextView exitButton;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Constants.startMediaPlayer(0);

        getSupportActionBar().hide();

        leaderboardDialog = new Dialog(this);

        ListView listView = (ListView) findViewById(R.id.lw_leaderboard);
        listView.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.list_white_text,
                Util.getLevelList(this)

        ));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                                showHighScores(position);
                                            }
                                        });
    }

    /*
        Parses the database for leaderboard for requested level ( position ).
        Only looks for the top 50 players.
     */
    private void showHighScores (final int position) {

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("leaderboard");

        leaderboardDialog.setContentView(R.layout.leaderboard_popup);
        progressBar = (ProgressBar) leaderboardDialog.findViewById(R.id.progressBar_leaderboard);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        progressBar.bringToFront();


        leaderBoardListView = (ListView) leaderboardDialog.findViewById(R.id.lw_level_scores);
        exitButton = (TextView) leaderboardDialog.findViewById(R.id.tv_exitButton);
        tvLevelSelected = (TextView) leaderboardDialog.findViewById(R.id.tv_levelSelected);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_white_text, mLeaderBoardList);
        leaderBoardListView.setAdapter(arrayAdapter);

        // Sort firebase by value, lowest first and then grab a query of the ordered data
        myRef.child("level"+(position+1)).orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mLeaderBoardList.clear();

                int count = 0; // top 50 count
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            count++;
                            String score = ds.getValue().toString();
                            String user = ds.getKey();
                            String format = String.format("%s: %s   %s: %s",
                                    getResources().getString(R.string.player),
                                    user,
                                    getResources().getString(R.string.score),
                                    score);
                            mLeaderBoardList.add(format);
                            if(count == 50) // not placed in top 50
                                return;
                }
                // reverse list after filled
                Collections.reverse(mLeaderBoardList);
                arrayAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);

                tvLevelSelected.setText(String.format("%s %s %s 50.", getResources().getString(R.string.level),position + 1, getResources().getString(R.string.top)));
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        leaderboardDialog.show();
        progressBar.setVisibility(View.VISIBLE);

    }


    public void popUpExitButtonCLicked(View view) {
        leaderboardDialog.dismiss();
    }





    @Override
    public void onResume() {
        super.onResume();
        Constants.startMediaPlayer(0);
    }


}

