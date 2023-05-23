package com.credo.credo_aio_inspection

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.credo.credo_aio_inspection.ble.*
import com.polidea.rxandroidble2.RxBleConnection
import soup.neumorphism.NeumorphButton
import soup.neumorphism.NeumorphImageButton

class MainActivity : AppCompatActivity() {
    companion object{
        private const val TAG = "AIOModuleActivity"
    }
    private val DELAY_TIME = 190000L
    private val total_num = 36000f
    private lateinit var bus: Bus
    private var startFlag = false
    private var startFlag0 = false
    private var startFlag1 = false
    private var startFlag2 = false
    private var startFlag3 = false
    private var startFlag4 = false
    private var startFlag5 = false
    private var isConnected = false
    private lateinit var pref: SharedPreferences
    private var testcount0 = 0
    private var testcount1 = 0
    private var testcount2 = 0
    private var testcount3 = 0
    private var testcount4 = 0
    private var testcount5 = 0
    private var testlist0:ArrayList<Int?> = arrayListOf()
    private var testlist1:ArrayList<Int?> = arrayListOf()
    private var testlist2:ArrayList<Int?> = arrayListOf()
    private var testlist3:ArrayList<Int?> = arrayListOf()
    private var testlist4:ArrayList<Int?> = arrayListOf()
    private var testlist5:ArrayList<Int?> = arrayListOf()
    private lateinit var first_aio_text1: TextView
    private lateinit var first_aio_text2: TextView
    private lateinit var first_aio_text3: TextView
    private lateinit var first_aio_text4: TextView
    private lateinit var second_aio_text1: TextView
    private lateinit var second_aio_text2: TextView
    private lateinit var second_aio_text3: TextView
    private lateinit var second_aio_text4: TextView
    private lateinit var third_aio_text1: TextView
    private lateinit var third_aio_text2: TextView
    private lateinit var third_aio_text3: TextView
    private lateinit var third_aio_text4: TextView
    private lateinit var fourth_aio_text1: TextView
    private lateinit var fourth_aio_text2: TextView
    private lateinit var fourth_aio_text3: TextView
    private lateinit var fourth_aio_text4: TextView
    private lateinit var fifth_aio_text1: TextView
    private lateinit var fifth_aio_text2: TextView
    private lateinit var fifth_aio_text3: TextView
    private lateinit var fifth_aio_text4: TextView
    private lateinit var sixth_aio_text1: TextView
    private lateinit var sixth_aio_text2: TextView
    private lateinit var sixth_aio_text3: TextView
    private lateinit var sixth_aio_text4: TextView
    private lateinit var status_tv: TextView
    private lateinit var scan_btn: Button
    private lateinit var start_btn: Button
    private lateinit var first_aio_call: Button
    private lateinit var second_aio_call: Button
    private lateinit var third_aio_call: Button
    private lateinit var fourth_aio_call: Button
    private lateinit var fifth_aio_call: Button
    private lateinit var sixth_aio_call: Button
    private lateinit var handler: Handler
    private var device_btn01: NeumorphImageButton? = null
    private var device_btn02: NeumorphImageButton? = null
    private var device_btn03: NeumorphImageButton? = null
    private var device_btn04: NeumorphImageButton? = null
    private var device_btn05: NeumorphImageButton? = null
    private var device_btn06: NeumorphImageButton? = null
    private var mLeDeviceListAdapter: LeDeviceListAdapter? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager
    private var alertClickedMacList = ArrayList<String>()
    private var maclist = ArrayList<String?>()
    var first0 = 0
    var second0 = 0
    var third0 = 0
    var first1 = 0
    var second1 = 0
    var third1 = 0
    var first2 = 0
    var second2 = 0
    var third2 = 0
    var first3 = 0
    var second3 = 0
    var third3 = 0
    var first4 = 0
    var second4 = 0
    var third4 = 0
    var first5 = 0
    var second5 = 0
    var third5 = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        for(i in 0..5){
            maclist.add("-")
        }

        initialize()
        permissionCheck()

        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        bus = Bus.getSharedBus(this)
        bus.setMode(AIOMODE.AIO)
        Bus.register(TAG, gattInterface)

        handler = Handler(Looper.getMainLooper())

        pref = application.getSharedPreferences("Device", MODE_PRIVATE)

        showAlertDialog()

        scan_btn.setOnClickListener {
            showAlertDialog()
        }


        start_btn.setOnClickListener {
            startFlag = true
            reset()
            if (bus.isConnected(maclist[0]!!)) {
                bus.send(CMD.BLE_TEST.v, maclist[0]!!)
                startFlag0 = true
            }
            if (bus.isConnected(maclist[1]!!)) {
                bus.send(CMD.BLE_TEST.v, maclist[1]!!)
                startFlag1 = true
            }
            if (bus.isConnected(maclist[2]!!)) {
                bus.send(CMD.BLE_TEST.v, maclist[2]!!)
                startFlag2 = true
            }
            if (bus.isConnected(maclist[3]!!)) {
                bus.send(CMD.BLE_TEST.v, maclist[3]!!)
                startFlag3 = true
            }
            if (bus.isConnected(maclist[4]!!)) {
                bus.send(CMD.BLE_TEST.v, maclist[4]!!)
                startFlag4 = true
            }
            if (bus.isConnected(maclist[5]!!)) {
                bus.send(CMD.BLE_TEST.v, maclist[5]!!)
                startFlag5 = true
            }
        }

        first_aio_call.setOnClickListener {
            if (bus.isConnected(maclist[0]!!) && !startFlag0) {
                bus.send(CMD.CALL.v, maclist[0]!!)
            }
        }

        second_aio_call.setOnClickListener {
            if (bus.isConnected(maclist[1]!!) && !startFlag1) {
                bus.send(CMD.CALL.v, maclist[1]!!)
            }
        }

        third_aio_call.setOnClickListener {
            if (bus.isConnected(maclist[2]!!) && !startFlag2) {
                bus.send(CMD.CALL.v, maclist[2]!!)
            }
        }

        fourth_aio_call.setOnClickListener {
            if (bus.isConnected(maclist[3]!!) && !startFlag3) {
                bus.send(CMD.CALL.v, maclist[3]!!)
            }
        }

        fifth_aio_call.setOnClickListener {
            if (bus.isConnected(maclist[4]!!) && !startFlag4) {
                bus.send(CMD.CALL.v, maclist[4]!!)
            }
        }

