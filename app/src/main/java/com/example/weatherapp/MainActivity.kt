package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {

    // IMPORTANT: Paste the API key your team generated here
    private val apiKey = "728020102a1f44c9b8e31751261208"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Link UI Components
        val etCityName = findViewById<EditText>(R.id.etCityName)
        val btnSearchWeather = findViewById<Button>(R.id.btnSearchWeather)
        val tvCityName = findViewById<TextView>(R.id.tvCityName)
        val tvTemperature = findViewById<TextView>(R.id.tvTemperature)
        val tvCondition = findViewById<TextView>(R.id.tvCondition)
        val tvHumidity = findViewById<TextView>(R.id.tvHumidity)
        val tvWindSpeed = findViewById<TextView>(R.id.tvWindSpeed)
        val tvError = findViewById<TextView>(R.id.tvError)

        //Set up the Click Listener
        btnSearchWeather.setOnClickListener {
            val city = etCityName.text.toString().trim()

            //Validate Input (Case 1: Empty City Name)
            if (city.isEmpty()) {
                tvError.text = "Please enter a city name."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Hide error if input is valid
            tvError.visibility = View.GONE

            //Construct the GET Request URL
            val url = "https://api.weatherapi.com/v1/current.json?key=$apiKey&q=$city"
            val queue = Volley.newRequestQueue(this)

            //Create the API Request
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    // 6. Parse JSON on Success
                    try {
                        val locationObj = response.getJSONObject("location")
                        val cityName = locationObj.getString("name")

                        val currentObj = response.getJSONObject("current")
                        val temp = currentObj.getDouble("temp_c")
                        val condition = currentObj.getJSONObject("condition").getString("text")
                        val humidity = currentObj.getInt("humidity")
                        val wind = currentObj.getDouble("wind_kph")

                        //Display the Data
                        tvCityName.text = "City: $cityName"
                        tvTemperature.text = "Temperature: $temp°C"
                        tvCondition.text = "Condition: $condition"
                        tvHumidity.text = "Humidity: $humidity%"
                        tvWindSpeed.text = "Wind Speed: $wind km/h"

                    } catch (e: Exception) {
                        tvError.text = "Error parsing response data."
                        tvError.visibility = View.VISIBLE
                    }
                },
                { error ->
                    //Handle Errors (Case 2, 3, & 4)
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 400) {
                            tvError.text = "Invalid city name. Please try again."
                        } else {
                            tvError.text = "API Error. Please try again later."
                        }
                    } else {
                        tvError.text = "Network error. Please check your internet connection."
                    }
                    tvError.visibility = View.VISIBLE
                }
            )

            //Execute the Request
            queue.add(jsonObjectRequest)
        }
    }
}