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

                NavHost(navController = navController, startDestination = "pp") {
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

                    /*
                    composable("paquetes") {
                        PaquetesScreen(navController = navController)
                    }



                    composable(
                        "paquetes_categoria/{categoria}",
                        arguments = listOf(navArgument("categoria") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val categoria = backStackEntry.arguments?.getString("categoria")
                        if (categoria != null) {
                            PaquetesPorCategoriaScreen(navController, categoria)
                        }
                    }
                    */
                    composable(
                        "historial_usuario/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        if (userId != null) {
                            HistorialUsuarioScreen(userId = userId, navController = navController)
                        }
                    }
                }
            }
        }
    }
}
