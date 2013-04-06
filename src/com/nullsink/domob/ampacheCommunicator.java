package com.nullsink.domob;

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

import java.net.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.ArrayList;
import com.nullsink.domob.objects.*;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.Integer;
import java.lang.Long;
import java.security.MessageDigest;
import java.util.Date;

public class ampacheCommunicator
{

    public String authToken = "";
    public int artists;
    public int albums;
    public int songs;
    private String update;
    private Context mCtxt;
    public String lastErr;
    /// Used for calls to Log
    private static final String TAG = "ampacheCommunicator";

    private XMLReader reader;

    private SharedPreferences prefs;
    /// Ampache server URL
    private URL mServerUrl;

    public ampacheCommunicator(SharedPreferences preferences, Context context) throws Exception {
        prefs = preferences;
        mCtxt = context;
        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
        reader = XMLReaderFactory.createXMLReader();
        parsePreferences(preferences);
    }

    /**
     * Parses the Ampache server connection preferences and constructs a URL
     * @param preferences
     */
    private void parsePreferences(SharedPreferences preferences) {
      boolean missingProtocol = false;
      URL tempURL = null;
      String serverProtocol;
      String urlPreference;

      // Load the protocol preference, this should be good already
      serverProtocol = preferences.getString("server_protocol_preference", "");
      // Read the URL preference field, this will need some checking
      urlPreference = preferences.getString("server_url_preference", "");

      // See if constructing a URL directly from the preference works
      try {
        tempURL = new URL(urlPreference);
      } catch (MalformedURLException e) {
        if (e.getMessage().contains("Protocol not found")) {
          missingProtocol = true;
        }
        lastErr = e.getMessage();
      }

      // If the protocol is missing, create the URL again
      if (missingProtocol) {
        try {
          tempURL = new URL(serverProtocol + "://" + urlPreference);
        } catch (MalformedURLException e) {
          lastErr = e.getMessage();
        }
      }

      // Force the protocol to be the value in the preferences
      try {
        tempURL = new URL(serverProtocol, tempURL.getHost(), tempURL.getFile());
      } catch (MalformedURLException e) {
        lastErr = e.getMessage();
      }

      // Finally update the class variable
      mServerUrl = tempURL;
    }

    public void ping() {
        dataHandler hand = new dataHandler();
        reader.setContentHandler(hand);
        try {
            reader.parse(new InputSource(fetchFromServer("auth=" + this.authToken)));
            if (hand.errorCode == 401) {
                this.perform_auth_request();
            }
        } catch (Exception poo) {
        }
    }

    public void perform_auth_request() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        /* Get the current time, and convert it to a string */
        String time = Long.toString((new Date()).getTime() / 1000);

        /* build our passphrase hash */
        md.reset();

        /* first hash the password */
        String pwHash = prefs.getString("server_password_preference", "");
        md.update(pwHash.getBytes(), 0, pwHash.length());
        String preHash = time + asHex(md.digest());

        /* then hash the timestamp in */
        md.reset();
        md.update(preHash.getBytes(), 0, preHash.length());
        String hash = asHex(md.digest());

        /* request server auth */
        ampacheAuthParser hand = new ampacheAuthParser();
        reader.setContentHandler(hand);
        String user = prefs.getString("server_username_preference", "");
        try {
            reader.parse(new InputSource(fetchFromServer("action=handshake&auth="+hash+"&timestamp="+time+"&version=350001&user="+user)));
        } catch (Exception poo) {
            lastErr = "Could not connect to server";
        }

        if (hand.errorCode != 0) {
            lastErr = hand.error;
        }

