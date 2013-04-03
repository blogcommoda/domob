package com.nullsink.domob.test;

import com.nullsink.domob.prefsActivity;

import android.test.ActivityInstrumentationTestCase2;

public class prefsActivityTest extends
    ActivityInstrumentationTestCase2<prefsActivity> {

  private prefsActivity mActivity;

  public prefsActivityTest() {
    super("com.nullsink.domob", prefsActivity.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    setActivityInitialTouchMode(false);

    mActivity = getActivity();
  }
}

