package com.example.proyectoarquitectura_desarrollo.Service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class PaqueteService {
    private val db = FirebaseFirestore.getInstance()
    private val paquetesCollection = db.collection("paquetes")

    suspend fun getAllPaquetes(): QuerySnapshot? = try {
        paquetesCollection.get().await()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    suspend fun updatePaquete(paqueteId: String, updates: Map<String, Any>): Boolean {
        return try {
            paquetesCollection.document(paqueteId).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deletePaquete(paqueteId: String): Boolean {
        return try {
            paquetesCollection.document(paqueteId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getPaquetesByUserId(userId: String): List<Map<String, Any>>? {
        return try {
            val result = paquetesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            result.documents.map { document ->
                val data = document.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = document.id // Añadir el ID del documento a los datos
                data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun crearPaquete(paquete: Map<String, Any>): Boolean {
        return try {
            // Primero, añadimos el paquete a la colección y obtenemos la referencia al documento creado
            val documentRef = paquetesCollection.add(paquete).await()

            // Ahora, añadimos el ID generado por Firestore al paquete y lo actualizamos en el documento
            val paqueteConId = paquete.toMutableMap()
            paqueteConId["id"] = documentRef.id
            documentRef.set(paqueteConId).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getPaquetesByCategoria(categoria: String): List<Map<String, Any>>? {
        return try {
            val result = db.collection("paquetes")
                .whereEqualTo("categoria", categoria)
                .get()
                .await()

            result.documents.map { document ->
                val data = document.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = document.id // Añadir el ID del documento a los datos
                data
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPaqueteById(paqueteId: String): Map<String, Any>? {
        return try {
            val document = paquetesCollection.document(paqueteId).get().await()
            if (document.exists()) {
                val data = document.data?.toMutableMap()
                data?.put("id", document.id) // Añadir el ID del documento a los datos
                data
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
