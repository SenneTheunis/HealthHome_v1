package com.example.elke.healthh;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



public class upload extends AppCompatActivity{

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


    /**
     * Main user interface for the Sample application. All Bluetooth health-related
     * operations happen in {@link BluetoothHDPService}. This activity passes
     * messages to and from the service.
     */
        private static final String TAG = "BluetoothHealthActivity";

        // Use the appropriate IEEE 11073 data types based on the devices used.
        // Below are some examples. Refer to relevant Bluetooth HDP specifications
        // for detail.
        // 0x1007 - blood pressure meter
        // 0x1008 - body thermometer
        // 0x100F - body weight scale
        private static final int HEALTH_PROFILE_SOURCE_DATA_TYPE = 0x1007;

        private static final int REQUEST_ENABLE_BT = 1;

        private TextView mConnectIndicator;
        private ImageView mDataIndicator;
        private TextView mStatusMessage;
        private TextView dias;
        private TextView syst;
        private TextView pulser;
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothDevice[] mAllBondedDevices;
        private BluetoothDevice mDevice;
        private int mDeviceIndex = 0;
        private Resources mRes;
        private Messenger mHealthService;
        private boolean mHealthServiceBound;

        // Handles events sent by {@link HealthHDPService}.
        private Handler mIncomingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // Application registration complete.
                    case BluetoothService.STATUS_HEALTH_APP_REG:
                        mStatusMessage.setText(String.format(
                                "App registration status : %d", msg.arg1));
                        break;
                    // Application unregistration complete.
                    case BluetoothService.STATUS_HEALTH_APP_UNREG:
                        mStatusMessage.setText(String.format(
                                "App unregistration status : %d", msg.arg1));
                        break;
                    // Reading data from HDP device.
                    case BluetoothService.STATUS_READ_DATA:
                        mStatusMessage.setText("Reading data...");
                        mDataIndicator.setImageLevel(1);
                        break;
                    // Finish reading data from HDP device.
                    case BluetoothService.STATUS_READ_DATA_DONE:
                        mStatusMessage.setText("Done with reading data...");

                        mDataIndicator.setImageLevel(0);
                        break;
                    // Channel creation complete. Some devices will automatically
                    // establish
                    // connection.
                    case BluetoothService.STATUS_CREATE_CHANNEL:
                        mStatusMessage.setText(String.format(
                               "Create channel status: %d",
                                msg.arg1));
                        mConnectIndicator.setText("CONNECTED");
                        break;
                    // Channel destroy complete. This happens when either the device
                    // disconnects or
                    // there is extended inactivity.
                    case BluetoothService.STATUS_DESTROY_CHANNEL:
                        mStatusMessage.setText(String.format(
                                "Destroy channel status: %d",
                                msg.arg1));
                        mConnectIndicator.setText("DISCONNECTED");
                        break;
                    case BluetoothService.GET_SYS:
                        syst.setText(String.format("Systolic pressure is : %d", msg.arg1));
                        break;
                    case BluetoothService.GET_DIAS:
                        dias.setText(String.format("Diastolic pressure is : %d", msg.arg1));
                        break;
                    case BluetoothService.GET_PULSE:
                        pulser.setText(String.format("Pulserate is : %d", msg.arg1));
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };

        private final Messenger mMessenger = new Messenger(mIncomingHandler);

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle("CONNECT");
            setContentView(R.layout.activity_upload);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Connect");
            setSupportActionBar(toolbar);

            // Check for Bluetooth availability on the Android platform.
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available",
                        Toast.LENGTH_LONG);
                finish();
                return;
            }
            setContentView(R.layout.activity_upload);
            mConnectIndicator = (TextView) findViewById(R.id.connect_ind);
            mStatusMessage = (TextView) findViewById(R.id.status_msg);
            mDataIndicator = (ImageView) findViewById(R.id.data_ind);
            syst = (TextView) findViewById(R.id.syst);
            dias = (TextView) findViewById(R.id.dias);
            pulser = (TextView) findViewById(R.id.pulser);
            mRes = getResources();
            mHealthServiceBound = false;


            // Initiates application registration through {@link
            // BluetoothService}.


            // Initiates application unregistration through {@link
            // BluetoothService}.


            // Initiates channel creation through {@link BluetoothService}. Some
            // devices will
            // initiate the channel connection, in which case, it is not necessary
            // to do this in the
            // application. When pressed, the user is asked to select from one of
            // the bonded devices
            // to connect to.
            Button connectButton = (Button) findViewById(R.id.button_connect_channel);
            connectButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    sendMessage(BluetoothService.MSG_REG_HEALTH_APP,
                            HEALTH_PROFILE_SOURCE_DATA_TYPE);
                    mAllBondedDevices = (BluetoothDevice[]) mBluetoothAdapter
                            .getBondedDevices().toArray(new BluetoothDevice[0]);

                    if (mAllBondedDevices.length > 0) {
                        int deviceCount = mAllBondedDevices.length;
                        if (mDeviceIndex < deviceCount)
                            mDevice = mAllBondedDevices[mDeviceIndex];
                        else {
                            mDeviceIndex = 0;
                            mDevice = mAllBondedDevices[0];
                        }
                        String[] deviceNames = new String[deviceCount];
                        int i = 0;
                        for (BluetoothDevice device : mAllBondedDevices) {
                            deviceNames[i++] = device.getName();
                        }
                        SelectDeviceDialogFragment deviceDialog = SelectDeviceDialogFragment
                                .newInstance(deviceNames, mDeviceIndex);
                        deviceDialog.show(getFragmentManager(), "deviceDialog");
                    }
                }
            });


        }

        // Sets up communication with {@link BluetoothService}.
        private ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                mHealthServiceBound = true;
                Message msg = Message.obtain(null,
                        BluetoothService.MSG_REG_CLIENT);
                msg.replyTo = mMessenger;
                mHealthService = new Messenger(service);
                try {
                    mHealthService.send(msg);
                } catch (RemoteException e) {
                    Log.w(TAG, "Unable to register client to service.");
                    e.printStackTrace();
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                mHealthService = null;
                mHealthServiceBound = false;
            }
        };

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (mHealthServiceBound)
                unbindService(mConnection);
