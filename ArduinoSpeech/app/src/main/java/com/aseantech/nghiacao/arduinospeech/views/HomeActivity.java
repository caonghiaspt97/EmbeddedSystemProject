package com.aseantech.nghiacao.arduinospeech.views;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aseantech.nghiacao.arduinospeech.R;
import com.aseantech.nghiacao.arduinospeech.adapters.DeviceAdapter;
import com.aseantech.nghiacao.arduinospeech.adapters.SlidePagerAdapter;
import com.aseantech.nghiacao.arduinospeech.models.Slide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {
    private final int BLUETOOTH_REQUEST_CODE = 2012;

    private List<Slide> listSlides;
    private ViewPager slidePager;
    private TabLayout indicator;
    private SlidePagerAdapter adapter;
    private SwipeRefreshLayout swipeLayout;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    private ListView lvPairedDevices, lvAvailableDevices;
    private List<BluetoothDevice> listPairedDevices, listAvailableDevices, listDiscoverableDevices;
    private DeviceAdapter pairedAdapter, availableAdapter;


    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice mBluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setWidget();

        //Init slide
        initListSlide();
        adapter = new SlidePagerAdapter(this, listSlides);
        slidePager.setAdapter(adapter);
        indicator.setupWithViewPager(slidePager, true);
        //setup timer for slide pager
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 1000, 4000);

        // Register receiver to listen bluetooth state
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothState, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bondState, intentFilter2);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState(mBluetoothAdapter);

        // Show 2 list devices
        loadUI();

        // Swipe down to reload
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadUI();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        swipeLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent)
        );

        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.setSelected(true);
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothDevice = listPairedDevices.get(i);

                if (listDiscoverableDevices.contains(mBluetoothDevice)) {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomeActivity.this, "This device is not available!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        lvAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.setSelected(true);
                mBluetoothAdapter.cancelDiscovery();
                progressDialog = new ProgressDialog(HomeActivity.this);
                progressDialog.setMessage("Pairing to " + listAvailableDevices.get(i).getName());
                progressDialog.show();
                listAvailableDevices.get(i).createBond();

                IntentFilter intentFilter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                registerReceiver(bondState, intentFilter2);
            }
        });
    }

    private void setWidget() {
        slidePager = findViewById(R.id.slider_pager);
        indicator = findViewById(R.id.indicator);
        swipeLayout = findViewById(R.id.swipe_container);
        lvPairedDevices = findViewById(R.id.lv_paired_devices);
        lvAvailableDevices = findViewById(R.id.lv_available_devices);
        progressBar = findViewById(R.id.progress_discover);
    }

    private void initListSlide() {
        listSlides = new ArrayList<>();
        listSlides.add(new Slide(R.drawable.arduino));
        listSlides.add(new Slide(R.drawable.welcome1));
        listSlides.add(new Slide(R.drawable.welcome2));
        listSlides.add(new Slide(R.drawable.welcome3));
    }

    private void checkBluetoothState(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth don't support this device", Toast.LENGTH_SHORT).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, BLUETOOTH_REQUEST_CODE);

            IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothState, filter1);
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
            IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothState, filter1);
        }
    }

    private void setPairedDevicesListView(List<BluetoothDevice> pairedDevices) {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                pairedDevices.add(device);
            }
            pairedAdapter = new DeviceAdapter(getApplicationContext(), R.layout.item_bluetooth_device, pairedDevices);
            lvPairedDevices.setAdapter(pairedAdapter);
        }
    }

    private void setAvailableDevicesListView(List<BluetoothDevice> availableDevices) {
        availableAdapter = new DeviceAdapter(getApplicationContext(), R.layout.item_bluetooth_device, availableDevices);
        lvAvailableDevices.setAdapter(availableAdapter);
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.INVISIBLE);
        }
        mBluetoothAdapter.startDiscovery();
        IntentFilter discoverIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoverReceiver, discoverIntent);

        IntentFilter discoverStartIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(discoverReceiver, discoverStartIntent);

        IntentFilter discoverFinishIntent = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoverReceiver, discoverFinishIntent);

    }

    private void loadUI() {
        listDiscoverableDevices = new ArrayList<>();
        listPairedDevices = new ArrayList<>();
        listAvailableDevices = new ArrayList<>();
        setPairedDevicesListView(listPairedDevices);
        setAvailableDevicesListView(listAvailableDevices);
    }

    class SliderTimer extends TimerTask {
        @Override
        public void run() {
            HomeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (slidePager.getCurrentItem() < listSlides.size() - 1) {
                        slidePager.setCurrentItem(slidePager.getCurrentItem() + 1);
                    } else {
                        slidePager.setCurrentItem(0);
                    }
                }
            });
        }
    }

    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("TAG", "onReceive: STATE_OFF");
                        Toast.makeText(getApplicationContext(), "Bluetooth is disabled", Toast.LENGTH_SHORT).show();
                        Intent enableBluetooth1 = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetooth1, BLUETOOTH_REQUEST_CODE);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("TAG", "onReceive: STATE_TURNING_OFF");
                        Toast.makeText(getApplicationContext(), "Disabling bluetooth", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("TAG", "onReceive: STATE_ON");
                        Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_SHORT).show();

                        loadUI();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("TAG", "onReceive: STATE_TURNING_ON");
                        Toast.makeText(getApplicationContext(), "Enabling bluetooth", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    BroadcastReceiver discoverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.INVISIBLE);
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!listPairedDevices.contains(device)) {
                    listAvailableDevices.add(device);
                    availableAdapter.notifyDataSetChanged();
                }
                listDiscoverableDevices.add(device);
            }
        }
    };

    BroadcastReceiver bondState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d("TAG", "onReceive: BOND_NONE");
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No devices are paired", Toast.LENGTH_SHORT).show();
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d("TAG", "onReceive: BOND_BONDING");
                    Toast.makeText(getApplicationContext(), "Pairing with " + device.getName(), Toast.LENGTH_SHORT).show();
                }
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d("TAG", "onReceive: BOND_BONDED");
                    Toast.makeText(getApplicationContext(), "Paired with " + device.getName(), Toast.LENGTH_SHORT).show();

                    progressDialog.dismiss();
                    mBluetoothDevice = device;

                    Intent intent1 = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent1);

                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(bluetoothState, filter1);
            } else {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("You must enable bluetooth to use app");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Cancel discovery
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Unregister Receiver
        unregisterReceiver(bluetoothState);
        unregisterReceiver(discoverReceiver);
    }
}
