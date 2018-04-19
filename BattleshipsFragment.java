package com.example.szymi44.bluetoothapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;


public class BattleshipsFragment extends Fragment {

    public BluetoothConnectionService bluetoothConnectionService;

    private static final int NUMBER_OF_SHIPS = 5;
    public static String MARK_CHOSEN = "MARK_CHOSEN";
    private int tempShips = NUMBER_OF_SHIPS;
    private int existShip;
    private AlertDialog dialog;

    private int row;
    private int col;

    public String turn = "X";
    private String myMark;

    ImageButton[][] shipButtons = new ImageButton[5][5];
    ImageButton shipButton00;
    ImageButton shipButton01;
    ImageButton shipButton02;
    ImageButton shipButton03;
    ImageButton shipButton04;
    ImageButton shipButton10;
    ImageButton shipButton11;
    ImageButton shipButton12;
    ImageButton shipButton13;
    ImageButton shipButton14;
    ImageButton shipButton20;
    ImageButton shipButton21;
    ImageButton shipButton22;
    ImageButton shipButton23;
    ImageButton shipButton24;
    ImageButton shipButton30;
    ImageButton shipButton31;
    ImageButton shipButton32;
    ImageButton shipButton33;
    ImageButton shipButton34;
    ImageButton shipButton40;
    ImageButton shipButton41;
    ImageButton shipButton42;
    ImageButton shipButton43;
    ImageButton shipButton44;

    ImageButton[][] shotButtons = new ImageButton[5][5];
    ImageButton shotButton00;
    ImageButton shotButton01;
    ImageButton shotButton02;
    ImageButton shotButton03;
    ImageButton shotButton04;
    ImageButton shotButton10;
    ImageButton shotButton11;
    ImageButton shotButton12;
    ImageButton shotButton13;
    ImageButton shotButton14;
    ImageButton shotButton20;
    ImageButton shotButton21;
    ImageButton shotButton22;
    ImageButton shotButton23;
    ImageButton shotButton24;
    ImageButton shotButton30;
    ImageButton shotButton31;
    ImageButton shotButton32;
    ImageButton shotButton33;
    ImageButton shotButton34;
    ImageButton shotButton40;
    ImageButton shotButton41;
    ImageButton shotButton42;
    ImageButton shotButton43;
    ImageButton shotButton44;

    boolean imReady, youReady;
    boolean[][] myShipsPositions = new boolean[5][5];

    public BattleshipsFragment() {
    }