//            unregisterReceiver(mReceiver);
            //disconnectChannel();
            //sendMessageWithDevice(BluetoothService.MSG_UNREG_HEALTH_APP);
            //sendMessageWithDevice(BluetoothService.MSG_UNREG_CLIENT);
        }

        @Override
        protected void onStart() {
            super.onStart();
            // If Bluetooth is not on, request that it be enabled.
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                initialize();
            }
        }

        /**
         * Ensures user has turned on Bluetooth on the Android device.
         */
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_ENABLE_BT:
                    if (resultCode == Activity.RESULT_OK) {
                        initialize();
                    } else {
                        finish();
                        return;
                    }
            }
        }

        /**
         * Used by {@link SelectDeviceDialogFragment} to record the bonded Bluetooth
         * device selected by the user.
         *
         * @param position
         *            Position of the bonded Bluetooth device in the array.
         */
        public void setDevice(int position) {
            mDevice = this.mAllBondedDevices[position];
            mDeviceIndex = position;
        }

        private void connectChannel() {
            sendMessageWithDevice(BluetoothService.MSG_CONNECT_CHANNEL);
        }

        private void disconnectChannel() {
            sendMessageWithDevice(BluetoothService.MSG_DISCONNECT_CHANNEL);
        }

        private void initialize() {
            // Starts health service.
            Intent intent = new Intent(this, BluetoothService.class);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        // Intent filter and broadcast receive to handle Bluetooth on event.
        private IntentFilter initIntentFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            return filter;
        }

        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR) == BluetoothAdapter.STATE_ON) {
                        initialize();
                    }
                }
            }
        };

        // Sends a message to {@link BluetoothService}.
        private void sendMessage(int what, int value) {
            if (mHealthService == null) {
                Log.d(TAG, "Health Service not connected.");
                return;
            }

            try {
                mHealthService.send(Message.obtain(null, what, value, 0));
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to reach service.");
                e.printStackTrace();
            }
        }

        // Sends an update message, along with an HDP BluetoothDevice object, to
        // {@link BluetoothService}. The BluetoothDevice object is needed by the
        // channel creation
        // method.
        private void sendMessageWithDevice(int what) {
            if (mHealthService == null) {
                Log.d(TAG, "Health Service not connected.");
                return;
            }

            try {
                mHealthService.send(Message.obtain(null, what, mDevice));
            } catch (RemoteException e) {
                Log.w(TAG, "Unable to reach service.");
                e.printStackTrace();
            }
        }

        /**
         * Dialog to display a list of bonded Bluetooth devices for user to select
         * from. This is needed only for channel connection initiated from the
         * application.
         */
        public static class SelectDeviceDialogFragment extends DialogFragment {

            public static SelectDeviceDialogFragment newInstance(String[] names,
                                                                 int position) {
                SelectDeviceDialogFragment frag = new SelectDeviceDialogFragment();
                Bundle args = new Bundle();
                args.putStringArray("names", names);
                args.putInt("position", position);
                frag.setArguments(args);
                return frag;
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                String[] deviceNames = getArguments().getStringArray("names");
                int position = getArguments().getInt("position", -1);
                if (position == -1)
                    position = 0;
                return new AlertDialog.Builder(getActivity())
                        .setTitle("Select a device")
                        .setPositiveButton("Okay",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        ((upload) getActivity())
                                                .connectChannel();
                                    }
                                })
                        .setSingleChoiceItems(deviceNames, position,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        ((upload) getActivity())
                                                .setDevice(which);
                                    }
                                }).create();
            }
        }


    }





