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

import com.nullsink.domob.prefsActivity;
import android.preference.Preference;
import android.test.ActivityInstrumentationTestCase2;

public class PrefsActivityTest extends
    ActivityInstrumentationTestCase2<prefsActivity> {

  private prefsActivity mActivity;
  private Preference mServerUrlPreference;
  private Preference mServerUsernamePreference;
  private Preference mServerPasswordPreference;

  public PrefsActivityTest() {
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

