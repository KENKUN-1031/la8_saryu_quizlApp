package app.yoshida.saryu.quizlapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.yoshida.saryu.quizlapp.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater).apply { setContentView(this.root)}
        val quizList = intent.getSerializableExtra("GPT_RESPONSE") as? MutableList<*>

        if (quizList != null) {
            // Iterate through the quizList and use it as needed
            for (quiz in quizList) {
                Log.d("Quiz", quiz.toString())
            }
        } else {
            Log.d("Quiz", "No data received!")
        }
    }
}