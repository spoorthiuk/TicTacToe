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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class MultipleDeviceGameLauncher : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDeviceListAdapter: ArrayAdapter<String>
    private lateinit var showPairedDevicesButton: Button
    private lateinit var startServerButton: Button
    private lateinit var listView: ListView
    private lateinit var startServerText: TextView
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private val MY_UUID: UUID = UUID.fromString("7df84998-f8e1-4d14-a3cb-aae69067e321")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiple_devices_choices)

        // Initialize Bluetooth adapter and check permissions
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        showPairedDevicesButton = findViewById(R.id.bluetoothON)
        startServerButton = findViewById(R.id.startServerButton)
        listView = findViewById(R.id.listViewDevices)
        startServerText = findViewById(R.id.serverText)

        // Adapter to list paired devices
        pairedDeviceListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = pairedDeviceListAdapter

        requestPermissionsIfNeeded()

        // Set up buttons
        showPairedDevicesButton.setOnClickListener { showPairedDevices() }
        startServerButton.setOnClickListener {
            startServerText.text = "Waiting for player to join"
            startBluetoothServer() }

        // Handle device selection for connection
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = bluetoothAdapter.bondedDevices.elementAtOrNull(position)
            selectedDevice?.let {
                connectToDevice(it)
            } ?: Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        if (!permissions.all { ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
        }
    }

    private fun startBluetoothServer() {
        Thread {
            try {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("TicTacToeServer", MY_UUID)
                Log.d("BluetoothServer", "Server started, waiting for connection...")
                clientSocket = serverSocket?.accept()
                clientSocket?.let { socket ->
                    Log.d("BluetoothServer", "Device connected to server")
                    runOnUiThread {
                        Toast.makeText(this, "Device connected as Server", Toast.LENGTH_SHORT).show()
                        GamePlayAcrossDevicesActivity.isServer = true // Set server role
                        serverSocket?.close()
                        startGameActivity(socket)
                    }
                }
            } catch (e: IOException) {
                Log.e("BluetoothServer", "Error starting server", e)
            }
        }.start()
    }


    private fun connectToDevice(device: BluetoothDevice) {
        Toast.makeText(this, "Connecting to ${device.name}", Toast.LENGTH_SHORT).show()
        Thread {
            clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
            clientSocket?.let { socket ->
                try {
                    Log.d("BluetoothClient", "Attempting to connect to ${device.name}")
                    socket.connect()
                    Log.d("BluetoothClient", "Successfully connected to ${device.name}")
                    runOnUiThread {
                        Toast.makeText(this, "Connected to Server", Toast.LENGTH_SHORT).show()
                        GamePlayAcrossDevicesActivity.isServer = false // Set client role
                        startGameActivity(socket)
                    }
                } catch (e: IOException) {
                    Log.e("BluetoothClient", "Connection failed", e)
                    socket.close()
                    clientSocket = null
                }
            }
        }.start()
    }

    private fun startGameActivity(socket: BluetoothSocket) {
        clientSocket = socket
        GamePlayAcrossDevicesActivity.connectedSocket = socket
        startActivity(Intent(this, GamePlayAcrossDevicesActivity::class.java))
    }

    private fun showPairedDevices() {
        if (!bluetoothAdapter.isEnabled) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
        } else {
            val pairedDevices = bluetoothAdapter.bondedDevices
            val availableDevices = pairedDevices.map { it.name ?: it.address }.toMutableList()
            pairedDeviceListAdapter.clear()
            pairedDeviceListAdapter.addAll(availableDevices)
            pairedDeviceListAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
        private const val REQUEST_PERMISSIONS = 1
    }
}
