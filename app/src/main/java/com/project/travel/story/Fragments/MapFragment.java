package com.project.travel.story.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.core.content.FileProvider;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.location.LocationListener;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.travel.story.Activities.CategoriesActivity;
import com.project.travel.story.Activities.EditPhotoActivity;
import com.project.travel.story.Activities.MainActivity;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.R;
import com.project.travel.story.Utility.PhotoUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerDragListener {

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    FloatingActionButton cameraFAB;
    FloatingActionButton locationFAB;

    Uri cameraImageUri;
    String mCurrentPhotoPath;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private Query query;
    private ValueEventListener markersEventListener;

    PhotoUtility photoUtility;

    static public List<Marker> markers;
    private Marker selectedMarker;

    Double lat;
    Double lng;
    Double userLat;
    Double userLng;

    boolean isCurrentLocation = false;
    boolean followMode = true;
    long minTime = 2500;
    float minDistance = 10;

    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int CATEGORIES_REQUEST = 3;
    private static final int EDIT_REQUEST = 4;
    private static final int CREATE_REQUEST = 5;

    String[] items = new String[]{"Time", "Press here to choose category", "Press here to add description"};
    int[] icons = new int[]{R.drawable.ic_clock, R.drawable.ic_marker, R.drawable.ic_edit};


    public MapFragment(){

    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        photoUtility = new PhotoUtility();
        markers = new ArrayList<>();

        mDatabase =FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();

        query = mDatabaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("photos")
                .child(MainActivity.currentAlbum.getValue())
                .orderByKey();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        cameraFAB = getActivity().findViewById(R.id.cameraFAB);
        locationFAB = getActivity().findViewById(R.id.locationFAB);
        cameraFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCurrentLocation = true;
                dispatchTakePictureIntent();
            }
        });
        locationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(followMode){
                    locationManager.removeUpdates(locationListener);
                    locationFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.nav_button_discovery)));
                    followMode = false;
                } else{
                    if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    } else {
                        locationFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.nav_button_follow)));
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
                        followMode = true;
                    }
                }

            }
        });

       /* categories = getActivity().findViewById(R.id.categories);
        categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent categories = new Intent(getContext(), CategoriesActivity.class);
                startActivityForResult(categories, CATEGORIES_REQUEST);
            }
        });*/
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(this);

        InfoWindow infoWindow = new InfoWindow(getContext());
        mMap.setInfoWindowAdapter(infoWindow);

        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);

        initLocationListener();

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            if (followMode)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
        }
        getUserData();
    }

    private void initLocationListener(){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                userLat = userLocation.latitude;
                userLng = userLocation.longitude;
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 15.0f);
                mMap.animateCamera(cameraUpdate);
                // mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                // mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                Toast.makeText(getContext(), userLocation.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        PhotoDetails clickedInfoWindowData = (PhotoDetails) marker.getTag();
        selectedMarker = marker;
       // markers.remove(marker);
       // marker.remove();

        Intent editIntent = new Intent(getContext(), EditPhotoActivity.class);
        editIntent.putExtra("purpose", "edit");
        editIntent.putExtra("details", clickedInfoWindowData);
        editIntent.putExtra("items", items);
        editIntent.putExtra("icons", icons);
        startActivityForResult(editIntent, EDIT_REQUEST);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            if(followMode){
                locationFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.nav_button_discovery)));
                locationManager.removeUpdates(locationListener);
                followMode = false;
                Toast.makeText(getContext(), "Follow mode disabled.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMapLongClick(LatLng point){

        isCurrentLocation = false;
        lat = point.latitude;
        lng = point.longitude;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_title);
        builder.setItems(R.array.dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    PhotoUtility.dispatchGalleryIntentFragment(MapFragment.this, GALLERY_REQUEST);
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

    @Override
    public void onMarkerDragStart(Marker marker){
        Toast.makeText(getContext(), "Drag started.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        LatLng position = marker.getPosition();
        PhotoDetails details = (PhotoDetails) marker.getTag();
        saveNewLatLng(position, details);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            Intent editIntent = new Intent(getContext(), EditPhotoActivity.class);
            editIntent.putExtra("purpose", "create");
            editIntent.putExtra("imageUri", selectedImage.toString());
            String realPath = PhotoUtility.getRealPathFromURI(getContext(), selectedImage);
            editIntent.putExtra("realPath", realPath);
            editIntent.putExtra("items", items);
            editIntent.putExtra("icons", icons);
            if (isCurrentLocation && userLng != null && userLat != null){
                editIntent.putExtra("lat", userLat.toString());
                editIntent.putExtra("lng",userLng.toString());
                startActivityForResult(editIntent, CREATE_REQUEST);
            }
            else if (!isCurrentLocation){
                editIntent.putExtra("lat", lat.toString());
                editIntent.putExtra("lng",lng.toString());
                startActivityForResult(editIntent, CREATE_REQUEST);
            }
            else Toast.makeText(getContext(), "App can't get user coordinates.",
                    Toast.LENGTH_LONG).show();
        }
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
            photoUtility.galleryAddPic(mCurrentPhotoPath, getContext());
            Intent editIntent = new Intent(getContext(), EditPhotoActivity.class);
            editIntent.putExtra("purpose", "create");
            editIntent.putExtra("imageUri", cameraImageUri.toString());
            editIntent.putExtra("realPath", mCurrentPhotoPath);
            editIntent.putExtra("items", items);
            editIntent.putExtra("icons", icons);
            if (isCurrentLocation && userLng != null && userLat != null){
                editIntent.putExtra("lat", userLat.toString());
                editIntent.putExtra("lng",userLng.toString());
                startActivityForResult(editIntent, CREATE_REQUEST);
            }
            else if (!isCurrentLocation){
                editIntent.putExtra("lat", lat.toString());
                editIntent.putExtra("lng",lng.toString());
                startActivityForResult(editIntent, CREATE_REQUEST);
            }
            else Toast.makeText(getContext(), "App can't get user coordinates.",
                        Toast.LENGTH_LONG).show();

        }
        if(requestCode == CATEGORIES_REQUEST && resultCode == RESULT_OK){
            MainActivity.isNaturePicked = data.getBooleanExtra("nature", false);
            MainActivity.isFriendsPicked = data.getBooleanExtra("friends", true);
            MainActivity.isDefaultPicked = data.getBooleanExtra("default", false);
            filterMarkers();
        }
        if (requestCode == EDIT_REQUEST && resultCode == RESULT_OK){
            PhotoDetails photoDetails = (PhotoDetails) data.getSerializableExtra("details");
            selectedMarker.setTag(photoDetails);
            selectedMarker.setIcon(getMarkerIcon(getMarkerColor(photoDetails)));
            selectedMarker.showInfoWindow();
        }
        if (requestCode == CREATE_REQUEST && resultCode == RESULT_OK){
            PhotoDetails photoDetails = (PhotoDetails) data.getSerializableExtra("details");
            addMarker(photoDetails);
        }
    }

    public void filterMarkers(){
        for (int i = 0; i < markers.size(); i++) {
            PhotoDetails details = (PhotoDetails) markers.get(i).getTag();
            if(details.getCategory().equals("Nature") && MainActivity.isNaturePicked){
                markers.get(i).setVisible(true);
            } else if (details.getCategory().equals("Friends") && MainActivity.isFriendsPicked){
                markers.get(i).setVisible(true);
            } else if (details.getCategory().equals("Default") && MainActivity.isDefaultPicked){
                markers.get(i).setVisible(true);
            } else markers.get(i).setVisible(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 1 &&
                permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            } else{
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                if (followMode)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime , minDistance, locationListener);
            }
        } else {
            Toast.makeText(getContext(), "Permission denied.\n If you want to see your location give geolocation permission.",
                    Toast.LENGTH_LONG).show();
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

    private void getUserData(){
        initEventValueListener();
        query.addListenerForSingleValueEvent(markersEventListener);
    }

    private void saveNewLatLng(LatLng newPosition, PhotoDetails details){
        details.setLat(String.valueOf(newPosition.latitude));
        details.setLng(String.valueOf(newPosition.longitude));
        mDatabaseReference.child("users").child(FirebaseAuth.getInstance().getUid()).child("photos").child(MainActivity.currentAlbum.getValue())
                .child(details.getKey()).setValue(details);
    }

    private void initEventValueListener(){
        markersEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot issue : dataSnapshot.getChildren()){
                    PhotoDetails details = issue.getValue(PhotoDetails.class);
                    addMarker(details);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void addMarker(PhotoDetails details){
        LatLng position = new LatLng(Double.parseDouble(details.getLat()),Double.parseDouble(details.getLng()));
        Marker m = mMap.addMarker(new MarkerOptions().position(position).icon(getMarkerIcon(getMarkerColor(details))));
        m.setTag(details);
        m.setDraggable(true);
        markers.add(m);
    }

    private String getMarkerColor(PhotoDetails details){
        String color = "#ffffff";
        if(details.getCategory().equals("Default")) color = getResources().getString(R.string.default_category_color);
        else if(details.getCategory().equals("Friends")) color = getResources().getString(R.string.friends_category_color);
        else if(details.getCategory().equals("Nature")) color = getResources().getString(R.string.nature_category_color);
        return color;
    }

    public static BitmapDescriptor getMarkerIcon(String color){
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_menu_categories:
                Intent categories = new Intent(getContext(), CategoriesActivity.class);
                categories.putExtra("default", MainActivity.isDefaultPicked);
                categories.putExtra("nature", MainActivity.isNaturePicked);
                categories.putExtra("friends", MainActivity.isFriendsPicked);
                startActivityForResult(categories, CATEGORIES_REQUEST);
                Toast.makeText(getContext(), "Categories clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!followMode){
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        locationManager.removeUpdates(locationListener);
      //  query.removeEventListener(markersEventListener);

        /*if (!followMode){
            locationFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.nav_button_follow)));
            followMode = true;
        }*/
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {
            locationManager.removeUpdates(locationListener);
        } else {
            if (!followMode){
                locationManager.removeUpdates(locationListener);
            } else {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        query.removeEventListener(markersEventListener);
        if (!followMode)
            locationFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.nav_button_follow)));

    }

}
