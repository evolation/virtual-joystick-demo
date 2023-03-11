package io.github.controlwear.joystickdemo;

import android.util.Log;

public class BackgroundHC05 implements Runnable {
    Thread backgroundThread;
    private int m_speed;
    private int m_steering;

    public void start() {
        if( backgroundThread == null ) {
            backgroundThread = new Thread( this );
            backgroundThread.start();
        }
    }

    public void stop() {
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
        byte[] frame= {(byte)205,(byte)171,0,0,0,0,0,0};
        frame[2] = (byte) (m_steering & 0xff);
        frame[3] = (byte) ((m_steering & 0xffff) >> 8);
        frame[4] = (byte) (m_speed & 0xff);
        frame[5] = (byte) ((m_speed & 0xffff) >> 8);
        short crc = (short)(frame[0]^frame[1]^frame[2]^frame[3]^frame[4]^frame[5]^frame[6]^frame[7]);
        frame[6] = (byte) (crc & 0xff);
        frame[7] = (byte) ((crc & 0xffff) >> 8);
        Log.i("TAG",bytesToHex(frame));
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