package com.example.whattheweatherlike

data class Weather(var city : String = "NoNe",
                   var temp : String = "0",
                   var humidity : String = "0",
                   var iconId : Int = 0,)