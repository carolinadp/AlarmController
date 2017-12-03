package edu.up.alarmcontroller;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.UUID;


public class AlarmControlActivity extends AppCompatActivity {

    // Button btnOn, btnOff, btnDis;
    ImageButton On, Off;
    Button Discnt;
    ToggleButton active;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        //view of the ledControl
        setContentView(R.layout.activity_alarm_control);

        //call the widgets
        //On = (ImageButton)findViewById(R.id.on);
        //Off = (ImageButton)findViewById(R.id.off);
        Discnt = (Button)findViewById(R.id.disconnect);
        active = (ToggleButton)findViewById(R.id.active);

        ActiveState.getInstance().setContext(this);

        BluetoothController.getInstance().setContext(this);
        BluetoothController.getInstance().execute(); //Call the class to connect



        Discnt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                BluetoothController.getInstance().Disconnect(); //close connection
            }
        });

        active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    ActiveState.getInstance().activate();
                } else {
                    ActiveState.getInstance().deactivate();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_led_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




}
