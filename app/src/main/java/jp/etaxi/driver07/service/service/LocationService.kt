package jp.etaxi.driver07.service.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import jp.etaxi.driver07.API
import jp.etaxi.driver07.MainActivity
import jp.etaxi.driver07.ParameterModel

import java.util.*

class LocationService : Service() {
    var counter = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private val TAG = "LocationService"

    private var databaserefernece: DatabaseReference? = null



    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
            1,
           Notification()
        )

        requestLocationUpdates()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val NOTIFICATION_CHANNEL_ID = "jp.etaxi.driver07"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running count::" + counter)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stoptimertask()
        //if(Preference.getInstance().getValue(this, PrefConst.PREFKEY_ACTIVE_ORDER_STATUS, "").equals("2")){
        //    val broadcastIntent = Intent()
        //    broadcastIntent.action = "restartservice"
       //     broadcastIntent.setClass(this, RestartBackgroundService::class.java)
        //    this.sendBroadcast(broadcastIntent)
       // }
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                var count = counter++
                if (latitude != 0.0 && longitude != 0.0) {
                    databaserefernece?.child("lat")?.setValue(latitude)
                    databaserefernece?.child("lng")?.setValue(longitude)
                    var genzai =latitude.toString() + ":::" + longitude.toString() + "Count" + count.toString()
                    Log.d("Location::",genzai)

                    val intent = Intent("com.example.broadcast.MY_NOTIFICATION1")
                    intent.putExtra("data", genzai)
                    intent.putExtra("lat",latitude)
                    intent.putExtra("lng",longitude)
                    sendBroadcast(intent)

                }
            }
        }
        timer!!.schedule(
            timerTask,
            0,
            1000
        )
    }


    fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }




    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest()
        request.setInterval(10000)
        request.setFastestInterval(5000)
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location = locationResult.getLastLocation()
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude


                        Log.d("Location Service", "location update $location")
                    }
                }
            }, Looper.myLooper()!!)
        }
    }









}