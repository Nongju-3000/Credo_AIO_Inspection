package com.credo.credo_aio_inspection.ble

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.credo.credo_aio_inspection.utils.ScanExceptionHandler
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.*
import com.polidea.rxandroidble2.exceptions.BleCharacteristicNotFoundException
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import com.polidea.rxandroidble2.exceptions.BleScanException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import java.net.SocketException
import java.util.*
import java.util.concurrent.TimeUnit

enum class CMD(val v: Int){
    NEW_RANGE(999), PRACTICE(243), EVALUATION(244), READY(241), CALL(242), MAGNET(192), CALIBRATION(176), CALIBRATION_MAGNET(186), POWER_OFF(255), BLE_TEST(221);
    companion object{
        fun from(i: Int): CMD? = values().first{ it.v == i }
    }
}

enum class PLAYMODE(val p: Int){
    NONE(0), PRACTICE(1), EVALUATION(2);
}

enum class AIOMODE(val m: Int){
    NONE(0), HANDS(1), AIO(2), CPR(3);
    companion object{
        fun from(i: Int): AIOMODE? = values().first{ it.m == i }
    }
}

enum class Gatt_UUID(val id: UUID){
    service(UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")),
    char_position(UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")),
    char_bletest(UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")),
    char_breath(UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")),
    char_depth(UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB")),
    char_tx(UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")),
    descriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
}
enum class Action(val str: String){
    SEND_ANGLE("action_getAngle"),
    SEND_DEPTH("action_getDepth"),
    SEND_BATTERY("action_getBattery"),
    SEND_CONNECTION("action_finishConnection"),
    SEND_STATUS("action_STATUS"),
    SEND_READY("action_ready"),
    SEND_CALL("action_call"),
    SEND_FINISHWORKER("Action_worker"),
    SEND_START("action_start");

    companion object{
        fun from(s: String): Action? = values().first{ it.str == s}
    }
}

enum class DataType(val v: Int) {
    Depth(0xf1), Angle(0xf2), Position(0xf3), Breath(0xf4), Test(0xf5);

    companion object {
        fun build(v: Int): DataType? = DataType.values().first { it.v == v }
    }
}

enum class BLEAction(val str: String) {
    ScanControll("action_startScann"),
    ConnectionControl("action_connectionControl"),
    ConnectionMultiControl("action,connectionMultiControl"),
    SendData("action_sendData"),
    RangeChanged("action_rangeChanged"),
    SendMultiData("action_sendMultiData"),
    ModeChange("action_modeChange");
    companion object {
        fun from(s: String): BLEAction? = BLEAction.values().first { it.str == s }
    }
}

interface GattInterface {
    fun bleNotSupport(reason: String)
    fun bluetoothPower(isOn: Boolean)
    fun bleScanning(started: Boolean)
    fun bleScanfound(dev: MutableList<BleDevice>)
    fun connectionStatus(index: Int, device: BleDevice, to: RxBleConnection.RxBleConnectionState)
    fun datReceived(mac: String, type: DataType, value: Number)
}

class GattService: Service(){
    companion object{
        private const val TAG = "GattManager"

        val extras = (0..10).map{ "extra$it" }

        fun scanControl(context: Context, start: Boolean){
            Log.e(TAG, "scanControll: $start")
            val sender = Intent(context, GattService::class.java)
            sender.action = BLEAction.ScanControll.str
            sender.putExtra(extras[0], start)
            Log.e(TAG, "scanControll: ${sender.action}")
            context.startService(sender)
        }
        fun connectionControl(context: Context, connect: Boolean, m: String?, index: Int){
            val sender = Intent(context, GattService::class.java)
            sender.action = BLEAction.ConnectionControl.str
            sender.putExtra(extras[0], connect)
            m?.let{
                sender.putExtra(extras[1], m)
                sender.putExtra(extras[2], index)
            }
            context.startService(sender)
        }
        fun connectionMultiControl(context: Context, connect: Boolean, macs: ArrayList<String>?){
            val sender = Intent(context, GattService::class.java)
            sender.action = BLEAction.ConnectionMultiControl.str
            sender.putExtra(extras[0], connect)
            if(macs != null) {
                sender.putExtra(extras[1], macs)
            }
            context.startService(sender)
        }
        fun sendData(context: Context, dat: Int, mac: String){
            val sender = Intent(context, GattService::class.java)
            sender.action = BLEAction.SendData.str
            sender.putExtra(extras[0], dat)
            sender.putExtra(extras[1], mac)
            context.startService(sender)
        }
        fun sendMultiData(context: Context, dat: Int){
            val sender = Intent(context, GattService::class.java)
            sender.action = BLEAction.SendMultiData.str
            sender.putExtra(extras[0], dat)
            context.startService(sender)
        }
        fun rangeChanged(context: Context, min: Int, max: Int){
            val sender = Intent(context, GattService::class.java)
            sender.action = BLEAction.RangeChanged.str
            sender.putExtra(extras[0], min)
            sender.putExtra(extras[0], max)
            context.startService(sender)
        }
        fun modeChange(context: Context, aioMode: Int){
            val sender = Intent(context, GattService::class.java)
            sender.action = BLEAction.ModeChange.str
            sender.putExtra(extras[0], aioMode)
            context.startService(sender)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {return null}
    override fun onUnbind(intent: Intent?): Boolean {
        gattManager.disconnect()
        return super.onUnbind(intent)
    }

    private lateinit var bus: Bus
    private lateinit var gattManager: GattManager
    override fun onCreate(){
        super.onCreate()
        bus = Bus.getSharedBus(this)
        gattManager = GattManager(this, bus.backgroundInterface)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let{
            Log.e(TAG, "onStartCommand: $it")
            BLEAction.from(it)?.let{ action ->
                when(action){
                    BLEAction.ScanControll ->{
                        Log.e(TAG, "onStartCommand: ${intent.getBooleanExtra(extras[0], false)}")
                        val start = intent.getBooleanExtra(extras[0], false)
                        if(start) gattManager.startScanning()
                        else gattManager.stopScanning()
                    }
                    BLEAction.ConnectionControl ->{
                        val connect = intent.getBooleanExtra(extras[0], false)
                        val m = intent.getStringExtra(extras[1])
                        val index = intent.getIntExtra(extras[2], -1)
                        if (connect) m?.let { mac -> gattManager.connect(mac, index) }
                        else {}
                    }
                    BLEAction.ConnectionMultiControl ->{
                        val connect = intent.getBooleanExtra(extras[0], false)
                        val ms = intent.getStringArrayListExtra(extras[1])
                        if (connect) ms?.let { macs -> gattManager.connect(macs) }
                        else gattManager.disconnect()
                    }
                    BLEAction.SendData ->{
                        val value = intent.getIntExtra(extras[0], 0)
                        val mac = intent.getStringExtra(extras[1])
                        gattManager.sendData(mac!!, value)
                    }
                    BLEAction.SendMultiData ->{
                        val value = intent.getIntExtra(extras[0], 0)
                        gattManager.sendDatas(value)
                    }
                    BLEAction.RangeChanged ->{
                        val min = intent.getIntExtra(extras[0], 50)
                        val max = intent.getIntExtra(extras[1], 60)
                        gattManager.changeRange(min, max)
                    }
                    BLEAction.ModeChange ->{
                        val aioMode = intent.getIntExtra(extras[0], 0)
                        gattManager.modeChange(aioMode)
                    }
                }
            }
        }
        return START_NOT_STICKY
    }
}

class GattManager(private val context: Context, private val gattInterface: GattInterface) {
    companion object{
        private const val TAG = "GattManager"
    }
    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var rxBleClient: RxBleClient?
    private val mRxBleConnections = ArrayList<RxBleConnection?>()
    private val compositeList = ArrayList<CompositeDisposable>()
    private val disconnectTriggerSubject = PublishSubject.create<Boolean>()
    private var devices = ArrayList<BleDevice?>()
    private val handler = Handler(Looper.getMainLooper())
    private var scanDisposable: Disposable? = null
    private var connectionList = ArrayList<String>()
    private val min_list = ArrayList<Int>()
    private val max_list = ArrayList<Int>()
    private var aioMode = AIOMODE.NONE

    init{
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        rxBleClient = RxBleClient.create(context)

        for(i in 0 until 6){
            mRxBleConnections.add(null)
            devices.add(null)
            compositeList.add(CompositeDisposable())
        }

        RxBleClient.updateLogOptions(
            LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        )
        RxJavaPlugins.setErrorHandler { e: Throwable ->
            if (e is UndeliverableException) {
                Log.e(TAG, e.message!!)
            }
            if (e is IOException || e is SocketException) {
                Log.e(TAG, e.message!!)
            }
            if (e is InterruptedException) {
                Log.e(TAG, e.message!!)
            }
            if (e is NullPointerException || e is IllegalArgumentException) {
                // that's likely a bug in the application
                Log.e(TAG, e.message!!)
            }
            if (e is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Log.e(TAG, e.message!!)
            }
            Log.e(
                "Undeliverable exception received, not sure what to do",
                e.message!!, e.cause
            )
        }
    }

    fun modeChange(aioMode: Int){
        when(aioMode){
            0 -> this.aioMode = AIOMODE.NONE
            1 -> this.aioMode = AIOMODE.HANDS
            2 -> this.aioMode = AIOMODE.AIO
            3 -> this.aioMode = AIOMODE.CPR
        }
    }

    fun startScanning(){
        if(!isScanning()) {
            Log.e(TAG, "startScanning")
            scanDisposable = rxBleClient?.scanBleDevices(
                com.polidea.rxandroidble2.scan.ScanSettings.Builder()
                    .setScanMode(com.polidea.rxandroidble2.scan.ScanSettings.SCAN_MODE_BALANCED)
                    .setCallbackType(com.polidea.rxandroidble2.scan.ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                    .build(),
                com.polidea.rxandroidble2.scan.ScanFilter.Builder()
                    .build()
            )
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(
                    {
                        if (it.bleDevice.macAddress != null) {
                            val mac = it.bleDevice.macAddress
                            if (it.bleDevice.name != null) {
                                val name = it.bleDevice.name
                                gattInterface.bleScanfound(mutableListOf(BleDevice(mac, name!!)))
                            }
                        }
                    }
                ) { throwable: Throwable? ->
                    if (throwable != null) {
                        this.onScanFailure(throwable)
                    }
                }
        }
    }
    private fun onScanFailure(throwable: Throwable){
        if (throwable is BleScanException){
            Log.e(TAG, "scan fail ${throwable.reason}")
            ScanExceptionHandler.handleException(context, throwable)
        }
    }
    private fun dispose(){
        scanDisposable = null
    }
    private fun isScanning(): Boolean{
        return scanDisposable != null
    }
    fun stopScanning(){
        scanDisposable?.dispose()
        scanDisposable = null
    }


    fun connect(mac: String, index: Int){
        try {
            val bleDevice = rxBleClient?.getBleDevice(mac)
            var device: BleDevice? = null
            val connectionObservable =
                bleDevice?.establishConnection(false)?.takeUntil(disconnectTriggerSubject)
                    ?.compose(ReplayingShare.instance())
            compositeList[index].clear()

            val connectionDisposable = connectionObservable
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.doOnError{
                    if(it is NullPointerException){
                        Log.e(TAG, it.message.toString())
                    }
                }
                ?.subscribe(
                    { connection ->
                        connection.requestConnectionPriority(
                            BluetoothGatt.CONNECTION_PRIORITY_HIGH,
                            500,
                            TimeUnit.MILLISECONDS
                        )
                        device = BleDevice(mac, bleDevice.name!!)
                        devices[index] = device

                        mRxBleConnections[index] = connection
                        device!!.mRxBleConnection = connection
                        val stateDisposable: Disposable =
                            bleDevice.observeConnectionStateChanges()
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { newState ->
                                    if (newState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                                        gattInterface.connectionStatus(
                                            index,
                                            device!!,
                                            newState
                                        )
                                    }
                                }
                        compositeList[index].add(stateDisposable)
                        broadCastRxConnectionUpdate(bleDevice, device, index)
                    },
                    this::onConnectionFailure,
                    this::onConnectionFinished,
                )
            compositeList[index].add(connectionDisposable!!)
        }catch (e: java.lang.NullPointerException){
            e.printStackTrace()
        }
    }

    fun connect(macs: ArrayList<String>){
        connectionList.addAll(macs)
        handler.postDelayed(connectionRunnable, 0)
    }

    private fun broadCastRxConnectionUpdate(rxBledevice: RxBleDevice, device: BleDevice?, index: Int){
        val rxBleConnection = device?.mRxBleConnection
        if (this.aioMode == AIOMODE.CPR) {
            if (rxBledevice.name?.contains("CPR-BA")!!) {
                rxEnableNotification(rxBledevice, device, rxBleConnection!!, index)
            } else {
                val disposable = rxBleConnection?.setupNotification(Gatt_UUID.char_position.id)
                    ?.doOnError {
                        if (it is BleCharacteristicNotFoundException) {
                            handler.postDelayed({
                                disconnect(device.mac, index)
                            }, 1000)
                        }
                    }
                    ?.doOnSubscribe { _ ->
                        rxEnableNotification(rxBledevice, device, rxBleConnection, index)
                    }?.flatMap { notificationObservable ->
                        notificationObservable
                    }?.subscribe { bytes ->
                        //positionReceived(rxBledevice, bytes)
                        testReceived(rxBledevice, bytes)
                        this::onNotificationSetupFailure
                    }
                compositeList[index].add(disposable!!)
            }
        } else {
            val disposable = rxBleConnection?.setupNotification(Gatt_UUID.char_position.id)
                ?.doOnError {
                    if (it is BleCharacteristicNotFoundException) {
                        handler.postDelayed({
                            disconnect(device.mac, index)
                        }, 1000)
                    }
                }
                ?.doOnSubscribe { _ ->
                    Log.e(TAG, "broad noti finished in $index")
                    rxEnableNotification(rxBledevice, device, rxBleConnection, index)
                }
                ?.flatMap { notificationObservable ->
                    notificationObservable
                }?.subscribe { bytes ->
                    testReceived(rxBledevice, bytes)
                    this::onNotificationSetupFailure
                }
            compositeList[index].add(disposable!!)
        }
    }

    private fun rxEnableNotification(rxBleDevice: RxBleDevice, device: BleDevice?, rxBleConnection: RxBleConnection, index: Int){
        var disposable: Disposable? = null

        if (!device?.isCharDRegistereds!!) {
            device.isCharDRegistereds = true
            disposable = rxBleConnection.setupNotification(Gatt_UUID.char_breath.id)
                ?.doOnError {
                    if (it is BleCharacteristicNotFoundException) {
                        handler.postDelayed({
                            disconnect(device.mac, index)
                        }, 1000)
                    }
                }
                ?.doOnSubscribe { _ ->
                    if (rxBleDevice.name?.contains("AIO")!!) {
                        rxEnableNotification(rxBleDevice, device, rxBleConnection, index)
                    } else {
                        Log.e(TAG, "rx noti finished in $index")
                        if (device.mRxBleConnection != null) {
                            device.connectionStatus =
                                RxBleConnection.RxBleConnectionState.CONNECTED
                            writeCharacteristic(CMD.READY.v.toString(), index)
                        }else{
                            Log.e(TAG, "rxBleConnection is null?")
                        }
                    }
                }
                ?.flatMap { notificationObservable ->
                    notificationObservable
                }?.subscribe { bytes ->
                    breathReceived(rxBleDevice, bytes)
                    this::onNotificationSetupFailure
                }
        } else if (!device.isCharARegistereds) {
            device.isCharARegistereds = true
            disposable = rxBleConnection.setupNotification(Gatt_UUID.char_depth.id)
                ?.doOnError {
                    if (it is BleCharacteristicNotFoundException) {
                        handler.postDelayed({
                            disconnect(device.mac, index)
                        }, 1000)
                    }
                }
                ?.doOnSubscribe { _ ->
                    if (device.mRxBleConnection != null)
                        writeCharacteristic(CMD.READY.v.toString(), index)
                }
                ?.flatMap { notificationObservable ->
                    notificationObservable
                }?.subscribe { bytes ->
                    depthReceived(rxBleDevice, bytes)
                    this::onNotificationSetupFailure
                }
        }
        compositeList[index].add(disposable!!)

    }

    private fun positionReceived(device: RxBleDevice, bytes: ByteArray){
        val byte = bytes.first()
        val value = byte.toUByte().toInt()

        if(device.name?.contains("AIO")!!){
            gattInterface.datReceived(device.macAddress, DataType.Position, value)
        }else{
            gattInterface.datReceived(device.macAddress, DataType.Depth, value)
        }
    }
    private fun breathReceived(device: RxBleDevice, bytes: ByteArray){
        val byte = bytes.first()
        val value = byte.toUByte().toInt()

        if(device.name?.contains("AIO")!!){
            gattInterface.datReceived(device.macAddress, DataType.Breath, value)
        }else{
            gattInterface.datReceived(device.macAddress, DataType.Angle, value)
        }
    }
    private fun depthReceived(device: RxBleDevice, bytes: ByteArray){
        val byte = bytes.first()
        val value = byte.toUByte().toInt()

        gattInterface.datReceived(device.macAddress, DataType.Depth, value)
    }
    private fun testReceived(device: RxBleDevice, bytes: ByteArray){
        val byte = bytes.first()
        val value = byte.toUByte().toInt()
        gattInterface.datReceived(device.macAddress, DataType.Test, value)
    }

    private val connectionRunnable = object: Runnable{
        override fun run() {
            if(connectionList.size > 0){
                connectionList.forEach{
                }
                val mac = connectionList[0]
               // connect(mac)
                connectionList.removeAt(0)
                handler.postDelayed(this, 500)
            }else{
                handler.removeCallbacks(this)
            }
        }
    }

    private fun disconnect(mac: String, index: Int){
        try {
            if(devices[index] != null) {
                gattInterface.connectionStatus(
                    index,
                    devices[index]!!,
                    RxBleConnection.RxBleConnectionState.DISCONNECTED
                )
            }
            compositeList[index].clear()
        }catch (e: java.lang.NullPointerException){
            e.printStackTrace()
        }
    }

    fun disconnect(){
        try {
            Log.e(TAG, "disconnect call")
            devices.clear()
            mRxBleConnections.clear()
            for (i in 0 until 6) {
                compositeList[i].clear()
                mRxBleConnections.add(null)
                devices.add(null)
            }
        }catch (e: java.lang.NullPointerException){
            e.printStackTrace()
        }
    }

    fun sendData(mac: String, value: Int){
        var index = -1
        devices.forEachIndexed { i, d ->
            if(d?.mac == mac){
                index = i
            }
        }
        handler.postDelayed({
            if(index != -1) {
                writeCharacteristic(value.toString(), index)
            }else{
                Log.e(TAG,"index is -1")
            }
        }, 100)
    }

    fun sendDatas(value: Int){
        devices.forEach{
            handler.postDelayed({
                if(it?.mac != null) {
                    sendData(it.mac, value)
                }
            }, 100)
        }
    }

    fun changeRange(min: Int, max: Int){
        min_list.clear();
        min_list.clear();
        for(i in 0..devices.size){
            min_list.add(min);
            max_list.add(max);
        }

    }

    private fun onNotificationSetupFailure(throwable: Throwable){
        Log.e(TAG, "Notification setup Failure ${throwable.message} , ${throwable.cause}")
    }

    private fun onConnectionFinished() {
        Log.e(TAG, "onConnectionFinished?")
    }

    private fun onConnectionFailure(throwable: Throwable) {

    }

    private fun isAvail(): Boolean{
        if(bluetoothAdapter == null){
            gattInterface.bleNotSupport("This device is not support BluetoothLE service")
            return false
        }
        if(!bluetoothAdapter!!.isEnabled){
            gattInterface.bleNotSupport("Bluetooth is off")
            return false
        }
        return true
    }

    private fun writeCharacteristic(data: String, index: Int){
        try{
            val rxBleConnection = mRxBleConnections[index]
            if(rxBleConnection != null) {
                var hex = Integer.toHexString(data.toInt())
                if(devices[index]?.name != null){
                    if(devices[index]!!.name.contains("AIO")){
                        if(data == CMD.EVALUATION.v.toString()){
                            hex = Integer.toHexString(CMD.PRACTICE.v)
                        }
                    }
                }
                if (hex.length == 1) {
                    hex = "0$hex"
                }
                val sender = hex.hexToBytes()
                val disposable = rxBleConnection.writeCharacteristic(Gatt_UUID.char_tx.id, sender)
                    .subscribe({
                        if(data == CMD.READY.v.toString()){
                            gattInterface.connectionStatus(index, devices[index]!!, RxBleConnection.RxBleConnectionState.CONNECTED)
                        }
                    },
                        { throwable ->
                            try {
                                Log.e(TAG, throwable.message.toString())
                                if (devices[index] != null) {
                                    if (devices[index]?.mac != null) {
                                        handler.postDelayed({
                                            disconnect(devices[index]?.mac!!, index)
                                        }, 1000)
                                    }
                                }
                            }catch (e: java.lang.NullPointerException){
                                e.printStackTrace()
                            }
                        })

                compositeList[index].add(disposable)
            }else{
                Log.e(TAG, "rxBleConnection is null in $index")
            }
            if(data == CMD.NEW_RANGE.v.toString()){
                handler.post{
                    val min = min_list[index]
                    handler.postDelayed({
                        if(min != 0){
                            min_list[index] = 0
                            writeCharacteristic(min.toString(), index)
                        }
                    }, 100)
                }
            }

            if(data.toInt() in 30..70){
                if(min_list[index] == 0 && max_list[index] != 0){
                    handler.postDelayed({
                        val max = max_list[index]
                        max_list[index] = 0
                        writeCharacteristic(max.toString(), index)
                    }, 100)
                }
            }

        }catch(ex: Exception){
            Log.e(TAG, "write exception = ${ex.message}")
        }
    }

    private fun String.hexToBytes(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun isConnected(mac: String): Boolean{
        return rxBleClient?.getBleDevice(mac)?.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED
    }
}