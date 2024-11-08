package com.example.proyectoarquitectura_desarrollo.Model

import com.google.firebase.Timestamp

data class Evento(
    var id: String = "",
    var descripcion: String = "",  // Nueva propiedad, si la necesitas
    var createdAt: Timestamp? = null,
    var date: Timestamp? = null,
    var lugar: String = "",
    var cotizacionId: String = "",
    var packageId: String = "",
    var userId: String = "",
    var isConfirmed: Boolean = false,
    var extras: List<Extra> = emptyList()
)
data class Extra(
    val detalle: String = "",
    val cantidad: Int = 0,
    val valorUnitario: Int = 0
)

