/*
 * Copyright (C) 2009 The Android Open Source Project
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

package cz.monetplus.blueterm.terminal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import com.verifone.vmf.api.VMF;
import com.verifone.vmf.api.VMF.AppLinkListener;
import com.verifone.vmf.api.VMF.PrinterDataListener;

import cz.monetplus.blueterm.BTPrinterListener;
import cz.monetplus.blueterm.HandleMessages;
import cz.monetplus.blueterm.MessageThread;
import cz.monetplus.blueterm.SlipInputReader;
import cz.monetplus.blueterm.Vx600ConnectionListener;
import cz.monetplus.blueterm.util.MonetUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class TerminalServiceBT {
    // Debugging
    private static final String TAG = "TerminalService";
    // private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
    // UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
    // UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // public static final String NAME = "TERM_S_BT";

    // Member fields
    // private final BluetoothAdapter bluetoothAdapter;

    private Activity activity;

    private MessageThread messageThread;

    private ConnectThread mConnectThread;

    private ConnectedThread mConnectedThread;

    // private int currentTerminalState;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * 
     * @param context
     *            The UI Activity Context
     * @param handler
     *            A Handler to send messages back to the UI Activity
     */
    /**
     * @param context
     *            Application context.
     * @param messageThread
     *            Message thread with queue.
     * @param adapter
     *            Bluetooth adapter (only one for application).
     */
    public TerminalServiceBT(Activity activity, MessageThread messageThread) {
        // currentTerminalState = TerminalState.STATE_NONE;
        setState(TerminalState.STATE_NONE);
        this.messageThread = messageThread;
        this.activity = activity;
    }

    /**
     * Set the current state of the chat connection.
     * 
     * @param state
     *            An integer defining the current connection state.
     */
    private synchronized void setState(int state) {
        // Log.d(TAG, "setState() " + currentTerminalState + " -> " + state);
        // currentTerminalState = state;

        // Give the new state to the Handler so the UI Activity can update
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_STATE_CHANGE,
                    state, -1);
        }
    }

    /**
     * @return Return the current connection state.
     */
    public synchronized int getState() {
        if (messageThread != null) {
            return messageThread.getCurrentTerminalState();
        }

        return TerminalState.STATE_NONE;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        interrupt();

        setState(TerminalState.STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * 
     * @param device
     *            The BluetoothDevice to connect
     * @param secure
     *            Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(/* BluetoothDevice device, Boolean secure */) {
        // Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (messageThread.getCurrentTerminalState() == TerminalState.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.interrupt();
                try {
                    mConnectedThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.interrupt();
            try {
                mConnectedThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mConnectedThread = null;
        }

        setState(TerminalState.STATE_CONNECTING);

        while (getState() != TerminalState.STATE_CONNECTING) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                 e.printStackTrace();
            }
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(/* device, secure */);
        // mConnectThread = new AcceptThread(bluetoothAdapter, secure);
        mConnectThread.start();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection.
     * 
     * @param socket
     *            The BluetoothSocket on which the connection was made.
     * @param device
     *            The BluetoothDevice that has been connected.
     * @throws Exception
     */
    public synchronized void connected() throws Exception {
        Log.d(TAG, "connected");

        // interrupt();

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(messageThread, activity);
        mConnectedThread.start();
    }

//    public void join() {
//        Log.d(TAG, "join");
//
//        if (mConnectThread != null) {
//            try {
//                mConnectThread.join(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//
//        if (mConnectedThread != null) {
//            try {
//                mConnectedThread.join(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * Stop all threads.
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        // Kdyz zastavuju, tak uz nic nikam neposilej.
        setState(TerminalState.STATE_NONE);
        messageThread = null;

        interrupt();
    }

    private synchronized void interrupt() {
        if (mConnectThread != null) {

            // do {
//            try {
                mConnectThread.interrupt();
//                mConnectThread.join(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            // } while (mConnectThread.isAlive());

            mConnectThread = null;
        }

        if (mConnectedThread != null) {

            // do {
//            try {
                mConnectedThread.interrupt();
//                mConnectedThread.join(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            // } while (mConnectedThread.isAlive());
            mConnectedThread = null;
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner.
     * 
     * @param out
     *            The bytes to write
     * @throws IOException
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) throws IOException {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (messageThread.getCurrentTerminalState() != TerminalState.STATE_CONNECTED) {
                return;
            }
            r = mConnectedThread;
        }

        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed(Integer reasonCode, String reasonString) {
        // if (Looper.myLooper() != null && mHandler != null) {
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST, -1, -1,
                    "Unable to connect device");
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT, reasonCode,
                    0, reasonString);
        }

        // Start the service over to restart none mode
        TerminalServiceBT.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(Integer reasonCode, String reasonString) {

        // if (Looper.myLooper() != null && mHandler != null) {
        if (messageThread != null) {
            // Send a failure message back to the Activity
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST, -1, -1,
                    reasonString);
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT, reasonCode,
                    0, reasonString);
        }

        // Start the service over to restart none mode
        TerminalServiceBT.this.start();
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    public class ConnectThread extends Thread {
        public ConnectThread(/* BluetoothDevice device, boolean secure */) {
            interrupt();
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread:");
            setName("ConnectThread");

            // Reset the ConnectThread because we're done
            synchronized (TerminalServiceBT.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            try {
                connected();
            } catch (Exception e) {
                Log.e(TAG, "Connection error: " + e.getMessage());
            }
        }

        public void interrupt() {
            super.interrupt();
        }
    }
}
