package com.vidhya.focuslock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var tvTimer: TextView
    private lateinit var btnStartLock: MaterialButton
    private lateinit var btnSelectApps: Button
    private lateinit var durationSlider: Slider
    private lateinit var tvSelectedDuration: TextView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        tvTimer = findViewById(R.id.tvTimer)
        btnStartLock = findViewById(R.id.btnStartLock)
        btnSelectApps = findViewById(R.id.btnSelectApps)
        durationSlider = findViewById(R.id.durationSlider)
        tvSelectedDuration = findViewById(R.id.tvSelectedDuration)

        val chip25 = findViewById<View>(R.id.chip25)
        val chip45 = findViewById<View>(R.id.chip45)
        val chip60 = findViewById<View>(R.id.chip60)

        chip25?.setOnClickListener { setDuration(25) }
        chip45?.setOnClickListener { setDuration(45) }
        chip60?.setOnClickListener { setDuration(60) }

        durationSlider.addOnChangeListener { _, value, _ ->
            val minutes = value.toInt()
            tvSelectedDuration.text = "$minutes minutes"
        }

        btnSelectApps.setOnClickListener {
            showAppSelectionDialog()
        }

        btnStartLock.setOnClickListener {
            if (sessionManager.isSessionActive()) {
                // Unlock requires solving the puzzle
                startActivity(Intent(this, MathPuzzleActivity::class.java))
            } else {
                startFocusSession()
            }
        }

        checkPermissions()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setDuration(minutes: Int) {
        durationSlider.value = minutes.toFloat()
        tvSelectedDuration.text = "$minutes minutes"
    }

    private fun startFocusSession() {
        val duration = durationSlider.value.toInt()
        val blockedCount = sessionManager.blockedPackages.size

        if (blockedCount == 0) {
            Toast.makeText(this, "Please select at least 1 app to block first!", Toast.LENGTH_LONG).show()
            showAppSelectionDialog()
            return
        }

        sessionManager.startSession(duration)
        val serviceIntent = Intent(this, SessionTimerService::class.java)
        startService(serviceIntent)

        Toast.makeText(this, "Focus Mode Activated for $duration mins! Stay focused.", Toast.LENGTH_SHORT).show()
        updateUI()
    }

    private fun updateUI() {
        if (sessionManager.isSessionActive()) {
            btnStartLock.text = "Emergency Unlock (Solve Math)"
            btnStartLock.setBackgroundColor(getColor(R.color.red_danger))
            durationSlider.isEnabled = false
            btnSelectApps.isEnabled = false
            startTimerDisplay(sessionManager.getRemainingTimeMs())
        } else {
            countDownTimer?.cancel()
            btnStartLock.text = "Lock & Focus Now"
            btnStartLock.setBackgroundColor(getColor(R.color.emerald_primary))
            durationSlider.isEnabled = true
            btnSelectApps.isEnabled = true
            val mins = durationSlider.value.toInt()
            tvTimer.text = String.format(Locale.getDefault(), "%02d:00", mins)
        }
    }

    private fun startTimerDisplay(durationMs: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                updateUI()
                Toast.makeText(this@MainActivity, "Focus Session Complete! Great job!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun showAppSelectionDialog() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && it.packageName != packageName }
            .sortedBy { pm.getApplicationLabel(it).toString() }

        val appNames = installedApps.map { pm.getApplicationLabel(it).toString() }.toTypedArray()
        val appPackages = installedApps.map { it.packageName }.toTypedArray()
        val checkedItems = BooleanArray(appPackages.size) { index ->
            sessionManager.blockedPackages.contains(appPackages[index])
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Apps to Block")
        builder.setMultiChoiceItems(appNames, checkedItems) { _, which, isChecked ->
            checkedItems[which] = isChecked
        }
        builder.setPositiveButton("Save Selection") { _, _ ->
            val selected = mutableSetOf<String>()
            for (i in checkedItems.indices) {
                if (checkedItems[i]) selected.add(appPackages[i])
            }
            sessionManager.blockedPackages = selected
            Toast.makeText(this, "${selected.size} apps configured for blocklist", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun checkPermissions() {
        // Accessibility prompt
        if (!isAccessibilityServiceEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Enable Focus Accessibility")
                .setMessage("FocusLock requires Accessibility Service to detect when blocked apps are opened and keep you on task.")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = "$packageName/${FocusAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(expected) == true
    }
}
