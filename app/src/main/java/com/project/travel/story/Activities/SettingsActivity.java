package com.project.travel.story.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.crashlytics.android.Crashlytics;
import com.project.travel.story.R;
import com.project.travel.story.Utility.DialogUtility;
import com.project.travel.story.Utility.NightModeUtility;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nightMode)
    SwitchCompat nightMode;
    @BindView(R.id.nightModeSystem)
    SwitchCompat nightModeSys;

    @BindString(R.string.settings_title)
    String settingsTitle;

    private DialogUtility dialogUtility;
    private NightModeUtility nightModeUtility;

    private int selectedMode;
    private int delayTime = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        toolbar.setTitle(settingsTitle);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        invalidateOptionsMenu();

        dialogUtility = new DialogUtility();
        nightModeUtility = new NightModeUtility();
        selectedMode = nightModeUtility.retrieveNightModeFromPreferences(this);
        if(selectedMode == AppCompatDelegate.MODE_NIGHT_YES) nightMode.setChecked(true);
        else if (selectedMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) nightModeSys.setChecked(true);
        else nightMode.setChecked(false);

        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                compoundButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isChecked){
                            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            nightModeSys.setChecked(false);
                            nightMode.setChecked(true);
                            selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
                        } else {
                            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            nightModeSys.setChecked(false);
                            selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
                        }
                    }
                },delayTime);
            }
        });

        nightModeSys.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                compoundButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isChecked){
                            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            nightMode.setChecked(false);
                            nightModeSys.setChecked(true);
                            selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
                        } else {
                            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            nightMode.setChecked(false);
                            selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
                        }
                    }
                },delayTime);
            }
        });
    }

    public void signOut(View view){
        dialogUtility.signOutDialog(this, this);
    }

    private int getCurrentNightMode() {
        return getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
    }

    public void crashMe(View v){
        Crashlytics.getInstance().crash();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_done, menu);
        menu.findItem(R.id.action_menu_photo).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_menu_done:
                AppCompatDelegate.setDefaultNightMode(selectedMode);
                nightModeUtility.saveNightModeToPreferences(this, selectedMode);
                this.finish();
                break;
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    public void updatePassLink(View view) {
        Intent intentUpdatePass = new Intent(getApplicationContext(), ChangePasswordActivity.class);
        startActivity(intentUpdatePass);
    }

    public void updateEmailLink(View view) {
        Intent intentUpdateEmail = new Intent(getApplicationContext(), ChangeEmailActivity.class);
        startActivity(intentUpdateEmail);
    }
}
