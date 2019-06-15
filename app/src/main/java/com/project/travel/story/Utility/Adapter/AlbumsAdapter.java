package com.project.travel.story.Utility.Adapter;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.travel.story.Activities.EditAlbumActivity;
import com.project.travel.story.Activities.MainActivity;
import com.project.travel.story.R;
import com.project.travel.story.Utility.Model.AlbumDetails;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.Utility.PhotoUtility;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<AlbumDetails> albumsList;
    private StfalconImageViewer viewer;

    private Fragment fragment;

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    private static final int EDIT_REQUEST = 4;
    private String[] items = new String[]{"Time", "Tap here to set title", "Press here to add description"};
    private int[] icons = new int[]{R.drawable.ic_clock, R.drawable.ic_edit, R.drawable.ic_edit};

    StorageReference storageReference;
    FirebaseStorage storage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener valueEventListener;
    private Query query;


    public AlbumsAdapter(List<AlbumDetails> albumsList, Fragment fragment) {
        this.albumsList = albumsList;
        this.fragment = fragment;
        storage = FirebaseStorage.getInstance().getReference().getStorage();
        mDatabase =FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.card_album, parent, false));
            case VIEW_TYPE_LOADING:
                return new FooterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == albumsList.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return albumsList.size();
    }

    public void update(AlbumDetails response, int position){
        albumsList.set(position, response);
        notifyItemChanged(position);
    }

    public void add(AlbumDetails response) {
        albumsList.add(response);
        notifyItemInserted(albumsList.size() - 1);
    }

    public void addToTop(AlbumDetails response){
        albumsList.add(0, response);
        notifyItemInserted(0);
    }

    public void addAll(List<AlbumDetails> postItems) {
        for (AlbumDetails response : postItems) {
            add(response);
        }
    }

    private void remove(AlbumDetails postItems) {
        int position = albumsList.indexOf(postItems);
        if (position > -1) {
            albumsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addLoading() {
        isLoaderVisible = true;
        add(new AlbumDetails());
    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = albumsList.size() - 1;
        AlbumDetails item = getItem(position);
        if (item != null) {
            albumsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    AlbumDetails getItem(int position) {
        return albumsList.get(position);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.btn_explore)
        Button explore;
        @BindView(R.id.albumTitle)
        TextView title;
        @BindView(R.id.albumDescription)
        TextView description;
        @BindView(R.id.albumCover)
        ImageView cover;
        @BindView(R.id.albumEdit)
        ImageView edit;
        @BindView(R.id.albumDelete)
        ImageView delete;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        protected void clear() {

        }

        public void onBind(final int position){
            super.onBind(position);
            final AlbumDetails album = albumsList.get(position);
            title.setText(album.getTitle());
            description.setText(album.getDescription());
            description.setTextIsSelectable(true);
            title.setTextIsSelectable(true);
            description.measure(-1, -1);
            title.measure(-1, -1);
            PhotoUtility.loadImage(itemView.getContext(), album.getPhotoURL(), cover);

            final String[] url = new String[]{album.getPhotoURL()};
            cover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageClick(itemView.getContext(), album);
                }
            });
            explore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentAlbum(album.getKey());
                }
            });
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startEditActivity(itemView.getContext(), position, album);
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteAlbumDialog(itemView.getContext(), album);
                }
            });
        }
    }

    public class FooterHolder extends BaseViewHolder {

        ProgressBar mProgressBar;

        FooterHolder(View view) {
            super(view);
            mProgressBar = view.findViewById(R.id.progressBar);
        }

        @Override
        protected void clear() {

        }

    }

    private void imageClick(final Context context, final AlbumDetails item){
        String[] a = new String[]{item.getPhotoURL()};
        LayoutInflater inflater = LayoutInflater.from(context);
        View overlay = inflater.inflate(R.layout.album_cover_overlay, null);
        TextView description = overlay.findViewById(R.id.imageOverlayDescriptionText);
        ImageView download = overlay.findViewById(R.id.imageOverlayDownloadButton);
        final ImageView share = overlay.findViewById(R.id.imageOverlayShareButton);

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String downloadFrom = "images/users/" + FirebaseAuth.getInstance().getUid() + "/" + item.getKey();
               new PhotoUtility().startPhotoDownloadService(context, downloadFrom);
            }
        });
        description.setText(item.getDescription());

        viewer = new StfalconImageViewer.Builder<>(context, a, new ImageLoader<String>() {
            @Override
            public void loadImage(ImageView imageView, String image) {
                Glide.with(context).load(image).apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder).format(DecodeFormat.PREFER_ARGB_8888)).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PhotoUtility photoUtility = new PhotoUtility();
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", item.getDescription());
                                clipboard.setPrimaryClip(clip);
                                Uri imageUri = photoUtility.getBitmapFromDrawable(context, ((BitmapDrawable) resource).getBitmap());
                                if (imageUri != null) {
                                    photoUtility.shareImage(context, imageUri);
                                }
                            }
                        });
                        return false;
                    }
                }).into(imageView);
            }
        }).withOverlayView(overlay).show();
    }

    private void setCurrentAlbum(String key){
       String currentVal = MainActivity.currentAlbum.getValue();
        if(currentVal == null || !currentVal.equals(key))
            MainActivity.currentAlbum.setValue(key);
    }

    private void deleteAlbumDialog(final Context context, final AlbumDetails item){
        final AlertDialog builder = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_yes_no, null);
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        Button buttonOK = dialogView.findViewById(R.id.buttonOK);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);

        titleTextView.setText(R.string.delete_album);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storageReference = storage.getReferenceFromUrl(item.getPhotoURL());
                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        deleteAlbum(context, item);
                        remove(item);
                        builder.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        Toast.makeText(context, "Cancelled: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        builder.setView(dialogView);
        builder.show();
    }

    private void deleteAlbum(Context context, AlbumDetails item){
        String key = item.getKey();
        List<PhotoDetails> photos;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(item.getKey().equals(MainActivity.currentAlbum.getValue())) MainActivity.currentAlbum.setValue(null);
        query = mDatabaseReference.child("users").child(uid).child("photos").child(key).orderByKey();
        ref.child("users").child(uid).child("albums").child(key).removeValue();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ref.child("users").child(uid).child("photos").child(key).removeValue();
                for(DataSnapshot issue : dataSnapshot.getChildren()){
                    PhotoDetails details = issue.getValue(PhotoDetails.class);
                    storageReference = storage.getReferenceFromUrl(details.getPhotoURL());
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Toast.makeText(context, "Cancelled: ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    private void startEditActivity(Context context, int position, AlbumDetails albumDetails){
        Intent editIntent = new Intent(context, EditAlbumActivity.class);
        editIntent.putExtra("purpose", "edit");
        editIntent.putExtra("details", albumDetails);
        editIntent.putExtra("items", items);
        editIntent.putExtra("icons", icons);
        editIntent.putExtra("position", position);
        fragment.startActivityForResult(editIntent, EDIT_REQUEST);
    }
}
