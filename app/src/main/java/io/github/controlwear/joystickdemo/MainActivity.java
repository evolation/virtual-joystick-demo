package io.github.controlwear.joystickdemo;

import static android.support.v4.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {


    private TextView mTextViewAngleLeft;
    private TextView mTextViewStrengthLeft;

    private TextView mTextViewAngleRight;
    private TextView mTextViewStrengthRight;
    private TextView mTextViewCoordinateRight;
    private BackgroundHC05 controller ;

    private BluetoothSocket m_btSocket;
    public BluetoothAdapter btAdapter;
    public BluetoothDevice btDevice;
    public BluetoothSocket btSocket;

    public static final String SERVICE_ID = "00001101-0000-1000-8000-00805f9b34fb"; //SPP UUID
    public static final String SERVICE_ADDRESS = "98:D3:61:F6:C8:FC"; // HC-05 BT ADDRESS


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDevice = btAdapter.getRemoteDevice(SERVICE_ADDRESS);
        if(btAdapter == null) {
            Log.e("TAG", "Bluetooth not available");
        } else {
            if(!btAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,3);
            } else {
                ConnectThread connectThread = new ConnectThread(btDevice);
                connectThread.start();
            }
        }


        controller = new BackgroundHC05();
//        controller.start(null);
        mTextViewAngleLeft = (TextView) findViewById(R.id.textView_angle_left);
        mTextViewStrengthLeft = (TextView) findViewById(R.id.textView_strength_left);

        JoystickView joystickLeft = (JoystickView) findViewById(R.id.joystickView_left);

        joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mTextViewAngleLeft.setText(angle + "°");
                mTextViewStrengthLeft.setText(strength + "%");
                int normalize_steering = 0;
                if (angle == 180){
                    normalize_steering = -strength * 10;
                }else{
                    normalize_steering = strength * 10;
                }
                controller.updateSteering(normalize_steering);
//                Log.i("TAG","Steering :"+normalize_steering);
            }
        });


        mTextViewAngleRight = (TextView) findViewById(R.id.textView_angle_right);
        mTextViewStrengthRight = (TextView) findViewById(R.id.textView_strength_right);
        mTextViewCoordinateRight = findViewById(R.id.textView_coordinate_right);

        final JoystickView joystickRight = (JoystickView) findViewById(R.id.joystickView_right);
        joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onMove(int angle, int strength) {

                int speed = 0;
                if (angle > 180){
                    speed = -(strength * 10);
                }
                else
                {
                    speed = (strength * 10);
                }
                controller.updateSpeed(speed);
//                Log.i("TAG","Speed :"+ speed);

                mTextViewAngleRight.setText(angle + "°");
                mTextViewStrengthRight.setText(strength + "%");

            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        controller.stop();
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket thisSocket;
        private final BluetoothDevice thisDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            thisDevice = device;

            try {
                tmp = thisDevice.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_ID));
            } catch (IOException e) {
                Log.e("TEST", "Can't connect to service");
            }
            thisSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            btAdapter.cancelDiscovery();

            try {
                thisSocket.connect();
                Log.d("TESTING", "Connected to shit");
            } catch (IOException connectException) {
                try {
                    thisSocket.close();
                } catch (IOException closeException) {
                    Log.e("TEST", "Can't close socket");
                }
                return;
            }

            btSocket = thisSocket;
            controller.start(btSocket);
        }
        public void cancel() {
            try {
                controller.stop();
                thisSocket.close();
            } catch (IOException e) {
                Log.e("TEST", "Can't close socket");
            }
        }
    }
}