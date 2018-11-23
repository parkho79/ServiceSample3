package com.parkho.servicesample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.parkho.servicesample.PhService.MessengerMsg;

import java.util.concurrent.TimeUnit;

public class PhMainActivity extends AppCompatActivity
{
    // Service 와 통신할 messenger
    final Messenger mActivityMessenger = new Messenger(new ActivityHandler());
    private Messenger mBoundServiceMessenger;

    private TextView mTvTime;

   private class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message a_msg) {
            switch (a_msg.what) {
                case MessengerMsg.CONNECT:
                    Snackbar.make(findViewById(android.R.id.content), R.string.service_connected, Snackbar.LENGTH_LONG).show();
                    break;

                case MessengerMsg.GET_TIMESTAMP:
                    String strTime = convertTimeFormat(a_msg.getData().getLong("parkho"));
                    mTvTime.setText(strTime);
                    break;

                default:
                    super.handleMessage(a_msg);
            }
        }
    }


   // https://www.truiton.com/2015/01/android-bind-service-using-messenger/

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName a_name, IBinder a_service) {
            mBoundServiceMessenger = new Messenger(a_service);

            Message msg = Message.obtain(null, MessengerMsg.CONNECT);
            msg.replyTo = mActivityMessenger;
            try {
                mBoundServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName a_name) {
            mBoundServiceMessenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new OnClickListener() {
            public void onClick(View a_view) {
                Message msg = Message.obtain(null, MessengerMsg.MUSIC_PLAY);
                try {
                    mBoundServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                msg = Message.obtain(null, MessengerMsg.GET_TIMESTAMP);
                try {
                    mBoundServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnStop = findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new OnClickListener() {
            public void onClick(View a_view) {
                Message msg = Message.obtain(null, MessengerMsg.MUSIC_STOP);
                try {
                    mBoundServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        mTvTime = findViewById(R.id.tv_time);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 서비스 시작
        Intent intent = new Intent(this, PhService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Message msg = Message.obtain(null, MessengerMsg.DISCONNECT);
        try {
            mBoundServiceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (mBoundServiceMessenger != null) {
            unbindService(mServiceConnection);
            mBoundServiceMessenger = null;
        }
    }

    /**
     * Time format
     */
    private String convertTimeFormat(final long a_time) {
        final long hr = TimeUnit.MILLISECONDS.toHours(a_time);
        final long min = TimeUnit.MILLISECONDS.toMinutes(a_time - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(a_time - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final String strSec = String.format("%02d:%02d:%02d", hr, min, sec);
        return strSec;
    }
}