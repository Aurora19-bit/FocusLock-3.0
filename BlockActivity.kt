package com.vidhya.focuslock

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
    }

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block)

        sessionManager = SessionManager(this)

        val btnGoHome = findViewById<Button>(R.id.btnGoHome)
        val btnEmergency = findViewById<Button>(R.id.btnEmergency)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)

        val blockedApp = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: "This application"
        tvMessage.text = "You locked $blockedApp to stay focused on your goals!"

        btnGoHome.setOnClickListener {
            // Return to device home screen immediately
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            finish()
        }

        btnEmergency.setOnClickListener {
            // Take user to math puzzle for emergency unlock pass
            val puzzleIntent = Intent(this, MathPuzzleActivity::class.java)
            startActivity(puzzleIntent)
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from slipping back into the restricted app
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
