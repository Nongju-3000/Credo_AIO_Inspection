package com.credo.credo_aio_inspection.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.MediaCodec.MetricsConstants.MODE
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleConnection.RxBleConnectionState
import java.util.ConcurrentModificationException

class Bus(private val context: Context) {
    companion object{
        private const val TAG = "BUS"
        private var bus: Bus? = null

        fun getSharedBus(context: Context): Bus{
            if(this.bus == null) bus = Bus(context)
            return this.bus!!
        }
        private var interfaceMap = mutableMapOf<String, GattInterface>()
        fun register(tag: String, gattInterface: GattInterface){
            interfaceMap[tag] = gattInterface
        }
        fun unRegister(tag: String){
            interfaceMap.remove(tag)
        }
        private var io = object: GattInterface{
            override fun bleNotSupport(reason: String) {
                interfaceMap.values.forEach{
                    it.bleNotSupport(reason)
                }
            }

            override fun bluetoothPower(isOn: Boolean) {
                interfaceMap.values.forEach {
                    it.bluetoothPower(isOn)
                }
            }

            override fun bleScanning(started: Boolean) {
                interfaceMap.values.forEach {
                    it.bleScanning(started)
                }
            }

            override fun bleScanfound(dev: MutableList<BleDevice>) {
                interfaceMap.values.forEach {
                    it.bleScanfound(dev)
                }
            }

            override fun connectionStatus(index: Int, device: BleDevice, to: RxBleConnectionState) {
                interfaceMap.values.forEach {
                    it.connectionStatus(index, device, to)
                }
            }

            override fun datReceived(mac: String, type: DataType, value: Number) {
                interfaceMap.values.forEach {
                    it.datReceived(mac, type, value)
                }
            }
        }
    }
    private var isScanning = false
    var foundDevices = mutableListOf<BleDevice>()
    var devices = ArrayList<Device?>()
    var aioMode = AIOMODE.NONE
    private var beepHandler: Handler? = null
    private var connectionHandler: Handler? = null

    val sendList = ArrayList<Int>()
    init{
        for(i in 0 until 6) {
            devices.add(null)
            sendList.add(CMD.EVALUATION.v)
        }
    }

    fun startScanning() = GattService.scanControl(context, true)
    fun stopScanning() {
        foundDevices.clear()
        GattService.scanControl(context, false)
    }

    fun connect(macs: ArrayList<String>){
        GattService.connectionMultiControl(context, true, macs)
    }

    fun connect(mac: String, index: Int){
        GattService.connectionControl(context, true, mac, index)
    }

    fun disconnect(){
        devices.clear()
        for(i in 0 until 6) {
            devices.add(null)
        }
        foundDevices.clear()
        GattService.connectionMultiControl(context, false, null)
    }

    fun send(dat: Int){
        GattService.sendMultiData(context, dat)
        if(dat == CMD.PRACTICE.v || dat == CMD.EVALUATION.v){
            devices.forEach{
                if(dat == CMD.PRACTICE.v) it?.playState = PLAYMODE.PRACTICE
                else it?.playState = PLAYMODE.EVALUATION
            }
        }
    }

    val sendHandler = Handler(Looper.getMainLooper())
    fun send(dat: Int, mac: String){
        if(dat == CMD.CALL.v){
            sendHandler.postDelayed(sendInterface, 0)
        }
        GattService.sendData(context, dat, mac)
    }

    val sendInterface = object: Runnable{
        override fun run() {

        }
    }

    fun rangeChange(min: Int, max: Int){
        GattService.rangeChanged(context, min, max)
    }

    fun setMode(aioMode: AIOMODE){
        if(this.aioMode != aioMode){
            disconnect()
        }
        this.aioMode = aioMode
        GattService.modeChange(context, aioMode.m)
    }

    fun changeStateToNone(){
        devices.forEach{
            it?.playState = PLAYMODE.NONE
        }
    }

    fun isConnected(mac: String): Boolean{
        devices.forEach{
            if(it?.mac == mac){
                if(it.connectionStatus == RxBleConnectionState.CONNECTED){
                    return true
                }
            }
        }
        return false
    }

    fun startReConnection(start: Boolean){
        if(start){
            if(connectionHandler == null){
                connectionHandler = Handler(Looper.getMainLooper())
                connectionHandler?.postDelayed(connectionRunnable, 500)
            }
        }else{
            if(connectionHandler != null) {
                connectionHandler?.removeCallbacks(connectionRunnable)
                connectionHandler = null
            }
        }
    }

    private val connectionRunnable = object: Runnable{
        override fun run() {
            try {
                if(connectionHandler != null) {
                    devices.forEachIndexed { index, device ->
                        if (device != null) {
                            if(!isConnected(device.mac)){
                                connect(device.mac, index)
                            }
                        }
                    }
                    connectionHandler?.postDelayed(this, 1000)
                }
            }catch(e: ConcurrentModificationException){
                Log.e(TAG, e.message.toString())
            }
        }
    }

    var backgroundInterface = object:GattInterface{
        override fun bleNotSupport(reason: String) {
            io.bleNotSupport(reason)
        }

        override fun bluetoothPower(isOn: Boolean) {
            io.bluetoothPower(isOn)
        }

        override fun bleScanning(started: Boolean) {
            isScanning = started
            if(started){
                foundDevices.clear()
            }
            io.bleScanning(started)
        }

        override fun bleScanfound(dev: MutableList<BleDevice>) {
            try{
                val d = dev.first()
                var filter = false
                foundDevices.forEach{
                    if(it.mac == d.mac){
                        filter = true
                    }
                }
                if(!filter){
                    foundDevices.add(d)
                    io.bleScanfound(foundDevices)
                }
            }catch (e: Exception){
                Log.e(TAG, "exception is called in blescanfound")}
        }

        override fun connectionStatus(index: Int, device: BleDevice, to: RxBleConnectionState) {
            Log.e(TAG, "status is changed to $to")
            if(devices[index] != null) {
                devices[index]?.connectionStatus = to
            }
            if(to == RxBleConnectionState.CONNECTED){
                if(devices[index] == null){
                    devices[index] = Device(device.mac, device.name)
                }

                if(devices[index]?.playState != PLAYMODE.NONE){
                    if(devices[index]?.playState == PLAYMODE.PRACTICE) send(CMD.PRACTICE.v, device.mac)
                    else send(CMD.EVALUATION.v, device.mac)
                }
            }

            io.connectionStatus(index, device, to)
        }

        override fun datReceived(mac: String, type: DataType, value: Number) {
            io.datReceived(mac, type, value)
        }
    }
    private var bpm: Int = 110


}