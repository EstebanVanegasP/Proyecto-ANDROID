package com.example.proyectoarquitectura_desarrollo.Service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.tasks.await

class UsuarioService {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getAllUsers() = try {
        usersCollection.get().await()
    } catch (e: Exception) {
        null
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            usersCollection.document(userId).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            usersCollection.document(userId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
