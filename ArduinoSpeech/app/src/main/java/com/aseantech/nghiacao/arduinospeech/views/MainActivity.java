package com.aseantech.nghiacao.arduinospeech.views;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aseantech.nghiacao.arduinospeech.R;
import com.aseantech.nghiacao.arduinospeech.services.BluetoothService;
import com.aseantech.nghiacao.arduinospeech.services.Constants;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Button btnConnect;
    private ImageButton btnRecord;
    private TextView tvConnectedDevice, tvPreview;
    private ImageView imgLight;

    private BluetoothService bluetoothService;
    private BluetoothDevice bluetoothDevice = HomeActivity.mBluetoothDevice;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setWidget();

        tvConnectedDevice.setText("");
        bluetoothService = new BluetoothService(MainActivity.this, mHandler);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                bluetoothService = new BluetoothService(MainActivity.this, mHandler);
                if(bluetoothService.getState() == BluetoothService.STATE_CONNECTION_NONE){
                    bluetoothService.connect(bluetoothDevice,MY_UUID);
                    btnRecord.setEnabled(true);
                    btnRecord.setImageResource(R.drawable.mic_active);
                }

                if(bluetoothService.getState() == BluetoothService.STATE_CONNECTED){
                    bluetoothService.stop();
                    tvConnectedDevice.setText("");
                    btnRecord.setEnabled(false);
                    btnRecord.setImageResource(R.drawable.mic_not_active);
                }

            }
        });

        //Speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    tvPreview.setText(matches.get(0));
                    Log.d("Result", "onResults: " + tvPreview.getText().toString().trim());
                    if (tvPreview.getText().toString().trim().toUpperCase().equals("BẬT") || tvPreview.getText().toString().trim().toUpperCase().equals("MỞ") || tvPreview.getText().toString().trim().toUpperCase().equals("ON") || tvPreview.getText().toString().trim().toUpperCase().equals("TURN ON")) {
                        turnOnLight(bluetoothService);
                        imgLight.setImageResource(R.drawable.light_on);
                    } else if (tvPreview.getText().toString().trim().toUpperCase().equals("TẮT") || tvPreview.getText().toString().trim().toUpperCase().equals("OFF") || tvPreview.getText().toString().trim().toUpperCase().equals("TURN OFF")) {
                        turnOffLight(bluetoothService);
                        imgLight.setImageResource(R.drawable.light_off);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        //when the user removed the finger
                        speechRecognizer.stopListening();
                        btnRecord.setImageResource(R.drawable.mic_active);
                        btnRecord.setBackgroundColor(Color.TRANSPARENT);
                        tvPreview.setHint("You will see input here");
                        break;
                    case MotionEvent.ACTION_DOWN:
                        //finger is on the button
                        tvPreview.setText("");
                        tvPreview.setHint("Listening...");
                        btnRecord.setImageResource(R.drawable.mic_on);
                        btnRecord.setBackgroundColor(Color.TRANSPARENT);
                        speechRecognizer.startListening(speechRecognizerIntent);
                        break;
                }
                return false;
            }
        });
    }

    private void setWidget() {
        tvConnectedDevice = findViewById(R.id.tv_connected_device);
        tvPreview = findViewById(R.id.tv_preview);
        btnRecord = findViewById(R.id.btn_record);
        imgLight = findViewById(R.id.img_light);
        btnConnect = findViewById(R.id.btn_connect);
        btnRecord.setEnabled(false);
        btnRecord.setImageResource(R.drawable.mic_not_active);
    }

    private void turnOnLight(BluetoothService bluetoothConnection)
    {
        String txtSend = "1";
        bluetoothConnection.write(txtSend.getBytes());
    }

    private void turnOffLight(BluetoothService bluetoothConnection)
    {
        String txtSend = "2";
        bluetoothConnection.write(txtSend.getBytes());
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            btnConnect.setText(getResources().getString(R.string.disconnect));
                            btnConnect.setBackgroundResource(R.drawable.button_border_disconnect);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            btnConnect.setText(getResources().getString(R.string.connect));
                            btnConnect.setBackgroundResource(R.drawable.background_text_disable);
                            break;
                        case BluetoothService.STATE_CONNECTION_NONE:
                            btnConnect.setText(getResources().getString(R.string.connect));
                            btnConnect.setBackgroundResource(R.drawable.change_background_button);
                            break;
                    }
                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    tvConnectedDevice.setText(msg.getData().getString(Constants.DEVICE_NAME));
                    Toast.makeText(MainActivity.this, "Connected to " + tvConnectedDevice.getText(), Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (bluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService.getState() == bluetoothService.STATE_CONNECTION_NONE) {
                // Start the Bluetooth chat services
                bluetoothService.start();
            }
        }
    }
}
