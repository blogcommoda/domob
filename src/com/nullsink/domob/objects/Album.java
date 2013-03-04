package com.nullsink.domob.objects;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2013 Ed Baker          <edward.david.baker@gmail.com>
 *
 * +------------------------------------------------------------------------+
 * | This program is free software; you can redistribute it and/or          |
 * | modify it under the terms of the GNU General Public License            |
 * | as published by the Free Software Foundation; either version 2         |
 * | of the License, or (at your option) any later version.                 |
 * |                                                                        |
 * | This program is distributed in the hope that it will be useful,        |
 * | but WITHOUT ANY WARRANTY; without even the implied warranty of         |
 * | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
 * | GNU General Public License for more details.                           |
 * |                                                                        |
 * | You should have received a copy of the GNU General Public License      |
 * | along with this program; if not, write to the Free Software            |
 * | Foundation, Inc., 59 Temple Place - Suite 330,                         |
 * | Boston, MA  02111-1307, USA.                                           |
 * +------------------------------------------------------------------------+
 */

import com.nullsink.domob.MediaCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.os.Parcel;
import android.util.Log;

public class Album extends ampacheObject {
    public String artist = "";
    public String tracks = "";
    public String year = "";
    public String extra = null;
    /// Used for calls to Log
    private static final String TAG = "Album";

    public String getType() {
        return "Album";
    }

    public Integer getYear() {
        Integer parsedYear = -1;
        try {
          parsedYear = Integer.parseInt(year);
        } catch (NumberFormatException e) {
          Log.e(TAG, e.getMessage());
        }
        return parsedYear;
    }

    public String extraString() {
        if (extra == null) {
          if (year.equals("N/A")) {
            extra = tracks + " tracks";
          } else {
            extra = year + " - " + tracks + " tracks";
          }
        }
        return extra;
    }

    public Bitmap cachedArtworkBitmap(Context ctx) {
      Bitmap bitmap = null;
      MediaCache mediaCache =  new MediaCache(ctx);
      long albumId = -1;
      String artworkPath = null;

      try {
        albumId = Long.valueOf(id);
      } catch (NumberFormatException e) {
        Log.e(TAG, e.getMessage());
      }

      artworkPath = mediaCache.cachedArtPath(albumId);
      mediaCache.close(); // Close the media cache since we no longer need it

      Log.i(TAG, "Decoding bitmap for artworkPath=" + artworkPath);
      bitmap = BitmapFactory.decodeFile(artworkPath);

      return bitmap;
    }

    public String childString() {
        return "album_songs";
    }

    public boolean hasChildren() {
	return true;
    }

    public String[] allChildren() {
        String[] dir = {"album_songs", this.id};
        return dir;
    }

    public Album() {
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(artist);
        out.writeString(tracks);
        out.writeString(year);
    }

    public Album(Parcel in) {
        super.readFromParcel(in);
        artist = in.readString();
        tracks = in.readString();
        year = in.readString();
    }

    public static final Parcelable.Creator CREATOR
        = new Parcelable.Creator() {
                public Album createFromParcel(Parcel in) {
                    return new Album(in);
                }

                public Album[] newArray(int size) {
                    return new Album[size];
                }
            };
}
