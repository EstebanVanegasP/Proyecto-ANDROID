package com.example.proyectoarquitectura_desarrollo.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectoarquitectura_desarrollo.Service.EventoService
import com.example.proyectoarquitectura_desarrollo.Model.Evento
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventoScreen(navController: NavController, eventoService: EventoService = EventoService()) {
    val coroutineScope = rememberCoroutineScope()
    var eventos by remember { mutableStateOf<List<Evento>>(emptyList()) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            eventos = eventoService.getEventosActivos()
        }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Eventos Activos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(eventos) { evento ->
            val formattedDate = evento.date?.toDate()?.let { dateFormat.format(it) } ?: "Sin fecha"
            EventoItem(evento = evento, navController = navController, formattedDate = formattedDate)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun EventoItem(evento: Evento, navController: NavController, formattedDate: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                navController.navigate("editar_evento/${evento.id}")
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Evento: ${evento.descripcion}")
            Text("Fecha del Evento: $formattedDate")
            Text("Lugar: ${evento.lugar}")
        }
    }
}
