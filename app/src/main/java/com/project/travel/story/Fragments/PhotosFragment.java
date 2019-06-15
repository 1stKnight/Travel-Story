package com.project.travel.story.Fragments;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.travel.story.Activities.EditAlbumActivity;
import com.project.travel.story.Activities.MainActivity;
import com.project.travel.story.R;
import com.project.travel.story.Utility.Adapter.OnItemCheckListener;
import com.project.travel.story.Utility.Model.AlbumDetails;
import com.project.travel.story.Utility.Pagination.GridPaginationScrollListener;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.Utility.Adapter.PhotosAdapter;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PhotosFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.photos_recycler_view)
    RecyclerView recyclerView;
    private PhotosAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private FloatingActionButton showCheckboxesFAB;
    private FloatingActionButton deletePhotosFAB;

    private List<PhotoDetails> photosList = new ArrayList<>();private static final int PAGE_START = 1;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private int itemCount = 0;
    private String lastKey = null;

    private ArrayList<PhotoDetails> currentSelectedItems;
    static public ArrayList<PhotoDetails> removedObjects;

    private static final int EDIT_REQUEST = 4;


    public PhotosFragment(){

    }

    public static PhotosFragment newInstance() {
        return new PhotosFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        currentSelectedItems = new ArrayList<>();
        removedObjects = new ArrayList<>();

        mAdapter = new PhotosAdapter(photosList, this, new OnItemCheckListener() {
            @Override
            public void onItemCheck(PhotoDetails item) {
                currentSelectedItems.add(item);
                Toast.makeText(getContext(), "Size: " + currentSelectedItems.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemUncheck(PhotoDetails item) {
                currentSelectedItems.remove(item);
            }
        });

        mLayoutManager = new GridLayoutManager(getContext(), 3);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.isHeader(position) ? mLayoutManager.getSpanCount() : 1;
            }
        });
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        swipeRefresh.setOnRefreshListener(this);

        showCheckboxesFAB = getActivity().findViewById(R.id.showCheckBoxesFAB);
        deletePhotosFAB = getActivity().findViewById(R.id.deletePhotosFAB);
       // loadTestItems();

        mAdapter.addHeader();

        recyclerView.addOnScrollListener(new GridPaginationScrollListener(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                    isLoading = true;
                    currentPage++;
                    loadPhotos();
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

        showCheckboxesFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideCheckBoxes();
            }
        });
        deletePhotosFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSelectedItems.size() == 0)
                    Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
                else{
                   // mAdapter.removeSelected();
                    deletePhotos();
                    mAdapter.checkedItems.clear();
                    currentSelectedItems.clear();
                }
            }
        });
    }

    private void loadPhotos(){
        Query query;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (itemCount == 0)
            query = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid).child("photos")
                    .child(MainActivity.currentAlbum.getValue())
                    .orderByKey()
                    .limitToFirst(GridPaginationScrollListener.PAGE_SIZE);
        else
            query = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid).child("photos")
                    .child(MainActivity.currentAlbum.getValue())
                    .orderByKey()
                    .limitToFirst(GridPaginationScrollListener.PAGE_SIZE + 1)
                    .startAt(lastKey);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<PhotoDetails> photos = new ArrayList<>();
                PhotoDetails album;
                int childrenCount = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    album = userSnapshot.getValue(PhotoDetails.class) ;
                    if(!album.getKey().equals(lastKey)){
                        childrenCount++;
                        itemCount++;
                        photos.add(album);
                        lastKey = userSnapshot.getKey();
                    }
                }
                if (currentPage != PAGE_START && !isLastPage) mAdapter.removeLoading();
                if (childrenCount == 0 || childrenCount < GridPaginationScrollListener.PAGE_SIZE) isLastPage = true;
                mAdapter.addAll(photos);
                swipeRefresh.setRefreshing(false);
                if (!isLastPage) mAdapter.addLoading();
                isLoading = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Cancelled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

   private void deletePhotos(){
       DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
       String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
       for(int i = 0; i < currentSelectedItems.size(); i++){
           ref.child("users").child(uid).child("photos")
                   .child(MainActivity.currentAlbum.getValue()).child(currentSelectedItems.get(i).getKey()).removeValue();
           if(MapFragment.markers != null) {
               int index = mAdapter.getIndex(currentSelectedItems.get(i));
               MapFragment.markers.get(index - 1).remove();
               MapFragment.markers.remove(index - 1);
           }
           mAdapter.remove(currentSelectedItems.get(i));
       }
   }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == EDIT_REQUEST && resultCode == RESULT_OK){
            int position = data.getIntExtra("position", 1);
            PhotoDetails photoDetails = (PhotoDetails) data.getSerializableExtra("details");
            mAdapter.update(photoDetails, position);
            if(MapFragment.markers != null) {
                Marker marker =  MapFragment.markers.get(position - 1);
                marker.setTag(photoDetails);
                marker.setIcon(MapFragment.getMarkerIcon(getMarkerColor(photoDetails)));
            }
        }
    }

    private String getMarkerColor(PhotoDetails details){
        String color = "#ffffff";
        if(details.getCategory().equals("Default")) color = getResources().getString(R.string.default_category_color);
        else if(details.getCategory().equals("Friends")) color = getResources().getString(R.string.friends_category_color);
        else if(details.getCategory().equals("Nature")) color = getResources().getString(R.string.nature_category_color);
        return color;
    }

   private void hideCheckBoxes(){
       mAdapter.isCheckboxHidden = !mAdapter.isCheckboxHidden;
       MainActivity.isEditMode = !MainActivity.isEditMode;
       if(mAdapter.isCheckboxHidden) deletePhotosFAB.hide();
       else deletePhotosFAB.show();
       currentSelectedItems.clear();
       mAdapter.checkedItems.clear();
       mAdapter.notifyDataSetChanged();
   }

    @Override
    public void onRefresh() {
        refreshRecyclerView();
    }

    private void refreshRecyclerView(){
        itemCount = 0;
        currentSelectedItems.clear();
        currentPage = PAGE_START;
        isLastPage = false;
        mAdapter.clear();
        lastKey = null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden && isLastPage) loadPhotos();
    }
}
