package com.sound.ampache;

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

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import com.sound.ampache.staticMedia.MediaPlayerControl;
import com.sound.ampache.objects.Song;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.Toast;
import android.net.Uri;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

public final class playlistActivity extends Activity implements MediaPlayerControl, OnBufferingUpdateListener, OnCompletionListener, OnItemClickListener
{
    private staticMedia mc;
    private ListView lv;

    private playlistAdapter pla;
    private Boolean prepared = true;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        /* make sure we're authenticated */
        amdroid.comm.ping();

        amdroid.mp.setOnBufferingUpdateListener(this);
        amdroid.mp.setOnCompletionListener(this);
        mc = new staticMedia(this);
     
        amdroid.mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    amdroid.mp.start();
                    if (amdroid.playListVisible) {
                        mc.setEnabled(true);
                        mc.show();
                        prepared = true;
                    }
                }});

        mc.setPrevNextListeners(new nextList(), new prevList());

        // Set up our view :D
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout v = (LinearLayout) inflate.inflate(R.layout.playlist, null);
        v.addView(mc.getController());
        setContentView(v);

        lv = (ListView) findViewById(R.id.list);
        lv.setOnItemClickListener(this);

        pla = new playlistAdapter(this);
        lv.setAdapter(pla);

        mc.setMediaPlayer(this);

        if (amdroid.mp.isPlaying()) {
            mc.setEnabled(true);
            mc.show();
        } else {
            mc.setEnabled(false);
        }

        // Center the playlist at the current song
        centerList( 0 );
    }

    // List for keeping track of Shuffle/Random
    private ArrayList shuffleHistory = new ArrayList();

    private boolean shuffleEnabled = false;
    private boolean  repeatEnabled = false;

    private void nextInPlaylist() {
        if ( shuffleEnabled ) {
            // So we don't play a song more than once
            if ( !shuffleHistory.contains( amdroid.playingIndex ) )
                shuffleHistory.add( amdroid.playingIndex );

            // Just played the last song, repeat if repeat is enabled, stop otherwise
            if ( shuffleHistory.size() >= amdroid.playlistCurrent.size() && repeatEnabled )
                shuffleHistory.clear();
            else
		amdroid.playingIndex = amdroid.playlistCurrent.size();

            int next = 0;
            Random rand = new Random();

            // Try random numbers until finding one that is not used
            do {
                next = rand.nextInt( amdroid.playlistCurrent.size() );
            } while ( shuffleHistory.contains( next ) );

            // Set next playing index
            amdroid.playingIndex = next;
        } else {
            amdroid.playingIndex++;

            // Reset playlist to beginning if repeat is enabled
            if ( amdroid.playingIndex >= amdroid.playlistCurrent.size() && repeatEnabled )
                amdroid.playingIndex = 0;
        }
    }

    private void prevInPlaylist() {
        if ( shuffleEnabled ) {
            int currIndex = shuffleHistory.indexOf( amdroid.playingIndex );

            // Call a random next song if this is the first song
            if ( shuffleHistory.size() < 1 ) {
                nextInPlaylist();

                return;
            }

            // Previous (Current item is not in the shuffle history)
            if ( currIndex == -1 ) {
                // Set previous song
                amdroid.playingIndex = (Integer)shuffleHistory.get( shuffleHistory.size() - 1 );

                // Remove item, I consider Previous like an undo
                shuffleHistory.remove( shuffleHistory.size() - 1 );
            }
            // This shouldn't be possible, but...
            else if ( currIndex > 0 ) {
                amdroid.playingIndex = (Integer)shuffleHistory.get( currIndex - 1 );

                shuffleHistory.remove( currIndex );
            }
        }
        // Do not call previous if it is the first song
        else if ( amdroid.playingIndex > 0 )
            amdroid.playingIndex--;
    }

    /* on pause and on resume make sure that we don't attempt to display the MediaController when 
     * we can't see it */
    @Override
    protected void onResume() {
        super.onResume();
        amdroid.playListVisible = true;
    }
    
    @Override
    protected void onPause() {
        super.onResume();
        amdroid.playListVisible = false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.pl_clear:
            if (isPlaying())
                amdroid.mp.stop();
            amdroid.playingIndex = 0;
            pla.clearItems();
            mc.setEnabled(false);
            break;

        case R.id.pl_save:
            try {
                FileOutputStream pout = openFileOutput("playlist", 0);
                ObjectOutputStream pos = new ObjectOutputStream(pout);
                pos.writeObject(amdroid.playlistCurrent);
                pout.close();
            } catch (Exception poo) {
                Toast.makeText(this, "Error: " + poo.toString(), Toast.LENGTH_LONG).show();
            }
            break;

        case R.id.pl_load:
            if (isPlaying())
                amdroid.mp.stop();
            amdroid.playingIndex = 0;
            mc.setEnabled(false);
            try {
                FileInputStream pin = openFileInput("playlist");
                ObjectInputStream poin = new ObjectInputStream(pin);
                amdroid.playlistCurrent = (ArrayList<Song>) poin.readObject();
                pin.close();
            } catch (Exception poo) {
                Toast.makeText(this, "Error: " + poo.toString(), Toast.LENGTH_LONG).show();
            }
            pla.refresh();
            break;

	case R.id.pl_shuffle:
	    if ( item.isChecked() ) {
		// Clean Shuffle History
		shuffleHistory.clear();

		item.setChecked( false );
		item.setTitle( R.string.shuffle );

		// Disable Shuffle
		shuffleEnabled = false;
	    } else {
		item.setChecked( true );
		item.setTitle( R.string.shuffle2 );

		// Enable Shuffle
		shuffleEnabled = true;
	    }

	    break;

	case R.id.pl_repeat:
	    if ( item.isChecked() ) {
		item.setChecked( false );
		item.setTitle( R.string.repeat );

		// Disable Repeat
		repeatEnabled = false;
	    } else {
		item.setChecked( true );
		item.setTitle( R.string.repeat2 );

		// Enable Repeat
		repeatEnabled = true;
	    }

	    break;
        }
        return true;
    }


    /* callbacks for the MediaPlayer and MediaController */
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        amdroid.bufferPC = percent;
    }

    public int getBufferPercentage() {
        return amdroid.bufferPC;
    }

    public int getCurrentPosition() {
        if (amdroid.mp.isPlaying()) {
            return amdroid.mp.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (amdroid.mp.isPlaying()) {
            return amdroid.mp.getDuration();
        }
        return 0;
    }

    public boolean isPlaying() {
        return amdroid.mp.isPlaying();
    }

    public void pause() {
        if (amdroid.mp.isPlaying()) {
            amdroid.mp.pause();
        }
    }

    public void seekTo(int pos) {
        if (amdroid.mp.isPlaying()) {
            amdroid.mp.seekTo(pos);
        }
    }

    public void start() {
        amdroid.mp.start();
    }

    public void play() {
        if (amdroid.playingIndex >= amdroid.playlistCurrent.size()) {
            amdroid.playingIndex = amdroid.playlistCurrent.size();
            mc.setEnabled(false);
            return;
        }

        if (amdroid.playingIndex < 0) {
            amdroid.playingIndex = 0;
            mc.setEnabled(false);
            return;
        }

        Song chosen = (Song) amdroid.playlistCurrent.get(amdroid.playingIndex);

        if (amdroid.mp.isPlaying()) {
            amdroid.mp.stop();
        }

        amdroid.mp.reset();
        try {
            amdroid.mp.setDataSource(chosen.liveUrl());
            amdroid.mp.prepareAsync();
            prepared = false;
        } catch (Exception blah) {
            return;
        }
        turnOnPlayingView();
    }

    /* These functions help with displaying the |> icon next to the currently playing song */
    private void turnOffPlayingView() {
        if (amdroid.playingIndex >= lv.getFirstVisiblePosition() && amdroid.playingIndex <= lv.getLastVisiblePosition()) {
            View holder = lv.getChildAt(amdroid.playingIndex - lv.getFirstVisiblePosition());
            if (holder != null) {
                ImageView img = (ImageView) holder.findViewById(R.id.art);
                img.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void turnOnPlayingView() {
        if (amdroid.playingIndex >= lv.getFirstVisiblePosition() && amdroid.playingIndex <= lv.getLastVisiblePosition()) {
            View holder = lv.getChildAt(amdroid.playingIndex - lv.getFirstVisiblePosition());
            if (holder != null) {
                ImageView img = (ImageView) holder.findViewById(R.id.art);
                img.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onCompletion(MediaPlayer media) {
        turnOffPlayingView();
        nextInPlaylist();
        play();
    }

    public void onItemClick(AdapterView l, View v, int position, long id) {
        if (prepared) {
            turnOffPlayingView();
            amdroid.playingIndex = position;
            play();
        }
    }

    /* our child classes */

    private class prevList implements OnClickListener
    {
        public void onClick(View v) {
            turnOffPlayingView();
            prevInPlaylist();

            // Center the playlist just above the next song (-1 is handled by android)
            centerList( -1 );

            play();
        }
    }

    private class nextList implements OnClickListener
    {
        public void onClick(View v) {
            turnOffPlayingView();
            nextInPlaylist();

            // Center the playlist just above the next song (-1 is handled by android)
            centerList( -1 );

            play();
        }
    }

    private void centerList ( int adjust )
    {
            lv.setSelection( amdroid.playingIndex + adjust );
    }

    private class playlistAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        public playlistAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return amdroid.playlistCurrent.size();
        }

        public Object getItem(int position) {
            return amdroid.playlistCurrent.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void refresh() {
            notifyDataSetChanged();
        }

        public void clearItems() {
            amdroid.playlistCurrent.clear();
            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            plI holder;
            Song cur = amdroid.playlistCurrent.get(position);

            /* we don't reuse */
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.playlist_item, null);
                holder = new plI();

                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.other = (TextView) convertView.findViewById(R.id.other);
                holder.art = (ImageView) convertView.findViewById(R.id.art);

                convertView.setTag(holder);
            } else {
                holder = (plI) convertView.getTag();
            }

            holder.title.setText(cur.name);
            holder.other.setText(cur.extraString());
            if (amdroid.mp.isPlaying() && amdroid.playingIndex == position) {
                holder.art.setVisibility(View.VISIBLE);
            } else {
                holder.art.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }

    static class plI {
        TextView title;
        TextView other;
        ImageView art;
    }

}

// ex:tabstop=4 shiftwidth=4 expandtab:

