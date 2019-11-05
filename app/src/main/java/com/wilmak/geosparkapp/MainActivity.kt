package com.wilmak.geosparkapp

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geospark.lib.GeoSpark
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.geospark.lib.callback.*
import com.geospark.lib.model.*
import com.geospark.lib.model.GeoSparkError
import com.geospark.lib.model.GeoSparkUser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import java.io.*
import kotlin.collections.HashMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        val User_Name = "Demo User"
        val LocInfo_Filename = "LocInfoData.txt"
        val ALARM_REQUEST_CODE = 1001
    }

    private var mTripId = ""
    private var mUserId = ""
    private var mUserName = ""
    private val sdf = SimpleDateFormat("hh:mm:ss")

    private lateinit var mAlarmManager: AlarmManager
    private lateinit var mPendingIntent: PendingIntent

    private var mFos: FileOutputStream? = null
    private var mSWriter: OutputStreamWriter? = null

    private var mMap: GoogleMap? = null

    data class LocationPoint(
        var lat: Double,
        var lng: Double,
        var accurracy: Double,
        var timeStr: String
    )

    private val mLocationPoints = mutableListOf<LocationPoint>()

    private var mIsGeneralInfoReceiverRegistered = false
    private lateinit var mButtons: HashMap<Int, Button>

    private val generalInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                GeoSparkDemoApp.ACTION_DEMOAPP_LOCATION_INFO -> {
                    intent.let {
                        it.extras?.let {
                            val bundle = it.getBundle("locationInfo")
                            dumpLocationInfoBundle(bundle)
                        }
                    }
                }
                GeoSparkDemoApp.ACTION_DEMOAPP_USER_LOGIN -> {
                    val id = intent.getStringExtra("Geospark.LoginUser.UserId")
                    val desc = intent.getStringExtra("Geospark.LoginUser.Description")
                    txt_activity.append("User Login: ${id} Desc: ${desc}\n")
                    clearLogInfoFile()
                    enableAllGeoSparkTrackings()
                }
                GeoSparkDemoApp.ACTION_DEMOAPP_USER_LOGIN_FAILURE -> {
                    val ec = intent.getStringExtra("Geospark.LoginUser.Failure.EC")
                    val desc = intent.getStringExtra("Geospark.LoginUser.Failure.Desc")
                    txt_activity.append("User Login failure: ${ec} Desc: ${desc}\n")
                }
                GeoSparkDemoApp.ACTION_DEMOAPP_PERIODIC_LOCATION_UPDATE -> {
                    GeoSpark.getCurrentLocation(
                        applicationContext,
                        20,
                        object : GeoSparkLocationCallback {
                            override fun onFailure(p0: GeoSparkError?) {
                            }

                            override fun location(lat: Double, lng: Double, accuraccy: Double) {
                                val timeStr =
                                    SimpleDateFormat("hh:mm:ss").run {
                                        format(Date(System.currentTimeMillis()))
                                    }
                                txt_activity.append("${timeStr}:Current loc:${lat},${lng}\n")
                                saveMapEntryToFile(lat, lng, accuraccy, timeStr)
                            }
                        })
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //restoreStateAndCheckToClearLocationPoints()
        openLocInfoFileForWriting()

        mButtons = hashMapOf(
            Pair(R.id.btn_getUser, btn_getUser), Pair(R.id.btn_logout, btn_logout),
            Pair(R.id.btn_startTracking, btn_startTracking), Pair(R.id.btn_startTrip, btn_startTrip)
        )

        //scheduleActiveLocationUpdateService()

        GeoSpark.notificationOpenedHandler(this, getIntent());
        disableBatteryOptimization()

        val mapFragment = SupportMapFragment()
        mapFragment.getMapAsync(this)

        val tran = supportFragmentManager.beginTransaction()
        tran.add(R.id.layout_frame, mapFragment)
        tran.commit()

        btn_startTrip.setOnClickListener {
            if (btn_startTrip.text.toString().toLowerCase(Locale.getDefault()).equals(
                    R.string.start_trip.toString().toLowerCase(
                        Locale.getDefault()
                    )
                )
            )
                stopTrip()
            else
                startTrip()
        }

        btn_startTracking.setOnClickListener {
            if (isTrackingOn())
                stopTracking()
            else
                requestGeoSparkPermissionsAndStartTracking()
        }

        btn_getUser.setOnClickListener {
            val builder = AlertDialog.Builder(this@MainActivity).let {
                it.setView(R.layout.dialog_layout1)
                it.setTitle("User Name Gathering")
                it.setIcon(R.drawable.common_google_signin_btn_icon_light)
                it.setPositiveButton(R.string.common_signin_button_text,
                    DialogInterface.OnClickListener { dialog, which ->
                        val dialog_txt =
                            (dialog as Dialog).findViewById(R.id.txt_name_edit) as EditText
                        val nameText = dialog_txt.text.toString()
                        if (!TextUtils.isEmpty(nameText)) {
                            if (TextUtils.isEmpty(nameText) || !GeoSparkDemoApp.UserIdMap.containsKey(
                                    nameText
                                )
                            ) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "User name not found!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                mUserName = nameText
                                txt_name.setText("Welcome $mUserName")
                                GeoSpark.getUser(
                                    this,
                                    GeoSparkDemoApp.UserIdMap[mUserName],
                                    object : GeoSparkCallBack {

                                        public override fun onSuccess(geoSparkUser: GeoSparkUser) {
                                            val intent =
                                                Intent(GeoSparkDemoApp.ACTION_DEMOAPP_USER_LOGIN)
                                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                                            intent.putExtra(
                                                "Geospark.LoginUser.UserId",
                                                geoSparkUser.userId
                                            )
                                            intent.putExtra(
                                                "Geospark.LoginUser.Description",
                                                geoSparkUser.description
                                            )
                                            sendBroadcast(intent)
                                            mUserId = geoSparkUser.userId
                                            restoreUIState()
                                        }

                                        public override fun onFailure(geoSparkError: GeoSparkError) {
                                            Log.w(
                                                "GeoSparkDemoApp",
                                                "Create User failure: ${geoSparkError.errorCode} ${geoSparkError.errorMessage}"
                                            )
                                            val intent =
                                                Intent(GeoSparkDemoApp.ACTION_DEMOAPP_USER_LOGIN_FAILURE)
                                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                                            intent.putExtra(
                                                "Geospark.LoginUser.Failure.EC",
                                                geoSparkError.errorCode
                                            )
                                            intent.putExtra(
                                                "Geospark.LoginUser.Failure.Desc",
                                                geoSparkError.errorMessage
                                            )
                                            sendBroadcast(intent)
                                            restoreUIState()
                                        }
                                    })
                            }
                        }
                    })
                it.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->

                })
            }
            val dialog = builder.create()
            dialog.show()
        }

        btn_logout.setOnClickListener {
            mUserId = ""
            stopTrip()
            stopTracking()
            GeoSpark.logout(this, object : GeoSparkLogoutCallBack {
                override fun onSuccess(message: String?) {
                    restoreUIState()
                    clearLogInfoFile()
                    clearMap()
                }

                override fun onFailure(geosparkErr: GeoSparkError?) {
                    restoreUIState()
                    clearLogInfoFile()
                    clearMap()
                }
            });
        }
    }

    override fun onResume() {
        super.onResume()
        clearActivityView()
        restoreStateAndCheckToClearLocationPoints()
        restoreUIState()
        GeoSparkDemoLocationUpdateService.locationJob(this)
        if (!mIsGeneralInfoReceiverRegistered) {
            val filter = IntentFilter()
            filter.addAction(GeoSparkDemoApp.ACTION_DEMOAPP_LOCATION_INFO)
            filter.addAction(GeoSparkDemoApp.ACTION_DEMOAPP_USER_LOGIN)
            filter.addAction(GeoSparkDemoApp.ACTION_DEMOAPP_USER_LOGIN_FAILURE)
            filter.addAction(GeoSparkDemoApp.ACTION_DEMOAPP_PERIODIC_LOCATION_UPDATE)
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(generalInfoReceiver, filter)
            mIsGeneralInfoReceiverRegistered = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mIsGeneralInfoReceiverRegistered)
            unregisterReceiver(generalInfoReceiver)
        closeLocInfoFile()
        mAlarmManager.cancel(mPendingIntent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        var start = LatLng(22.28552, 114.15769)
        if (mLocationPoints.size > 0)
            start = LatLng(mLocationPoints.first().lat, mLocationPoints.first().lng)
        mMap?.let {
            it.setMinZoomPreference(it.maxZoomLevel - 3)
            drawTripLineOnMap()
            it.moveCamera(CameraUpdateFactory.newLatLng(start))
        }
    }

    private fun disableBatteryOptimization() {
        GeoSpark.disableBatteryOptimization(this)
        if (!GeoSpark.isBatteryOptimizationEnabled(this)) {
            txt_activity.append("Battery optimization disabled @ ${sdf.format(Date().time)}\n")
        }
    }

    private fun enableAllGeoSparkTrackings() {
        GeoSpark.toggleEvents(this, true, true, true, object : GeoSparkEventsCallback {

            override fun onSuccess(geoSparkEvents: GeoSparkEvents) {
                //This code will update the Device token
                val preferences = getSharedPreferences("GeoSparkDemoApp", Context.MODE_PRIVATE)
                val deviceToken = preferences.getString("deviceToken", "")
                if (deviceToken!!.isNotEmpty()) {
                    GeoSpark.setDeviceToken(applicationContext, deviceToken)
                }
                val strRes =
                    "Events=${geoSparkEvents.isActivityEventsActive} GeoFence=${geoSparkEvents.isGeofenceEventsActive} Trips=${geoSparkEvents.isTripEventsActive}\n"
                txt_activity.append(strRes)
            }

            override fun onFailure(geoSparkError: GeoSparkError) {
                txt_activity.append("\nFeature enablement failure ErrCode=${geoSparkError.errorCode} ErrMsg=${geoSparkError.errorMessage}\n")
            }
        })
        GeoSpark.setTrackingInAppState(this, arrayOf(GeoSpark.Type.ALWAYS_ON))
        GeoSpark.setTrackingInMotion(this, arrayOf(GeoSpark.Type.ALL))
        txt_activity.append("FCM Device Token=${GeoSpark.getDeviceToken(this)}\n")
    }

    private fun requestGeoSparkPermissionsAndStartTracking() {
        if (!GeoSpark.checkLocationPermission(this)) {
            GeoSpark.requestLocationPermission(this)
        } else if (!GeoSpark.checkLocationServices(this)) {
            GeoSpark.requestLocationServices(this)
        } else {
            GeoSpark.startTracking(this)
            if (GeoSpark.isLocationTracking(this)) {
                txt_activity.append("Tracking is enabled @ ${sdf.format(Date().time)}\n")
                restoreUIState()
            }
        }
    }

    private fun stopTracking() {
        GeoSpark.stopTracking(this)
        if (!GeoSpark.isLocationTracking(this)) {
            txt_activity.append("Tracking ended @ ${sdf.format(Date().time)}\n")
            clearLogInfoFile()
            restoreUIState()
            clearMap()
        }
    }

    private fun startTrip() {
        if (!TextUtils.isEmpty(mTripId)) {
            txt_activity.append("Trip already started \n")
            return
        }
        mTripId = "TripId_${System.currentTimeMillis()}" // Pass the trip id from create trip api
        if (!GeoSpark.checkLocationPermission(this)) {
            GeoSpark.requestLocationPermission(this)
        } else if (!GeoSpark.checkLocationServices(this)) {
            GeoSpark.requestLocationServices(this)
        } else {
            GeoSpark.startTrip(
                this,
                mTripId,
                "Trip@${sdf.format(Date(System.currentTimeMillis()))}",
                object : GeoSparkTripCallBack {

                    override fun onSuccess(geoSparkTrip: GeoSparkTrip) {
                        txt_activity.append("Trip started @ ${sdf.format(Date(System.currentTimeMillis()))} ${geoSparkTrip.msg}\n")
                        restoreUIState()
                    }

                    override fun onFailure(geoSparkError: GeoSparkError) {
                        txt_activity.append("Trip start failure @${sdf.format(Date(System.currentTimeMillis()))} ErrCode=${geoSparkError.errorCode} ErrMsg=${geoSparkError.errorMessage}\n")
                        restoreUIState()
                    }
                })
        }
    }

    private fun stopTrip() {
        if (!GeoSpark.checkLocationPermission(this)) {
            GeoSpark.requestLocationPermission(this)
        } else if (!GeoSpark.checkLocationServices(this)) {
            GeoSpark.requestLocationServices(this)
        } else {
            GeoSpark.endTrip(this, mTripId, object : GeoSparkTripCallBack {

                override fun onSuccess(geoSparkTrip: GeoSparkTrip) {
                    txt_activity.append("Trip ended @ ${sdf.format(Date().time)} ${geoSparkTrip.msg}\n")
                    restoreUIState()
                }

                override fun onFailure(geoSparkError: GeoSparkError) {
                    txt_activity.append("Trip end failure ErrCode=${geoSparkError.errorCode} ErrMsg=${geoSparkError.errorMessage}\n")
                    restoreUIState()
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            GeoSpark.REQUEST_CODE_LOCATION_PERMISSION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    txt_activity.append("Location permission granted @ ${sdf.format(Date().time)}\n")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GeoSpark.REQUEST_CODE_LOCATION_ENABLED) {
            txt_activity.append("Location Service granted @ ${sdf.format(Date().time)}\n")
        }
    }

    private fun dumpLocationInfoBundle(bundle: Bundle?) {
        bundle?.let {
            val locInfo = it.getSerializable("Geospark.LocationInfo") as LocationInfo
            val sb = StringBuilder()

            sb.append("***provider: ${if (locInfo.provider.isNotEmpty()) locInfo.provider else "N/A"}, ")
            sb.append("lat:${if (locInfo.latitude != -1.0) locInfo.latitude.toString() else "N/A"}, ")
            sb.append("long: ${if (locInfo.longitude != -1.0) locInfo.longitude.toString() else "N/A"}, ")
            sb.append("alt: ${if (locInfo.altitude != -1.0) locInfo.altitude.toString() else "N/A"}\n")

            sb.append("speed: ${if (locInfo.speed != -1.0) locInfo.speed.toString() else "N/A"}, ")
            sb.append("bearing: ${if (locInfo.bearing != -1.0) locInfo.bearing.toString() else "N/A"}, ")
            sb.append("time: ${if (locInfo.time != 0L) locInfo.time.toString() else "N/A"}, ")
            sb.append("elapsed time: ${if (locInfo.elpasedTime != -1.0) locInfo.elpasedTime.toString() else "N/A"}")
            txt_activity.append(sb.toString())

            if (locInfo.latitude != -1.0 && locInfo.longitude != -1.0) {
                saveMapEntryToFile(
                    locInfo.latitude,
                    locInfo.longitude,
                    -1.0,
                    sdf.format(Date(System.currentTimeMillis()))
                )
            }
        }
    }

    private fun clearActivityView() {
        txt_activity.setText("")
    }

    private fun isTrackingOn(): Boolean {
        return GeoSpark.isLocationTracking(this)
    }

    private fun saveState() {
        val preferences = getSharedPreferences("GeoSparkDemoApp", Context.MODE_PRIVATE)
        if (!TextUtils.isEmpty(mUserId)) {
            val editor = preferences.edit()
            editor.putString("userId", mUserId)
            editor.putString("userName", mUserName)
            editor.commit()
        }
    }

    private fun restoreStateAndCheckToClearLocationPoints() {
        val preferences = getSharedPreferences("GeoSparkDemoApp", Context.MODE_PRIVATE)
        val uid = preferences.getString("userId", "")
        if (!TextUtils.isEmpty(uid)) {
            mUserId = uid!!
            getAllLocPoints()
            mLocationPoints.forEach {
                txt_activity.append("${it.timeStr}:Current loc:${it.lat},${it.lng}\n")
            }
        } else {
            clearLogInfoFile()
        }
        val uname = preferences.getString("userName", "")
        if (!TextUtils.isEmpty(uname)) {
            mUserName = uname!!
            txt_name.setText("Welcome $mUserName")
        }
    }

    private fun scheduleActiveLocationUpdateService() {
        val executor = ScheduledThreadPoolExecutor(2)
        executor.scheduleAtFixedRate(
            object : Runnable {
                override fun run() {
                    val intent =
                        Intent(GeoSparkDemoApp.ACTION_DEMOAPP_PERIODIC_LOCATION_UPDATE)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    applicationContext.sendBroadcast(intent)
                }
            }, 0, 15, TimeUnit.SECONDS
        )
    }

    private fun setTripUI() {
        // Tracking must be on.
        btn_startTrip.isEnabled = true
        GeoSpark.activeTrips(this, object : GeoSparkTripsCallBack {

            override fun onFailure(geoSparkError: GeoSparkError) {
                btn_startTrip.setText(R.string.start_trip)
            }

            override fun onSuccess(trips: MutableList<GeoSparkActiveTrips>?) {
                btn_startTrip.setText(if (trips != null && trips.size > 0) R.string.stop_trip else R.string.start_trip)
            }
        })
    }

    private fun restoreUIState() {
        if (TextUtils.isEmpty(mUserId)) {
            mButtons.filter { pair -> pair.key != R.id.btn_getUser }
                .forEach { pair -> pair.value.isEnabled = false }
            mButtons[R.id.btn_getUser]?.isEnabled = true
            return
        }

        GeoSpark.getUser(this, mUserId, object : GeoSparkCallBack {
            override fun onSuccess(geoSparkUser: GeoSparkUser) {
                mButtons[R.id.btn_getUser]?.isEnabled = false
                mButtons[R.id.btn_startTracking]?.isEnabled = true
                mButtons[R.id.btn_logout]?.isEnabled = true
                if (isTrackingOn()) {
                    mButtons[R.id.btn_startTracking]?.setText(R.string.stop_tracking)
                    setTripUI()
                } else {
                    mButtons[R.id.btn_startTracking]?.setText(R.string.start_tracking)
                    mButtons[R.id.btn_startTrip]?.isEnabled = false;
                }
                mUserName = geoSparkUser.description
                txt_name.setText("Welcome $mUserName")
            }

            override fun onFailure(geoSparkError: GeoSparkError) {
                mButtons.filter { pair -> pair.key != R.id.btn_getUser }
                    .forEach { pair -> pair.value.isEnabled = false }
                mButtons[R.id.btn_getUser]?.isEnabled = true
            }
        })
    }

    private fun openLocInfoFileForWriting() {
        mFos = applicationContext.openFileOutput(LocInfo_Filename, Context.MODE_APPEND)
        mSWriter = OutputStreamWriter(mFos!!)
    }

    private fun closeLocInfoFile() {
        mSWriter?.close()
        mFos?.close()
        mLocationPoints.clear()
    }

    private fun removeLocInfoFile() {
        val f = applicationContext.getFileStreamPath(LocInfo_Filename)
        if (f.exists())
            f.delete()
    }

    private fun clearLogInfoFile() {
        closeLocInfoFile()
        removeLocInfoFile()
        openLocInfoFileForWriting()
    }

    private fun saveMapEntryToFile(lat: Double, lng: Double, accuracy: Double, timeStr: String) {
        if (lat == 0.0 || lat == -1.0 || lng == 0.0 || lng == -1.0) {
            return
        }
        var lastLocationPoint: LocationPoint? = null
        if (mLocationPoints.size > 0) {
            lastLocationPoint = mLocationPoints.last()
        }
        if (lastLocationPoint?.lat == lat || lastLocationPoint?.lng == lng)
            return
        mLocationPoints.add(LocationPoint(lat, lng, accuracy, timeStr))
        drawTripLineOnMap()
        mSWriter?.append("$lat,$lng,$accuracy,$timeStr\n")
        mSWriter?.flush()
    }

    private fun getAllLocPoints() {

        try {
            Log.i("MainActivity", "System File Path: ${applicationContext.filesDir}")
            val fisTemp = applicationContext.openFileInput(LocInfo_Filename)
            val isr = InputStreamReader(fisTemp)
            val bufReader = BufferedReader(isr)
            var line = bufReader.readLine()

            mLocationPoints.clear()
            while (!TextUtils.isEmpty(line)) {
                val sArray = line.split(",")
                if (sArray.size != 4)
                    continue

                val currLocInfo = LocationPoint(
                    sArray[0].toDouble(),
                    sArray[1].toDouble(),
                    sArray[2].toDouble(),
                    sArray[3]
                )
                mLocationPoints.add(currLocInfo)
                line = bufReader.readLine()
            }
            bufReader.close()
            isr.close()
            fisTemp.close()
        } catch (ex: FileNotFoundException) {
        }
    }

    private fun clearMap() {
        mMap?.clear()
    }

    private fun drawTripLineOnMap() {
        if (mLocationPoints.size == 0)
            return
        mMap?.let {
            clearMap()
            val latlngL = mLocationPoints.map {
                LatLng(it.lat, it.lng)
            }.toList()
            it.addPolyline(
                PolylineOptions()
                    .jointType(JointType.ROUND)
                    .clickable(false)
                    .addAll(latlngL)
            )
            val firstPoint = mLocationPoints.first()
            val lastPoint = mLocationPoints.last()
            val firstLatLng = LatLng(firstPoint.lat, firstPoint.lng)
            val lastLatLng = LatLng(lastPoint.lat, lastPoint.lng)
            it.addMarker(MarkerOptions().position(firstLatLng).title("You start from here"))
            it.addMarker(MarkerOptions().position(lastLatLng).title("Your current position"))
            it.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng))
        }
    }

}
