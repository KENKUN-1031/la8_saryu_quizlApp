package app.yoshida.saryu.quizlapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
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
            binding.answerButton1.setOnClickListener {
                checkAnswer(binding.answerButton1.text.toString())
            }
            binding.answerButton2.setOnClickListener {
                checkAnswer(binding.answerButton2.text.toString())
            }
            binding.answerButton3.setOnClickListener {
                checkAnswer(binding.answerButton3.text.toString())
            }
            binding.nextButton.setOnClickListener {
                if (quizCount == quizList.size){
                    val resultIntent: Intent = Intent(this, ResultActivity::class.java)
                    resultIntent.putExtra("QuizCount", quizList.size)
                    resultIntent.putExtra("CorrectCount", correctCount)
                    startActivity(resultIntent)

                }else{
                    binding.judgeImage.isVisible = false
                    binding.nextButton.isVisible = false
                    binding.answerButton1.isEnabled = true
                    binding.answerButton2.isEnabled = true
                    binding.answerButton3.isEnabled = true
                    binding.correctAnswerText.text = ""
                    displayQuestion(quizList)
                }
            }
        } else {
            Log.d("Quiz", "No data received!")
        }
    }

    fun displayQuestion(quizList: MutableList<ArrayList<String>>){
        val question: List<String> = quizList[quizCount]
        binding.quizText.text = question[0]
        val number = quizCount + 1
        binding.questionNumber.text = "Question $number"
        binding.answerButton1.text = question[1]
        binding.answerButton2.text = question[2]
        binding.answerButton3.text = question[3]

        correctAnswer = question[4]
    }

    fun checkAnswer(answerText: String){
        if (answerText == correctAnswer) {
            binding.judgeImage.setImageResource(R.drawable.maru_image)
            binding.judgeImage.bringToFront()
            correctCount++
        }else {
            binding.judgeImage.setImageResource(R.drawable.batu_image)
            binding.judgeImage.bringToFront()
        }
        showAnswer()
        quizCount++
    }

    fun showAnswer(){
        binding.correctAnswerText.text = "正解: $correctAnswer"
        binding.judgeImage.isVisible = true
        binding.nextButton.isVisible = true
        binding.answerButton1.isEnabled = false
        binding.answerButton2.isEnabled = false
        binding.answerButton3.isEnabled = false
    }
}