        sixth_aio_call.setOnClickListener {
            if (bus.isConnected(maclist[5]!!) && !startFlag5) {
                bus.send(CMD.CALL.v, maclist[5]!!)
            }
        }
    }

    private fun reset(){
        status_tv.text = " "
        first_aio_text1.text = ""
        first_aio_text2.text = ""
        first_aio_text3.text = ""
        first_aio_text4.text = ""
        second_aio_text1.text = ""
        second_aio_text2.text = ""
        second_aio_text3.text = ""
        second_aio_text4.text = ""
        third_aio_text1.text = ""
        third_aio_text2.text = ""
        third_aio_text3.text = ""
        third_aio_text4.text = ""
        fourth_aio_text1.text = ""
        fourth_aio_text2.text = ""
        fourth_aio_text3.text = ""
        fourth_aio_text4.text = ""
        fifth_aio_text1.text = ""
        fifth_aio_text2.text = ""
        fifth_aio_text3.text = ""
        fifth_aio_text4.text = ""
        sixth_aio_text1.text = ""
        sixth_aio_text2.text = ""
        sixth_aio_text3.text = ""
        sixth_aio_text4.text = ""
        testcount0 = 0
        testcount1 = 0
        testcount2 = 0
        testcount3 = 0
        testcount4 = 0
        testcount5 = 0
        testlist0.clear()
        testlist1.clear()
        testlist2.clear()
        testlist3.clear()
        testlist4.clear()
        testlist5.clear()
    }

    private fun showAlertDialog(){
        bus.startReConnection(false)
        isConnected = false

        alertClickedMacList.clear()
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.alert_layout, null)
        builder.setView(view)

        val close_btn = view.findViewById<Button>(R.id.close_btn)
        val connect = view.findViewById<Button>(R.id.connect)
        val scan_btn = view.findViewById<Button>(R.id.scan_btn)
        val list_device = view.findViewById<ListView>(R.id.List_device)
        val mac_address_01 = view.findViewById<TextView>(R.id.mac_address_01)
        val mac_address_02 = view.findViewById<TextView>(R.id.mac_address_02)
        val mac_address_03 = view.findViewById<TextView>(R.id.mac_address_03)
        val mac_address_04 = view.findViewById<TextView>(R.id.mac_address_04)
        val mac_address_05 = view.findViewById<TextView>(R.id.mac_address_05)
        val mac_address_06 = view.findViewById<TextView>(R.id.mac_address_06)
        val pad01 = view.findViewById<TextView>(R.id.band01)
        val pad02 = view.findViewById<TextView>(R.id.band02)
        val pad03 = view.findViewById<TextView>(R.id.band03)
        val pad04 = view.findViewById<TextView>(R.id.band04)
        val pad05 = view.findViewById<TextView>(R.id.band05)

        pad01.text = getString(R.string.pad01)
        pad02.text = getString(R.string.pad02)
        pad03.text = getString(R.string.pad03)
        pad04.text = getString(R.string.pad04)
        pad05.text = getString(R.string.pad05)

        bus.disconnect()
        val dialog = builder.create()

        mLeDeviceListAdapter = LeDeviceListAdapter()
        mLeDeviceListAdapter?.clear()
        list_device.adapter = mLeDeviceListAdapter
        bus.startScanning()

        var count = 0
        list_device.setOnItemClickListener{ _, _, position, _ ->
            count += 1
            when(count){
                1 ->{
                    val mac = mLeDeviceListAdapter!!.getDevice(position).mac
                    alertClickedMacList.add(mac)
                    mac_address_01.text = mac
                    mLeDeviceListAdapter!!.remove(position)
                    mLeDeviceListAdapter!!.notifyDataSetChanged()
                }
                2 ->{
                    val mac = mLeDeviceListAdapter!!.getDevice(position).mac
                    alertClickedMacList.add(mac)
                    mac_address_02.text = mac
                    mLeDeviceListAdapter!!.remove(position)
                    mLeDeviceListAdapter!!.notifyDataSetChanged()
                }
                3 ->{
                    val mac = mLeDeviceListAdapter!!.getDevice(position).mac
                    alertClickedMacList.add(mac)
                    mac_address_03.text = mac
                    mLeDeviceListAdapter!!.remove(position)
                    mLeDeviceListAdapter!!.notifyDataSetChanged()
                }
                4 ->{
                    val mac = mLeDeviceListAdapter!!.getDevice(position).mac
                    alertClickedMacList.add(mac)
                    mac_address_04.text = mac
                    mLeDeviceListAdapter!!.remove(position)
                    mLeDeviceListAdapter!!.notifyDataSetChanged()
                }
                5 ->{
                    val mac = mLeDeviceListAdapter!!.getDevice(position).mac
                    alertClickedMacList.add(mac)
                    mac_address_05.text = mac
                    mLeDeviceListAdapter!!.remove(position)
                    mLeDeviceListAdapter!!.notifyDataSetChanged()
                }
                6 ->{
                    val mac = mLeDeviceListAdapter!!.getDevice(position).mac
                    alertClickedMacList.add(mac)
                    mac_address_06.text = mac
                    mLeDeviceListAdapter!!.remove(position)
                    mLeDeviceListAdapter!!.notifyDataSetChanged()
                }
            }
        }

        connect.setOnClickListener {
            permissionCheck()
            if(checkPermission()) {
                bus.stopScanning()
                if (mac_address_01.text.toString() != "") {
                    pref.edit().putString("device_01_aio", mac_address_01.text.toString()).apply()
                } else {
                    pref.edit().putString("device_01_aio", "-").apply()
                }
                if (mac_address_02.text.toString() != "") {
                    pref.edit().putString("device_02_aio", mac_address_02.text.toString()).apply()
                } else {
                    pref.edit().putString("device_02_aio", "-").apply()
                }
                if (mac_address_03.text.toString() != "") {
                    pref.edit().putString("device_03_aio", mac_address_03.text.toString()).apply()
                } else {
                    pref.edit().putString("device_03_aio", "-").apply()
                }
                if (mac_address_04.text.toString() != "") {
                    pref.edit().putString("device_04_aio", mac_address_04.text.toString()).apply()
                } else {
                    pref.edit().putString("device_04_aio", "-").apply()
                }
                if (mac_address_05.text.toString() != "") {
                    pref.edit().putString("device_05_aio", mac_address_05.text.toString()).apply()
                } else {
                    pref.edit().putString("device_05_aio", "-").apply()
                }
                if (mac_address_06.text.toString() != "") {
                    pref.edit().putString("device_06_aio", mac_address_06.text.toString()).apply()
                } else {
                    pref.edit().putString("device_06_aio", "-").apply()
                }

                dialog.dismiss()
                showScanDialog()
            }else{
                return@setOnClickListener
            }
        }

        scan_btn.setOnClickListener {
            permissionCheck()
            if(checkPermission()) {
                bus.disconnect()
                bus.stopScanning()
                dialog.dismiss()
                showAlertDialog()
            }else{
                return@setOnClickListener
            }
        }
        close_btn.setOnClickListener {
            bus.stopScanning()
            dialog.dismiss()
            showScanDialog()
        }

        val width = resources.displayMetrics.widthPixels * 0.9f
        val height = resources.displayMetrics.heightPixels * 0.7f
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        dialog.window?.setLayout(width.toInt(), height.toInt())
    }

    private fun showScanDialog(){
        bus.startReConnection(false)
        mLeDeviceListAdapter = LeDeviceListAdapter()
        mLeDeviceListAdapter?.mLeDevices?.clear()
        bus.startScanning()
        val macList = ArrayList<String?>()
        for(i in 0 until 6){
            macList.add("-")
        }
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.band_scan_layout, null)
        builder.setView(view)

        val band_dialog_reset = view.findViewById<NeumorphButton>(R.id.band_dialog_reset)
        val band_dialog_layout = view.findViewById<NeumorphButton>(R.id.band_dialog_layout)
        device_btn01 = view.findViewById(R.id.device_btn01)
        device_btn02 = view.findViewById(R.id.device_btn02)
        device_btn03 = view.findViewById(R.id.device_btn03)
        device_btn04 = view.findViewById(R.id.device_btn04)
        device_btn05 = view.findViewById(R.id.device_btn05)
        device_btn06 = view.findViewById(R.id.device_btn06)
        val band_scan_reset = view.findViewById<NeumorphButton>(R.id.band_scan_reset)
        val scanProgress = view.findViewById<ProgressBar>(R.id.scanProgressBar)
        val scanImageView = view.findViewById<ImageView>(R.id.scanImageView)
        scanProgress.visibility = View.VISIBLE

        device_btn01!!.setImageResource(R.drawable.cpr_off)
        device_btn02!!.setImageResource(R.drawable.cpr_off)
        device_btn03!!.setImageResource(R.drawable.cpr_off)
        device_btn04!!.setImageResource(R.drawable.cpr_off)
        device_btn05!!.setImageResource(R.drawable.cpr_off)
        device_btn06!!.setImageResource(R.drawable.cpr_off)

        val dialog = builder.create()

        val device01 = pref.getString("device_01_aio", "-")!!
        val device02 = pref.getString("device_02_aio", "-")!!
        val device03 = pref.getString("device_03_aio", "-")!!
        val device04 = pref.getString("device_04_aio", "-")!!
        val device05 = pref.getString("device_05_aio", "-")!!
        val device06 = pref.getString("device_06_aio", "-")!!

        var count = 0

        if(device01 != "-"){
            macList[0] = device01
            count += 1
        }
        if(device02 != "-"){
            macList[1] = device02
            count += 1
        }
        if(device03 != "-"){
            macList[2] = device03
            count += 1
        }
        if(device04 != "-"){
            macList[3] = device04
            count += 1
        }
        if(device05 != "-"){
            macList[4] = device05
            count += 1
        }
        if(device06 != "-"){
            macList[5] = device06
            count += 1
        }

        var connectionCheck = false

        var checkCount = 0
        val connectThread = Thread{
            try {
                while (!connectionCheck) {
                    if(!Thread.currentThread().isInterrupted) {
                        try{
                            Thread.sleep(500)
                        }catch(e: InterruptedException){
                            Thread.currentThread().interrupt()
                        }
                    }
                    if(connectionCheck){
                        break
                    }

                    connectionCheck = connectionCheck(macList, count)
                    if (connectionCheck) {
                        handler.post{
                            bus.stopScanning()
                            scanProgress.visibility = View.GONE
                            scanImageView.visibility = View.VISIBLE
                        }
                    }

                    val foundDevices = mLeDeviceListAdapter!!.mLeDevices
                    checkCount += 1

                    if (checkCount % 10 == 0) {
                        macList.forEachIndexed { index, mac ->
                            handler.postDelayed({
                                if (mac != null) {
                                    if (!bus.isConnected(mac)) {
                                        if(foundDevices.size > 0) {
                                            foundDevices.forEach {
                                                if (it.mac == mac) {
                                                    bus.connect(mac, index)
                                                }
                                            }
                                        }else{
                                            if(alertClickedMacList.size != 0){
                                                alertClickedMacList.forEach{
                                                    if(it == mac){
                                                        bus.connect(mac, index)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }, 0)
                        }
                    }
                    // in 45 secs, nothing connected, change the view to show alertdialog
                    if (checkCount == 90) {
                        if (!isConnected) {
                            handler.postDelayed({
                                val popupBuilder = AlertDialog.Builder(this)
                                val popupView =
                                    layoutInflater.inflate(R.layout.dialog_reconnect, null)

                                val confirmBtn = popupView.findViewById<Button>(R.id.confirmBtn)

                                connectionCheck = true
                                popupBuilder.setView(popupView)
                                val popupDialog = popupBuilder.create()
                                handler.post{
                                    bus.stopScanning()
                                    scanProgress.visibility = View.GONE
                                    scanImageView.visibility = View.VISIBLE
                                }
                                confirmBtn.setOnClickListener {
                                    bus.stopScanning()
                                    checkCount = 0
                                    connectionCheck = true
                                    popupDialog.dismiss()
                                }

                                popupDialog.setCancelable(false)
                                popupDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                popupDialog.show()
                            }, 0)
                            break
                        }
                    }
                    if (checkCount == 60) {
                        if (isConnected) {
                            connectionCheck = true
                            handler.postDelayed({
                                bus.stopScanning()
                                scanProgress.visibility = View.GONE
                                scanImageView.visibility = View.VISIBLE
                            }, 0)
                        } else {
                            handler.postDelayed({
                                val popupBuilder = AlertDialog.Builder(this)
                                val popupView =
                                    layoutInflater.inflate(R.layout.dialog_reconnect, null)

                                val confirmBtn = popupView.findViewById<Button>(R.id.confirmBtn)

                                popupBuilder.setView(popupView)
                                val popupDialog = popupBuilder.create()
                                connectionCheck = true
                                handler.post{
                                    bus.stopScanning()
                                    scanProgress.visibility = View.GONE
                                    scanImageView.visibility = View.VISIBLE
                                }
                                confirmBtn.setOnClickListener {
                                    bus.stopScanning()
                                    checkCount = 0
                                    popupDialog.dismiss()
                                }

                                popupDialog.setCancelable(false)
                                popupDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                popupDialog.show()
                            }, 0)
                        }
                        break
                    }
                }
            }catch(e: ConcurrentModificationException){
                Log.e(TAG, e.message.toString())
            }
        }
        connectThread.start()

        band_dialog_reset.visibility = View.VISIBLE
        band_dialog_layout.visibility = View.VISIBLE

        if(device_btn01 != null) {
            device_btn01!!.setOnClickListener {
                if(connectionCheck) {
                    if (bus.isConnected(device01)) {
                        bus.send(CMD.CALL.v, device01)
                    }
                }
            }
        }
        if(device_btn02 != null) {
            device_btn02!!.setOnClickListener {
                if(connectionCheck) {
                    if (bus.isConnected(device02)) {
                        bus.send(CMD.CALL.v, device02)
                    }
                }
            }
        }
        if(device_btn03 != null) {
            device_btn03!!.setOnClickListener {
                if(connectionCheck) {
                    if (bus.isConnected(device03)) {
                        bus.send(CMD.CALL.v, device03)
                    }
                }
            }
        }
        if(device_btn04 != null) {
            device_btn04!!.setOnClickListener {
                if(connectionCheck) {
                    if (bus.isConnected(device04)) {
                        bus.send(CMD.CALL.v, device04)
                    }
                }
            }
        }
        if(device_btn05 != null) {
            device_btn05!!.setOnClickListener {
                if(connectionCheck) {
                    if (bus.isConnected(device05)) {
                        bus.send(CMD.CALL.v, device05)
                    }
                }
            }
        }
        if(device_btn06 != null) {
            device_btn06!!.setOnClickListener {
                if(connectionCheck) {
                    if (bus.isConnected(device06)) {
                        bus.send(CMD.CALL.v, device06)
                    }
                }
            }
        }

        band_dialog_layout.setOnClickListener {
            if(connectionCheck) {
                connectionCheck = true
                bus.stopScanning()
                if (bus.isConnected(device01)) {
                    isConnected = true
                }
                if (bus.isConnected(device02)) {
                    isConnected = true
                }
                if (bus.isConnected(device03)) {
                    isConnected = true
                }
                if (bus.isConnected(device04)) {
                    isConnected = true
                }
                if (bus.isConnected(device05)) {
                    isConnected = true
                }
                if (bus.isConnected(device06)) {
                    isConnected = true
                }
                for (i in 0 until 6) {
                    macList.add("-")
                }
                if (bus.devices.size > 0) {
                    try {
                        bus.devices.forEachIndexed { index, device ->
                            if (device != null) {
                                macList[index] = device.mac
                            }
                        }
                    }catch(e: ConcurrentModificationException){
                        Log.e(TAG, e.message.toString())
                    }
                }
                maclist = macList
                bus.startReConnection(true)
                dialog.dismiss()
            }
        }
        band_scan_reset.setOnClickListener {
            connectThread.interrupt()
            connectionCheck = true
            checkCount = 0
            isConnected = false
            bus.stopScanning()
            dialog.dismiss()
            showScanDialog()
        }
        band_dialog_reset.setOnClickListener {
            if(connectionCheck) {
                connectThread.interrupt()
                isConnected = false
                connectionCheck = true
                checkCount = 0
                bus.stopScanning()
                bus.disconnect()
                dialog.dismiss()
                showAlertDialog()
            }
        }

        val width = resources.displayMetrics.widthPixels * 0.9f
        val height = resources.displayMetrics.heightPixels * 0.7f
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        dialog.window?.setLayout(width.toInt(), height.toInt())
    }

    fun connectionCheck(macList: ArrayList<String?>, count: Int): Boolean{
        var check = 0
        try {
            val connected = bus.devices
            for(device in connected){
                if(device?.mac != null) {
                    val address = device.mac
                    if (bus.isConnected(address)) {
                        if (macList[0] == address) {
                            if (device_btn01 != null) {
                                isConnected = true
                                check += 1
                                device_btn01!!.setImageResource(R.drawable.cpr_on)
                            }
                        } else if (macList[1] == address) {
                            if (device_btn02 != null) {
                                isConnected = true
                                check += 1
                                device_btn02!!.setImageResource(R.drawable.cpr_on)
                            }
                        } else if (macList[2] == address) {
                            if (device_btn03 != null) {
                                isConnected = true
                                check += 1
                                device_btn03!!.setImageResource(R.drawable.cpr_on)
                            }
                        } else if (macList[3] == address) {
                            if (device_btn04 != null) {
                                isConnected = true
                                check += 1
                                device_btn04!!.setImageResource(R.drawable.cpr_on)
                            }
                        } else if (macList[4] == address) {
                            if (device_btn05 != null) {
                                isConnected = true
                                check += 1
                                device_btn05!!.setImageResource(R.drawable.cpr_on)
                            }
                        } else if (macList[5] == address) {
                            if (device_btn06 != null) {
                                isConnected = true
                                check += 1
                                device_btn06!!.setImageResource(R.drawable.cpr_on)
                            }
                        }
                    }
                }
            }
        }catch(e: SecurityException){
            Log.e(TAG, "securit exception is called")
        }
        return check == count
    }

    private fun checkPermission(): Boolean{
        val targetSdkVersion = applicationInfo.targetSdkVersion
        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            result = applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = applicationContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        return result
    }

    private fun permissionCheck(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
        } else
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
    }

    inner class LeDeviceListAdapter: BaseAdapter() {
        var mLeDevices = ArrayList<BleDevice>()
        private var scanResult = ArrayList<String>()
        fun addDevice(device: BleDevice){
            if(!scanResult.contains(device.mac)){
                scanResult.add(device.mac)
                mLeDevices.add(device)
                Log.e(TAG, "add device: ${device.mac}")
            }
        }

        fun getDevice(position: Int): BleDevice{
            return mLeDevices[position]
        }

        fun remove(position: Int){
            scanResult.removeAt(position)
            mLeDevices.removeAt(position)
        }

        fun clear(){
            scanResult.clear()
            mLeDevices.clear()
        }

        override fun getCount(): Int {
            return mLeDevices.size
        }

        override fun getItem(p0: Int): Any {
            return mLeDevices[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getView(i: Int, p0: View?, viewGroup: ViewGroup?): View {
            val viewHolder: ViewHolder?
            var view = p0
            if(view == null){
                view = layoutInflater.inflate(R.layout.listitem_device, null)
                viewHolder = ViewHolder()
                viewHolder.deviceAddress = view.findViewById(R.id.device_address)
                viewHolder.deviceName = view.findViewById(R.id.device_name)
                view.tag = viewHolder
            }else{
                viewHolder = view.tag as ViewHolder
            }
            val device = mLeDevices[i]
            viewHolder.deviceName?.text = device.name
            viewHolder.deviceAddress?.text = device.mac
            viewHolder.deviceName?.setTextColor(Color.parseColor("#ff5b00"))
            viewHolder.deviceAddress?.setTextColor(Color.parseColor("#ff5b00"))

            return view!!
        }
    }

    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }

    private fun initialize(){
        first_aio_text1 = findViewById(R.id.first_aio_text1)
        first_aio_text2 = findViewById(R.id.first_aio_text2)
        first_aio_text3 = findViewById(R.id.first_aio_text3)
        first_aio_text4 = findViewById(R.id.first_aio_text4)
        second_aio_text1 = findViewById(R.id.second_aio_text1)
        second_aio_text2 = findViewById(R.id.second_aio_text2)
        second_aio_text3 = findViewById(R.id.second_aio_text3)
        second_aio_text4 = findViewById(R.id.second_aio_text4)
        third_aio_text1 = findViewById(R.id.third_aio_text1)
        third_aio_text2 = findViewById(R.id.third_aio_text2)
        third_aio_text3 = findViewById(R.id.third_aio_text3)
        third_aio_text4 = findViewById(R.id.third_aio_text4)
        fourth_aio_text1 = findViewById(R.id.fourth_aio_text1)
        fourth_aio_text2 = findViewById(R.id.fourth_aio_text2)
        fourth_aio_text3 = findViewById(R.id.fourth_aio_text3)
        fourth_aio_text4 = findViewById(R.id.fourth_aio_text4)
        fifth_aio_text1 = findViewById(R.id.fifth_aio_text1)
        fifth_aio_text2 = findViewById(R.id.fifth_aio_text2)
        fifth_aio_text3 = findViewById(R.id.fifth_aio_text3)
        fifth_aio_text4 = findViewById(R.id.fifth_aio_text4)
        sixth_aio_text1 = findViewById(R.id.sixth_aio_text1)
        sixth_aio_text2 = findViewById(R.id.sixth_aio_text2)
        sixth_aio_text3 = findViewById(R.id.sixth_aio_text3)
        sixth_aio_text4 = findViewById(R.id.sixth_aio_text4)
        first_aio_call = findViewById(R.id.first_aio_call)
        second_aio_call = findViewById(R.id.second_aio_call)
        third_aio_call = findViewById(R.id.third_aio_call)
        fourth_aio_call = findViewById(R.id.fourth_aio_call)
        fifth_aio_call = findViewById(R.id.fifth_aio_call)
        sixth_aio_call = findViewById(R.id.sixth_aio_call)
        status_tv = findViewById(R.id.status_tv)
        scan_btn = findViewById(R.id.scan_btn)
        start_btn = findViewById(R.id.start_btn)
    }

    fun getIndexFromMac(mac: String): Int{
        return this.maclist.indexOf(mac)
    }


    private val gattInterface = object: GattInterface {
        override fun bleNotSupport(reason: String) {
            Toast.makeText(this@MainActivity, "ble not support, reason = $reason", Toast.LENGTH_SHORT).show()
        }

        override fun bluetoothPower(isOn: Boolean) {
            if (!isOn) {
                Toast.makeText(
                    this@MainActivity,
                    "bluetooth power is off",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun bleScanning(started: Boolean) {}

        override fun bleScanfound(dev: MutableList<BleDevice>) {
            try {
                bus.foundDevices.forEach {
                    if (it.name.contains("AIO")) {
                        if(!alertClickedMacList.contains(it.mac)) {
                            mLeDeviceListAdapter!!.addDevice(it)
                            mLeDeviceListAdapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }catch(e: ConcurrentModificationException){
                Log.e(TAG, e.message.toString())
            }
        }

        override fun connectionStatus(index: Int, device: BleDevice, to: RxBleConnection.RxBleConnectionState) {
            if(to == RxBleConnection.RxBleConnectionState.CONNECTED){
                if(!startFlag) {
                    when (index) {
                        0 -> {
                            if (device_btn01 != null) {
                                isConnected = true
                                device_btn01!!.setImageResource(R.drawable.cpr_on)
                            }
                        }
                        1 -> {
                            if (device_btn02 != null) {
                                isConnected = true
                                device_btn02!!.setImageResource(R.drawable.cpr_on)
                            }
                        }
                        2 -> {
                            if (device_btn03 != null) {
                                isConnected = true
                                device_btn03!!.setImageResource(R.drawable.cpr_on)
                            }
                        }
                        3 -> {
                            if (device_btn04 != null) {
                                isConnected = true
                                device_btn04!!.setImageResource(R.drawable.cpr_on)
                            }
                        }
                        4 -> {
                            if (device_btn05 != null) {
                                isConnected = true
                                device_btn05!!.setImageResource(R.drawable.cpr_on)
                            }
                        }
                        5 -> {
                            if (device_btn06 != null) {
                                isConnected = true
                                device_btn06!!.setImageResource(R.drawable.cpr_on)
                            }
                        }
                    }
                }
            }else if(to == RxBleConnection.RxBleConnectionState.DISCONNECTED){
                if(!startFlag) {
                    when (index) {
                        0 -> {
                            if (device_btn01 != null) {
                                device_btn01!!.setImageResource(R.drawable.cpr_off)
                            }
                        }
                        1 -> {
                            if (device_btn02 != null) {
                                device_btn02!!.setImageResource(R.drawable.cpr_off)
                            }
                        }
                        2 -> {
                            if (device_btn03 != null) {
                                device_btn03!!.setImageResource(R.drawable.cpr_off)
                            }
                        }
                        3 -> {
                            if (device_btn04 != null) {
                                device_btn04!!.setImageResource(R.drawable.cpr_off)
                            }
                        }
                        4 -> {
                            if (device_btn05 != null) {
                                device_btn05!!.setImageResource(R.drawable.cpr_off)
                            }
                        }
                        5 -> {
                            if (device_btn06 != null) {
                                device_btn06!!.setImageResource(R.drawable.cpr_off)
                            }
                        }
                    }
                }
            }
        }

        val format = DecimalFormat("0.00")
        var sending0 = Handler(Looper.getMainLooper())
        var sending1 = Handler(Looper.getMainLooper())
        var sending2 = Handler(Looper.getMainLooper())
        var sending3 = Handler(Looper.getMainLooper())
        var sending4 = Handler(Looper.getMainLooper())
        var sending5 = Handler(Looper.getMainLooper())

        override fun datReceived(mac: String, type: DataType, value: Number) {
            if(startFlag) {
                when (type) {
                    DataType.Test -> {
                        val index = getIndexFromMac(mac)
                        if(index != -1){
                            when (index){
                                0 -> {
                                    if(value.toInt() != 255) {
                                        if(testlist0.size == 1){
                                            sending0.postDelayed({
                                                if(startFlag0) {
                                                    Log.e(TAG, "AIO0 handler run")
                                                    when (testcount0) {
                                                        0 -> {
                                                            first0 = testlist0.size
                                                            runOnUiThread {
                                                                first_aio_text1.text =
                                                                    "1차 테스트 : " + format.format(((first0.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist0.clear()
                                                        }
                                                        1 -> {
                                                            second0 = testlist0.size
                                                            runOnUiThread {
                                                                first_aio_text2.text =
                                                                    "2차 테스트 : " + format.format(((second0.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist0.clear()
                                                        }
                                                        2 -> {
                                                            third0 = testlist0.size
                                                            runOnUiThread {
                                                                first_aio_text3.text =
                                                                    "3차 테스트 : " + format.format(((third0.toFloat() / total_num) * 100)) + "%\n"
                                                                first_aio_text4.text =
                                                                    "총 테스트 : " + format.format(((first0.toFloat() + second0.toFloat() + third0.toFloat()) / (total_num * 3f) * 100.0f)) + "%\n"
                                                            }
                                                            testlist0.clear()
                                                            startFlag = false
                                                        }
                                                    }
                                                    testcount0++
                                                    startFlag0 = false

                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        if (testcount0 < 3) {
                                                            bus.send(CMD.BLE_TEST.v, maclist[0]!!)
                                                            startFlag0 = true
                                                        }
                                                    }, 2000)
                                                }
                                            }, DELAY_TIME)
                                            Log.e(TAG, "AIO0 handler activated")
                                        }
                                        if(testlist0.size % 200 == 0)
                                            runOnUiThread {
                                                status_tv.append("■")
                                            }
                                        if(testlist0.size % 2400 == 0)
                                            runOnUiThread {
                                                status_tv.text = " "
                                            }
                                        testlist0.add(value.toInt())
                                    } else{
                                        if(startFlag0) {
                                            when (testcount0) {
                                                0 -> {
                                                    first0 = testlist0.size
                                                    runOnUiThread {
                                                        first_aio_text1.text =
                                                            "1차 테스트 : " + format.format(((first0.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist0.clear()
                                                }
                                                1 -> {
                                                    second0 = testlist0.size
                                                    runOnUiThread {
                                                        first_aio_text2.text =
                                                            "2차 테스트 : " + format.format(((second0.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist0.clear()
                                                }
                                                2 -> {
                                                    third0 = testlist0.size
                                                    runOnUiThread {
                                                        first_aio_text3.text =
                                                            "3차 테스트 : " + format.format(((third0.toFloat() / total_num) * 100)) + "%\n"
                                                        first_aio_text4.text =
                                                            "총 테스트 : " + format.format(((first0.toFloat() + second0.toFloat() + third0.toFloat()) / (total_num * 3f) * 100.0f)) + "%\n"
                                                    }
                                                    testlist0.clear()
                                                }
                                            }
                                            testcount0++
                                            startFlag0 = false

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (testcount0 < 3) {
                                                    bus.send(CMD.BLE_TEST.v, maclist[0]!!)
                                                    sending0.removeCallbacksAndMessages(null)
                                                    Log.e(TAG, "AIO0 handler cancel")
                                                    startFlag0 = true
                                                }
                                            }, 2000)
                                        }
                                    }
                                }
                                1 -> {
                                    if(value.toInt() != 255) {
                                        if(testlist1.size == 1){
                                            sending1.postDelayed({
                                                if(startFlag1) {
                                                    Log.e(TAG, "AIO1 handler run")
                                                    when (testcount1) {
                                                        0 -> {
                                                            first1 = testlist1.size
                                                            runOnUiThread{
                                                                second_aio_text1.text = "1차 테스트 : " + format.format(((first1.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist1.clear()
                                                        }
                                                        1 -> {
                                                            second1 = testlist1.size
                                                            runOnUiThread{
                                                                second_aio_text2.text = "2차 테스트 : " + format.format(((second1.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist1.clear()
                                                        }
                                                        2 -> {
                                                            third1 = testlist1.size
                                                            runOnUiThread {
                                                                second_aio_text3.text =
                                                                    "3차 테스트 : " + format.format(((third1.toFloat() / total_num) * 100)) + "%\n"
                                                                second_aio_text4.text =
                                                                    "총 테스트 : " + format.format(((first1.toFloat() + second1.toFloat() + third1.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                            }
                                                            testlist1.clear()
                                                        }
                                                    }
                                                    testcount1++
                                                    startFlag1 = false
                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        if (testcount1 < 3) {
                                                            bus.send(CMD.BLE_TEST.v, maclist[1]!!)
                                                            startFlag1 = true
                                                        }
                                                    }, 2000)
                                                }
                                            }, DELAY_TIME)
                                            Log.e(TAG, "AIO1 handler activated")
                                        }
                                        testlist1.add(value.toInt())
                                    } else{
                                        if(startFlag1) {
                                            when (testcount1) {
                                                0 -> {
                                                    first1 = testlist1.size
                                                    runOnUiThread {
                                                        second_aio_text1.text =
                                                            "1차 테스트 : " + format.format(((first1.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist1.clear()
                                                }
                                                1 -> {
                                                    second1 = testlist1.size
                                                    runOnUiThread {
                                                        second_aio_text2.text =
                                                            "2차 테스트 : " + format.format(((second1.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist1.clear()
                                                }
                                                2 -> {
                                                    third1 = testlist1.size
                                                    runOnUiThread {
                                                        second_aio_text3.text =
                                                            "3차 테스트 : " + format.format(((third1.toFloat() / total_num) * 100)) + "%\n"
                                                        second_aio_text4.text =
                                                            "총 테스트 : " + format.format(((first1.toFloat() + second1.toFloat() + third1.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                    }
                                                    testlist1.clear()
                                                }
                                            }
                                            testcount1++
                                            startFlag1 = false

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (testcount1 < 3) {
                                                    bus.send(CMD.BLE_TEST.v, maclist[1]!!)
                                                    sending1.removeCallbacksAndMessages(null)
                                                    Log.e(TAG, "AIO1 handler cancel")
                                                    startFlag1 = true
                                                }
                                            }, 2000)
                                        }
                                    }
                                }
                                2 -> {
                                    var sending = Handler(Looper.getMainLooper())
                                    if(value.toInt() != 255) {
                                        if(testlist2.size == 1){
                                            sending2.postDelayed({
                                                if(startFlag2) {
                                                    Log.e(TAG, "AIO2 handler run")
                                                    when (testcount2) {
                                                        0 -> {
                                                            first2 = testlist2.size
                                                            runOnUiThread {
                                                                third_aio_text1.text =
                                                                    "1차 테스트 : " + format.format(((first2.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist2.clear()
                                                        }
                                                        1 -> {
                                                            second2 = testlist2.size
                                                            runOnUiThread {
                                                                third_aio_text2.text =
                                                                    "2차 테스트 : " + format.format(((second2.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist2.clear()
                                                        }
                                                        2 -> {
                                                            third2 = testlist2.size
                                                            runOnUiThread {
                                                                third_aio_text3.text =
                                                                    "3차 테스트 : " + format.format(((third2.toFloat() / total_num) * 100)) + "%\n"
                                                                third_aio_text4.text =
                                                                    "총 테스트 : " + format.format(((first2.toFloat() + second2.toFloat() + third2.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                            }
                                                            testlist2.clear()
                                                        }
                                                    }
                                                    testcount2++
                                                    startFlag2 = false

                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        if (testcount2 < 3) {
                                                            bus.send(CMD.BLE_TEST.v, maclist[2]!!)
                                                            startFlag2 = true
                                                        }
                                                    }, 2000)
                                                }
                                            }, DELAY_TIME)
                                            Log.e(TAG, "AIO2 handler activated")
                                        }
                                        testlist2.add(value.toInt())
                                    } else{
                                        if(startFlag2) {
                                            when (testcount2) {
                                                0 -> {
                                                    first2 = testlist2.size
                                                    runOnUiThread {
                                                        third_aio_text1.text =
                                                            "1차 테스트 : " + format.format(((first2.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist2.clear()
                                                }
                                                1 -> {
                                                    second2 = testlist2.size
                                                    runOnUiThread {
                                                        third_aio_text2.text =
                                                            "2차 테스트 : " + format.format(((second2.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist2.clear()
                                                }
                                                2 -> {
                                                    third2 = testlist2.size
                                                    runOnUiThread {
                                                        third_aio_text3.text =
                                                            "3차 테스트 : " + format.format(((third2.toFloat() / total_num) * 100)) + "%\n"
                                                        third_aio_text4.text =
                                                            "총 테스트 : " + format.format(((first2.toFloat() + second2.toFloat() + third2.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                    }
                                                    testlist2.clear()
                                                }
                                            }
                                            testcount2++
                                            startFlag2 = false

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (testcount2 < 3) {
                                                    bus.send(CMD.BLE_TEST.v, maclist[2]!!)
                                                    sending2.removeCallbacksAndMessages(null)
                                                    Log.e(TAG, "AIO2 handler cancel")
                                                    startFlag2 = true
                                                }
                                            }, 2000)
                                        }
                                    }
                                }
                                3 -> {
                                    if(value.toInt() != 255) {
                                        if(testlist3.size == 1){
                                            sending3.postDelayed({
                                                if(startFlag3) {
                                                    Log.e(TAG, "AIO3 handler run")
                                                    when (testcount3) {
                                                        0 -> {
                                                            first3 = testlist3.size
                                                            runOnUiThread {
                                                                fourth_aio_text1.text =
                                                                    "1차 테스트 : " + format.format(((first3.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist3.clear()
                                                        }
                                                        1 -> {
                                                            second3 = testlist3.size
                                                            runOnUiThread {
                                                                fourth_aio_text2.text =
                                                                    "2차 테스트 : " + format.format(((second3.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist3.clear()
                                                        }
                                                        2 -> {
                                                            third3 = testlist3.size
                                                            runOnUiThread {
                                                                fourth_aio_text3.text =
                                                                    "3차 테스트 : " + format.format(((third3.toFloat() / total_num) * 100)) + "%\n"
                                                                fourth_aio_text4.text =
                                                                    "총 테스트 : " + format.format(((first3.toFloat() + second3.toFloat() + third3.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                            }
                                                            testlist3.clear()
                                                        }
                                                    }
                                                    testcount3++
                                                    startFlag3 = false
                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        if (testcount3 < 3) {
                                                            bus.send(CMD.BLE_TEST.v, maclist[3]!!)
                                                            startFlag3 = true
                                                        }
                                                    }, 2000)
                                                }
                                            }, DELAY_TIME)
                                            Log.e(TAG, "AIO3 handler activated")
                                        }
                                        testlist3.add(value.toInt())
                                    } else{
                                        if(startFlag3) {
                                            when (testcount3) {
                                                0 -> {
                                                    first3 = testlist3.size
                                                    runOnUiThread {
                                                        fourth_aio_text1.text =
                                                            "1차 테스트 : " + format.format(((first3.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist3.clear()
                                                }
                                                1 -> {
                                                    second3 = testlist3.size
                                                    runOnUiThread {
                                                        fourth_aio_text2.text =
                                                            "2차 테스트 : " + format.format(((second3.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist3.clear()
                                                }
                                                2 -> {
                                                    third3 = testlist3.size
                                                    runOnUiThread {
                                                        fourth_aio_text3.text =
                                                            "3차 테스트 : " + format.format(((third3.toFloat() / total_num) * 100)) + "%\n"
                                                        fourth_aio_text4.text =
                                                            "총 테스트 : " + format.format(((first3.toFloat() + second3.toFloat() + third3.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                    }
                                                    testlist3.clear()
                                                }
                                            }
                                            testcount3++
                                            startFlag3 = false
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (testcount3 < 3) {
                                                    bus.send(CMD.BLE_TEST.v, maclist[3]!!)
                                                    sending3.removeCallbacksAndMessages(null)
                                                    Log.e(TAG, "AIO3 handler cancel")
                                                    startFlag3 = true
                                                }
                                            }, 2000)
                                        }
                                    }
                                }
                                4 -> {
                                    if(value.toInt() != 255) {
                                        if(testlist4.size == 1){
                                            sending4.postDelayed({
                                                if(startFlag4) {
                                                    Log.e(TAG, "AIO4 handler run")
                                                    when (testcount4) {
                                                        0 -> {
                                                            first4 = testlist4.size
                                                            runOnUiThread {
                                                                fifth_aio_text1.text =
                                                                    "1차 테스트 : " + format.format(((first4.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist4.clear()
                                                        }
                                                        1 -> {
                                                            second4 = testlist4.size
                                                            runOnUiThread {
                                                                fifth_aio_text2.text =
                                                                    "2차 테스트 : " + format.format(((second4.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist4.clear()
                                                        }
                                                        2 -> {
                                                            third4 = testlist4.size
                                                            runOnUiThread {
                                                                fifth_aio_text3.text =
                                                                    "3차 테스트 : " + format.format(((third4.toFloat() / total_num) * 100)) + "%\n"
                                                                fifth_aio_text4.text =
                                                                    "총 테스트 : " + format.format(((first4.toFloat() + second4.toFloat() + third4.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                            }
                                                            testlist4.clear()
                                                        }
                                                    }
                                                    testcount4++
                                                    startFlag4 = false
                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        if (testcount4 < 3) {
                                                            bus.send(CMD.BLE_TEST.v, maclist[4]!!)
                                                            startFlag4 = true
                                                        }
                                                    }, 2000)
                                                }
                                            }, DELAY_TIME)
                                            Log.e(TAG, "AIO4 handler activated")
                                        }
                                        testlist4.add(value.toInt())
                                    } else{
                                        if(startFlag4) {
                                            when (testcount4) {
                                                0 -> {
                                                    first4 = testlist4.size
                                                    runOnUiThread {
                                                        fifth_aio_text1.text =
                                                            "1차 테스트 : " + format.format(((first4.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist4.clear()
                                                }
                                                1 -> {
                                                    second4 = testlist4.size
                                                    runOnUiThread {
                                                        fifth_aio_text2.text =
                                                            "2차 테스트 : " + format.format(((second4.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist4.clear()
                                                }
                                                2 -> {
                                                    third4 = testlist4.size
                                                    runOnUiThread {
                                                        fifth_aio_text3.text =
                                                            "3차 테스트 : " + format.format(((third4.toFloat() / total_num) * 100)) + "%\n"
                                                        fifth_aio_text4.text =
                                                            "총 테스트 : " + format.format(((first4.toFloat() + second4.toFloat() + third4.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                    }
                                                    testlist4.clear()
                                                }
                                            }
                                            testcount4++
                                            startFlag4 = false

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (testcount4 < 3) {
                                                    bus.send(CMD.BLE_TEST.v, maclist[4]!!)
                                                    sending4.removeCallbacksAndMessages(null)
                                                    Log.e(TAG, "AIO4 handler cancel")
                                                    startFlag4 = true
                                                }
                                            }, 2000)
                                        }
                                    }
                                }
                                5 -> {
                                    if(value.toInt() != 255) {
                                        if(testlist5.size == 1){
                                            sending5.postDelayed({
                                                if(startFlag5) {
                                                    Log.e(TAG, "AIO5 handler run")
                                                    when (testcount5) {
                                                        0 -> {
                                                            first5 = testlist5.size
                                                            runOnUiThread {
                                                                sixth_aio_text1.text =
                                                                    "1차 테스트 : " + format.format(((first5.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist5.clear()
                                                        }
                                                        1 -> {
                                                            second5 = testlist5.size
                                                            runOnUiThread {
                                                                sixth_aio_text2.text =
                                                                    "2차 테스트 : " + format.format(((second5.toFloat() / total_num) * 100)) + "%\n"
                                                            }
                                                            testlist5.clear()
                                                        }
                                                        2 -> {
                                                            third5 = testlist5.size
                                                            runOnUiThread {
                                                                sixth_aio_text3.text =
                                                                    "3차 테스트 : " + format.format(((third5.toFloat() / total_num) * 100)) + "%\n"
                                                                sixth_aio_text4.text =
                                                                    "총 테스트 : " + format.format(((first5.toFloat() + second5.toFloat() + third5.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                            }
                                                            testlist5.clear()
                                                        }
                                                    }
                                                    testcount5++
                                                    startFlag5 = false

                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        if (testcount5 < 3) {
                                                            bus.send(CMD.BLE_TEST.v, maclist[5]!!)
                                                            startFlag5 = true
                                                        }
                                                    }, 2000)
                                                }
                                            }, DELAY_TIME)
                                            Log.e(TAG, "AIO5 handler activated")
                                        }
                                        testlist5.add(value.toInt())
                                    } else{
                                        if(startFlag5) {
                                            when (testcount5) {
                                                0 -> {
                                                    first5 = testlist5.size
                                                    runOnUiThread {
                                                        sixth_aio_text1.text =
                                                            "1차 테스트 : " + format.format(((first5.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist5.clear()
                                                }
                                                1 -> {
                                                    second5 = testlist5.size
                                                    runOnUiThread {
                                                        sixth_aio_text2.text =
                                                            "2차 테스트 : " + format.format(((second5.toFloat() / total_num) * 100)) + "%\n"
                                                    }
                                                    testlist5.clear()
                                                }
                                                2 -> {
                                                    third5 = testlist5.size
                                                    runOnUiThread {
                                                        sixth_aio_text3.text =
                                                            "3차 테스트 : " + format.format(((third5.toFloat() / total_num) * 100)) + "%\n"
                                                        sixth_aio_text4.text =
                                                            "총 테스트 : " + format.format(((first5.toFloat() + second5.toFloat() + third5.toFloat()) / (total_num * 3f) * 100)) + "%\n"
                                                    }
                                                    testlist5.clear()
                                                }
                                            }
                                            testcount5++
                                            startFlag5 = false

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (testcount5 < 3) {
                                                    bus.send(CMD.BLE_TEST.v, maclist[5]!!)
                                                    sending5.removeCallbacksAndMessages(null)
                                                    Log.e(TAG, "AIO5 handler cancel")
                                                    startFlag5 = true
                                                }
                                            }, 2000)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}