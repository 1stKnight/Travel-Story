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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.project.travel.story.Activities.EditPhotoActivity;
import com.project.travel.story.Activities.MainActivity;
import com.project.travel.story.Fragments.MapFragment;
import com.project.travel.story.R;
import com.project.travel.story.Utility.Model.AlbumDetails;
import com.project.travel.story.Utility.Model.PhotoDetails;
import com.project.travel.story.Utility.PhotoUtility;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotosAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<PhotoDetails> photosList;
    private StfalconImageViewer viewer;
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_HEADER = 2;
    private boolean isLoaderVisible = false;
    public boolean isCheckboxHidden = true;
    public Map<Integer, Boolean> checkedItems;
    StorageReference storageReference;
    FirebaseStorage storage;

    Fragment fragment;

    private static final int EDIT_REQUEST = 4;
    private String[] items = new String[]{"Time", "Tap here to set title", "Press here to add description"};
    private int[] icons = new int[]{R.drawable.ic_clock, R.drawable.ic_edit, R.drawable.ic_edit};

    @NonNull
    private OnItemCheckListener onItemCheckListener;

    public PhotosAdapter(List<PhotoDetails> photosList,Fragment fragment ,@NonNull OnItemCheckListener onItemCheckListener) {
        this.photosList = photosList;
        this.onItemCheckListener = onItemCheckListener;
        this.fragment = fragment;
        checkedItems = new TreeMap<>(Collections.reverseOrder());
        storage = FirebaseStorage.getInstance().getReference().getStorage();
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false));
            case VIEW_TYPE_LOADING:
                return new FooterHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            case VIEW_TYPE_HEADER:
                return new HeaderHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_photos_header, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        if(holder instanceof ViewHolder){
            ViewHolder viewHolder = (ViewHolder) holder;
        }
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if(isHeader(position)) return VIEW_TYPE_HEADER;
        if (isLoaderVisible) {
            return position == photosList.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }

    public void add(PhotoDetails response) {
        photosList.add(response);
        notifyItemInserted(photosList.size() - 1);
    }

    public void addAll(List<PhotoDetails> postItems) {
        for (PhotoDetails response : postItems) {
            add(response);
        }
    }

    public void remove(PhotoDetails postItems) {
        int position = photosList.indexOf(postItems);
        if (position > -1) {
            photosList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void update(PhotoDetails response, int position){
        photosList.set(position, response);
        notifyItemChanged(position);
    }

    public int getIndex(PhotoDetails postItems){
        return photosList.indexOf(postItems);
    }

    public void addLoading() {
        isLoaderVisible = true;
        add(new PhotoDetails());
    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = photosList.size() - 1;
        PhotoDetails item = getItem(position);
        if (item != null && position != 0) {
            photosList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addHeader(){
        add(new PhotoDetails());
    }

    public void updateHeader(){

    }

    public void clear() {
        while (getItemCount() > 1) {
            remove(getItem(1));
        }
        checkedItems.clear();
    }

    PhotoDetails getItem(int position) {
        return photosList.get(position);
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.photo_thumbnail)
        ImageView photoThumbnail;
        @BindView(R.id.layout_checkbox)
        RelativeLayout checkboxLayout;
        @BindView(R.id.checkbox)
        CheckBox checkbox;
        @BindView(R.id.background)
        CardView cardBackground;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            checkbox.setClickable(false);
            checkbox.setChecked(false);
        }

        @Override
        protected void clear() {

        }

        public void onBind(int position){
            super.onBind(position);
            final PhotoDetails photo = photosList.get(position);
            if(isCheckboxHidden) checkboxLayout.setVisibility(View.GONE);
            else checkboxLayout.setVisibility(View.VISIBLE);
            checkbox.setChecked(checkedItems.getOrDefault(position, false));
            PhotoUtility.loadImage(itemView.getContext(), photo.getPhotoURL(), photoThumbnail);
            photoThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageClick(itemView.getContext(), position, photo);
                }
            });
            checkboxLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkbox.isChecked()){
                        onItemCheckListener.onItemUncheck(photo);
                        checkedItems.remove(position);
                        checkbox.setChecked(false);
                    }
                    else{
                        onItemCheckListener.onItemCheck(photo);
                        checkedItems.put(position, true);
                        checkbox.setChecked(true);
                    }
                }
            });
            setBackgroundColor(itemView.getContext(), photo.getCategory(), cardBackground);
        }

        private void setBackgroundColor(Context context, String category, CardView cardBackground){
            switch (category) {
                case "Default":
                    cardBackground.setCardBackgroundColor(ContextCompat.getColor(context, R.color.Default));
                    break;
                case "Friends":
                    cardBackground.setCardBackgroundColor(ContextCompat.getColor(context, R.color.Friends));
                    break;
                case "Nature":
                    cardBackground.setCardBackgroundColor(ContextCompat.getColor(context, R.color.Nature));
                    break;
            }
        }
    }

    public class FooterHolder extends BaseViewHolder {

        ProgressBar mProgressBar;

        FooterHolder(View view) {
            super(view);
            mProgressBar = view.findViewById(R.id.progressBar);
        }

        @Override
        protected void clear() {   }

    }

    public class HeaderHolder extends BaseViewHolder {

        @BindView(R.id.albumCover)
        ImageView albumCover;
        @BindView(R.id.albumTitle)
        TextView albumTitle;

        HeaderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            fillHeader(itemView.getContext());
        }

        @Override
        protected void clear() {   }

        private void fillHeader(Context context){
            Query query;
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            query = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(uid).child("albums")
                    .child(MainActivity.currentAlbum.getValue());

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    AlbumDetails album = dataSnapshot.getValue(AlbumDetails.class);
                    albumTitle.setText(album.getTitle());
                    PhotoUtility.loadImage(itemView.getContext(), album.getPhotoURL(), albumCover);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Cancelled: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean isHeader(int position) {
        return position == 0;
    }

    private void imageClick(final Context context, int position, final PhotoDetails item){
        String[] a = new String[]{item.getPhotoURL()};
        int positionCorrect = photosList.indexOf(item);
        LayoutInflater inflater = LayoutInflater.from(context);
        View overlay = inflater.inflate(R.layout.photos_image_overlay, null);
        TextView description = overlay.findViewById(R.id.imageOverlayDescriptionText);
        ImageView edit = overlay.findViewById(R.id.imageOverlayEditButton);
        ImageView delete = overlay.findViewById(R.id.imageOverlayDeleteButton);
        ImageView download = overlay.findViewById(R.id.imageOverlayDownloadButton);
        final ImageView share = overlay.findViewById(R.id.imageOverlayShareButton);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditActivity(context, positionCorrect, item);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePhotoDialog(context, item);
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String downloadFrom = "images/users/" + FirebaseAuth.getInstance().getUid() + "/photos/" + item.getKey();
                new PhotoUtility().startPhotoDownloadService(context, downloadFrom);
            }
        });
        description.setText(item.getDescription());

       viewer =  new StfalconImageViewer.Builder<>(context, a, new ImageLoader<String>() {
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
                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", item.getDescription());
                                clipboard.setPrimaryClip(clip);
                                PhotoUtility photoUtility = new PhotoUtility();
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

    private void deletePhotoDialog(final Context context, final PhotoDetails item){
        int index = getIndex(item);
        final AlertDialog builder = new AlertDialog.Builder(context).create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_yes_no, null);
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        Button buttonOK = dialogView.findViewById(R.id.buttonOK);
        Button buttonClose = dialogView.findViewById(R.id.buttonClose);

        titleTextView.setText(R.string.delete_photo);

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
                        // File deleted successfully
                        if(MapFragment.markers != null) {
                            MapFragment.markers.get(index - 1).remove();
                            MapFragment.markers.remove(index - 1);
                        }
                        deletePhoto(item.getKey());
                        remove(item);
                        viewer.dismiss();
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

    private void deletePhoto(String key){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref.child("users").child(uid).child("photos")
                .child(MainActivity.currentAlbum.getValue()).child(key).removeValue();
    }

    private void startEditActivity(Context context, int position, PhotoDetails photoDetails){
        Intent editIntent = new Intent(context, EditPhotoActivity.class);
        editIntent.putExtra("purpose", "edit");
        editIntent.putExtra("details", photoDetails);
        editIntent.putExtra("items", items);
        editIntent.putExtra("icons", icons);
        editIntent.putExtra("position", position);
        viewer.dismiss();
        fragment.startActivityForResult(editIntent, EDIT_REQUEST);
    }
}
