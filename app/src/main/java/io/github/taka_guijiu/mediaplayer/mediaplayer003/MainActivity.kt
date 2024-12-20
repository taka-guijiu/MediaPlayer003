package io.github.taka_guijiu.mediaplayer.mediaplayer003
// 参考資料
// 斎藤著「Androidアプリ開発の教科書Kotlin対応　第12章



import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import java.io.IOException
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    val mediaPlayer = MediaPlayer()
    lateinit var btnPlay: Button
    lateinit var btnStop: Button
    lateinit var swLoopOn: Switch
    lateinit var tvCurPos: TextView

    val getContentSound =
        registerForActivityResult(StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val resultIntent = it.data
                val uri: Uri? = resultIntent?.data

                mediaPlayer.apply {
                    stop()
                    reset()
                }

                if (uri != null) {
                    mediaPlayer.apply {
                        setDataSource(this@MainActivity, uri)   // 音源を設定
                        //メディアソースの再生準備が整ったときに呼び出されるコールバックの登録する
                        setOnPreparedListener {
                            // 各ボタンをタップ可能に設定
                            btnPlay.setEnabled(true)
                            btnPlay.text = getString(R.string.play)
                            btnStop.setEnabled(true)
                        }

                        // 再生中にメディアソースの終端に到達したときに呼び出されるコールバックを登録
                        setOnCompletionListener {
                            // ループ設定がされていなければ
                            if (!isLooping()) {
                                // 再生ボタンのラベルを「再生」に設定
                                btnPlay.text = getString(R.string.play)
                            }
                        }
                        prepareAsync()
                    }
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        btnPlay = findViewById<Button>(R.id.btnPlay)
        btnStop = findViewById<Button>(R.id.btnStop)
        swLoopOn = findViewById<Switch>(R.id.swLoop)
        tvCurPos = findViewById<TextView>(R.id.tvCurPos)

        btnPlay.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                try {
                    mediaPlayer.start()
                    btnPlay.text = getString(R.string.pause)
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    // 再生を一時停止
                    mediaPlayer.pause()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                btnPlay.text = getString(R.string.play)
            }
        }

        btnStop.setOnClickListener {
            try {
                //tvCurPos.text = "CurPos:" + mediaPlayer.currentPosition.toString()
                mediaPlayer.stop()
                mediaPlayer.prepare()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            btnPlay.text = getString(R.string.play)
        }

        // ループスイッチの状態が変化したときに、MediaPlayerのインスタンスに反映
        swLoopOn.setOnCheckedChangeListener(
            object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(
                    buttonView: CompoundButton?,
                    isChecked: Boolean,
                ) {
                    mediaPlayer.isLooping = isChecked
                }
            }
        )

        /* 現在時刻を表示する
            参考：https://qiita.com/kenichiro-yamato/items/161b43d152f32a3248c7
                 https://www.project-unknown.jp/entry/2018/10/09/015515
         */
        timer(name = "testTimer", period = 1000) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            HandlerCompat.createAsync(mainLooper).post {
                val tvClock = findViewById<TextView>(R.id.tvClock)
                tvClock.text = "時刻：${hour}時 ${minute}分 ${second}秒"
            }
        }

        // 再生経過時間を表示する
        timer(name = "curPos", period = 50L){
            HandlerCompat.createAsync(mainLooper).post{
                tvCurPos.text = "再生経過時間:" + convertMillisTo60(mediaPlayer.currentPosition)  // 一時停止した時にcurrentPosition(ミリ秒)を60進数に変換し表示する
            }
        }
    }


    // アプリを一時的に隠した時の処理
    override fun onPause() {
        super.onPause()
        try {
            mediaPlayer.pause()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // オプションメニュー用xmlファイルをインフレートする
        menuInflater.inflate(R.menu.option_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 選択されたメニューのIDのR値による処理分岐
        when (item.itemId) {
            R.id.openSoundFolder -> {
                // 暗黙インテントにより既成のファイルピッカーを呼び出す
                val iSound = Intent(Intent.ACTION_OPEN_DOCUMENT)
                iSound.type = "audio/mpeg"
                iSound.putExtra(Intent.EXTRA_TITLE, "memo.mp3")
                getContentSound.launch(iSound)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // ミリ秒ー＞60進数変換
    // https://pisuke-code.com/android-ways-to-format-millis/
    fun convertMillisTo60(millis: Int): String? {
        val l = millis % 1000
        val s = millis / 1000 % 60
        val m = millis / 1000 / 60 % 60
        var h = millis / 1000 / (60 * 60) % 24
        h += millis / 1000 / (60 * 60 * 24) * 24
        return String.format("%d:%02d:%02d.%02d", h, m, s, l)
    }


}