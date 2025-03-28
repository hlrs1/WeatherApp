package com.weatherapp.repo

import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.db.local.LocalDatabase
import com.weatherapp.model.City
import com.weatherapp.model.User

class Repository (
    private val fbDB: FBDatabase,
    private val localDB : LocalDatabase
) : FBDatabase.Listener {
    interface Listener {
        fun onUserLoaded(user: User)
        fun onUserSignOut()
        fun onCityAdded(city: City)
        fun onCityUpdated(city: City)
        fun onCityRemoved(city: City)
    }
    private var listener : Listener? = null
    fun setListener(listener: Listener? = null) {
        this.listener = listener
    }
    init {
        fbDB.setListener(this)
    }
    fun add(city: City) = fbDB.add(city)
    fun remove(city: City) = fbDB.remove(city)
    fun update(city: City) = fbDB.update(city)
    override fun onUserLoaded(user: User) = listener?.onUserLoaded(user)?:Unit
    override fun onUserSignOut() = listener?.onUserSignOut()?:Unit
    override fun onCityAdded(city: City) {
        localDB.insert(city)
        listener?.onCityAdded(city)
    }
    override fun onCityUpdated(city: City) {
        localDB.update(city)
        listener?.onCityUpdated(city)
    }
    override fun onCityRemoved(city: City) {
        localDB.delete(city)
        listener?.onCityRemoved(city)
    }
}
