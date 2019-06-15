package com.project.travel.story.Utility.Service;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.project.travel.story.Activities.MainActivity;
import com.project.travel.story.R;
import com.project.travel.story.Utility.PhotoUtility;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DownloadService extends BaseTaskService {

    private static final String TAG = "DownloadService";

    /** Actions **/
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String DOWNLOAD_COMPLETED = "download_completed";
    public static final String DOWNLOAD_ERROR = "download_error";

    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";

    private StorageReference mStorageRef;
    private PhotoUtility photoUtility;
    private String mCurrentPhotoPath;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        photoUtility = new PhotoUtility();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            // Get the path to download from the intent
            String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);
            downloadFromPath(downloadPath);
        }

        return START_REDELIVER_INTENT;
    }

    private void downloadFromPath(final String downloadPath) {
        Log.d(TAG, "downloadFromPath:" + downloadPath);

        // Mark task started
        taskStarted();
        showProgressNotification(getString(R.string.progress_downloading), 0, 0);
        File localFile = null;
        try {
            localFile = photoUtility.createImageFile(getResources().getString(R.string.app_name));
            mCurrentPhotoPath = localFile.getAbsolutePath();
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
            Toast.makeText(this,"Can't create file", Toast.LENGTH_SHORT).show();
        }

        mStorageRef.child(downloadPath).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "download:SUCCESS");

                // Send success broadcast with number of bytes downloaded
                broadcastDownloadFinished(downloadPath, taskSnapshot.getTotalByteCount());
                showDownloadFinishedNotification(downloadPath, (int) taskSnapshot.getTotalByteCount());
                photoUtility.galleryAddPic(mCurrentPhotoPath ,getApplicationContext());

                // Mark task completed
                taskCompleted();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Dismiss Progress Dialog\\
                Log.w(TAG, "download:FAILURE", e);
                e.printStackTrace();
                // Send failure broadcast
                broadcastDownloadFinished(downloadPath, -1);
                showDownloadFinishedNotification(downloadPath, -1);

                // Mark task completed
                taskCompleted();

            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                long bytesDownloaded = taskSnapshot.getBytesTransferred();
                long totalBytes = taskSnapshot.getTotalByteCount();
                showProgressNotification(getString(R.string.progress_downloading), bytesDownloaded, totalBytes);
            }
        });
    }

    /**
     * Broadcast finished download (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastDownloadFinished(String downloadPath, long bytesDownloaded) {
        boolean success = bytesDownloaded != -1;
        String action = success ? DOWNLOAD_COMPLETED : DOWNLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded);
        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished download.
     */
    private void showDownloadFinishedNotification(String downloadPath, int bytesDownloaded) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_DOWNLOAD_PATH, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = bytesDownloaded != -1;
        String caption = success ? getString(R.string.download_success) : getString(R.string.download_failure);
        showFinishedNotification(caption, intent, true);
    }


    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_COMPLETED);
        filter.addAction(DOWNLOAD_ERROR);

        return filter;
    }
}
