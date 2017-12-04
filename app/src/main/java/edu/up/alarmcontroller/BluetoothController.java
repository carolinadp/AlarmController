package edu.up.alarmcontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by carolinadelgadillo on 12/3/17.
 */

public class BluetoothController extends AsyncTask<Void, Void, Void>  // UI thread
{
    private static BluetoothController instance = new BluetoothController();

    private boolean ConnectSuccess = true; //if it's here, it's almost connected
    private Context context = null;
    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    BluetoothAdapter myBluetooth = null;
    private boolean isBtConnected = false;
    String address = null;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    OutputStream outputStream;
    InputStream inputStream;

    private volatile boolean stopWorker;
    private int readBufferPosition;
    byte readBuffer[];
    private Thread workerThread;

    private BluetoothController(){
        super();
    }

    public static BluetoothController getInstance() {
        if (instance == null)
            instance = new BluetoothController();
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute()
    {
        progress = ProgressDialog.show(context, "Connecting...", "Please wait!!!");  //show a progress dialog
    }

    @Override
    protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
    {
        try
        {
            if (btSocket == null || !isBtConnected)
            {
                Intent newint = ((Activity)context).getIntent();
                address = newint.getStringExtra(DeviceListActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device
                myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection
                inputStream = btSocket.getInputStream();
                outputStream = btSocket.getOutputStream();
            }
        }
        catch (IOException e)
        {
            ConnectSuccess = false;//if the try failed, you can check the exception here
        }
        return null;
    }

    @Nullable
    public List<String> getInputLines(){
        final byte delimiter = 10;
        ArrayList<String > ans = new ArrayList<> ();
        try
        {
            int bytesAvailable = inputStream.available();
            if(bytesAvailable > 0)
            {
                byte[] packetBytes = new byte[bytesAvailable];
                inputStream.read(packetBytes);
                for(int i=0;i<bytesAvailable;i++)
                {
                    byte b = packetBytes[i];
                    if(b == delimiter)
                    {
                        byte[] encodedBytes = new byte[readBufferPosition];
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                        final String data = new String(encodedBytes, "US-ASCII");
                        readBufferPosition = 0;
                        ans.add(data);
                    }
                    else
                    {
                        readBuffer[readBufferPosition++] = b;
                    }
                }
            }
        }
        catch (IOException ex)
        {
        }
        return ans;
    }

    public void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if (data.equals(Receive.IM_HERE)) {
                                                ActiveState.getInstance().receive();
                                            } else if (data.equals(Receive.START_ALARM)) {
                                                ActiveState.getInstance().startAlarm();
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(context,s, Toast.LENGTH_LONG).show();
    }

    public void sendMessage(String message)
    {
        if (btSocket!=null)
        {
            try
            {
                outputStream.write(message.getBytes());
            }
            catch (IOException e)
            {
                msg("Error " + e.getMessage());
            }
        }
    }

    @Override
    protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
    {
        super.onPostExecute(result);

        if (!ConnectSuccess)
        {
            msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
            instance = null;
            ((Activity) context).finish();
        }
        else
        {
            msg("Connected.");
            isBtConnected = true;
        }
        progress.dismiss();
    }

    public void Disconnect()
    {
        ActiveState.getInstance().deactivate();
        instance.stopWorker = true;
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                inputStream.close();
                outputStream.close();
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error " + e.getMessage());}
        }
        instance = null;
        ((Activity) context).finish(); //return to the first layout

    }
}
