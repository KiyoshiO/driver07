package jp.etaxi.driver07

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

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
        var defaultPosition = LatLng(35.70316439, 139.57984714)
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




}