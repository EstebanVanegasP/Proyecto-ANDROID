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
import com.example.proyectoarquitectura_desarrollo.Service.CotizacionService
import com.example.proyectoarquitectura_desarrollo.Service.PaqueteService
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.proyectoarquitectura_desarrollo.Model.ElementoState

data class ElementoState(
    val cantidad: MutableState<Int>,
    val valorUnitario: MutableState<Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCotizacionScreen(
    cotizacionId: String,
    navController: NavController,
    cotizacionService: CotizacionService = CotizacionService(),
    paqueteService: PaqueteService = PaqueteService()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var cotizacion by remember { mutableStateOf<Map<String, Any>?>(null) }
    var fechaTimestamp by remember { mutableStateOf<Timestamp?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var valorTotal by remember { mutableStateOf(0) }
    var invitados by remember { mutableStateOf(0) }
    var valorPorInvitado by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }  // Estado para el indicador de progreso



    val elementos = listOf(
        "Mesero", "Comida", "Cristalería", "Platos", "Mesas",
        "Torta", "DJ", "Maestro de Ceremonia", "Flores", "Globos", "Decoración"
    )
    var elementosState by remember { mutableStateOf<Map<String, ElementoState>>(emptyMap()) }

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    LaunchedEffect(cotizacionId) {
        coroutineScope.launch {
            val data = cotizacionService.getCotizacionById(cotizacionId)
            if (data != null) {
                cotizacion = data
                fechaTimestamp = data["date"] as? Timestamp
                observaciones = data["observaciones"] as? String ?: ""
                lugar = data["lugar"] as? String ?: ""
                nota = data["nota"] as? String ?: ""  // Cargar "nota" desde la BD

                val packageId = data["packageId"] as? String
                if (packageId != null) {
                    val paqueteData = paqueteService.getPaqueteById(packageId)
                    descripcion = paqueteData?.get("descripcion") as? String ?: ""
                    valorTotal = (paqueteData?.get("valorTotal") as? Number)?.toInt() ?: 0
                    invitados = (paqueteData?.get("invitados") as? Number)?.toInt() ?: 0
                    valorPorInvitado = (paqueteData?.get("valorPorInvitado") as? Number)?.toInt() ?: 0

                    val elementosExistentes = paqueteData?.get("elementos") as? Map<String, Map<String, Any>> ?: emptyMap()
                    elementosState = elementos.associateWith { nombreElemento ->
                        val detalles = elementosExistentes[nombreElemento]
                        ElementoState(
                            cantidad = mutableStateOf((detalles?.get("cantidad") as? Number)?.toInt() ?: 0),
                            valorUnitario = mutableStateOf((detalles?.get("valorUnitario") as? Number)?.toInt() ?: 0)
                        )
                    }
                }
            }
        }
    }


    val totalCalculated by remember {
        derivedStateOf {
            elementosState.values.sumOf { it.cantidad.value * it.valorUnitario.value }
        }
    }

    if (cotizacion != null) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("Editar Cotización", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

                // Estado de la Cotización
                Text("Estado: ${cotizacion?.get("status") as? String ?: "Desconocido"}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Fecha del evento
                TextField(
                    value = fechaTimestamp?.toDate()?.let { dateFormat.format(it) } ?: "", // Convertir solo para mostrar
                    onValueChange = {},
                    label = { Text("Fecha del evento") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                fechaTimestamp = Timestamp(calendar.time) // Guardamos como Timestamp
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar Fecha")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Observaciones
                TextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Lugar
                TextField(
                    value = lugar,
                    onValueChange = { lugar = it },
                    label = { Text("Lugar") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Detalles del Paquete
                Text("Detalles del Paquete", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                Divider()

                elementosState.forEach { (nombreElemento, elementoState) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nombreElemento,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = elementoState.cantidad.value.toString(),
                            onValueChange = { value -> elementoState.cantidad.value = value.toIntOrNull() ?: 0 },
                            label = { Text("Cantidad") },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = elementoState.valorUnitario.value.toString(),
                            onValueChange = { value -> elementoState.valorUnitario.value = value.toIntOrNull() ?: 0 },
                            label = { Text("Valor Unitario") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resumen del Valor Total
                Text("Valor Total Calculado: $totalCalculated", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Notas Internas
                TextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Notas Internas (Solo Administrador)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de Acción
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = {
                            if (fechaTimestamp == null || lugar.isEmpty()) {
                                Toast.makeText(context, "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
                            } else {
                                actionType = "aceptar"
                                showDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aceptar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            actionType = "rechazar"
                            showDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        isLoading = true  // Activar el indicador de progreso
                        coroutineScope.launch {
                            val success = fechaTimestamp?.let {
                                cotizacionService.updateCotizacionData(
                                    cotizacionId,
                                    it,
                                    observaciones,
                                    lugar,
                                    elementosState,
                                    nota
                                )
                            } ?: false

                            if (success) {
                                Toast.makeText(context, "Cambios guardados temporalmente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false  // Desactivar el indicador de progreso después de guardar
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading  // Desactivar el botón mientras se guarda
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),  // Indicador de progreso pequeño
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar Cambios")
                    }
                }

                // Vista Previa del Evento (si deseas mantenerla)
                Spacer(modifier = Modifier.height(16.dp))


                // Vista Previa del Evento
                Card(
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vista Previa del Evento", style = MaterialTheme.typography.titleMedium)
                        Text("Fecha del Evento: ${fechaTimestamp?.toDate()?.let { dateFormat.format(it) } ?: ""}")
                        Text("Lugar: $lugar")
                        Text("Valor Total: $totalCalculated")
                    }
                }
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val success = if (actionType == "aceptar") {
                            // Aquí llamamos a updateCotizacionStatus y se creará un evento si se cambia a "activa"
                            cotizacionService.updateCotizacionStatus(cotizacionId, "activa")
                        } else {
                            cotizacionService.updateCotizacionStatus(cotizacionId, "rechazada")
                        }
                        if (success) {
                            Toast.makeText(context, "Cotización $actionType con éxito", Toast.LENGTH_SHORT).show()
                            navController.navigate("pp")
                        } else {
                            Toast.makeText(context, "Error al $actionType la cotización", Toast.LENGTH_SHORT).show()
                        }
                        showDialog = false
                    }
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            text = { Text("¿Estás seguro de que deseas $actionType esta cotización?") }
        )
    }
}

