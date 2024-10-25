package com.example.proyectoarquitectura_desarrollo.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectoarquitectura_desarrollo.Service.PaqueteService
import kotlinx.coroutines.launch

@Composable
fun HistorialUsuarioScreen(
    userId: String,
    navController: NavController,
    paqueteService: PaqueteService = PaqueteService()
) {
    val coroutineScope = rememberCoroutineScope()
    var historial by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            val historialList = paqueteService.getPaquetesByUserId(userId)
            if (historialList != null) {
                historial = historialList
            } else {
                historial = emptyList()
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Historial de Paquetes/Eventos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (historial.isEmpty()) {
            Text("No hay paquetes o eventos registrados para este usuario.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historial) { paquete ->
                    val nombrePaquete = paquete["categoria"] as? String ?: "Nombre no disponible"
                    val descripcion = paquete["descripcion"] as? String ?: "Descripción no disponible"
                    val fecha = paquete["fecha"] as? String ?: "Fecha no disponible"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Categoría: $nombrePaquete", style = MaterialTheme.typography.bodyLarge)
                            Text("Descripción: $descripcion", style = MaterialTheme.typography.bodyMedium)
                            Text("Fecha: $fecha", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
