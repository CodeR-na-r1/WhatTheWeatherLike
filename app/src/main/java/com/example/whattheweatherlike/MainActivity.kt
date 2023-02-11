package com.example.whattheweatherlike

import com.example.whattheweatherlike.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.reflect.Type
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherAadapter: WeatherRecycleAdapter

    lateinit var API_KEY: String

    private var cities = mutableListOf<String>()
    private var weatherInfoList = mutableListOf<Weather>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myLog", "onCreate")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        this.API_KEY = resources.getString(R.string.API_KEY)

        weatherAadapter = WeatherRecycleAdapter(this.weatherInfoList as ArrayList<Weather>)

        if (findViewById<ImageView>(R.id.weatherView) == null){
            binding.weatherList.apply {
                layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
                adapter = weatherAadapter
            }
        }
        else {
            binding.weatherList.apply {
                layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
                adapter = weatherAadapter
            }
        }

        binding.btnAddCity?.let{
            it.setOnClickListener {
                if (addCity(binding.inputCityName?.text.toString())) {
                    this.getWeatherForCity(binding.inputCityName?.text.toString())
                    binding.inputCityName?.setText("")
                }
            }
        }

        binding.btnUpdateTemp?.let{
            it.setOnClickListener {
                this.updateWeatherForAllCities()
            }
        }
    }

    private fun addCity(text: String): Boolean {
        return if (text == "") {
            Toast.makeText(this, "The name of the city is empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            this.cities.add(text)
            true
        }
    }

    private fun requestWeatherForCity(nameCity: String): Weather {
        val weatherURL =
            "https://api.openweathermap.org/data/2.5/weather?q=${nameCity}&appid=${this.API_KEY}&units=metric";
        var data = ""
        val weather = Weather()
        weather.city = nameCity

        lateinit var stream: InputStream

        try {
            stream = URL(weatherURL).getContent() as InputStream
        } catch (e: IOException) {
            weather.city = "Error Internet connection!"
        }

        try {
            data = Scanner(stream).nextLine() ?: ""

            val jsonObj = JSONObject(data)

            weather.temp = jsonObj.getJSONObject("main").getString("temp").let { "$it°" }
            weather.humidity = jsonObj.getJSONObject("main").getString("humidity").let { "$it%" }
            weather.iconName = JSONObject(jsonObj.getJSONArray("weather").getString(0)).getString("icon").let { "_${it.subSequence(1, it.length)}.png" }
        } catch (e: Exception) {
                weather.city = "Ошибка запроса! (проверьте название города)"
        }

        return weather
    }

    private fun updateWeatherForAllCities() {
        GlobalScope.launch(Dispatchers.IO) {
            weatherInfoList.clear()

            cities.forEach { it -> getWeatherForCity(it) }
        }
    }

    private fun getWeatherForCity(nameCity: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val resWeather = requestWeatherForCity(nameCity)
            weatherInfoList.add(resWeather)
            withContext(Dispatchers.Main) {
                weatherAadapter.updateData(ArrayList<Weather>(weatherInfoList))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("myLog", "onPause")

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        val dataCities = Gson().toJson(this.cities)
        sharedPreferences.edit().putString("citiesData", dataCities).apply()

        val dataWeatherInfo = Gson().toJson(this.weatherInfoList)
        sharedPreferences.edit().putString("weatherInfoData", dataWeatherInfo).apply()
    }

    override fun onResume() {
        super.onResume()
        Log.d("myLog", "onResume")

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        // load list of cities

        val dataCitiesJson = sharedPreferences.getString("citiesData", "")

        try {
            val type: Type = object : TypeToken<MutableList<String>>() {}.type
                this.cities = Gson().fromJson<MutableList<String>>(dataCitiesJson, type)
        }
        catch (e: Exception) {
            Log.d("myLog", "Error Load Data about cities!")
        }

        // load list with weather for cities

        val dataWeatherInfoJson = sharedPreferences.getString("weatherInfoData", "")

        try {
            val type: Type = object : TypeToken<MutableList<Weather>>() {}.type
            this.weatherInfoList = Gson().fromJson<MutableList<Weather>>(dataWeatherInfoJson, type)
        }
        catch (e: Exception) {
            Log.d("myLog", "Error Load Data about weather!")
        }

        // update adapter data who we are load now
        (this.binding.weatherList.adapter as WeatherRecycleAdapter).updateData(ArrayList(this.weatherInfoList))
    }
}