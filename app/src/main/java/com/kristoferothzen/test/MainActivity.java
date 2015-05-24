package com.kristoferothzen.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.view.Display;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import jp.co.olympus.camerakit.OACentralConfiguration;
import jp.co.olympus.camerakit.OLYCamera;
import jp.co.olympus.camerakit.OLYCameraConnectionListener;
import jp.co.olympus.camerakit.OLYCameraKitException;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements OLYCameraConnectionListener{
    public BluetoothDevice cameraDevice;
    private final String TAG = this.toString();
    private Executor connectionExecutor = Executors.newFixedThreadPool(1);
    private BluetoothAdapter btAdapter;
    private BroadcastReceiver connectionReceiver;
    private OLYCamera camera;
    {
        camera = new OLYCamera();
        camera.setConnectionListener(this);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
        OACentralConfiguration OACentral = OACentralConfiguration.load();
        String BLEName = OACentral.getBleName();
        String BLECode = OACentral.getBleCode();
        BluetoothDevice bluetoothDevice;
        @Override
        public void onLeScan(final BluetoothDevice device,int rssi,byte[] scanRecord){
            if(BLEName.equals(device.getName())){
                btAdapter.stopLeScan(this);
                try{
                    camera.setBluetoothDevice(device);
                    makePublic(device);
                    camera.setBluetoothPassword(BLECode);
                    camera.wakeup();
                    camera.connect(OLYCamera.ConnectionType.BluetoothLE);
                }
                catch(OLYCameraKitException e){
                    Log.w(TAG,"Connecting to the camera failed: "+ e.getMessage());
                }
            }
        }
    };

    public void statusKnappKlickad(View V) {
        //Visa statusen vid knapptryckningen
        TextView statusTextBox;
        String connectionStatus = "Penisen";
        statusTextBox = (TextView) findViewById(R.id.statusText);
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (wifiManager.isWifiEnabled() && info != null && info.getNetworkId() != -1) {
            connectionStatus = startConnectingCamera();
        }
        //connectionStatus = String.valueOf(camera.isConnected());
        statusTextBox.setText(connectionStatus);
    }


    public String startConnectingCamera(){
        String connectionStatus = "Not connected yet";
        //camera.setContext(Context context);
        //BluetoothDevice Luffe =
        camera.setContext(getApplicationContext());
        camera.setConnectionListener(this);
        OACentralConfiguration OACentral = OACentralConfiguration.load();
        String BLEName = OACentral.getBleName();
        String BLECode = OACentral.getBleCode();
        try {
            camera.setBluetoothDevice(cameraDevice);
            camera.setBluetoothPassword(BLECode);
            camera.wakeup();
            camera.connect(OLYCamera.ConnectionType.BluetoothLE);
            camera.wakeup();
            camera.connect(OLYCamera.ConnectionType.WiFi);
            connectionStatus = "Connection succeeded!";
        } catch (OLYCameraKitException e) {
            Log.w(TAG, "Connecting to the camera failed: " + e.getMessage());
            connectionStatus = String.valueOf(e.getMessage());
            return connectionStatus;
        }


        return connectionStatus;
    }


    void makePublic(BluetoothDevice bluetoothDevice){
        cameraDevice=bluetoothDevice;
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onDisconnectedFromCamera() {
        /*if (!isDestroyed()) {
            ConnectingFragment fragment = new ConnectingFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment1, fragment);
            transaction.commit();
        }*/
    }

    @Override
    public void onDisconnectedByError(OLYCamera camera, OLYCameraKitException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onDisconnectedFromCamera();
            }
        });
    }
    private OLYCamera.LiveViewSize toLiveViewSize(String quality) {
        if (quality.equalsIgnoreCase("QVGA")) {
            return OLYCamera.LiveViewSize.QVGA;
        } else if (quality.equalsIgnoreCase("VGA")) {
            return OLYCamera.LiveViewSize.VGA;
        } else if (quality.equalsIgnoreCase("SVGA")) {
            return OLYCamera.LiveViewSize.SVGA;
        } else if (quality.equalsIgnoreCase("XGA")) {
            return OLYCamera.LiveViewSize.XGA;
        }
        return OLYCamera.LiveViewSize.QVGA;
    }
}
