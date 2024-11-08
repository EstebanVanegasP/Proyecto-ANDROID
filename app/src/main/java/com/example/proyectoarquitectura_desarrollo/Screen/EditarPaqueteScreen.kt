package com.example.proyectoarquitectura_desarrollo.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectoarquitectura_desarrollo.Service.PaqueteService
import kotlinx.coroutines.launch
import com.example.proyectoarquitectura_desarrollo.Model.ElementoState


@Composable
fun EditarPaqueteScreen(
    paqueteId: String,
    navController: NavController,
    paqueteService: PaqueteService = PaqueteService()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var paquete by remember { mutableStateOf<Map<String, Any>?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var valorTotal by remember { mutableStateOf(0) }
    var invitados by remember { mutableStateOf(0) }
    var valorPorInvitado by remember { mutableStateOf(0) }

    var elementosState by remember { mutableStateOf<Map<String, ElementoState>>(emptyMap()) }

    LaunchedEffect(paqueteId) {
        coroutineScope.launch {
            val data = paqueteService.getPaqueteById(paqueteId)
            if (data != null) {
                paquete = data
                descripcion = data["descripcion"] as? String ?: ""
                valorTotal = (data["valorTotal"] as? Number)?.toInt() ?: 0
                invitados = (data["invitados"] as? Number)?.toInt() ?: 0
                valorPorInvitado = (data["valorPorInvitado"] as? Number)?.toInt() ?: 0

                val elementos = data["elementos"] as? Map<String, Map<String, Any>> ?: emptyMap()
                elementosState = elementos.mapValues { (_, detalles) ->
                    val cantidad = (detalles["cantidad"] as? Number)?.toInt() ?: 0
                    val valorUnitario = (detalles["valorUnitario"] as? Number)?.toInt() ?: 0
                    ElementoState(
                        cantidad = mutableStateOf(cantidad),
                        valorUnitario = mutableStateOf(valorUnitario)
                    )
                }
            }
        }
    }

    if (paquete != null) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text(
                    text = "Editar Paquete",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Categoría: ${paquete!!["categoria"] as? String ?: "N/A"}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = if (invitados == 0) "" else invitados.toString(),
                            onValueChange = { invitados = it.toIntOrNull() ?: 0 },
                            label = { Text("Invitados") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Elementos Adicionales",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                            onValueChange = { value ->
                                elementoState.cantidad.value = value.toIntOrNull() ?: 0
                            },
                            label = { Text("Cantidad") },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = elementoState.valorUnitario.value.toString(),
                            onValueChange = { value ->
                                elementoState.valorUnitario.value = value.toIntOrNull() ?: 0
                            },
                            label = { Text("Valor Unitario") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Actualizar el valorTotal y valorPorInvitado si es necesario
                            valorTotal = elementosState.values.sumOf {
                                it.cantidad.value * it.valorUnitario.value
                            }
                            valorPorInvitado = if (invitados > 0) valorTotal / invitados else 0

                            val updatedData = mapOf(
                                "descripcion" to descripcion,
                                "valorTotal" to valorTotal,
                                "invitados" to invitados,
                                "valorPorInvitado" to valorPorInvitado,
                                "elementos" to elementosState.mapValues { (_, estado) ->
                                    mapOf(
                                        "cantidad" to estado.cantidad.value,
                                        "valorUnitario" to estado.valorUnitario.value
                                    )
                                }
                            )
                            val success = paqueteService.updatePaquete(paqueteId, updatedData)
                            if (success) {
                                Toast.makeText(context, "Paquete actualizado", Toast.LENGTH_SHORT).show()
                                navController.navigate("pp")
                            } else {
                                Toast.makeText(context, "Error al actualizar el paquete", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val success = paqueteService.deletePaquete(paqueteId)
                            if (success) {
                                Toast.makeText(context, "Paquete eliminado", Toast.LENGTH_SHORT).show()
                                navController.navigate("pp")
                            } else {
                                Toast.makeText(context, "Error al eliminar el paquete", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar Paquete")
                }
            }
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}

