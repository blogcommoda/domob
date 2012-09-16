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
import android.view.View;
import android.widget.ImageView;

public class MediaCache
{
  private DownloadManager dm;
  private long enqueue;
  private Context mContext;

  MediaCache (Context mCtxt)
  {
    mContext = mCtxt;
    dm = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
  }

  /** \brief Add a song to the local music cache.
   *  \param[in] songUid The unique ID as from Ampache.
   */
  public void cache_file(long songUid) throws Exception
  {

  }

  /** \brief Check to see if a song is already in the local music cache.
   *  \return Returns true if the song is already cached, false otherwise.
   *  \param[in] songUid The unique ID as from Ampache.
   */
  private boolean check_if_cached(long songUid) throws Exception
  {
    // Initially set to false. Will switch to true if we find the file.
    boolean cached = false;

    return cached;
  }
}

