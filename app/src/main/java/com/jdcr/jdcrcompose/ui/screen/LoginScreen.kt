package com.jdcr.jdcrcompose.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jdcr.jdcrcompose.R
import com.jdcr.jdcrcompose.auth.DemoAuthService
import com.jdcr.jdcrcompose.ui.component.BrandMark
import com.jdcr.navigation.common.auth.AuthLoginState
import com.jdcr.navigation.common.auth.AuthLoginType
import com.jdcr.navigation.common.auth.LoginOptions
import com.jdcr.navigation.common.auth.LoginReason

@Composable
fun LoginScreen(
    options: LoginOptions,
    state: AuthLoginState,
    onLogin: (AuthLoginType) -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var account by rememberSaveable { mutableStateOf(DemoAuthService.DEMO_ACCOUNT) }
    var password by rememberSaveable { mutableStateOf(DemoAuthService.DEMO_PASSWORD) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val isLoading = state is AuthLoginState.Loading
    val error = (state as? AuthLoginState.Error)?.throwable?.message
    val canSubmit = account.isNotBlank() && password.isNotBlank() && !isLoading
    val submit = {
        if (canSubmit) {
            onLogin(AuthLoginType.Account(account.trim(), password))
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandMark()
                    TextButton(
                        onClick = onLeave,
                        enabled = !isLoading,
                    ) {
                        Text(stringResource(R.string.leave_login))
                    }
                }
                Spacer(Modifier.height(56.dp))

                Text(
                    text = stringResource(R.string.login_eyebrow),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = when (options.reason) {
                        LoginReason.ProtectedRoute -> stringResource(R.string.login_for_protected_route)
                        LoginReason.SessionExpired -> stringResource(R.string.login_for_session_expired)
                        else -> stringResource(R.string.login_default_message)
                    },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text(stringResource(R.string.account)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    isError = error != null,
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = showPassword,
                        onCheckedChange = { showPassword = it },
                        enabled = !isLoading,
                    )
                    Text(
                        text = stringResource(R.string.show_password),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = canSubmit,
                ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Text(stringResource(R.string.logging_in))
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.login),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
