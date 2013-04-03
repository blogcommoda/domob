package com.nullsink.domob.test;

import com.nullsink.domob.prefsActivity;

import android.preference.Preference;
import android.test.ActivityInstrumentationTestCase2;

public class prefsActivityTest extends
    ActivityInstrumentationTestCase2<prefsActivity> {

  private prefsActivity mActivity;
  private Preference mServerUrlPreference;
  private Preference mServerUsernamePreference;
  private Preference mServerPasswordPreference;

  public prefsActivityTest() {
    super("com.nullsink.domob", prefsActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    setActivityInitialTouchMode(false);

    mActivity = getActivity();

    mServerUrlPreference = mActivity.findPreference("server_url_preference");
    mServerUsernamePreference = mActivity.findPreference("server_username_preference");
    mServerPasswordPreference = mActivity.findPreference("server_password_preference");
  }

  public void testPreConditions() {
    // First check that all of the preference fields exist
    assertTrue(mServerUrlPreference != null);
    assertTrue(mServerUsernamePreference != null);
    assertTrue(mServerPasswordPreference != null);
  }
}

