package com.project.travel.story.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InfoWindow implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private Marker mMarker;

    @BindView(R.id.pic)
    ImageView img;
    @BindView(R.id.date)
    TextView dateView;
    @BindView(R.id.description)
    TextView descriptionView;


    public InfoWindow(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.info_window, null);

        ButterKnife.bind(this, view);

        mMarker = marker;

        PhotoDetails infoWindowData = (PhotoDetails) marker.getTag();

        Date date = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd", Locale.US);
        try {
            date  = new SimpleDateFormat("MMMM dd, yyyy - HH:mm", Locale.US).parse(infoWindowData.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Glide.with(view.getContext()).load(infoWindowData.getPhotoURL()).apply(new RequestOptions()
                .placeholder(R.drawable.placeholder)).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                e.printStackTrace();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (!dataSource.equals(DataSource.MEMORY_CACHE)) mMarker.showInfoWindow();
                return false;
            }
        }).into(img);

        dateView.setText(df.format(date));
        descriptionView.setText(infoWindowData.getDescription());


        // setUpImg(infoWindowData.getPhotoURL(), img);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void setUpImg(String url, final ImageView img){
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        final long MAX_SIZE = 1300 * 1300;
        storageRef.getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                img.setImageBitmap(bitmap);
                mMarker.showInfoWindow();
                Toast.makeText(context, "Image downloaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
