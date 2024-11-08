package com.example.proyectoarquitectura_desarrollo.Service

import android.util.Log
import com.example.proyectoarquitectura_desarrollo.Model.Extra
import com.example.proyectoarquitectura_desarrollo.Model.Evento
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventoService {
    private val db = FirebaseFirestore.getInstance()
    private val eventosCollection = db.collection("eventos")

    // Método para obtener un evento por su ID
    suspend fun getEventoById(eventoId: String): Evento? {
        return try {
            val document = eventosCollection.document(eventoId).get().await()
            if (document.exists()) {
                document.toObject(Evento::class.java)?.apply {
                    id = document.id
                }
            } else null
        } catch (e: Exception) {
            Log.e("EventoService", "Error al obtener el evento $eventoId: ${e.message}", e)
            null
        }
    }

    // Método para crear un evento a partir de una cotización
    suspend fun crearEventoDesdeCotizacion(
        cotizacionId: String,
        date: Timestamp,
        lugar: String,
        paqueteId: String,
        userId: String
    ): Boolean {
        return try {
            // Verificar si ya existe un evento para esta cotización
            val existingEvent = eventosCollection
                .whereEqualTo("cotizacionId", cotizacionId)
                .get()
                .await()

            if (existingEvent.isEmpty) {
                val eventoData = mapOf(
                    "createdAt" to Timestamp.now(),
                    "date" to date,
                    "lugar" to lugar,
                    "cotizacionId" to cotizacionId,
                    "packageId" to paqueteId,
                    "userId" to userId,
                    "isConfirmed" to true,
                    "extra" to emptyList<Map<String, Any>>() // Inicialmente vacío
                )
                eventosCollection.add(eventoData).await()
                true
            } else {
                Log.e("EventoService", "Ya existe un evento para la cotización $cotizacionId")
                false // No se crea un evento duplicado
            }
        } catch (e: Exception) {
            Log.e("EventoService", "Error al crear el evento desde la cotización $cotizacionId: ${e.message}", e)
            false
        }
    }


    // Método para agregar un extra a un evento existente
    suspend fun agregarExtra(eventoId: String, extra: Extra): Boolean {
        return try {
            val extraMap = mapOf(
                "detalle" to extra.detalle,
                "cantidad" to extra.cantidad,
                "valorUnitario" to extra.valorUnitario
            )
            eventosCollection.document(eventoId)
                .update("extra", FieldValue.arrayUnion(extraMap))
                .await()
            true
        } catch (e: Exception) {
            Log.e("EventoService", "Error al agregar extra al evento $eventoId: ${e.message}", e)
            false
        }
    }

    // Método para obtener todos los eventos activos
    suspend fun getEventosActivos(): List<Evento> {
        return try {
            eventosCollection
                .whereEqualTo("isConfirmed", true)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(Evento::class.java)?.apply { id = document.id }
                }
        } catch (e: Exception) {
            Log.e("EventoService", "Error al obtener eventos activos: ${e.message}", e)
            emptyList()
        }
    }
}
