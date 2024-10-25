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
import com.example.proyectoarquitectura_desarrollo.Service.PaqueteService
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun CategoriaScreen(
    navController: NavController,
    paqueteService: PaqueteService = PaqueteService()
) {
    val coroutineScope = rememberCoroutineScope()
    val categorias = listOf("boda", "quinceanios", "babyshower", "cumpleanios", "despedida", "reunion")

    var paquetesPorCategoria by remember { mutableStateOf<Map<String, List<Map<String, Any>>>>(emptyMap()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    // Obtener paquetes por categoría
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val paquetes = paqueteService.getAllPaquetes()?.documents?.mapNotNull { document ->
                val data = document.data?.toMutableMap()
                if (data != null) {
                    data["id"] = document.id // Añadimos el ID del documento a los datos
                    data
                } else {
                    null
                }
            }
            paquetesPorCategoria = categorias.associateWith { categoria ->
                paquetes?.filter { it["categoria"] == categoria } ?: emptyList()
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Editar Paquetes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        if (categoriaSeleccionada == null) {
            LazyColumn {
                items(categorias) { categoria ->
                    val cantidadPaquetes = paquetesPorCategoria[categoria]?.size ?: 0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { categoriaSeleccionada = categoria },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Categoría: $categoria")
                            Text("Paquetes: $cantidadPaquetes")
                        }
                    }
                }
            }
        } else {
            val paquetes = paquetesPorCategoria[categoriaSeleccionada] ?: emptyList()
            LazyColumn {
                items(paquetes) { paquete ->
                    val paqueteId = paquete["id"] as? String
                    val descripcion = paquete["descripcion"] as? String ?: "Descripción no disponible"

                    if (paqueteId != null && paqueteId.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    Log.d("CategoriaScreen", "Navegando a editar_paquete con ID: $paqueteId")
                                    navController.navigate("editar_paquete/$paqueteId")
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Descripción: $descripcion")
                            }
                        }
                    } else {
                        Log.e("CategoriaScreen", "Paquete sin ID: $paquete")
                    }
                }
            }

            Button(
                onClick = { categoriaSeleccionada = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Volver a Categorías")
            }
        }
    }
}
