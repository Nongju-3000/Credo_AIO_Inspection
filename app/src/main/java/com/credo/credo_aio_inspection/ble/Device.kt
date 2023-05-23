package com.credo.credo_aio_inspection.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.polidea.rxandroidble2.RxBleConnection

class Device(val mac: String, val name: String){
    companion object{
        private const val TAG = "DEVICE"
    }
    var connectionStatus : RxBleConnection.RxBleConnectionState = RxBleConnection.RxBleConnectionState.DISCONNECTED
    var playState = PLAYMODE.NONE

    override fun equals(other: Any?): Boolean{
        try{
            return this.mac == (other as Device).mac
        }catch(e: Exception){}
        return false
    }
}

class BleDevice(val mac: String, val name: String){
    companion object{
        private const val TAG = "BleDevice"
    }
    var connectionStatus : RxBleConnection.RxBleConnectionState = RxBleConnection.RxBleConnectionState.DISCONNECTED
    var isCharDRegistereds = false
    var isCharARegistereds = false
    var mRxBleConnection: RxBleConnection? = null

    override fun equals(other: Any?): Boolean{
        try{
            return this.mac == (other as Device).mac
        }catch(e: Exception){}
        return false
    }
}