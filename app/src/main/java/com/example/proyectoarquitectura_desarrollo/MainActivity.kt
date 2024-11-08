package com.example.proyectoarquitectura_desarrollo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.proyectoarquitectura_desarrollo.ui.theme.ProyectoArquitectura_DesarrolloTheme
import com.example.proyectoarquitectura_desarrollo.Screen.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            ProyectoArquitectura_DesarrolloTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "pp") { //RECUERDA CAMBIAR ESTOOOO :P por login
                    composable("login") {
                        LoginScreen(
                            auth = auth,
                            onLoginSuccess = { navController.navigate("pp") },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegistroScreen(
                            auth = auth,
                            onNavigateToLogin = { navController.navigate("login") }
                        )
                    }
                    composable("pp") {
                        PpScreen(navController = navController)
                    }
                    composable("usuarios") {
                        UsuarioScreen(navController = navController)
                    }
                    composable("crear_paquete") {
                        CrearPaqueteScreen(navController = navController)
                    }
                    composable("categorias") {
                        CategoriaScreen(navController = navController)
                    }
                    composable(
                        "editar_paquete/{paqueteId}",
                        arguments = listOf(navArgument("paqueteId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val paqueteId = backStackEntry.arguments?.getString("paqueteId")
                        if (paqueteId != null) {
                            EditarPaqueteScreen(
                                paqueteId = paqueteId,
                                navController = navController
                            )
                        }
                    }

                    composable(
                        "historial_usuario/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        if (userId != null) {
                            HistorialUsuarioScreen(userId = userId, navController = navController)
                        }
                    }

                    // Pantalla de Cotizaciones
                    composable("cotizaciones") {
                        CotizacionScreen(navController = navController)
                    }

                    // Nueva pantalla de Editar Cotización
                    composable(
                        "editar_cotizacion/{cotizacionId}",
                        arguments = listOf(navArgument("cotizacionId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val cotizacionId = backStackEntry.arguments?.getString("cotizacionId")
                        if (cotizacionId != null) {
                            EditarCotizacionScreen(
                                cotizacionId = cotizacionId,
                                navController = navController
                            )
                        }
                    }

                    composable("eventos") {
                        EventoScreen(navController = navController)
                    }

                    composable(
                        "editar_evento/{eventoId}",
                        arguments = listOf(navArgument("eventoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val eventoId = backStackEntry.arguments?.getString("eventoId")
                        if (eventoId != null) {
                            EditarEventoScreen(eventoId = eventoId, navController = navController)
                        }
                    }


                }
            }
        }
    }
}
