package com.example.weatherwise

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import com.example.weatherwise.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchWeatherData("delhi")
        searchCity()
    }

    private fun searchCity() {
        val searchView = binding.svSearch
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                fetchWeatherData(query?:"delhi")
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(WeatherApi::class.java)

        val response = retrofit.getWeather(cityName, "34a4d18ef7e96a7a84c422c583754cc5", "metric")
        response.enqueue(object: Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if(response.isSuccessful && responseBody != null) {
                    bindValues(response)

            }

        }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity, "There might be an Error with the entered query.", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun bindValues(response: Response<WeatherApp>) {
        val responseBody = response.body()!!

        //creating all variables
        val temperature = responseBody.main.temp.toString()
        val humidity = responseBody.main.humidity
        val windSpeed = responseBody.wind.speed
        val sunRise = responseBody.sys.sunrise
        val sunSet = responseBody.sys.sunset
        val seaLevel = responseBody.main.pressure
        val condition = responseBody.weather.firstOrNull()?.main?: "Unknown"
        val maxTemp = responseBody.main.temp_max
        val minTemp = responseBody.main.temp_min
        val cityName = responseBody.name

        //bind to UI
        binding.tvTemperature.text = "$temperature °C"
        binding.tvCondition.text = condition
        binding.tvHumidity.text = humidity.toString() + " %"
        binding.tvMaxTemp.text = "Max Temp: " + maxTemp.toString() + " °C"
        binding.tvMinTemp.text = "Min Temp: " + minTemp.toString() + " °C"
        binding.tvSea.text = seaLevel.toString() + " hPa"
        binding.tvWeatherCondition.text = condition
        binding.tvSunrise.text = setTime(sunRise.toLong())
        binding.tvSunset.text = setTime(sunSet.toLong())
        binding.tvWind.text = windSpeed.toString() + " m/s"
        binding.tvCityName.text = cityName
        binding.tvDay.text = setDay()
        binding.tvDate.text = setDate()

        changeImagesWithWeatherCondition(condition)

    }

    private fun changeImagesWithWeatherCondition(condition: String) {
        when(condition) {

            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Haze", "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }

        binding.lottieAnimationView.playAnimation()
    }

    private fun setDay(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())

    }

    private fun setDate(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setTime(timeStamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timeStamp*1000))
    }
}