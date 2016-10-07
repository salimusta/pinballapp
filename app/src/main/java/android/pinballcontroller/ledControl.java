package android.pinballcontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;

public class ledControl extends AppCompatActivity {

    Button btnRight, btnLeft;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = ledControl.class.getSimpleName();

    private int flippersData = 0;
    private int oldFlippersData = 0;

    ConnectBT bt = new ConnectBT() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(DevicesList.EXTRA_ADDRESS);

        //view of the ledControl layout
        setContentView(R.layout.activity_led_control);

        bt.execute();

        //call the widgtes
        btnLeft = (Button)findViewById(R.id.leftButton);
        btnRight = (Button)findViewById(R.id.rightButton);

        btnLeft.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //turnOnLed();
                    flippersData = flippersData & 0b10;
                    //Log.d(TAG, "LEFT UP"  + flippersData);
                    sendFlippersData();
                }else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //turnOffLed();
                    flippersData = flippersData | 0b01;
                    //Log.d(TAG, "LEFT DOWN"  + flippersData);
                    sendFlippersData();
                }

                return false;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //turnOnLed();
                    flippersData = flippersData & 0b01;
                    //Log.d(TAG, "RIGHT UP" + flippersData);
                    sendFlippersData();
                }else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //turnOffLed();
                    flippersData = flippersData | 0b10;
                    //Log.d(TAG, "RIGHT DOWN"  + flippersData);
                    sendFlippersData();
                }

                return false;
            }
        });
/*
        btnLeft.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "LED ON ");
                turnOnLed();      //method to turn on
            }
        });
*/
        /*
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "LED OFF ");
                turnOffLed();   //method to turn off
            }
        });*/
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }



    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void turnOffLed()
    {

        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".toString().getBytes());

                /*StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[2048];
                InputStream tmpIn = btSocket.getInputStream();
                if(tmpIn.available() > 0){
                    int readed = tmpIn.read(buffer);
                    for(int i = 0; i < readed-2;i++){
                        sb.append(String.format("%c", buffer[i]));
                    }
                    Log.d(TAG, "sb2=" + sb.toString());
                }*/


            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void sendFlippersData()
    {
        if (btSocket!=null)
        {
            try
            {
                Log.d(TAG, "SENDING "  + Integer.toString(flippersData));
                btSocket.getOutputStream().write(Integer.toString(flippersData).getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

}



