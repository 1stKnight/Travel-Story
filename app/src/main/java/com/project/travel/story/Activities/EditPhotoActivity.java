package com.project.travel.story.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.project.travel.story.Utility.DialogUtility;
import com.project.travel.story.Utility.GPSUtility;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.R;
import com.project.travel.story.Utility.PhotoUtility;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class EditPhotoActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.photo_image)
    ImageView photoImage;

    @BindView(R.id.progress_bar_layout)
    RelativeLayout progress;
    @BindView(R.id.progress_bar_text)
    TextView progressText;
    @BindView(R.id.progress_bar_percent)
    TextView progressPercent;
    @BindView(R.id.save_geotag)
    SwitchCompat saveGeotagSwitch;
    @BindView(R.id.save_geotag_layout)
    LinearLayout saveGeotagLayout;

    TextView categoryTextView;
    TextView descriptionTextView;

    String description;

    String[] optArr;
    Uri[] imageUri = new Uri[1];
    String downloadUrl;
    String realPath;

    String lat;
    String lng;

    Uri cameraImageUri;
    String mCurrentPhotoPath;

    final Context context = this;

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mDatabase;

    String categoryPicked;
    boolean isCategoryPicked = false;

    String purpose;
    PhotoDetails details;
    int position = 0;

    String[] items;
    int[] icons;

    DialogUtility dialogUtility;
    PhotoUtility photoUtility;

    boolean isNewPhoto = false;

    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    SimpleAdapter adapter;
    List<HashMap<String,String>> aList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ButterKnife.bind(this);

        progressText.setText(getResources().getString(R.string.progress_uploading));
        progress.setVisibility(View.GONE);

        photoUtility = new PhotoUtility();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        optArr = getResources().getStringArray(R.array.dialog_category_options);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //for full transparency
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        //        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //for partial transparency
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.parseColor("#59000000"));

        Bundle extras = getIntent().getExtras();

        items = extras.getStringArray("items");
        icons = extras.getIntArray("icons");

        purpose = extras.getString("purpose");
        if(purpose.equals("edit")){
            details = (PhotoDetails) extras.getSerializable("details");
            categoryPicked = details.getCategory();
            description = details.getDescription();
            items[0] = details.getDate();
            items[1] = details.getCategory();
            items[2] = details.getDescription();
            isCategoryPicked = true;
            position = extras.getInt("position");
            PhotoUtility.loadImage(this, details.getPhotoURL(), photoImage);
            saveGeotagLayout.setVisibility(View.GONE);
        }
        else if (purpose.equals("create")){
            imageUri[0] = Uri.parse(extras.getString("imageUri"));
            PhotoUtility.loadImageUri(this, imageUri[0], photoImage);
            realPath = extras.getString("realPath");
            items[0] = PhotoUtility.getPictureTime(realPath);
            description = items[2];
            lat = extras.getString("lat");
            lng = extras.getString("lng");
        }

        photoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageClick();
            }
        });

        dialogUtility = new DialogUtility();

        initListView();
    }

    private void initListView(){
        aList = new ArrayList<>();

        for(int i = 0; i < items.length; i++){
            HashMap<String, String> hm = new HashMap<String,String>();
                hm.put("text", items[i]);
                hm.put("icon", Integer.toString(icons[i]) );
                aList.add(hm);
        }
        // Keys used in Hashmap
        String[] from = { "icon","text"};

        // Ids of views in listview_edit_layout
        int[] to = { R.id.icon,R.id.text};

        // Instantiating an adapter to store each items
        // R.layout.listview_edit_layout defines the layout of each item
        adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.listview_edit_layout, from, to);

        // Getting a reference to listview of main.xml layout file
        ListView listView = findViewById(R.id.listview);

        // Setting the adapter to the listView
        listView.setAdapter(adapter);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View container, int position, long id) {
                if (position == 1){
                    // Getting the Container Layout of the ListView
                    LinearLayout linearLayoutParent = (LinearLayout) container;
                    // Getting the inner Linear Layout
                    RelativeLayout relativeLayoutChild = (RelativeLayout) linearLayoutParent.getChildAt(1);
                    // Getting the items TextView
                    categoryTextView = (TextView) relativeLayoutChild.getChildAt(0);
                    pickCategoryDialog();
                }
                if (position == 2){
                    LinearLayout linearLayoutParent = (LinearLayout) container;
                    RelativeLayout relativeLayoutChild = (RelativeLayout) linearLayoutParent.getChildAt(1);
                    descriptionTextView = (TextView) relativeLayoutChild.getChildAt(0);
                    dialogUtility.editTextDialog(context,"Description", "Please enter description",
                            "You should enter description before continuing", descriptionTextView,
                            5, 150, true);
                }
            }
        };
        // Setting the item click listener for the listview
        listView.setOnItemClickListener(itemClickListener);
    }

    private void pickCategoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_category_title);
        builder.setItems(R.array.dialog_category_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    isCategoryPicked = true;
                    categoryPicked = optArr[which];
                    categoryTextView.setText(optArr[which]);
                } else if (which == 1){
                    isCategoryPicked = true;
                    categoryPicked = optArr[which];
                    categoryTextView.setText(optArr[which]);
                } else if (which == 2){
                    isCategoryPicked = true;
                    categoryPicked = optArr[which];
                    categoryTextView.setText(optArr[which]);

                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void imageClick(){
        if(purpose.equals("edit") && !isNewPhoto){
            String arrUrl[] = new String[1];
            arrUrl[0] = details.getPhotoURL();
            new StfalconImageViewer.Builder<>(context, arrUrl, new ImageLoader<String>() {
                @Override
                public void loadImage(ImageView imageView, String image) {
                    PhotoUtility.loadImage(context, image, imageView);
                }
            }).show();
        } else {
            new StfalconImageViewer.Builder<>(context, imageUri, new ImageLoader<Uri>() {
                @Override
                public void loadImage(ImageView imageView, Uri image) {
                    PhotoUtility.loadImageUri(context, image, imageView);
                }
            }).show();
        }
    }

    private void doneClick(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(!isCategoryPicked){
            Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show();
            return;
        }
        if(descriptionTextView != null) description = descriptionTextView.getText().toString();
        if(purpose.equals("create")){
            if(saveGeotagSwitch.isChecked()){
                try {
                    GPSUtility.saveExifData(realPath, Double.valueOf(lat),
                            Double.valueOf(lng), description);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context,"Error while saving EXIF data",Toast.LENGTH_SHORT).show();
                }
            }

            String key = mDatabase.push().getKey();
            PhotoDetails message = new PhotoDetails(items[0], categoryPicked, description, downloadUrl, uid, lat, lng);
            uploadNewPhoto(key, uid, message);

        }
        else if(purpose.equals("edit")){
            details.setCategory(categoryPicked);
            details.setDescription(description);
            details.setDate(items[0]);
            if(!isNewPhoto){
                mDatabase.child("users").child(uid).child("photos").child(MainActivity.currentAlbum.getValue())
                        .child(details.getKey()).setValue(details);
                //mDatabase.child("PhotoDetails").child(details.getPhotoKey()).setValue(details);
                Toast.makeText(EditPhotoActivity.this,"Data sent successfully",Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("details", details);
                returnIntent.putExtra("position", position);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
           else uploadNewPhoto(details.getKey(), uid, details);
        }
    }

    public void uploadNewPhoto(String key, String uid, PhotoDetails message){
        progress.setVisibility(View.VISIBLE);
        final StorageReference photostoragereference = mStorageRef.child("images/").child("users/").child(uid + "/")
                .child("photos/").child(key);
        photostoragereference.putFile(imageUri[0]).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progress.setVisibility(View.GONE);
                Toast.makeText(context,"Can't upload",Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(this,new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progress.setVisibility(View.GONE);
                        downloadUrl = uri.toString();
                        message.setPhotoURL(downloadUrl);
                        message.setKey(key);
                        mDatabase.child("users").child(uid).child("photos").child(MainActivity.currentAlbum.getValue())
                                .child(key).setValue(message);
                        Toast.makeText(EditPhotoActivity.this,"Data sent successfully",Toast.LENGTH_SHORT).show();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("details", message);
                        returnIntent.putExtra("position", position);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int bytesTransferred = (int) taskSnapshot.getBytesTransferred();
                int totalBytes = (int) taskSnapshot.getTotalByteCount();

                int progress = (100 *  bytesTransferred) / totalBytes ;

                progressPercent.setText(progress + "/100%");
            }
        });
    }

    private void photoChangeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title);
        builder.setItems(R.array.dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    PhotoUtility.dispatchGalleryIntent(EditPhotoActivity.this, GALLERY_REQUEST);
                }
                if (which == 1)
                {
                    dispatchTakePictureIntent();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = photoUtility.createImageFile(getResources().getString(R.string.app_name));
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this,"Can't create file", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this,
                        "com.project.travel.story",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            imageUri[0] = selectedImage;
            isNewPhoto = true;
            realPath = PhotoUtility.getRealPathFromURI(this, selectedImage);
            PhotoUtility.loadImageUri(this, selectedImage, photoImage);
            items[0] = PhotoUtility.getPictureTime(realPath);
        }
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            photoUtility.galleryAddPic(mCurrentPhotoPath, this);
            imageUri[0] = cameraImageUri;
            isNewPhoto = true;
            realPath = mCurrentPhotoPath;
            PhotoUtility.loadImage(this, mCurrentPhotoPath, photoImage);
            items[0] = PhotoUtility.getPictureTime(realPath);
        }
        if(categoryTextView != null) items[1] = categoryTextView.getText().toString();
        if(descriptionTextView != null) items[2] = descriptionTextView.getText().toString();
        for(int i = 0; i < items.length; i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("text", items[i]);
            hm.put("icon", Integer.toString(icons[i]) );
            aList.set(i,hm);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_menu_done:
                doneClick();
                break;
            case R.id.action_menu_photo:
                photoChangeDialog();
                break;
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED);
                this.finish();
                break;
        }
        return true;
    }
}
