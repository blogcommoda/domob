package com.nullsink.domob;

import android.app.Service;

import android.content.Intent;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.media.AudioManager;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;

import com.nullsink.domob.objects.*;
import com.nullsink.domob.IPlayerService;

public class playerService extends Service {
    //private IBinder mBinder;
    private static boolean mServiceInUse = false;
    private ArrayList<Song> playlistCurrent;
    private MediaPlayer mp;
    private static Boolean mResumeAfterCall;
    private WakeLock mWakeLock;   
    private int mServiceStartId = -1;

    //Handle phone calls
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int ringvolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                    if (ringvolume > 0) {
                        mResumeAfterCall = (mp.isPlaying() || mResumeAfterCall);
                        mp.pause();
                    }
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    // pause the music while a conversation is in progress
                    mResumeAfterCall = (mp.isPlaying() || mResumeAfterCall);
                    mp.pause();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    // start playing again
                    if (mResumeAfterCall) {
                        // resume playback only if music was playing
                        // when the call was answered
                        mp.start();
                        mResumeAfterCall = false;
                    }
                }
            }
        };


    @Override
    public IBinder onBind(Intent intent) {
        //mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        return mBinder;
    }
    
    @Override
    public void onRebind(Intent intent) {
        //mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mServiceInUse = false;
        
        //Dont shutdown if we're still playing or paused for a call
        if (isPlaying() || mResumeAfterCall) {
            return true;
        }
        
        //otherwise we stop now
        stopSelf(mServiceStartId);
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        mp = new MediaPlayer();
        
        TelephonyManager tmgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        tmgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakeLock.setReferenceCounted(false);

        //Delayed stop refresh
        //--
    }

    @Override
    public void onDestroy() {
        
        mp.release();
        mp = null;

        //Delayed stop shutdown
        //--
        
        TelephonyManager tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tmgr.listen(mPhoneStateListener, 0);

        // No longer need to prevent the phone from sleeping.
        mWakeLock.release();

        super.onDestroy();
    }
    
    private Handler mDelayedStopHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // Work on this later.
            }
        };

    public boolean isPlaying() {
        return mp.isPlaying();
    }
    
    
    //Functions exposed via AIDL
    private final IPlayerService.Stub mBinder = new IPlayerService.Stub() {
            public boolean isPlaying() {
                return isPlaying();
            }
            
            public void enqueue(List<Song> songs) {
            }
            
            public void playPause(){}
            public void next() {}
            public void prev() {}
            public void seek(int msec) {}
        };

}