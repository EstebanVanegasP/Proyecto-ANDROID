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
import com.example.proyectoarquitectura_desarrollo.Service.UsuarioService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.proyectoarquitectura_desarrollo.Model.ElementoState


@Composable
fun CotizacionScreen(
    navController: NavController,
    paqueteService: PaqueteService = PaqueteService(),
    usuarioService: UsuarioService = UsuarioService(),
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val coroutineScope = rememberCoroutineScope()
    var cotizaciones by remember { mutableStateOf<List<Cotizacion>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val cotizacionList = db.collection("cotizaciones").get().await().documents.mapNotNull { document ->
                document.toObject(Cotizacion::class.java)?.apply {
                    id = document.id
                }
            }

            cotizaciones = cotizacionList.map { cotizacion ->
                val paquete = paqueteService.getPaqueteById(cotizacion.packageId)
                val usuario = usuarioService.getUserById(cotizacion.userId)
                cotizacion.copy(
                    paqueteDescripcion = paquete?.get("descripcion") as? String ?: "Descripción no disponible",
                    usuarioNombre = usuario?.get("nombre") as? String ?: "Usuario no disponible"
                )
            }
        }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Gestión de Cotizaciones", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(cotizaciones) { cotizacion ->
            // Asegúrate de pasar `navController` aquí
            CotizacionItem(cotizacion = cotizacion, navController = navController)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
fun CotizacionItem(cotizacion: Cotizacion, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                navController.navigate("editar_cotizacion/${cotizacion.id}")
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Descripción: ${cotizacion.paqueteDescripcion} - ${cotizacion.usuarioNombre}")
            Text("Estado: ${cotizacion.status}")
        }
    }
}

// Modelo de datos para la cotización
data class Cotizacion(
    var id: String = "",
    var packageId: String = "",
    var userId: String = "",
    var paqueteDescripcion: String = "",
    var usuarioNombre: String = "",
    var status: String = ""
)
