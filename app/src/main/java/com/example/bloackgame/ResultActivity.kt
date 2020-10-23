package com.example.bloackgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val points = intent.getIntExtra(SCORE, 0)
        score.text = points.toString()

        replay.setOnClickListener {
            startActivity(Intent(this, PlayActivity::class.java))
            finish()
        }
    }
}