package com.weatherapp.db.local

import android.content.Context
import androidx.room.Room
import com.weatherapp.model.City
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LocalDatabase (context : Context, databaseName : String) {
    private var roomDB : LocalRoomDatabase = Room.databaseBuilder(
        context = context,
        klass = LocalRoomDatabase::class.java,
        name = databaseName
    ).build()
    private var scope : CoroutineScope = CoroutineScope(Dispatchers.IO)
    fun insert(city: City) = scope.launch {
        roomDB.localCityDao().upsert(city.toLocalCity())
    }
    fun update(city: City) = scope.launch {
        roomDB.localCityDao().upsert(city.toLocalCity())
    }
    fun delete(city: City) = scope.launch {
        roomDB.localCityDao().delete(city.toLocalCity())
    }
    fun getCities() = roomDB.localCityDao().getCities().map {
        list -> list.map { it.toCity() }
    }
}