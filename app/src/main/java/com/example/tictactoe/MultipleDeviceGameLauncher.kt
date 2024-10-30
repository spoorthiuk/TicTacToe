package com.example.tictactoe

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tictactoe.bluetooth.BluetoothDeviceDomain
import com.example.tictactoe.data.AndroidBluetoothController
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class MultipleDeviceGameLauncher : AppCompatActivity()
{
    private lateinit var androidBluetoothController: AndroidBluetoothController
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDeviceListAdapter: ArrayAdapter<String>
    private lateinit var showPairedDevicesButton: Button
    private lateinit var listView: ListView
    private lateinit var bluetoothDevicesList: List<BluetoothDeviceDomain>
    private val phonePermissions = mutableListOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN).apply { }.toTypedArray()
    private var serverSocket:BluetoothServerSocket? = null
    private var clientSocket:BluetoothSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiple_devices_choices)
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        showPairedDevicesButton = findViewById(R.id.bluetoothON)
        listView = findViewById(R.id.listViewDevices)
        pairedDeviceListAdapter = ArrayAdapter(this,R.layout.list_item,R.id.device_name, mutableListOf() )
        listView.adapter = pairedDeviceListAdapter

        if(!phonePermissions.all{ ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED })
        {
            ActivityCompat.requestPermissions(this,phonePermissions,1)
        }
        showPairedDevices()
        startBluetoothServer()
        listView.setOnItemClickListener { parent, view, position, id ->
            val bluetoothDevice : BluetoothDevice? = androidBluetoothController.getBondedDevices()?.elementAt(position)
            connectToDevice(bluetoothDevice)
        }
    }

    private fun connectToDevice(selectedDevice: BluetoothDevice?) {
        Toast.makeText(this,"Connecting to ${selectedDevice?.name}",Toast.LENGTH_SHORT).show()
        Thread{
            clientSocket = bluetoothAdapter
                .getRemoteDevice(selectedDevice?.address)?.
                createRfcommSocketToServiceRecord(MY_UUID)

            clientSocket?.let { socket ->
                try {
                    socket.connect()
                    manageConnectedSocket(socket)
                }catch (e:IOException){
                    socket.close()
                    clientSocket = null
                }
            }
//            var socket: BluetoothSocket? = null
//            try{
//                socket = selectedDevice?.createRfcommSocketToServiceRecord(MY_UUID)
//                socket?.connect()
//
//                runOnUiThread {
//                    Toast.makeText(this,"Connected to ${selectedDevice?.name}",Toast.LENGTH_SHORT).show()
//                }
//                manageConnectedSocket(socket)
//            }catch (e: IOException) {
//                Log.e(TAG, "Error connecting to device", e)
//                runOnUiThread {
//                    Toast.makeText(this, "Failed to connect to ${selectedDevice?.name}", Toast.LENGTH_LONG).show()
//                }
//                try {
//                    socket?.close()
//                } catch (closeException: IOException) {
//                    Log.e(TAG, "Could not close the client socket", closeException)
//                }
//            }
        }.start()
    }

    private fun manageConnectedSocket(socket: BluetoothSocket?) {
        
    }

    private fun startBluetoothServer(){
        Thread{
            try {
                serverSocket= bluetoothAdapter.listenUsingRfcommWithServiceRecord("TicTacToeApp", MY_UUID)
                var shouldLoop = true
                while (shouldLoop){
                    clientSocket = try{
                        serverSocket?.accept()
                    }catch (e:IOException){
                        shouldLoop = false
                        null
                    }
                    clientSocket?.let {
                        runOnUiThread {
                            Toast.makeText(this,"Connected to Device",Toast.LENGTH_SHORT).show()
                        }
                        serverSocket?.close()
                    }
                }
            }catch (e: IOException){
                Log.e(TAG,"Error Connecting to Device",e)
            }
        }
    }

    private fun showPairedDevices()
    {
        showPairedDevicesButton.setOnClickListener {
            if (!bluetoothAdapter.isEnabled)
            {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
            }
            androidBluetoothController = AndroidBluetoothController(this)
            bluetoothDevicesList = androidBluetoothController.pairedDevices.value

            val availableDevices = mutableListOf<String>()
            bluetoothDevicesList.forEach{
                if(it.name.toString() == "null")
                {
                    availableDevices.add(it.macAddress)
                }
                else
                {
                    availableDevices.add(it.name.toString())
                }
            }
            pairedDeviceListAdapter.clear()
            pairedDeviceListAdapter.addAll(availableDevices)
            pairedDeviceListAdapter.notifyDataSetChanged()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1)
        {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            {
                showPairedDevices()
            } else
            {
                Toast.makeText(this, "Permissions required to show list of available devices", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                Toast.makeText(applicationContext,"Bluetooth is Enabled",Toast.LENGTH_LONG).show()
            }
            if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(applicationContext,"Bluetooth Connection Cancelled",Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object
    {
        const val REQUEST_ENABLE_BT = 1
        private val TAG = MultipleDeviceGameLauncher::class.java.simpleName
        private val MY_UUID: UUID = UUID.fromString("7df84998-f8e1-4d14-a3cb-aae69067e321")  // SPP UUID
    }
}


