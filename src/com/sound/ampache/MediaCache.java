package com.sound.ampache;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;
import java.io.File;

public class MediaCache
{
  private DownloadManager mDownloadManager;
  private long mDownloadId;
  private Context mContext;
  private static final long maxCacheSize = 100*1024*1024; /// Maximum amount of data to cache
  private File cacheDir; /// Folder to store all of the local files
  private File tempDownloadDir; /// Folder to temporarily store files while downloading
  private static final String TAG = "MediaCache"; /// Used for calls to Log
  /// Called when the download finishes. This calls our private method to actually do the work.
  BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver()
  {
    public void onReceive(Context context, Intent intent)
    {
      download_complete(intent);
    }
  };

  MediaCache (Context mCtxt)
  {
    mContext = mCtxt;
    mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);

    // Setup the directory to store the cache on the external storage
    File externalMusicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    cacheDir = new File(externalMusicDir.getAbsolutePath() + "/ampachecache");
    if (cacheDir.exists() == false)
    {
      Log.i(TAG, cacheDir + " does not exist, creating directory.");
      cacheDir.mkdirs();
    }

    // Setup the directory to store the temporary DownloadManager files
    File externalDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    tempDownloadDir = new File(externalDownloadDir.getAbsolutePath() + "/ampachetmp");
    if (tempDownloadDir.exists() == false)
    {
      Log.i(TAG, tempDownloadDir + " does not exist, creating directory.");
      tempDownloadDir.mkdirs();
    }

    // When the Android download manager finishes a download
    mContext.registerReceiver(downloadCompleteReceiver,
                              new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  /** \brief Add a song to the local music cache.
   *  \param[in] songUid The unique ID as from Ampache.
   *  \param[in] songUrl The actual live URL to download the track from Ampache.
   */
  public void cache_song(long songUid, String songUrl) throws Exception
  {
    // If the song is already cached, we are already done
    if (check_if_cached(songUid) == true)
    {
      return;
    }

    Log.i(TAG, "Attempting to cache song ID " + songUid);
    // Generate a new request to then add to the download manager queue.
    Request request = new Request(Uri.parse(songUrl));
    // We can keep track of the Ampache song ID in the download description
    request.setDescription(String.valueOf(songUid));
    // Set the destination to the external device in the downloads directory
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                              "ampachetmp/" + songUid);
    // Queue up the request
    mDownloadId = mDownloadManager.enqueue(request);
  }

  /** \brief Handle the song finished download.
   *
   */
  private void download_complete(Intent intent)
  {
    Log.i(TAG, "In download_complete method");
    String action = intent.getAction();
    // Check to see if the action corresponds to a completed download
    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
      // Query for more info using the ID
      Query query = new Query();
      query.setFilterById(mDownloadId);
      Cursor cur = mDownloadManager.query(query);

      // Access the first row of data returned
      if (cur.moveToFirst())
      {
        // Find the column which corresponds to the download status
        int statusIndex = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
        // If the download was successful try and move the file to our cache location
        if (DownloadManager.STATUS_SUCCESSFUL == cur.getInt(statusIndex))
        {
          // Find the column which corresponds to the current file URI
          int uriIndex = cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
          // Retreive the temporary URI to where DownloadManager stored the file
          String downloadUri = cur.getString(uriIndex);
          File downloadFile = new File(Uri.parse(downloadUri).getPath());

          // Find the column which corresponds to the description we provided (Ampache song id)
          int descriptionIndex = cur.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION);
          // Retreive the description
          long ampacheSongUid = cur.getLong(descriptionIndex);

          // Setup the destination file
          String destinationPath = cached_song_path(ampacheSongUid);
          File destinationFile = new File(Uri.parse(destinationPath).getPath());

          // Move the file
          Log.i(TAG, "Moving " + downloadFile + " to " + destinationFile);
          if (downloadFile.renameTo(destinationFile))
          {
            Log.i(TAG, destinationFile + " moved successfully");
          }
        }
      }
    }
  }

  /** \brief Check to see if a song is already in the local music cache.
   *  \return Returns true if the song is already cached, false otherwise.
   *  \param[in] songUid The unique ID as from Ampache.
   */
  public boolean check_if_cached(long songUid) throws Exception
  {
    // Initially set to false. Will switch to true if we find the file.
    boolean cached = false;
    // Construct the path to check for the cached song
    File testFile = new File(cached_song_path(songUid));

    Log.i(TAG, "Checking if " + testFile + " exists.");
    if (testFile.exists() == true)
    {
      cached = true;
      Log.i(TAG, testFile + " exists.");
    }

    return cached;
  }

  /**
   * \return Returns a string with the path to the cached file or location
   *         where the file would be cached. In other words, this takes a
   *         song UID and converts that into a string for the file path.
   * \param[in] songUid the unique ID as from Ampache
   */
  public String cached_song_path(long songUid)
  {
    String path = cacheDir.getAbsolutePath() + "/" + songUid;
    return path;
  }
}

