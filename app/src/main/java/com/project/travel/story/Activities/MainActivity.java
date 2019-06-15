package com.project.travel.story.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.project.travel.story.Fragments.AlbumFragment;
import com.project.travel.story.Fragments.MapFragment;
import com.project.travel.story.Fragments.PhotosFragment;
import com.project.travel.story.R;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.Utility.NightModeUtility;

public class MainActivity extends AppCompatActivity {

    public static MutableLiveData<String> currentAlbum = new MutableLiveData<>();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    boolean hideCategories = true;

    public static boolean isEditMode = false;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    @BindView(R.id.cameraFAB)
    FloatingActionButton cameraFAB;
    @BindView(R.id.locationFAB)
    FloatingActionButton locationFAB;
    @BindView(R.id.addFAB)
    FloatingActionButton addFAB;
    @BindView(R.id.showCheckBoxesFAB)
    FloatingActionButton showCheckBoxesFAB;
    @BindView(R.id.deletePhotosFAB)
    FloatingActionButton deletePhotosFAB;

    @BindString(R.string.title_map)
    String mapTitle;
    @BindString(R.string.title_albums)
    String albumsTitle;
    @BindString(R.string.title_photos)
    String photosTitle;

    private static final String TAG_FRAGMENT_ALBUMS = "fragment_albums";
    private static final String TAG_FRAGMENT_MAP = "fragment_map";
    private static final String TAG_FRAGMENT_PHOTOS = "fragment_photos";

    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    public static boolean isNaturePicked = true;
    public static boolean isFriendsPicked= true;
    public static boolean isDefaultPicked = true;

    public static final int CATEGORIES_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new NightModeUtility().initNightMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        cameraFAB.hide();
        locationFAB.hide();
        showCheckBoxesFAB.hide();
        deletePhotosFAB.hide();

        toolbar.setTitle(albumsTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        //loadFragment(new AlbumFragment());
        fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_ALBUMS);
        if (fragment == null) {
            fragment = AlbumFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.frame_container, fragment,
                    TAG_FRAGMENT_ALBUMS).commit();
        }
        fragmentManager.beginTransaction().show(fragment).commit();
        currentFragment = fragment;

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        currentAlbum.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newAlbum) {
                // Update the UI, in this case, a TextView.
                removeFragment(TAG_FRAGMENT_MAP);
                removeFragment(TAG_FRAGMENT_PHOTOS);
                if(newAlbum != null)
                Toast.makeText(getBaseContext(),newAlbum, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_albums:
                    addFAB.show();
                    cameraFAB.hide();
                    locationFAB.hide();
                    showCheckBoxesFAB.hide();
                    deletePhotosFAB.hide();
                    toolbar.setTitle(albumsTitle);
                    hideCategories = true;
                    invalidateOptionsMenu();
                    fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_ALBUMS);
                    if (fragment == null) {
                        fragment = AlbumFragment.newInstance();
                        fragmentManager.beginTransaction().add(R.id.frame_container, fragment,
                                TAG_FRAGMENT_ALBUMS).hide(currentFragment).commit();
                        currentFragment = fragment;
                    } else if (fragment != currentFragment){
                        fragmentManager.beginTransaction().hide(currentFragment).show(fragment).commit();
                        currentFragment = fragment;
                    }
                    return true;
                case R.id.navigation_map:
                    if(currentAlbum.getValue() != null) {
                        addFAB.hide();
                        showCheckBoxesFAB.hide();
                        deletePhotosFAB.hide();
                        locationFAB.show();
                        cameraFAB.show();
                        toolbar.setTitle(mapTitle);
                        hideCategories = false;
                        invalidateOptionsMenu();
                        fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_MAP);
                        if (fragment == null) {
                            fragment = MapFragment.newInstance();
                            fragmentManager.beginTransaction().add(R.id.frame_container, fragment,
                                    TAG_FRAGMENT_MAP).hide(currentFragment).commit();
                            currentFragment = fragment;
                        } else if (fragment != currentFragment) {
                            fragmentManager.beginTransaction().hide(currentFragment).show(fragment).commit();
                            currentFragment = fragment;
                        }
                    } else Toast.makeText(MainActivity.this, "Select or create album to proceed", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_photos:
                    if(currentAlbum.getValue() != null) {
                        addFAB.hide();
                        cameraFAB.hide();
                        locationFAB.hide();
                        showCheckBoxesFAB.show();
                        if(isEditMode) deletePhotosFAB.show();
                        else deletePhotosFAB.hide();
                        hideCategories = true;
                        invalidateOptionsMenu();
                        toolbar.setTitle(photosTitle);
                        fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT_PHOTOS);
                        if (fragment == null) {
                            fragment = PhotosFragment.newInstance();
                            fragmentManager.beginTransaction().add(R.id.frame_container, fragment,
                                    TAG_FRAGMENT_PHOTOS).hide(currentFragment).commit();
                            currentFragment = fragment;
                        } else if (fragment != currentFragment) {
                            fragmentManager.beginTransaction().hide(currentFragment).show(fragment).commit();
                            currentFragment = fragment;
                        }
                    } else Toast.makeText(MainActivity.this, "Select or create album to proceed", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };

    public void removeFragment(String tag){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null){
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (hideCategories){
            menu.findItem(R.id.action_menu_categories).setVisible(false);
        }else{
            menu.findItem(R.id.action_menu_categories).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_menu_categories:
                return super.onOptionsItemSelected(item);
            case R.id.action_menu_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void categoriesClick(View view){
        Intent categories = new Intent(this, CategoriesActivity.class);
        startActivityForResult(categories, CATEGORIES_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

}
