package com.weatherapp.model

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.weatherapp.api.WeatherService
import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.model.City
import com.weatherapp.model.Forecast
import com.weatherapp.model.User
import com.weatherapp.model.Weather
import com.weatherapp.monitor.ForecastMonitor
import com.weatherapp.ui.nav.Route
import kotlin.random.Random

class MainViewModel(
    private val db: FBDatabase,
    private val service: WeatherService,
    private val monitor: ForecastMonitor
) : ViewModel(), FBDatabase.Listener {

    private val _cities = mutableStateMapOf<String, City>()
    val cities: List<City>
        get() = _cities.values.toList()

    private var _city = mutableStateOf<City?>(null)
    var city: City?
        get() = _city.value
        set(tmp) { _city.value = tmp?.copy(salt = Random.nextLong()) }

    private val _user = mutableStateOf<User?>(null)
    val user: User?
        get() = _user.value

    init {
        db.setListener(this)
    }

    fun remove(city: City) {
        db.remove(city)
        _cities.remove(city.name)
    }

    fun add(name: String) {
        service.getLocation(name) { lat, lng ->
            if (lat != null && lng != null) {
                val newCity = City(name = name, location = LatLng(lat, lng))
                if (!_cities.containsKey(newCity.name)) {
                    db.add(newCity)
                    _cities[newCity.name] = newCity
                }
            }
        }
    }

    fun add(location: LatLng) {
        service.getName(location.latitude, location.longitude) { name ->
            if (name != null) {
                val newCity = City(name = name, location = location)
                if (!_cities.containsKey(newCity.name)) {
                    db.add(newCity)
                    _cities[newCity.name] = newCity
                }
            }
        }
    }

    fun update(city: City) {
        db.update(city)
        refresh(city)
        monitor.updateCity(city)
    }

    fun loadWeather(city: City) {
        service.getCurrentWeather(city.name) { apiWeather ->
            val updatedCity = city.copy(
                weather = Weather(
                    date = apiWeather?.current?.last_updated ?: "...",
                    desc = apiWeather?.current?.condition?.text ?: "...",
                    temp = apiWeather?.current?.temp_c ?: -1.0,
                    imgUrl = "https:" + (apiWeather?.current?.condition?.icon ?: "")
                )
            )

            refresh(updatedCity)
            monitor.updateCity(updatedCity)
        }
    }

    fun loadForecast(city : City) {
        service.getForecast(city.name) { result ->
            city.forecast = result?.forecast?.forecastday?.map {
                Forecast(
                    date = it.date?:"00-00-0000",
                    weather = it.day?.condition?.text?:"Erro carregando!",
                    tempMin = it.day?.mintemp_c?:-1.0,
                    tempMax = it.day?.maxtemp_c?:-1.0,
                    imgUrl = ("https:" + it.day?.condition?.icon)
                )
            }

            refresh(city)
            monitor.updateCity(city)
        }
    }

    fun loadBitmap(city: City) {
        service.getBitmap(city.weather!!.imgUrl) { bitmap ->
            city.weather!!.bitmap = bitmap
            onCityUpdate(city)
        }
    }

    override fun onUserLoaded(user: User) {
        _user.value = user
    }

    override fun onCityAdded(city: City) {
        _cities[city.name] = city
    }

    override fun onCityUpdate(city: City) {
        refresh(city)
        monitor.updateCity(city)
    }

    override fun onCityRemoved(city: City) {
        _cities.remove(city.name)

        if (_city.value?.name == city.name) {
            _city.value = null
        }

        monitor.cancelCity(city)
    }

    private fun refresh(city: City) {
        val copy = city.copy(
            salt = Random.nextLong(),
            weather = city.weather?:_cities[city.name]?.weather,
            forecast = city.forecast?:_cities[city.name]?.forecast
        )
        if (_city.value?.name == city.name) _city.value = copy
        _cities.remove(city.name)
        _cities[city.name] = copy
    }

    private var _page = mutableStateOf<Route>(Route.Home)
    var page: Route
        get() = _page.value
        set(tmp) { _page.value = tmp }

    override fun onUserSignOut() {
        monitor.cancelAll()
        _user.value = null
        _cities.clear()
    }
}

class MainViewModelFactory(
    private val db : FBDatabase,
    private val service : WeatherService,
    private val monitor: ForecastMonitor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(db, service, monitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}