package jp.etaxi.driver07

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // インテントに登録されている、名前"data"に対応する文字列をトーストで表示する
        Toast.makeText(context, intent?.getStringExtra("data"), Toast.LENGTH_SHORT).show()


    }
}