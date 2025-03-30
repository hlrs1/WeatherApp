package com.weatherapp.api

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherService {
    private var weatherAPI: WeatherServiceAPI
    init {
        val retrofitAPI = Retrofit.Builder()
            .baseUrl(WeatherServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        weatherAPI = retrofitAPI.create(WeatherServiceAPI::class.java)
    }
    suspend fun getName(lat: Double, lng: Double):String? = withContext(Dispatchers.IO){
        search("$lat,$lng")?.name // retorno
    }
    suspend fun getLocation(name: String): LatLng? = withContext(Dispatchers.IO) {
        (search(name)?.name as LatLng?) // retorno
    }

    private fun search(query: String) : APILocation? {
        val call: Call<List<APILocation>?> = weatherAPI.search(query)
        val apiLoc = call.execute().body()
        return if (!apiLoc.isNullOrEmpty()) apiLoc[0] else null
    }
    suspend fun getCurrentWeather(name: String): APICurrentWeather? =
        withContext(Dispatchers.IO) {
            val call: Call<APICurrentWeather?> = weatherAPI.currentWeather(name)
            call.execute().body() // retorno
        }
    suspend fun getForecast(name: String) : APIWeatherForecast? = withContext(Dispatchers.IO) {
        val call: Call<APIWeatherForecast?> = weatherAPI.forecast(name)
        call.execute().body() // retorno
    }
    suspend fun getBitmap(imgUrl: String) : Bitmap? = withContext(Dispatchers.IO) {
        Picasso.get().load(imgUrl).get() // retorno
    }
}