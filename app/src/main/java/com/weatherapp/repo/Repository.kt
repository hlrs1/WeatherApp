package com.weatherapp.repo

import com.google.android.gms.maps.model.LatLng
import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.db.fb.toFBCity
import com.weatherapp.db.local.LocalCity
import com.weatherapp.db.local.LocalDatabase
import com.weatherapp.db.local.toCity
import com.weatherapp.db.local.toLocalCity
import com.weatherapp.model.City
import com.weatherapp.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class Repository(
    private val fbDB: FBDatabase,
    private val localDB: LocalDatabase
) {
    val user: Flow<User> = fbDB.user.map { fbUser ->
        fbUser.toUser() // Converte FBUser para User
    }

    val cities: Flow<List<City>> = localDB.cities.map { list ->
        list.map { it.toCity() } // Converte LocalCity para City
    }

    fun add(city: City) {
        val fbCity = city.toFBCity() // Converte City para FBCity
        fbDB.add(fbCity)

        val localCity = city.toLocalCity() // Converte City para LocalCity
        localDB.insert(localCity)
    }

    fun remove(city: City) {
        val fbCity = city.toFBCity()
        fbDB.remove(fbCity)

        val localCity = city.toLocalCity()
        localDB.delete(localCity)
    }

    fun update(city: City) {
        val fbCity = city.toFBCity()
        fbDB.update(fbCity)

        val localCity = city.toLocalCity()
        localDB.update(localCity)
    }

//    fun LocalCity.toCity() = City(
//        name = this.name,
//        location = LatLng(this.latitude, this.longitude),
//        isMonitored = this.isMonitored
//    )
//    fun City.toLocalCity() = LocalCity(
//        name = this.name,
//        latitude = this.location?.latitude?:0.0,
//        longitude = this.location?.longitude?:0.0,
//        isMonitored = this.isMonitored
//    )

}

