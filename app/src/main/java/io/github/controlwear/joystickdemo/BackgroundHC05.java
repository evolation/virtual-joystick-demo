package io.github.controlwear.joystickdemo;

import static android.graphics.Color.GREEN;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BackgroundHC05 implements Runnable {

    public BackgroundHC05(RelativeLayout p_layout) {
        this.layout = p_layout;
    }
    private RelativeLayout layout;
    Thread backgroundThread;
    private int m_speed;
    private int m_steering;

    private OutputStream m_outStream;

    public void start(BluetoothSocket p_btSocket) {
        if(m_outStream == null) {
            try {
                if (p_btSocket != null){
                    m_outStream = p_btSocket.getOutputStream();
                    layout.setBackgroundColor(GREEN);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if( backgroundThread == null ) {
            backgroundThread = new Thread( this );
            backgroundThread.start();
        }
    }

    public void stop() {
        if(m_outStream != null) {
            m_outStream = null;
        }
        if( backgroundThread != null ) {
            backgroundThread.interrupt();
        }
    }
    public void updateSpeed(int p_speed){
        m_speed = p_speed;
    }
    public void updateSteering(int p_steering){
        m_steering = p_steering;
    }
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    public void sendHC05Packet(){

        byte[] frame= {(byte)205,(byte)171,(byte)(m_steering & 0xff),(byte)((m_steering & 0xffff) >> 8),(byte)(m_speed & 0xff),(byte)((m_speed & 0xffff) >> 8),(byte)0,(byte)0};

        int frame_uint16 = BitUtils.uint16((byte)205,(byte)171);

        int steering_uint16 = BitUtils.uint16((byte)(m_steering & 0xff),(byte)((m_steering & 0xffff) >> 8));
        int speed_uint16 = BitUtils.uint16((byte)(m_speed & 0xff),(byte)((m_speed & 0xffff) >> 8));

        int crc = frame_uint16 ^ steering_uint16 ^ speed_uint16;
//        Log.i("crc :",""+crc);
        frame[6] = (byte) ((crc & 0xffff) >> 8);
        frame[7] = (byte) (crc & 0xff);

        Log.i("TAG",bytesToHex(frame));

//        byte[] frame= {(byte)205,(byte)171,(byte)00,(byte)00,(byte)255,(byte)00,(byte)50,(byte)171};
        if (m_outStream != null){
            try {
                m_outStream.write(frame);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void run() {
        try {
            Log.i("TAG","Thread starting.");
            while( !backgroundThread.interrupted() ) {
                sendHC05Packet();
                Thread.sleep(160);
            }
            Log.i("TAG","Thread stopping.");
        } catch( InterruptedException ex ) {
            // important you respond to the InterruptedException and stop processing
            // when its thrown!  Notice this is outside the while loop.
            Log.i("TAG","Thread shutting down as it was requested to stop.");
        } finally {
            backgroundThread = null;
        }
    }
}