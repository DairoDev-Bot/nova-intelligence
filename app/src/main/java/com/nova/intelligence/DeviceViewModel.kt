package com.nova.intelligence

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class DeviceViewModel(private val app: Application) : AndroidViewModel(app) {

    /** Datos fijos del equipo, leídos una sola vez fuera del hilo principal. */
    val specs: StateFlow<DeviceSpecs?> = flow { emit(DeviceReader.readSpecs(app)) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Datos en vivo: solo se actualizan mientras la pantalla está visible. */
    val live: StateFlow<LiveStats> = flow {
        while (true) {
            emit(DeviceReader.readLive(app))
            delay(LIVE_REFRESH_MS)
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LiveStats())

    private companion object {
        const val LIVE_REFRESH_MS = 3_000L
    }
}
