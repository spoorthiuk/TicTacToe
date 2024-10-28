package com.example.tictactoe.bluetooth

import com.example.tictactoe.data.BluetoothMessage


sealed interface ConnectionResult {
    object ConnectionEstablished: ConnectionResult
    data class TransferSucceeded(val message: BluetoothMessage): ConnectionResult
    data class Error(val message: String): ConnectionResult
}