package com.nullsink.domob.test;

import com.nullsink.domob.ampacheCommunicator;
import com.nullsink.domob.prefsActivity;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

public class AmpacheCommunicatorTest extends
    AndroidTestCase {

  private ampacheCommunicator mAmpacheCommunicator;
  private Context mCtxt;
  private Activity mActivity;
  private SharedPreferences mSharedPreferences;
  private static final String PASSWORD_PREFERENCE_KEY = "server_password_preference";
  private static final String USERNAME_PREFERENCE_KEY = "server_username_preference";
  private static final String URL_PREFERENCE_KEY = "server_url_preference";
  private static final String SERVER_USERNAME = "test";
  private static final String SERVER_PASSWORD = "test";
  private static final String SERVER_URL_HTTP = "http://localhost/ampache";
  private static final String SERVER_URL_NO_HTTP = "localhost/ampache";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    mCtxt = getContext();
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mCtxt);
  }

  public void testHttpUrl() {
    // First set all of the shared preference values
    SharedPreferences.Editor editor = mSharedPreferences.edit();
    editor.putString(USERNAME_PREFERENCE_KEY, SERVER_USERNAME);
    editor.putString(PASSWORD_PREFERENCE_KEY, SERVER_PASSWORD);
    editor.putString(URL_PREFERENCE_KEY, SERVER_URL_HTTP);
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

  public void testNoHttpUrl() {
    // First set all of the shared preference values
    SharedPreferences.Editor editor = mSharedPreferences.edit();
    editor.putString(USERNAME_PREFERENCE_KEY, SERVER_USERNAME);
    editor.putString(PASSWORD_PREFERENCE_KEY, SERVER_PASSWORD);
    editor.putString(URL_PREFERENCE_KEY, SERVER_URL_NO_HTTP);
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
