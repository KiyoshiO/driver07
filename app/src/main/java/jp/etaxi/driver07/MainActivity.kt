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
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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

    var mapReprintSpan=10
    var countDownNumber=30
    var dummyCount=0
    var oneShotFlg: Boolean=true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val messageArea= findViewById<TextView>(R.id.message)
        button1.setOnClickListener {
            //Toast.makeText(this, "受諾", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@MainActivity, MainActivity2::class.java)
            //生成したオブジェクトを引数に画面を起動！
            intent.putExtra("TEXT_KEY","東京都葛飾区青戸８丁目７−１９" + "\n" +"セブン-イレブン 葛飾青戸８丁目店")
            startActivity(intent)

        }


        button3.setOnClickListener {
            //Toast.makeText(this, "拒否", Toast.LENGTH_SHORT).show()
            oneShotFlg=true
            button1.visibility=View.INVISIBLE
            button2.visibility=View.INVISIBLE
            button3.visibility=View.INVISIBLE
            countDownNumber=30
            dummyCount=0
            messageArea.setText("待機中")
        }


        button1.visibility=View.INVISIBLE
        button2.visibility=View.INVISIBLE
        button3.visibility=View.INVISIBLE
        messageArea.setText("待機中")
        var address1:String="東京都葛飾区青戸８丁目７−１９" + "\n" +"セブン-イレブン 葛飾青戸８丁目店"

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
                    //Toast.makeText(context, intent.getStringExtra("data")+"main", Toast.LENGTH_SHORT).show()
                    latitude= intent.getDoubleExtra("lat",0.0)
                    longitude= intent.getDoubleExtra("lng",0.0)

                    //マップ更新タイミング
                    if (mapReprintSpan ==0){
                        //位置情報を取得後の処理
                        cameraPosition(mMap)
                        setMarker(mMap)
                        //button.setText(latitude.toString())
                        mapReprintSpan=10
                    } else {
                        mapReprintSpan = mapReprintSpan -1

                    }

                    if (oneShotFlg){
                        if (dummyCount>30){
                            button1.visibility=View.VISIBLE
                            button2.visibility=View.VISIBLE
                            button3.visibility=View.VISIBLE
                            oneShotFlg=false

                            messageArea.setText(address1)
                        } else {
                            dummyCount=dummyCount+1
                        }
                    }

                    if (button2.visibility==View.VISIBLE) {
                        button2.setText(countDownNumber.toString())
                        countDownNumber=countDownNumber-1
                        if (countDownNumber==0){
                            button1.visibility=View.INVISIBLE
                            button2.visibility=View.INVISIBLE
                            button3.visibility=View.INVISIBLE
                            countDownNumber=30
                            messageArea.setText("待機中")
                            dummyCount=0
                            oneShotFlg=true
                        }
                    }



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
            //Toast.makeText(this, id.toString(), Toast.LENGTH_SHORT).show()
            //click時にカメラを移動する trueは移動しない
            false
        }

        //ピンマーカーにセットしたタイトルのクリックイベントリスナー
        mMap.setOnInfoWindowClickListener { marker ->
            val id = marker.id.replace("m", "").toInt()
            //Toast.makeText(this, id.toString(), Toast.LENGTH_SHORT).show()
        }


        cameraPosition(mMap)
        setMarker(mMap)
    }




    //ｈｔｔｐアクセステスト
    fun test(){
        var pas= ArrayList<ParameterModel?>()
        pas.add (ParameterModel("userCode","12345689"))
        pas.add (ParameterModel("orderType","nomalTaxi"))
        pas.add (ParameterModel("reserveFlag","now"))

        call_postApi("http://e-taxi.jp/","ordermenu.php",pas)
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
        options.position( LatLng(latitude,longitude) )
        mMap.addMarker(options);

        //options.position( LatLng(35.70138951, 139.5772505) )
        //options.title("test")
        //mMap.addMarker(options);
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
                        DynamicToast.makeError(this@MainActivity, "エラー100", 2500).show()
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
            //Toast.makeText(this,"サービススタート",Toast.LENGTH_SHORT).show()
        } else {
            //Toast.makeText(this,"サービス実行中",Toast.LENGTH_SHORT).show()
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

