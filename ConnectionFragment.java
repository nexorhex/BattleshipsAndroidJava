package com.example.szymi44.bluetoothapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConnectionFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ConnectionFragment";
    public MainActivity mainActivity;
    BluetoothAdapter mBluetoothAdapter;
    private static BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBluetoothDevice;
    FragmentManager fragmentManager;
    BattleshipsFragment battleshipsFragment;

    Button btnDevice;
    Button btnSend;
    EditText etMessage;
    TextView chat;

    StringBuilder messages;
    List<BluetoothDevice> potentialPar;

    public ConnectionFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_connection, container, false);
        mainActivity = (MainActivity) getActivity();
        mBluetoothAdapter = mainActivity.getmBluetoothAdapter();


        potentialPar = new ArrayList<>();
        messages = new StringBuilder();

        Button btnGame = (Button) layout.findViewById(R.id.btnGame);
        btnGame.setOnClickListener(this);
        Button btnMakeDisc = (Button) layout.findViewById(R.id.btnMakeDisc);
        btnMakeDisc.setOnClickListener(this);
        btnDevice = (Button) layout.findViewById(R.id.btnDevice);
        btnDevice.setOnClickListener(this);
        Button btnStartConnection = (Button) layout.findViewById(R.id.btnConnect);
        btnStartConnection.setOnClickListener(this);
        btnSend = (Button) layout.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        etMessage = (EditText) layout.findViewById(R.id.etMassage);
        chat = (TextView) layout.findViewById(R.id.chat);


        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(messageReciever, new IntentFilter("incomingMassage"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(internalReciver, new IntentFilter("connect"));

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mainActivity.registerReceiver(mReciver2, filter1);

        return layout;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGame:
                onClickButtonStartGame(view);
                break;
            case R.id.btnDisc:
                onClickDiscover(view);
                break;
            case R.id.btnMakeDisc:
                onClickMakeDisc(view);
                break;
            case R.id.btnDevice:
                showDevices();
                break;
            case R.id.btnConnect:
                startConnection();
                break;
            case R.id.btnSend:
                byte[] bytes = etMessage.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);

                messages.append(Build.MODEL + ": " + etMessage.getText().toString() + "\n");
                //chat.setTextColor(Color.BLUE);
                chat.setText(messages);
                etMessage.setText("");
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mBluetoothConnection == null) {
            mBluetoothConnection = new BluetoothConnectionService(getActivity(), handler);
        }

        //showDevices();
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();

            switch (msg.what) {

                case Constant.MESSAGE_DEVICE_NAME:
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to", Toast.LENGTH_LONG).show();
                    }
                    break;
                case Constant.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chat.setText("Starting the game!");
                    if (readMessage.equals("chosingDialogQuery")) {
                        AlertDialog dialog = createDialog();
                        dialog.show();
                    } else if (readMessage.equals("Player 1 wants to start!")) {
                        AlertDialog dialog = createDialog2();
                        dialog.show();
                    } else if (readMessage.equals("Player 1 is a chicken!")) {
                        AlertDialog dialog = createDialog3();
                        dialog.show();
                    }
                    break;
            }

        }
    };

    public AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you want to start?").setTitle("The 2 player is ready!");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                sendMessage("Player 1 wants to start!");

                fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                battleshipsFragment = new BattleshipsFragment();
                ft.replace(R.id.frag_container, battleshipsFragment.newInstance("X"));
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                Toast.makeText(getActivity(), "Player 1 starting the game", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendMessage("Player 1 is a chicken!");
                Toast.makeText(getActivity(), "Convince him to play!", Toast.LENGTH_LONG).show();
            }
        });
        return builder.create();
    }

    public AlertDialog createDialog2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Still do you want to play?").setTitle("Player 1 ready to play");
        builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                battleshipsFragment = new BattleshipsFragment();
                ft.replace(R.id.frag_container, battleshipsFragment.newInstance("O"));
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                Toast.makeText(getActivity(), "Let's start the game!", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("No way!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity(), "He is cheaky beaky!", Toast.LENGTH_LONG).show();
            }
        });
        return builder.create();
    }

    public AlertDialog createDialog3() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Disgusting!").setTitle("Player 1 doesn't want to play");
        builder.setNeutralButton("Ok :(", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity(), "He is a coward!", Toast.LENGTH_LONG).show();
            }
        });
        return builder.create();
    }

    public void sendMessage(String msg) {
        mBluetoothConnection.write(msg.getBytes());
    }

    public void onClickButtonStartGame(View view) {
        BattleshipsFragment battleshipsFragment = new BattleshipsFragment();
        String text = "chosingDialogQuery";
        sendMessage(text);
        /*FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frag_container, battleshipsFragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();*/
    }

    public void onClickDiscover(View view) {
        if (mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "Discovery cancled");
            mBluetoothAdapter.cancelDiscovery();

            mBluetoothAdapter.startDiscovery();

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            mainActivity.registerReceiver(mReceiver, filter);
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            mainActivity.registerReceiver(mReceiver, filter);
        }
    }

    public void onClickMakeDisc(View view) {
        Intent intent = new Intent(mainActivity.getmBluetoothAdapter().ACTION_REQUEST_DISCOVERABLE);
        startActivity(intent);
    }


    public void showDevices() {
        Set<BluetoothDevice> pairedDevice = mainActivity.getmBluetoothAdapter().getBondedDevices();

        if (pairedDevice.size() > 0) {
            final BluetoothDevice blueDev[] = new BluetoothDevice[pairedDevice.size()];
            final String[] item = new String[blueDev.length];
            int i = 0;
            for (BluetoothDevice device : pairedDevice) {
                blueDev[i] = device;
                item[i] = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                i++;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setTitle("Devices");
            builder.setSingleChoiceItems(item, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    if (i >= 0 && i < blueDev.length) {

                        mBluetoothDevice = blueDev[i];
                        btnDevice.setText(blueDev[i].getName());
                        Log.d(TAG, mBluetoothDevice.getName());

                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            // mBluetoothConnection = new BluetoothConnectionService(mainActivity,handler);
        }
    }

    public void startConnection() {
        Log.d(TAG, mBluetoothDevice.getName());
        if (mBluetoothDevice != null)
            startBTConnection(mBluetoothDevice, mainActivity.getMyUuidInsceure());
    }

    //Starting chat service method
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection");
        if (device != null && uuid != null) {
            mBluetoothConnection.startClient(device, uuid);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action Found");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                potentialPar.add(device);
                Log.d(TAG, device.getName() + " " + device.getAddress());
            }
        }
    };

    private final BroadcastReceiver mReciver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    mBluetoothDevice = device;
                }
                if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDING) {

                }
                if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {

                }
            }
        }
    };

    BroadcastReceiver messageReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("TheMessage");
            messages.append(mBluetoothDevice.getName() + ": " + text + "\n");
            //chat.setTextColor(Color.RED);
            chat.setText(messages);

        }
    };
    BroadcastReceiver internalReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("Connect");
            chat.setText(messages); //do edycji
            Toast.makeText(mainActivity, text, Toast.LENGTH_LONG);//do edycji
            //chat.setTextColor(Color.BLUE);
        }
    };

    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReciver2);
        getActivity().unregisterReceiver(mReceiver);
        getActivity().unregisterReceiver(messageReciever);
        getActivity().unregisterReceiver(internalReciver);
    }

    static public BluetoothConnectionService getBluetoothService() {
        return mBluetoothConnection;
    }
}
