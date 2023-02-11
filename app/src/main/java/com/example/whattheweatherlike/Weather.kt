package com.example.whattheweatherlike

data class Weather(var city : String = "NoNe",
                   var temp : String = "0",
                   var humidity : String = "0",
                   var wind_speed : String = "0",
                   var day_duration : String = "0");