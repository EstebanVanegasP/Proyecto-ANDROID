package com.example.proyectoarquitectura_desarrollo.Screen

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectoarquitectura_desarrollo.Model.Extra
import com.example.proyectoarquitectura_desarrollo.Model.Evento
import com.example.proyectoarquitectura_desarrollo.Service.EventoService
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditarEventoScreen(
    eventoId: String,
    navController: NavController,
    eventoService: EventoService = EventoService()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var evento by remember { mutableStateOf<Evento?>(null) } // Cambiado a Evento? en lugar de Map
    var fechaTimestamp by remember { mutableStateOf<Timestamp?>(null) }
    var lugar by remember { mutableStateOf("") }
    var extras by remember { mutableStateOf<List<Extra>>(emptyList()) } // Cambiado a List<Extra>
    var detalleExtra by remember { mutableStateOf("") }
    var cantidadExtra by remember { mutableStateOf("") }
    var valorUnitarioExtra by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Cargar los datos del evento al inicio
    LaunchedEffect(eventoId) {
        coroutineScope.launch {
            val data = eventoService.getEventoById(eventoId)
            if (data != null) {
                evento = data
                fechaTimestamp = data.date
                lugar = data.lugar ?: ""
                extras = data.extras // Usamos `extras` en plural para coincidir con el modelo
            }
        }
    }

    // Formatear la fecha para mostrarla en un formato legible
    val formattedDate = fechaTimestamp?.toDate()?.let { dateFormat.format(it) } ?: "Sin fecha"

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Editar Evento", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

            // Mostrar la fecha del evento
            TextField(
                value = formattedDate,
                onValueChange = {},
                label = { Text("Fecha del evento") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        DatePickerDialog(context, { _, year, month, dayOfMonth ->
                            val calendar = Calendar.getInstance()
                            calendar.set(year, month, dayOfMonth)
                            fechaTimestamp = Timestamp(calendar.time)
                        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show()
                    }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar Fecha")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar el lugar del evento
            TextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar y agregar extras
            Text("Extras", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
            extras.forEach { extra ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Detalle: ${extra.detalle}", modifier = Modifier.weight(1f))
                    Text("Cantidad: ${extra.cantidad}", modifier = Modifier.weight(1f))
                    Text("Valor Unitario: ${extra.valorUnitario}", modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Agregar Extra", style = MaterialTheme.typography.titleSmall)
            TextField(
                value = detalleExtra,
                onValueChange = { detalleExtra = it },
                label = { Text("Detalle") },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = cantidadExtra,
                onValueChange = { cantidadExtra = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = valorUnitarioExtra,
                onValueChange = { valorUnitarioExtra = it },
                label = { Text("Valor Unitario") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(
                onClick = {
                    val extra = Extra(
                        detalle = detalleExtra,
                        cantidad = cantidadExtra.toIntOrNull() ?: 0,
                        valorUnitario = valorUnitarioExtra.toIntOrNull() ?: 0
                    )
                    coroutineScope.launch {
                        isLoading = true
                        val success = eventoService.agregarExtra(eventoId, extra)
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Extra agregado", Toast.LENGTH_SHORT).show()
                            extras = extras + extra
                            detalleExtra = ""
                            cantidadExtra = ""
                            valorUnitarioExtra = ""
                        } else {
                            Toast.makeText(context, "Error al agregar extra", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Añadir Extra")
                }
            }
        }
    }
}
