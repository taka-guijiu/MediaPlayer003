package io.github.taka_guijiu.mediaplayer.mediaplayer003
// 参考資料
// 斎藤著「Androidアプリ開発の教科書Kotlin対応　第12章



import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException

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
//                                btnBack.setEnabled(true)
//                                btnForward.setEnabled(true)
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
                    tvCurPos.text = mediaPlayer.currentPosition.toString()  // 一時停止した時にcurrentPosition(ミリ秒)を表示する
                    mediaPlayer.pause()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                btnPlay.text = getString(R.string.play)
            }
        }

        btnStop.setOnClickListener {
            try {
                tvCurPos.text = mediaPlayer.currentPosition.toString()
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


}