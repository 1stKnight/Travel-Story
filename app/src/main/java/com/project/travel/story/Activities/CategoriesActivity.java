package com.project.travel.story.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.project.travel.story.R;

public class CategoriesActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.natureButton)
    CheckBox natureBtn;
    @BindView(R.id.friendsButton)
    CheckBox friendsBtn;
    @BindView(R.id.defaultButton)
    CheckBox defaultBtn;

    @BindString(R.string.categories_title)
    String categoriesTitle;

    boolean isNaturePicked = true;
    boolean isFriendsPicked= true;
    boolean isDefaultPicked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        ButterKnife.bind(this);

        toolbar.setTitle(categoriesTitle);
        setSupportActionBar(toolbar);
        invalidateOptionsMenu();

        Bundle extras = getIntent().getExtras();
        isDefaultPicked = extras.getBoolean("default");
        isNaturePicked = extras.getBoolean("nature");
        isFriendsPicked = extras.getBoolean("friends");
        defaultBtn.setChecked(isDefaultPicked);
        natureBtn.setChecked(isNaturePicked);
        friendsBtn.setChecked(isFriendsPicked);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void onCategoryPicked(View view){
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()){
            case R.id.natureButton:
                if(checked) isNaturePicked = true;
                else isNaturePicked = false;
                break;
            case R.id.friendsButton:
                if(checked) isFriendsPicked = true;
                else isFriendsPicked = false;
                break;
            case R.id.defaultButton:
                if(checked) isDefaultPicked = true;
                else isDefaultPicked = false;
                break;
        }
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
                Intent returnIntent = new Intent();
                returnIntent.putExtra("nature", isNaturePicked);
                returnIntent.putExtra("friends", isFriendsPicked);
                returnIntent.putExtra("default", isDefaultPicked);
                setResult(Activity.RESULT_OK, returnIntent);
                this.finish();
                break;
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }
}
