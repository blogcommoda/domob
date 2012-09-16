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

  /*public void cache_file() throws Exception
  {

  }*/
}
