package com.weatherapp.model

import com.google.android.gms.maps.model.LatLng

data class City (
    val name : String,
    val isMonitored: Boolean = false,
    var weather: Weather? = null,
    val location: LatLng? = null,
    val salt: Long? = null,
    var forecast: List<Forecast>? = null
)