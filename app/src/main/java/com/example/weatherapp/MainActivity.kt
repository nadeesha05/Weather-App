package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

/**
 * MainActivity handles the backend logic for the Weather App, including
 * user input validation, API requests using Volley, and JSON parsing.
 */
class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var etCityName: EditText
    private lateinit var btnSearchWeather: Button
    private lateinit var tvCityName: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvError: TextView

    // Volley Request Queue for handling network operations
    private lateinit var requestQueue: RequestQueue

    // API configuration details
    private val apiKey = "728020102a1f44c9b8e31751261208"
    private val baseUrl = "https://api.weatherapi.com/v1/current.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Connect all UI components using findViewById
        initializeUI()

        // Handle Window Insets for edge-to-edge compatibility
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 6. Create a Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this)

        // Set Click Listener for the Search Button
        btnSearchWeather.setOnClickListener {
            // 2. Read the city entered by the user
            val city = etCityName.text.toString()

            // 3. Trim unnecessary spaces
            val trimmedCity = city.trim()

            // 4. Input validation: If the input is empty, do not call the API
            if (trimmedCity.isEmpty()) {
                showError(getString(R.string.error_empty_city))
            } else {
                // Clear previous errors and fetch weather data
                hideError()
                fetchWeather(trimmedCity)
            }
        }
    }

    /**
     * Connects the UI elements from the layout to the Activity variables.
     */
    private fun initializeUI() {
        etCityName = findViewById(R.id.etCityName)
        btnSearchWeather = findViewById(R.id.btnSearchWeather)
        tvCityName = findViewById(R.id.tvCityName)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvCondition = findViewById(R.id.tvCondition)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWindSpeed = findViewById(R.id.tvWindSpeed)
        tvError = findViewById(R.id.tvError)
    }

    /**
     * Builds the request, sends it via Volley, and handles the response.
     */
    private fun fetchWeather(cityName: String) {
        // 5. Build the request URL using Base URL, API Key, and City Name
        val url = "$baseUrl?key=$apiKey&q=$cityName"

        // 7. Use JsonObjectRequest for GET request
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // 9. When successful: Parse the JSON response
                try {
                    // Extracting "location" object for City Name
                    val location = response.getJSONObject("location")
                    val name = location.getString("name")

                    // Extracting "current" object for Temp, Humidity, and Wind Speed
                    val current = response.getJSONObject("current")
                    val tempC = current.getDouble("temp_c")
                    val humidity = current.getInt("humidity")
                    val windKph = current.getDouble("wind_kph")

                    // Extracting nested "condition" object for Weather Condition text
                    val condition = current.getJSONObject("condition")
                    val conditionText = condition.getString("text")

                    // Update the UI TextViews with extracted data
                    updateWeatherUI(name, tempC, conditionText, humidity, windKph)

                } catch (e: JSONException) {
                    e.printStackTrace()
                    showError(getString(R.string.error_parsing))
                }
            },
            { error ->
                // 11, 12, 13. Handle various error cases (Network, API, Invalid City)
                handleRequestError(error)
            }
        )

        // 8. Send the asynchronous GET request
        requestQueue.add(jsonObjectRequest)
    }

    /**
     * Updates the TextViews with the weather data and hides the error message.
     */
    private fun updateWeatherUI(city: String, temp: Double, condition: String, humidity: Int, wind: Double) {
        tvCityName.text = getString(R.string.label_city, city)
        tvTemperature.text = getString(R.string.label_temperature, temp)
        tvCondition.text = getString(R.string.label_condition, condition)
        tvHumidity.text = getString(R.string.label_humidity, humidity)
        tvWindSpeed.text = getString(R.string.label_wind_speed, wind)

        // 15. Hide tvError after a successful response
        hideError()
    }

    /**
     * Handles errors from the Volley request and displays appropriate messages.
     */
    private fun handleRequestError(error: VolleyError) {
        val networkResponse = error.networkResponse
        if (networkResponse != null) {
            // Case 2: Handle invalid city responses (Status Code 400 for WeatherAPI)
            if (networkResponse.statusCode == 400) {
                showError(getString(R.string.error_invalid_city))
            } else {
                // Case 4: API Failure
                showError(getString(R.string.error_api_failure))
            }
        } else if (error is NoConnectionError || error is NetworkError) {
            // Case 3: No Internet
            showError(getString(R.string.error_no_connection))
        } else {
            // Case 4: API Failure
            showError(getString(R.string.error_api_failure))
        }
    }

    /**
     * Displays an error message to the user using the tvError TextView.
     */
    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    /**
     * Hides the error message TextView.
     */
    private fun hideError() {
        tvError.visibility = View.GONE
    }
}
