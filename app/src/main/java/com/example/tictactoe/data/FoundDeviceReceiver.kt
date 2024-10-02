package com.example.tictactoe.data

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FoundDeviceReceiver (
    private val onDEviceFound: (BluetoothDevice) -> Unit
) : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            BluetoothDevice.ACTION_FOUND -> {
                val device = intent
            }
        }
    }
}