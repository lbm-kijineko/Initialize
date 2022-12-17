package jp.co.lbm.initialize

import android.annotation.SuppressLint
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listButton = findViewById<Button>(R.id.listButton)
        listButton.setOnClickListener {
            Log.d("DEBUG", "listButton.setOnClickListener")

            val apiurl = "http://10.0.2.2:3000/api/v1/list"

            receiveWebAPI(apiurl)
        }
    }

    /**
     * ファイル一覧を取得するメソッド
     *
     * @param
     */
    @UiThread
    private fun receiveWebAPI(url: String) {
        lifecycleScope.launch {
            // JSON文字列を受け取る
            val result = webAPIBackgroundRunner(url)
            postRunner(result)
        }
    }

    /**
     * 非同期でWebApiにアクセするメソッド
     *
     * @param url
     */
    @WorkerThread
    private suspend fun webAPIBackgroundRunner(url: String): String {
        val resultValue = withContext(Dispatchers.IO) {
            var result = ""
            // URLオブジェクトを生成。
            val url = URL(url)
            // URLオブジェクトからHttpURLConnectionオブジェクトを取得。
            val con = url.openConnection() as? HttpURLConnection
            // conがnullじゃないならば…
            con?.let {
                try {
                    // 接続に使ってもよい時間を設定。
                    it.connectTimeout = 1000
                    // データ取得に使ってもよい時間。
                    it.readTimeout = 1000
                    // HTTP接続メソッドをGETに設定。
                    it.requestMethod = "GET"
                    // 接続。
                    it.connect()

                    val responseCode = it.responseCode
                    Log.d("DEBUG", "ステータスコード = $responseCode")

                    // HttpURLConnectionオブジェクトからレスポンスデータを取得。
                    val stream = it.inputStream
                    // レスポンスデータであるInputStreamオブジェクトを文字列に変換。
                    result = is2String(stream)

                    Log.d("DEBUG", "result = $result")

                    // InputStreamオブジェクトを解放。
                    stream.close()
                }
                catch(e: SocketTimeoutException) {
                    Log.w("DEBUG", "通信タイムアウト", e)
                }
                catch (e: Exception) {
                    Log.d("DEBUG", "例外 = $e")
                }
                // HttpURLConnectionオブジェクトを解放。
                it.disconnect()
            }
            result
        }
        return resultValue
    }

    @UiThread
    private suspend fun postRunner(result: String) {
        Log.d("DEBUG", "result = $result")
//        "DLVoiceFileList":[{"extension":"zip","filepath":"http://localhost:3000/api/v1","filename":"voice","filesize":"","SHA1":"e5f540de1a24e442819dfedeffc9327c6714e7a0"}]
        val rootJSON = JSONObject(result)
        val dlVoiceFileListJSONArray = rootJSON.getJSONArray("DLVoiceFileList")

        Log.d("DEBUG", "dlVoiceFileListJSONArray = $dlVoiceFileListJSONArray")

        for (i in 0 until dlVoiceFileListJSONArray.length()) {
            val voiceFileJsonObject = dlVoiceFileListJSONArray.getJSONObject(i)
            Log.d("DEBUG", "voiceFileJsonObject = $voiceFileJsonObject")

            val extension = voiceFileJsonObject.getString("extension")
            Log.d("DEBUG", "extension = $extension")

            val filepath = voiceFileJsonObject.getString("filepath")
            Log.d("DEBUG", "filepath = $filepath")

            val filename = voiceFileJsonObject.getString("filename")
            Log.d("DEBUG", "filename = $filename")



        }

//        for (i in 0 until dlVoiceFileListJSONArray.length()) {
//            val voiceFileJsonObject = dlVoiceFileListJSONArray.getJSONObject(i)
//
//            Log.d("DEBUG", "voiceFileJsonObject = $voiceFileJsonObject")
//
//        }
    }

    private fun is2String(stream: InputStream): String {
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
        var line = reader.readLine()
        while(line != null) {
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }


    companion object {
//        private const val APP_TAG = "initialize"
    }
}
