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
  private DownloadManager dm;
  private long enqueue;
  private Context mContext;
  private static final long maxCacheSize = 100*1024*1024; /// Maximum amount of data to cache
  private File cacheDir; /// Folder to store all of the local files
  private static final String TAG = "MediaCache"; /// Used for calls to Log

  MediaCache (Context mCtxt)
  {
    mContext = mCtxt;
    dm = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);

    // Setup the directory to store the cache on the external storage
    File externalMusicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    cacheDir = new File(externalMusicDir.getAbsolutePath() + "/ampachecache");
    if (cacheDir.exists() == false)
    {
      Log.i(TAG, cacheDir + " does not exist, creating directory.");
      cacheDir.mkdirs();
    }
  }

  /** \brief Add a song to the local music cache.
   *  \param[in] songUid The unique ID as from Ampache.
   */
  public void cache_song(long songUid) throws Exception
  {
    // If the song is already cached, we are already done
    if (check_if_cached(songUid) == true)
    {
      return;
    }

    Log.i(TAG, "Attempting to cache song ID " + songUid);
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
    File testFile = new File(cacheDir.getAbsolutePath() + "/" + songUid);

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
  private String cached_song_path(long songUid) throws Exception
  {
    String path = cacheDir.getAbsolutePath() + "/" + songUid;
    return path;
  }
}

