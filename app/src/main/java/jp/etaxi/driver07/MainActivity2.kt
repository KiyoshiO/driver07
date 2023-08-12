package jp.etaxi.driver07

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

var dummy =0
var ttt=0

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val text = intent.getStringExtra("TEXT_KEY")
        val message: TextView=findViewById(R.id.textView)
        val message0: TextView=findViewById(R.id.textView2)
        var button1=findViewById<Button>(R.id.button1)
        var button2=findViewById<Button>(R.id.button2)
        var button3=findViewById<Button>(R.id.button3)

        button1.setText("")
        button2.setText("")

        message0.setText("お客様回答待ち")
        message.setText(text)

        button1.setOnClickListener{
            if (button1.text=="迎車場所到着通知（２回目）"){
                button1.setText("迎車場所到着通知（３回目）")
            }
            if (button1.text=="迎車場所到着通知（１回目）"){
                button1.setText("迎車場所到着通知（２回目）")
            }
            if (button1.text=="迎車回送開始"){
                button1.setText("迎車場所到着通知（１回目）")
                button2.text="　　乗　　車　　"
            }
        }

        button2.setOnClickListener{
            if (button2.text=="　　乗　　車　　"){
                button2.setText("　　降　　車　　")
                button1.setText("             ")
            } else {
                if (button2.text=="　　降　　車　　"){
                    toBack()

                }
            }

        }

        button3.setOnClickListener{
                 toBack()


        }


        //レシーバー匿名クラス
        registerReceiver(
            object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    // インテントに登録されている、名前"data"に対応する文字列をトーストで表示する
                    //Toast.makeText(context, intent.getStringExtra("data")+"main", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, dummy.toString()+"--"+ttt.toString(), Toast.LENGTH_SHORT).show()
                    if (dummy==20){
                        message0.setText("迎車確定")
                        button1.setText("迎車回送開始")
                        dummy=0
                        ttt=ttt+1
                    } else {
                        if(button1.text=="") {
                            dummy = dummy + 1
                        }
                    }

                }
            },
            IntentFilter("com.example.broadcast.MY_NOTIFICATION1")
        )



    }

    override fun onResume() {
        super.onResume()
    // 初期化したい処理を書く
        dummy=0

    }

fun toBack(){

    val intent = Intent(this@MainActivity2, MainActivity::class.java)
    startActivity(intent)

}






}