package edu.uw.eep523.summer2021.takepictures

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.util.*






class MainActivity : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var sensorEnabled = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f


    private lateinit var catSoundMediaPlayer: MediaPlayer
    private var NMAX = 50
    private var mode = "FREE_MODE"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadData()
        reset()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        catSoundMediaPlayer = MediaPlayer.create(this, R.raw.cat_sound);
        val switch = findViewById<Switch>(R.id.switch1)
        val editor = findViewById<EditText>(R.id.editTextNumber)
        switch?.setOnCheckedChangeListener { _, isChecked ->
            var message = ""
            if (isChecked) {
                if (editor.text.isBlank() || editor.inputType.equals(Int)) {
                    NMAX = 10
                } else {
                    NMAX = editor.text.toString().toInt()
                }
                message = "User Defined Mode: ON, NMAX: $NMAX"
                mode = "USER_DEFINED_MODE"
            } else {
                message = "User Defined Mode: OFF"
                mode = "FREE_MODE"
            }
            Toast.makeText(
                this@MainActivity, message,
                Toast.LENGTH_SHORT
            ).show()
        }

        val stop = findViewById<Button>(R.id.stop_but)
        stop.setOnClickListener {
            stopCounter()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorEnabled = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(this, "Accelerator not detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var stepsTaken = findViewById<TextView>(R.id.steps_view)

        if (sensorEnabled) {
            totalSteps = event!!.values[0]

            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            if (currentSteps >= NMAX && mode == "USER_DEFINED_MODE") {
                catSoundMediaPlayer.start()
                sensorEnabled = false
            }
            stepsTaken.text = ("$currentSteps")
        }
    }

    fun reset() {
        var stepsTaken = findViewById<TextView>(R.id.steps_view)
        stepsTaken.setOnClickListener {
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        stepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            stepsTaken.text = 0.toString()
            sensorEnabled = true
            saveData()
            true
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        Log.d("MainActivity", "$savedNumber")

        previousTotalSteps = savedNumber
    }

    private fun stopCounter() {
        sensorEnabled = false
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}


