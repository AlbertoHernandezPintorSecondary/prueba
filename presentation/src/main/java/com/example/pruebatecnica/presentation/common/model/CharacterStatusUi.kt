package com.example.pruebatecnica.presentation.common.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.pruebatecnica.presentation.R
import com.example.pruebatecnica.presentation.theme.StatusAlive
import com.example.pruebatecnica.presentation.theme.StatusDead
import com.example.pruebatecnica.presentation.theme.StatusUnknown

/** Character status as the UI shows it: label + indicator color. */
enum class CharacterStatusUi(
    @param:StringRes val labelRes: Int,
    val color: Color,
) {
    ALIVE(R.string.status_alive, StatusAlive),
    DEAD(R.string.status_dead, StatusDead),
    UNKNOWN(R.string.status_unknown, StatusUnknown),
}
