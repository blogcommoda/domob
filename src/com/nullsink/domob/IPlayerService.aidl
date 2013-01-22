//AIDL bindings for the playerService
//
// This file lays out all of the Remote Procedure Calls exposed by the playerService

package com.nullsink.domob;

//Special types that we're using
import com.nullsink.domob.objects.Song;

interface IPlayerService {
    boolean isPlaying();

    void enqueue(in List<Song> songs);

    void playPause();
    void next();
    void prev();
    void seek(int msec);
}