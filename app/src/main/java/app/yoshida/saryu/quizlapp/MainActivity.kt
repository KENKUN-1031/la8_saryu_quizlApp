package app.yoshida.saryu.quizlapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import app.yoshida.saryu.quizlapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val GPT_ENDPOINT = "https://api.openai.com/v1/chat/completions"
    private val API_KEY = BuildConfig.OPENAI_API_KEY

    private val client = OkHttpClient() //httpClientのインスタンス化

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        binding.quizStartButton.setOnClickListener { //クリックされたタイミング
            var inputText = binding.urlInputEditText.text.toString().trim() //入力されたurlの取得
            if (!inputText.startsWith("http://") && !inputText.startsWith("https://")) {
                inputText = "https://$inputText"
            }

            CoroutineScope(Dispatchers.IO).launch {
                val textData = fetchTextFromUrl(inputText) //ここで失敗してる可能性
//                Log.d("TextData", textData.toString())
                if (textData != null) {
                    // テキストデータの取得に成功した場合の処理
                    val gptResponse = sendGPTRequest(inputText)
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@MainActivity, QuizActivity::class.java)
                        intent.putExtra("GPT_RESPONSE", gptResponse)
                        startActivity(intent)
                    }
                } else {
                    // エラー処理
                    println("データの取得に失敗しました。")
                }


            }

            Log.d("ButtonPressed", inputText)
        }
    }

    private fun sendGPTRequest(queryText: String): String? {
        val client = OkHttpClient()
        val jsonRequest = """
            {
                "model": "gpt-4o-mini",
                "messages": [
                    {
                        "role": "user",
                        "content": "$queryText この内容に関して3択クイズを作るならどんな問題と答えにするかを実際に3問出してみて欲しい！"
                    }
                ]
            }
        """.trimIndent()
        val requestBody = jsonRequest.toRequestBody("application/json".toMediaTypeOrNull())
        val request =
            Request.Builder().url(GPT_ENDPOINT).addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $API_KEY").post(requestBody).build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) { //gptからレスポンスが帰ってきた時の処理 ←ここ大事
                val answer = parseGPTResponse(responseBody)
                editResponse(answer.toString()) //resを編集する関数にresを渡す
                Log.d("MainActivity", answer.toString())
                return answer
            } else {
                Log.e("MainActivity", "エラーがー発生しました！！！！: ${responseBody}")
                return "エラーが発生しました"
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "APIリクエストエラー: ${e.message}")
            return "ネットワークエラーが発生しました"
        }
    }

    private fun parseGPTResponse(responseBody: String): String? {
        return try {
            val jsonObj = JSONObject(responseBody)
            val choices: JSONArray = jsonObj.getJSONArray("choices")
            val choice = choices.getJSONObject(0)
            val message = choice.getJSONObject("message")
            message.getString("content")
        } catch (e: JSONException) {
            Log.e("MainActivity", "レスポンス解析エラー: ${e.message}")
            "解析エラーが発生しました"
        }
    }

    private fun editResponse(gptResponse: String){
        val test = """
                質問1: 次のうち、JavaScriptのフレームワークではないものはどれか？
                a) React
                b) Angular
                c) PHP
                【答え】 c) PHP
                
                質問2: 次のうち、プログラミング言語でないものはどれか？
                a) Java
                b) Python
                c) Photoshop
                【答え】 c) Photoshop
                
                質問3: 次のうち、Web開発で使用されるCSSフレームワークはどれか？
                a) Bootstrap
                b) jQuery
                c) Swift
                【答え】 a) Bootstrap
        """.trimIndent()
    }

    // サスペンド関数としてネットワーク操作を定義
    private suspend fun fetchTextFromUrl(url: String): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        return@withContext try {
            // リクエストを実行してレスポンスを取得
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // レスポンスが不成功の場合はnullを返す
                    null
                } else {
                    // レスポンスボディを文字列として返す
                    response.body?.string()
                }
            }
        } catch (e: IOException) {
            // エラーメッセージをログに出力
            Log.e("Network Error", "Error fetching data from URL", e)
            null
        }
    }
}