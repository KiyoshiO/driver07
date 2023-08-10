package jp.etaxi.driver07

import android.Manifest
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.annotations.NotNull
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import jp.etaxi.driver07.service.Util.isMyServiceRunning
import jp.etaxi.driver07.service.service.LocationService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var mLocationService = LocationService()
    var mServiceIntent: Intent? = null

    private lateinit var mMap: GoogleMap
    var latitude: Double = 0.0
    var longitude: Double = 0.0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val button = findViewById<Button>(R.id.button2)
        button.setOnClickListener {
            Toast.makeText(this, "クリックされました。", Toast.LENGTH_SHORT).show()



        }


        //サービス起動
        startlocationservice()

        //レシーバーセット
/*        this.applicationContext.registerReceiver(
            MyBroadcastReceiver(),
            IntentFilter("com.example.broadcast.MY_NOTIFICATION1")
        )
*/
        //レシーバー匿名クラス
        registerReceiver(
            object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    // インテントに登録されている、名前"data"に対応する文字列をトーストで表示する
                    Toast.makeText(context, intent.getStringExtra("data")+"main", Toast.LENGTH_SHORT).show()
                    latitude= intent.getDoubleExtra("lat",0.0)
                    longitude= intent.getDoubleExtra("lng",0.0)
                    cameraPosition(mMap)
                }
            },
            IntentFilter("com.example.broadcast.MY_NOTIFICATION1")
        )


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //ピンマーカーのクリックイベントリスナー
        mMap.setOnMarkerClickListener { marker ->
            val id = marker.id.replace("m", "").toInt()
            Toast.makeText(this, id.toString(), Toast.LENGTH_SHORT).show()
            //click時にカメラを移動する trueは移動しない
            false
        }

        //ピンマーカーにセットしたタイトルのクリックイベントリスナー
        mMap.setOnInfoWindowClickListener { marker ->
            val id = marker.id.replace("m", "").toInt()
            Toast.makeText(this, id.toString(), Toast.LENGTH_SHORT).show()
        }


        cameraPosition(mMap)
        setMarker(mMap)
    }

    fun cameraPosition(mMap:GoogleMap){
        var defaultPosition = LatLng(latitude,longitude)
        //カメラオブジェクト zoomレベルは小さいほど広角になっていく。
        val camera = CameraUpdateFactory.newLatLngZoom(defaultPosition, 15f)
        //カメラの移動
        mMap.moveCamera(camera)
    }

    fun setMarker(mMap:GoogleMap){
        val options = MarkerOptions()
        options.position( LatLng(35.70436915, 139.57947137) )
        mMap.addMarker(options);

        options.position( LatLng(35.70138951, 139.5772505) )
        options.title("test")
        mMap.addMarker(options);
    }





    fun call_postApi(baseurl: String?,method: String,parameterModels: ArrayList<ParameterModel?>?) {
        API().call_POSTApi(
            baseurl,
            parameterModels,
            method,
            object : Callback {
                @Throws(IOException::class)
                override fun onResponse(@NotNull call: Call, @NotNull response: Response) {
                    Parseresonse(response.body!!.string(), method)
                }

                override fun onFailure(@NotNull call: Call, @NotNull e: IOException) {
                    Log.d("execption==", e.toString())
                    runOnUiThread {
                        DynamicToast.makeError(this@MainActivity, "エラー1000", 2500).show()
                    }
                }
            }
        )
        Log.d("call_postApi","HttpAccess")
    }



    fun call_GetApi(
        baseurl: String?,
        method: String,
        parameterModels: ArrayList<ParameterModel?>?
    ) { // parameters will be added after ? or & as wonderful Get method

        API().call_GETApi(baseurl, parameterModels, method, object : Callback {
            override fun onFailure(@NotNull call: Call, @NotNull e: IOException) {

                runOnUiThread { DynamicToast.makeError(this@MainActivity, "エラー200", 2500).show() }
            }

            @Throws(IOException::class)
            override fun onResponse(@NotNull call: Call, @NotNull response: Response) {

                Parseresonse(response.body!!.string(), method)
            }
        })
    }



    private fun Parseresonse(json: String, method: String) {
        Log.d("params===", json)

        try {
            val response = JSONObject(json)
            if (method == "json") {
                if (response.has("error_message")) {
                    runOnUiThread {
                        try {
                            DynamicToast.makeError(
                                this@MainActivity,
                                response.getString("error_message"),
                                2500
                            ).show()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    runOnUiThread { returnapireponse(response, method) }
                }
            } else {
                val result_code = response.getInt("result_code")
                if (result_code == 0) {
                    runOnUiThread { returnapireponse(response, method) }
                } else {
                    runOnUiThread {
                        try {
                            DynamicToast.makeError(
                                this@MainActivity,
                                response.getString("message"),
                                2500
                            ).show()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            runOnUiThread { DynamicToast.makeError(this@MainActivity, "エラー300", 2500).show() }
        }
    }

    fun returnapireponse(response: JSONObject?, method: String?) {}


    fun startlocationservice() {
        //============= GPS service==========================
        requestPermissionsSafely(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200
        )
        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (!isMyServiceRunning(mLocationService.javaClass, this)) {
            startService(mServiceIntent)
            Toast.makeText(
                this,
                "サービススタート",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "サービス実行中",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun endlocationservice() {
        if (mServiceIntent != null) {
            Log.d("stopservice==", "stopservie")
            stopService(mServiceIntent)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissionsSafely(
        permissions: Array<String?>?,
        requestCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions!!, requestCode)
        }
    }

    override fun onDestroy() {
        if (mServiceIntent != null) {
            stopService(mServiceIntent)
        }
        super.onDestroy()
    }


}

