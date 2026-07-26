package com.jdcr.navigation.common.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdcr.navigation.route.BaseAppRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException


interface SplashUiInitializer {
    suspend fun initialize(): Result<BaseAppRoute>
}

sealed interface SplashUiState {
    object Initializing : SplashUiState
    data class Completed(val destination: BaseAppRoute) : SplashUiState
    data class Failed(val throwable: Throwable) : SplashUiState
}

internal class SplashCoordinator(
    private val stateInitializer: SplashUiInitializer,
) : ViewModel() {

    private val mutableState =
        MutableStateFlow<SplashUiState>(SplashUiState.Initializing)

    val state: StateFlow<SplashUiState> = mutableState.asStateFlow()

    private var initializeJob: Job? = null

    init {
        initialize()
    }

    fun retry() {
        initialize()
    }

    private fun initialize() {
        if (initializeJob?.isActive == true) return

        initializeJob = viewModelScope.launch {
            mutableState.value = SplashUiState.Initializing

            val result = try {
                stateInitializer.initialize()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            }

            result.fold(
                onSuccess = {
                    mutableState.value = SplashUiState.Completed(it)
                },
                onFailure = {
                    mutableState.value = SplashUiState.Failed(it)
                },
            )
        }
    }

}