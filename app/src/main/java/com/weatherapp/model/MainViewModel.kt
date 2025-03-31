package com.weatherapp.model

import Repository
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.weatherapp.api.WeatherService
import com.weatherapp.api.toWeather
import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.model.City
import com.weatherapp.model.Forecast
import com.weatherapp.model.User
import com.weatherapp.model.Weather
import com.weatherapp.monitor.ForecastMonitor
import com.weatherapp.ui.nav.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel (
    private val repository: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor): ViewModel() {

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
        viewModelScope.launch (Dispatchers.Main) {
            repository.user.collect { user ->
                _user.value = user.copy()
            }
        }
        viewModelScope.launch (Dispatchers.Main) {
            repository.cities.collect { list ->
                val names = list.map { it.name }
                val newCities = list.filter { it.name !in _cities.keys }
                val oldCities = list.filter { it.name in _cities.keys }
                _cities.keys.removeIf { it !in names } // remove cidades deletadas
                newCities.forEach { _cities[it.name] = it } // adiciona cidades novas
                oldCities.forEach { refresh(it) }
            }
        }
    }

//    fun remove(city: City) {
//        db.remove(city)
//        _cities.remove(city.name)
//    }

    fun add(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val location = service.getLocation(name)?:return@launch
        repository.add(City( name = name, location = location))
    }

    fun add(location: LatLng) = viewModelScope.launch(Dispatchers.IO) {
        val resultLocation = service.getLocation(location.toString()) ?: return@launch
        repository.add(City(name = resultLocation.toString(), location = resultLocation))

    }


    fun update(city: City) {
        repository.update(city)
        refresh(city)
        monitor.updateCity(city)
    }

    fun loadWeather(city: City) = viewModelScope.launch(Dispatchers.IO) {
        city.weather = service.getCurrentWeather(city.name)?.toWeather()
        refresh(city)
    }

    fun loadForecast(city: City) = viewModelScope.launch(Dispatchers.IO) {
        city.forecast = service.getForecast(city.name)?.forecast as List<Forecast>?
        refresh(city)
    }

    fun loadBitmap(city: City) = viewModelScope.launch(Dispatchers.IO) {
        service.getBitmap(city.weather!!.imgUrl)
        refresh(city)
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
        monitor.updateCity(copy)
    }

    private var _page = mutableStateOf<Route>(Route.Home)
    var page: Route
        get() = _page.value
        set(tmp) { _page.value = tmp }

}

class MainViewModelFactory(
    private val db : Repository,
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