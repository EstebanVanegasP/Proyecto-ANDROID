package com.example.proyectoarquitectura_desarrollo.Service

import com.example.proyectoarquitectura_desarrollo.Model.ElementoState
import com.example.proyectoarquitectura_desarrollo.Service.EventoService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log


class CotizacionService {
    private val db = FirebaseFirestore.getInstance()
    private val cotizacionesCollection = db.collection("cotizaciones")
    private val eventoService = EventoService()  // Crear una instancia de EventoService


    suspend fun getCotizacionById(cotizacionId: String): Map<String, Any>? {
        return try {
            val document = cotizacionesCollection.document(cotizacionId).get().await()
            if (document.exists()) {
                document.data
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Método para actualizar la información de la cotización
    suspend fun updateCotizacionData(
        cotizacionId: String,
        fecha: Timestamp,
        observaciones: String,
        lugar: String,
        elementosState: Map<String, ElementoState>,
        nota: String
    ): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "date" to fecha,
                "observaciones" to observaciones,
                "lugar" to lugar,
                "nota" to nota
            )

            // Convertir elementosState a un formato que Firestore pueda almacenar
            val elementosMap = elementosState.mapValues { (_, elementoState) ->
                mapOf(
                    "cantidad" to elementoState.cantidad.value,
                    "valorUnitario" to elementoState.valorUnitario.value
                )
            }
            updates["elementos"] = elementosMap

            // Actualizar la cotización en Firestore
            cotizacionesCollection.document(cotizacionId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e("CotizacionService", "Error al actualizar la cotización $cotizacionId: ${e.message}", e)
            false
        }
    }


    // Método para actualizar el estado de la cotización
    suspend fun updateCotizacionStatus(cotizacionId: String, status: String): Boolean {
        return try {
            // Actualizar el status de la cotización en Firestore
            cotizacionesCollection.document(cotizacionId).update("status", status).await()

            // Si el status es "activa", crea un evento basado en esta cotización
            if (status == "activa") {
                val cotizacionData = getCotizacionById(cotizacionId)
                if (cotizacionData != null) {
                    val date = cotizacionData["date"] as? Timestamp
                    val lugar = cotizacionData["lugar"] as? String ?: ""
                    val packageId = cotizacionData["packageId"] as? String ?: ""
                    val userId = cotizacionData["userId"] as? String ?: ""

                    // Llamar a crearEventoDesdeCotizacion en EventoService
                    eventoService.crearEventoDesdeCotizacion(
                        cotizacionId = cotizacionId,
                        date = date ?: Timestamp.now(),
                        lugar = lugar,
                        paqueteId = packageId,
                        userId = userId
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
