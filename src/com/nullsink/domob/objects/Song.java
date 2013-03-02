package com.nullsink.domob.objects;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Jacob Alexander   < haata@users.sf.net >
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

import android.os.Parcelable;
import android.os.Parcel;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.lang.ClassNotFoundException;

public class Song extends ampacheObject implements Externalizable {
    public String artist = "";
    public String art = "";
    public String url = "";
    public String album = "";
    public String albumId = "";
    public String genre = "";
    public String extra = null;

    public String getType() {
        return "Song";
    }

    public String extraString() {
        if (extra == null) {
            extra = artist + " - " + album;
        }

        return extra;
    }

    public String childString() {
        return "";
    }

    /* Replace the old session id with our current one */
    public String liveUrl() {
        return url.replaceAll("sid=[^&]+","sid=" + com.nullsink.domob.domob.comm.authToken).replaceFirst(".ogg$", ".mp3");
    }

    /* Replace old session id, to use with the Album Art */
    public String liveArt() {
        String updatedArt;

        updatedArt = art.replaceAll("auth=[^&]+","auth=" + com.nullsink.domob.domob.comm.authToken);

        // TODO: Chat with Ampache team to find out what is going on with artwork URL.
        // Ampache returns URL                  foo/ampache/image.php?id=55object_type=album&auth=12345&name=art.jpg
        // The correct URL is something such as foo/ampache/image.php?id=55&auth=12345
        updatedArt = updatedArt.replace("&name=art.jpg", "");
        updatedArt = updatedArt.replace("object_type=album", "");

        return updatedArt;
    }
    
    public boolean hasChildren() {
        return false;
    }

    public String[] allChildren() {
        return null;
    }

    public Song() {
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(artist);
        out.writeString(art);
        out.writeString(url);
        out.writeString(album);
        out.writeString(albumId);
        out.writeString(genre);
        out.writeString(extra);
    }

    public Song(Parcel in) {
        super.readFromParcel(in);
        artist = in.readString();
        art = in.readString();
        url = in.readString();
        album = in.readString();
        albumId = in.readString();
        genre = in.readString();
        extra = in.readString();
    }

    public static final Parcelable.Creator CREATOR
        = new Parcelable.Creator() {
                public Song createFromParcel(Parcel in) {
                    return new Song(in);
                }

                public Song[] newArray(int size) {
                    return new Song[size];
                }
            };

    /* for external */

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = (String) in.readObject();
        name = (String) in.readObject();
        artist = (String) in.readObject();
        art = (String) in.readObject();
        url = (String) in.readObject();
        album = (String) in.readObject();
        albumId = (String) in.readObject();
        genre = (String) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(id);
        out.writeObject(name);
        out.writeObject(artist);
        out.writeObject(art);
        out.writeObject(url);
        out.writeObject(album);
        out.writeObject(albumId);
        out.writeObject(genre);
    }

}

// ex:tabstop=4 shiftwidth=4 expandtab:

