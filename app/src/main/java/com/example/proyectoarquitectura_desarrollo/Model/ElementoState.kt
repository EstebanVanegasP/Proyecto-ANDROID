package com.example.proyectoarquitectura_desarrollo.Model

import androidx.compose.runtime.MutableState

data class ElementoState(
    val cantidad: MutableState<Int>,
    val valorUnitario: MutableState<Int>
)
