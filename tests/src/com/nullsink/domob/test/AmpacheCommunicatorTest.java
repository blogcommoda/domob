package com.nullsink.domob.test;

/* Copyright (c) 2013 Ed Baker <edward.david.baker@gmail.com>
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

import com.nullsink.domob.ampacheCommunicator;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

public class AmpacheCommunicatorTest extends
    AndroidTestCase {

  private ampacheCommunicator mAmpacheCommunicator;
  private Context mCtxt;
  private SharedPreferences mSharedPreferences;
  private static final String PASSWORD_PREFERENCE_KEY = "server_password_preference";
  private static final String USERNAME_PREFERENCE_KEY = "server_username_preference";
  private static final String URL_PREFERENCE_KEY = "server_url_preference";
  private static final String PROTOCOL_PREFERENCE_KEY = "server_protocol_preference";
  private static final String SERVER_USERNAME = "test";
  private static final String SERVER_PASSWORD = "test";
  private static final String[] SERVER_URLS = {"localhost/ampache",
                                               "http://localhost/ampache",
                                               "https://localhost/ampache"};
  private static final String[] SERVER_PROTOCOLS = {"http"};

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    mCtxt = getContext();
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mCtxt);
  }

  public void testFetchFromServer() {
    // Loop through all of the protocols
    for (int x = 0; x < SERVER_PROTOCOLS.length; x++) {
      // Loop through all of the URLs as well
      for (int y=0; y < SERVER_URLS.length; y++) {
        // First set all of the shared preference values
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(USERNAME_PREFERENCE_KEY, SERVER_USERNAME);
        editor.putString(PASSWORD_PREFERENCE_KEY, SERVER_PASSWORD);
        editor.putString(URL_PREFERENCE_KEY, SERVER_URLS[y]);
        editor.putString(PROTOCOL_PREFERENCE_KEY, SERVER_PROTOCOLS[x]);
        editor.commit();

        // With the preferences setup, create the communicator
        try {
          mAmpacheCommunicator = new ampacheCommunicator(mSharedPreferences, mCtxt);
        } catch (Exception e) {
          fail("Could not create ampacheCommunicator.");
        }

        // Check to see if constructing the URL throws an exception
        try {
          mAmpacheCommunicator.fetchFromServer("");
        } catch (Exception e) {
          fail(e.toString());
        }
      }
    }
  }

  public void testPerformAuthRequest() {
    // Loop through all of the protocols
    for (int x = 0; x < SERVER_PROTOCOLS.length; x++) {
      // Loop through all of the URLs as well
      for (int y=0; y < SERVER_URLS.length; y++) {
        // First set all of the shared preference values
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(USERNAME_PREFERENCE_KEY, SERVER_USERNAME);
        editor.putString(PASSWORD_PREFERENCE_KEY, SERVER_PASSWORD);
        editor.putString(URL_PREFERENCE_KEY, SERVER_URLS[y]);
        editor.putString(PROTOCOL_PREFERENCE_KEY, SERVER_PROTOCOLS[x]);
        editor.commit();

        // With the preferences setup, create the communicator
        try {
          mAmpacheCommunicator = new ampacheCommunicator(mSharedPreferences, mCtxt);
        } catch (Exception e) {
          fail("Could not create ampacheCommunicator.");
        }

        // Try to connect to the server
        try {
          mAmpacheCommunicator.perform_auth_request();
        } catch (Exception e) {
          fail("Could not perform_auth_request.");
        }

        // If the connection was successful, the authtoken will be set
        assertNotNull(mAmpacheCommunicator.authToken);
        assertNotSame(mAmpacheCommunicator.authToken, "");
      }
    }
  }
}
