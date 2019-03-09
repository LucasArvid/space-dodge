package su.ju.arlu1695.projectgame;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private Switch allowNotifications;
    private Switch allowMusic;
    private Switch allowSound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().hide();

        // Declaration of switches
        allowNotifications = (Switch) findViewById(R.id.s_notifications);
        allowMusic = (Switch) findViewById(R.id.s_music);
        allowSound = (Switch) findViewById(R.id.s_sound);
        resumeSettingsActivity();

        FirebaseDatabase.getInstance().getReference().child("Games").child("Zjb3LfBbJcSeu4i0j6KJ6ckYNO72-ylP7GwE0xxOEn3UDQA46LaX0roX2").child("challenged").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Look for opponent death
                    if (dataSnapshot.child("Dead").getValue().equals("true")) {
                        String opponentScore = dataSnapshot.child("Score").getValue().toString();
                        Toast.makeText(SettingsActivity.this, opponentScore, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        allowNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked() == true) {
                    Constants.ALLOW_INVITES = true;
                } else {
                    Constants.ALLOW_INVITES = false;
                }
            }
        });

        allowMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked() == true) {
                    Constants.ALLOW_MUSIC = true;
                } else {
                    Constants.ALLOW_MUSIC = false;
                }
            }
        });

        allowSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked() == true) {
                    Constants.ALLOW_SOUND = true;
                } else {
                    Constants.ALLOW_SOUND = false;
                }
            }
        });

    }

    // Return to user set settings (returns to default otherwise.)
    private void resumeSettingsActivity() {
        allowNotifications.setChecked(Constants.ALLOW_INVITES);
        allowMusic.setChecked(Constants.ALLOW_MUSIC);
        allowSound.setChecked(Constants.ALLOW_SOUND);

    }
}
