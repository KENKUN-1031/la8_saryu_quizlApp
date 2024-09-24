package app.yoshida.saryu.quizlapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.yoshida.saryu.quizlapp.databinding.ActivityQuizBinding
import java.util.ArrayList

class QuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizBinding

    var quizCount: Int = 0
    var correctCount: Int = 0
    var correctAnswer: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater).apply { setContentView(this.root)}
        val quizList = intent.getSerializableExtra("GPT_RESPONSE") as? MutableList<ArrayList<String>>
        // quizListにデータが入ってる
        if (quizList != null) {
            //この中に続きの処理を書いてもいい
            displayQuestion(quizList)
            for (quiz in quizList) {
                Log.d("Quiz", quiz.toString())
            }
        } else {
            Log.d("Quiz", "No data received!")
        }
    }

    fun displayQuestion(quizList: MutableList<ArrayList<String>>){
        val question: List<String> = quizList[quizCount]
        binding.quizText.text = question[0]
        binding.answerButton1.text = question[1]
        binding.answerButton2.text = question[2]
        binding.answerButton3.text = question[3]

    }
}