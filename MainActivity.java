package com.example.szymi44.bluetoothapp;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import java.util.UUID;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    private static final UUID MY_UUID_INSCEURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public static UUID getMyUuidInsceure() {
        return MY_UUID_INSCEURE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectionFragment connectionFragment = new ConnectionFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frag_container, connectionFragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
    }


}
