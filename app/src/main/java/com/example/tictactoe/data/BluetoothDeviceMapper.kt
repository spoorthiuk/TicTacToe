package com.example.tictactoe.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.tictactoe.bluetooth.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain{
    return BluetoothDeviceDomain(
        name = name,
        macAddress = address
    )
}