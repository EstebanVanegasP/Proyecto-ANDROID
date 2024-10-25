package com.example.proyectoarquitectura_desarrollo.Screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectoarquitectura_desarrollo.Service.UsuarioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun UsuarioScreen(
    navController: NavController,
    usuarioService: UsuarioService = UsuarioService()
) {
    val coroutineScope = rememberCoroutineScope()
    var administradores by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var clientes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Obtener la lista de usuarios desde Firestore
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val usersSnapshot = usuarioService.getAllUsers()
            if (usersSnapshot != null) {
                val allUsers = usersSnapshot.documents.mapNotNull { document ->
                    document.data?.plus("id" to document.id)
                }

                // Filtrar administradores y clientes
                administradores = allUsers.filter { it["role"] == "admin" }
                clientes = allUsers.filter { it["role"] == "cliente" }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Sección de administradores
            if (administradores.isNotEmpty()) {
                item {
                    Text(
                        text = "ADMINISTRADORES",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(administradores) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        navController = navController,
                        coroutineScope = coroutineScope,
                        usuarioService = usuarioService,
                        onUsuarioEliminado = {
                            administradores = administradores.filterNot { it["id"] == usuario["id"] }
                        }
                    )
                }
            }

            // Sección de clientes
            if (clientes.isNotEmpty()) {
                item {
                    Text(
                        text = "CLIENTES",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(clientes) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        navController = navController,
                        coroutineScope = coroutineScope,
                        usuarioService = usuarioService,
                        onUsuarioEliminado = {
                            clientes = clientes.filterNot { it["id"] == usuario["id"] }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UsuarioCard(
    usuario: Map<String, Any>,
    navController: NavController,
    coroutineScope: CoroutineScope,
    usuarioService: UsuarioService,
    onUsuarioEliminado: () -> Unit
) {
    val email = usuario["email"] as? String ?: "No Email"
    val nombre = usuario["nombre"] as? String ?: "No Name"
    val userId = usuario["id"] as? String ?: ""
    val apellidos = usuario["apellidos"] as? String ?: ""
    val cel = usuario["cel"] as? String ?: ""
    val role = usuario["role"] as? String ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nombre(s): $nombre", style = MaterialTheme.typography.bodyLarge)
            Text("Apellidos: $apellidos", style = MaterialTheme.typography.bodyMedium)
            Text("Email: $email", style = MaterialTheme.typography.bodyMedium)
            Text("Celular: $cel", style = MaterialTheme.typography.bodyMedium)
            Text("Role: $role", style = MaterialTheme.typography.bodyMedium)
            Text("ID: $userId", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    navController.navigate("historial_usuario/$userId")
                }) {
                    Text("Ver Historial")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    coroutineScope.launch {
                        val success = usuarioService.deleteUser(userId)
                        if (success) {
                            onUsuarioEliminado()
                        }
                    }
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
