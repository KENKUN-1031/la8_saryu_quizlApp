package app.yoshida.saryu.quizlapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.yoshida.saryu.quizlapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
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
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(this.root) }

        binding.quizStartButton.setOnClickListener {
            val inputText = binding.urlInputEditText.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val gptResponse = sendGPTRequest(inputText)

                withContext(Dispatchers.Main) {
                    val intent = Intent(this@MainActivity, QuizActivity::class.java)
                    intent.putExtra("GPT_RESPONSE", gptResponse)
                    startActivity(intent)
                }
            }

            Log.d("ButtonPressed", inputText)
        }
    }

    private fun sendGPTRequest(queryText: String): String? {
        val client = OkHttpClient()
        val jsonRequest = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "user",
                        "content": "$queryText"
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
}