    public static final BattleshipsFragment newInstance(String mark) {
        BattleshipsFragment game = new BattleshipsFragment();
        Bundle bdl = new Bundle(1);
        bdl.putString(MARK_CHOSEN, mark);

        game.setArguments(bdl);
        return game;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myMark = getArguments().getString(MARK_CHOSEN);

        bluetoothConnectionService = ConnectionFragment.getBluetoothService();
        bluetoothConnectionService.putNewHandler(handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battleships, container, false);

        existShip = 0;
        imReady = false;
        youReady = false;
        initButtons(view);
        buttonsToArray();
        disableShotField();

        for (int i = 0; i < shipButtons.length; i++) {
            for (int j = 0; j < shipButtons[1].length; j++) {
                shipButtons[i][j].setBackgroundColor(Color.BLUE);
            }
        }
        //Listeners for shipButtons
        for (int i = 0; i < shipButtons.length; i++) {
            for (int j = 0; j < shipButtons[1].length; j++) {

                String row = String.valueOf(i);
                String col = String.valueOf(j);
                final String rowcol = row + col;

                shipButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tempShips != 0) {
                            handleShipField(rowcol);

                        } else {
                            disableShipsField();
                            enableShotField();
                            imReady = true;
                            Toast.makeText(getActivity().getApplicationContext(), imReady + "   " + youReady, Toast.LENGTH_LONG).show();
                            String text = "Ready";
                            //bluetoothConnectionService.write(text.getBytes());
                            sendMessage(text);
                        }
                    }
                });
            }
        }

        //Listeners for shotButtons
        for (int i = 0; i < shotButtons.length; i++) {
            for (int j = 0; j < shotButtons[1].length; j++) {

                String row = String.valueOf(i);
                String col = String.valueOf(j);
                final String rowcol = row + col;

                shotButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        handleShotField(rowcol);
                    }
                });
            }
        }
        return view;
    }

    public void sendMessage(String msg) {
        bluetoothConnectionService.write(msg.getBytes());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothConnectionService != null)
            bluetoothConnectionService.stop();
    }


    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();
            byte[] readBuf = (byte[]) msg.obj;

            switch (msg.what) {
                case Constant.MESSAGE_READ:
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.equals("Ready")) {
                        youReady = true;
                    }
                    break;

                case Constant.MESSAGE_COORDINATE:
                    String readCoordinate = new String(readBuf, 0, msg.arg1);
                    int row = readCoordinate.codePointAt(0) - 48;
                    int col = readCoordinate.codePointAt(1) - 48;
                    if (myShipsPositions[row][col] == true) {
                        myShipsPositions[row][col] = false;
                        existShip--;
                        switchTurn(turn);
                        shipButtons[row][col].setBackgroundColor(Color.RED);
                        String hit = new String("hit");
                        bluetoothConnectionService.write(hit.getBytes());

                        Toast.makeText(getActivity(), "Trafił - " + existShip, Toast.LENGTH_SHORT).show();
                    } else {
                        switchTurn(turn);
                        shipButtons[row][col].setBackgroundColor(Color.YELLOW);
                        String miss = new String("miss");
                        bluetoothConnectionService.write(miss.getBytes());

                        Toast.makeText(getActivity(), "Nie Trafił Buahhaha", Toast.LENGTH_SHORT).show();
                    }
                    if (areAllFalse(myShipsPositions)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Przegrales!").setTitle("Game Over");
                        String gameOver = "gameOver";
                        bluetoothConnectionService.write(gameOver.getBytes());
                        dialog = builder.create();
                        dialog.show();
                    }
                    break;
                case Constant.MESSAGE_RESPOND:
                    String readRespond = new String(readBuf, 0, msg.arg1);
                    if (readRespond.equals("hit")) {
                        shotResult(true);
                        Toast.makeText(getActivity(), "Trafiłeś", Toast.LENGTH_SHORT).show();
                    } else if (readRespond.equals("miss")) {
                        shotResult(false);
                        Toast.makeText(getActivity(), "Nie trafiłeś :(", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constant.MESSAGE_GAME_OVER:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Wygrales!").setTitle("Game Over");
                    dialog = builder.create();
                    dialog.show();
                    break;
            }
        }
    };

    private void handleShipField(String rowcol) {

        row = rowcol.codePointAt(0) - 48;
        col = rowcol.codePointAt(1) - 48;
        shipButtons[row][col].setBackgroundColor(Color.GREEN);
        shipButtons[row][col].setEnabled(false);

        myShipsPositions[row][col] = true;
        tempShips--;
        existShip++;
        Toast.makeText(getActivity().getApplicationContext(), "Liczba statków do postawienia: " + String.valueOf(tempShips), Toast.LENGTH_SHORT).show();

    }

    public static boolean areAllFalse(boolean[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[1].length; j++) {
                if (array[i][j]) return false;
            }
        }
        return true;
    }

    private void handleShotField(String rowcol) {
        row = rowcol.codePointAt(0) - 48;
        col = rowcol.codePointAt(1) - 48;

        if (youReady && imReady) {
            //Toast.makeText(getActivity(), youReady + "wartosc you ready", Toast.LENGTH_LONG).show();
            if (turn.equals(myMark)) {
                switchTurn(turn);
                bluetoothConnectionService.write(rowcol.getBytes());
            } else
                Toast.makeText(getActivity(), "Not your turn!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Second player isn't ready yet!", Toast.LENGTH_SHORT).show();
        }

        /*if (myShipsPositions[row][col] == true) {
            Toast.makeText(getActivity().getApplicationContext(), "Trafiłeś", Toast.LENGTH_LONG).show();
            shotButtons[row][col].setBackgroundColor(Color.RED);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Pudło", Toast.LENGTH_LONG).show();
            shotButtons[row][col].setBackgroundColor(Color.BLACK);
        }*/
    }

    private void switchTurn(String currentTurn) {
        if (currentTurn.equals("X"))
            turn = "O";
        else
            turn = "X";
    }

    private void initButtons(View view) {
        shipButton00 = (ImageButton) view.findViewById(R.id.shipButton00);
        shipButton01 = (ImageButton) view.findViewById(R.id.shipButton01);
        shipButton02 = (ImageButton) view.findViewById(R.id.shipButton02);
        shipButton03 = (ImageButton) view.findViewById(R.id.shipButton03);
        shipButton04 = (ImageButton) view.findViewById(R.id.shipButton04);
        shipButton10 = (ImageButton) view.findViewById(R.id.shipButton10);
        shipButton11 = (ImageButton) view.findViewById(R.id.shipButton11);
        shipButton12 = (ImageButton) view.findViewById(R.id.shipButton12);
        shipButton13 = (ImageButton) view.findViewById(R.id.shipButton13);
        shipButton14 = (ImageButton) view.findViewById(R.id.shipButton14);
        shipButton20 = (ImageButton) view.findViewById(R.id.shipButton20);
        shipButton21 = (ImageButton) view.findViewById(R.id.shipButton21);
        shipButton22 = (ImageButton) view.findViewById(R.id.shipButton22);
        shipButton23 = (ImageButton) view.findViewById(R.id.shipButton23);
        shipButton24 = (ImageButton) view.findViewById(R.id.shipButton24);
        shipButton30 = (ImageButton) view.findViewById(R.id.shipButton30);
        shipButton31 = (ImageButton) view.findViewById(R.id.shipButton31);
        shipButton32 = (ImageButton) view.findViewById(R.id.shipButton32);
        shipButton33 = (ImageButton) view.findViewById(R.id.shipButton33);
        shipButton34 = (ImageButton) view.findViewById(R.id.shipButton34);
        shipButton40 = (ImageButton) view.findViewById(R.id.shipButton40);
        shipButton41 = (ImageButton) view.findViewById(R.id.shipButton41);
        shipButton42 = (ImageButton) view.findViewById(R.id.shipButton42);
        shipButton43 = (ImageButton) view.findViewById(R.id.shipButton43);
        shipButton44 = (ImageButton) view.findViewById(R.id.shipButton44);


        shotButton00 = (ImageButton) view.findViewById(R.id.shotButton00);
        shotButton01 = (ImageButton) view.findViewById(R.id.shotButton01);
        shotButton02 = (ImageButton) view.findViewById(R.id.shotButton02);
        shotButton03 = (ImageButton) view.findViewById(R.id.shotButton03);
        shotButton04 = (ImageButton) view.findViewById(R.id.shotButton04);
        shotButton10 = (ImageButton) view.findViewById(R.id.shotButton10);
        shotButton11 = (ImageButton) view.findViewById(R.id.shotButton11);
        shotButton12 = (ImageButton) view.findViewById(R.id.shotButton12);
        shotButton13 = (ImageButton) view.findViewById(R.id.shotButton13);
        shotButton14 = (ImageButton) view.findViewById(R.id.shotButton14);
        shotButton20 = (ImageButton) view.findViewById(R.id.shotButton20);
        shotButton21 = (ImageButton) view.findViewById(R.id.shotButton21);
        shotButton22 = (ImageButton) view.findViewById(R.id.shotButton22);
        shotButton23 = (ImageButton) view.findViewById(R.id.shotButton23);
        shotButton24 = (ImageButton) view.findViewById(R.id.shotButton24);
        shotButton30 = (ImageButton) view.findViewById(R.id.shotButton30);
        shotButton31 = (ImageButton) view.findViewById(R.id.shotButton31);
        shotButton32 = (ImageButton) view.findViewById(R.id.shotButton32);
        shotButton33 = (ImageButton) view.findViewById(R.id.shotButton33);
        shotButton34 = (ImageButton) view.findViewById(R.id.shotButton34);
        shotButton40 = (ImageButton) view.findViewById(R.id.shotButton40);
        shotButton41 = (ImageButton) view.findViewById(R.id.shotButton41);
        shotButton42 = (ImageButton) view.findViewById(R.id.shotButton42);
        shotButton43 = (ImageButton) view.findViewById(R.id.shotButton43);
        shotButton44 = (ImageButton) view.findViewById(R.id.shotButton44);

    }

    private void buttonsToArray() {
        shipButtons[0][0] = shipButton00;
        shipButtons[0][1] = shipButton01;
        shipButtons[0][2] = shipButton02;
        shipButtons[0][3] = shipButton03;
        shipButtons[0][4] = shipButton04;
        shipButtons[1][0] = shipButton10;
        shipButtons[1][1] = shipButton11;
        shipButtons[1][2] = shipButton12;
        shipButtons[1][3] = shipButton13;
        shipButtons[1][4] = shipButton14;
        shipButtons[2][0] = shipButton20;
        shipButtons[2][1] = shipButton21;
        shipButtons[2][2] = shipButton22;
        shipButtons[2][3] = shipButton23;
        shipButtons[2][4] = shipButton24;
        shipButtons[3][0] = shipButton30;
        shipButtons[3][1] = shipButton31;
        shipButtons[3][2] = shipButton32;
        shipButtons[3][3] = shipButton33;
        shipButtons[3][4] = shipButton34;
        shipButtons[4][0] = shipButton40;
        shipButtons[4][1] = shipButton41;
        shipButtons[4][2] = shipButton42;
        shipButtons[4][3] = shipButton43;
        shipButtons[4][4] = shipButton44;

        shotButtons[0][0] = shotButton00;
        shotButtons[0][1] = shotButton01;
        shotButtons[0][2] = shotButton02;
        shotButtons[0][3] = shotButton03;
        shotButtons[0][4] = shotButton04;
        shotButtons[1][0] = shotButton10;
        shotButtons[1][1] = shotButton11;
        shotButtons[1][2] = shotButton12;
        shotButtons[1][3] = shotButton13;
        shotButtons[1][4] = shotButton14;
        shotButtons[2][0] = shotButton20;
        shotButtons[2][1] = shotButton21;
        shotButtons[2][2] = shotButton22;
        shotButtons[2][3] = shotButton23;
        shotButtons[2][4] = shotButton24;
        shotButtons[3][0] = shotButton30;
        shotButtons[3][1] = shotButton31;
        shotButtons[3][2] = shotButton32;
        shotButtons[3][3] = shotButton33;
        shotButtons[3][4] = shotButton34;
        shotButtons[4][0] = shotButton40;
        shotButtons[4][1] = shotButton41;
        shotButtons[4][2] = shotButton42;
        shotButtons[4][3] = shotButton43;
        shotButtons[4][4] = shotButton44;

    }

    private void disableShipsField() {
        for (int i = 0; i < shipButtons.length; i++) {
            for (int j = 0; j < shipButtons[1].length; j++) {
                shipButtons[i][j].setEnabled(false);
                //shipButtons[i][j].setBackgroundColor(Color.BLUE);
            }
        }
    }

    private void enableShotField() {
        for (int i = 0; i < shipButtons.length; i++) {
            for (int j = 0; j < shipButtons[1].length; j++) {
                shotButtons[i][j].setEnabled(true);
            }
        }
    }

    private void disableShotField() {
        for (int i = 0; i < shipButtons.length; i++) {
            for (int j = 0; j < shipButtons[1].length; j++) {
                shotButtons[i][j].setEnabled(false);
            }
        }
    }

    private void shotResult(boolean result) {
        if (result) {
            shotButtons[row][col].setBackgroundColor(Color.RED);
            shotButtons[row][col].setClickable(false);

        } else {
            shotButtons[row][col].setBackgroundColor(Color.YELLOW);
            shotButtons[row][col].setClickable(false);

        }

    }

}

