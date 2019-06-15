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
import com.project.travel.story.R;
import com.project.travel.story.Utility.Model.AlbumDetails;
import com.project.travel.story.Utility.DialogUtility;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.Utility.PhotoUtility;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditAlbumActivity extends AppCompatActivity {
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
    @BindView(R.id.save_geotag_layout)
    LinearLayout saveGeotagLayout;

    TextView descriptionTextView;
    TextView titleTextView;

    String description;
    String title;
    int position;

    String[] optArr;
    Uri[] imageUri = new Uri[1];
    Bitmap imageBitmap;
    String downloadUrl;

    final Context context = this;

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mDatabase;

    String purpose;
    AlbumDetails details;

    String[] items;
    int[] icons;

    DialogUtility dialogUtility;
    PhotoUtility photoUtility;

    Uri cameraImageUri;
    String mCurrentPhotoPath;

    boolean isNewPhoto = false;

    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ButterKnife.bind(this);

        photoUtility = new PhotoUtility();
        saveGeotagLayout.setVisibility(View.GONE);

        progressText.setText(getResources().getString(R.string.progress_uploading));
        progress.setVisibility(View.GONE);

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
            details = (AlbumDetails) extras.getSerializable("details");
            description = details.getDescription();
            title = details.getTitle();
            items[0] = details.getDate();
            items[1] = details.getTitle();
            items[2] = details.getDescription();
            position = extras.getInt("position");
            PhotoUtility.loadImage(this, details.getPhotoURL(), photoImage);
        }
        else if (purpose.equals("createAlbum") || purpose.equals("create")){
            imageUri[0] = Uri.parse(extras.getString("imageUri"));
            PhotoUtility.loadImageUri(this, imageUri[0], photoImage);
            items[0] = getCurrentTime();
            title = items[1];
            description = items[2];
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
        List<HashMap<String,String>> aList = new ArrayList<>();

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
        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.listview_edit_layout, from, to);

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
                    titleTextView = (TextView) relativeLayoutChild.getChildAt(0);
                    dialogUtility.editTextDialog(context,"Title", "Please enter title",
                                "You should enter title before continuing", titleTextView,
                                1, 40, false);
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
        if(titleTextView == null  && purpose.equals("createAlbum")
                || titleTextView != null && titleTextView.getText().toString().equals(items[1]) && purpose.equals("createAlbum")){
            Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show();
            return;
        }
        if(titleTextView != null) title = titleTextView.getText().toString();
        if(descriptionTextView != null) description = descriptionTextView.getText().toString();
        if (purpose.equals("createAlbum")){
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri[0]);
            } catch (IOException e) {
                Toast.makeText(context,"File not found error",Toast.LENGTH_SHORT).show();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 76, baos);
            byte[] data = baos.toByteArray();
            baos = null;

            String key = mDatabase.push().getKey();
            AlbumDetails message = new AlbumDetails(items[0], titleTextView.getText().toString(),
                    description, downloadUrl, key);
            uploadNewPhoto(key, uid, message, data);
        }
        else if(purpose.equals("edit")){
            details.setTitle(title);
            details.setDescription(description);
            if(!isNewPhoto) {
                mDatabase.child("users").child(uid).child("albums").child(details.getKey()).setValue(details);
                Toast.makeText(this, "Data sent successfully", Toast.LENGTH_SHORT).show();
                Intent data = new Intent();
                data.putExtra("item", details);
                data.putExtra("position", position);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
            else{
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri[0]);
                } catch (IOException e) {
                    Toast.makeText(context,"File not found error",Toast.LENGTH_SHORT).show();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 76, baos);
                byte[] data = baos.toByteArray();
                baos = null;
                uploadNewPhoto(details.getKey(),uid,details, data);
            }

        }
    }

    public void uploadNewPhoto(String key, String uid, AlbumDetails message, byte[] data){
        progress.setVisibility(View.VISIBLE);
        final StorageReference photostoragereference = mStorageRef.child("images/").child("users/").child(uid + "/").child(key);
        photostoragereference.putBytes(data).addOnFailureListener(new OnFailureListener() {
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
                        downloadUrl = uri.toString();
                        message.setPhotoURL(downloadUrl);
                        progress.setVisibility(View.GONE);
                        mDatabase.child("users").child(uid).child("albums").child(key).setValue(message);
                        Toast.makeText(context,"Data sent successfully",Toast.LENGTH_SHORT).show();
                        Intent data = new Intent();
                        data.putExtra("item", details);
                        data.putExtra("position", position);
                        setResult(Activity.RESULT_OK, data);
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
                    PhotoUtility.dispatchGalleryIntent(EditAlbumActivity.this, GALLERY_REQUEST);
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

    private String getCurrentTime(){
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy - HH:mm", Locale.US);
        String date = df.format(currentTime);
        //capitalize first letter
        date = date.substring(0, 1).toUpperCase() + date.substring(1);
        return date;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            imageUri[0] = selectedImage;
            isNewPhoto = true;
            PhotoUtility.loadImageUri(this, selectedImage, photoImage);
        }
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            photoUtility.galleryAddPic(mCurrentPhotoPath, this);
            imageUri[0] = cameraImageUri;
            isNewPhoto = true;
            PhotoUtility.loadImage(this, mCurrentPhotoPath, photoImage);
        }
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
                this.finish();
                break;
        }
        return true;
    }
}