        authToken = hand.token;
        artists = hand.artists;
        albums = hand.albums;
        songs = hand.songs;
        update = hand.update;
    }

    public InputStream fetchFromServer(String append) throws Exception {
        String newUrl = mServerUrl.toString() + "/server/xml.server.php?" + append;
        URL fullUrl = new URL(newUrl);
        return fullUrl.openStream();
    }

    public interface ampacheDataReceiver
    {
        public void receiveObjects(ArrayList data);
    }

    public class ampacheRequestHandler extends Thread
    {
        private ampacheDataReceiver recv = null;
        private dataHandler hand;
        private Context mCtx;

        private String type;
        private String filter;

        public Handler incomingRequestHandler;
        public Boolean stop = false;

        public void run() {
            Looper.prepare();

            incomingRequestHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        String[] directive = (String[]) msg.obj;
                        String append = "";
                        boolean goodcache = false;
                        String error = null;
                        Message reply = new Message();
                        ArrayList<ampacheObject> goods = null;
                        InputSource dataIn = null;

                        append = "action=" + directive[0];

                        if (directive[0].equals("artists")) {
                            hand = new ampacheArtistParser();
                        } else if (directive[0].equals("artist_albums")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheAlbumParser();
                        } else if (directive[0].equals("artist_songs")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheSongParser();
                        } else if (directive[0].equals("album_songs")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheSongParser();
                        } else if (directive[0].equals("playlist_songs")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheSongParser();
                        } else if (directive[0].equals("tag_artists")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheArtistParser();
                        } else if (directive[0].equals("tag_songs")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheSongParser();
                        } else if (directive[0].equals("albums")) {
                            hand = new ampacheAlbumParser();
                        } else if (directive[0].equals("playlists")) {
                            hand = new ampachePlaylistParser();
                        } else if (directive[0].equals("songs")) {
                            hand = new ampacheSongParser();
                        } else if (directive[0].equals("tags")) {
                            hand = new ampacheTagParser();
                        } else if (directive[0].equals("search_songs")) {
                            hand = new ampacheSongParser();
                            append += "&filter=" + directive[1];
                        } else {
                            return;
                        }

                        if (msg.what == 0x1336) {
                            append += "&offset=" + msg.arg1 + "&limit=100";
                            reply.arg1 = msg.arg1;
                            reply.arg2 = msg.arg2;
                        }

                        append += "&auth=" + authToken;

                        if (stop == true) {
                            stop = false;
                            return;
                        }

                        /* now we fetch */
                        try {
                            dataIn = new InputSource(fetchFromServer(append));
                        } catch (Exception poo) {
                            error = poo.toString();
                        }

                        if (stop == true) {
                            stop = false;
                            return;
                        }

                        /* all done loading data, now to parse */
                        reader.setContentHandler(hand);
                        try {
                            reader.parse(dataIn);
                        } catch (Exception poo) {
                            error = poo.toString();;
                        }

                        if (hand.error != null) {
                            if (hand.errorCode == 401) {
                                try {
                                    ampacheCommunicator.this.perform_auth_request();
                                    this.sendMessage(msg);
                                } catch (Exception poo) {
                                }
                                return;
                            }
                            error = hand.error;
                        }

                        if (stop == true) {
                            stop = false;
                            return;
                        }

                        if (error == null) {
                            reply.what = msg.what;
                            reply.obj = hand.data;
                        } else {
                            reply.what = 0x1338;
                            reply.obj = error;
                        }
                        try {
                            msg.replyTo.send(reply);
                        } catch (Exception poo) {
                            //well shit, that sucks doesn't it
                        }
                    }
                };
            Looper.loop();
        }
    }

    private class dataHandler extends DefaultHandler {
        public ArrayList<ampacheObject> data = new ArrayList();
        public String error = null;
        public int errorCode = 0;
        protected CharArrayWriter contents = new CharArrayWriter();

        public void startDocument() throws SAXException {

        }

        public void endDocument() throws SAXException {

        }

        public void characters( char[] ch, int start, int length )throws SAXException {
            contents.write( ch, start, length );
        }

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            if (localName.equals("error"))
                errorCode = Integer.parseInt(attr.getValue("code"));
            contents.reset();
        }

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {
            if (localName.equals("error")) {
                error = contents.toString();
            }
        }

    }

    private class ampacheAuthParser extends dataHandler {
        public String token = "";
        public int artists = 0;
        public int albums = 0;
        public int songs = 0;
        public String update = "";

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

            super.endElement(namespaceURI, localName, qName);

            if (localName.equals("auth")) {
                token = contents.toString();
            }

            if (localName.equals("artists")) {
                artists = Integer.parseInt(contents.toString());
            }
            if (localName.equals("albums")) {
                albums = Integer.parseInt(contents.toString());
            }
            if (localName.equals("songs")) {
                songs = Integer.parseInt(contents.toString());
            }

            if (localName.equals("add")) {
                update = contents.toString();
            }
        }
    }

    private class ampacheArtistParser extends dataHandler {
        private Artist current;

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("artist")) {
                current = new Artist();
                current.id = attr.getValue("id");
            }
        }

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

            super.endElement(namespaceURI, localName, qName);

            if (localName.equals("name")) {
                current.name = contents.toString();
            }

            if (localName.equals("albums")) {
                current.albums = contents.toString() + " albums";
            }

            if (localName.equals("artist")) {
                data.add(current);
            }

        }
    }

    private class ampacheAlbumParser extends dataHandler {
        private Album current;

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("album")) {
                current = new Album();
                current.id = attr.getValue("id");
            }
        }

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

            super.endElement(namespaceURI, localName, qName);

            if (localName.equals("name")) {
                current.name = contents.toString();
            }

            if (localName.equals("artist")) {
                current.artist = contents.toString();
            }

            if (localName.equals("tracks")) {
                current.tracks = contents.toString();
            }

            if (localName.equals("year")) {
              current.year = contents.toString();
            }

            if (localName.equals("album")) {
                data.add(current);
            }
        }
    }

    private class ampacheTagParser extends dataHandler {
        private Tag current;

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("tag")) {
                current = new Tag();
                current.id = attr.getValue("id");
            }
        }

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

            super.endElement(namespaceURI, localName, qName);

            if (localName.equals("name")) {
                current.name = contents.toString();
            }
	    if (localName.equals("albums")) {
		current.albums = contents.toString();
	    }
	    if (localName.equals("artists")){
		current.artists = contents.toString();
	    }
            if (localName.equals("tag")) {
                data.add(current);
            }
        }
    }

    private class ampachePlaylistParser extends dataHandler {
        private Playlist current;

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("playlist")) {
                current = new Playlist();
                current.id = attr.getValue("id");
            }
        }

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

            super.endElement(namespaceURI, localName, qName);

            if (localName.equals("name")) {
                current.name = contents.toString();
            }

            if (localName.equals("owner")) {
                current.owner = contents.toString();
            }

            if (localName.equals("items")) {
                current.count = contents.toString();
            }

            if (localName.equals("playlist")) {
                data.add(current);
            }
        }
    }

    private class ampacheSongParser extends dataHandler {
        private Song current;

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("song")) {
                current = new Song();
                current.id = attr.getValue("id");
            } else if (localName.equals("album")) {
                current.albumId = attr.getValue("id");
            }
        }

        //TODO: Do we actually need this, or can we parse everything in startElement?
        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

            super.endElement(namespaceURI, localName, qName);

            if (localName.equals("song")) {
                data.add(current);
            }

            if (localName.equals("title")) {
                current.name = contents.toString();
            }

            if (localName.equals("artist")) {
                current.artist = contents.toString();
            }

            if (localName.equals("art")) {
                current.art = contents.toString();
            }

            if (localName.equals("url")) {
                current.url = contents.toString();
            }

            if (localName.equals("album")) {
                current.album = contents.toString();
            }

            if (localName.equals("genre")) {
                current.genre = contents.toString();
            }
        }
    }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String asHex(byte[] buf)
    {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i)
            {
                chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
                chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
            }
        return new String(chars);
    }
}
