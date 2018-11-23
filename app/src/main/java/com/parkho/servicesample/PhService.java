package com.parkho.servicesample;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;

public class PhService extends Service
{
    public interface MessengerMsg {
        int CONNECT = 1;
        int DISCONNECT = 2;
        int GET_TIMESTAMP = 3;
        int MUSIC_PLAY = 4;
        int MUSIC_STOP = 5;
    }

    // Activity 와 통신할 messenger
    private final Messenger mServiceMessenger = new Messenger(new ServiceHandler());
    private Messenger mActivityMessenger;

    private MediaPlayer mMediaPlayer;

    private long mStartTime;

    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message a_msg) {
            Message replyMsg;

            switch (a_msg.what) {
                case MessengerMsg.CONNECT:
                    mActivityMessenger = a_msg.replyTo;

                    replyMsg = Message.obtain(null, MessengerMsg.CONNECT);
                    try {
                        mActivityMessenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case MessengerMsg.DISCONNECT:
                    mActivityMessenger = null;
                    break;

                case MessengerMsg.GET_TIMESTAMP:
                    Bundle bundle = new Bundle();
                    bundle.putLong("parkho", getTime());
                    replyMsg = Message.obtain(null, MessengerMsg.GET_TIMESTAMP);
                    replyMsg.setData(bundle);
                    try {
                        mActivityMessenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    // 1초에 한 번씩 time 확인
                    sendEmptyMessageDelayed(MessengerMsg.GET_TIMESTAMP, 1000);
                    break;

                case MessengerMsg.MUSIC_PLAY:
                    onStartMusic();
                    break;

                case MessengerMsg.MUSIC_STOP:
                    onStopMusic();

                    removeCallbacksAndMessages(null);
                    break;

                default:
                    super.handleMessage(a_msg);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mStartTime = 0;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServiceMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private void onStartMusic() {
        mStartTime = SystemClock.elapsedRealtime();

        mMediaPlayer = MediaPlayer.create(this, R.raw.sample);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
    }

    private void onStopMusic() {
        mMediaPlayer.stop();
        mMediaPlayer.seekTo(0);
    }

    private long getTime() {
        return SystemClock.elapsedRealtime() - mStartTime;
    }
}