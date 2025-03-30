package com.weatherapp.api

import com.weatherapp.BuildConfig
import com.weatherapp.api.WeatherServiceAPI.Companion.API_KEY
import com.weatherapp.model.Forecast
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

fun APIWeatherForecast.toForecast(): List<Forecast>? {
    return forecast?.forecastday?.map {
        Forecast(
            date = it.date?:"00-00-0000",
            weather = it.day?.condition?.text?:"Erro carregando!",
            tempMin = it.day?.mintemp_c?:-1.0,
            tempMax = it.day?.maxtemp_c?:-1.0,
            imgUrl = ("https:" + it.day?.condition?.icon)
        )
    }
}