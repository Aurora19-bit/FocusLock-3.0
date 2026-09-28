package com.vidhya.focuslock

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MathPuzzleActivity : AppCompatActivity() {

    private lateinit var tvEquation: TextView
    private lateinit var etAnswer: EditText
    private lateinit var btnSubmit: Button
    private lateinit var sessionManager: SessionManager

    private var correctAnswer: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        sessionManager = SessionManager(this)

        tvEquation = findViewById(R.id.tvEquation)
        etAnswer = findViewById(R.id.etAnswer)
        btnSubmit = findViewById(R.id.btnSubmit)

        generatePuzzle()

        btnSubmit.setOnClickListener {
            val userText = etAnswer.text.toString().trim()
            val userVal = userText.toIntOrNull()

            if (userVal == null) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userVal == correctAnswer) {
                Toast.makeText(this, "Correct! Granted 2-minute emergency pass.", Toast.LENGTH_LONG).show()
                sessionManager.grantEmergencyPass(2)
                finish()
            } else {
                Toast.makeText(this, "Incorrect! Keep your focus!", Toast.LENGTH_SHORT).show()
                generatePuzzle()
                etAnswer.text.clear()
            }
        }
    }

    private fun generatePuzzle() {
        val a = Random.nextInt(15, 60)
        val b = Random.nextInt(12, 35)
        val c = Random.nextInt(20, 80)
        correctAnswer = (a * b) - c
        tvEquation.text = "$a × $b - $c = ?"
    }
}
