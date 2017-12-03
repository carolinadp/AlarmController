package edu.up.alarmcontroller;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by carolinadelgadillo on 12/2/17.
 */

class ActiveState {
    private static final ActiveState ourInstance = new ActiveState();
    private Handler mHandler = new Handler();
    private static int mInterval = 5000;
    private volatile boolean received = true;
    private volatile boolean active = false;
    private Context context = null;

    private Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            if (!received) {
                startAlarm();
            } else {
                received = false;
                BluetoothController.getInstance().sendMessage(Transmit.YOU_THERE);
            }
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };

    public void setContext(Context context){
        this.context = context;
    }

    public static ActiveState getInstance() {
        return ourInstance;
    }

    private ActiveState() {

    }

    public void activate(){
        mStatusChecker.run();
        BluetoothController.getInstance().beginListenForData();
        this.active = true;
    }

    public void deactivate() {
        mHandler.removeCallbacks(mStatusChecker);
        this.active = false;
        this.received = true;
    }

    public boolean isActive(){
        return this.active;
    }

    public void receive() {
        this.received = true;
    }

    public void startAlarm() {
        // TODO: start alarm
        msg("Alarm started");
        BluetoothController.getInstance().sendMessage(Transmit.ALARM_STARTED);
        mHandler.removeCallbacks(mStatusChecker);
        deactivate();
    }

    private void msg(String s)
    {
        Toast.makeText(context,s, Toast.LENGTH_LONG).show();
    }


}
