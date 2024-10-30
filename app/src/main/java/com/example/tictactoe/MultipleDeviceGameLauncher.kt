package com.example.tictactoe

import AndroidBluetoothController
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tictactoe.bluetooth.BluetoothDeviceDomain
import com.example.tictactoe.bluetooth.ConnectionResult
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MultipleDeviceGameLauncher : AppCompatActivity() {

    private lateinit var androidBluetoothController: AndroidBluetoothController
    private lateinit var scannedDeviceListAdapter: ArrayAdapter<String>
    private lateinit var pairedDeviceListAdapter: ArrayAdapter<String>
    private lateinit var scanButton: Button
    private lateinit var listViewScanned: ListView
    private lateinit var listViewPaired: ListView
    private lateinit var hostButton: Button
    private lateinit var joinButton: Button

    private val phonePermissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiple_devices_choices)

        // Initialize Bluetooth Controller
        androidBluetoothController = AndroidBluetoothController(this)

        scanButton = findViewById(R.id.scanDevicesButton)
        //showPairedDevicesButton = findViewById(R.id.hostButton)
        listViewScanned = findViewById(R.id.listViewScannedDevices)
        listViewPaired = findViewById(R.id.listViewPairedDevices)

        // Initialize separate adapters for paired and scanned devices
        scannedDeviceListAdapter = ArrayAdapter(this, R.layout.list_item, R.id.device_name, mutableListOf())
        pairedDeviceListAdapter = ArrayAdapter(this, R.layout.list_item, R.id.device_name, mutableListOf())

        listViewScanned.adapter = scannedDeviceListAdapter
        listViewPaired.adapter = pairedDeviceListAdapter

        requestPermissionsIfNeeded()

        androidBluetoothController = AndroidBluetoothController(this)

        hostButton = findViewById(R.id.hostButton)
        joinButton = findViewById(R.id.joinButton)

        scanButton.setOnClickListener {
            startScanning()
        }

        hostButton.setOnClickListener {
            // Start the server for this device
            androidBluetoothController.startServer()
            Toast.makeText(this, "Waiting for player to join...", Toast.LENGTH_SHORT).show()
        }

        joinButton.setOnClickListener {
            // Display paired devices to connect to
            showPairedDevices()
        }

        mainScope.launch {
            androidBluetoothController.connectionEvents.collectLatest { result ->
                when (result) {
                    is ConnectionResult.ConnectionEstablished -> {
                        Toast.makeText(this@MultipleDeviceGameLauncher, "Connection Established", Toast.LENGTH_SHORT).show()

                        // Launch the TicTacToe game activity
                        val intent = Intent(this@MultipleDeviceGameLauncher, MultiplayerTicTacToeGameActivity::class.java)

                        // You might want to pass any necessary data, like player names or roles, to the game activity
                        // intent.putExtra("PLAYER_ROLE", "Host" or "Joiner")

                        startActivity(intent)
                    }
                    is ConnectionResult.Error -> {
                        Toast.makeText(this@MultipleDeviceGameLauncher, "Connection Error: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                    is ConnectionResult.TransferSucceeded -> TODO()
                }
            }
        }

    }

    private fun requestPermissionsIfNeeded() {
        if (!hasBluetoothPermissions()) {
            ActivityCompat.requestPermissions(this, phonePermissions, 1)
        } else {
            updatePairedDevices()
        }
    }

    private fun startScanning() {
        if (hasBluetoothPermissions()) {
            androidBluetoothController.startDiscovery()
            mainScope.launch {
                androidBluetoothController.scannedDevices.collectLatest { devices ->
                    updateScannedDeviceList(devices)
                    Log.i("ScannedDevices", devices.toString())
                }
            }
        } else {
            Toast.makeText(this, "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return phonePermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun updatePairedDevices() {
        if (hasBluetoothPermissions()) {
            androidBluetoothController.refreshPairedDevices()
            mainScope.launch {
                androidBluetoothController.pairedDevices.collectLatest { devices ->
                    updatePairedDeviceList(devices)
                }
            }
        }
    }

    /*private fun showPairedDevices() {
        if (hasBluetoothPermissions()) {
            val pairedDevices = BluetoothAdapter.getDefaultAdapter()?.bondedDevices
            val deviceNames = pairedDevices?.map { it.name ?: "Unknown Device (${it.address})" } ?: listOf("No paired devices found")
            Log.i("PairedDevices", deviceNames.toString())

            pairedDeviceListAdapter.clear()
            pairedDeviceListAdapter.addAll(deviceNames)
            pairedDeviceListAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Paired devices shown", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun showPairedDevices() {
        if (hasBluetoothPermissions()) {
            // Retrieve paired devices from the BluetoothController
            val pairedDevices = BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.toList()
            val deviceNames = pairedDevices?.map { it.name } ?: listOf()

            // Show dialog to pick a device to connect to
            AlertDialog.Builder(this)
                .setTitle("Select a Device to Join")
                .setItems(deviceNames.toTypedArray()) { _, index ->
                    pairedDevices?.get(index)?.let { selectedDevice ->
                        // Start client connection to selected device
                        androidBluetoothController.startClient(selectedDevice)
                        Toast.makeText(this, "Connecting to ${selectedDevice.name}...", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateScannedDeviceList(devices: List<BluetoothDeviceDomain>) {
        val deviceNames = devices.map { it.name ?: "Unknown Device (${it.address})" }
        Log.i("ScannedDeviceNames", deviceNames.toString())
        scannedDeviceListAdapter.clear()
        scannedDeviceListAdapter.addAll(deviceNames)
        scannedDeviceListAdapter.notifyDataSetChanged()
    }

    private fun updatePairedDeviceList(devices: List<BluetoothDeviceDomain>) {
        // This method can remain as is, or you can choose to modify it to filter out non-paired devices if needed.
        val deviceNames = devices.map { it.name ?: "Unknown Device (${it.address})" }
        Log.i("PairedDeviceNames", deviceNames.toString())
        pairedDeviceListAdapter.clear()
        pairedDeviceListAdapter.addAll(deviceNames)
        pairedDeviceListAdapter.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            updatePairedDevices()
        } else {
            Toast.makeText(this, "Permissions required to show devices", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        androidBluetoothController.stopDiscovery() // Ensure discovery is stopped
        androidBluetoothController.release()
    }
}
