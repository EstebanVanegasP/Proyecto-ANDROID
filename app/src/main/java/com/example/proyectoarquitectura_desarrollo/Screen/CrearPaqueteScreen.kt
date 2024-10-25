package com.example.proyectoarquitectura_desarrollo.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class Elemento(
    val incluir: MutableState<Boolean>,
    val cantidad: MutableState<Int>,
    val valorUnitario: MutableState<Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPaqueteScreen(
    navController: NavController,
    paqueteService: PaqueteService = PaqueteService()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Obtenemos el contexto aquí, dentro de un @Composable

    // Campos básicos
    var categoria by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var valorTotal by remember { mutableStateOf(0) }
    var invitados by remember { mutableStateOf(0) }
    var valorPorInvitado by remember { mutableStateOf(0) }

    // Lista de categorías para el Dropdown
    val categorias = listOf(
        "boda", "quinceanios", "babyshower", "cumpleanios", "despedida", "reunion"
    )

    // Estado para controlar el menú desplegable
    var expanded by remember { mutableStateOf(false) }
    val iconoDropdown = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    // Campos para los elementos adicionales
    val elementos = listOf(
        "Mesero", "Comida", "Cristalería", "Platos", "Mesas",
        "Torta", "DJ", "Maestro de Ceremonia", "Flores", "Globos", "Decoración"
    )

    // Estado para cada elemento
    val estadosElementos = elementos.associateWith {
        Elemento(
            incluir = remember { mutableStateOf(false) },
            cantidad = remember { mutableStateOf(0) },
            valorUnitario = remember { mutableStateOf(0) }
        )
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            // Título de la pantalla
            Text(
                text = "Crear Paquete",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Sección de campos básicos
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Menú desplegable para Categoría
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = categoria,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = {
                                Icon(iconoDropdown, contentDescription = null)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categorias.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        categoria = opcion
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

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
        }

        items(elementos) { elemento ->
            val estado = estadosElementos[elemento]!!

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = estado.incluir.value,
                    onCheckedChange = { estado.incluir.value = it }
                )
                Text(
                    text = elemento,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = if (estado.cantidad.value == 0) "" else estado.cantidad.value.toString(),
                    onValueChange = { estado.cantidad.value = it.toIntOrNull() ?: 0 },
                    enabled = estado.incluir.value,
                    label = { Text("Cantidad") },
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = if (estado.valorUnitario.value == 0) "" else estado.valorUnitario.value.toString(),
                    onValueChange = { estado.valorUnitario.value = it.toIntOrNull() ?: 0 },
                    enabled = estado.incluir.value,
                    label = { Text("Valor Unitario") },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        valorTotal = estadosElementos.values
                            .filter { it.incluir.value }
                            .sumOf { it.cantidad.value * it.valorUnitario.value }
                        valorPorInvitado = if (invitados > 0) valorTotal / invitados else 0
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Actualizar Valores")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        estadosElementos.forEach { (_, estado) ->
                            estado.incluir.value = false
                            estado.cantidad.value = 0
                            estado.valorUnitario.value = 0
                        }
                        valorTotal = 0
                        valorPorInvitado = 0
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restablecer Valores")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para guardar el paquete
            Button(onClick = {
                coroutineScope.launch {
                    val paquete = hashMapOf(
                        "categoria" to categoria,
                        "descripcion" to descripcion,
                        "valorTotal" to valorTotal,
                        "invitados" to invitados,
                        "valorPorInvitado" to valorPorInvitado,
                        "elementos" to estadosElementos.entries
                            .filter { it.value.incluir.value }
                            .associate { it.key to mapOf(
                                "cantidad" to it.value.cantidad.value,
                                "valorUnitario" to it.value.valorUnitario.value
                            )}
                    )
                    val success = paqueteService.crearPaquete(paquete)
                    if (success) {
                        Toast.makeText(context, "Paquete guardado con éxito", Toast.LENGTH_SHORT).show()
                        navController.navigate("pp")
                    } else {
                        Toast.makeText(context, "Error al guardar el paquete", Toast.LENGTH_SHORT).show()
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar Paquete")
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar el Valor Total y Valor por Invitado en TextFields no editables
            TextField(
                value = valorTotal.toString(),
                onValueChange = {},
                label = { Text("Valor Total") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = valorPorInvitado.toString(),
                onValueChange = {},
                label = { Text("Valor por Invitado") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
