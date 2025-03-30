package com.weatherapp.db.fb

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.weatherapp.model.City
import com.weatherapp.model.User
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

class FBDatabase {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    val user: Flow<FBUser>
        get() {
            if (auth.currentUser == null) return emptyFlow()
            return db.collection("users")
                .document(auth.currentUser!!.uid)
                .snapshots()
                .map { snapshot -> snapshot.toObject(FBUser::class.java) !! }
        }

    val cities: Flow<List<FBCity>>
        get() {
            if (auth.currentUser == null) return emptyFlow()
            return db.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("cities")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FBCity::class.java) }
        }

    fun register(user: User) {
        if (auth.currentUser == null)
            throw RuntimeException("User not logged in!")
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid + "").set(user.toFBUser());
    }

    fun add(city: FBCity) {
        if (auth.currentUser == null) return
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("cities")
            .document(city.name.toString())
            .set(city)
            .addOnSuccessListener { /* Sucesso */ }
            .addOnFailureListener { /* Tratar erros */ }
    }

    fun remove(city: FBCity) {
        if (auth.currentUser == null) return
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("cities")
            .document(city.name.toString())
            .delete()
            .addOnSuccessListener { /* Sucesso */ }
            .addOnFailureListener { /* Tratar erros */ }
    }

    fun update(city: FBCity) {
        if (auth.currentUser == null) return
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("cities")
            .document(city.name.toString())
            .set(city)
            .addOnSuccessListener { /* Sucesso */ }
            .addOnFailureListener { /* Tratar erros */ }
    }
}