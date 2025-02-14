package com.weatherapp.api

import com.weatherapp.BuildConfig
import com.weatherapp.api.WeatherServiceAPI.Companion.API_KEY
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class APIWeatherForecast (
    var location: APILocation? = null,
    var current: APIWeatherForecast? = null,
    var forecast: APIForecast? = null
)

interface APIWeatherService {
    companion object {
        const val API_KEY = BuildConfig.WEATHER_API_KEY
    }
    @GET("forecast.json?key=$API_KEY&days=10&lang=pt")
    fun forecast(@Query("q") name: String): Call<APIWeatherForecast?>
}