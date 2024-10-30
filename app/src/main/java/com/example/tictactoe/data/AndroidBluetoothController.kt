import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import com.example.tictactoe.bluetooth.BluetoothDeviceDomain
import com.example.tictactoe.data.BluetoothMessage
import com.example.tictactoe.data.FoundDeviceReceiver
import com.example.tictactoe.data.toBluetoothDeviceDomain
import com.example.tictactoe.bluetooth.ConnectionResult
import com.example.tictactoe.bluetooth.BluetoothController
import com.example.tictactoe.bluetooth.BluetoothStateReceiver
import com.example.tictactoe.bluetooth.BluetoothTransferService
import com.example.tictactoe.bluetooth.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
): BluetoothController {
    private var serverSocket: BluetoothServerSocket? = null
    private var communicationSocket: BluetoothSocket? = null
    private val uuid: UUID = UUID.fromString("27b7d1da-08c7-4505-a6d1-2459987e5e2d") // Use the same UUID on both devices
    // StateFlow to observe connection status
    //val isConnected = MutableStateFlow(false)

    // Start server for connection
    private val _connectionEvents = MutableSharedFlow<ConnectionResult>()
    val connectionEvents: SharedFlow<ConnectionResult> get() = _connectionEvents.asSharedFlow()

    fun startServer() {
        serverSocket = BluetoothAdapter.getDefaultAdapter()
            .listenUsingRfcommWithServiceRecord("TicTacToeGame", uuid)

        // Start a coroutine for non-blocking I/O operations
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("Bluetooth", "Waiting for client connection...")
                communicationSocket = serverSocket?.accept() // Blocking call

                if (communicationSocket != null) {
                    serverSocket?.close() // Close after client connects
                    _isConnected.value = true

                    // Emit a successful connection event
                    _connectionEvents.emit(ConnectionResult.ConnectionEstablished)
                    Log.d("Bluetooth", "Client connected successfully")

                    handleConnection(communicationSocket) // Start handling communication
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Server error: ", e)
                _connectionEvents.emit(ConnectionResult.Error("Connection failed"))
            }
        }
    }
    // Handle data transfer
    private fun handleConnection(socket: BluetoothSocket?) {
        val inputStream = socket?.inputStream
        val outputStream = socket?.outputStream
        if (inputStream != null && outputStream != null) {
            listenForMessages(inputStream)
            this.outputStream = outputStream
        }
    }

    private fun listenForMessages(inputStream: InputStream) {
        Thread {
            val buffer = ByteArray(1024)
            try {
                while (true) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val message = String(buffer, 0, bytesRead)
                        Log.d("Bluetooth", "Received message: $message")
                        // Pass this message to your game logic to update game state
                    }
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Disconnected", e)
                _isConnected.value = false
            }
        }.start()
    }

    private var outputStream: OutputStream? = null

    fun sendMessage(message: String) {
        try {
            outputStream?.write(message.toByteArray())
            Log.d("Bluetooth", "Message sent: $message")
        } catch (e: IOException) {
            Log.e("Bluetooth", "Error sending message", e)
        }
    }

    // Connect to server as client
    fun startClient(serverDevice: BluetoothDevice) {
        communicationSocket = serverDevice.createRfcommSocketToServiceRecord(uuid)

        Thread {
            try {
                communicationSocket?.connect() // Blocking call
                _isConnected.value = true
                handleConnection(communicationSocket)
            } catch (e: IOException) {
                Log.e("Bluetooth", "Client error: ", e)
            }
        }.start()
    }


    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var dataTransferService: BluetoothTransferService? = null

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if(bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device.")
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    override fun startDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while(shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch(e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                    val service = BluetoothTransferService(it)
                    dataTransferService = service

                    emitAll(
                        service
                            .listenForIncomingMessages()
                            .map {
                                ConnectionResult.TransferSucceeded(it)
                            }
                    )
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult> {
        return flow {
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentClientSocket = bluetoothAdapter
                ?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)

                    BluetoothTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map { ConnectionResult.TransferSucceeded(it) }
                        )
                    }
                } catch(e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }

        if(dataTransferService == null) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = bluetoothAdapter?.name ?: "Unknown name",
            isFromLocalUser = true
        )

        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    fun refreshPairedDevices() {
        updatePairedDevices()
    }

    private fun updatePairedDevices() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}