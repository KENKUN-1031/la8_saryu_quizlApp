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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.util.ArrayList
import kotlin.math.sign

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
//    private val GPT_ENDPOINT = "https://api.openai.com/v1/chat/completions"
//    private val API_KEY = BuildConfig.OPENAI_API_KEY

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
                    val gptResponse: MutableList<ArrayList<String>> = sendGPTRequest2(inputText) //ここで関数読んでる
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@MainActivity, QuizActivity::class.java)
                        intent.putExtra("GPT_RESPONSE", ArrayList(gptResponse))
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

    //APIから帰ってきたレスポンスを加工する
    private fun editResponse(responseText: String): MutableList<ArrayList<String>> {
        val quizzes = responseText.trim().split("\n\n")
        val quizList = mutableListOf<ArrayList<String>>()


        for (quiz in quizzes) {
            val singleQuiz = mutableListOf<String>()
            val lines = quiz.split("\n")
            val question = lines[0].removePrefix("質問: ").trim()
            val choices = lines[1].removePrefix("選択肢: ").trim().split("; ")
            val answer = lines[2].removePrefix("答え: ").trim()

            // 質問、選択肢、答えを使った処理をここに記述
            Log.d("質問", "$question")
            Log.d("選択肢", "$choices")
            Log.d("答え", "$answer")
            // このfor文の中でリストに入れる & for文の外でQuizActivityに変数を渡す
            singleQuiz.add(question)
            for (choice in choices) {
                singleQuiz.add(choice)
            }
            singleQuiz.add(answer)
            quizList.add(singleQuiz as ArrayList<String>)
        }
        Log.d("リストの中身", quizList.toString())
        return quizList
    }


    private fun sendGPTRequest2(queryText: String): MutableList<ArrayList<String>> {
        val client = OkHttpClient()

        // OpenAI APIのエンドポイント
        val GPT_ENDPOINT = "https://api.openai.com/v1/chat/completions"

        // APIキーを設定（実際のキーに置き換えてください）
        val API_KEY = BuildConfig.OPENAI_API_KEY

        // プロンプトの内容を作成
        val contentText = """
        $queryText

        この内容に関して3択クイズを3問作成してください。各クイズは以下の形式で出力してください。

        質問: [質問文]
        選択肢: [選択肢1]; [選択肢2]; [選択肢3]
        答え: [正解の選択肢]

        上記の形式で3問分を出力してください。
    """.trimIndent()

        // JSONオブジェクトを構築
        val messageObject = JSONObject()
        messageObject.put("role", "user")
        messageObject.put("content", contentText)

        val messagesArray = JSONArray()
        messagesArray.put(messageObject)

        val jsonBody = JSONObject()
        jsonBody.put("model", "gpt-4o-mini") // 正しいモデル名を使用
        jsonBody.put("messages", messagesArray)

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(GPT_ENDPOINT)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $API_KEY")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    // レスポンスを解析する関数を呼び出し（実装は省略）
                    val answer = parseGPTResponse(responseBody)
                    // 必要に応じてレスポンスを編集する関数を呼び出し（実装は省略）
                    val finalList: MutableList<ArrayList<String>> = editResponse(answer.toString())
                    Log.d("MainActivity", answer.toString())
                    return finalList
                } else {
                    Log.e("MainActivity", "エラーが発生しました: $responseBody")
                    val error = "エラーが発生しました"
                    val result: MutableList<ArrayList<String>> = mutableListOf(
                        arrayListOf(error)
                    )
                    return result


                }
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "APIリクエストエラー: ${e.message}")
            val error = "ネットワークエラーが発生しました"
            val result: MutableList<ArrayList<String>> = mutableListOf(
                arrayListOf(error)
            )
            return result
        }
    }
}