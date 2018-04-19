package com.example.szymi44.bluetoothapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by Szymi44 on 2018-03-20.
 */

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "BluetoothApp";

    private static final UUID MY_UUID_INSCEURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private Handler handler;

    private final BluetoothAdapter mBluetoothAdapter;
    Context context;

    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectionThread;
    private ConnectedThread mConnectedThread;

    private BluetoothDevice mBluetoothDevice;
    private UUID deviceUUID;
    private ProgressDialog progressDialog;


    public BluetoothConnectionService(Context context, Handler handler) {
        this.context = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = handler;
        start();
    }

    //This thread runs while listening for incoming connections.
    //It behaves like a server-side client. It runs until a connection is
    //accepted (or until cancelled).
    private class AcceptThread extends Thread {
        // the local server socket
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            //Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSCEURE);
                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSCEURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mServerSocket = tmp;

        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;
            //This is a blocking call and will only return on a
            //successful connection or an exception

            try {
                Log.d(TAG, "run: REFCOM server socket start");
                if (mServerSocket != null)
                    socket = mServerSocket.accept();
                Log.d(TAG, "run: REFCON server socket accepted connecton");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (socket != null) {
                connected(socket, mBluetoothDevice);
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread");
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed" + e.getMessage());
            }
        }
    }

    public void putNewHandler(Handler handler) {
        this.handler = handler;
    }

    //This thread runs while attempting to make an outgoing connection
    //with a device It runs straight through; the connection either
    //succeeds or fails

    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            BluetoothSocket tmp = null;
            Log.d(TAG, "ConnectedThread started");
            mBluetoothDevice = device;
            deviceUUID = uuid;

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID");
                tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommmSocket " + e.getMessage());
            }
            mSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "RUN mConnectThread");

            //Get a BluetoothSocket for a connection with the given
            //BluetoothDevice


            mBluetoothAdapter.cancelDiscovery(); //otherwise it will slow connection

            //This is a blocking call and will only return on a
            //successful connection or an exception
            try {
                mSocket.connect();
                Log.d(TAG, "ConnectThread: Connected");

            } catch (IOException e) {
                //Close the socket
                try {
                    mSocket.close();
                    Log.d(TAG, "run: Closed Socket");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectionThread: Could not connect to UUID " + MY_UUID_INSCEURE);
            }
            connected(mSocket, mBluetoothDevice);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: closing Client Socket.");
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mSocket is Connectthread failed " + e.getMessage());
            }
        }
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            mConnectionThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    /**
     * AcceptThread starts and sits waiting for a connection \.
     * Then ConntectThread starts and attemps to make a connection
     * with other devices AcceptThread
     *
     * @param device
     * @param uuid
     */
    public synchronized void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: Started");
        progressDialog = ProgressDialog.show(context, "Connecting Bluetooth", "Plase wait...", true);
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            mConnectionThread = null;
        }

        if (device != null) {
            mConnectionThread = new ConnectThread(device, uuid);
            mConnectionThread.start();
        }
    }

    public synchronized void stop() {
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            mConnectionThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            //dismiss the progressdialog when connection is established
            try {
                progressDialog.dismiss();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


            Intent intent = new Intent("connect");
            intent.putExtra("Connect", "Connect");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024]; //buffer store for the stream
            int bytes; //bytes returned from read()

            //keep listening to the inputStream until an exception occurs
            while (true) {
                //read from the inputStream
                try {
                    bytes = mInputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream " + incomingMessage);

                    //EDIT
                    if (incomingMessage.equals("chosingDialogQuery") || incomingMessage.equals("Player 1 wants to start!") || incomingMessage.equals("Player 1 is a chicken!")
                            || incomingMessage.equals("Ready")) {
                        handler.obtainMessage(Constant.MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    } else if (isTwoDigits(incomingMessage)) {
                        handler.obtainMessage(Constant.MESSAGE_COORDINATE, bytes, -1, buffer).sendToTarget();
                    } else if (incomingMessage.equals("hit") || incomingMessage.equals("miss")) {
                        handler.obtainMessage(Constant.MESSAGE_RESPOND, bytes, -1, buffer).sendToTarget();
                    } else if (incomingMessage.equals("gameOver")) {
                        handler.obtainMessage(Constant.MESSAGE_GAME_OVER, bytes, -1, buffer).sendToTarget();
                    } else {
                        Intent incomingMassageIntent = new Intent("incomingMassage");
                        incomingMassageIntent.putExtra("TheMessage", incomingMessage);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(incomingMassageIntent);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "read: Error reading to inputstream " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputStream" + text);
            try {
                mOutputStream.write(bytes);
            } catch (IOException e) {
                Log.d(TAG, "Write: Error writing to outputstream " + e.getMessage());
            }
        }

        //Call this from the main activity to shutdown the connection
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void connected(BluetoothSocket mSocket, BluetoothDevice mBluetoothDevice) {
        Log.d(TAG, "connected: Starting");
        //start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mSocket);
        mConnectedThread.start();

        Message msg = handler.obtainMessage(Constant.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.DEVICE_NAME, "zzz");
        msg.setData(bundle);
        handler.sendMessage(msg);

    }

    public void write(byte[] out) {
        //Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called");
        //perform write
        mConnectedThread.write(out);
    }

    public boolean isTwoDigits(String s) {
        return s.matches("[0-9]{2}");
    }
}
