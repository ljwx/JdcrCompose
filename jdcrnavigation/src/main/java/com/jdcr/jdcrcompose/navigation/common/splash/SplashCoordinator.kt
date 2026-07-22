package com.jdcr.jdcrcompose.navigation.common.splash

import com.jdcr.jdcrcompose.navigation.route.BaseAppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


interface AppStateInitializer {
    suspend fun initialize(): Result<Unit>
}

sealed interface SplashUiState {
    object Initializing : SplashUiState
    data class Completed(val destination: BaseAppRoute) : SplashUiState
    data class Failed(val message: String) : SplashUiState
}

class SplashCoordinator(
    private val stateInitializer: AppStateInitializer,
    private val destination: BaseAppRoute
) {

    private val mutableState =
        MutableStateFlow<SplashUiState>(
            SplashUiState.Initializing,
        )
    val state: StateFlow<SplashUiState> =
        mutableState.asStateFlow()

    suspend fun initialize() {
        mutableState.value = SplashUiState.Initializing
        stateInitializer.initialize()
            .onSuccess {
                mutableState.value =
                    SplashUiState.Completed(destination)
            }
            .onFailure { throwable ->
                mutableState.value =
                    SplashUiState.Failed(
                        message = throwable.message
                            ?: "初始化失败",
                    )
            }
    }

}