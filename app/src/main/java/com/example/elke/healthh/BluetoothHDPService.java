package com.example.elke.healthh;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothHealthAppConfiguration;
import android.bluetooth.BluetoothHealthCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;





/**
 * This Service encapsulates Bluetooth Health API to establish, manage, and
 * disconnect communication between the Android device and a Bluetooth
 * HDP-enabled device. Possible HDP device type includes blood pressure monitor,
 * glucose meter, thermometer, etc.
 *
 * As outlined in the <a href=
 * "http://developer.android.com/reference/android/bluetooth/BluetoothHealth.html"
 * >BluetoothHealth</a> documentation, the steps involve: 1. Get a reference to
 * the BluetoothHealth proxy object. 2. Create a BluetoothHealth callback and
 * register an application configuration that acts as a Health SINK. 3.
 * Establish connection to a health device. Some devices will initiate the
 * connection. It is unnecessary to carry out this step for those devices. 4.
 * When connected successfully, read / write to the health device using the file
 * descriptor. The received data needs to be interpreted using a health manager
 * which implements the IEEE 11073-xxxxx specifications. 5. When done, close the
 * health channel and unregister the application. The channel will also close
 * when there is extended inactivity.
 */
public class BluetoothHDPService extends Service {
    private static final String TAG = "BluetoothHDPService";
    private int count;
    private byte[] invoke = {0x00, 0x00};
    public static final int RESULT_OK = 0;
    public static final int RESULT_FAIL = -1;
    private TextView syst;
    private TextView dias;
    private TextView pulser;
    // Status codes sent back to the UI client.
    // Application registration complete.
    public static final int STATUS_HEALTH_APP_REG = 100;
    // Application unregistration complete.
    public static final int STATUS_HEALTH_APP_UNREG = 101;
    // Channel creation complete.
    public static final int STATUS_CREATE_CHANNEL = 102;
    // Channel destroy complete.
    public static final int STATUS_DESTROY_CHANNEL = 103;
    // Reading data from Bluetooth HDP device.
    public static final int STATUS_READ_DATA = 104;
    // Done with reading data.
    public static final int STATUS_READ_DATA_DONE = 105;
    public static final int GET_SYS = 106;
    public static final int GET_DIAS = 107;
    public static final int GET_PULSE = 108;

    // Message codes received from the UI client.
    // Register client with this service.
    public static final int MSG_REG_CLIENT = 200;
    // Unregister client from this service.
    public static final int MSG_UNREG_CLIENT = 201;
    // Register health application.
    public static final int MSG_REG_HEALTH_APP = 300;
    // Unregister health application.
    public static final int MSG_UNREG_HEALTH_APP = 301;
    // Connect channel.
    public static final int MSG_CONNECT_CHANNEL = 400;
    // Disconnect channel.
    public static final int MSG_DISCONNECT_CHANNEL = 401;

    private BluetoothHealthAppConfiguration mHealthAppConfig;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothHealth mBluetoothHealth;
    private BluetoothDevice mDevice;
    private int mChannelId;

    private Messenger mClient;

