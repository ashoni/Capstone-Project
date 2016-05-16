package com.example.shustrik.vkdocs.download;


import android.content.Context;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Downloads files for the Google Drive
 */
public class GoogleDriveDownloader {
    public static void download(final GoogleApiClient mGoogleApiClient, DriveId mFileId,
                                final MainActivity activity, final Callback callback) {
        final DriveFile file = Drive.DriveApi.getFile(mGoogleApiClient, mFileId);
        file.getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult metadataResult) {
                if (metadataResult.getStatus().isSuccess()) {
                    File tmp = new File(activity.getFilesDir(),
                            System.currentTimeMillis() / 1000 + "_" + metadataResult.getMetadata().getTitle());
                    downloadDriveContentToFile(file, mGoogleApiClient, tmp,
                            metadataResult.getMetadata().getTitle(), activity, callback);
                } else {
                    callback.onDownloadFail(metadataResult.getStatus().getStatusMessage());
                }
            }
        });

    }

    private static void downloadDriveContentToFile(DriveFile file,
                                                   final GoogleApiClient mGoogleApiClient,
                                                   final File localFile,
                                                   final String name,
                                                   final MainActivity activity,
                                                   final Callback callback) {
        ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            callback.onDownloadFail(result.getStatus().getStatusMessage());
                            return;
                        }
                        DriveContents contents = result.getDriveContents();

                        try {
                            InputStream reader = contents.getInputStream();
                            OutputStream output = activity.openFileOutput(localFile.getName(), Context.MODE_PRIVATE);

                            byte data[] = new byte[1024];
                            int count = 0;
                            while ((count = reader.read(data)) != -1) {
                                output.write(data, 0, count);
                            }

                            output.flush();
                            output.close();
                            reader.close();
                        } catch (IOException e) {
                            callback.onDownloadFail(activity.getString(R.string.file_reading_error));
                        }

                        contents.discard(mGoogleApiClient);
                        callback.onDownloadSuccess(localFile, name);
                    }
                };
        file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(contentsOpenedCallback);
    }

    public interface Callback {
        void onDownloadSuccess(File f, String s);

        void onDownloadFail(String reason);
    }
}
