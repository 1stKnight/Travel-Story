package com.project.travel.story.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.travel.story.Activities.EditAlbumActivity;
import com.project.travel.story.R;
import com.project.travel.story.Utility.Model.AlbumDetails;
import com.project.travel.story.Utility.Adapter.AlbumsAdapter;
import com.project.travel.story.Utility.Pagination.LinearPaginationScrollListener;
import com.project.travel.story.Utility.PhotoUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AlbumFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.albums_recycler_view)
    RecyclerView recyclerView;
    private AlbumsAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private List<AlbumDetails> albumsList = new ArrayList<>();
    private static final int PAGE_START = 1;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private int itemCount = 0;
    private String lastKey = null;

    FloatingActionButton addFAB;

    private int WRITE_PERMISSION_REQUEST_CODE;
    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CREATE_REQUEST = 3;
    private static final int EDIT_REQUEST = 4;

    private String[] items = new String[]{"Time", "Tap here to set title", "Press here to add description"};
    private int[] icons = new int[]{R.drawable.ic_clock, R.drawable.ic_edit, R.drawable.ic_edit};

    private PhotoUtility photoUtility;

    Uri cameraImageUri;
    String mCurrentPhotoPath;

    public AlbumFragment() {
        // Required empty public constructor
    }

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, view);
        checkReadWritePermission();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        photoUtility = new PhotoUtility();

        swipeRefresh.setOnRefreshListener(this);

        mAdapter = new AlbumsAdapter(albumsList, this);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        //preparedListItem();
        loadAlbums();
        //testItems();

        addFAB = getActivity().findViewById(R.id.addFAB);
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.dialog_title);
                builder.setItems(R.array.dialog_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
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
        });

        recyclerView.addOnScrollListener(new LinearPaginationScrollListener(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
               // preparedListItem();
                loadAlbums();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

    private void checkReadWritePermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST_CODE);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = photoUtility.createImageFile(getResources().getString(R.string.app_name));
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(getContext(),"Can't create file", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(getContext(),
                        "com.project.travel.story",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            Intent editIntent = new Intent(getContext(), EditAlbumActivity.class);
            editIntent.putExtra("purpose", "createAlbum");
            editIntent.putExtra("imageUri", selectedImage.toString());
            String realPath = photoUtility.getRealPathFromURI(getContext(), selectedImage);
            editIntent.putExtra("realPath", realPath);
            editIntent.putExtra("items", items);
            editIntent.putExtra("icons", icons);
            startActivityForResult(editIntent, CREATE_REQUEST);
        }
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            photoUtility.galleryAddPic(mCurrentPhotoPath, getContext());
            Intent editIntent = new Intent(getContext(), EditAlbumActivity.class);
            editIntent.putExtra("purpose", "createAlbum");
            editIntent.putExtra("imageUri", cameraImageUri.toString());
            editIntent.putExtra("realPath", mCurrentPhotoPath);
            editIntent.putExtra("items", items);
            editIntent.putExtra("icons", icons);
            startActivityForResult(editIntent, CREATE_REQUEST);
        }
        if(requestCode == CREATE_REQUEST && resultCode == RESULT_OK){
            if(isLastPage) {
                loadAlbums();
            }
        }
        if(requestCode == EDIT_REQUEST && resultCode == RESULT_OK){
            mAdapter.update((AlbumDetails) data.getSerializableExtra("item"),
                    data.getIntExtra("position", 1));

        }
    }

    private void loadAlbums(){
        Query query;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (itemCount == 0)
            query = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid).child("albums")
                    .orderByKey()
                    .limitToFirst(LinearPaginationScrollListener.PAGE_SIZE);
        else
            query = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid).child("albums")
                    .orderByKey()
                    .limitToFirst(LinearPaginationScrollListener.PAGE_SIZE + 1)
                    .startAt(lastKey);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AlbumDetails> albums = new ArrayList<>();
                AlbumDetails album;
                int childrenCount = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    album = userSnapshot.getValue(AlbumDetails.class) ;
                    if(!album.getKey().equals(lastKey)){
                        childrenCount++;
                        itemCount++;
                        albums.add(album);
                        lastKey = userSnapshot.getKey();
                    }
                }
                if (currentPage != PAGE_START && !isLastPage) mAdapter.removeLoading();
                if (childrenCount == 0 || childrenCount < LinearPaginationScrollListener.PAGE_SIZE) isLastPage = true;
                mAdapter.addAll(albums);
                swipeRefresh.setRefreshing(false);
                if (!isLastPage) mAdapter.addLoading();
                isLoading = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Cancelled " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*private void preparedListItem() {
        final ArrayList<AlbumDetails> items = new ArrayList<>();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    itemCount++;
                    AlbumDetails al1 = new AlbumDetails("somedate", "Nice title", "Nice description",
                            "https://dummyimage.com/600x400/000/fff.jpg&text=" + itemCount,"best key ever");
                    items.add(al1);

                }
                if (currentPage != PAGE_START) mAdapter.removeLoading();
                mAdapter.addAll(items);
                swipeRefresh.setRefreshing(false);
                if (currentPage < totalPage) mAdapter.addLoading();
                else isLastPage = true;
                isLoading = false;

            }
        }, 2000);
    }*/

    @Override
    public void onRefresh() {
        itemCount = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        mAdapter.clear();
        lastKey = null;
       // preparedListItem();
        loadAlbums();
    }
}