    // Handles events sent by {@link HealthHDPActivity}.
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Register UI client to this service so the client can receive
                // messages.
                case MSG_REG_CLIENT:
                    Log.d(TAG, "Activity client registered");
                    mClient = msg.replyTo;
                    break;
                // Unregister UI client from this service.
                case MSG_UNREG_CLIENT:
                    mClient = null;
                    break;
                // Register health application.
                case MSG_REG_HEALTH_APP:
                    registerApp(msg.arg1);
                    break;
                // Unregister health application.
                case MSG_UNREG_HEALTH_APP:
                    unregisterApp();
                    break;
                // Connect channel.
                case MSG_CONNECT_CHANNEL:
                    mDevice = (BluetoothDevice) msg.obj;
                    connectChannel();
                    break;
                // Disconnect channel.
                case MSG_DISCONNECT_CHANNEL:
                    mDevice = (BluetoothDevice) msg.obj;
                    disconnectChannel();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Make sure Bluetooth and health profile are available on the Android
     * device. Stop service if they are not available.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Bluetooth adapter isn't available. The client of the service is
            // supposed to verify that it is available and activate before
            // invoking this service.
            stopSelf();
            return;
        }
        if (!mBluetoothAdapter.getProfileProxy(this, mBluetoothServiceListener,
                BluetoothProfile.HEALTH)) {
            Toast.makeText(this,
                    "Bluetooth health profile not available",
                    Toast.LENGTH_LONG);
            stopSelf();
            return;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BluetoothHDPService is running.");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    };

    // Register health application through the Bluetooth Health API.
    private void registerApp(int dataType) {
        mBluetoothHealth.registerSinkAppConfiguration(TAG, dataType,
                mHealthCallback);
    }

    // Unregister health application through the Bluetooth Health API.
    private void unregisterApp() {
        mBluetoothHealth.unregisterAppConfiguration(mHealthAppConfig);
    }

    // Connect channel through the Bluetooth Health API.
    private void connectChannel() {
        Log.i(TAG, "connectChannel()" );
        mBluetoothHealth.connectChannelToSource(mDevice, mHealthAppConfig);
    }

    // Disconnect channel through the Bluetooth Health API.
    private void disconnectChannel() {
        Log.i(TAG, "disconnectChannel()");
        mBluetoothHealth.disconnectChannel(mDevice, mHealthAppConfig,
                mChannelId);
    }

    // Callbacks to handle connection set up and disconnection clean up.
    private final BluetoothProfile.ServiceListener mBluetoothServiceListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = (BluetoothHealth) proxy;
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "onServiceConnected to profile: " + profile);
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = null;
            }
        }
    };

    private final BluetoothHealthCallback mHealthCallback = new BluetoothHealthCallback() {
        // Callback to handle application registration and unregistration
        // events. The service
        // passes the status back to the UI client.
        public void onHealthAppConfigurationStatusChange(
                BluetoothHealthAppConfiguration config, int status) {
            if (status == BluetoothHealth.APP_CONFIG_REGISTRATION_FAILURE) {
                mHealthAppConfig = null;
                sendMessage(STATUS_HEALTH_APP_REG, RESULT_FAIL);
            } else if (status == BluetoothHealth.APP_CONFIG_REGISTRATION_SUCCESS) {
                mHealthAppConfig = config;
                sendMessage(STATUS_HEALTH_APP_REG, RESULT_OK);
            } else if (status == BluetoothHealth.APP_CONFIG_UNREGISTRATION_FAILURE
                    || status == BluetoothHealth.APP_CONFIG_UNREGISTRATION_SUCCESS) {
                sendMessage(
                        STATUS_HEALTH_APP_UNREG,
                        status == BluetoothHealth.APP_CONFIG_UNREGISTRATION_SUCCESS ? RESULT_OK
                                : RESULT_FAIL);
            }
        }

        // Callback to handle channel connection state changes.
        // Note that the logic of the state machine may need to be modified
        // based on the HDP device.
        // When the HDP device is connected, the received file descriptor is
        // passed to the
        // ReadThread to read the content.
        public void onHealthChannelStateChange(
                BluetoothHealthAppConfiguration config, BluetoothDevice device,
                int prevState, int newState, ParcelFileDescriptor fd,
                int channelId) {
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, String.format(
                        "prevState\t%d ----------> newState\t%d", prevState,
                        newState));
            if (prevState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED
                    && newState == BluetoothHealth.STATE_CHANNEL_CONNECTED) {
                if (config.equals(mHealthAppConfig)) {

                    mChannelId = channelId;
                    sendMessage(STATUS_CREATE_CHANNEL, RESULT_OK);
                    (new ReadThread(fd)).start();
                } else {
                    sendMessage(STATUS_CREATE_CHANNEL, RESULT_FAIL);
                }
            } else if (prevState == BluetoothHealth.STATE_CHANNEL_CONNECTING
                    && newState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED) {
                sendMessage(STATUS_CREATE_CHANNEL, RESULT_FAIL);
            } else if (newState == BluetoothHealth.STATE_CHANNEL_DISCONNECTED) {
                if (config.equals(mHealthAppConfig)) {
                    sendMessage(STATUS_DESTROY_CHANNEL, RESULT_OK);
                } else {
                    sendMessage(STATUS_DESTROY_CHANNEL, RESULT_FAIL);
                }
            }
        }
    };

    // Sends an update message to registered UI client.
    private void sendMessage(int what, int value) {
        if (mClient == null) {
            Log.d(TAG, "No clients registered.");
            return;
        }

        try {
            mClient.send(Message.obtain(null, what, value, 0));
        } catch (RemoteException e) {
            // Unable to reach client.
            e.printStackTrace();
        }
    }


    // Thread to read incoming data received from the HDP device.  This sample application merely
    // reads the raw byte from the incoming file descriptor.  The data should be interpreted using
    // a health manager which implements the IEEE 11073-xxxxx specifications.
    private class ReadThread extends Thread {
        private ParcelFileDescriptor mFd;

        public ReadThread(ParcelFileDescriptor fd) {
            super();
            mFd = fd;
        }

        @Override
        public void run() {
            Log.e("TEST", "Read Data 1");
            FileInputStream fis = new FileInputStream(mFd.getFileDescriptor());
            final byte data[] = new byte[500];
            Log.i(TAG, "Read Data 2");
            try {
                while(fis.read(data) > -1) {
                    // At this point, the application can pass the raw data to a parser that
                    // has implemented the IEEE 11073-xxxxx specifications.  Instead, this sample
                    // simply indicates that some data has been received.

                    Log.i(TAG, "INBOUND");
                   // String test = byte2hex(data);
                   // Log.i(TAG, test);
                    /** Log.i(TAG, "INBOUND BYTES");
                     for(int i = 0; i<data.length;i++){
                     Log.i(TAG, String.valueOf(data[i]));}**/

                    if (data[0] != (byte) 0x00)
                    {

                        if(data[0] == (byte) 0xE2){
                            Log.i(TAG, "E2 - Association Request");
                            count = 1;

                            (new WriteThread(mFd)).start();
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            count = 2;
                            (new WriteThread(mFd)).start();
                        }
                        else if (data[0] == (byte)0xE7){
                            Log.i(TAG, "E7 - Data Given");


                            if(data[3] != (byte)0xda){

                                invoke[0] = data[6];
                                invoke[1] = data[7];

                                Log.i(TAG, "E7 - Measurement results");
                                /** ByteBuffer sys = ByteBuffer.allocate(2);
                                 sys.order(ByteOrder.LITTLE_ENDIAN);
                                 sys.put(data[45]);
                                 sys.put(data[46]);
                                 short sysVal = sys.getShort(0);**/
                                Log.i(TAG, " Sys - "+ data[45]);
                                int sys = (int) data[45];
                                sendMessage(GET_SYS, data[45]);

                                /** ByteBuffer dia = ByteBuffer.allocate(2);
                                 dia.order(ByteOrder.LITTLE_ENDIAN);
                                 dia.put(data[47]);
                                 dia.put(data[48]);
                                 short diaVal = dia.getShort(0);**/
                                Log.i(TAG, " Dia - "+ data[47]);
                                sendMessage(GET_DIAS, data[47]);
                                /**  ByteBuffer pulse = ByteBuffer.allocate(2);
                                 pulse.order(ByteOrder.LITTLE_ENDIAN);
                                 pulse.put(data[62]);
                                 pulse.put(data[63]);
                                 float pulseVal = pulse.get(0);**/
                                Log.i(TAG, " Pulse - " + data[63]);
                                sendMessage(GET_PULSE, data[63]);
                                /** sendMessage(GET_DIAS, data[45]);
                                 sendMessage(GET_SYS, data[47]);
                                 sendMessage(GET_PULSE, data[63]);**/
                                /**  sendMessage(9920, sysVal);
                                 sendMessage(9921, (int)pulseVal);**/



                                /**   for(int i=0; i<(data.length-2); i++){
                                 ByteBuffer bb = ByteBuffer.allocate(2);
                                 bb.order(ByteOrder.LITTLE_ENDIAN);
                                 bb.put(data[i]);
                                 bb.put(data[i+1]);
                                 short shortVal = bb.getShort(0);
                                 Log.i(TAG, i+" Short Val - "+shortVal);

                                 }**/

                                count = 3;
                                //set invoke id so get correct response
                                (new WriteThread(mFd)).start();
                            }
                            else if(data[3] == (byte) 0x5A){

                                Log.i(TAG, "3d byte is 0x5A");
                                for(int i = 0; i<data.length;i++){
                                    Log.i(TAG, String.valueOf(data[i]));}
                                invoke[0] = data[6];
                                invoke[0] = data[7];
                                count = 3;
                                //set invoke id so get correct response
                                (new WriteThread(mFd)).start();
                            }
                            //parse data!!
                        }

                        else if (data[0] == (byte) 0xE4)
                        {
                            count = 4;
                            (new WriteThread(mFd)).start();
                        }
                        //zero out the data
                        for (int i = 0; i < data.length; i++){
                            data[i] = (byte) 0x00;
                        }
                    }
                    sendMessage(STATUS_READ_DATA, 0);
                }
            } catch(IOException ioe) {}
            if (mFd != null) {
                try {
                    mFd.close();
                } catch (IOException e) { /* Do nothing. */ }
            }
            sendMessage(STATUS_READ_DATA_DONE, 0);
        }
    }

    private class WriteThread extends Thread {
        private ParcelFileDescriptor mFd;

        public WriteThread(ParcelFileDescriptor fd) {
            super();
            mFd = fd;
        }

        @Override
        public void run() {
            FileOutputStream fos = new FileOutputStream(mFd.getFileDescriptor());
            final byte data_AR[] = new byte[] {         (byte) 0xE3, (byte) 0x00,
                    (byte) 0x00, (byte) 0x2C,
                    (byte) 0x00, (byte) 0x00,
                    (byte) 0x50, (byte) 0x79,
                    (byte) 0x00, (byte) 0x26,
                    (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x80, (byte) 0x00,
                    (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x08,  //bt add for phone, can be automate in the future
                    (byte) 0x88, (byte) 0x77, (byte) 0x66, (byte) 0x55, (byte) 0x44, (byte) 0x33, (byte) 0x22, (byte)0x11,
                    (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            final byte data_DR[] = new byte[] {         (byte) 0xE7, (byte) 0x00,
                    (byte) 0x00, (byte) 0x12,
                    (byte) 0x00, (byte) 0x10,
                    (byte) invoke[0], (byte) invoke[1],
                    (byte) 0x02, (byte) 0x01,
                    (byte) 0x00, (byte) 0x0A,
                    (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x0D, (byte) 0x1D,
                    (byte) 0x00, (byte) 0x00 };

            final byte get_MDS[] = new byte[] {         (byte) 0xE7, (byte) 0x00,
                    (byte) 0x00, (byte) 0x0E,
                    (byte) 0x00, (byte) 0x0C,
                    (byte) 0x12, (byte) 0x34,
                    (byte) 0x01, (byte) 0x03,
                    (byte) 0x00, (byte) 0x06,
                    (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00 };

            final byte data_RR[] = new byte[] {         (byte) 0xE5, (byte) 0x00,
                    (byte) 0x00, (byte) 0x02,
                    (byte) 0x00, (byte) 0x00 };

//            final byte data_RRQ[] = new byte[] {        (byte) 0xE4, (byte) 0x00,
//                                                        (byte) 0x00, (byte) 0x02,
//                                                        (byte) 0x00, (byte) 0x00 };
//
//            final byte data_ABORT[] = new byte[] {      (byte) 0xE6, (byte) 0x00,
//                                                        (byte) 0x00, (byte) 0x02,
//                                                        (byte) 0x00, (byte) 0x00 };
            try {
                Log.i(TAG, String.valueOf(count));
                if (count == 1)
                {
                    fos.write(data_AR);
                    Log.i(TAG, "Association Responded!");
                }
                else if (count == 2)
                {
                    fos.write(get_MDS);
                    Log.i(TAG, "Get MDS object attributes!");
                }
                else if (count == 3)
                {
                    fos.write(data_DR);
                    Log.i(TAG, "Data Responsed!");
                }
                else if (count == 4)
                {
                    fos.write(data_RR);
                    Log.i(TAG, "Data Released!");
                }
            } catch(IOException ioe) {}
        }

    }
}